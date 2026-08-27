package com.hashmimotors.app.ui.inventory

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hashmimotors.app.domain.model.Supplier
import com.hashmimotors.app.ui.components.HmCard
import com.hashmimotors.app.ui.components.HmEmptyState
import com.hashmimotors.app.ui.components.HmTopBar
import com.hashmimotors.app.ui.components.hmFieldColors
import com.hashmimotors.app.ui.theme.Gold
import com.hashmimotors.app.ui.theme.Ivory
import com.hashmimotors.app.ui.theme.IvoryMute
import com.hashmimotors.app.ui.theme.StatusError

@Composable
fun SupplierScreen(
    viewModel: InventoryViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var showAdd by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        HmTopBar(
            title = "Suppliers",
            subtitle = "${state.suppliers.size} vendors",
            onBack = onBack,
            trailing = {
                IconButton(onClick = { showAdd = true }) {
                    Icon(Icons.Filled.Add, "Add", tint = Gold)
                }
            }
        )
        if (state.suppliers.isEmpty()) {
            HmEmptyState(
                title = "No suppliers yet",
                body = "Add a vendor so stock-in can be tagged.",
                icon = Icons.Filled.LocalShipping
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.suppliers, key = { it.id }) { sup ->
                    HmCard(modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(sup.name, color = Ivory, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                                if (sup.phone.isNotBlank()) Text(sup.phone, color = IvoryMute, fontSize = 13.sp)
                                if (!sup.gstin.isNullOrBlank()) Text("GSTIN ${sup.gstin}", color = IvoryMute, fontSize = 12.sp)
                                if (!sup.paymentTerms.isNullOrBlank()) Text(sup.paymentTerms ?: "", color = Gold, fontSize = 12.sp)
                            }
                            IconButton(onClick = { viewModel.deleteSupplier(sup) }) {
                                Icon(Icons.Filled.Delete, "Delete", tint = StatusError)
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(40.dp)) }
            }
        }
    }

    if (showAdd) {
        var name by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var gstin by remember { mutableStateOf("") }
        var terms by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAdd = false },
            title = { Text("Add supplier") },
            text = {
                Column {
                    OutlinedTextField(name, { name = it }, label = { Text("Name *") }, singleLine = true, colors = hmFieldColors())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        phone, { phone = it }, label = { Text("Phone") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true, colors = hmFieldColors()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(gstin, { gstin = it.uppercase() }, label = { Text("GSTIN") }, singleLine = true, colors = hmFieldColors())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(terms, { terms = it }, label = { Text("Payment terms") }, singleLine = true, colors = hmFieldColors())
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (name.isNotBlank()) {
                        viewModel.saveSupplier(
                            Supplier(
                                name = name.trim(),
                                phone = phone.trim(),
                                gstin = gstin.ifBlank { null },
                                paymentTerms = terms.ifBlank { null }
                            )
                        )
                        showAdd = false
                    }
                }) { Text("Save", color = Gold) }
            },
            dismissButton = { TextButton(onClick = { showAdd = false }) { Text("Cancel") } }
        )
    }
}
