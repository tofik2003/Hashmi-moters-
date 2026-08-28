package com.hashmimotors.app.ui.billing

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hashmimotors.app.domain.model.InvoiceLine
import com.hashmimotors.app.domain.model.Part
import com.hashmimotors.app.ui.catalog.CatalogViewModel
import com.hashmimotors.app.ui.catalog.PartListItem
import com.hashmimotors.app.ui.theme.BrandGold

@Composable
fun BillingScreen(
    billingViewModel: BillingViewModel = hiltViewModel(),
    catalogViewModel: CatalogViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onSaved: (String) -> Unit,
    onAddItem: () -> Unit
) {
    val state by billingViewModel.state.collectAsState()
    val catalogState by catalogViewModel.uiState.collectAsState()
    val customers by billingViewModel.customers.collectAsState()
    val quickParts by billingViewModel.quickParts.collectAsState()
    var showPartPicker by remember { mutableStateOf(false) }
    var showBarcodeDialog by remember { mutableStateOf(false) }
    var barcodeText by remember { mutableStateOf("") }
    var showCustomerSuggestions by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Show transient errors (e.g. barcode not found) as a snackbar
    LaunchedEffect(state.error) {
        state.error?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            billingViewModel.clearError()
        }
    }

    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(24.dp)) }

            // Top bar
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                    Text(
                        text = "New Bill",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { showBarcodeDialog = true }) {
                        Icon(
                            Icons.Filled.QrCodeScanner,
                            contentDescription = "Scan barcode",
                            tint = BrandGold
                        )
                    }
                    IconButton(onClick = { billingViewModel.repeatLastBill() }) {
                        Icon(
                            Icons.Filled.History,
                            contentDescription = "Repeat last bill",
                            tint = BrandGold
                        )
                    }
                }
            }

            // Customer section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Person,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Customer",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            if (customers.isNotEmpty()) {
                                TextButton(onClick = { showCustomerSuggestions = !showCustomerSuggestions }) {
                                    Text(
                                        text = if (showCustomerSuggestions) "Hide" else "Saved",
                                        color = BrandGold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = state.customerName,
                            onValueChange = {
                                billingViewModel.setCustomerName(it)
                                billingViewModel.setCustomer(null)
                            },
                            placeholder = { Text("Customer name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = state.customerPhone,
                            onValueChange = {
                                billingViewModel.setCustomerPhone(it)
                                billingViewModel.setCustomer(null)
                            },
                            placeholder = { Text("Phone (optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true
                        )
                        // Saved customers — tap to autofill name + phone
                        if (showCustomerSuggestions && customers.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(customers, key = { it.id }) { customer ->
                                    Card(
                                        modifier = Modifier
                                            .clickable {
                                                billingViewModel.setCustomer(customer)
                                                billingViewModel.setCustomerName(customer.name)
                                                billingViewModel.setCustomerPhone(customer.phone)
                                            },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.12f))
                                    ) {
                                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                            Text(
                                                text = customer.name,
                                                color = Color.White,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            if (customer.phone.isNotBlank()) {
                                                Text(
                                                    text = customer.phone,
                                                    color = Color.White.copy(alpha = 0.6f),
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Quick Add tiles — one tap to add frequent parts
            if (quickParts.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Quick Add",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Most billed parts",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 11.sp
                        )
                    }
                }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(quickParts, key = { it.id }) { part ->
                            QuickAddTile(
                                part = part,
                                alreadyInCart = state.lines.any { it.partId == part.id },
                                onClick = { billingViewModel.addPart(part, qty = 1) }
                            )
                        }
                    }
                }
            }

            // Lines header + Add Item
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Items (${state.totalItems})",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    TextButton(onClick = {
                        catalogViewModel.onSearchChange("")
                        showPartPicker = true
                    }) {
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
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .background(
                                color = Color.White.copy(alpha = 0.05f),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No items added yet",
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }
                }
            } else {
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

            // Bill-level discount
            item {
                OutlinedTextField(
                    value = state.discountText,
                    onValueChange = { billingViewModel.setDiscountText(it) },
                    label = { Text("Bill Discount %") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
            }

            // Notes
            item {
                OutlinedTextField(
                    value = state.notes,
                    onValueChange = { billingViewModel.setNotes(it) },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 1
                )
            }

            // Totals card
            item {
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
            }

            // Save button
            item {
                com.hashmimotors.app.ui.components.AnimatedBigButton(
                    text = if (state.isSaving) "Saving..." else "Save Bill",
                    icon = Icons.Filled.Save,
                    enabled = !state.isSaving && state.lines.isNotEmpty(),
                    onClick = {
                        billingViewModel.saveBill(userId = "default-user")
                    }
                )
            }
        }

        // Auto-navigate when saved
        androidx.compose.runtime.LaunchedEffect(state.savedInvoice) {
            state.savedInvoice?.let { invoice ->
                onSaved(invoice.id)
                billingViewModel.clearCart()
            }
        }

        // Snackbar for transient errors
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
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

    if (showBarcodeDialog) {
        BarcodeDialog(
            barcode = barcodeText,
            onBarcodeChange = { barcodeText = it },
            onConfirm = {
                billingViewModel.addPartByBarcode(barcodeText)
                barcodeText = ""
                showBarcodeDialog = false
            },
            onDismiss = { showBarcodeDialog = false }
        )
    }
}

/**
 * One-tap tile used in the "Quick Add" row on the billing screen.
 */
@Composable
private fun QuickAddTile(
    part: Part,
    alreadyInCart: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                brush = if (alreadyInCart)
                    Brush.linearGradient(listOf(Color(0xFF2BB673).copy(alpha = 0.9f), Color(0xFF1E7A4B)))
                else
                    Brush.linearGradient(listOf(Color.White.copy(alpha = 0.12f), Color.White.copy(alpha = 0.08f)))
            )
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Text(
                text = part.name,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
            Text(
                text = if (alreadyInCart) "In cart ✓" else "₹${"%,.0f".format(part.sellingPrice)}",
                color = if (alreadyInCart) Color.White.copy(alpha = 0.85f) else BrandGold,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Small dialog to type/scan a barcode straight into the bill.
 */
@Composable
private fun BarcodeDialog(
    barcode: String,
    onBarcodeChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Scan / enter barcode") },
        text = {
            Column {
                Text(
                    text = "Scan a barcode or type it below to add the part to this bill.",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = barcode,
                    onValueChange = onBarcodeChange,
                    placeholder = { Text("Barcode number") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = barcode.isNotBlank()
            ) {
                Text("Add", color = BrandGold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White.copy(alpha = 0.7f))
            }
        }
    )
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
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${index + 1}. ${line.partSnapshot.name}",
                    color = Color.White,
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
                    color = Color.White.copy(alpha = 0.6f),
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
                    singleLine = true
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
                    singleLine = true
                )
                // Line total
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Total",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 10.sp
                    )
                    Text(
                        text = "₹${"%,.0f".format(line.lineTotal)}",
                        color = Color.White,
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
    color: Color = Color.White,
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
            color = Color.White.copy(alpha = 0.8f),
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
            .background(Color.White.copy(alpha = 0.2f))
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
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search parts...") },
                leadingIcon = {
                    Icon(Icons.Filled.Add, null, tint = Color.White.copy(alpha = 0.6f))
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (parts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No parts. Add some in Catalog first.",
                        color = Color.White.copy(alpha = 0.6f)
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
