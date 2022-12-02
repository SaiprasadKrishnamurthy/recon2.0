package com.taxreco.recon.reconciliationui.parser

import com.taxreco.recon.reconciliationui.model.*
import java.util.*

class TotalsChecksRDLFragmentParser : RDLFragmentParser {

    override fun parse(lineNo: Int, line: String, setting: ReconciliationSetting): List<RDLParseError> {
        if (line.contains(RulesetType.TotalsChecks.name)) {
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
                    val tokens = query.split("[\\p{Punct}\\s&&[^_]&&[^.]]+".toRegex()).map { it.trim() }
                        .filter { it.isNotBlank() }
                    if (tokens.size != 7 && tokens.size != 4) {
                        return listOf(
                            RDLParseError(
                                lineNo,
                                "The totals checks must be of the format: For example: 'sales.invoiceNo == tdsLedger.invoiceNo AND valueWithinTolerance(sales.tdsOnSales, tdsLedger.tds, 1.0)'.\n No complex expressions are allowed."
                            )
                        )
                    }
                    var rs =
                        setting.rulesets.find { it.name.trim().lowercase() == name.trim().lowercase() }
                    if (rs == null) {
                        rs = MatchRuleSet()
                        setting.rulesets.add(rs)
                    }
                    rs.name = name
                    if (type == RulesetType.TotalsChecks) {
                        rs.type = RulesetType.TotalsChecks
                        if (tokens.size == 7) {
                            rs.rules.add(
                                Rule(
                                    id = UUID.randomUUID().toString(),
                                    totalsChecks = TotalsChecks(
                                        totalsBucketFields = listOf(tokens[0], tokens[1]),
                                        totalsOnFields = listOf(tokens[4], tokens[5]),
                                        totalsTolerance = tokens[6].trim().toDouble()
                                    ),
                                    tagsWhenMatched = matchTags.toMutableList(),
                                    tagsWhenNotMatched = unmatchTags.toMutableList()
                                )
                            )
                        } else if (tokens.size == 4) {
                            rs.rules.add(
                                Rule(
                                    id = UUID.randomUUID().toString(),
                                    totalsChecks = TotalsChecks(
                                        totalsOnFields = listOf(tokens[1], tokens[2]),
                                        totalsTolerance = tokens[3].trim().toDouble()
                                    ),
                                    tagsWhenMatched = matchTags.toMutableList(),
                                    tagsWhenNotMatched = unmatchTags.toMutableList()
                                )
                            )
                        }
                    }
                }
            }
        }
        return emptyList()
    }
}