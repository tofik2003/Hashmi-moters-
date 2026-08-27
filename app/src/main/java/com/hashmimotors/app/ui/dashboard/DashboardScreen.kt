package com.hashmimotors.app.ui.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hashmimotors.app.ui.components.AnimatedBarChart
import com.hashmimotors.app.ui.components.AnimatedCounter
import com.hashmimotors.app.ui.components.HmCard
import com.hashmimotors.app.ui.components.HmIconWell
import com.hashmimotors.app.ui.theme.Gold
import com.hashmimotors.app.ui.theme.GoldSoft
import com.hashmimotors.app.ui.theme.Ivory
import com.hashmimotors.app.ui.theme.IvoryMute
import com.hashmimotors.app.ui.theme.StatusWarning
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onSearch: () -> Unit,
    onNewBill: () -> Unit,
    onAddStock: () -> Unit,
    onAddPart: () -> Unit,
    onFitment: () -> Unit,
    onInventory: () -> Unit,
    onReports: () -> Unit,
    onHistory: () -> Unit,
    onSettings: () -> Unit,
    onCustomers: () -> Unit = {},
    onScan: () -> Unit = {},
    onImport: () -> Unit = {},
    onInvoiceClick: (String) -> Unit = {}
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val greeting = remember {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        when {
            hour < 12 -> "Good morning"
            hour < 17 -> "Good afternoon"
            else -> "Good evening"
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(Modifier.height(8.dp))
            Row(
                Modifier.fillMaxWidth().padding(top = 28.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(greeting.uppercase(), color = Gold, fontSize = 11.sp, letterSpacing = 1.6.sp, fontWeight = FontWeight.Medium)
                    Text(state.shopName, color = Ivory, fontSize = 26.sp, fontWeight = FontWeight.SemiBold)
                    Text("Counter · catalog · bills", color = IvoryMute, fontSize = 13.sp)
                }
                IconButton(onClick = onSettings) {
                    Icon(Icons.Filled.Settings, "Settings", tint = IvoryMute)
                }
            }
        }

        item {
            HmCard(modifier = Modifier.fillMaxWidth()) {
                Text("Today’s counter", color = IvoryMute, fontSize = 12.sp, letterSpacing = 0.8.sp)
                Spacer(Modifier.height(4.dp))
                AnimatedCounter(target = state.todaySales, fontSize = 36, textColor = GoldSoft)
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    MiniStat("Bills", state.todayBills.toString())
                    MiniStat("Items sold", state.todayItems.toString())
                    MiniStat("In catalog", state.totalParts.toString())
                }
            }
        }

        item {
            HmCard(modifier = Modifier.fillMaxWidth(), onClick = onScan) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    HmIconWell(Icons.Filled.QrCodeScanner)
                    Spacer(Modifier.padding(8.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Fast scan", color = Ivory, fontWeight = FontWeight.SemiBold)
                        Text("In / out of stock, or identify a label", color = IvoryMute, fontSize = 12.sp)
                    }
                    Text("Open", color = Gold, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QuickAction("New bill", Icons.Filled.ReceiptLong, Modifier.weight(1f), onNewBill)
                QuickAction("Catalog", Icons.Filled.Search, Modifier.weight(1f), onSearch)
            }
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QuickAction("Receive", Icons.Filled.Inventory, Modifier.weight(1f), onAddStock)
                QuickAction("Add part", Icons.Filled.Add, Modifier.weight(1f), onAddPart)
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                HmCard(modifier = Modifier.weight(1f), onClick = onInventory) {
                    Text("Stock value", color = IvoryMute, fontSize = 11.sp)
                    Text("₹${"%,.0f".format(state.totalStockValue)}", color = Ivory, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                HmCard(modifier = Modifier.weight(1f), onClick = onReports) {
                    Text("This week", color = IvoryMute, fontSize = 11.sp)
                    Text("₹${"%,.0f".format(state.weekTotal)}", color = Ivory, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            HmCard(modifier = Modifier.fillMaxWidth()) {
                Text("Sales · last 7 days", color = Ivory, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(10.dp))
                AnimatedBarChart(
                    data = state.weekSales,
                    labels = listOf("−6", "−5", "−4", "−3", "−2", "−1", "Today"),
                    barColor = Gold,
                    highlightColor = GoldSoft
                )
            }
        }

        if (state.lowStockCount > 0) {
            item {
                HmCard(modifier = Modifier.fillMaxWidth(), onClick = onInventory) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        HmIconWell(Icons.Filled.Warning, tint = StatusWarning)
                        Spacer(Modifier.padding(8.dp))
                        Column {
                            Text("Low stock", color = Ivory, fontWeight = FontWeight.SemiBold)
                            Text("${state.lowStockCount} parts at or below reorder level", color = IvoryMute, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        item {
            Row(
                Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Recent bills", color = Ivory, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Text("See all", color = Gold, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable { onHistory() })
            }
        }

        if (state.recentInvoices.isEmpty()) {
            item {
                HmCard(modifier = Modifier.fillMaxWidth(), onClick = onNewBill) {
                    Text("No bills yet", color = Ivory, fontWeight = FontWeight.SemiBold)
                    Text("Create a bill from the counter tab. The whole shop is free — nothing is locked.", color = IvoryMute, fontSize = 12.sp)
                }
            }
        } else {
            items(state.recentInvoices, key = { it.id }) { inv ->
                HmCard(modifier = Modifier.fillMaxWidth(), onClick = { onInvoiceClick(inv.id) }) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(inv.invoiceNo, color = Ivory, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text(
                                "${inv.customerSnapshot.name} · ${SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(inv.date))}",
                                color = IvoryMute,
                                fontSize = 12.sp
                            )
                        }
                        Text("₹${"%,.0f".format(inv.grandTotal)}", color = GoldSoft, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item { Spacer(Modifier.height(96.dp)) }
    }
}

@Composable
private fun MiniStat(label: String, value: String) {
    Column {
        Text(value, color = Ivory, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(label, color = IvoryMute, fontSize = 11.sp)
    }
}

@Composable
private fun QuickAction(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    HmCard(modifier = modifier, onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            HmIconWell(icon, size = 36)
            Spacer(Modifier.padding(6.dp))
            Text(label, color = Ivory, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        }
    }
}
