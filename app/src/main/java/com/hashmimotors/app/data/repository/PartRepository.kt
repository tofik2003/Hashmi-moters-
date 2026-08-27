package com.hashmimotors.app.data.repository

import com.hashmimotors.app.data.local.PartDao
import com.hashmimotors.app.data.local.PartEntity
import com.hashmimotors.app.data.local.StockMovementDao
import com.hashmimotors.app.data.local.StockMovementEntity
import com.hashmimotors.app.domain.model.Part
import com.hashmimotors.app.domain.model.StockMovement
import com.hashmimotors.app.domain.model.StockMovementType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for Part operations.
 * Bridges between Room entities and domain models.
 */
@Singleton
class PartRepository @Inject constructor(
    private val partDao: PartDao,
    private val stockMovementDao: StockMovementDao
) {
    fun getAllParts(): Flow<List<Part>> = partDao.getAll().map { list ->
        list.map { it.toDomain() }
    }

    fun getPartById(id: String): Flow<Part?> = partDao.getById(id).map { it?.toDomain() }

    suspend fun getPartByIdOnce(id: String): Part? = partDao.getByIdOnce(id)?.toDomain()

    suspend fun getPartByBarcode(barcode: String): Part? =
        partDao.getByBarcode(barcode.trim())?.toDomain()

    fun searchParts(query: String): Flow<List<Part>> = partDao.search(query).map { list ->
        list.map { it.toDomain() }
    }

    fun getLowStock(): Flow<List<Part>> = partDao.getLowStock().map { list ->
        list.map { it.toDomain() }
    }

    fun getOutOfStock(): Flow<List<Part>> = partDao.getOutOfStock().map { list ->
        list.map { it.toDomain() }
    }

    fun getPartCount(): Flow<Int> = partDao.count()

    fun getLowStockCount(): Flow<Int> = partDao.lowStockCount()

    fun getTotalStockValue(): Flow<Double> = partDao.totalStockValue().map { it ?: 0.0 }

    suspend fun savePart(part: Part) {
        partDao.insert(part.toEntity())
    }

    suspend fun saveAllParts(parts: List<Part>) {
        partDao.insertAll(parts.map { it.toEntity() })
    }

    suspend fun deletePart(part: Part) {
        partDao.delete(part.toEntity())
    }

    /**
     * Add stock to a part (purchase received).
     */
    suspend fun addStock(
        partId: String,
        qty: Int,
        userId: String,
        supplierId: String? = null,
        costPerUnit: Double? = null,
        reason: String? = null
    ) {
        if (qty <= 0) return
        val existing = partDao.getByIdOnce(partId) ?: return
        if (costPerUnit != null || supplierId != null) {
            partDao.update(
                existing.copy(
                    costPrice = costPerUnit ?: existing.costPrice,
                    supplierId = supplierId ?: existing.supplierId,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
        partDao.adjustStock(partId, qty)
        stockMovementDao.insert(
            StockMovementEntity(
                id = java.util.UUID.randomUUID().toString(),
                partId = partId,
                type = StockMovementType.IN.name,
                qty = qty,
                refType = if (supplierId != null) "SUPPLIER" else "PURCHASE",
                refId = supplierId,
                reason = reason ?: "Stock in +$qty",
                userId = userId
            )
        )
    }

    /**
     * Adjust stock manually (damage, count correction, etc).
     */
    suspend fun adjustStock(partId: String, delta: Int, reason: String, userId: String) {
        if (delta == 0) return
        partDao.adjustStock(partId, delta)
        stockMovementDao.insert(
            StockMovementEntity(
                id = java.util.UUID.randomUUID().toString(),
                partId = partId,
                type = StockMovementType.ADJUST.name,
                qty = delta,
                refType = "ADJUSTMENT",
                refId = null,
                reason = reason,
                userId = userId
            )
        )
    }

    /** Physical count — set absolute on-hand quantity. */
    suspend fun setStock(partId: String, qty: Int, reason: String, userId: String) {
        val current = partDao.getByIdOnce(partId)?.stockQty ?: return
        val safe = qty.coerceAtLeast(0)
        val delta = safe - current
        partDao.setStock(partId, safe)
        stockMovementDao.insert(
            StockMovementEntity(
                id = java.util.UUID.randomUUID().toString(),
                partId = partId,
                type = StockMovementType.ADJUST.name,
                qty = delta,
                refType = "COUNT",
                refId = null,
                reason = reason.ifBlank { "Stock take → $safe" },
                userId = userId
            )
        )
    }

    /**
     * Decrement stock for a sale (used by billing).
     */
    suspend fun decrementStock(partId: String, qty: Int, invoiceId: String, userId: String) {
        if (qty <= 0) return
        partDao.adjustStock(partId, -qty)
        stockMovementDao.insert(
            StockMovementEntity(
                id = java.util.UUID.randomUUID().toString(),
                partId = partId,
                type = StockMovementType.OUT.name,
                qty = -qty,
                refType = "INVOICE",
                refId = invoiceId,
                userId = userId
            )
        )
    }

    fun getRecentMovements(limit: Int = 80): Flow<List<StockMovement>> =
        stockMovementDao.getRecent(limit).map { list -> list.map { it.toDomain() } }

    fun getMovementsForPart(partId: String): Flow<List<StockMovement>> =
        stockMovementDao.getForPart(partId).map { list -> list.map { it.toDomain() } }
}

fun StockMovementEntity.toDomain() = StockMovement(
    id = id,
    partId = partId,
    type = runCatching { StockMovementType.valueOf(type) }.getOrDefault(StockMovementType.ADJUST),
    qty = qty,
    refType = refType,
    refId = refId,
    reason = reason,
    userId = userId,
    timestamp = timestamp
)

// ============================================
// Mappers
// ============================================
fun PartEntity.toDomain() = Part(
    id = id,
    sku = sku,
    name = name,
    oemNumbers = oemNumbers,
    brand = brand,
    categoryId = categoryId,
    mrp = mrp,
    sellingPrice = sellingPrice,
    costPrice = costPrice,
    gstPercent = gstPercent,
    hsnCode = hsnCode,
    stockQty = stockQty,
    reorderLevel = reorderLevel,
    supplierId = supplierId,
    photoPaths = photoPaths,
    barcode = barcode,
    notes = notes,
    active = active,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Part.toEntity() = PartEntity(
    id = id,
    sku = sku,
    name = name,
    oemNumbers = oemNumbers,
    brand = brand,
    categoryId = categoryId,
    mrp = mrp,
    sellingPrice = sellingPrice,
    costPrice = costPrice,
    gstPercent = gstPercent,
    hsnCode = hsnCode,
    stockQty = stockQty,
    reorderLevel = reorderLevel,
    supplierId = supplierId,
    photoPaths = photoPaths,
    barcode = barcode,
    notes = notes,
    active = active,
    createdAt = createdAt,
    updatedAt = System.currentTimeMillis()
)
