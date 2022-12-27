package com.taxreco.recon.engine.util

import info.debatty.java.stringsimilarity.NormalizedLevenshtein
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.math.abs


class F {

    fun equals(a: String, b: String) =
        a.lowercase(Locale.getDefault()).trim() == b.lowercase(Locale.getDefault()).trim()

    fun startsWith(a: String, b: String) = a.lowercase(Locale.getDefault()).trim().startsWith(
        b.lowercase(Locale.getDefault()).trim()
    )

    fun contains(a: String, b: String) = a.lowercase().trim().contains(b.lowercase().trim())

    fun removeAllSymbols(a: String): String {
        return a.replace("[^A-Za-z0-9 ]".toRegex(), "")
    }

    fun removeSymbols(a: String, list: List<Char>): String {
        return a.filter { !list.contains(it) }
    }

    fun validNumber(a: String): Boolean {
        return try {
            a.toDouble()
            true
        } catch (ex: Exception) {
            false
        }
    }

    fun validNonZeroNumber(a: String): Boolean {
        return try {
            return a.toDouble() > 0
        } catch (ex: Exception) {
            false
        }
    }

    fun containsAny(a: String, b: String): Boolean {
        val x = a.lowercase().split("[\\p{Punct}\\s]+".toRegex())
        val y = b.lowercase().split("[\\p{Punct}\\s]+".toRegex())
        return x.any { y.contains(it) }
    }

    fun dateMatch(a: String, b: String, toleranceInDays: Int = 0): Boolean {
        val x = DateUtil.parseDate(a)
        val y = DateUtil.parseDate(b)
        val diff = ChronoUnit.DAYS.between(x, y)
        return abs(diff.toDouble()) <= toleranceInDays
    }

    fun fuzzyMatch(a: String, b: String, thresholdScore: Double, ignoreWords: List<String> = listOf()): Boolean {
        val nl = NormalizedLevenshtein()
        val x = a.lowercase().split("[\\p{Punct}\\s]+".toRegex())
            .filter { w -> ignoreWords.map { it.lowercase().trim() }.contains(w.lowercase().trim()) }
        val y = b.lowercase().split("[\\p{Punct}\\s]+".toRegex())
            .filter { w -> ignoreWords.map { it.lowercase().trim() }.contains(w.lowercase().trim()) }
        val (big, small) = if (x.size > y.size) x to y else y to x
        val score = small
            .mapIndexed { i, s -> nl.distance(s.lowercase(), big[i].lowercase()) }
            .max()
        return score >= thresholdScore
    }

    fun valueWithinTolerance(a: Any?, b: Any?, t: Double): Boolean {
        if (a == null || b == null) return false
        return abs(a.toString().toDouble() - b.toString().toDouble()) <= t
    }
}