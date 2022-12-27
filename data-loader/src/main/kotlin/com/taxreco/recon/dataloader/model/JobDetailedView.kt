package com.taxreco.recon.dataloader.model

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class ChunkInfo(val chunkName: String, val status: DataLoadJobStatus, val errorMsg: String? = null)

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class JobDetailedView(
    val jobId: String,
    var name: String? = null,
    var user: String? = null,
    var startedMillis: Long? = null,
    var endedMillis: Long? = null,
    var definitions: DataDefinitions? = null,
    var chunks: MutableList<ChunkInfo> = mutableListOf()
)
