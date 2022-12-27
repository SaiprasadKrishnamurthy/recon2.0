package com.taxreco.recon.dataloader.listener

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.taxreco.recon.dataloader.model.DataLoadEvent
import com.taxreco.recon.dataloader.model.DataLoadService
import com.taxreco.recon.dataloader.repository.DataLoadJobInfoRepository
import org.slf4j.LoggerFactory
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener
import org.springframework.stereotype.Component

@Component
class DataLoadEventListener(
    private val dataLoadJobInfoRepository: DataLoadJobInfoRepository,
    private val dataLoadService: DataLoadService
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        @JvmStatic
        private val logger = LoggerFactory.getLogger(javaClass.enclosingClass)
    }

    @SqsListener("\${dataload.event.queue}")
    fun onDataLoadEvent(raw: String) {
        val jacksonObjectMapper = jacksonObjectMapper()
        val payload = jacksonObjectMapper.readValue(raw, Map::class.java)["payload"]
        val event = jacksonObjectMapper.readValue(payload.toString(), DataLoadEvent::class.java)
        dataLoadService.loadData(event)
    }
}