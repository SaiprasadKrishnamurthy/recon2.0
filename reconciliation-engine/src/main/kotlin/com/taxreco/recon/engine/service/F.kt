package com.taxreco.recon.engine.service

import java.util.*
import kotlin.math.abs

class F {

    fun equals(a: String, b: String) =
        a.lowercase(Locale.getDefault()).trim() == b.lowercase(Locale.getDefault()).trim()

    fun startsWith(a: String, b: String) = a.lowercase(Locale.getDefault()).trim().startsWith(
        b.lowercase(Locale.getDefault()).trim()
    )

    fun contains(a: String, b: String) = a.lowercase().trim().contains(b.lowercase().trim())

    fun containsAny(a: String, b: String): Boolean {
        val x = a.lowercase().split("[\\p{Punct}\\s]+".toRegex())
        val y = b.lowercase().split("[\\p{Punct}\\s]+".toRegex())
        return x.any { y.contains(it) }
    }

    fun valueWithinTolerance(a: Any?, b: Any?, t: Double): Boolean {
        if (a == null || b == null) return false
        return abs(a.toString().toDouble() - b.toString().toDouble()) <= t
    }
}