package com.taxreco.recon.dataloader.util

import java.time.LocalDate
import java.time.Month
import java.time.format.DateTimeFormatter

object DateUtil {
    fun yearAndQuarter(date: String, format: String): Pair<Int, Int> {
        val quarters = mapOf(
            Month.JANUARY to 4,
            Month.FEBRUARY to 4,
            Month.MARCH to 4,
            Month.APRIL to 1,
            Month.MAY to 1,
            Month.JUNE to 1,
            Month.JULY to 2,
            Month.AUGUST to 2,
            Month.SEPTEMBER to 2,
            Month.OCTOBER to 3,
            Month.NOVEMBER to 3,
            Month.DECEMBER to 3
        )

        val dateFormatter = DateTimeFormatter.ofPattern(format)
        val localDate = LocalDate.parse(date, dateFormatter)
        return localDate.year to (quarters[localDate.month] ?: error(""))
    }

    fun yearAndQuarter(date: LocalDate): Pair<Int, Int> {
        val quarters = mapOf(
            Month.JANUARY to 4,
            Month.FEBRUARY to 4,
            Month.MARCH to 4,
            Month.APRIL to 1,
            Month.MAY to 1,
            Month.JUNE to 1,
            Month.JULY to 2,
            Month.AUGUST to 2,
            Month.SEPTEMBER to 2,
            Month.OCTOBER to 3,
            Month.NOVEMBER to 3,
            Month.DECEMBER to 3
        )
        return date.year to (quarters[date.month] ?: error(""))
    }

    fun getFinancialYear(date: LocalDate): Int {
        return if (date.month > Month.MARCH) return date.year else date.year - 1
    }

    fun nextFy(date: LocalDate): Int {
        val yearAndQuarter = yearAndQuarter(date)
        return if (yearAndQuarter.second > 3) yearAndQuarter.first else yearAndQuarter.first + 1
    }

    fun parseDate(input: String): LocalDate {
        val patterns = listOf(
            DateTimeFormatter.ofPattern("dd-MMM-yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yy"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("dd/MMM/yyyy"),
            DateTimeFormatter.ofPattern("dd/MMM/yy"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy")
        )
        patterns.forEach { p ->
            try {
                return LocalDate.parse(input.replace(" \"", "").trim(), p)
            } catch (ex: Exception) {
            }
        }
        throw IllegalArgumentException("Invalid Date: $input")
    }

    fun getPrevAndCurrPeriods(start: LocalDate): Pair<Pair<LocalDate, LocalDate>, Pair<LocalDate, LocalDate>> {
        val startPrev = start.year - 1
        val startCurrent = start.year
        val prev = Pair(LocalDate.of(startPrev, Month.APRIL, 1), LocalDate.of(startCurrent, Month.APRIL, 1))
        val curr = Pair(LocalDate.of(start.year, Month.APRIL, 1), LocalDate.of(start.year + 1, Month.APRIL, 1))
        return Pair(prev, curr)
    }

    fun monthIndex(monthName: String): Int {
        return Month.valueOf(monthName.toUpperCase()).value
    }
}