package com.taxreco.recon.reconciliationui.parser

import com.taxreco.recon.reconciliationui.model.*


class DatasourceRDLFragmentParser : RDLFragmentParser {

    override fun parse(lineNo: Int, line: String, setting: ReconciliationSetting): List<RDLParseError> {
        val template = "DEFINE\\s+DATASOURCE\\s+(.*?)\\s+WITH\\s+BUCKET\\s+FIELD\\s+(.*?)\\s+AS\\s+\\((.*?)\\)".toRegex()
        val matches = template.find(line.trim())
        val groups = matches?.groups
        val datasourceId = groups?.get(1)
        val bucketField = groups?.get(2)
        val predicate = groups?.get(3)


        if (datasourceId != null && bucketField != null && predicate != null) {
            setting.dataSources.add(
                DataSource(
                    id = datasourceId.value,
                    bucketField = bucketField.value,
                    predicate = predicate.value
                )
            )
        } else {
            val errorMsg =
                "The data source definition must be in this format \n DEFINE DATASOURCE <datasource name> WITH BUCKET FIELD <bucket field> AS (<filter clause>) \n"
            return listOf(RDLParseError(lineNo, errorMsg))
        }
        return emptyList()
    }
}