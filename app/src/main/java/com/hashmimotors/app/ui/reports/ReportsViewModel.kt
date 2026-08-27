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

    // combine() is limited to five flows, so the nine source flows are grouped
    // into three triples/pairs and merged in a second combine().
    private val todayFlow = combine(
        invoiceRepository.getTodaySalesTotal(),
        invoiceRepository.getTodayBillCount(),
        invoiceRepository.getTodayItemsSold()
    ) { sales, bills, items -> Triple(sales, bills, items) }

    private val monthFlow = combine(
        invoiceRepository.getMonthSalesTotal(),
        invoiceRepository.getMonthBillCount()
    ) { sales, bills -> sales to bills }

    private val inventoryFlow = combine(
        partRepository.getPartCount(),
        partRepository.getLowStockCount(),
        partRepository.getTotalStockValue()
    ) { total, low, value -> Triple(total, low, value) }

    val uiState: StateFlow<ReportsUiState> = combine(
        todayFlow,
        monthFlow,
        inventoryFlow
    ) { today, month, inventory ->
        val (monthSales, monthBills) = month
        ReportsUiState(
            todaySales = today.first,
            todayBills = today.second,
            todayItems = today.third,
            monthSales = monthSales,
            monthBills = monthBills,
            avgBillValue = if (monthBills > 0) monthSales / monthBills else 0.0,
            totalParts = inventory.first,
            lowStockCount = inventory.second,
            stockValue = inventory.third
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReportsUiState())
}
