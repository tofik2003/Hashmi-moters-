package com.hashmimotors.app.domain.money

import java.util.Calendar

/**
 * Indian financial-year helpers.
 *
 * GST invoice numbers must be sequential within a financial year, which in India
 * runs from 1 April to 31 March - not the calendar year.
 */
object FinancialYear {

    /** Calendar month (0-based) in which the Indian financial year starts (April = 3). */
    private const val FY_START_MONTH = Calendar.APRIL

    /**
     * Financial-year label for a timestamp, e.g.
     *  - Aug 2026 -> "2026-27"
     *  - Feb 2027 -> "2026-27"  (still FY 2026-27, Jan-Mar belong to the FY
     *    that started the previous April)
     */
    fun label(epochMillis: Long): String {
        val cal = Calendar.getInstance().apply { timeInMillis = epochMillis }
        val year = cal.get(Calendar.YEAR)
        val startYear = if (cal.get(Calendar.MONTH) >= FY_START_MONTH) year else year - 1
        return "$startYear-${((startYear + 1) % 100).toString().padStart(2, '0')}"
    }
}
