package com.taxreco.recon.dataloader.model

data class DataLoadJobInfo(
    val jobId: String,
    val tenant: String,
    val dataDefinitions: DataDefinitions,
    val name: String,
    val started: Long = System.currentTimeMillis(),
    val ended: Long? = null,
    val chunkName: String,
    val errorMsg: String? = null,
    val userId: String
)