package com.hashmimotors.app.ui.billing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hashmimotors.app.data.repository.CustomerRepository
import com.hashmimotors.app.data.repository.InvoiceRepository
import com.hashmimotors.app.data.repository.PartRepository
import com.hashmimotors.app.data.repository.ShopRepository
import com.hashmimotors.app.domain.model.Customer
import com.hashmimotors.app.domain.model.CustomerSnapshot
import com.hashmimotors.app.domain.model.Invoice
import com.hashmimotors.app.domain.model.InvoiceLine
import com.hashmimotors.app.domain.model.InvoiceStatus
import com.hashmimotors.app.domain.model.InvoiceType
import com.hashmimotors.app.domain.model.Part
import com.hashmimotors.app.domain.model.PartSnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BillingUiState(
    val lines: List<InvoiceLine> = emptyList(),
    val customer: Customer? = null,
    val billDiscountPct: Double = 0.0,
    val notes: String = "",
    val isSaving: Boolean = false,
    val savedInvoice: Invoice? = null,
    val error: String? = null,
    val paymentMode: String = "CASH",
    val customers: List<Customer> = emptyList()
) {
    val subtotal: Double get() = lines.sumOf { it.lineTotal }
    val totalDiscount: Double get() {
        val lineDiscounts = lines.sumOf { it.qty * it.rate * (it.discountPct / 100.0) }
        val billLevelDiscount = subtotal * (billDiscountPct / 100.0)
        return lineDiscounts + billLevelDiscount
    }
    val totalGst: Double get() = 0.0 // Composition = no tax on bill
    val grandTotal: Double get() = (subtotal - totalDiscount).coerceAtLeast(0.0)
    val totalItems: Int get() = lines.sumOf { it.qty }
}

@HiltViewModel
class BillingViewModel @Inject constructor(
    private val partRepo: PartRepository,
    private val customerRepo: CustomerRepository,
    private val invoiceRepo: InvoiceRepository,
    private val shopRepo: ShopRepository
) : ViewModel() {

    private val _state = MutableStateFlow(BillingUiState())
    val state: StateFlow<BillingUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            customerRepo.getAllCustomers().collect { list ->
                _state.update { it.copy(customers = list) }
            }
        }
    }

    fun addPart(part: Part, qty: Int = 1) {
        if (qty <= 0) return
        val snapshot = PartSnapshot(
            name = part.name,
            oemNumbers = part.oemNumbers,
            hsnCode = part.hsnCode,
            mrp = part.mrp,
            gstPercent = part.gstPercent
        )
        val newLine = InvoiceLine(
            partId = part.id,
            partSnapshot = snapshot,
            qty = qty,
            rate = part.sellingPrice,
            discountPct = 0.0,
            lineTotal = part.sellingPrice * qty
        )
        _state.update { current ->
            // If part already in cart, increase qty
            val existing = current.lines.find { it.partId == part.id }
            val newLines = if (existing != null) {
                current.lines.map {
                    if (it.partId == part.id) {
                        it.copy(qty = it.qty + qty, lineTotal = (it.qty + qty) * it.rate)
                    } else it
                }
            } else {
                current.lines + newLine
            }
            current.copy(lines = newLines)
        }
    }

    fun updateLineQty(index: Int, newQty: Int) {
        if (newQty <= 0) {
            removeLine(index)
            return
        }
        _state.update { current ->
            val newLines = current.lines.toMutableList()
            if (index in newLines.indices) {
                val line = newLines[index]
                newLines[index] = line.copy(
                    qty = newQty,
                    lineTotal = newQty * line.rate * (1 - line.discountPct / 100.0)
                )
            }
            current.copy(lines = newLines)
        }
    }

    fun updateLineRate(index: Int, newRate: Double) {
        _state.update { current ->
            val newLines = current.lines.toMutableList()
            if (index in newLines.indices) {
                val line = newLines[index]
                newLines[index] = line.copy(
                    rate = newRate.coerceAtLeast(0.0),
                    lineTotal = line.qty * newRate.coerceAtLeast(0.0) * (1 - line.discountPct / 100.0)
                )
            }
            current.copy(lines = newLines)
        }
    }

    fun updateLineDiscount(index: Int, newDiscountPct: Double) {
        _state.update { current ->
            val newLines = current.lines.toMutableList()
            if (index in newLines.indices) {
                val line = newLines[index]
                newLines[index] = line.copy(
                    discountPct = newDiscountPct.coerceIn(0.0, 100.0),
                    lineTotal = line.qty * line.rate * (1 - newDiscountPct.coerceIn(0.0, 100.0) / 100.0)
                )
            }
            current.copy(lines = newLines)
        }
    }

    fun removeLine(index: Int) {
        _state.update { current ->
            val newLines = current.lines.toMutableList()
            if (index in newLines.indices) newLines.removeAt(index)
            current.copy(lines = newLines)
        }
    }

    fun setCustomer(customer: Customer?) {
        _state.update { it.copy(customer = customer) }
    }

    fun setBillDiscount(pct: Double) {
        _state.update { it.copy(billDiscountPct = pct.coerceIn(0.0, 100.0)) }
    }

    fun setNotes(notes: String) {
        _state.update { it.copy(notes = notes) }
    }

    fun setPaymentMode(mode: String) {
        _state.update { it.copy(paymentMode = mode) }
    }

    fun clearCart() {
        _state.value = BillingUiState()
    }

    /**
     * Save the current cart as an invoice. Decrements stock for each line.
     */
    fun saveBill(
        userId: String,
        walkInName: String = "Walk-in Customer",
        walkInPhone: String = ""
    ) {
        val current = _state.value
        if (current.lines.isEmpty()) {
            _state.update { it.copy(error = "Cart is empty") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }
            try {
                val shop = shopRepo.getShopOnce()
                val invoiceNo = shopRepo.getNextInvoiceNumber()

                val customer = current.customer
                val snapshot = if (customer != null) {
                    CustomerSnapshot(
                        name = customer.name,
                        phone = customer.phone,
                        address = customer.address,
                        gstin = customer.gstin
                    )
                } else {
                    CustomerSnapshot(
                        name = walkInName.ifBlank { "Walk-in Customer" },
                        phone = walkInPhone,
                        address = null,
                        gstin = null
                    )
                }

                if (customer == null && walkInName.isNotBlank() && walkInName != "Walk-in Customer") {
                    customerRepo.saveCustomer(
                        Customer(
                            name = walkInName.trim(),
                            phone = walkInPhone.trim()
                        )
                    )
                }

                val credit = current.paymentMode.equals("CREDIT", ignoreCase = true)
                val invoice = Invoice(
                    invoiceNo = invoiceNo,
                    date = System.currentTimeMillis(),
                    type = InvoiceType.BILL_OF_SUPPLY,
                    customerId = customer?.id,
                    customerSnapshot = snapshot,
                    userId = userId,
                    lines = current.lines,
                    subtotal = current.subtotal,
                    totalDiscount = current.totalDiscount,
                    totalGst = current.totalGst,
                    grandTotal = current.grandTotal,
                    status = if (credit) InvoiceStatus.UNPAID else InvoiceStatus.PAID,
                    paymentMode = current.paymentMode,
                    notes = current.notes
                )

                invoiceRepo.saveInvoice(invoice)

                // Decrement stock for each line
                current.lines.forEach { line ->
                    partRepo.decrementStock(line.partId, line.qty, invoice.id, userId)
                }

                // Update customer total purchases
                if (customer != null) {
                    customerRepo.saveCustomer(
                        customer.copy(
                            totalPurchases = customer.totalPurchases + invoice.grandTotal,
                            lastVisit = System.currentTimeMillis()
                        )
                    )
                }

                _state.value = BillingUiState(savedInvoice = invoice, customers = current.customers)
            } catch (e: Exception) {
                _state.update { it.copy(isSaving = false, error = e.message ?: "Failed to save bill") }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}
