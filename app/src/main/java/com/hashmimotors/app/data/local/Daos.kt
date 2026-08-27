package com.hashmimotors.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PartDao {
    @Query("SELECT * FROM parts WHERE active = 1 ORDER BY name ASC")
    fun getAll(): Flow<List<PartEntity>>

    @Query("SELECT * FROM parts WHERE id = :id")
    fun getById(id: String): Flow<PartEntity?>

    @Query("SELECT * FROM parts WHERE id = :id")
    suspend fun getByIdOnce(id: String): PartEntity?

    @Query("""
        SELECT * FROM parts
        WHERE active = 1
        AND (name LIKE '%' || :query || '%'
             OR sku LIKE '%' || :query || '%'
             OR brand LIKE '%' || :query || '%'
             OR oemNumbers LIKE '%' || :query || '%')
        ORDER BY name ASC
    """)
    fun search(query: String): Flow<List<PartEntity>>

    @Query("SELECT * FROM parts WHERE stockQty <= reorderLevel AND active = 1 ORDER BY stockQty ASC")
    fun getLowStock(): Flow<List<PartEntity>>

    @Query("SELECT * FROM parts WHERE stockQty = 0 AND active = 1")
    fun getOutOfStock(): Flow<List<PartEntity>>

    @Query("SELECT COUNT(*) FROM parts WHERE active = 1")
    fun count(): Flow<Int>

    @Query("SELECT COUNT(*) FROM parts WHERE stockQty <= reorderLevel AND active = 1")
    fun lowStockCount(): Flow<Int>

    @Query("SELECT SUM(stockQty * costPrice) FROM parts WHERE active = 1")
    fun totalStockValue(): Flow<Double?>

    @Query("SELECT SUM(stockQty * mrp) FROM parts WHERE active = 1")
    fun totalStockAtMrp(): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(part: PartEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(parts: List<PartEntity>)

    @Update
    suspend fun update(part: PartEntity)

    @Delete
    suspend fun delete(part: PartEntity)

    @Query("UPDATE parts SET stockQty = stockQty + :delta, updatedAt = :timestamp WHERE id = :partId")
    suspend fun adjustStock(partId: String, delta: Int, timestamp: Long = System.currentTimeMillis())
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY sortOrder ASC, name ASC")
    fun getAll(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getById(id: String): CategoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<CategoryEntity>)

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun count(): Int
}

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAll(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getById(id: String): CustomerEntity?

    @Query("""
        SELECT * FROM customers
        WHERE name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%'
        ORDER BY name ASC
    """)
    fun search(query: String): Flow<List<CustomerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(customer: CustomerEntity)

    @Update
    suspend fun update(customer: CustomerEntity)

    @Delete
    suspend fun delete(customer: CustomerEntity)
}

@Dao
interface SupplierDao {
    @Query("SELECT * FROM suppliers ORDER BY name ASC")
    fun getAll(): Flow<List<SupplierEntity>>

    @Query("SELECT * FROM suppliers WHERE id = :id")
    suspend fun getById(id: String): SupplierEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(supplier: SupplierEntity)

    @Update
    suspend fun update(supplier: SupplierEntity)

    @Delete
    suspend fun delete(supplier: SupplierEntity)
}

@Dao
interface VehicleDao {
    @Query("SELECT * FROM vehicles ORDER BY make ASC, model ASC")
    fun getAll(): Flow<List<VehicleEntity>>

    @Query("SELECT * FROM vehicles WHERE make = :make ORDER BY model ASC")
    fun getByMake(make: String): Flow<List<VehicleEntity>>

    @Query("SELECT * FROM vehicles WHERE id = :id")
    suspend fun getById(id: String): VehicleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vehicles: List<VehicleEntity>)

    @Query("SELECT COUNT(*) FROM vehicles")
    suspend fun count(): Int
}

@Dao
interface FitmentDao {
    @Query("SELECT * FROM fitments WHERE vehicleId = :vehicleId")
    fun getForVehicle(vehicleId: String): Flow<List<FitmentEntity>>

    @Query("SELECT * FROM fitments WHERE partId = :partId")
    fun getForPart(partId: String): Flow<List<FitmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(fitment: FitmentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(fitments: List<FitmentEntity>)

    @Delete
    suspend fun delete(fitment: FitmentEntity)
}

@Dao
interface InvoiceDao {
    @Query("SELECT * FROM invoices ORDER BY date DESC")
    fun getAll(): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE date >= :startOfDay AND date < :endOfDay ORDER BY date DESC")
    fun getForDay(startOfDay: Long, endOfDay: Long): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE id = :id")
    fun getById(id: String): Flow<InvoiceEntity?>

    @Query("SELECT * FROM invoices WHERE id = :id")
    suspend fun getByIdOnce(id: String): InvoiceEntity?

    @Query("SELECT * FROM invoices WHERE customerId = :customerId ORDER BY date DESC")
    fun getForCustomer(customerId: String): Flow<List<InvoiceEntity>>

    @Query("SELECT COUNT(*) FROM invoices WHERE date >= :startOfDay AND date < :endOfDay")
    fun countForDay(startOfDay: Long, endOfDay: Long): Flow<Int>

    @Query("SELECT COALESCE(SUM(grandTotal), 0) FROM invoices WHERE date >= :startOfDay AND date < :endOfDay")
    fun totalForDay(startOfDay: Long, endOfDay: Long): Flow<Double>

    @Query("SELECT COALESCE(SUM(grandTotal), 0) FROM invoices WHERE date >= :startOfDay AND date < :endOfDay AND status = 'PAID'")
    fun paidTotalForDay(startOfDay: Long, endOfDay: Long): Flow<Double>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(invoice: InvoiceEntity)

    @Update
    suspend fun update(invoice: InvoiceEntity)

    @Delete
    suspend fun delete(invoice: InvoiceEntity)
}

@Dao
interface StockMovementDao {
    @Query("SELECT * FROM stock_movements ORDER BY timestamp DESC LIMIT :limit")
    fun getRecent(limit: Int = 50): Flow<List<StockMovementEntity>>

    @Query("SELECT * FROM stock_movements WHERE partId = :partId ORDER BY timestamp DESC")
    fun getForPart(partId: String): Flow<List<StockMovementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(movement: StockMovementEntity)
}

@Dao
interface ShopDao {
    @Query("SELECT * FROM shop WHERE id = 'default'")
    fun get(): Flow<ShopEntity?>

    @Query("SELECT * FROM shop WHERE id = 'default'")
    suspend fun getOnce(): ShopEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(shop: ShopEntity)

    @Query("UPDATE shop SET invoiceCounter = invoiceCounter + 1 WHERE id = 'default'")
    suspend fun incrementInvoiceCounter()
}

@Dao
interface SettingsDao {
    @Query("SELECT * FROM app_settings WHERE id = 'default'")
    fun get(): Flow<SettingsEntity?>

    @Query("SELECT * FROM app_settings WHERE id = 'default'")
    suspend fun getOnce(): SettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(settings: SettingsEntity)
}

@Dao
interface UserDao {
    @Query("SELECT * FROM users")
    fun getAll(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getById(id: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserEntity)

    @Query("DELETE FROM users")
    suspend fun deleteAll()
}
