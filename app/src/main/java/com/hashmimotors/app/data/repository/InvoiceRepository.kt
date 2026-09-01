package com.hashmimotors.app.data.repository

import com.hashmimotors.app.data.local.InvoiceDao
import com.hashmimotors.app.data.local.InvoiceEntity
import com.hashmimotors.app.data.local.InvoiceLineEmbedded
import com.hashmimotors.app.domain.model.Invoice
import com.hashmimotors.app.domain.model.InvoiceLine
import com.hashmimotors.app.domain.model.InvoiceStatus
import com.hashmimotors.app.domain.model.InvoiceType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/** One point on the sales chart. */
data class SalesPoint(val label: String, val value: Double)

@Singleton
class InvoiceRepository @Inject constructor(
    private val invoiceDao: InvoiceDao
) {
    fun getAllInvoices(): Flow<List<Invoice>> = invoiceDao.getAll().map { list ->
        list.map { it.toDomain() }
    }

    fun getInvoiceById(id: String): Flow<Invoice?> = invoiceDao.getById(id).map { it?.toDomain() }

    suspend fun getInvoiceByIdOnce(id: String): Invoice? = invoiceDao.getByIdOnce(id)?.toDomain()

    fun getInvoicesForDay(startOfDay: Long, endOfDay: Long): Flow<List<Invoice>> =
        invoiceDao.getForDay(startOfDay, endOfDay).map { list -> list.map { it.toDomain() } }

    fun getInvoicesForCustomer(customerId: String): Flow<List<Invoice>> =
        invoiceDao.getForCustomer(customerId).map { list -> list.map { it.toDomain() } }

    fun getTodaySalesTotal(): Flow<Double> {
        val (start, end) = todayRange()
        return invoiceDao.totalForDay(start, end)
    }

    fun getTodayBillCount(): Flow<Int> {
        val (start, end) = todayRange()
        return invoiceDao.countForDay(start, end)
    }

    suspend fun saveInvoice(invoice: Invoice) {
        invoiceDao.insert(invoice.toEntity())
    }

    suspend fun deleteInvoice(invoice: Invoice) {
        invoiceDao.delete(invoice.toEntity())
    }

    private fun todayRange(): Pair<Long, Long> {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        val end = start + 24L * 60 * 60 * 1000
        return start to end
    }

    /**
     * Sales totals for the last [days] days (including today), with zeros for
     * missing days. Used by the dashboard chart.
     */
    fun getSalesForLastNDays(days: Int): Flow<List<SalesPoint>> {
        val dayFormatter = SimpleDateFormat("EEE", Locale.getDefault())
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val todayStart = cal.timeInMillis
        val fromMillis = todayStart - (days - 1) * 24L * 60 * 60 * 1000
        return invoiceDao.dailyTotalsSince(fromMillis).map { list ->
            val byDay = list.associateBy { it.day }
            (0 until days).map { i ->
                val dayMillis = todayStart - (days - 1 - i) * 24L * 60 * 60 * 1000
                val dayIndex = dayMillis / 86_400_000L
                SalesPoint(
                    label = dayFormatter.format(Date(dayMillis)),
                    value = byDay[dayIndex]?.total ?: 0.0
                )
            }
        }
    }
}

// =================== Mappers ===================

fun InvoiceEntity.toDomain() = Invoice(
    id = id,
    invoiceNo = invoiceNo,
    date = date,
    type = runCatching { InvoiceType.valueOf(type) }.getOrDefault(InvoiceType.BILL_OF_SUPPLY),
    customerId = customerId,
    customerSnapshot = com.hashmimotors.app.domain.model.CustomerSnapshot(
        name = customerName,
        phone = customerPhone,
        address = customerAddress,
        gstin = customerGstin
    ),
    userId = userId,
    lines = lines.map { e ->
        InvoiceLine(
            partId = e.partId,
            partSnapshot = com.hashmimotors.app.domain.model.PartSnapshot(
                name = e.partName,
                oemNumbers = e.oemNumbers,
                hsnCode = e.hsnCode,
                mrp = e.mrp,
                gstPercent = e.gstPercent
            ),
            qty = e.qty,
            rate = e.rate,
            discountPct = e.discountPct,
            lineTotal = e.lineTotal
        )
    },
    subtotal = subtotal,
    totalDiscount = totalDiscount,
    totalGst = totalGst,
    grandTotal = grandTotal,
    status = runCatching { InvoiceStatus.valueOf(status) }.getOrDefault(InvoiceStatus.PAID),
    notes = notes,
    synced = synced,
    createdAt = createdAt
)

fun Invoice.toEntity() = InvoiceEntity(
    id = id,
    invoiceNo = invoiceNo,
    date = date,
    type = type.name,
    customerId = customerId,
    customerName = customerSnapshot.name,
    customerPhone = customerSnapshot.phone,
    customerAddress = customerSnapshot.address,
    customerGstin = customerSnapshot.gstin,
    userId = userId,
    lines = lines.map { l ->
        InvoiceLineEmbedded(
            partId = l.partId,
            partName = l.partSnapshot.name,
            oemNumbers = l.partSnapshot.oemNumbers,
            hsnCode = l.partSnapshot.hsnCode,
            mrp = l.partSnapshot.mrp,
            gstPercent = l.partSnapshot.gstPercent,
            qty = l.qty,
            rate = l.rate,
            discountPct = l.discountPct,
            lineTotal = l.lineTotal
        )
    },
    subtotal = subtotal,
    totalDiscount = totalDiscount,
    totalGst = totalGst,
    grandTotal = grandTotal,
    status = status.name,
    notes = notes,
    synced = synced,
    createdAt = createdAt
)
