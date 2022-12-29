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
    val definitions: Map<String, FieldType>,
    val transformations: Map<String, DataTransformation> = emptyMap(),
    val validations: List<ValidationRule> = emptyList()
)

data class ValidationRule(val rule: String, val errorMessageIfRuleFails: String)

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class DataTransformation(val precondition: String?, val function: String, val defaultValue: String?)

data class ErrorRow(val row: Map<String, Any?>, val errors: List<String>)

enum class FieldType(val dbType: String) {
    number("decimal"),
    string("varchar(200)"),
    text("text"),
    date("date"),
    boolean("boolean"),
    long("bigint")
}
