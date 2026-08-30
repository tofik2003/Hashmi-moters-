package com.hashmimotors.app.ui.billing

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.hashmimotors.app.domain.model.Invoice
import com.hashmimotors.app.domain.model.Shop
import com.hashmimotors.app.ui.components.AnimatedBigButton
import com.hashmimotors.app.ui.theme.BrandGold
import com.hashmimotors.app.ui.theme.BrandGoldBright
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun InvoicePreviewScreen(
    invoiceId: String,
    viewModel: InvoicePreviewViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val invoice by viewModel.invoice.collectAsState()
    val shop by viewModel.shop.collectAsState()

    var showSuccess by remember { mutableStateOf(true) }

    LaunchedEffect(invoiceId) {
        viewModel.loadInvoice(invoiceId)
    }

    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(24.dp))
            // Top bar
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text(
                    text = "Bill of Supply",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            invoice?.let { inv ->
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Success banner
                    if (showSuccess) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFF2E7D32).copy(alpha = 0.35f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(Color(0xFF2E7D32), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Filled.Check,
                                            contentDescription = null,
                                            tint = Color.White
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Bill Saved & Stock Deducted",
                                            color = Color.White,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = inv.invoiceNo,
                                            color = BrandGoldBright,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Invoice details
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.09f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = shop?.name?.ifBlank { "Hashmi Motors" } ?: "Hashmi Motors",
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (shop?.address?.isNotBlank() == true) {
                                    Text(
                                        text = shop!!.address,
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 12.sp
                                    )
                                }
                                if (shop?.city?.isNotBlank() == true) {
                                    Text(
                                        text = "${shop!!.city}${if (shop!!.state.isNotBlank()) ", " + shop!!.state else ""}${if (shop!!.pincode.isNotBlank()) " - " + shop!!.pincode else ""}",
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 12.sp
                                    )
                                }
                                if (shop?.phone?.isNotBlank() == true) {
                                    Text(
                                        text = "Ph: ${shop!!.phone}",
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 12.sp
                                    )
                                }
                                if (shop?.gstin?.isNotBlank() == true) {
                                    Text(
                                        text = "GSTIN: ${shop!!.gstin}",
                                        color = BrandGold,
                                        fontSize = 12.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.15f)))
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Bill No", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                                        Text(inv.invoiceNo, color = BrandGoldBright, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Date", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                                        Text(
                                            text = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                                                .format(Date(inv.date)),
                                            color = Color.White,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Customer", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                                        Text(inv.customerSnapshot.name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                    if (inv.customerSnapshot.phone.isNotBlank()) {
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("Phone", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                                            Text(inv.customerSnapshot.phone, color = Color.White, fontSize = 13.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Items list
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.09f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Purchased Items",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                inv.lines.forEachIndexed { idx, line ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "${idx + 1}. ${line.partSnapshot.name}",
                                                color = Color.White,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            if (line.partSnapshot.oemNumbers.isNotEmpty()) {
                                                Text(
                                                    text = "OEM: ${line.partSnapshot.oemNumbers.first()}",
                                                    color = Color.White.copy(alpha = 0.55f),
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                        Text(
                                            text = "${line.qty} x ₹${"%,.0f".format(line.rate)}",
                                            color = Color.White.copy(alpha = 0.7f),
                                            fontSize = 12.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp)
                                        )
                                        Text(
                                            text = "₹${"%,.0f".format(line.lineTotal)}",
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    if (idx < inv.lines.size - 1) {
                                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.08f)))
                                    }
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.2f)))
                                Spacer(modifier = Modifier.height(8.dp))

                                TotalRow("Subtotal", inv.subtotal)
                                if (inv.totalDiscount > 0) {
                                    TotalRow("Discount", -inv.totalDiscount, color = Color(0xFFFFA000))
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Grand Total", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "₹${"%,.0f".format(inv.grandTotal)}",
                                        color = BrandGoldBright,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }
                    }

                    // Composition note
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFFA000).copy(alpha = 0.15f)
                            )
                        ) {
                            Text(
                                text = "Composition taxable person — not eligible to collect tax on supplies.",
                                color = Color(0xFFFFA000),
                                fontSize = 11.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }

                // Share buttons
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ShareActionButton(
                            icon = Icons.Filled.Share,
                            label = "Share",
                            modifier = Modifier.weight(1f),
                            onClick = { sharePdf(context, inv, shop) }
                        )
                        ShareActionButton(
                            icon = Icons.Filled.PictureAsPdf,
                            label = "View PDF",
                            modifier = Modifier.weight(1f),
                            onClick = { generateAndOpenPdf(context, inv, shop) }
                        )
                        ShareActionButton(
                            icon = Icons.Filled.Sms,
                            label = "SMS",
                            modifier = Modifier.weight(1f),
                            onClick = { shareViaSms(context, inv) }
                        )
                    }
                    AnimatedBigButton(
                        text = "Share on WhatsApp",
                        icon = Icons.Filled.Share,
                        onClick = { shareOnWhatsApp(context, inv, shop) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ShareActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(68.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.12f)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, tint = BrandGoldBright, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun TotalRow(label: String, value: Double, color: Color = Color.White) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
        Text("₹${"%,.0f".format(value)}", color = color, fontSize = 13.sp)
    }
}

private fun shareOnWhatsApp(context: Context, invoice: Invoice, shop: Shop?) {
    val phone = invoice.customerSnapshot.phone.filter { it.isDigit() }
    val phoneWithCountry = if (phone.length == 10) "91$phone" else phone
    val shopTitle = shop?.name?.ifBlank { "Hashmi Motors" } ?: "Hashmi Motors"
    val message = buildString {
        append("*$shopTitle*\n")
        if (!shop?.phone.isNullOrBlank()) append("Ph: ${shop?.phone}\n")
        append("-------------------------\n")
        append("*BILL OF SUPPLY*\n")
        append("Bill No: ${invoice.invoiceNo}\n")
        append("Date: ${SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(invoice.date))}\n")
        append("Customer: ${invoice.customerSnapshot.name}\n")
        append("-------------------------\n")
        invoice.lines.forEach { line ->
            append("${line.qty} x ${line.partSnapshot.name}\n")
            append("   ₹${"%,.0f".format(line.rate)} = ₹${"%,.0f".format(line.lineTotal)}\n")
        }
        append("-------------------------\n")
        append("*GRAND TOTAL: ₹${"%,.0f".format(invoice.grandTotal)}*\n\n")
        append("Thank you for your business!")
    }
    val intent = Intent(Intent.ACTION_VIEW).apply {
        data = android.net.Uri.parse("https://wa.me/${phoneWithCountry}?text=${android.net.Uri.encode(message)}")
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    runCatching { context.startActivity(intent) }
        .onFailure {
            shareViaSms(context, invoice)
        }
}

private fun shareViaSms(context: Context, invoice: Invoice) {
    val message = buildString {
        append("Hashmi Motors\n")
        append("Bill: ${invoice.invoiceNo}\n")
        append("Total: Rs.${"%,.0f".format(invoice.grandTotal)}\n")
        append("Thank you!")
    }
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, message)
        if (invoice.customerSnapshot.phone.isNotBlank()) {
            putExtra("address", invoice.customerSnapshot.phone)
        }
    }
    context.startActivity(Intent.createChooser(intent, "Share via SMS").apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    })
}

private fun sharePdf(context: Context, invoice: Invoice, shop: Shop?) {
    val file = generatePdf(context, invoice, shop)
    if (file != null) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Bill PDF").apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }
}

private fun generateAndOpenPdf(context: Context, invoice: Invoice, shop: Shop?) {
    val file = generatePdf(context, invoice, shop)
    if (file != null) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(intent) }
    }
}

private fun generatePdf(context: Context, invoice: Invoice, shop: Shop?): File? {
    return runCatching {
        val pdf = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
        val page = pdf.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 11f
            isAntiAlias = true
        }
        val titlePaint = Paint(paint).apply {
            textSize = 18f
            isFakeBoldText = true
        }

        var y = 45f
        val shopName = shop?.name?.ifBlank { "Hashmi Motors" } ?: "Hashmi Motors"
        canvas.drawText(shopName, 40f, y, titlePaint)
        y += 20f
        paint.textSize = 10f
        if (!shop?.address.isNullOrBlank()) {
            canvas.drawText(shop!!.address, 40f, y, paint); y += 14f
        }
        if (!shop?.phone.isNullOrBlank()) {
            canvas.drawText("Ph: ${shop!!.phone}", 40f, y, paint); y += 14f
        }
        if (!shop?.gstin.isNullOrBlank()) {
            canvas.drawText("GSTIN: ${shop!!.gstin}", 40f, y, paint); y += 14f
        }
        y += 10f
        canvas.drawLine(40f, y, 555f, y, paint); y += 16f

        canvas.drawText("BILL OF SUPPLY", 40f, y, Paint(paint).apply { isFakeBoldText = true; textSize = 13f })
        y += 16f
        canvas.drawText("Bill No: ${invoice.invoiceNo}", 40f, y, paint)
        canvas.drawText(
            "Date: ${SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(invoice.date))}",
            380f, y, paint
        )
        y += 16f
        canvas.drawText("Customer: ${invoice.customerSnapshot.name}", 40f, y, paint)
        if (invoice.customerSnapshot.phone.isNotBlank()) {
            canvas.drawText("Phone: ${invoice.customerSnapshot.phone}", 380f, y, paint)
        }
        y += 20f

        canvas.drawLine(40f, y, 555f, y, paint); y += 14f
        canvas.drawText("Item / Description", 40f, y, Paint(paint).apply { isFakeBoldText = true })
        canvas.drawText("Qty", 320f, y, Paint(paint).apply { isFakeBoldText = true })
        canvas.drawText("Rate", 380f, y, Paint(paint).apply { isFakeBoldText = true })
        canvas.drawText("Total", 480f, y, Paint(paint).apply { isFakeBoldText = true })
        y += 12f
        canvas.drawLine(40f, y, 555f, y, paint); y += 16f

        invoice.lines.forEach { line ->
            val name = line.partSnapshot.name.take(42)
            canvas.drawText(name, 40f, y, paint)
            canvas.drawText(line.qty.toString(), 320f, y, paint)
            canvas.drawText("%.0f".format(line.rate), 380f, y, paint)
            canvas.drawText("%.0f".format(line.lineTotal), 480f, y, paint)
            y += 16f
        }

        y += 10f
        canvas.drawLine(40f, y, 555f, y, paint); y += 18f
        canvas.drawText("Subtotal:", 380f, y, paint)
        canvas.drawText("Rs.${"%,.0f".format(invoice.subtotal)}", 480f, y, paint); y += 16f
        if (invoice.totalDiscount > 0) {
            canvas.drawText("Discount:", 380f, y, paint)
            canvas.drawText("- Rs.${"%,.0f".format(invoice.totalDiscount)}", 480f, y, paint); y += 16f
        }
        y += 6f
        val totalPaint = Paint(paint).apply { textSize = 14f; isFakeBoldText = true }
        canvas.drawText("GRAND TOTAL:", 330f, y, totalPaint)
        canvas.drawText("Rs.${"%,.0f".format(invoice.grandTotal)}", 460f, y, totalPaint)

        y += 30f
        paint.textSize = 9f
        canvas.drawText("Composition taxable person — not eligible to collect tax on supplies.", 40f, y, paint)

        pdf.finishPage(page)

        val dir = File(context.cacheDir, "invoices").apply { mkdirs() }
        val file = File(dir, "${invoice.invoiceNo.replace("/", "_")}.pdf")
        FileOutputStream(file).use { pdf.writeTo(it) }
        pdf.close()
        file
    }.getOrNull()
}
