package com.taxreco.recon.engine.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class Rule(
    var id: String,
    var fieldChecks: FieldChecks? = null,
    var totalsChecks: TotalsChecks? = null,
    var entriesChecks: EntriesChecks? = null,
    var entriesOneToManyChecks: EntriesOneToManyChecks? = null,
    var tagsWhenMatched: MutableList<String>,
    var tagsWhenNotMatched: MutableList<String>
)