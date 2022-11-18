package com.taxreco.recon.reconciliationui.rest

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.taxreco.recon.reconciliationui.model.ReconciliationTriggeredEvent
import io.nats.client.Connection
import org.slf4j.LoggerFactory.getLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/api/v1/recon")
@RestController
class TriggerResource(
    private val connection: Connection,
    @Value("\${reconTriggerSubject}") private val reconTriggerSubject: String
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        @JvmStatic
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @PostMapping("/trigger/{jobId}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun trigger(@PathVariable("jobId") jobId: String): Map<String, String> {
        val r = ReconciliationTriggeredEvent(
            jobId = jobId,
            tenantId = "taxreco",
            reconSettingName = "Settings 1",
            reconSettingVersion = 1L,
            streamResults = true
        )
        println(" ----- >$reconTriggerSubject <--------")
        connection.jetStream().publish(
            reconTriggerSubject,
            jacksonObjectMapper().writeValueAsBytes(r)
        )
        return mapOf("jobId" to jobId)
    }
}