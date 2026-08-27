package com.hashmimotors.app.data.repository

import androidx.room.withTransaction
import com.hashmimotors.app.data.local.HashmiDatabase
import com.hashmimotors.app.data.local.InvoiceDao
import com.hashmimotors.app.data.local.InvoiceEntity
import com.hashmimotors.app.data.local.InvoiceLineEmbedded
import com.hashmimotors.app.data.local.PartDao
import com.hashmimotors.app.data.local.StockMovementDao
import com.hashmimotors.app.data.local.StockMovementEntity
import com.hashmimotors.app.domain.model.Invoice
import com.hashmimotors.app.domain.model.InvoiceLine
import com.hashmimotors.app.domain.model.InvoiceStatus
import com.hashmimotors.app.domain.model.InvoiceType
import com.hashmimotors.app.domain.model.StockMovementType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InvoiceRepository @Inject constructor(
    private val db: HashmiDatabase,
    private val invoiceDao: InvoiceDao,
    private val partDao: PartDao,
    private val stockMovementDao: StockMovementDao
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
        return invoiceDao.totalBetween(start, end)
    }

    fun getTodayBillCount(): Flow<Int> {
        val (start, end) = todayRange()
        return invoiceDao.countBetween(start, end)
    }

    fun getMonthSalesTotal(): Flow<Double> {
        val (start, end) = currentMonthRange()
        return invoiceDao.totalBetween(start, end)
    }

    fun getMonthBillCount(): Flow<Int> {
        val (start, end) = currentMonthRange()
        return invoiceDao.countBetween(start, end)
    }

    /**
     * Total units sold today. Line quantities live inside the serialised `lines`
     * column, so they are summed in memory over today's invoices (bounded by a
     * single day of counter sales).
     */
    fun getTodayItemsSold(): Flow<Int> {
        val (start, end) = todayRange()
        return invoiceDao.getForDay(start, end).map { list ->
            list.sumOf { invoice -> invoice.lines.sumOf { it.qty } }
        }
    }

    suspend fun saveInvoice(invoice: Invoice) {
        invoiceDao.insert(invoice.toEntity())
    }

    /**
     * Persist a completed sale: the invoice plus one OUT stock movement per line.
     *
     * Everything runs inside a single database transaction, so a crash or error
     * halfway through cannot leave an invoice behind whose stock was never
     * decremented (or vice versa).
     */
    suspend fun recordSale(invoice: Invoice, userId: String) {
        db.withTransaction {
            invoiceDao.insert(invoice.toEntity())
            invoice.lines.forEach { line ->
                if (line.qty <= 0) return@forEach
                partDao.adjustStock(line.partId, -line.qty, invoice.createdAt)
                stockMovementDao.insert(
                    StockMovementEntity(
                        id = UUID.randomUUID().toString(),
                        partId = line.partId,
                        type = StockMovementType.OUT.name,
                        qty = -line.qty,
                        refType = "INVOICE",
                        refId = invoice.id,
                        userId = userId,
                        timestamp = invoice.createdAt
                    )
                )
            }
        }
    }

    suspend fun deleteInvoice(invoice: Invoice) {
        invoiceDao.delete(invoice.toEntity())
    }

    /** Half-open range [00:00 today, 00:00 tomorrow) in the device time zone. */
    private fun todayRange(): Pair<Long, Long> {
        val cal = startOfDayCalendar()
        val start = cal.timeInMillis
        return start to (start + 24L * 60 * 60 * 1000)
    }

    /** Half-open range [1st of this month 00:00, 1st of next month 00:00). */
    private fun currentMonthRange(): Pair<Long, Long> {
        val cal = startOfDayCalendar()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val start = cal.timeInMillis
        cal.add(Calendar.MONTH, 1)
        return start to cal.timeInMillis
    }

    private fun startOfDayCalendar(): Calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
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
