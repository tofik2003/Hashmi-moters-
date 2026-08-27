package com.hashmimotors.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hashmimotors.app.data.repository.InvoiceRepository
import com.hashmimotors.app.data.repository.PartRepository
import com.hashmimotors.app.data.repository.ShopRepository
import com.hashmimotors.app.domain.model.Invoice
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import javax.inject.Inject

data class DashboardUiState(
    val todaySales: Double = 0.0,
    val todayBills: Int = 0,
    val todayItems: Int = 0,
    val lowStockCount: Int = 0,
    val totalParts: Int = 0,
    val totalStockValue: Double = 0.0,
    val shopName: String = "Hashmi Motors",
    val weekSales: List<Float> = List(7) { 0f },
    val weekTotal: Double = 0.0,
    val recentInvoices: List<Invoice> = emptyList()
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    invoiceRepo: InvoiceRepository,
    partRepo: PartRepository,
    shopRepo: ShopRepository
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = combine(
        invoiceRepo.getAllInvoices(),
        partRepo.getLowStockCount(),
        partRepo.getPartCount(),
        partRepo.getTotalStockValue(),
        shopRepo.getShop()
    ) { invoices, lowStock, totalParts, stockValue, shop ->
        val (start, end) = todayRange()
        val today = invoices.filter { it.date in start until end }
        val week = weekBuckets(invoices)
        DashboardUiState(
            todaySales = today.sumOf { it.grandTotal },
            todayBills = today.size,
            todayItems = today.sumOf { inv -> inv.lines.sumOf { it.qty } },
            lowStockCount = lowStock,
            totalParts = totalParts,
            totalStockValue = stockValue,
            shopName = shop?.name ?: "Hashmi Motors",
            weekSales = week,
            weekTotal = week.sum().toDouble(),
            recentInvoices = invoices.take(6)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )
}

private fun todayRange(): Pair<Long, Long> {
    val cal = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    val start = cal.timeInMillis
    return start to start + 24L * 60 * 60 * 1000
}

private fun weekBuckets(invoices: List<Invoice>): List<Float> {
    val cal = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    val todayStart = cal.timeInMillis
    val day = 24L * 60 * 60 * 1000
    return (6 downTo 0).map { offset ->
        val start = todayStart - offset * day
        val end = start + day
        invoices.filter { it.date in start until end }.sumOf { it.grandTotal }.toFloat()
    }
}
