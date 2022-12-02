package com.taxreco.recon.reconciliationui.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

@Document("reconciliationSetting")
data class ReconciliationSetting(
    @Id
    var id: String = UUID.randomUUID().toString(),
    var name: String = "",
    var version: Long = System.currentTimeMillis(),
    var created: Long = System.currentTimeMillis(),
    var owner: String = "",
    var dataSources: MutableList<DataSource> = mutableListOf(),
    var group: String = "",
    var rulesets: MutableList<MatchRuleSet> = mutableListOf()
)