package com.taxreco.recon.dataloader.model

import com.fasterxml.jackson.annotation.JsonInclude
import java.util.*

data class DataLoadRequest(
    val jobId: String = UUID.randomUUID().toString(),
    val name: String,
    val tags: List<String> = emptyList(),
    val zipFilePath: String,
    val apiUser: ApiUser,
    val dataDefinitions: DataDefinitions
)

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class DataDefinitions(
    val idField: String? = null,
    val keyFields: List<String> = emptyList(),
    var tags: List<String> = emptyList(),
    val fieldDefinitions: Map<String, FieldType>
)

enum class FieldType(val dbType: String) {
    number("decimal"),
    string("varchar(200)"),
    text("text"),
    date("date"),
    boolean("boolean"),
    long("bigint")
}
