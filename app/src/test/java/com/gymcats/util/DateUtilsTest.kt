package com.gymcats.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class DateUtilsTest {

    @Test
    fun currentMonthMatchesYearMonthNow() {
        assertEquals(YearMonth.now().toString(), DateUtils.currentMonth())
    }

    @Test
    fun fromDateSupportsConfiguredProgressPeriods() {
        assertEquals(LocalDate.now().minusMonths(1).toString(), DateUtils.fromDate(1))
        assertEquals(LocalDate.now().minusMonths(3).toString(), DateUtils.fromDate(3))
        assertEquals(LocalDate.now().minusMonths(6).toString(), DateUtils.fromDate(6))
    }

    @Test
    fun formatDisplayUsesDayMonthYearPattern() {
        assertEquals("05/06/2026", DateUtils.formatDisplay("2026-06-05"))
    }
}
