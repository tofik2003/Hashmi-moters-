package com.hashmimotors.app.ui.reports

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.hashmimotors.app.ui.components.HmCard
import com.hashmimotors.app.ui.components.HmTopBar
import com.hashmimotors.app.ui.theme.Gold
import com.hashmimotors.app.ui.theme.Ivory
import com.hashmimotors.app.ui.theme.IvoryMute
import com.hashmimotors.app.ui.theme.StatusWarning
import com.hashmimotors.app.util.CsvExporter

@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        HmTopBar(
            title = "Reports",
            subtitle = "Paid sales, dues, profit, movers",
            onBack = onBack,
            trailing = {
                TextButton(onClick = {
                    val file = CsvExporter.parts(context, state.parts)
                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/csv"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(intent, "Export catalog CSV"))
                    Toast.makeText(context, "Catalog CSV ready", Toast.LENGTH_SHORT).show()
                }) { Text("CSV", color = Gold) }
            }
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                ReportCard(
                    title = "Today (paid)",
                    rows = listOf(
                        "Sales" to "₹${"%,.0f".format(state.todaySales)}",
                        "Bills" to state.todayBills.toString(),
                        "Items sold" to state.todayItems.toString()
                    )
                )
            }
            item {
                ReportCard(
                    title = "This month",
                    rows = listOf(
                        "Paid sales" to "₹${"%,.0f".format(state.monthSales)}",
                        "Bills" to state.monthBills.toString(),
                        "Avg bill" to "₹${"%,.0f".format(state.avgBillValue)}",
                        "Est. gross profit" to "₹${"%,.0f".format(state.estimatedProfit)}"
                    )
                )
            }
            item {
                ReportCard(
                    title = "Credit / unpaid",
                    rows = listOf(
                        "Open bills" to state.unpaidCount.toString(),
                        "Amount due" to "₹${"%,.0f".format(state.unpaidTotal)}"
                    )
                )
            }
            item {
                ReportCard(
                    title = "Inventory",
                    rows = listOf(
                        "Active SKUs" to state.totalParts.toString(),
                        "Stock at cost" to "₹${"%,.0f".format(state.stockValue)}",
                        "Low stock" to state.lowStockCount.toString()
                    )
                )
            }
            if (state.topSkus.isNotEmpty()) {
                item {
                    HmCard(modifier = Modifier.fillMaxWidth()) {
                        Text("Fast movers", color = Gold, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))
                        state.topSkus.forEach { sku ->
                            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("${sku.qty} × ${sku.name}", color = Ivory, fontSize = 13.sp, modifier = Modifier.weight(1f))
                                Text("₹${"%,.0f".format(sku.amount)}", color = Ivory, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
            if (state.deadStock.isNotEmpty()) {
                item {
                    HmCard(modifier = Modifier.fillMaxWidth()) {
                        Text("Sitting stock (never billed)", color = StatusWarning, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))
                        state.deadStock.forEach { p ->
                            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(p.name, color = Ivory, fontSize = 13.sp, modifier = Modifier.weight(1f))
                                Text("${p.stockQty} pcs", color = IvoryMute, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(40.dp)) }
        }
    }
}

@Composable
private fun ReportCard(title: String, rows: List<Pair<String, String>>) {
    HmCard(modifier = Modifier.fillMaxWidth()) {
        Text(title, color = Gold, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        rows.forEach { (label, value) ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(label, color = IvoryMute, fontSize = 14.sp)
                Text(value, color = Ivory, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
