package com.hashmimotors.app.util

/**
 * Minimal, dependency-free CSV parser that handles RFC-4180 style quoting
 * (quoted fields, escaped double-quotes, commas and newlines inside quotes).
 */
object CsvParser {

    /**
     * Parses raw CSV text into a list of rows, each a list of fields.
     * Blank lines and fully-empty rows are skipped.
     */
    fun parse(text: String): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        var row = mutableListOf<String>()
        val field = StringBuilder()
        var inQuotes = false
        var i = 0

        while (i < text.length) {
            val c = text[i]
            when {
                inQuotes -> {
                    when (c) {
                        '"' -> {
                            if (i + 1 < text.length && text[i + 1] == '"') {
                                field.append('"')
                                i++
                            } else {
                                inQuotes = false
                            }
                        }
                        else -> field.append(c)
                    }
                }
                c == '"' -> inQuotes = true
                c == ',' -> {
                    row.add(field.toString())
                    field.setLength(0)
                }
                c == '\n' -> {
                    row.add(field.toString())
                    field.setLength(0)
                    if (row.any { it.isNotBlank() }) rows.add(row)
                    row = mutableListOf()
                }
                c == '\r' -> Unit // ignore carriage returns
                else -> field.append(c)
            }
            i++
        }

        // Flush any trailing field/row without a trailing newline.
        if (field.isNotEmpty() || row.isNotEmpty()) {
            row.add(field.toString())
            if (row.any { it.isNotBlank() }) rows.add(row)
        }

        return rows
    }

    /**
     * The canonical header row describing every supported column.
     * Shown to users as a template and used to build sample content.
     */
    const val TEMPLATE_HEADER =
        "name,sku,oem,brand,category,mrp,selling_price,stock,reorder_level,hsn,barcode,notes"

    val SAMPLE_CSV = """$TEMPLATE_HEADER
Oil Filter - Maruti Swift,OF-SWIFT-01,16510M68K10,Bosch,Filters,220,185,40,10,8421,8901234567890,High moving
Brake Pad Set - Front,BP-FR-02,55810-63J10,TVS Girling,Brakes,1650,1420,25,5,8708,8901234567891,
Air Filter - Hyundai i20,AF-I20-03,28113-1R100,Mann,Filters,350,299,15,4,8421,8901234567892,Fast seller
"""
}
