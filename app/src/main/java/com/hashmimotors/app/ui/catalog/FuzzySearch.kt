package com.hashmimotors.app.ui.catalog

import com.hashmimotors.app.domain.model.Part

/**
 * Typo-tolerant part search.
 *
 * Ranks parts by how well they match a query so that small spelling mistakes
 * (e.g. "bearng" → "bearing") still surface the right part. Runs entirely
 * in-memory, which is instant for a 500–2000 part catalog.
 */
object FuzzySearch {

    /** Returns parts ordered by relevance. Parts with no match are dropped. */
    fun rank(parts: List<Part>, query: String): List<Part> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return parts.sortedBy { it.name.lowercase() }
        return parts
            .mapNotNull { part ->
                val score = score(part, q)
                if (score > 0) part to score else null
            }
            .sortedWith(
                compareByDescending<Pair<Part, Int>> { it.second }
                    .thenBy { it.first.name.lowercase() }
            )
            .map { it.first }
    }

    private fun score(part: Part, q: String): Int {
        val name = part.name.lowercase().trim()
        val sku = part.sku.lowercase().trim()
        val brand = part.brand?.lowercase()?.trim() ?: ""
        val oems = part.oemNumbers.map { it.lowercase().trim() }
        val barcode = part.barcode?.lowercase()?.trim() ?: ""

        // Exact matches first
        if (name == q) return 1000
        if (sku == q || barcode == q || oems.any { it == q }) return 960
        if (name.startsWith(q)) return 900
        if (name.contains(q)) return 850
        if (oems.any { it.contains(q) }) return 760
        if (brand.contains(q)) return 700
        if (sku.contains(q)) return 650
        if (barcode.contains(q)) return 600

        // Word-prefix ("oil f" → "oil filter")
        if (name.split(' ').any { it.startsWith(q) }) return 500

        // Subsequence ("hfd" → "hydraulic filter ..."?)
        if (isSubsequence(q, name)) return 420

        // Typo tolerance via edit distance on name words
        val maxDist = maxOf(1, q.length / 3)
        val nameWordBest = name.split(' ').minOfOrNull { levenshtein(q, it) } ?: Int.MAX_VALUE
        if (nameWordBest <= maxDist) return 340 - nameWordBest * 10

        // Typo tolerance on the full name
        if (levenshtein(q, name) <= maxDist) return 300

        // Typo tolerance on OEM numbers
        val oemBest = oems.minOfOrNull { levenshtein(q, it) } ?: Int.MAX_VALUE
        if (oemBest <= maxDist) return 250 - oemBest * 5

        // Typo tolerance on brand
        if (brand.isNotBlank() && levenshtein(q, brand) <= maxDist) return 220

        return 0
    }

    private fun isSubsequence(q: String, target: String): Boolean {
        if (q.length > target.length) return false
        var i = 0
        var j = 0
        while (i < q.length && j < target.length) {
            if (q[i] == target[j]) i++
            j++
        }
        return i == q.length
    }

    /** Standard Levenshtein edit distance. */
    fun levenshtein(a: String, b: String): Int {
        if (a.isEmpty()) return b.length
        if (b.isEmpty()) return a.length

        var prev = IntArray(b.length + 1) { it }
        var curr = IntArray(b.length + 1)

        for (i in 1..a.length) {
            curr[0] = i
            for (j in 1..b.length) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                curr[j] = minOf(
                    prev[j] + 1,        // deletion
                    curr[j - 1] + 1,    // insertion
                    prev[j - 1] + cost  // substitution
                )
            }
            val tmp = prev
            prev = curr
            curr = tmp
        }
        return prev[b.length]
    }
}
