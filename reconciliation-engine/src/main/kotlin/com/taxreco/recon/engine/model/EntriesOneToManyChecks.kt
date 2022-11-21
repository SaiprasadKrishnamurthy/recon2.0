package com.taxreco.recon.engine.model

data class EntriesOneToManyChecks(
    val bucketFieldA: String? = null,
    val bucketFieldB: String? = null,
    val valueFieldA: String,
    val valueFieldB: String,
    val valueTolerance: Double = 0.0
)
