package com.taxreco.recon.dataloader.repository

import com.taxreco.recon.dataloader.model.ApiUser
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class ApiUserRepository(private val jdbcTemplate: JdbcTemplate) {
    fun findByClientIdAndApiKey(clientId: String, apiKey: String): ApiUser? {
        return try {
            val sql = """
                select * from api_user where client_id='$clientId' and api_key='$apiKey'
            """.trimIndent()
            val row = jdbcTemplate.queryForMap(sql)
            val user = ApiUser()
            user.id = row["id"].toString()
            user.tenant = row["tenant"].toString()
            user.objectStorageId = row["object_storage_id"].toString()
            user
        } catch (ex: Exception) {
            null
        }
    }
}