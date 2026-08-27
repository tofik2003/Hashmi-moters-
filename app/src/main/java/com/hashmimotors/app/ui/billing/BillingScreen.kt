package com.hashmimotors.app.ui.billing

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hashmimotors.app.domain.model.InvoiceLine
import com.hashmimotors.app.ui.catalog.CatalogViewModel
import com.hashmimotors.app.ui.catalog.PartListItem
import com.hashmimotors.app.ui.components.HmTopBar
import com.hashmimotors.app.ui.components.hmFieldColors
import com.hashmimotors.app.ui.theme.Ivory
import com.hashmimotors.app.ui.theme.Gold

@Composable
fun BillingScreen(
    billingViewModel: BillingViewModel = hiltViewModel(),
    catalogViewModel: CatalogViewModel = hiltViewModel(),
    incomingBarcode: String = "",
    onIncomingConsumed: () -> Unit = {},
    onBack: () -> Unit,
    onSaved: (String) -> Unit,
    onScanItem: () -> Unit = {},
    onAddItem: () -> Unit = {}
) {
    val state by billingViewModel.state.collectAsState()
    val catalogState by catalogViewModel.uiState.collectAsState()
    var customerName by remember { mutableStateOf("Walk-in Customer") }
    var customerPhone by remember { mutableStateOf("") }
    var billDiscountText by remember { mutableStateOf("0") }
    var notes by remember { mutableStateOf("") }
    var showPartPicker by remember { mutableStateOf(false) }
    var scanMessage by remember { mutableStateOf<String?>(null) }

    androidx.compose.runtime.LaunchedEffect(incomingBarcode, catalogState.parts) {
        if (incomingBarcode.isBlank()) return@LaunchedEffect
        val match = catalogState.parts.firstOrNull {
            it.barcode.equals(incomingBarcode, ignoreCase = true) ||
                it.sku.equals(incomingBarcode, ignoreCase = true) ||
                it.oemNumbers.any { oem -> oem.equals(incomingBarcode, ignoreCase = true) }
        }
        if (match != null) {
            billingViewModel.addPart(match, qty = 1)
            scanMessage = "Added ${match.name}"
        } else {
            catalogViewModel.onSearchChange(incomingBarcode)
            showPartPicker = true
            scanMessage = "No exact match for $incomingBarcode — pick a part"
        }
        onIncomingConsumed()
    }

    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Column(modifier = Modifier.fillMaxSize()) {
            HmTopBar(title = "New Bill", subtitle = "Scan or add parts, then save", onBack = onBack)
            Spacer(modifier = Modifier.height(8.dp))
            if (!scanMessage.isNullOrBlank()) {
                Text(scanMessage ?: "", color = Gold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))
            }
            if (!state.error.isNullOrBlank()) {
                Text(state.error ?: "", color = Color(0xFFFF6B6B), fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Customer section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Ivory.copy(alpha = 0.08f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Person,
                            contentDescription = null,
                            tint = Ivory.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Customer",
                            color = Ivory,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        placeholder = { Text("Customer name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                colors = hmFieldColors()
            )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customerPhone,
                        onValueChange = { customerPhone = it },
                        placeholder = { Text("Phone (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                colors = hmFieldColors()
            )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Lines section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Items (${state.totalItems})",
                    color = Ivory,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Row {
                    TextButton(onClick = onScanItem) {
                        Text("Scan", color = Gold)
                    }
                    TextButton(onClick = { showPartPicker = true }) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Add Item",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Lines list
            if (state.lines.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(
                            color = Ivory.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No items added yet",
                        color = Ivory.copy(alpha = 0.5f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.lines.size) { idx ->
                        val line = state.lines[idx]
                        BillLineItem(
                            line = line,
                            index = idx,
                            onQtyChange = { newQty -> billingViewModel.updateLineQty(idx, newQty) },
                            onRateChange = { newRate -> billingViewModel.updateLineRate(idx, newRate) },
                            onRemove = { billingViewModel.removeLine(idx) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bill-level discount + notes
            OutlinedTextField(
                value = billDiscountText,
                onValueChange = {
                    billDiscountText = it.filter { c -> c.isDigit() || c == '.' }
                    billDiscountText.toDoubleOrNull()?.let { d -> billingViewModel.setBillDiscount(d) }
                },
                label = { Text("Bill Discount %") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                colors = hmFieldColors()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = notes,
                onValueChange = {
                    notes = it
                    billingViewModel.setNotes(it)
                },
                label = { Text("Notes (optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 1,
                colors = hmFieldColors()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text("Payment", color = Ivory, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(6.dp))
            androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf("CASH", "UPI", "CARD", "CREDIT")) { mode ->
                    val selected = state.paymentMode == mode
                    androidx.compose.material3.FilterChip(
                        selected = selected,
                        onClick = { billingViewModel.setPaymentMode(mode) },
                        label = { Text(if (mode == "CREDIT") "Credit" else mode.lowercase().replaceFirstChar { it.uppercase() }) }
                    )
                }
            }
            if (state.customers.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Saved customers", color = IvoryMute, fontSize = 12.sp)
                androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.customers.take(12), key = { it.id }) { c ->
                        androidx.compose.material3.AssistChip(
                            onClick = {
                                customerName = c.name
                                customerPhone = c.phone
                                billingViewModel.setCustomer(c)
                            },
                            label = { Text(c.name) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Totals card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    TotalRow("Subtotal", state.subtotal)
                    if (state.totalDiscount > 0) {
                        TotalRow("Discount", -state.totalDiscount, color = Color(0xFFFFA000))
                    }
                    HorizontalDivider()
                    TotalRow("Total", state.grandTotal, bold = true, big = true)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Save button
            com.hashmimotors.app.ui.components.AnimatedBigButton(
                text = if (state.isSaving) "Saving..." else "Save Bill",
                icon = Icons.Filled.Save,
                enabled = !state.isSaving && state.lines.isNotEmpty(),
                onClick = {
                    billingViewModel.saveBill(
                        userId = "default-user",
                        walkInName = customerName,
                        walkInPhone = customerPhone
                    )
                }
            )

            // Auto-navigate when saved
            androidx.compose.runtime.LaunchedEffect(state.savedInvoice) {
                state.savedInvoice?.let { invoice ->
                    onSaved(invoice.id)
                    billingViewModel.clearCart()
                }
            }
        }
    }

    if (showPartPicker) {
        PartPickerSheet(
            parts = catalogState.parts,
            searchQuery = catalogState.searchQuery,
            onSearchChange = { catalogViewModel.onSearchChange(it) },
            onPartClick = { part ->
                billingViewModel.addPart(part, qty = 1)
                showPartPicker = false
            },
            onDismiss = { showPartPicker = false }
        )
    }
}

@Composable
private fun BillLineItem(
    line: InvoiceLine,
    index: Int,
    onQtyChange: (Int) -> Unit,
    onRateChange: (Double) -> Unit,
    onRemove: () -> Unit
) {
    var qtyText by remember(line.qty) { mutableStateOf(line.qty.toString()) }
    var rateText by remember(line.rate) { mutableStateOf(line.rate.toString()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Ivory.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${index + 1}. ${line.partSnapshot.name}",
                    color = Ivory,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                    maxLines = 2
                )
                IconButton(onClick = onRemove) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Remove",
                        tint = Color(0xFFFF6B6B)
                    )
                }
            }
            if (line.partSnapshot.oemNumbers.isNotEmpty()) {
                Text(
                    text = "OEM: ${line.partSnapshot.oemNumbers.first()}",
                    color = Ivory.copy(alpha = 0.6f),
                    fontSize = 11.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Qty
                OutlinedTextField(
                    value = qtyText,
                    onValueChange = {
                        qtyText = it.filter { c -> c.isDigit() }
                        qtyText.toIntOrNull()?.let { onQtyChange(it) }
                    },
                    label = { Text("Qty") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                colors = hmFieldColors()
            )
                // Rate
                OutlinedTextField(
                    value = rateText,
                    onValueChange = {
                        rateText = it.filter { c -> c.isDigit() || c == '.' }
                        rateText.toDoubleOrNull()?.let { onRateChange(it) }
                    },
                    label = { Text("Rate") },
                    modifier = Modifier.weight(1.5f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                colors = hmFieldColors()
            )
                // Line total
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Total",
                        color = Ivory.copy(alpha = 0.6f),
                        fontSize = 10.sp
                    )
                    Text(
                        text = "₹${"%,.0f".format(line.lineTotal)}",
                        color = Ivory,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun TotalRow(
    label: String,
    value: Double,
    color: Color = Ivory,
    bold: Boolean = false,
    big: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = Ivory.copy(alpha = 0.8f),
            fontSize = if (big) 18.sp else 14.sp,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = "₹${"%,.0f".format(value)}",
            color = color,
            fontSize = if (big) 24.sp else 14.sp,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.SemiBold
        )
    }
}

@Composable
private fun HorizontalDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Ivory.copy(alpha = 0.2f))
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartPickerSheet(
    parts: List<com.hashmimotors.app.domain.model.Part>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onPartClick: (com.hashmimotors.app.domain.model.Part) -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.material3.ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1A2E)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text(
                text = "Add Item to Bill",
                color = Ivory,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search parts...") },
                leadingIcon = {
                    Icon(Icons.Filled.Add, null, tint = Ivory.copy(alpha = 0.6f))
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = hmFieldColors()
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (parts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No parts. Add some in Catalog first.",
                        color = Ivory.copy(alpha = 0.6f)
                    )
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(parts, key = { it.id }) { part ->
                        PartListItem(part = part, onClick = { onPartClick(part) })
                    }
                }
            }
        }
    }
}
