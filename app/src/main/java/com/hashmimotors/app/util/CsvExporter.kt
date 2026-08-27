package com.hashmimotors.app.util

import android.content.Context
import com.hashmimotors.app.domain.model.Invoice
import com.hashmimotors.app.domain.model.Part
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvExporter {
    fun parts(context: Context, parts: List<Part>): File {
        val header = "name,sku,brand,mrp,sellingPrice,costPrice,stockQty,reorderLevel,barcode,oem,hsn,notes"
        val rows = parts.map { p ->
            listOf(
                p.name,
                p.sku,
                p.brand.orEmpty(),
                p.mrp.toPlain(),
                p.sellingPrice.toPlain(),
                (p.costPrice ?: 0.0).toPlain(),
                p.stockQty.toString(),
                p.reorderLevel.toString(),
                p.barcode.orEmpty(),
                p.oemNumbers.joinToString(";"),
                p.hsnCode.orEmpty(),
                p.notes.orEmpty()
            ).joinToString(",") { csv(it) }
        }
        return write(context, "hashmi-catalog.csv", listOf(header) + rows)
    }

    fun invoices(context: Context, invoices: List<Invoice>): File {
        val header = "invoiceNo,date,customer,phone,status,payment,items,qty,subtotal,discount,total"
        val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val rows = invoices.map { inv ->
            listOf(
                inv.invoiceNo,
                fmt.format(Date(inv.date)),
                inv.customerSnapshot.name,
                inv.customerSnapshot.phone,
                inv.status.name,
                inv.paymentMode,
                inv.lines.size.toString(),
                inv.lines.sumOf { it.qty }.toString(),
                inv.subtotal.toPlain(),
                inv.totalDiscount.toPlain(),
                inv.grandTotal.toPlain()
            ).joinToString(",") { csv(it) }
        }
        return write(context, "hashmi-bills.csv", listOf(header) + rows)
    }

    private fun csv(value: String): String {
        val v = value.replace("\"", "\"\"")
        return if (v.contains(',') || v.contains('"') || v.contains('\n')) "\"$v\"" else v
    }

    private fun Double.toPlain(): String = if (this % 1.0 == 0.0) "%.0f".format(this) else "%.2f".format(this)

    private fun write(context: Context, name: String, lines: List<String>): File {
        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(dir, name)
        file.writeText(lines.joinToString("\n"))
        return file
    }
}
