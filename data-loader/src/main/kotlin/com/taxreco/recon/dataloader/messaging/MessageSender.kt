package com.taxreco.recon.dataloader.messaging

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.taxreco.recon.dataloader.model.DataLoadEvent
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Component


@Component
class MessageSender(
    private val queueMessagingTemplate: QueueMessagingTemplate,
    @Value("\${dataload.event.queue}") private val dataLoadEventQueue: String
) {
    fun sendDataLoadEvent(dataLoadEvent: DataLoadEvent) {
        val headers: MutableMap<String, Any> = HashMap()

        queueMessagingTemplate.convertAndSend(
            dataLoadEventQueue,
            MessageBuilder.withPayload(jacksonObjectMapper().writeValueAsString(dataLoadEvent))
                .build(),
            headers
        )
    }
}