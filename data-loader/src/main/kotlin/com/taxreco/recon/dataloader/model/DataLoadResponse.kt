package com.taxreco.recon.dataloader.model

import java.util.*

data class DataLoadResponse(
    val jobId: String = UUID.randomUUID().toString(),
    val name: String,
    val tags: List<String> = emptyList(),
    val errors: List<ApiError> = emptyList()
)

