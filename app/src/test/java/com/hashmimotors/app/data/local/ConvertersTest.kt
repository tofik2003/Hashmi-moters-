package com.hashmimotors.app.data.local

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The Room TypeConverters are the only thing standing between the serialised
 * columns and a crash (or silent data loss) when a bill or a part is read back.
 */
class ConvertersTest {

    private val converters = Converters()

    @Test
    fun `string list round trips`() {
        val original = listOf("04465-12345", "8901234567890", "OEM-XYZ")
        val encoded = converters.fromStringList(original)
        assertEquals(original, converters.toStringList(encoded))
    }

    @Test
    fun `empty and null string lists become empty lists`() {
        assertTrue(converters.toStringList(null).isEmpty())
        assertTrue(converters.toStringList("").isEmpty())
        assertEquals(emptyList<String>(), converters.toStringList(converters.fromStringList(emptyList())))
    }

    @Test
    fun `corrupt string list falls back to empty instead of throwing`() {
        assertTrue(converters.toStringList("not-json-at-all").isEmpty())
    }

    @Test
    fun `invoice lines round trip with every field intact`() {
        val original = listOf(
            InvoiceLineEmbedded(
                partId = "part-1",
                partName = "Front Brake Pad",
                oemNumbers = listOf("04465-12345", "04465-33471"),
                hsnCode = "8708",
                mrp = 1450.0,
                gstPercent = 28.0,
                qty = 2,
                rate = 1200.0,
                discountPct = 5.0,
                lineTotal = 2280.0
            ),
            InvoiceLineEmbedded(
                partId = "part-2",
                partName = "Oil Filter",
                oemNumbers = emptyList(),
                hsnCode = null,
                mrp = 350.0,
                gstPercent = 18.0,
                qty = 1,
                rate = 300.0,
                discountPct = 0.0,
                lineTotal = 300.0
            )
        )

        val decoded = converters.toInvoiceLineList(converters.fromInvoiceLineList(original))

        assertEquals(original, decoded)
        assertEquals("Front Brake Pad", decoded[0].partName)
        assertEquals(listOf("04465-12345", "04465-33471"), decoded[0].oemNumbers)
        assertEquals(null, decoded[1].hsnCode)
    }

    @Test
    fun `corrupt invoice lines fall back to empty instead of throwing`() {
        assertTrue(converters.toInvoiceLineList("{broken").isEmpty())
        assertTrue(converters.toInvoiceLineList(null).isEmpty())
    }
}
