package com.hashmimotors.app.data.repository

import com.hashmimotors.app.data.local.CategoryDao
import com.hashmimotors.app.data.local.PartDao
import com.hashmimotors.app.data.local.PartEntity
import com.hashmimotors.app.data.local.StockMovementDao
import com.hashmimotors.app.data.local.StockMovementEntity
import com.hashmimotors.app.data.seed.ReferenceDataRepository
import com.hashmimotors.app.domain.model.Part
import com.hashmimotors.app.domain.model.StockMovementType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for Part operations.
 * Bridges between Room entities and domain models.
 */
@Singleton
class PartRepository @Inject constructor(
    private val partDao: PartDao,
    private val stockMovementDao: StockMovementDao,
    private val categoryDao: CategoryDao,
    private val referenceDataRepository: ReferenceDataRepository
) {
    fun getAllParts(): Flow<List<Part>> = partDao.getAll().map { list ->
        list.map { it.toDomain() }
    }

    fun getPartById(id: String): Flow<Part?> = partDao.getById(id).map { it?.toDomain() }

    suspend fun getPartByIdOnce(id: String): Part? = partDao.getByIdOnce(id)?.toDomain()

    fun searchParts(query: String): Flow<List<Part>> = partDao.search(query).map { list ->
        list.map { it.toDomain() }
    }

    /** Smart search with scoring based on recency, frequency, and favorites */
    fun smartSearchParts(query: String): Flow<List<Part>> = partDao.smartSearch(query).map { list ->
        list.map { it.toDomain() }
    }

    /** Get recently scanned parts (past scan history) */
    fun getRecentlyScannedParts(limit: Int = 20): Flow<List<Part>> = partDao.getRecentlyScanned(limit).map { list ->
        list.map { it.toDomain() }
    }

    /** Get quick in-out items (fast moving parts) */
    fun getQuickInOutItems(limit: Int = 20): Flow<List<Part>> = partDao.getQuickInOutItems(limit).map { list ->
        list.map { it.toDomain() }
    }

    /** Get favorite parts */
    fun getFavoriteParts(): Flow<List<Part>> = partDao.getFavoriteParts().map { list ->
        list.map { it.toDomain() }
    }

    /** Increment scan count when a part is scanned/used */
    suspend fun recordScan(partId: String) {
        partDao.incrementScanCount(partId)
    }

    /** Record a sale for analytics */
    suspend fun recordSale(partId: String, qty: Int) {
        partDao.recordSale(partId, qty)
    }

    /** Toggle favorite status */
    suspend fun setFavorite(partId: String, isFavorite: Boolean) {
        partDao.setFavorite(partId, isFavorite)
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
    suspend fun addStock(partId: String, qty: Int, userId: String, supplierId: String? = null) {
        if (qty <= 0) return
        partDao.adjustStock(partId, qty)
        stockMovementDao.insert(
            StockMovementEntity(
                id = java.util.UUID.randomUUID().toString(),
                partId = partId,
                type = StockMovementType.IN.name,
                qty = qty,
                refType = if (supplierId != null) "SUPPLIER" else "MANUAL",
                refId = supplierId,
                userId = userId
            )
        )
    }

    /**
     * Adjust stock manually (damage, count correction, etc).
     */
    suspend fun adjustStock(partId: String, delta: Int, reason: String, userId: String) {
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

    fun getRecentMovements(limit: Int = 50): Flow<List<StockMovementEntity>> =
        stockMovementDao.getRecent(limit)

    /** Exact barcode lookup (used by barcode scanner routing). */
    suspend fun findByBarcodeOnce(barcode: String): Part? =
        partDao.findByBarcodeOnce(barcode.trim())?.toDomain()

    /** Exact SKU lookup (used for duplicate detection). */
    suspend fun findBySkuOnce(sku: String): Part? =
        partDao.findBySkuOnce(sku.trim())?.toDomain()

    /** Exact name lookup (used for duplicate detection). */
    suspend fun findByNameOnce(name: String): Part? =
        partDao.findByNameOnce(name.trim())?.toDomain()

    /** Next sequential auto SKU, e.g. HM-000053. */
    suspend fun nextSku(): String =
        String.format(Locale.US, "HM-%06d", partDao.countOnce() + 1)

    /**
     * Seeds a curated starter inventory into the parts table on first launch so
     * the catalog is populated out of the box. Categories are linked by name
     * (they are seeded before parts by [CategoryRepository.ensureSeeded]).
     */
    suspend fun ensureSeeded() {
        if (partDao.countOnce() > 0) return
        val categoryIdByName = categoryDao.getAllOnce().associateBy { it.name }
        val now = System.currentTimeMillis()
        val starters = referenceDataRepository.starterParts.mapIndexed { index, seed ->
            Part(
                sku = String.format(Locale.US, "ST-%04d", index + 1),
                name = seed.name,
                oemNumbers = emptyList(),
                brand = seed.brand,
                categoryId = categoryIdByName[seed.category]?.id,
                mrp = seed.mrp,
                sellingPrice = if (seed.sellingPrice > 0.0) seed.sellingPrice else seed.mrp,
                costPrice = seed.costPrice,
                gstPercent = seed.gstPercent,
                hsnCode = seed.hsnCode,
                stockQty = seed.stockQty,
                reorderLevel = seed.reorderLevel,
                supplierId = null,
                photoPaths = emptyList(),
                barcode = null,
                notes = null,
                active = true,
                createdAt = now,
                updatedAt = now
            )
        }
        if (starters.isNotEmpty()) {
            partDao.insertAll(starters.map { it.toEntity() })
        }
    }
}

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
    updatedAt = updatedAt,
    lastScannedAt = lastScannedAt,
    scanCount = scanCount,
    lastSoldAt = lastSoldAt,
    totalSold = totalSold,
    favorite = favorite
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
    updatedAt = System.currentTimeMillis(),
    lastScannedAt = lastScannedAt,
    scanCount = scanCount,
    lastSoldAt = lastSoldAt,
    totalSold = totalSold,
    favorite = favorite
)
