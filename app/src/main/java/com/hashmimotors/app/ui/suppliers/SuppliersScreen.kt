package com.hashmimotors.app.ui.suppliers

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hashmimotors.app.data.repository.SupplierRepository
import com.hashmimotors.app.domain.model.Supplier
import com.hashmimotors.app.ui.theme.BrandGoldBright
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SuppliersViewModel @Inject constructor(
    private val supplierRepository: SupplierRepository
) : ViewModel() {

    val suppliers: StateFlow<List<Supplier>> = supplierRepository.getAllSuppliers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addSupplier(
        name: String,
        phone: String = "",
        email: String = "",
        address: String = "",
        gstin: String = "",
        paymentTerms: String = "",
        notes: String = ""
    ) {
        if (name.isBlank()) return
        viewModelScope.launch {
            supplierRepository.saveSupplier(
                Supplier(
                    name = name.trim(),
                    phone = phone.trim(),
                    email = email.ifBlank { null },
                    address = address.ifBlank { null },
                    gstin = gstin.ifBlank { null },
                    paymentTerms = paymentTerms.ifBlank { null },
                    notes = notes.ifBlank { null }
                )
            )
        }
    }

    fun deleteSupplier(supplier: Supplier) {
        viewModelScope.launch { supplierRepository.deleteSupplier(supplier) }
    }
}

@Composable
fun SuppliersScreen(
    viewModel: SuppliersViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val suppliers by viewModel.suppliers.collectAsState()
    var showAdd by remember { mutableStateOf(false) }
    var toDelete by remember { mutableStateOf<Supplier?>(null) }

    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, "Back", tint = Color.White)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Suppliers",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${suppliers.size} supplier${if (suppliers.size != 1) "s" else ""}",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            if (suppliers.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.LocalShipping,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.3f),
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No suppliers yet",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tap + to add your first supplier",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(suppliers, key = { it.id }) { supplier ->
                        SupplierCard(
                            supplier = supplier,
                            onDelete = { toDelete = supplier }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAdd = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(Icons.Filled.Add, "Add Supplier", tint = Color.White)
        }
    }

    if (showAdd) {
        AddSupplierDialog(
            onDismiss = { showAdd = false },
            onAdd = { name, phone, email, address, gstin, terms, notes ->
                viewModel.addSupplier(name, phone, email, address, gstin, terms, notes)
                showAdd = false
            }
        )
    }

    toDelete?.let { supplier ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("Delete supplier?") },
            text = { Text("Remove \"${supplier.name}\" from your supplier list?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteSupplier(supplier)
                    toDelete = null
                }) {
                    Text("Delete", color = Color(0xFFFF8A80))
                }
            },
            dismissButton = {
                TextButton(onClick = { toDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SupplierCard(supplier: Supplier, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = supplier.name.take(1).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = supplier.name,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                val details = listOfNotNull(
                    supplier.phone.takeIf { it.isNotBlank() },
                    supplier.gstin?.takeIf { it.isNotBlank() }?.let { "GST: $it" },
                    supplier.paymentTerms?.takeIf { it.isNotBlank() }
                )
                if (details.isNotEmpty()) {
                    Text(
                        text = details.joinToString(" · "),
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Delete",
                    tint = Color(0xFFFF6B6B)
                )
            }
        }
    }
}

@Composable
private fun AddSupplierDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String, String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var gstin by remember { mutableStateOf("") }
    var terms by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Supplier") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = gstin,
                    onValueChange = { gstin = it },
                    label = { Text("GSTIN") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = terms,
                    onValueChange = { terms = it },
                    label = { Text("Payment terms (e.g. 30 days)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = { onAdd(name, phone, email, address, gstin, terms, notes) }
            ) {
                Text("Add", color = BrandGoldBright)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
