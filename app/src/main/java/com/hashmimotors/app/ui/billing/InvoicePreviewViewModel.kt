package com.hashmimotors.app.ui.billing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hashmimotors.app.data.repository.InvoiceRepository
import com.hashmimotors.app.data.repository.PartRepository
import com.hashmimotors.app.data.repository.ShopRepository
import com.hashmimotors.app.domain.model.Invoice
import com.hashmimotors.app.domain.model.InvoiceStatus
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
    private val shopRepository: ShopRepository,
    private val partRepository: PartRepository
) : ViewModel() {

    private val _invoice = MutableStateFlow<Invoice?>(null)
    val invoice: StateFlow<Invoice?> = _invoice.asStateFlow()

    private val _shop = MutableStateFlow<Shop?>(null)
    val shop: StateFlow<Shop?> = _shop.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun loadInvoice(id: String) {
        viewModelScope.launch {
            _invoice.value = invoiceRepository.getInvoiceByIdOnce(id)
            _shop.value = shopRepository.getShopOnce()
        }
    }

    fun markPaid() {
        val inv = _invoice.value ?: return
        if (inv.status == InvoiceStatus.VOID) return
        viewModelScope.launch {
            val next = inv.copy(status = InvoiceStatus.PAID, paymentMode = if (inv.paymentMode == "CREDIT") "CASH" else inv.paymentMode)
            invoiceRepository.saveInvoice(next)
            _invoice.value = next
            _message.value = "Marked as paid"
        }
    }

    fun voidBill() {
        val inv = _invoice.value ?: return
        if (inv.status == InvoiceStatus.VOID) return
        viewModelScope.launch {
            inv.lines.forEach { line ->
                partRepository.adjustStock(
                    partId = line.partId,
                    delta = line.qty,
                    reason = "Void ${inv.invoiceNo}",
                    userId = inv.userId.ifBlank { "default-user" }
                )
            }
            val next = inv.copy(status = InvoiceStatus.VOID)
            invoiceRepository.saveInvoice(next)
            _invoice.value = next
            _message.value = "Bill voided — stock restored"
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
