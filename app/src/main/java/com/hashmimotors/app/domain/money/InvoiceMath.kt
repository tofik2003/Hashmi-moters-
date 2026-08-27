package com.hashmimotors.app.domain.money

/**
 * Pure invoice arithmetic.
 *
 * Kept free of Android and Room dependencies so it can be covered by plain JVM
 * unit tests - this is the code that decides how much a customer is charged.
 */
object InvoiceMath {

    /**
     * Discounted value of a single line.
     *
     * This is the only place a line total is computed; qty, rate and discount
     * edits all funnel through it so stored `lineTotal` values can never drift
     * out of sync with the cart totals.
     */
    fun lineTotal(qty: Int, rate: Double, discountPct: Double): Double {
        val safeQty = qty.coerceAtLeast(0)
        val safeRate = rate.coerceAtLeast(0.0)
        val safeDiscount = discountPct.coerceIn(0.0, 100.0)
        return safeQty * safeRate * (1.0 - safeDiscount / 100.0)
    }

    /** Gross value of a set of lines, before any discount. */
    fun subtotal(grossValues: List<Double>): Double = grossValues.sum()

    /**
     * Bill-level discount. Applied to the value that already had its per-line
     * discounts removed, so a line discount is never counted twice.
     */
    fun billDiscount(netAfterLineDiscounts: Double, billDiscountPct: Double): Double =
        netAfterLineDiscounts * (billDiscountPct.coerceIn(0.0, 100.0) / 100.0)

    /** Final amount payable, never negative. */
    fun grandTotal(subtotal: Double, totalDiscount: Double): Double =
        (subtotal - totalDiscount).coerceAtLeast(0.0)
}
