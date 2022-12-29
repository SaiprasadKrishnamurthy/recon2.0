package com.taxreco.recon.dataloader.rule

import com.taxreco.recon.dataloader.util.DateUtil
import info.debatty.java.stringsimilarity.NormalizedLevenshtein
import org.apache.commons.lang3.math.NumberUtils
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.math.abs
import kotlin.math.roundToInt

class Functions {
    fun equals(a: String, b: String) =
        a.toLowerCase(Locale.getDefault()).trim() == b.toLowerCase(Locale.getDefault()).trim()

    fun startsWith(a: String, b: String) = a.toLowerCase(Locale.getDefault()).trim().startsWith(
        b.toLowerCase(Locale.getDefault()).trim()
    )

    fun contains(a: String, b: String) = a.toLowerCase().trim().contains(b.toLowerCase().trim())

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
        val x = a.toLowerCase().split("[\\p{Punct}\\s]+".toRegex())
        val y = b.toLowerCase().split("[\\p{Punct}\\s]+".toRegex())
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
        val x = a.toLowerCase().split("[\\p{Punct}\\s]+".toRegex())
            .filter { w -> ignoreWords.map { it.toLowerCase().trim() }.contains(w.toLowerCase().trim()) }
        val y = b.toLowerCase().split("[\\p{Punct}\\s]+".toRegex())
            .filter { w -> ignoreWords.map { it.toLowerCase().trim() }.contains(w.toLowerCase().trim()) }
        val (big, small) = if (x.size > y.size) x to y else y to x
        val score = small
            .mapIndexed { i, s -> nl.distance(s.toLowerCase(), big[i].toLowerCase()) }
            .max() ?: 0.0
        return score >= thresholdScore
    }

    fun valueWithinTolerance(a: Any?, b: Any?, t: Double): Boolean {
        if (a == null || b == null) return false
        return abs(a.toString().toDouble() - b.toString().toDouble()) <= t
    }

    fun unquote(a: String): String = a.replace("\"", "").replace("'", "")

    fun isPresent(a: Any?) = a != null && a.toString().trim().isNotBlank()

    fun fixDecimalPlaces(a: Any?, place: Int) = String.format("%.${place}f", a)

    fun round(a: Any?) = a.toString().toDouble().roundToInt().toDouble()

    fun upperCase(a: Any?) = a.toString().toUpperCase()

    fun lowerCase(a: Any?) = a.toString().toLowerCase()

    fun isValidNumber(a: Any?) = NumberUtils.isCreatable(a.toString().trim())


}