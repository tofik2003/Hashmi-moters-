package com.hashmimotors.app.ui.inventory

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hashmimotors.app.domain.model.Part
import com.hashmimotors.app.domain.model.StockMovement
import com.hashmimotors.app.ui.catalog.PartListItem
import com.hashmimotors.app.ui.components.HmCard
import com.hashmimotors.app.ui.components.HmEmptyState
import com.hashmimotors.app.ui.components.HmTopBar
import com.hashmimotors.app.ui.components.hmFieldColors
import com.hashmimotors.app.ui.theme.Gold
import com.hashmimotors.app.ui.theme.Ivory
import com.hashmimotors.app.ui.theme.IvoryMute
import com.hashmimotors.app.ui.theme.StatusError
import com.hashmimotors.app.ui.theme.StatusWarning
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun InventoryScreen(
    viewModel: InventoryViewModel = hiltViewModel(),
    incomingQuery: String = "",
    onIncomingConsumed: () -> Unit = {},
    onBack: () -> Unit,
    onPartClick: (String) -> Unit,
    onAddStock: () -> Unit = {},
    onScan: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var countPart by remember { mutableStateOf<Part?>(null) }

    LaunchedEffect(incomingQuery) {
        if (incomingQuery.isNotBlank()) {
            searchQuery = incomingQuery
            onIncomingConsumed()
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        HmTopBar(
            title = "Inventory",
            subtitle = "Cost ₹${"%,.0f".format(state.totalValue)}  ·  MRP ₹${"%,.0f".format(state.mrpValue)}",
            onBack = onBack,
            trailing = {
                Row {
                    IconButton(onClick = onScan) {
                        Icon(Icons.Filled.QrCodeScanner, "Scan", tint = Gold)
                    }
                    IconButton(onClick = onAddStock) {
                        Icon(Icons.Filled.Add, "Receive", tint = Gold)
                    }
                }
            }
        )

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Mini("SKUs", state.allStock.size.toString(), Modifier.weight(1f))
            Mini("Low", state.lowStock.size.toString(), Modifier.weight(1f), warn = state.lowStock.isNotEmpty())
            Mini("Out", state.outOfStock.size.toString(), Modifier.weight(1f), warn = state.outOfStock.isNotEmpty())
        }
        Spacer(Modifier.height(10.dp))

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            contentColor = Ivory
        ) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("All") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Low") })
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Out") })
            Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }, text = { Text("Ledger") })
        }
        Spacer(Modifier.height(10.dp))

        if (selectedTab != 3) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Name, OEM, barcode…") },
                leadingIcon = { Icon(Icons.Filled.Search, null, tint = IvoryMute) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = hmFieldColors()
            )
            Spacer(Modifier.height(10.dp))
        }

        val parts = when (selectedTab) {
            1 -> state.lowStock
            2 -> state.outOfStock
            else -> state.allStock
        }
        val filtered = if (searchQuery.isBlank()) parts else parts.filter {
            it.name.contains(searchQuery, true) ||
                it.sku.contains(searchQuery, true) ||
                it.barcode.orEmpty().contains(searchQuery, true) ||
                it.oemNumbers.any { oem -> oem.contains(searchQuery, true) }
        }

        when {
            selectedTab == 3 -> LedgerList(state.movements, state.allStock)
            filtered.isEmpty() -> HmEmptyState(
                title = if (selectedTab == 1) "No low-stock parts" else if (selectedTab == 2) "Nothing is out" else "No parts yet",
                body = "Receive stock or add a part from Workshop."
            )
            else -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filtered, key = { it.id }) { part ->
                        Column {
                            PartListItem(part = part, onClick = { onPartClick(part.id) })
                            Row(
                                Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(onClick = {
                                    viewModel.adjustStock(part.id, -1, "Counter −1", "default-user")
                                }) {
                                    Icon(Icons.Filled.Remove, null, tint = IvoryMute)
                                }
                                Text("${part.stockQty}", color = Ivory, fontWeight = FontWeight.Bold)
                                TextButton(onClick = {
                                    viewModel.adjustStock(part.id, 1, "Counter +1", "default-user")
                                }) {
                                    Icon(Icons.Filled.Add, null, tint = Gold)
                                }
                                TextButton(onClick = { countPart = part }) {
                                    Text("Count", color = Gold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                    item { Spacer(Modifier.height(88.dp)) }
                }
            }
        }
    }

    countPart?.let { part ->
        CountDialog(
            part = part,
            onDismiss = { countPart = null },
            onConfirm = { qty, reason ->
                viewModel.setStock(part.id, qty, reason, "default-user")
                countPart = null
            }
        )
    }
}

@Composable
private fun Mini(label: String, value: String, modifier: Modifier = Modifier, warn: Boolean = false) {
    HmCard(modifier = modifier) {
        Text(value, color = if (warn) StatusWarning else Ivory, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(label, color = IvoryMute, fontSize = 11.sp)
    }
}

@Composable
private fun LedgerList(movements: List<StockMovement>, parts: List<Part>) {
    if (movements.isEmpty()) {
        HmEmptyState(title = "No movements yet", body = "Sales, stock-in, and counts appear here.")
        return
    }
    val names = remember(parts) { parts.associate { it.id to it.name } }
    val fmt = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(movements, key = { it.id }) { m ->
            HmCard(modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text(names[m.partId] ?: m.partId.take(8), color = Ivory, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text(
                            listOfNotNull(m.type.name, m.refType, m.reason).joinToString(" · "),
                            color = IvoryMute,
                            fontSize = 11.sp
                        )
                        Text(fmt.format(Date(m.timestamp)), color = IvoryMute, fontSize = 11.sp)
                    }
                    Text(
                        if (m.qty > 0) "+${m.qty}" else "${m.qty}",
                        color = if (m.qty < 0) StatusError else Gold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }
        }
        item { Spacer(Modifier.height(88.dp)) }
    }
}

@Composable
private fun CountDialog(part: Part, onDismiss: () -> Unit, onConfirm: (Int, String) -> Unit) {
    var qty by remember { mutableStateOf(part.stockQty.toString()) }
    var reason by remember { mutableStateOf("Physical count") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Stock take — ${part.name}") },
        text = {
            Column {
                Text("On books: ${part.stockQty}", color = IvoryMute, fontSize = 13.sp)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = qty,
                    onValueChange = { qty = it.filter { c -> c.isDigit() } },
                    label = { Text("Counted qty") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = hmFieldColors()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason") },
                    singleLine = true,
                    colors = hmFieldColors()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(qty.toIntOrNull() ?: part.stockQty, reason) }) {
                Text("Save count", color = Gold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
