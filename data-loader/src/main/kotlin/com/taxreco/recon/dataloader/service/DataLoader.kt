package com.taxreco.recon.dataloader.service

import com.taxreco.recon.dataloader.messaging.MessageSender
import com.taxreco.recon.dataloader.model.DataLoadRequest
import com.taxreco.recon.dataloader.model.DataLoadService
import org.springframework.stereotype.Component

@Component
class DataLoader(
    private val dataLoadService: DataLoadService,
    private val messageSender: MessageSender
) {
    fun submitDataLoadJob(dataLoadRequest: DataLoadRequest) {
        dataLoadService.submitDataLoadJob(dataLoadRequest)
            .forEach { messageSender.sendDataLoadEvent(it) }
    }
}