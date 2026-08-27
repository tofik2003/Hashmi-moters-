package com.hashmimotors.app.util

import com.hashmimotors.app.domain.model.Part
import kotlin.math.min

/**
 * Counter search: exact barcode/SKU/OEM first, then tokens, then light typo tolerance.
 */
object SmartSearch {

    fun rank(parts: List<Part>, raw: String): List<Part> {
        val q = raw.trim()
        if (q.isBlank()) return parts
        val nq = normalize(q)
        val tokens = nq.split(' ').filter { it.isNotBlank() }
        return parts.map { it to score(it, q, nq, tokens) }
            .filter { it.second > 0 }
            .sortedWith(compareByDescending<Pair<Part, Int>> { it.second }.thenBy { it.first.name.lowercase() })
            .map { it.first }
    }

    fun suggestions(parts: List<Part>, raw: String, limit: Int = 8): List<String> {
        val q = raw.trim()
        if (q.isBlank()) {
            return parts.mapNotNull { it.brand?.trim()?.takeIf { b -> b.isNotBlank() } }
                .distinctBy { it.lowercase() }
                .take(limit)
        }
        val nq = normalize(q)
        if (nq.isBlank()) return emptyList()
        val brands = parts.mapNotNull { it.brand }.filter { normalize(it).contains(nq) }
        val names = parts.map { it.name }.filter {
            val n = normalize(it)
            n.startsWith(nq) || n.contains(" $nq") || n.contains(nq)
        }
        val skus = parts.map { it.sku }.filter { it.isNotBlank() && normalize(it).contains(nq) }
        return (brands + names + skus).distinctBy { it.lowercase() }.take(limit)
    }

    fun hint(raw: String, count: Int): String? {
        if (raw.isBlank()) return null
        return if (count == 0) "No match — try OEM, brand, or scan the label"
        else "$count ranked match${if (count == 1) "" else "es"}"
    }

    /** Pull OEM-like codes and first lines from a label photo / OCR dump. */
    fun queryFromOcr(text: String): String {
        val codes = Regex("[A-Z0-9][A-Z0-9\\-]{4,}").findAll(text.uppercase())
            .map { it.value }
            .distinct()
            .take(5)
            .toList()
        val lines = text.lines().map { it.trim() }.filter { it.length in 3..48 }.take(3)
        return (codes + lines).joinToString(" ").take(96)
    }

    fun normalize(value: String): String =
        value.lowercase().replace(Regex("[^a-z0-9]+"), " ").trim()

    private fun haystack(part: Part): String = buildString {
        append(part.name); append(' ')
        append(part.sku); append(' ')
        append(part.brand.orEmpty()); append(' ')
        append(part.barcode.orEmpty()); append(' ')
        part.oemNumbers.forEach { append(it); append(' ') }
        append(part.notes.orEmpty()); append(' ')
        append(part.hsnCode.orEmpty())
    }

    private fun score(part: Part, raw: String, nq: String, tokens: List<String>): Int {
        val trimmed = raw.trim()
        val name = part.name
        val sku = part.sku
        val barcode = part.barcode.orEmpty()
        var s = 0
        if (barcode.equals(trimmed, ignoreCase = true)) s += 1000
        if (sku.equals(trimmed, ignoreCase = true)) s += 900
        if (part.oemNumbers.any { it.equals(trimmed, ignoreCase = true) }) s += 850
        if (name.equals(trimmed, ignoreCase = true)) s += 800
        if (barcode.contains(trimmed, ignoreCase = true) && trimmed.length >= 4) s += 420
        if (sku.contains(trimmed, ignoreCase = true) && trimmed.length >= 2) s += 360
        val nh = normalize(haystack(part))
        val nameN = normalize(name)
        if (nq.length >= 2 && nh.contains(nq)) s += 280
        if (nq.length >= 2 && nameN.startsWith(nq)) s += 240
        if (tokens.isNotEmpty()) {
            val hit = tokens.count { it.length >= 2 && nh.contains(it) }
            if (hit == tokens.size) s += 180 + tokens.size * 25
            else if (hit > 0) s += hit * 45
        }
        if (s == 0 && nq.length >= 4) {
            val words = nameN.split(' ').filter { it.length >= 3 }
            if (words.any { levenshtein(it, nq) <= 2 } ||
                (sku.isNotBlank() && levenshtein(normalize(sku), nq) <= 2)
            ) {
                s += 70
            }
        }
        return s
    }

    private fun levenshtein(a: String, b: String): Int {
        if (a == b) return 0
        if (a.isEmpty()) return b.length
        if (b.isEmpty()) return a.length
        if (kotlin.math.abs(a.length - b.length) > 2) return 99
        val m = a.length
        val n = b.length
        var prev = IntArray(n + 1) { it }
        var cur = IntArray(n + 1)
        for (i in 1..m) {
            cur[0] = i
            for (j in 1..n) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                cur[j] = min(min(cur[j - 1] + 1, prev[j] + 1), prev[j - 1] + cost)
            }
            val tmp = prev
            prev = cur
            cur = tmp
        }
        return prev[n]
    }
}
