package com.hashmimotors.app.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hashmimotors.app.data.repository.InvoiceRepository
import com.hashmimotors.app.data.repository.PartRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import javax.inject.Inject

data class ReportsUiState(
    val todaySales: Double = 0.0,
    val todayBills: Int = 0,
    val todayItems: Int = 0,
    val monthSales: Double = 0.0,
    val monthBills: Int = 0,
    val avgBillValue: Double = 0.0,
    val totalParts: Int = 0,
    val stockValue: Double = 0.0,
    val lowStockCount: Int = 0
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    invoiceRepository: InvoiceRepository,
    partRepository: PartRepository
) : ViewModel() {

    val uiState: StateFlow<ReportsUiState> = combine(
        invoiceRepository.getAllInvoices(),
        partRepository.getPartCount(),
        partRepository.getLowStockCount(),
        partRepository.getTotalStockValue()
    ) { invoices, totalParts, lowStock, stockValue ->
        val now = System.currentTimeMillis()
        val dayStart = startOfDay(now)
        val monthStart = startOfMonth(now)
        val today = invoices.filter { it.date >= dayStart }
        val month = invoices.filter { it.date >= monthStart }
        ReportsUiState(
            todaySales = today.sumOf { it.grandTotal },
            todayBills = today.size,
            todayItems = today.sumOf { inv -> inv.lines.sumOf { it.qty } },
            monthSales = month.sumOf { it.grandTotal },
            monthBills = month.size,
            avgBillValue = if (month.isNotEmpty()) month.sumOf { it.grandTotal } / month.size else 0.0,
            totalParts = totalParts,
            stockValue = stockValue,
            lowStockCount = lowStock
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
