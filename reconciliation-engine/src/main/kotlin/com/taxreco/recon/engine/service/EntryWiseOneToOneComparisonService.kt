package com.taxreco.recon.engine.service

import com.taxreco.recon.engine.model.MatchRuleSet
import com.taxreco.recon.engine.model.ReconciliationContext
import com.taxreco.recon.engine.model.RulesetEvaluationService
import com.taxreco.recon.engine.model.RulesetType
import com.taxreco.recon.engine.util.Constants.MATCH_KEY_ATTRIBUTE
import org.springframework.stereotype.Service
import java.util.*

@Service
class EntryWiseOneToOneComparisonService : RulesetEvaluationService {

    override fun match(
        reconciliationContext: ReconciliationContext,
        ruleSet: MatchRuleSet
    ) {
        if (supportedRulesetType() == ruleSet.type) {
            ruleSet.rules.forEach { rule ->
                val entriesChecks = rule.entriesChecks!!
                val tokens = entriesChecks.expression.split("[\\p{Punct}\\s]+".toRegex())
                val datasources = tokens
                    .filter { reconciliationContext.reconciliationSetting.dataSources.map { d -> d.id }.contains(it) }
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
                val (big, small) = if (recordsA.size > recordsB.size) recordsA to recordsB else recordsB to recordsA
                big.forEach ca@{ a ->
                    small.forEach { b ->
                        if (!b.matchedWithKeys.contains(a.name)) {
                            val result = evalExpression(entriesChecks.expression, a.name, a.attrs, b.name, b.attrs)
                            if (result) {
                                val matchKey = rule.id + "__" + UUID.randomUUID().toString()
                                a.matchedWithKeys.add(b.name)
                                b.matchedWithKeys.add(a.name)
                                b.attrs[MATCH_KEY_ATTRIBUTE] = matchKey
                                a.attrs[MATCH_KEY_ATTRIBUTE] = matchKey
                                return@ca
                            }
                        }
                    }
                }
                addMatchTags(rule, recordsA, recordsB)
            }
        }
    }

    private fun evalExpression(
        expression: String,
        nameA: String,
        attrsA: MutableMap<String, Any?>,
        nameB: String,
        attrsB: MutableMap<String, Any?>
    ): Boolean {
        val vars = mutableMapOf(nameA to attrsA, nameB to attrsB)
        val expr = cleanseExpr(expression, vars)
        return evaluate(expr, vars)
    }

    override fun supportedRulesetType(): RulesetType {
        return RulesetType.EntryWiseOneToOneChecks
    }
}