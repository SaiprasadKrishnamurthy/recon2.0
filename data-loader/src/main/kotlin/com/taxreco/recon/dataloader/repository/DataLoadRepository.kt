package com.taxreco.recon.dataloader.repository

import com.fasterxml.jackson.databind.MappingIterator
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.taxreco.recon.dataloader.ftp.S3Manager
import com.taxreco.recon.dataloader.model.ApiUser
import com.taxreco.recon.dataloader.model.DataDefinitions
import com.taxreco.recon.dataloader.model.DataLoadEvent
import com.taxreco.recon.dataloader.model.FieldType
import com.taxreco.recon.dataloader.util.DateUtil
import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.BatchPreparedStatementSetter
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.io.File
import java.sql.Date
import java.sql.PreparedStatement
import java.util.*


@Repository
class DataLoadRepository(
    private val jdbcTemplate: JdbcTemplate,
    private val s3Manager: S3Manager
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        @JvmStatic
        private val logger = LoggerFactory.getLogger(javaClass.enclosingClass)
    }

    fun setupTables(event: DataLoadEvent) {
        val tableName = event.name
        val colDefs = event.dataDefinitions.fieldDefinitions.map { fd ->
            val fieldName = normalise(fd.key)
            if (fd.key == event.dataDefinitions.idField) {
                fieldName + " " + fd.value.dbType + " primary key"
            } else {
                fieldName + " " + fd.value.dbType
            }
        }.joinToString(", ")
        val createTable = """
            create table if not exists ${event.tenant}.${tableName}(
               $colDefs
            )
        """.trimIndent()
        jdbcTemplate.update(createTable)
    }

    fun setupIndices(event: DataLoadEvent) {
        event.dataDefinitions.keyFields.forEach { key ->
            val sql = """
                create index if not exists "${event.tenant}_${event.name}_${normalise(key)}" on ${event.tenant}.${event.name} using btree (${
                normalise(
                    key
                )
            })
            """.trimIndent()
            logger.info(" Creating index: $sql")
            //jdbcTemplate.update(sql)
        }
    }

    fun dropIndices(apiUser: ApiUser, name: String, dataDefinitions: DataDefinitions) {
        dataDefinitions.keyFields.forEach { key ->
            val sql = """
                drop index if exists "${apiUser.tenant}_${name}_${normalise(key)}"
            """.trimIndent()
            jdbcTemplate.update(sql)
            logger.info(" Dropping index: $sql")
        }
    }

    fun loadData(event: DataLoadEvent) {
        val workDir = File(UUID.randomUUID().toString())
        FileUtils.forceMkdir(workDir)
        try {
            val s3Folder = "data_load/${event.jobId}"
            val downloadedFile = s3Manager.downloadFileFromDir(event.apiUser, s3Folder, event.chunkName, workDir)

            downloadedFile?.let { load(event, it) }
        } finally {
            FileUtils.deleteQuietly(workDir)
        }
    }

    private fun load(event: DataLoadEvent, file: File) {
        val mapper = CsvMapper()
        val schema = CsvSchema.emptySchema().withHeader().withColumnSeparator('\t')
        val iterator: MappingIterator<Map<String, String>> = mapper.readerFor(MutableMap::class.java)
            .with(schema)
            .readValues(file)
        val keys = event.dataDefinitions.fieldDefinitions.keys.toList()
        val keysNormalised = event.dataDefinitions.fieldDefinitions.keys.map { normalise(it) }.toList()
        val placeHolders = event.dataDefinitions.fieldDefinitions.keys.map { "?" }

        val sql = """
            insert into ${event.tenant}.${event.name} (${keysNormalised.joinToString(",")}) values (${
            placeHolders.joinToString(
                ","
            )
        }) 
        ON CONFLICT ON CONSTRAINT ${event.name}_pkey  
        DO UPDATE SET 
        ${keysNormalised.joinToString(", ") { "$it=excluded.$it" }}
        """.trimIndent()

        val rows = mutableListOf<Map<String, Any?>>()
        iterator.forEach { rows.add(it) }

        if (event.dataDefinitions.idField != null) {
            rows.sortBy { it[event.dataDefinitions.idField].toString() }
        }

        try {
            val updated = jdbcTemplate.batchUpdate(sql, object : BatchPreparedStatementSetter {
                override fun getBatchSize(): Int {
                    return rows.size
                }

                override fun setValues(ps: PreparedStatement, i: Int) {
                    val entry = rows[i]
                    keys.forEachIndexed { colidx, key ->
                        if (event.dataDefinitions.fieldDefinitions[key] == FieldType.date) {
                            if (entry[key]?.toString() != null) {
                                ps.setDate(colidx + 1, Date.valueOf(DateUtil.parseDate(entry[key].toString())))
                            } else {
                                ps.setDate(colidx + 1, null)
                            }
                        } else if (event.dataDefinitions.fieldDefinitions[key] == FieldType.number) {
                            ps.setDouble(colidx + 1, entry[key].toString().trim().toDoubleOrNull() ?: 0.0)
                        } else if (event.dataDefinitions.fieldDefinitions[key] == FieldType.boolean) {
                            ps.setBoolean(colidx + 1, entry[key]?.toString()?.toBoolean() ?: false)
                        } else {
                            ps.setString(colidx + 1, entry[key]?.toString())
                        }
                    }
                }
            })
            println(" Updated: ${updated.size}")
        } catch (ex: Exception) {
            throw ex
        }
    }

    private fun normalise(key: String): String {
        val re = Regex("[^A-Za-z0-9]")
        return re.replace(key, "").toLowerCase()
    }
}