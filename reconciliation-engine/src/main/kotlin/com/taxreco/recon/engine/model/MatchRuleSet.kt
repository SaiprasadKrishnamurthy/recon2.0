package com.taxreco.recon.engine.model

data class MatchRuleSet(
    var name: String = "",
    var type: RulesetType = RulesetType.EntryWiseOneToOneChecks,
    var rules: MutableList<Rule> = mutableListOf()
)