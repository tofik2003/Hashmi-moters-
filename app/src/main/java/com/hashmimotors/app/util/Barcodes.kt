package com.hashmimotors.app.util

/**
 * Small pure helpers for generating store barcodes.
 */
object Barcodes {

    /**
     * EAN-13 check digit for a 12-digit base string.
     */
    fun checkDigit(base12: String): Int {
        var sum = 0
        base12.forEachIndexed { i, c ->
            val d = c - '0'
            sum += if (i % 2 == 0) d else d * 3
        }
        return (10 - (sum % 10)) % 10
    }

    /**
     * Generate a unique EAN-13-style code using the in-store "2" prefix, so it
     * can never collide with a real manufacturer barcode. Derived from the
     * current time, so consecutive codes are effectively unique.
     */
    fun generateEan13(): String {
        val base = "2" + (System.currentTimeMillis() % 100_000_000_000L).toString().padStart(11, '0')
        return base + checkDigit(base)
    }

    /**
     * Short numeric internal code (12 digits) used as a fallback when a part
     * has no external barcode.
     */
    fun generateInternalCode(): String {
        val base = "8" + (System.currentTimeMillis() % 100_000_000_000L).toString().padStart(11, '0')
        return base
    }
}
