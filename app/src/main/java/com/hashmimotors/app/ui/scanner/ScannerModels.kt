package com.hashmimotors.app.ui.scanner

/** A single line item recognized from a scanned bill. */
data class ScannedLine(
    val name: String,
    val qty: Int = 1,
    val rate: Double = 0.0,
    val amount: Double = 0.0
)

/** Result of parsing a scanned bill. */
data class BillParseResult(
    val lines: List<ScannedLine>,
    val detectedTotal: Double? = null
)
