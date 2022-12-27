package com.taxreco.recon.dataloader.model

enum class JobState {
    Inprogress, Failed, Success
}

data class JobListView(
    val jobId: String,
    val startedMillis: Long,
    val state: JobState
)
