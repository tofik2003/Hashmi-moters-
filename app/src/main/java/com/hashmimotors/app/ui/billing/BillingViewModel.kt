package com.hashmimotors.app.ui.billing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hashmimotors.app.data.repository.CustomerRepository
import com.hashmimotors.app.data.repository.InvoiceRepository
import com.hashmimotors.app.data.repository.ShopRepository
import com.hashmimotors.app.domain.model.Customer
import com.hashmimotors.app.domain.model.CustomerSnapshot
import com.hashmimotors.app.domain.model.Invoice
import com.hashmimotors.app.domain.model.InvoiceLine
import com.hashmimotors.app.domain.model.InvoiceStatus
import com.hashmimotors.app.domain.model.InvoiceType
import com.hashmimotors.app.domain.model.Part
import com.hashmimotors.app.domain.model.PartSnapshot
import com.hashmimotors.app.domain.money.InvoiceMath
import com.hashmimotors.app.ui.sound.HapticManager
import com.hashmimotors.app.ui.sound.SoundEffect
import com.hashmimotors.app.ui.sound.SoundManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    val error: String? = null
) {
    /** Gross value before any discount: sum of qty * rate. */
    val subtotal: Double get() = InvoiceMath.subtotal(lines.map { (it.qty * it.rate).toDouble() })

    /** Value after per-line discounts. Equal to sum of lineTotal. */
    val netAfterLineDiscounts: Double get() = lines.sumOf { it.lineTotal }

    /** Total money given away by per-line discounts. */
    val lineDiscount: Double get() = (subtotal - netAfterLineDiscounts).coerceAtLeast(0.0)

    /**
     * Bill-level discount, applied on top of the line-discounted value so that a
     * line discount is never subtracted twice from the grand total.
     */
    val billDiscount: Double get() = InvoiceMath.billDiscount(netAfterLineDiscounts, billDiscountPct)

    /** Sum of line + bill level discounts; each counted exactly once. */
    val totalDiscount: Double get() = lineDiscount + billDiscount

    val totalGst: Double get() = 0.0 // Composition scheme = no tax charged on the bill

    val grandTotal: Double get() = InvoiceMath.grandTotal(subtotal, totalDiscount)

    val totalItems: Int get() = lines.sumOf { it.qty }
}

@HiltViewModel
class BillingViewModel @Inject constructor(
    private val customerRepo: CustomerRepository,
    private val invoiceRepo: InvoiceRepository,
    private val shopRepo: ShopRepository,
    private val soundManager: SoundManager,
    private val hapticManager: HapticManager
) : ViewModel() {

    private val _state = MutableStateFlow(BillingUiState())
    val state: StateFlow<BillingUiState> = _state.asStateFlow()

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
            lineTotal = lineTotalFor(qty, part.sellingPrice, 0.0)
        )
        _state.update { current ->
            // If part already in cart, increase qty (keeping any discount already applied)
            val existing = current.lines.find { it.partId == part.id }
            val newLines = if (existing != null) {
                current.lines.map {
                    if (it.partId == part.id) {
                        val newQty = it.qty + qty
                        it.copy(
                            qty = newQty,
                            lineTotal = lineTotalFor(newQty, it.rate, it.discountPct)
                        )
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
                    lineTotal = lineTotalFor(newQty, line.rate, line.discountPct)
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
                val rate = newRate.coerceAtLeast(0.0)
                newLines[index] = line.copy(
                    rate = rate,
                    lineTotal = lineTotalFor(line.qty, rate, line.discountPct)
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
                val discount = newDiscountPct.coerceIn(0.0, 100.0)
                newLines[index] = line.copy(
                    discountPct = discount,
                    lineTotal = lineTotalFor(line.qty, line.rate, discount)
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
                if (shop == null || !shop.isSetupComplete) {
                    _state.update {
                        it.copy(
                            isSaving = false,
                            error = "Please complete Shop Setup before creating bills"
                        )
                    }
                    return@launch
                }
                val invoiceNo = shopRepo.claimNextInvoiceNumber()

                val customer = current.customer
                val snapshot = if (customer != null) {
                    CustomerSnapshot(
                        name = customer.name,
                        phone = customer.phone,
                        address = customer.address,
                        gstin = customer.gstin
                    )
                } else {
                    CustomerSnapshot(name = "Walk-in Customer", phone = "", address = null, gstin = null)
                }

                val invoice = Invoice(
                    invoiceNo = invoiceNo,
                    date = System.currentTimeMillis(),
                    // The app is built for the GST composition scheme, which issues a
                    // Bill of Supply and charges no tax on the bill. Regular-scheme tax
                    // invoices are not supported (see docs/APP_PLAN.md, decision #8).
                    type = InvoiceType.BILL_OF_SUPPLY,
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

                // Invoice + stock movements are written in a single transaction so a
                // bill can never exist without its stock being decremented.
                invoiceRepo.recordSale(invoice, userId)

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
                soundManager.play(SoundEffect.BILL_SAVED)
                hapticManager.success()
            } catch (e: Exception) {
                _state.update { it.copy(isSaving = false, error = e.message ?: "Failed to save bill") }
                soundManager.play(SoundEffect.ERROR)
                hapticManager.error()
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    companion object {
        /** See [InvoiceMath.lineTotal]; kept here as the single call site for the cart. */
        fun lineTotalFor(qty: Int, rate: Double, discountPct: Double): Double =
            InvoiceMath.lineTotal(qty, rate, discountPct)
    }
}
