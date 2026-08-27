package com.hashmimotors.app.util

import android.content.Context
import android.net.Uri
import com.hashmimotors.app.domain.model.Part
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.UUID

object CsvImporter {
    /**
     * Expected header (any order, extra columns ignored):
     * name, sku, brand, mrp, sellingPrice, stockQty, barcode, oem, hsn, reorderLevel, notes
     */
    fun parse(context: Context, uri: Uri): Result<List<Part>> = runCatching {
        val stream = context.contentResolver.openInputStream(uri)
            ?: error("Could not open the selected file")
        stream.use { input ->
            val lines = BufferedReader(InputStreamReader(input)).readLines()
                .map { it.trim() }
                .filter { it.isNotBlank() }
            if (lines.isEmpty()) error("The file is empty")

            val header = splitCsv(lines.first()).map { it.trim().lowercase() }
            val nameIdx = header.indexOfFirst { it in listOf("name", "part", "partname", "item") }
            if (nameIdx < 0) error("CSV needs a Name column")

            fun col(aliases: List<String>): Int =
                header.indexOfFirst { it in aliases }

            val skuIdx = col(listOf("sku", "code", "partcode"))
            val brandIdx = col(listOf("brand", "make"))
            val mrpIdx = col(listOf("mrp", "price", "listprice"))
            val sellIdx = col(listOf("sellingprice", "ourprice", "rate", "sell"))
            val stockIdx = col(listOf("stockqty", "stock", "qty", "quantity"))
            val barcodeIdx = col(listOf("barcode", "ean", "upc", "qr"))
            val oemIdx = col(listOf("oem", "oemnumbers", "oemnumber"))
            val hsnIdx = col(listOf("hsn", "hsncode"))
            val reorderIdx = col(listOf("reorderlevel", "reorder", "minstock"))
            val notesIdx = col(listOf("notes", "note", "remark"))

            lines.drop(1).mapNotNull { line ->
                val cols = splitCsv(line)
                fun at(i: Int) = cols.getOrNull(i)?.trim().orEmpty()
                val name = at(nameIdx)
                if (name.isBlank()) return@mapNotNull null
                val mrp = at(mrpIdx).toDoubleOrNull() ?: 0.0
                val sell = at(sellIdx).toDoubleOrNull() ?: mrp
                Part(
                    id = UUID.randomUUID().toString(),
                    sku = at(skuIdx),
                    name = name,
                    oemNumbers = at(oemIdx).split(",", ";", "|").map { it.trim() }.filter { it.isNotBlank() },
                    brand = at(brandIdx).ifBlank { null },
                    mrp = mrp,
                    sellingPrice = sell,
                    stockQty = at(stockIdx).toIntOrNull() ?: 0,
                    reorderLevel = at(reorderIdx).toIntOrNull() ?: 5,
                    barcode = at(barcodeIdx).ifBlank { null },
                    hsnCode = at(hsnIdx).ifBlank { null },
                    notes = at(notesIdx).ifBlank { null }
                )
            }
        }
    }

    private fun splitCsv(line: String): List<String> {
        val out = mutableListOf<String>()
        val cur = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            when {
                c == '"' && inQuotes && i + 1 < line.length && line[i + 1] == '"' -> {
                    cur.append('"'); i++
                }
                c == '"' -> inQuotes = !inQuotes
                c == ',' && !inQuotes -> {
                    out += cur.toString(); cur.clear()
                }
                else -> cur.append(c)
            }
            i++
        }
        out += cur.toString()
        return out
    }
}
