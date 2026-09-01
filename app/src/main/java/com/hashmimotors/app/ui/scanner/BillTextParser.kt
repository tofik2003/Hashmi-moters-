package com.hashmimotors.app.ui.scanner

import java.util.Locale

/**
 * Heuristic parser that turns raw OCR text from a bill into line items.
 *
 * Real-world shop bills vary a lot, so this uses permissive patterns:
 *  - a trailing amount makes a line an "item" (the preceding text is the name)
 *  - "2 x Name" / "Name 2X" / "Qty: 2" style prefixes set the quantity
 *  - lines like "Total"/"Grand Total" capture the bill total
 */
object BillTextParser {

    private val noiseKeywords = listOf(
        "invoice", "bill no", "bill#", "gst", "gstin", "phone", "tel", "mob",
        "address", "date", "time", "cashier", "counter", "cash", "card",
        "upi", "paid", "balance", "change", "round", "thank", "visit",
        "again", "terms", "return", "warranty", "www", "http", "email",
        "state code", "pin", "sr no", "sno", "sl no", "hsn", "sac",
        "taxable", "cgst", "sgst", "igst", "cess", "rate", "qty", "amt", "amount"
    )

    private val totalKeywords = listOf("grand total", "total amount", "net amount", "bill total", "payable", "total")

    fun parse(raw: String): BillParseResult {
        val lines = mutableListOf<ScannedLine>()
        var detectedTotal: Double? = null

        raw.split('\n')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .forEach { original ->
                val line = original
                    .replace("\u20B9", "Rs.")
                    .replace("Rs.", " Rs. ", ignoreCase = true)
                    .trim()
                    .replace(Regex("\\s+"), " ")

                val lower = line.lowercase(Locale.getDefault())

                // Skip obvious header/footer noise
                if (noiseKeywords.any { lower.contains(it) } && !looksLikeItem(lower)) return@forEach

                // Total detection
                val amountTokens = Regex("\\d+(?:[.,]\\d{1,2})?").findAll(line).toList()
                if (totalKeywords.any { lower.contains(it) } && amountTokens.isNotEmpty()) {
                    detectedTotal = amountTokens.last().value.toDoubleOrNull() ?: detectedTotal
                    return@forEach
                }

                // Item detection: line ends with a number (the amount)
                val match = Regex("^(.*?)\\s*([Rr][Ss]\\.?\\s*)?(\\d+(?:[.,]\\d{1,2})?)\\s*$").find(line)
                if (match == null) return@forEach

                var name = match.groupValues[1].trim().trimEnd(':', '-', ' ').trim()
                if (name.isEmpty()) return@forEach
                if (noiseKeywords.any { name.lowercase(Locale.getDefault()).contains(it) }) return@forEach

                val amount = match.groupValues[3].replace(",", "").toDoubleOrNull() ?: return@forEach

                // Quantity detection
                var qty = 1
                var rate = amount
                val qtyMatch = Regex("^(\\d+)\\s*[xX]\\s*").find(name)
                if (qtyMatch != null) {
                    qty = qtyMatch.groupValues[1].toIntOrNull()?.coerceAtLeast(1) ?: 1
                    name = name.removeRange(qtyMatch.range).trim()
                } else {
                    val qtySuffix = Regex("^(.*?)\\s+[xX]\\s*(\\d+)$").find(name)
                    if (qtySuffix != null) {
                        qty = qtySuffix.groupValues[2].toIntOrNull()?.coerceAtLeast(1) ?: 1
                        name = qtySuffix.groupValues[1].trim()
                    }
                }
                if (name.isBlank()) return@forEach
                if (qty > 1 && amount > 0) rate = amount / qty

                lines += ScannedLine(name = name, qty = qty, rate = rate, amount = amount)
            }

        return BillParseResult(lines = lines.distinctBy { it.name.lowercase(Locale.getDefault()) }, detectedTotal = detectedTotal)
    }

    private fun looksLikeItem(lower: String): Boolean {
        // If it contains an 'x' quantity marker or ends with digits, treat as item even if a
        // noise keyword appeared (e.g. "Oil Filter 2 x Rs.250").
        return Regex("\\d+\\s*[xX]").containsMatchIn(lower) || Regex("\\d+(?:[.,]\\d{1,2})?\\s*$").containsMatchIn(lower)
    }
}
