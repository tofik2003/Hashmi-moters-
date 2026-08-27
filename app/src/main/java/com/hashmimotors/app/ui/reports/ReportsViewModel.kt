package com.hashmimotors.app.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hashmimotors.app.data.repository.InvoiceRepository
import com.hashmimotors.app.data.repository.PartRepository
import com.hashmimotors.app.domain.model.InvoiceStatus
import com.hashmimotors.app.domain.model.Part
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import javax.inject.Inject

data class TopSku(val name: String, val qty: Int, val amount: Double)

data class ReportsUiState(
    val todaySales: Double = 0.0,
    val todayBills: Int = 0,
    val todayItems: Int = 0,
    val monthSales: Double = 0.0,
    val monthBills: Int = 0,
    val avgBillValue: Double = 0.0,
    val totalParts: Int = 0,
    val stockValue: Double = 0.0,
    val lowStockCount: Int = 0,
    val unpaidTotal: Double = 0.0,
    val unpaidCount: Int = 0,
    val estimatedProfit: Double = 0.0,
    val topSkus: List<TopSku> = emptyList(),
    val deadStock: List<Part> = emptyList(),
    val parts: List<Part> = emptyList(),
    val invoiceCount: Int = 0
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    invoiceRepository: InvoiceRepository,
    partRepository: PartRepository
) : ViewModel() {

    val uiState: StateFlow<ReportsUiState> = combine(
        invoiceRepository.getAllInvoices(),
        partRepository.getAllParts(),
        partRepository.getLowStockCount(),
        partRepository.getTotalStockValue()
    ) { invoices, parts, lowStock, stockValue ->
        val live = invoices.filter { it.status != InvoiceStatus.VOID }
        val now = System.currentTimeMillis()
        val dayStart = startOfDay(now)
        val monthStart = startOfMonth(now)
        val today = live.filter { it.date >= dayStart }
        val month = live.filter { it.date >= monthStart }
        val unpaid = live.filter { it.status == InvoiceStatus.UNPAID }
        val costById = parts.associate { it.id to (it.costPrice ?: it.sellingPrice * 0.72) }
        val soldIds = live.flatMap { it.lines.map { l -> l.partId } }.toSet()
        val skuMap = mutableMapOf<String, TopSku>()
        live.forEach { inv ->
            inv.lines.forEach { line ->
                val prev = skuMap[line.partId]
                skuMap[line.partId] = TopSku(
                    name = line.partSnapshot.name,
                    qty = (prev?.qty ?: 0) + line.qty,
                    amount = (prev?.amount ?: 0.0) + line.lineTotal
                )
            }
        }
        val profit = live.sumOf { inv ->
            inv.lines.sumOf { line ->
                val cost = costById[line.partId] ?: (line.rate * 0.72)
                (line.rate - cost) * line.qty
            } - inv.totalDiscount
        }
        ReportsUiState(
            todaySales = today.filter { it.status == InvoiceStatus.PAID }.sumOf { it.grandTotal },
            todayBills = today.size,
            todayItems = today.sumOf { inv -> inv.lines.sumOf { it.qty } },
            monthSales = month.filter { it.status == InvoiceStatus.PAID }.sumOf { it.grandTotal },
            monthBills = month.size,
            avgBillValue = if (month.isNotEmpty()) month.sumOf { it.grandTotal } / month.size else 0.0,
            totalParts = parts.size,
            stockValue = stockValue,
            lowStockCount = lowStock,
            unpaidTotal = unpaid.sumOf { it.grandTotal },
            unpaidCount = unpaid.size,
            estimatedProfit = profit,
            topSkus = skuMap.values.sortedByDescending { it.qty }.take(8),
            deadStock = parts.filter { it.stockQty > 0 && it.id !in soldIds }.take(8),
            parts = parts,
            invoiceCount = live.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReportsUiState())
}

private fun startOfDay(now: Long): Long {
    val cal = Calendar.getInstance()
    cal.timeInMillis = now
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}

private fun startOfMonth(now: Long): Long {
    val cal = Calendar.getInstance()
    cal.timeInMillis = now
    cal.set(Calendar.DAY_OF_MONTH, 1)
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}
