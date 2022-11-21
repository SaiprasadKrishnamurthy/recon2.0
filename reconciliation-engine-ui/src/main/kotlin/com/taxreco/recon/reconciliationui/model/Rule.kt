package com.taxreco.recon.reconciliationui.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class Rule(
    val id: String,
    val fieldChecks: FieldChecks? = null,
    val totalsChecks: TotalsChecks? = null,
    val entriesChecks: EntriesChecks? = null,
    val entriesOneToManyChecks: EntriesOneToManyChecks? = null,
    val tagsWhenMatched: List<String>,
    val tagsWhenNotMatched: List<String>
)