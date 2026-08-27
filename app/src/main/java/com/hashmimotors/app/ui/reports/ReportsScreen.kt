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
import androidx.compose.material3.MaterialTheme
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
import com.hashmimotors.app.ui.theme.Ivory

@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        com.hashmimotors.app.ui.components.HmTopBar(
            title = "Reports",
            subtitle = "From bills saved on this phone",
            onBack = onBack
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Today's stats
            item {
                ReportCard(
                    title = "Today",
                    rows = listOf(
                        "Total Sales" to "₹${"%,.0f".format(state.todaySales)}",
                        "Bills" to state.todayBills.toString(),
                        "Items Sold" to state.todayItems.toString()
                    )
                )
            }
            // This month
            item {
                ReportCard(
                    title = "This Month",
                    rows = listOf(
                        "Total Sales" to "₹${"%,.0f".format(state.monthSales)}",
                        "Bills" to state.monthBills.toString(),
                        "Avg per Bill" to "₹${"%,.0f".format(state.avgBillValue)}"
                    )
                )
            }
            // Inventory
            item {
                ReportCard(
                    title = "Inventory",
                    rows = listOf(
                        "Total Parts" to state.totalParts.toString(),
                        "Stock Value (at cost)" to "₹${"%,.0f".format(state.stockValue)}",
                        "Low Stock" to state.lowStockCount.toString()
                    )
                )
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
        colors = CardDefaults.cardColors(containerColor = Ivory.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            rows.forEach { (label, value) ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(label, color = Ivory.copy(alpha = 0.7f), fontSize = 14.sp)
                    Text(value, color = Ivory, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
