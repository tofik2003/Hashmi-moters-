package com.hashmimotors.app.domain.money

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class FinancialYearTest {

    private fun millis(year: Int, month: Int, day: Int): Long =
        Calendar.getInstance().apply {
            clear()
            set(year, month, day, 12, 0, 0)
        }.timeInMillis

    @Test
    fun `months from April to December belong to the financial year that started in April`() {
        assertEquals("2026-27", FinancialYear.label(millis(2026, Calendar.AUGUST, 27)))
        assertEquals("2026-27", FinancialYear.label(millis(2026, Calendar.APRIL, 1)))
        assertEquals("2026-27", FinancialYear.label(millis(2026, Calendar.DECEMBER, 31)))
    }

    @Test
    fun `january to march still belong to the previous financial year`() {
        // This is the bug the calendar-year calculation had: Jan-Mar 2027 must be
        // labelled 2026-27, not 2027-28.
        assertEquals("2026-27", FinancialYear.label(millis(2027, Calendar.JANUARY, 1)))
        assertEquals("2026-27", FinancialYear.label(millis(2027, Calendar.FEBRUARY, 10)))
        assertEquals("2026-27", FinancialYear.label(millis(2027, Calendar.MARCH, 31)))
        assertEquals("2027-28", FinancialYear.label(millis(2027, Calendar.APRIL, 1)))
    }

    @Test
    fun `end year is always two digits`() {
        assertEquals("2099-00", FinancialYear.label(millis(2099, Calendar.MAY, 1)))
        assertEquals("2100-01", FinancialYear.label(millis(2100, Calendar.MAY, 1)))
    }
}
