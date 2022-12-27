package com.taxreco.recon.dataloader.model

enum class DataLoadJobStatus {
    ChunkCreated, ChunkUploadedToStore, ChunkLoadedInDB, ChunkFailedToLoadInDB
}