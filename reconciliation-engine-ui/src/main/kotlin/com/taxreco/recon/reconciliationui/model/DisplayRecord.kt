package com.taxreco.recon.reconciliationui.model

data class Rec(val datasource: String, val rows: List<String>)
data class DisplayRecord(
    val matchKey: String,
    val groupName: String,
    val bucketValue: String,
    val tags: String,
    val rulesetType: RulesetType?,
    val records: List<Rec> = emptyList()
) {
    val tagsDisplay: String
    get() = tags.replace("_", " ")
}