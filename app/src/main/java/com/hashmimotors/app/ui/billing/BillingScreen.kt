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
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.runtime.LaunchedEffect
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
import com.hashmimotors.app.domain.model.Part
import com.hashmimotors.app.ui.catalog.CatalogViewModel
import com.hashmimotors.app.ui.catalog.PartListItem
import com.hashmimotors.app.ui.components.AnimatedBigButton
import com.hashmimotors.app.ui.components.GlassTextField
import com.hashmimotors.app.ui.sound.Feedback
import com.hashmimotors.app.ui.sound.LocalAppFeedback
import com.hashmimotors.app.ui.theme.BrandGold
import com.hashmimotors.app.ui.theme.BrandGoldBright

@Composable
fun BillingScreen(
    billingViewModel: BillingViewModel = hiltViewModel(),
    catalogViewModel: CatalogViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onSaved: (String) -> Unit,
    onAddItem: () -> Unit = {},
    onScanToBill: () -> Unit = {}
) {
    val state by billingViewModel.state.collectAsState()
    val catalogState by catalogViewModel.uiState.collectAsState()
    val customers by billingViewModel.customers.collectAsState()
    val quickParts by billingViewModel.quickParts.collectAsState()
    val feedback = LocalAppFeedback.current

    var showPartPicker by remember { mutableStateOf(false) }
    var showCustomerPicker by remember { mutableStateOf(false) }

    LaunchedEffect(state.savedInvoice) {
        state.savedInvoice?.let { invoice ->
            Feedback.success(feedback)
            onSaved(invoice.id)
            billingViewModel.clearCart()
        }
    }

    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(24.dp))

            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "New Bill",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Bill of Supply • Composition",
                        color = BrandGold,
                        fontSize = 11.sp
                    )
                }
                IconButton(onClick = {
                    Feedback.tap(feedback)
                    billingViewModel.repeatLastBill()
                }) {
                    Icon(Icons.Filled.History, contentDescription = "Repeat Last Bill", tint = BrandGoldBright)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Add Tiles Carousel
            if (quickParts.isNotEmpty()) {
                Text(
                    text = "⚡ Quick Add",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(end = 8.dp)
                ) {
                    items(quickParts, key = { it.id }) { part ->
                        QuickPartChip(
                            part = part,
                            onClick = {
                                Feedback.tap(feedback)
                                billingViewModel.addPart(part, qty = 1)
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Customer Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showCustomerPicker = !showCustomerPicker },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(BrandGold.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Person, null, tint = BrandGoldBright, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = state.customerName.ifBlank { "Walk-in Customer" },
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (state.customerPhone.isNotBlank()) {
                                Text(
                                    text = state.customerPhone,
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                        Text(
                            text = if (showCustomerPicker) "Close" else "Change",
                            color = BrandGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (showCustomerPicker) {
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = state.customerName,
                            onValueChange = { billingViewModel.setCustomerName(it) },
                            label = { Text("Customer Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = state.customerPhone,
                            onValueChange = { billingViewModel.setCustomerPhone(it) },
                            label = { Text("Customer Phone (for WhatsApp bill)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Cart Items Header & Add Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Items (${state.totalItems})",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { showPartPicker = true }) {
                        Icon(Icons.Filled.Add, null, tint = BrandGoldBright, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Item", color = BrandGoldBright, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Cart Items List
            if (state.lines.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(12.dp))
                        .clickable { showPartPicker = true },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🛒", fontSize = 36.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Cart is empty",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Tap + Add Item or choose from Quick Add above",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                    }
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
                        BillLineCard(
                            line = line,
                            index = idx,
                            onQtyInc = {
                                Feedback.tap(feedback)
                                billingViewModel.updateLineQty(idx, line.qty + 1)
                            },
                            onQtyDec = {
                                Feedback.tap(feedback)
                                billingViewModel.updateLineQty(idx, line.qty - 1)
                            },
                            onRateChange = { newRate -> billingViewModel.updateLineRate(idx, newRate) },
                            onDiscountChange = { newDisc -> billingViewModel.updateLineDiscount(idx, newDisc) },
                            onRemove = {
                                Feedback.tap(feedback)
                                billingViewModel.removeLine(idx)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Totals Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.12f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Subtotal", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                        Text("₹${"%,.0f".format(state.subtotal)}", color = Color.White, fontSize = 13.sp)
                    }
                    if (state.totalDiscount > 0) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Discount", color = Color(0xFFFFA000), fontSize = 13.sp)
                            Text("- ₹${"%,.0f".format(state.totalDiscount)}", color = Color(0xFFFFA000), fontSize = 13.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.2f)))
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Grand Total", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "₹${"%,.0f".format(state.grandTotal)}",
                            color = BrandGoldBright,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Save Bill Action
            AnimatedBigButton(
                text = if (state.isSaving) "Saving Bill..." else "Save Bill (₹${"%,.0f".format(state.grandTotal)})",
                icon = Icons.Filled.Save,
                enabled = !state.isSaving && state.lines.isNotEmpty(),
                onClick = {
                    billingViewModel.saveBill(userId = "shop-owner")
                }
            )
            Spacer(modifier = Modifier.height(56.dp))
        }
    }

    if (showPartPicker) {
        PartPickerSheet(
            parts = catalogState.parts,
            searchQuery = catalogState.searchQuery,
            onSearchChange = { catalogViewModel.onSearchChange(it) },
            onPartClick = { part ->
                Feedback.tap(feedback)
                billingViewModel.addPart(part, qty = 1)
                showPartPicker = false
            },
            onDismiss = { showPartPicker = false }
        )
    }
}

@Composable
private fun QuickPartChip(
    part: Part,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.1f))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Column {
            Text(
                text = part.name,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
            Text(
                text = "₹${"%,.0f".format(part.sellingPrice)}",
                color = BrandGoldBright,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun BillLineCard(
    line: InvoiceLine,
    index: Int,
    onQtyInc: () -> Unit,
    onQtyDec: () -> Unit,
    onRateChange: (Double) -> Unit,
    onDiscountChange: (Double) -> Unit,
    onRemove: () -> Unit
) {
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
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onRemove, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Filled.Delete, contentDescription = "Remove", tint = Color(0xFFFF6B6B), modifier = Modifier.size(18.dp))
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
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Qty Stepper
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                            .clickable { onQtyDec() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Remove, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                    Text(
                        text = " ${line.qty} ",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(BrandGold)
                            .clickable { onQtyInc() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Add, null, tint = Color(0xFF1A1A2E), modifier = Modifier.size(16.dp))
                    }
                }

                // Rate x Qty
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "@ ₹${"%,.0f".format(line.rate)}",
                        color = Color.White.copy(alpha = 0.65f),
                        fontSize = 12.sp
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PartPickerSheet(
    parts: List<Part>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onPartClick: (Part) -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.material3.ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF15193B)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Pick Part for Bill",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            GlassTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                label = "Search Part",
                placeholder = "Search name, OEM, brand...",
                leadingIcon = Icons.Filled.Search,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))
            if (parts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No matching parts found.", color = Color.White.copy(alpha = 0.6f))
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(parts, key = { it.id }) { part ->
                        PartListItem(part = part, onClick = { onPartClick(part) })
                    }
                }
            }
        }
    }
}
