package com.taxreco.recon.engine.model

data class TotalsChecks(
    val totalsBucketFields: List<String> = emptyList(),
    val totalsOnFields: List<String> = emptyList(),
    val totalsTolerance: Double = 0.0
)
