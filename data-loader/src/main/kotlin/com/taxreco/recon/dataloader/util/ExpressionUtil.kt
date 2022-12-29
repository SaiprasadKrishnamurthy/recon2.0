package com.taxreco.recon.dataloader.util

import com.taxreco.recon.dataloader.rule.Functions
import org.mvel2.MVEL

object ExpressionUtil {
    fun evaluate(
        expr: String,
        vars: Map<String, Map<String, Any?>>
    ): Boolean {
        try {
            val compiled = MVEL.compileExpression(expr)
            return MVEL.executeExpression(compiled, Functions(), vars) as Boolean
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
            return MVEL.executeExpression(compiled, Functions(), vars)
        } catch (ex: Exception) {
            if (ex.message?.contains("could not access:") == true) {
                return null
            }
            throw ex
        }
    }
}