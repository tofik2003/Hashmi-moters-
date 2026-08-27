package com.hashmimotors.app.ui.billing

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hashmimotors.app.data.repository.InvoiceRepository
import com.hashmimotors.app.domain.model.Invoice
import com.hashmimotors.app.domain.model.InvoiceStatus
import com.hashmimotors.app.ui.components.HmEmptyState
import com.hashmimotors.app.ui.components.HmTopBar
import com.hashmimotors.app.ui.components.hmFieldColors
import com.hashmimotors.app.ui.theme.Gold
import com.hashmimotors.app.ui.theme.Ivory
import com.hashmimotors.app.util.CsvExporter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

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
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf<InvoiceStatus?>(null) }
    val filtered = invoices.filter { inv ->
        val q = query.isBlank() ||
            inv.invoiceNo.contains(query, true) ||
            inv.customerSnapshot.name.contains(query, true) ||
            inv.customerSnapshot.phone.contains(query, true)
        val s = statusFilter == null || inv.status == statusFilter
        q && s
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        HmTopBar(
            title = "Invoice History",
            subtitle = "Search, filter, export",
            onBack = onBack,
            trailing = {
                TextButton(onClick = {
                    if (filtered.isEmpty()) {
                        Toast.makeText(context, "Nothing to export", Toast.LENGTH_SHORT).show()
                        return@TextButton
                    }
                    val file = CsvExporter.invoices(context, filtered)
                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/csv"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(intent, "Export bills CSV"))
                }) { Text("CSV", color = Gold) }
            }
        )
        Spacer(modifier = Modifier.height(8.dp))

        androidx.compose.material3.OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Search number, customer, phone") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = hmFieldColors()
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val chips = listOf<Pair<String, InvoiceStatus?>>(
                "All" to null,
                "Paid" to InvoiceStatus.PAID,
                "Credit" to InvoiceStatus.UNPAID,
                "Void" to InvoiceStatus.VOID
            )
            items(chips.size) { idx ->
                val (label, status) = chips[idx]
                FilterChip(
                    selected = statusFilter == status,
                    onClick = { statusFilter = status },
                    label = { Text(label) }
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (filtered.isEmpty()) {
            HmEmptyState(
                title = if (invoices.isEmpty()) "No bills yet" else "No matching bills",
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
