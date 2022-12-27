package com.taxreco.recon.dataloader.model

interface DataLoadService {
    fun submitDataLoadJob(dataLoadRequest: DataLoadRequest): List<DataLoadEvent>
    fun loadData(dataLoadEvent: DataLoadEvent)
}