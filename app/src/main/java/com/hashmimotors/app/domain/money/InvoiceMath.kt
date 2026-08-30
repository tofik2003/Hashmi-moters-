package com.hashmimotors.app.domain.money

/**
 * Pure invoice arithmetic for GST Composition / Bill of Supply.
 */
object InvoiceMath {

    /**
     * Discounted value of a single line.
     */
    fun lineTotal(qty: Int, rate: Double, discountPct: Double): Double {
        val safeQty = qty.coerceAtLeast(0)
        val safeRate = rate.coerceAtLeast(0.0)
        val safeDiscount = discountPct.coerceIn(0.0, 100.0)
        return safeQty * safeRate * (1.0 - safeDiscount / 100.0)
    }

    /** Gross value of lines. */
    fun subtotal(linesTotals: List<Double>): Double = linesTotals.sum()

    /**
     * Bill-level discount. Applied to net amount after line discounts.
     */
    fun billDiscount(netAfterLineDiscounts: Double, billDiscountPct: Double): Double =
        netAfterLineDiscounts * (billDiscountPct.coerceIn(0.0, 100.0) / 100.0)

    /** Final amount payable, never negative. */
    fun grandTotal(subtotal: Double, totalDiscount: Double): Double =
        (subtotal - totalDiscount).coerceAtLeast(0.0)
}
