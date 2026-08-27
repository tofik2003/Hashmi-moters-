package com.hashmimotors.app.di

import android.content.Context
import androidx.room.Room
import com.hashmimotors.app.data.local.CategoryDao
import com.hashmimotors.app.data.local.CustomerDao
import com.hashmimotors.app.data.local.FitmentDao
import com.hashmimotors.app.data.local.HashmiDatabase
import com.hashmimotors.app.data.local.InvoiceDao
import com.hashmimotors.app.data.local.PartDao
import com.hashmimotors.app.data.local.SettingsDao
import com.hashmimotors.app.data.local.ShopDao
import com.hashmimotors.app.data.local.StockMovementDao
import com.hashmimotors.app.data.local.SupplierDao
import com.hashmimotors.app.data.local.UserDao
import com.hashmimotors.app.data.local.VehicleDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): HashmiDatabase {
        return Room.databaseBuilder(
            context,
            HashmiDatabase::class.java,
            "hashmi_motors.db"
        )
            // NOTE: deliberately no fallbackToDestructiveMigration().
            // This database holds the shop's real parts, bills and stock history.
            // Silently wiping it on a schema change would destroy business data, so a
            // missing migration must fail loudly instead. When you bump
            // HashmiDatabase.version, add a matching Migration here.
            .build()
    }

    @Provides fun providePartDao(db: HashmiDatabase): PartDao = db.partDao()
    @Provides fun provideCategoryDao(db: HashmiDatabase): CategoryDao = db.categoryDao()
    @Provides fun provideCustomerDao(db: HashmiDatabase): CustomerDao = db.customerDao()
    @Provides fun provideSupplierDao(db: HashmiDatabase): SupplierDao = db.supplierDao()
    @Provides fun provideVehicleDao(db: HashmiDatabase): VehicleDao = db.vehicleDao()
    @Provides fun provideFitmentDao(db: HashmiDatabase): FitmentDao = db.fitmentDao()
    @Provides fun provideInvoiceDao(db: HashmiDatabase): InvoiceDao = db.invoiceDao()
    @Provides fun provideStockMovementDao(db: HashmiDatabase): StockMovementDao = db.stockMovementDao()
    @Provides fun provideShopDao(db: HashmiDatabase): ShopDao = db.shopDao()
    @Provides fun provideSettingsDao(db: HashmiDatabase): SettingsDao = db.settingsDao()
    @Provides fun provideUserDao(db: HashmiDatabase): UserDao = db.userDao()
}
