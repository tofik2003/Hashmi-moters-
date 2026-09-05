package com.hashmimotors.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        PartEntity::class,
        CategoryEntity::class,
        CustomerEntity::class,
        SupplierEntity::class,
        VehicleEntity::class,
        FitmentEntity::class,
        InvoiceEntity::class,
        StockMovementEntity::class,
        ShopEntity::class,
        SettingsEntity::class,
        UserEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class HashmiDatabase : RoomDatabase() {
    abstract fun partDao(): PartDao
    abstract fun categoryDao(): CategoryDao
    abstract fun customerDao(): CustomerDao
    abstract fun supplierDao(): SupplierDao
    abstract fun vehicleDao(): VehicleDao
    abstract fun fitmentDao(): FitmentDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun stockMovementDao(): StockMovementDao
    abstract fun shopDao(): ShopDao
    abstract fun settingsDao(): SettingsDao
    abstract fun userDao(): UserDao
}
