package com.hashmimotors.app.ui.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hashmimotors.app.ui.theme.BrandGold
import com.hashmimotors.app.ui.theme.BrandGoldBright

@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Spacer(modifier = Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Column {
                Text(
                    text = "Sales & Inventory Reports",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Real-time summary & analytics",
                    color = BrandGold,
                    fontSize = 12.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Today's stats
            item {
                ReportCard(
                    title = "Today's Performance",
                    rows = listOf(
                        "Total Revenue" to "₹${"%,.0f".format(state.todaySales)}",
                        "Invoices Issued" to state.todayBills.toString(),
                        "Parts / Units Sold" to "${state.todayItems} pcs",
                        "Avg Bill Size" to "₹${"%,.0f".format(state.avgBillValue)}"
                    )
                )
            }
            // This month
            item {
                ReportCard(
                    title = "This Month",
                    rows = listOf(
                        "Monthly Revenue" to "₹${"%,.0f".format(state.monthSales)}",
                        "Total Invoices" to state.monthBills.toString()
                    )
                )
            }
            // Inventory & Stock
            item {
                ReportCard(
                    title = "Inventory Valuation",
                    rows = listOf(
                        "Catalog SKUs" to state.totalParts.toString(),
                        "Total Stock Value (Cost)" to "₹${"%,.0f".format(state.stockValue)}",
                        "Low Stock Items" to state.lowStockCount.toString()
                    )
                )
            }
            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }
}

@Composable
private fun ReportCard(
    title: String,
    rows: List<Pair<String, String>>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                color = BrandGoldBright,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))
            rows.forEachIndexed { index, (label, value) ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(label, color = Color.White.copy(alpha = 0.75f), fontSize = 13.sp)
                    Text(value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
                if (index < rows.size - 1) {
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).padding(vertical = 2.dp))
                }
            }
        }
    }
}
