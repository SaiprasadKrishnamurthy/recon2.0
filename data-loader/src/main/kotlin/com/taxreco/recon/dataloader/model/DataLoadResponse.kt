package com.taxreco.recon.dataloader.model

import com.fasterxml.jackson.annotation.JsonInclude
import java.util.*

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class DataLoadResponse(
    val jobId: String = UUID.randomUUID().toString(),
    val name: String,
    val tags: List<String> = emptyList(),
    val errors: List<ApiError> = emptyList()
)

