package com.hashmimotors.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hashmimotors.app.data.repository.InvoiceRepository
import com.hashmimotors.app.data.repository.PartRepository
import com.hashmimotors.app.data.repository.ShopRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class DashboardUiState(
    val todaySales: Double = 0.0,
    val todayBills: Int = 0,
    val lowStockCount: Int = 0,
    val totalParts: Int = 0,
    val totalStockValue: Double = 0.0,
    val shopName: String = "Hashmi Motors Premium"
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    invoiceRepo: InvoiceRepository,
    partRepo: PartRepository,
    shopRepo: ShopRepository
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = combine(
        combine(
            invoiceRepo.getTodaySalesTotal(),
            invoiceRepo.getTodayBillCount(),
            partRepo.getLowStockCount()
        ) { sales, bills, lowStock ->
            Triple(sales, bills, lowStock)
        },
        combine(
            partRepo.getPartCount(),
            partRepo.getTotalStockValue(),
            shopRepo.getShop()
        ) { totalParts, stockValue, shop ->
            Triple(totalParts, stockValue, shop)
        }
    ) { first, second ->
        val (sales, bills, lowStock) = first
        val (totalParts, stockValue, shop) = second
        DashboardUiState(
            todaySales = sales,
            todayBills = bills,
            lowStockCount = lowStock,
            totalParts = totalParts,
            totalStockValue = stockValue,
            shopName = shop?.name ?: "Hashmi Motors Premium"
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )
}
