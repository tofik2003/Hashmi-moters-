package com.hashmimotors.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class QrProductParserTest {

    private fun parse(raw: String) = QrProductParser.parse(raw)

    // ---------------------------------------------------------------- JSON

    @Test
    fun `json with all fields`() {
        val p = parse("""{"name":"Oil Filter","price":250,"sku":"OF-01","barcode":"8901234567890","qty":2}""")
        assertEquals("Oil Filter", p?.name)
        assertEquals(250.0, p?.mrp ?: 0.0, 0.0001)
        assertEquals("OF-01", p?.sku)
        assertEquals("8901234567890", p?.barcode)
        assertEquals(2, p?.qty ?: -1)
    }

    @Test
    fun `json price as string with currency`() {
        val p = parse("""{"name":"Air Filter","mrp":"₹320.50"}""")
        assertEquals("Air Filter", p?.name)
        assertEquals(320.5, p?.mrp ?: 0.0, 0.0001)
    }

    @Test
    fun `json without name is not a product`() {
        assertNull(parse("""{"barcode":"8901234567890"}"""))
    }

    @Test
    fun `malformed json falls through to null`() {
        assertNull(parse("{not json at all"))
    }

    // ------------------------------------------------------------ key=value

    @Test
    fun `semicolon key value`() {
        val p = parse("name=Brake Pads;price=1250;sku=BP-77;qty=3")
        assertEquals("Brake Pads", p?.name)
        assertEquals(1250.0, p?.mrp ?: 0.0, 0.0001)
        assertEquals("BP-77", p?.sku)
        assertEquals(3, p?.qty ?: -1)
    }

    @Test
    fun `colon key value multiline`() {
        val p = parse("name: Engine Oil\nprice: 550\nbarcode: 8901111222333")
        assertEquals("Engine Oil", p?.name)
        assertEquals(550.0, p?.mrp ?: 0.0, 0.0001)
        assertEquals("8901111222333", p?.barcode)
    }

    // ------------------------------------------------------------- delimited

    @Test
    fun `pipe name price sku`() {
        val p = parse("Oil Filter | 250 | OF-01")
        assertEquals("Oil Filter", p?.name)
        assertEquals(250.0, p?.mrp ?: 0.0, 0.0001)
        assertEquals("OF-01", p?.sku)
    }

    @Test
    fun `pipe name sku price`() {
        val p = parse("Oil Filter | OF-01 | 250")
        assertEquals("Oil Filter", p?.name)
        assertEquals("OF-01", p?.sku)
        assertEquals(250.0, p?.mrp ?: 0.0, 0.0001)
    }

    @Test
    fun `tab name price barcode`() {
        val p = parse("Brake Pads\t1250\t8901234567890")
        assertEquals("Brake Pads", p?.name)
        assertEquals(1250.0, p?.mrp ?: 0.0, 0.0001)
        assertEquals("8901234567890", p?.barcode)
    }

    @Test
    fun `comma name price sku`() {
        val p = parse("Spark Plug, 240, SP-9")
        assertEquals("Spark Plug", p?.name)
        assertEquals(240.0, p?.mrp ?: 0.0, 0.0001)
        assertEquals("SP-9", p?.sku)
    }

    @Test
    fun `price with currency symbol and decimal`() {
        val p = parse("Car Shampoo|₹240.00|CSH-1")
        assertEquals("Car Shampoo", p?.name)
        assertEquals(240.0, p?.mrp ?: 0.0, 0.0001)
        assertEquals("CSH-1", p?.sku)
    }

    @Test
    fun `name only no second token is null`() {
        assertNull(parse("Oil Filter"))
    }

    // ------------------------------------------------- plain identifiers

    @Test
    fun `plain ean13 is not a product`() {
        assertNull(parse("8901234567890"))
    }

    @Test
    fun `url is not a product`() {
        assertNull(parse("https://example.com/item/123"))
    }

    @Test
    fun `blank is null`() {
        assertNull(parse("   "))
    }
}
