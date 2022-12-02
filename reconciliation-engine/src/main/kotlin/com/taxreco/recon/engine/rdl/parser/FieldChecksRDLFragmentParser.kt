package com.taxreco.recon.engine.rdl.parser

import com.taxreco.recon.engine.model.*
import java.util.*

class FieldChecksRDLFragmentParser : RDLFragmentParser {

    override fun parse(lineNo: Int, line: String, setting: ReconciliationSetting): List<RDLParseError> {
        if (line.contains(RulesetType.FieldChecks.name)) {
            val template =
                "DEFINE\\s+RULE\\s+(.*?)\\s+OF\\s+TYPE\\s+(.*?)\\s+AS\\s+\\((.*?)\\)\\s*TAGS\\s+WHEN\\s+MATCHED\\s+\\((.*?)\\)\\s*TAGS\\s+WHEN\\s+NOT\\s+MATCHED\\s+\\((.*?)\\)".trimIndent()
                    .toRegex()
            val matches = template.find(line.trim())
            val groups = matches?.groups

            if (groups != null) {
                if (groups.size != 6) {
                    listOf(RDLParseError(lineNo, "Invalid RDL"))
                } else {
                    val name = groups[1]!!.value
                    val type = RulesetType.valueOf(groups[2]!!.value.trim())
                    val query = groups[3]!!.value
                    val matchTags = groups[4]!!.value.split(",")
                    val unmatchTags = groups[5]!!.value.split(",")
                    var rs =
                        setting.rulesets.find { it.name.trim().lowercase() == name.trim().lowercase() }
                    if (rs == null) {
                        rs = MatchRuleSet()
                        setting.rulesets.add(rs)
                    }
                    rs.name = name
                    if (type == RulesetType.FieldChecks) {
                        rs.type = RulesetType.FieldChecks
                        rs.rules.add(
                            Rule(
                                id = UUID.randomUUID().toString(),
                                fieldChecks = FieldChecks(query),
                                tagsWhenMatched = matchTags.toMutableList(),
                                tagsWhenNotMatched = unmatchTags.toMutableList()
                            )
                        )
                    }
                }
            }
        }
        return emptyList()
    }
}