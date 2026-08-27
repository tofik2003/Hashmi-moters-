package com.hashmimotors.app.ui.billing

import com.hashmimotors.app.domain.model.InvoiceLine
import com.hashmimotors.app.domain.money.InvoiceMath
import com.hashmimotors.app.domain.model.PartSnapshot
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Regression tests for the cart totals shown to the customer and printed on the bill.
 *
 * The previous implementation took `subtotal` from the already-discounted line
 * totals and then subtracted the line discounts a second time from the grand
 * total, silently undercharging on every discounted line.
 */
class BillingUiStateTest {

    private val delta = 0.0001

    private fun line(qty: Int, rate: Double, discountPct: Double = 0.0) = InvoiceLine(
        partId = "p$qty$rate",
        partSnapshot = PartSnapshot(name = "Part $rate"),
        qty = qty,
        rate = rate,
        discountPct = discountPct,
        lineTotal = InvoiceMath.lineTotal(qty, rate, discountPct)
    )

    @Test
    fun `empty cart totals are zero`() {
        val state = BillingUiState()
        assertEquals(0.0, state.subtotal, delta)
        assertEquals(0.0, state.totalDiscount, delta)
        assertEquals(0.0, state.grandTotal, delta)
        assertEquals(0, state.totalItems)
    }

    @Test
    fun `a line discount is subtracted exactly once`() {
        val state = BillingUiState(lines = listOf(line(qty = 1, rate = 100.0, discountPct = 10.0)))

        assertEquals(100.0, state.subtotal, delta)
        assertEquals(90.0, state.netAfterLineDiscounts, delta)
        assertEquals(10.0, state.lineDiscount, delta)
        assertEquals(10.0, state.totalDiscount, delta)
        // Regression: used to be 80.0 (discount counted twice).
        assertEquals(90.0, state.grandTotal, delta)
    }

    @Test
    fun `line and bill discounts combine without double counting`() {
        val state = BillingUiState(
            lines = listOf(
                line(qty = 2, rate = 100.0),                       // 200 gross
                line(qty = 1, rate = 50.0, discountPct = 20.0)     // 50 gross, 40 net
            ),
            billDiscountPct = 10.0
        )

        assertEquals(250.0, state.subtotal, delta)
        assertEquals(240.0, state.netAfterLineDiscounts, delta)
        assertEquals(10.0, state.lineDiscount, delta)
        assertEquals(24.0, state.billDiscount, delta)
        assertEquals(34.0, state.totalDiscount, delta)
        assertEquals(216.0, state.grandTotal, delta)
        assertEquals(3, state.totalItems)
    }

    @Test
    fun `no discounts means grand total equals subtotal`() {
        val state = BillingUiState(
            lines = listOf(line(qty = 3, rate = 250.0), line(qty = 1, rate = 99.5))
        )
        assertEquals(849.5, state.subtotal, delta)
        assertEquals(0.0, state.totalDiscount, delta)
        assertEquals(849.5, state.grandTotal, delta)
    }

    @Test
    fun `a 100 percent bill discount never produces a negative total`() {
        val state = BillingUiState(
            lines = listOf(line(qty = 1, rate = 500.0)),
            billDiscountPct = 100.0
        )
        assertEquals(0.0, state.grandTotal, delta)
    }

    @Test
    fun `composition scheme bills carry no tax`() {
        val state = BillingUiState(lines = listOf(line(qty = 1, rate = 100.0)))
        assertEquals(0.0, state.totalGst, delta)
    }
}
