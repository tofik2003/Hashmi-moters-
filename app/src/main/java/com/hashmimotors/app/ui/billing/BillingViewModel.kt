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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BillingUiState(
    val lines: List<InvoiceLine> = emptyList(),
    val customer: Customer? = null,
    val customerName: String = "Walk-in Customer",
    val customerPhone: String = "",
    val billDiscountPct: Double = 0.0,
    val discountText: String = "0",
    val notes: String = "",
    val isSaving: Boolean = false,
    val savedInvoice: Invoice? = null,
    val error: String? = null
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

    /** Saved customers, used for quick name/phone autofill on the bill. */
    val customers: StateFlow<List<Customer>> = customerRepo.getAllCustomers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _quickParts = MutableStateFlow<List<Part>>(emptyList())
    val quickParts: StateFlow<List<Part>> = _quickParts.asStateFlow()

    init {
        refreshQuickParts()
    }

    /**
     * Parts most often billed — shown as one-tap "Quick Add" tiles.
     * Falls back to recently added parts when there is no bill history yet.
     */
    fun refreshQuickParts() {
        viewModelScope.launch {
            val invoices = invoiceRepo.getAllInvoices().first()
            val parts = partRepo.getAllPartsOnce()
            val counts = HashMap<String, Int>()
            invoices.forEach { inv ->
                inv.lines.forEach { line ->
                    counts[line.partId] = (counts[line.partId] ?: 0) + line.qty
                }
            }
            val top = counts.entries
                .sortedByDescending { it.value }
                .mapNotNull { (id, _) -> parts.find { it.id == id } }
                .take(12)
            _quickParts.value = if (top.isNotEmpty()) top
            else parts.sortedByDescending { it.createdAt }.take(8)
        }
    }

    fun addPart(part: Part, qty: Int = 1) {
        if (qty <= 0) return
        _state.update { it.copy(error = null) }
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

    fun setCustomerName(name: String) {
        _state.update { it.copy(customerName = name) }
    }

    fun setCustomerPhone(phone: String) {
        _state.update { it.copy(customerPhone = phone) }
    }

    fun setBillDiscount(pct: Double) {
        _state.update { it.copy(billDiscountPct = pct.coerceIn(0.0, 100.0)) }
    }

    fun setDiscountText(text: String) {
        val clean = text.filter { c -> c.isDigit() || c == '.' }.take(6)
        val pct = clean.toDoubleOrNull() ?: 0.0
        _state.update {
            it.copy(discountText = clean, billDiscountPct = pct.coerceIn(0.0, 100.0))
        }
    }

    fun setNotes(notes: String) {
        _state.update { it.copy(notes = notes) }
    }

    /** Scan a barcode and add the matching part straight to the cart. */
    fun addPartByBarcode(barcode: String) {
        val code = barcode.trim()
        if (code.isEmpty()) return
        viewModelScope.launch {
            val part = partRepo.getPartByBarcode(code)
            if (part != null) {
                addPart(part, qty = 1)
            } else {
                _state.update { it.copy(error = "No part found for barcode $code") }
            }
        }
    }

    /** Clone the most recent bill into the cart. */
    fun repeatLastBill() {
        viewModelScope.launch {
            val latest = invoiceRepo.getLatestInvoice()
            if (latest == null) {
                _state.update { it.copy(error = "No previous bill to repeat") }
                return@launch
            }
            // Bill-level discount = total discount minus any per-line discounts
            val lineDiscounts = latest.lines.sumOf {
                it.qty * it.rate * (it.discountPct / 100.0)
            }
            val billDiscount = (latest.totalDiscount - lineDiscounts).coerceAtLeast(0.0)
            val pct = if (latest.subtotal > 0) billDiscount / latest.subtotal * 100.0 else 0.0

            _state.update { current ->
                current.copy(
                    lines = latest.lines,
                    customer = null,
                    customerName = latest.customerSnapshot.name.ifBlank { "Walk-in Customer" },
                    customerPhone = latest.customerSnapshot.phone,
                    billDiscountPct = pct.coerceIn(0.0, 100.0),
                    discountText = if (pct > 0) "%.2f".format(pct) else "0",
                    notes = latest.notes ?: "",
                    error = null
                )
            }
        }
    }

    fun clearCart() {
        _state.value = BillingUiState()
    }

    /**
     * Save the current cart as an invoice. Decrements stock for each line.
     */
    fun saveBill(userId: String) {
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
                    // Typed-in customer details (the common counter case)
                    CustomerSnapshot(
                        name = current.customerName.ifBlank { "Walk-in Customer" },
                        phone = current.customerPhone,
                        address = null,
                        gstin = null
                    )
                }

                val invoice = Invoice(
                    invoiceNo = invoiceNo,
                    date = System.currentTimeMillis(),
                    type = if (shop?.gstMode == com.hashmimotors.app.domain.model.GstMode.COMPOSITION)
                        InvoiceType.BILL_OF_SUPPLY else InvoiceType.BILL_OF_SUPPLY,
                    customerId = customer?.id,
                    customerSnapshot = snapshot,
                    userId = userId,
                    lines = current.lines,
                    subtotal = current.subtotal,
                    totalDiscount = current.totalDiscount,
                    totalGst = current.totalGst,
                    grandTotal = current.grandTotal,
                    status = InvoiceStatus.PAID,
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

                _state.value = BillingUiState(savedInvoice = invoice)
            } catch (e: Exception) {
                _state.update { it.copy(isSaving = false, error = e.message ?: "Failed to save bill") }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}
