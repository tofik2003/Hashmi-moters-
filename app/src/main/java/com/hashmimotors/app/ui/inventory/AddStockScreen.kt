package com.hashmimotors.app.ui.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hashmimotors.app.ui.catalog.CatalogViewModel
import com.hashmimotors.app.ui.components.AnimatedBigButton

@Composable
fun AddStockScreen(
    inventoryViewModel: InventoryViewModel = hiltViewModel(),
    catalogViewModel: CatalogViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val state by catalogViewModel.uiState.collectAsState()
    val invState by inventoryViewModel.uiState.collectAsState()
    var selectedPartId by remember { mutableStateOf<String?>(null) }
    var qty by remember { mutableStateOf("") }
    var supplierId by remember { mutableStateOf<String?>(null) }

    val selectedPart = state.parts.find { it.id == selectedPartId }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Spacer(modifier = Modifier.height(24.dp))
        // Top bar
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, "Back", tint = Color.White)
            }
            Text(
                text = "Add Stock",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (selectedPart == null) {
            // Search + list
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { catalogViewModel.onSearchChange(it) },
                placeholder = { Text("Search part to add stock...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.parts, key = { it.id }) { part ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedPartId = part.id },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(part.name, color = Color.White, fontWeight = FontWeight.SemiBold)
                            Text(
                                "Current: ${part.stockQty} | MRP ₹${"%,.0f".format(part.mrp)}",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        } else {
            // Selected part view (scrollable so the button is always reachable)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Selected Part", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                        Text(selectedPart.name, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Current stock: ${selectedPart.stockQty}", color = Color.White, fontSize = 14.sp)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = qty,
                    onValueChange = { qty = it.filter { c -> c.isDigit() } },
                    label = { Text("Quantity to add *") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                if (invState.suppliers.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Supplier (optional)", color = Color.White, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    invState.suppliers.forEach { sup ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { supplierId = if (supplierId == sup.id) null else sup.id },
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (supplierId == sup.id) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                else Color.White.copy(alpha = 0.05f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(sup.name, color = Color.White, modifier = Modifier.weight(1f))
                                if (supplierId == sup.id) {
                                    Icon(Icons.Filled.Check, null, tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                AnimatedBigButton(
                    text = "Add ${qty.ifBlank { "0" }} to stock",
                    icon = Icons.Filled.Add,
                    enabled = qty.isNotBlank() && (qty.toIntOrNull() ?: 0) > 0,
                    onClick = {
                        val q = qty.toIntOrNull() ?: 0
                        if (q > 0) {
                            inventoryViewModel.addStock(selectedPartId!!, q, supplierId, "default-user")
                            onSaved()
                        }
                    }
                )
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
