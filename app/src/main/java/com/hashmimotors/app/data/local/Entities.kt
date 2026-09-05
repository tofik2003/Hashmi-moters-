package com.hashmimotors.app.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Room entity for Parts.
 * Persisted with all fields; list fields use TypeConverters.
 */
@Entity(
    tableName = "parts",
    indices = [
        Index(value = ["name"]),
        Index(value = ["sku"]),
        Index(value = ["barcode"]),
        Index(value = ["categoryId"]),
        Index(value = ["lastScannedAt"]),
        Index(value = ["scanCount"])
    ]
)
data class PartEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "sku")
    val sku: String,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "oemNumbers")
    val oemNumbers: List<String>,
    @ColumnInfo(name = "brand")
    val brand: String?,
    @ColumnInfo(name = "categoryId")
    val categoryId: String?,
    @ColumnInfo(name = "mrp")
    val mrp: Double,
    @ColumnInfo(name = "sellingPrice")
    val sellingPrice: Double,
    @ColumnInfo(name = "costPrice")
    val costPrice: Double?,
    @ColumnInfo(name = "gstPercent")
    val gstPercent: Double,
    @ColumnInfo(name = "hsnCode")
    val hsnCode: String?,
    @ColumnInfo(name = "stockQty")
    val stockQty: Int,
    @ColumnInfo(name = "reorderLevel")
    val reorderLevel: Int,
    @ColumnInfo(name = "supplierId")
    val supplierId: String?,
    @ColumnInfo(name = "photoPaths")
    val photoPaths: List<String>,
    @ColumnInfo(name = "barcode")
    val barcode: String?,
    @ColumnInfo(name = "notes")
    val notes: String?,
    @ColumnInfo(name = "active")
    val active: Boolean,
    @ColumnInfo(name = "createdAt")
    val createdAt: Long,
    @ColumnInfo(name = "updatedAt")
    val updatedAt: Long,
    @ColumnInfo(name = "lastScannedAt")
    val lastScannedAt: Long? = null,
    @ColumnInfo(name = "scanCount")
    val scanCount: Int = 0,
    @ColumnInfo(name = "lastSoldAt")
    val lastSoldAt: Long? = null,
    @ColumnInfo(name = "totalSold")
    val totalSold: Int = 0,
    @ColumnInfo(name = "favorite")
    val favorite: Boolean = false
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val icon: String,
    val sortOrder: Int
)

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey val id: String,
    val name: String,
    val phone: String,
    val email: String?,
    val address: String?,
    val gstin: String?,
    val totalPurchases: Double,
    val lastVisit: Long?,
    val notes: String?,
    val createdAt: Long
)

@Entity(tableName = "suppliers")
data class SupplierEntity(
    @PrimaryKey val id: String,
    val name: String,
    val phone: String,
    val email: String?,
    val address: String?,
    val gstin: String?,
    val paymentTerms: String?,
    val notes: String?
)

@Entity(tableName = "vehicles")
data class VehicleEntity(
    @PrimaryKey val id: String,
    val make: String,
    val model: String,
    val variants: List<String>,
    val yearFrom: Int,
    val yearTo: Int,
    val fuelTypes: List<String>,
    val bodyType: String?
)

@Entity(tableName = "fitments")
data class FitmentEntity(
    @PrimaryKey val id: String,
    val partId: String,
    val vehicleId: String,
    val position: String?,
    val notes: String?
)

@Entity(
    tableName = "invoices",
    indices = [Index(value = ["invoiceNo"], unique = true), Index(value = ["date"])]
)
data class InvoiceEntity(
    @PrimaryKey val id: String,
    val invoiceNo: String,
    val date: Long,
    val type: String,
    val customerId: String?,
    val customerName: String,
    val customerPhone: String,
    val customerAddress: String?,
    val customerGstin: String?,
    val userId: String,
    val lines: List<InvoiceLineEmbedded>,
    val subtotal: Double,
    val totalDiscount: Double,
    val totalGst: Double,
    val grandTotal: Double,
    val status: String,
    val notes: String?,
    val synced: Boolean,
    val createdAt: Long
)

@Serializable
data class InvoiceLineEmbedded(
    val partId: String,
    val partName: String,
    val oemNumbers: List<String>,
    val hsnCode: String?,
    val mrp: Double,
    val gstPercent: Double,
    val qty: Int,
    val rate: Double,
    val discountPct: Double,
    val lineTotal: Double
)

@Entity(tableName = "stock_movements", indices = [Index(value = ["partId"]), Index(value = ["timestamp"])])
data class StockMovementEntity(
    @PrimaryKey val id: String,
    val partId: String,
    val type: String,
    val qty: Int,
    val refType: String?,
    val refId: String?,
    val reason: String? = null,
    val userId: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "shop")
data class ShopEntity(
    @PrimaryKey val id: String,
    val name: String,
    val address: String,
    val city: String,
    val state: String,
    val stateCode: String,
    val pincode: String,
    val phone: String,
    val email: String?,
    val gstin: String,
    val pan: String?,
    val gstMode: String,
    val invoicePrefix: String,
    val invoiceCounter: Int,
    val logoPath: String?,
    val signaturePath: String?,
    val footerText: String,
    val isSetupComplete: Boolean
)

@Entity(tableName = "app_settings")
data class SettingsEntity(
    @PrimaryKey val id: String,
    val themeMode: String,
    val backgroundStyle: String,
    val accentColor: String,
    val soundsEnabled: Boolean,
    val soundVolume: Int,
    val animationsEnabled: Boolean,
    val animationSpeed: String,
    val tutorialShown: Boolean,
    val pinHash: String?,
    val linkedUserEmails: List<String>
)

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val phone: String?,
    val role: String,
    val biometricEnabled: Boolean,
    val createdAt: Long
)
