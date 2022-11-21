package com.taxreco.recon.reconciliationui.model

data class ReconciliationTriggeredEvent(
    val tenantId: String,
    val jobId: String,
    val rulesetTypes: List<RulesetType> = RulesetType.values().toList(),
    val startedAt: Long = System.currentTimeMillis(),
    val reconSettingName: String,
    val reconSettingVersion: Long,
    val streamResults: Boolean
)