package com.taxreco.recon.engine.service

import com.taxreco.recon.engine.model.*
import com.taxreco.recon.engine.service.Functions.MATCH_KEY_ATTRIBUTE
import org.springframework.stereotype.Service
import java.util.*
import kotlin.math.abs

@Service
class EntryWiseOneToManyComparisonService : RulesetEvaluationService {

    override fun match(
        reconciliationContext: ReconciliationContext,
        ruleSet: MatchRuleSet
    ) {
        if (supportedRulesetType() == ruleSet.type) {
            ruleSet.rules.forEach { rule ->
                val oneToManyChecks = rule.entriesOneToManyChecks!!
                val exp = oneToManyChecks.valueFieldA + " " + oneToManyChecks.valueFieldB
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

                if (oneToManyChecks.bucketFieldA != null) {
                    addGroups(oneToManyChecks.bucketFieldA, recordsA, groupsA)
                } else {
                    addGroups("\"1\"", recordsA, groupsA)
                }
                if (oneToManyChecks.bucketFieldB != null) {
                    addGroups(oneToManyChecks.bucketFieldB, recordsB, groupsB)
                } else {
                    addGroups("\"1\"", recordsB, groupsB)
                }

                // Check groups.
                groupsA.forEach { a ->
                    val elementsA = groupsA[a.key]?.map { it.attrs } ?: listOf()
                    val elementsB = groupsB[a.key]?.map { it.attrs } ?: listOf()
                    multimatch(
                        elementsA,
                        elementsB,
                        oneToManyChecks.valueFieldA.substringAfter("."),
                        oneToManyChecks.valueFieldB.substringAfter("."),
                        oneToManyChecks.valueTolerance
                    )
                }
                addMatchTags(rule, recordsA, recordsB)
            }
        }
    }

    private fun multimatch(
        a: List<MutableMap<String, Any?>>,
        b: List<MutableMap<String, Any?>>,
        oneFieldA: String,
        manyFieldB: String,
        tolerance: Double
    ) {
        a.forEach { one ->
            val valueA = one[oneFieldA]?.toString()?.toDoubleOrNull()
            if (valueA != null) {
                val arr = b
                    .filter {
                        it[MATCH_KEY_ATTRIBUTE] == null ||
                                it[MATCH_KEY_ATTRIBUTE]?.toString()?.startsWith("\$") == true
                    }
                    .mapNotNull { it[manyFieldB]?.toString()?.toDoubleOrNull() }
                val result = findElements(arr, valueA, tolerance)
                if (result.isNotEmpty()) {
                    val key = MatchKeyContext.keyFor()
                    one[MATCH_KEY_ATTRIBUTE] = key
                    result.map { b[it][MATCH_KEY_ATTRIBUTE] = key }
                }
            }
        }
    }

    private fun findElements(arr: List<Double>, n: Double, tolerance: Double = 0.0): List<Int> {
        val gathered = mutableListOf<Int>()
        parts(n, arr, gathered, 0.0, n, tolerance)
        return gathered
    }

    private fun parts(
        n: Double,
        arr: List<Double>,
        gathered: MutableList<Int>,
        acc: Double,
        target: Double = n,
        tolerance: Double
    ) {
        if (n <= 0) {
            if (acc != target && abs(acc - target) > tolerance) {
                gathered.clear()
            }
            return
        }
        val closest = when (val i = arr.indexOfFirst { it >= n }) {
            0 -> 0 to arr[0]
            arr.size - 1 -> arr.size - 1 to arr[arr.size - 1]
            -1 -> arr.size - 1 to arr[arr.size - 1]
            else -> i - 1 to arr[i - 1]
        }

        gathered.add(closest.first)
        parts(n - closest.second, arr, gathered, acc + closest.second, target, tolerance)
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
        return RulesetType.EntryWiseOneToManyChecks
    }
}