package com.taxreco.recon.engine.model

import com.taxreco.recon.engine.service.F
import com.taxreco.recon.engine.service.Functions.MATCH_KEY_ATTRIBUTE
import org.mvel2.MVEL

interface RulesetEvaluationService {
    fun match(reconciliationContext: ReconciliationContext, ruleSet: MatchRuleSet)
    fun supportedRulesetType(): RulesetType

    fun addMatchTags(rule: Rule, recordsA: List<TransactionRecord>, recordsB: List<TransactionRecord>) {
        listOf(recordsB, recordsA)
            .flatten()
            .forEach { r ->
                if (!r.attrs.containsKey(MATCH_KEY_ATTRIBUTE) ||
                    r.attrs[MATCH_KEY_ATTRIBUTE]?.toString()?.contains("$") == true
                ) {
                    r.matchTags.addAll(rule.tagsWhenNotMatched)
                } else {
                    r.matchTags.addAll(rule.tagsWhenMatched)
                }
            }
    }

    fun cleanseExpr(s: String, vars: MutableMap<String, MutableMap<String, Any?>>): String {
        val expr = s.replace(" and ", "&&")
            .replace(" AND ", "&&")
            .replace(" OR ", "||")
            .replace(" or ", "||")
        val regex = "\\w+\\.\\w+".toRegex()
        val x = regex.findAll(expr)
        // Fill all mandatory fields in the context
        x.toList()
            .map { it.value }
            .forEach { token ->
                val key = token.substringBefore(".")
                val value = token.substringAfter(".")
                if (!vars.containsKey(key)) {
                    vars[key] = mutableMapOf()
                }
                if (!vars[key]!!.containsKey(value)) {
                    vars[key]!![value] = null
                }
            }

        return expr
    }

    fun evaluate(
        expr: String,
        vars: Map<String, Map<String, Any?>>
    ): Boolean {
        try {
            val compiled = MVEL.compileExpression(expr)
            return MVEL.executeExpression(compiled, F(), vars) as Boolean
        } catch (ex: Exception) {
            ex.printStackTrace()
            if (ex.message?.contains("could not access:") == true) {
                return false
            }
            throw ex
        }
    }

    fun evaluateToObj(
        expr: String,
        vars: Map<String, Map<String, Any?>>
    ): Any? {
        try {
            val compiled = MVEL.compileExpression(expr)
            return MVEL.executeExpression(compiled, F(), vars)
        } catch (ex: Exception) {
            if (ex.message?.contains("could not access:") == true) {
                return null
            }
            throw ex
        }
    }
}