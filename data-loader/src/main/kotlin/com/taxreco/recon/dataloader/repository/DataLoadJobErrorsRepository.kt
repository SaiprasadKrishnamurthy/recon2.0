package com.taxreco.recon.dataloader.repository

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.taxreco.recon.dataloader.model.ApiUser
import com.taxreco.recon.dataloader.model.ErrorRow
import com.taxreco.recon.dataloader.model.ErrorSummaryView
import org.springframework.jdbc.core.BatchPreparedStatementSetter
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.PreparedStatement

@Repository
class DataLoadJobErrorsRepository(private val jdbcTemplate: JdbcTemplate) {

    fun save(jobId: String, dataname: String, chunkNme: String, apiUser: ApiUser, errors: List<ErrorRow>) {
        val sql = """
            insert into data_load_job_errors(job_id, data_name, tenant, rowJson, error_msgs, chunk_name) 
            values (?,?,?,?,?,?)
        """.trimIndent()

        jdbcTemplate.batchUpdate(sql, object : BatchPreparedStatementSetter {
            override fun getBatchSize(): Int {
                return errors.size
            }

            override fun setValues(ps: PreparedStatement, i: Int) {
                val entry = errors[i]
                ps.setString(1, jobId.trim())
                ps.setString(2, dataname.trim())
                ps.setString(3, apiUser.tenant.trim())
                ps.setString(4, jacksonObjectMapper().writeValueAsString(entry.row))
                val array =
                    ps.connection.createArrayOf("TEXT", entry.errors.toTypedArray())
                ps.setArray(5, array)
                ps.setString(6, chunkNme)


            }
        })
    }

    fun findErrorSummary(jobId: String, apiUser: ApiUser): ErrorSummaryView? {
        val sql = """
            select data_name, count(*) from data_load_job_errors where job_id='$jobId' group by data_name
        """.trimIndent()

        return jdbcTemplate.queryForList(sql)
            .map {
                ErrorSummaryView(jobId, it["data_name"].toString(), it["count"].toString().toInt())
            }.firstOrNull()
    }

    fun findErrorDetails(jobId: String, apiUser: ApiUser): List<LinkedHashMap<String, Any?>> {
        val sql = """
            select * from data_load_job_errors where job_id='$jobId'
        """.trimIndent()

        val j = jacksonObjectMapper()
        val results = mutableListOf<LinkedHashMap<String, Any?>>()
        jdbcTemplate.query(sql) {
            val record = j.readValue(it.getString("rowjson"), Map::class.java) as Map<String, Any?>
            val errors = it.getArray("error_msgs").array as Array<String>
            val element = linkedMapOf<String, Any?>()
            element["jobId"] = it.getString("job_id")
            element["data"] = it.getString("data_name")
            element["errors"] = errors
            element.putAll(record)
            results.add(element)
        }
        return results
    }
}