package com.taxreco.recon.reconciliationui.model

data class MatchRuleSet(
    val name: String,
    val type: RulesetType,
    val rules: List<Rule>
)