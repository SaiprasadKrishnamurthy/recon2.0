package com.taxreco.recon.reconciliationui.config

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.taxreco.recon.reconciliationui.model.DisplayRecord
import com.taxreco.recon.reconciliationui.model.MatchResult
import com.taxreco.recon.reconciliationui.model.Rec
import com.taxreco.recon.reconciliationui.model.ReconciliationJobProgressEvent
import io.nats.client.Connection
import io.nats.client.Message
import io.nats.client.PushSubscribeOptions
import io.nats.client.api.StreamConfiguration
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.SimpMessagingTemplate


@Configuration
class NatsConsumerConfig(
    private val connection: Connection,
    private val websocket: SimpMessagingTemplate,
    @Value("\${reconStreamName}") private val reconStreamName: String,
    @Value("\${reconTriggerSubject}") private val reconTriggerSubject: String
) {

    val jacksonObjectMapper = jacksonObjectMapper()

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        @JvmStatic
        private val logger = LoggerFactory.getLogger(javaClass.enclosingClass)
    }

    @PostConstruct
    fun setup() {
        init()
        reconResultSubscriber()
        reconProgressSubscriber()
    }

    private fun formatMap(map: Map<String, Any>): String {
        return map.map { it.key + " = " + it.value.toString() }.joinToString(",  ")
    }

    private fun init() {
        try {
            connection.jetStreamManagement().addStream(
                StreamConfiguration.Builder()
                    .name(reconStreamName)
                    .addSubjects(
                        reconTriggerSubject,
                        "taxreco.reconresult",
                        "taxreco.progress"
                    )
                    .build()
            )
        } catch (ex: Exception) {
            connection.jetStreamManagement().updateStream(
                StreamConfiguration.Builder()
                    .name(reconStreamName)
                    .addSubjects(
                        reconTriggerSubject,
                        "taxreco.reconresult",
                        "taxreco.progress"
                    )
                    .build()
            )
        }
    }

    private fun reconResultSubscriber() {
        val messageHandler: (Message) -> Unit = { msg ->
            try {
                val event = jacksonObjectMapper.readValue(msg.data, MatchResult::class.java)

                val displayRecord = DisplayRecord(
                    matchKey = event.matchKey,
                    groupName = event.groupName,
                    bucketValue = event.bucketValue,
                    rulesetType = event.rulesetType,
                    tags = event.dataset.tags.joinToString(", "),
                    records = event.dataset.records.map { r ->
                        Rec(
                            r.key,
                            r.value.map { formatMap(it) })
                    },
                )
                websocket.convertAndSend(
                    "/topic/result/${event.jobId}",
                    displayRecord
                )
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
        val push = PushSubscribeOptions.Builder()
            .durable("recon-ui-result-1")
            .build()
        val dispatcher = connection.createDispatcher()
        connection.jetStream().subscribe("taxreco.reconresult", dispatcher, messageHandler, true, push)
    }

    private fun reconProgressSubscriber() {
        val messageHandler: (Message) -> Unit = { msg ->
            val event = jacksonObjectMapper.readValue(msg.data, ReconciliationJobProgressEvent::class.java)
            websocket.convertAndSend(
                "/topic/progress/${event.jobId}",
                event
            )
        }
        val push = PushSubscribeOptions.Builder()
            .durable("recon-ui-progress-1")
            .build()
        val dispatcher = connection.createDispatcher()
        connection.jetStream().subscribe("taxreco.progress", dispatcher, messageHandler, true, push)
    }
}