package com.hashmimotors.app.ui.billing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hashmimotors.app.data.repository.InvoiceRepository
import com.hashmimotors.app.data.repository.ShopRepository
import com.hashmimotors.app.domain.model.Invoice
import com.hashmimotors.app.domain.model.Shop
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InvoicePreviewViewModel @Inject constructor(
    private val invoiceRepository: InvoiceRepository,
    private val shopRepository: ShopRepository
) : ViewModel() {

    private val _invoice = MutableStateFlow<Invoice?>(null)
    val invoice: StateFlow<Invoice?> = _invoice.asStateFlow()

    private val _shop = MutableStateFlow<Shop?>(null)
    val shop: StateFlow<Shop?> = _shop.asStateFlow()

    fun loadInvoice(id: String) {
        viewModelScope.launch {
            _invoice.value = invoiceRepository.getInvoiceByIdOnce(id)
            _shop.value = shopRepository.getShopOnce()
        }
    }
}
