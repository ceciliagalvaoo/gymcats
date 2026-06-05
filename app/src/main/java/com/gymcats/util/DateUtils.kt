package com.gymcats.util

import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

object DateUtils {
    fun today(): String = LocalDate.now().toString()
    fun currentMonth(): String = YearMonth.now().toString()
    fun fromDate(months: Int): String = LocalDate.now().minusMonths(months.toLong()).toString()
    fun formatDisplay(str: String): String =
        LocalDate.parse(str).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
}
