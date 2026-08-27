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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hashmimotors.app.data.repository.InvoiceRepository
import com.hashmimotors.app.domain.model.Invoice
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import com.hashmimotors.app.ui.components.HmEmptyState
import com.hashmimotors.app.ui.components.HmTopBar
import com.hashmimotors.app.ui.theme.Ivory
import com.hashmimotors.app.ui.components.hmFieldColors

@HiltViewModel
class InvoiceHistoryViewModel @Inject constructor(
    invoiceRepo: InvoiceRepository
) : ViewModel() {
    val invoices: StateFlow<List<Invoice>> = invoiceRepo.getAllInvoices()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

@Composable
fun InvoiceHistoryScreen(
    viewModel: InvoiceHistoryViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onInvoiceClick: (String) -> Unit
) {
    val invoices by viewModel.invoices.collectAsState()
    var query by remember { mutableStateOf("") }
    val filtered = if (query.isBlank()) invoices else invoices.filter {
        it.invoiceNo.contains(query, true) || it.customerSnapshot.name.contains(query, true)
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        HmTopBar(title = "Invoice History", subtitle = "Search by number or customer", onBack = onBack)
        Spacer(modifier = Modifier.height(8.dp))

        androidx.compose.material3.OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Search number or customer") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
                colors = hmFieldColors()
            )
        Spacer(modifier = Modifier.height(12.dp))

        if (filtered.isEmpty()) {
            HmEmptyState(
                title = if (query.isBlank()) "No bills yet" else "No matching bills",
                body = "Save a bill from the counter to see it here.",
                icon = Icons.Filled.Receipt
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filtered, key = { it.id }) { invoice ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onInvoiceClick(invoice.id) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Ivory.copy(alpha = 0.08f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Receipt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    invoice.invoiceNo,
                                    color = Ivory,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    invoice.customerSnapshot.name,
                                    color = Ivory.copy(alpha = 0.7f),
                                    fontSize = 12.sp
                                )
                                Text(
                                    SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                                        .format(Date(invoice.date)),
                                    color = Ivory.copy(alpha = 0.5f),
                                    fontSize = 11.sp
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "₹${"%,.0f".format(invoice.grandTotal)}",
                                    color = Ivory,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    invoice.status.name + " · " + invoice.paymentMode,
                                    color = Ivory.copy(alpha = 0.5f),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
