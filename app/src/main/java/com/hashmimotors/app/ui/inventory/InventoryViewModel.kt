package com.hashmimotors.app.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hashmimotors.app.data.repository.PartRepository
import com.hashmimotors.app.data.repository.SupplierRepository
import com.hashmimotors.app.domain.model.Part
import com.hashmimotors.app.domain.model.StockMovement
import com.hashmimotors.app.domain.model.Supplier
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InventoryUiState(
    val allStock: List<Part> = emptyList(),
    val lowStock: List<Part> = emptyList(),
    val outOfStock: List<Part> = emptyList(),
    val suppliers: List<Supplier> = emptyList(),
    val movements: List<StockMovement> = emptyList(),
    val totalValue: Double = 0.0,
    val mrpValue: Double = 0.0
)

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val partRepo: PartRepository,
    private val supplierRepo: SupplierRepository
) : ViewModel() {

    val uiState: StateFlow<InventoryUiState> = combine(
        combine(
            partRepo.getAllParts(),
            partRepo.getLowStock(),
            partRepo.getOutOfStock(),
            partRepo.getTotalStockValue(),
            supplierRepo.getAllSuppliers()
        ) { all, low, out, value, suppliers ->
            InventoryUiState(
                allStock = all,
                lowStock = low,
                outOfStock = out,
                totalValue = value,
                mrpValue = all.sumOf { it.stockQty * it.mrp },
                suppliers = suppliers
            )
        },
        partRepo.getRecentMovements(100)
    ) { base, movements ->
        base.copy(movements = movements)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = InventoryUiState()
    )

    fun addStock(partId: String, qty: Int, supplierId: String?, userId: String, cost: Double? = null) {
        viewModelScope.launch {
            partRepo.addStock(partId, qty, userId, supplierId, costPerUnit = cost)
        }
    }

    fun adjustStock(partId: String, delta: Int, reason: String, userId: String) {
        viewModelScope.launch {
            partRepo.adjustStock(partId, delta, reason, userId)
        }
    }

    fun setStock(partId: String, qty: Int, reason: String, userId: String) {
        viewModelScope.launch {
            partRepo.setStock(partId, qty, reason, userId)
        }
    }

    fun saveSupplier(supplier: Supplier) {
        viewModelScope.launch { supplierRepo.saveSupplier(supplier) }
    }

    fun deleteSupplier(supplier: Supplier) {
        viewModelScope.launch { supplierRepo.deleteSupplier(supplier) }
    }
}
