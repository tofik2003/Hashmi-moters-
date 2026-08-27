package com.hashmimotors.app.domain.money

import org.junit.Assert.assertEquals
import org.junit.Test

class InvoiceMathTest {

    private val delta = 0.0001

    @Test
    fun `line total applies the percentage discount`() {
        assertEquals(90.0, InvoiceMath.lineTotal(qty = 1, rate = 100.0, discountPct = 10.0), delta)
        assertEquals(200.0, InvoiceMath.lineTotal(qty = 2, rate = 100.0, discountPct = 0.0), delta)
        assertEquals(40.0, InvoiceMath.lineTotal(qty = 1, rate = 50.0, discountPct = 20.0), delta)
    }

    @Test
    fun `line total clamps impossible input`() {
        assertEquals(0.0, InvoiceMath.lineTotal(qty = -5, rate = 100.0, discountPct = 0.0), delta)
        assertEquals(0.0, InvoiceMath.lineTotal(qty = 1, rate = -100.0, discountPct = 0.0), delta)
        assertEquals(0.0, InvoiceMath.lineTotal(qty = 1, rate = 100.0, discountPct = 150.0), delta)
        assertEquals(100.0, InvoiceMath.lineTotal(qty = 1, rate = 100.0, discountPct = -20.0), delta)
    }

    @Test
    fun `bill discount is taken from the line-discounted value`() {
        // 240 is the value after a 10 rupee line discount on a 250 gross bill.
        assertEquals(24.0, InvoiceMath.billDiscount(240.0, 10.0), delta)
    }

    @Test
    fun `bill discount is clamped to 0-100 percent`() {
        assertEquals(0.0, InvoiceMath.billDiscount(240.0, -10.0), delta)
        assertEquals(240.0, InvoiceMath.billDiscount(240.0, 250.0), delta)
    }

    @Test
    fun `grand total never goes negative`() {
        assertEquals(0.0, InvoiceMath.grandTotal(100.0, 500.0), delta)
    }

    @Test
    fun `subtotal sums gross line values`() {
        assertEquals(250.0, InvoiceMath.subtotal(listOf(200.0, 50.0)), delta)
        assertEquals(0.0, InvoiceMath.subtotal(emptyList()), delta)
    }
}
