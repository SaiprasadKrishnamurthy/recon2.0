package com.taxreco.recon.engine.service

import com.taxreco.recon.engine.model.MatchRuleSet
import com.taxreco.recon.engine.model.ReconciliationContext
import com.taxreco.recon.engine.model.RulesetEvaluationService
import com.taxreco.recon.engine.model.RulesetType
import com.taxreco.recon.engine.util.Constants
import org.springframework.stereotype.Service
import java.util.*

@Service
class FieldChecksService : RulesetEvaluationService {

    override fun match(
        reconciliationContext: ReconciliationContext,
        ruleSet: MatchRuleSet
    ) {
        if (supportedRulesetType() == ruleSet.type) {
            ruleSet.rules.forEach { rule ->
                val tokens = rule.fieldChecks!!.expression.split("[\\p{Punct}\\s&&[^_]]+".toRegex())
                val datasources = tokens
                    .filter { reconciliationContext.reconciliationSetting.dataSources.map { d -> d.id }.contains(it) }
                    .distinct()

                val _rule = rule.copy()
                reconciliationContext.transactionRecords[datasources[0]]?.forEach { rec ->
                    try {
                        val result = evalExpression(_rule.fieldChecks!!.expression, rec.name, rec.attrs)
                        if (result) {
                            rec.matchTags.addAll(rule.tagsWhenMatched)
                            rec.attrs[Constants.MATCH_KEY_ATTRIBUTE] = UUID.randomUUID().toString()
                        } else {
                            rec.attrs[Constants.MATCH_KEY_ATTRIBUTE] = "$" + UUID.randomUUID().toString()
                            rec.matchTags.addAll(rule.tagsWhenNotMatched)
                        }
                    } catch (ex: Exception) {
                        rec.matchTags.addAll(rule.tagsWhenNotMatched)
                    }
                }
            }
        }
    }

    override fun supportedRulesetType(): RulesetType {
        return RulesetType.FieldChecks
    }

    private fun evalExpression(
        expression: String,
        nameA: String,
        attrsA: MutableMap<String, Any?>
    ): Boolean {
        val vars = mutableMapOf(nameA to attrsA)
        val expr = cleanseExpr(expression, vars)
        return evaluate(expr, vars)
    }
}