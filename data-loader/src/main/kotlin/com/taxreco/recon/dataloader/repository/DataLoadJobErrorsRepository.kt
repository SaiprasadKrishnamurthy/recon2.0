package com.taxreco.recon.dataloader.repository

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.taxreco.recon.dataloader.model.ApiUser
import com.taxreco.recon.dataloader.model.ErrorRow
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
}