package com.hashmimotors.app.util

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.parseToJsonElement

/**
 * A structured product payload decoded from a scanned QR/barcode.
 *
 * When a QR code stores a *full product* (name + price + SKU, etc.) instead of
 * just a numeric identifier, the scanner can hand us this object so the app can
 * auto-fill "Add Part" or add an item to a bill without any typing.
 */
data class QrProduct(
    val name: String,
    val mrp: Double? = null,
    val sku: String? = null,
    val barcode: String? = null,
    val qty: Int = 1
)

/**
 * Heuristic parser that turns a raw QR/barcode string into a [QrProduct] when
 * the code carries full product data. Returns `null` when the string is just a
 * plain identifier (numeric barcode, URL, unknown text) — callers then fall
 * back to the normal "search by barcode" behaviour.
 *
 * Supported formats (documented here so the unit tests + tools/mock_qr_run.py
 * mirror them exactly):
 *
 * 1. JSON — `{"name":"Oil Filter","price":250,"sku":"OF-01","barcode":"890...","qty":2}`
 *    Keys accepted: name/title/product/item/description, price/mrp/rate/amount,
 *    sku/code/id, barcode/ean/upc/gtin, qty/quantity.
 * 2. key=value / key: value — `name=Oil Filter;price=250;sku=OF-01`
 *    (segments split on ';' or newline; '=' or ':' separates key/value).
 * 3. Delimited — `Oil Filter | 250 | OF-01` (also TAB, or comma when the text
 *    clearly has a name + a numeric field):
 *      - name    = first token that contains a letter
 *      - price   = first remaining token that "looks like money"
 *      - barcode = first remaining token that is all digits and length >= 8
 *      - sku     = first remaining token that contains letters
 *
 * Money rule ("looks like money"): a token with no letters that parses as a
 * number AND (has a decimal point or a currency symbol, OR is under 1,000,000).
 * A bare integer >= 1,000,000 (e.g. an EAN-13) is treated as a code, not price.
 */
object QrProductParser {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    fun parse(raw: String): QrProduct? {
        val v = raw.trim()
        if (v.isEmpty()) return null

        if (v.startsWith("{")) {
            parseJson(v)?.let { return it }
        }
        parseKeyValue(v)?.let { return it }
        return parseDelimited(v)
    }

    // ------------------------------------------------------------------ JSON

    private fun parseJson(v: String): QrProduct? {
        val root = runCatching { json.parseToJsonElement(v).jsonObject }.getOrNull() ?: return null
        val name = jsonString(root, listOf("name", "title", "product", "item", "description")) ?: return null
        val mrp = jsonNumber(root, listOf("price", "mrp", "rate", "amount", "cost"))
        val sku = jsonString(root, listOf("sku", "code", "id", "itemCode"))
        val barcode = jsonString(root, listOf("barcode", "ean", "upc", "gtin"))
        val qty = jsonInt(root, listOf("qty", "quantity")) ?: 1
        return QrProduct(name = name, mrp = mrp, sku = sku, barcode = barcode, qty = qty.coerceAtLeast(1))
    }

    private fun jsonString(root: JsonObject, keys: List<String>): String? =
        keys.firstNotNullOfOrNull { key ->
            (root[key] as? JsonPrimitive)?.contentOrNull?.takeIf { it.isNotBlank() }
        }

    private fun jsonNumber(root: JsonObject, keys: List<String>): Double? =
        keys.firstNotNullOfOrNull { key ->
            (root[key] as? JsonPrimitive)?.contentOrNull?.let { parseMoney(it) }
        }

    private fun jsonInt(root: JsonObject, keys: List<String>): Int? =
        keys.firstNotNullOfOrNull { key ->
            (root[key] as? JsonPrimitive)?.contentOrNull?.let { parseQty(it) }
        }

    // ------------------------------------------------------------- key=value

    private fun parseKeyValue(v: String): QrProduct? {
        val map = mutableMapOf<String, String>()
        for (segment in v.split(Regex("[;\n\r]"))) {
            val idx = segment.indexOfFirst { it == '=' || it == ':' }
            if (idx <= 0) continue
            val key = segment.substring(0, idx).trim().lowercase()
            val value = segment.substring(idx + 1).trim()
            if (key.isNotEmpty() && value.isNotEmpty()) map[key] = value
        }
        if (map.isEmpty()) return null
        val name = map["name"] ?: map["title"] ?: map["product"] ?: map["item"] ?: return null
        val mrp = (map["price"] ?: map["mrp"] ?: map["rate"] ?: map["amount"])?.let { parseMoney(it) }
        val sku = map["sku"] ?: map["code"] ?: map["id"]
        val barcode = map["barcode"] ?: map["ean"] ?: map["upc"] ?: map["gtin"]
        val qty = (map["qty"] ?: map["quantity"])?.let { parseQty(it) } ?: 1
        return QrProduct(name = name, mrp = mrp, sku = sku, barcode = barcode, qty = qty.coerceAtLeast(1))
    }

    // ------------------------------------------------------------ delimited

    private fun parseDelimited(v: String): QrProduct? {
        val tokens = splitDelimited(v)
        if (tokens.size < 2) return null

        val name = tokens[0]
        if (!name.any { it.isLetter() }) return null

        // Price = first token (after the name) that looks like money.
        var priceIndex = -1
        for (i in 1 until tokens.size) {
            if (looksLikeMoney(tokens[i])) {
                priceIndex = i
                break
            }
        }
        val price = if (priceIndex >= 0) parseMoney(tokens[priceIndex]) else null

        var barcode: String? = null
        var sku: String? = null
        for (i in 1 until tokens.size) {
            if (i == priceIndex) continue
            val t = tokens[i]
            if (t.isEmpty()) continue
            if (barcode == null && t.all { it.isDigit() } && t.length >= 8) {
                barcode = t
            } else if (sku == null && t.any { it.isLetterOrDigit() } && t.any { it.isLetter() }) {
                sku = t
            }
        }

        if (price == null && sku == null && barcode == null) return null
        return QrProduct(name = name, mrp = price, sku = sku, barcode = barcode, qty = 1)
    }

    private fun splitDelimited(v: String): List<String> {
        val pipe = v.split('|').map { it.trim() }.filter { it.isNotEmpty() }
        if (pipe.size >= 2) return pipe
        val tab = v.split('\t').map { it.trim() }.filter { it.isNotEmpty() }
        if (tab.size >= 2) return tab
        val comma = v.split(',').map { it.trim() }.filter { it.isNotEmpty() }
        val looksLikeCsv = comma.size >= 2 &&
            comma.any { it.any { c -> c.isLetter() } } &&
            comma.any { looksLikeMoney(it) || it.all { c -> c.isDigit() } }
        return if (looksLikeCsv) comma else emptyList()
    }

    // --------------------------------------------------------------- helpers

    /** True when a token plausibly encodes an amount of money (see class KDoc). */
    private fun looksLikeMoney(t: String): Boolean {
        if (t.isEmpty() || t.any { it.isLetter() }) return false
        val hasSymbol = t.any { c -> c == '₹' || c == '$' || c == '€' || c == '£' || c == '¥' }
        val hasDecimal = t.contains('.') || t.contains(',')
        val value = parseMoney(t) ?: return false
        if (!hasSymbol && !hasDecimal && value >= 1_000_000.0) return false
        return true
    }

    private fun parseMoney(s: String): Double? {
        val cleaned = s.trim().filter { ch -> ch.isDigit() || ch == '.' || ch == '-' }
        if (cleaned.isEmpty() || cleaned == "-" || cleaned == ".") return null
        return cleaned.toDoubleOrNull()
    }

    private fun parseQty(s: String): Int? {
        val cleaned = s.trim().filter { it.isDigit() }
        if (cleaned.isEmpty()) return null
        return cleaned.toIntOrNull()
    }
}
