package com.taxreco.recon.reconciliationui.rest

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.taxreco.recon.reconciliationui.model.ReconciliationSetting
import com.taxreco.recon.reconciliationui.model.ReconciliationTriggeredEvent
import com.taxreco.recon.reconciliationui.model.RulesetType
import io.nats.client.Connection
import org.slf4j.LoggerFactory.getLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

@RequestMapping("/api/v1/recon")
@RestController
class TriggerResource(
    private val connection: Connection,
    private val mongoTemplate: MongoTemplate,
    @Value("\${reconTriggerSubject}") private val reconTriggerSubject: String
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        @JvmStatic
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @PostMapping("/trigger/{jobId}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun trigger(
        @PathVariable("jobId") jobId: String,
        @RequestParam("checksTypes", required = false, defaultValue = "") checksTypes: String
    ): Map<String, String> {
        var types = listOf<RulesetType>()
        try {
            types = checksTypes.split(",")
                .map { RulesetType.valueOf(it.trim()) }
                .toList()
        } catch (ex: Exception) {
        }

        val r = ReconciliationTriggeredEvent(
            jobId = jobId,
            tenantId = "taxreco",
            rulesetTypes = types.ifEmpty { RulesetType.values().toList() },
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

    @GetMapping("/setting", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun settings(): ReconciliationSetting {
        return mongoTemplate.findAll(ReconciliationSetting::class.java).first { it.name == "Settings 1" }
    }
}