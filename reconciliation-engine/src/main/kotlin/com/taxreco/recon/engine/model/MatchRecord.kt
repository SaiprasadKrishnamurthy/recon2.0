package com.taxreco.recon.engine.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Sharded

@Document(collection = "matchRecord")
@Sharded(shardKey = ["jobId"])
data class MatchRecord(
    @Id
    val id: String,
    val originalRecordId: String,
    val groupName: String,
    val jobId: String,
    val rulesetType: RulesetType,
    var tags: MutableSet<String> = mutableSetOf(),
    val bucketKey: String,
    val bucketValue: String,
    val matchKey: String,
    val record: MutableMap<String, Any?>,
    val datasource: String
)