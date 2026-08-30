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

    val uiState: StateFlow<ReportsUiState> = combine(
        combine(
            invoiceRepository.getTodaySalesTotal(),
            invoiceRepository.getTodayBillCount(),
            invoiceRepository.getTodayItemsSold()
        ) { sales, bills, items ->
            Triple(sales, bills, items)
        },
        combine(
            invoiceRepository.getMonthSalesTotal(),
            invoiceRepository.getMonthBillCount(),
            partRepository.getPartCount()
        ) { mSales, mBills, partsCount ->
            Triple(mSales, mBills, partsCount)
        },
        combine(
            partRepository.getTotalStockValue(),
            partRepository.getLowStockCount()
        ) { stockVal, lowStock ->
            stockVal to lowStock
        }
    ) { (todaySales, todayBills, todayItems), (mSales, mBills, partsCount), (stockVal, lowStock) ->
        ReportsUiState(
            todaySales = todaySales,
            todayBills = todayBills,
            todayItems = todayItems,
            monthSales = mSales,
            monthBills = mBills,
            avgBillValue = if (todayBills > 0) todaySales / todayBills else 0.0,
            totalParts = partsCount,
            stockValue = stockVal,
            lowStockCount = lowStock
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReportsUiState())
}
