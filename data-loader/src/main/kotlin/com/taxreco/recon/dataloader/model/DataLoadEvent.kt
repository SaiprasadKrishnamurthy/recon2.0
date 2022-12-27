package com.taxreco.recon.dataloader.model

data class DataLoadEvent(
    val attemptNo: Int = 1,
    val jobId: String,
    val tenant: String,
    val objectStorageId: String,
    val apiUser: ApiUser,
    val name: String,
    val tags: List<String>,
    val chunkName: String,
    val dataDefinitions: DataDefinitions
)
