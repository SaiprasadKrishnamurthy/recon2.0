package com.taxreco.recon.engine.service

import com.taxreco.recon.engine.model.*
import com.taxreco.recon.engine.util.Constants.MATCH_KEY_ATTRIBUTE
import org.springframework.stereotype.Service
import java.util.*
import kotlin.math.abs

@Service
class TotalsWiseComparisonService : RulesetEvaluationService {

    override fun match(
        reconciliationContext: ReconciliationContext,
        ruleSet: MatchRuleSet
    ) {
        if (supportedRulesetType() == ruleSet.type) {
            ruleSet.rules.forEach { rule ->
                val totalsChecks = rule.totalsChecks!!
                val exp = totalsChecks.totalsOnFields.joinToString(" ")
                val tokens = exp.split("[\\p{Punct}\\s&&[^_]]+".toRegex())
                val datasources = tokens
                    .filter { reconciliationContext.reconciliationSetting.dataSources.map { d -> d.id }.contains(it) }
                    .distinct()

                val recordsA = reconciliationContext.transactionRecords[datasources[0]]
                    ?.map {
                        if (!it.attrs.containsKey(MATCH_KEY_ATTRIBUTE)) {
                            it.attrs[MATCH_KEY_ATTRIBUTE] = "\$${UUID.randomUUID()}"
                        }
                        it
                    }?.filter { it.attrs[MATCH_KEY_ATTRIBUTE].toString().startsWith("$") }
                    ?: emptyList()
                val recordsB = reconciliationContext.transactionRecords[datasources[1]]
                    ?.map {
                        if (!it.attrs.containsKey(MATCH_KEY_ATTRIBUTE)) {
                            it.attrs[MATCH_KEY_ATTRIBUTE] = "\$${UUID.randomUUID()}"
                        }
                        it
                    }?.filter { it.attrs[MATCH_KEY_ATTRIBUTE].toString().startsWith("$") }
                    ?: emptyList()

                val groupsA = mutableMapOf<String, MutableList<TransactionRecord>>()
                val groupsB = mutableMapOf<String, MutableList<TransactionRecord>>()

                if (totalsChecks.totalsBucketFields.size == 2) {
                    addGroups(totalsChecks.totalsBucketFields[0], recordsA, groupsA)
                    addGroups(totalsChecks.totalsBucketFields[1], recordsB, groupsB)
                } else {
                    addGroups("\"1\"", recordsA, groupsA)
                    addGroups("\"1\"", recordsB, groupsB)
                }

                // Check groups.
                groupsA.forEach { a ->
                    val _rule = rule.copy()
                    val elementsA = groupsA[a.key]
                    val elementsB = groupsB[a.key]
                    val totalOnFieldA = totalsChecks.totalsOnFields[0].substringAfter(".")
                    val totalOnFieldB = totalsChecks.totalsOnFields[1].substringAfter(".")
                    val sumA = elementsA?.sumOf {
                        it.attrs[totalOnFieldA]?.toString()?.toDouble() ?: 0.0
                    }
                    val sumB = elementsB?.sumOf {
                        it.attrs[totalOnFieldB]?.toString()?.toDouble() ?: 0.0
                    }
                    var matchKey = UUID.randomUUID().toString()
                    if (sumA != null && sumB != null && abs(sumA - sumB) <= _rule.totalsChecks!!.totalsTolerance) {
                        elementsA.filter {
                            !it.attrs.containsKey(MATCH_KEY_ATTRIBUTE) || it.attrs[MATCH_KEY_ATTRIBUTE].toString()
                                .contains("$")
                        }.forEach { it.attrs[MATCH_KEY_ATTRIBUTE] = matchKey }
                        elementsB.filter {
                            !it.attrs.containsKey(MATCH_KEY_ATTRIBUTE) || it.attrs[MATCH_KEY_ATTRIBUTE].toString()
                                .contains("$")
                        }.forEach { it.attrs[MATCH_KEY_ATTRIBUTE] = matchKey }
                    } else {
                        matchKey = "\$${matchKey}"
                        elementsA?.forEach { it.attrs[MATCH_KEY_ATTRIBUTE] = matchKey }
                        elementsB?.forEach { it.attrs[MATCH_KEY_ATTRIBUTE] = matchKey }
                    }
                }
                addMatchTags(rule, recordsA, recordsB)
            }
        }
    }

    private fun addGroups(
        expression: String,
        records: List<TransactionRecord>,
        groups: MutableMap<String, MutableList<TransactionRecord>>
    ) {
        records.forEach { a ->
            val result = evalExpression(expression, a.name, a.attrs)
            if (result != null) {
                if (groups.containsKey(result.toString())) {
                    groups[result.toString()]?.add(a)
                } else {
                    groups[result.toString()] = mutableListOf(a)
                }
            }
        }
    }

    private fun evalExpression(
        expression: String,
        name: String,
        attrs: MutableMap<String, Any?>
    ): Any? {
        val vars = mutableMapOf(name to attrs)
        val expr = cleanseExpr(expression, vars)
        return evaluateToObj(expr, vars)
    }


    override fun supportedRulesetType(): RulesetType {
        return RulesetType.TotalsChecks
    }
}