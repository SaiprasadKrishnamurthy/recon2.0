package com.taxreco.recon.dataloader.repository

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.taxreco.recon.dataloader.model.*
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class DataLoadJobInfoRepository(private val jdbcTemplate: JdbcTemplate) {

    fun save(apiUser: ApiUser, dataLoadJobInfo: DataLoadJobInfo) {
        val sql = """
            insert into data_load_job_info(job_id, data_name, started_millis, chunk_name, status, user_id, tenant, data_definition) 
            values (?,?,?,?,?,?,?,?)
        """.trimIndent()
        jdbcTemplate.update(sql) { ps ->
            ps.setString(1, dataLoadJobInfo.jobId)
            ps.setString(2, dataLoadJobInfo.name)
            ps.setLong(3, dataLoadJobInfo.started)
            ps.setString(4, dataLoadJobInfo.chunkName)
            ps.setString(5, DataLoadJobStatus.ChunkCreated.name)
            ps.setString(6, apiUser.id)
            ps.setString(7, apiUser.tenant)
            ps.setString(8, jacksonObjectMapper().writeValueAsString(dataLoadJobInfo.dataDefinitions))
        }
    }


    fun updateStatus(
        apiUser: ApiUser,
        chunkName: String,
        jobId: String,
        dataLoadJobStatus: DataLoadJobStatus,
        ended: Boolean = false
    ) {
        val sql = """
            update data_load_job_info set status = ?, ended_millis=? where job_id=? and  chunk_name = ?
        """.trimIndent()
        jdbcTemplate.update(sql) { ps ->
            ps.setString(1, dataLoadJobStatus.name)
            ps.setLong(2, if (ended) System.currentTimeMillis() else -1)
            ps.setString(3, jobId.trim())
            ps.setString(4, chunkName.trim())
        }
    }

    fun updateStatusWithError(
        apiUser: ApiUser,
        chunkName: String,
        jobId: String,
        dataLoadJobStatus: DataLoadJobStatus,
        error: String
    ) {
        val sql = """
            update data_load_job_info set status = ?, ended_millis=?, error_msg=? where job_id=? and  chunk_name = ?
        """.trimIndent()
        jdbcTemplate.update(sql) { ps ->
            ps.setString(1, dataLoadJobStatus.name)
            ps.setLong(2, System.currentTimeMillis())
            ps.setString(3, error.trim())
            ps.setString(4, jobId.trim())
            ps.setString(5, chunkName.trim())
        }
    }

    fun allChunksLoadedInDB(apiUser: ApiUser, chunkName: String, jobId: String): Boolean {
        val sql = """
            select count(*) as a from data_load_job_info where job_id='${jobId}' and status != '${DataLoadJobStatus.ChunkLoadedInDB.name}'
        """.trimIndent()
        val updated = jdbcTemplate.queryForMap(sql)
        return updated["a"].toString().toInt() == 0
    }

    fun findAllJobs(apiUser: ApiUser, limit: Int): List<JobListView> {
        val sql = """
            with a as (
            select job_id, max(started_millis) x, status from data_load_job_info where tenant = '${apiUser.tenant}' group by job_id, status 
            ) select * from a order by x desc limit $limit
        """.trimIndent()
        val rows = jdbcTemplate.queryForList(sql)
        val groupedByJobId = rows.groupBy { it["job_id"].toString() }
            .mapValues { it.value.map { x -> DataLoadJobStatus.valueOf(x["status"].toString()) } }
        return rows.map { row ->
            var state = JobState.Inprogress
            if (groupedByJobId[row["job_id"].toString()]!!.contains(DataLoadJobStatus.ChunkFailedToLoadInDB)) {
                state = JobState.Failed
            } else if (groupedByJobId[row["job_id"].toString()]!!.all { it == DataLoadJobStatus.ChunkLoadedInDB }) {
                state = JobState.Success
            }
            JobListView(jobId = row["job_id"].toString(), startedMillis = row["x"].toString().toLong(), state = state)
        }
    }

    fun findJobDetail(apiUser: ApiUser, jobId: String): JobDetailedView? {
        val sql = """
            select * from data_load_job_info where job_id='${jobId}' and tenant='${apiUser.tenant}'
        """.trimIndent()
        val rows = jdbcTemplate.queryForList(sql)
        val jdv = JobDetailedView(jobId = jobId)
        rows.map { row ->
            val name = row["data_name"].toString()
            val userId = row["user_id"].toString()
            val started = row["started_millis"].toString().toLong()
            val ended = row["ended_millis"]?.toString()?.toLong() ?: -1L
            val chunkName = row["chunk_name"].toString()
            val status = row["status"].toString()
            val definition =
                jacksonObjectMapper().readValue(row["data_definition"].toString(), DataDefinitions::class.java)
            val error = row["error_msg"]?.toString()
            jdv.name = name
            jdv.user = userId
            jdv.startedMillis = started
            jdv.endedMillis = ended
            jdv.definitions = definition
            jdv.name = name
            jdv.chunks.add(ChunkInfo(chunkName, DataLoadJobStatus.valueOf(status), error))
        }
        return if (jdv.chunks.isEmpty())
            null
        else
            jdv
    }
}