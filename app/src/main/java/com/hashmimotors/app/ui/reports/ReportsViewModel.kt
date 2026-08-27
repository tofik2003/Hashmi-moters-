package com.hashmimotors.app.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hashmimotors.app.data.repository.InvoiceRepository
import com.hashmimotors.app.data.repository.PartRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val invoiceRepository: InvoiceRepository,
    private val partRepository: PartRepository
) : ViewModel() {

    val uiState: StateFlow<ReportsUiState> = combine(
        invoiceRepository.getTodaySalesTotal(),
        invoiceRepository.getTodayBillCount(),
        partRepository.getPartCount(),
        partRepository.getLowStockCount(),
        partRepository.getTotalStockValue()
    ) { today, bills, totalParts, lowStock, stockValue ->
        ReportsUiState(
            todaySales = today,
            todayBills = bills,
            totalParts = totalParts,
            lowStockCount = lowStock,
            stockValue = stockValue,
            avgBillValue = if (bills > 0) today / bills else 0.0
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReportsUiState())
}
