package com.hashmimotors.app.domain.model

import kotlinx.serialization.Serializable
import java.util.UUID

// ============================================
// USER
// ============================================
@Serializable
data class User(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val email: String,
    val phone: String? = null,
    val role: UserRole = UserRole.OWNER,
    val biometricEnabled: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
enum class UserRole { OWNER, FATHER }

// ============================================
// SHOP
// ============================================
@Serializable
data class Shop(
    val id: String = "default",
    val name: String = "Hashmi Motors",
    val address: String = "",
    val city: String = "",
    val state: String = "",
    val stateCode: String = "",
    val pincode: String = "",
    val phone: String = "",
    val email: String? = null,
    val gstin: String = "",
    val pan: String? = null,
    val gstMode: GstMode = GstMode.COMPOSITION,
    val invoicePrefix: String = "HM",
    val invoiceCounter: Int = 0,
    val logoPath: String? = null,
    val signaturePath: String? = null,
    val footerText: String = "Thank you for your business!",
    val isSetupComplete: Boolean = false
)

@Serializable
enum class GstMode { COMPOSITION, REGULAR }

// ============================================
// CATEGORY
// ============================================
@Serializable
data class Category(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val icon: String = "build",
    val sortOrder: Int = 0
)

// ============================================
// PART
// ============================================
@Serializable
data class Part(
    val id: String = UUID.randomUUID().toString(),
    val sku: String = "",
    val name: String,
    val oemNumbers: List<String> = emptyList(),
    val brand: String? = null,
    val categoryId: String? = null,
    val mrp: Double,
    val sellingPrice: Double,
    val costPrice: Double? = null,
    val gstPercent: Double = 0.0,
    val hsnCode: String? = null,
    val stockQty: Int = 0,
    val reorderLevel: Int = 5,
    val supplierId: String? = null,
    val photoPaths: List<String> = emptyList(),
    val barcode: String? = null,
    val notes: String? = null,
    val active: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

// ============================================
// VEHICLE
// ============================================
@Serializable
data class Vehicle(
    val id: String = UUID.randomUUID().toString(),
    val make: String,
    val model: String,
    val variants: List<String> = emptyList(),
    val yearFrom: Int,
    val yearTo: Int,
    val fuelTypes: List<String> = emptyList(),
    val bodyType: String? = null
)

// ============================================
// FITMENT
// ============================================
@Serializable
data class Fitment(
    val id: String = UUID.randomUUID().toString(),
    val partId: String,
    val vehicleId: String,
    val position: String? = null,
    val notes: String? = null
)

// ============================================
// CUSTOMER
// ============================================
@Serializable
data class Customer(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val phone: String = "",
    val email: String? = null,
    val address: String? = null,
    val gstin: String? = null,
    val totalPurchases: Double = 0.0,
    val lastVisit: Long? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

// ============================================
// SUPPLIER
// ============================================
@Serializable
data class Supplier(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val phone: String = "",
    val email: String? = null,
    val address: String? = null,
    val gstin: String? = null,
    val paymentTerms: String? = null,
    val notes: String? = null
)

// ============================================
// INVOICE
// ============================================
@Serializable
data class Invoice(
    val id: String = UUID.randomUUID().toString(),
    val invoiceNo: String = "",
    val date: Long = System.currentTimeMillis(),
    val type: InvoiceType = InvoiceType.BILL_OF_SUPPLY,
    val customerId: String? = null,
    val customerSnapshot: CustomerSnapshot = CustomerSnapshot(),
    val userId: String = "",
    val lines: List<InvoiceLine> = emptyList(),
    val subtotal: Double = 0.0,
    val totalDiscount: Double = 0.0,
    val totalGst: Double = 0.0,
    val grandTotal: Double = 0.0,
    val status: InvoiceStatus = InvoiceStatus.PAID,
    val notes: String? = null,
    val synced: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
data class InvoiceLine(
    val partId: String,
    val partSnapshot: PartSnapshot = PartSnapshot(),
    val qty: Int,
    val rate: Double,
    val discountPct: Double = 0.0,
    val lineTotal: Double = 0.0
)

@Serializable
data class PartSnapshot(
    val name: String = "",
    val oemNumbers: List<String> = emptyList(),
    val hsnCode: String? = null,
    val mrp: Double = 0.0,
    val gstPercent: Double = 0.0
)

@Serializable
data class CustomerSnapshot(
    val name: String = "Walk-in Customer",
    val phone: String = "",
    val address: String? = null,
    val gstin: String? = null
)

@Serializable
enum class InvoiceType { BILL_OF_SUPPLY, CASH_MEMO, CREDIT_NOTE }

@Serializable
enum class InvoiceStatus { PAID, UNPAID, VOID }

// ============================================
// STOCK MOVEMENT
// ============================================
@Serializable
data class StockMovement(
    val id: String = UUID.randomUUID().toString(),
    val partId: String,
    val type: StockMovementType,
    val qty: Int,
    val refType: String? = null,
    val refId: String? = null,
    val reason: String? = null,
    val userId: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
enum class StockMovementType { IN, OUT, ADJUST }

// ============================================
// APP SETTINGS
// ============================================
@Serializable
data class AppSettings(
    val id: String = "default",
    val themeMode: ThemeMode = ThemeMode.AUTO,
    val backgroundStyle: BackgroundStyle = BackgroundStyle.GRADIENT_PARTICLES,
    val accentColor: AccentColorType = AccentColorType.INDIGO,
    val soundsEnabled: Boolean = true,
    val soundVolume: Int = 80,
    val animationsEnabled: Boolean = true,
    val animationSpeed: AnimationSpeed = AnimationSpeed.NORMAL,
    val tutorialShown: Boolean = false,
    val pinHash: String? = null,
    val linkedUserEmails: List<String> = emptyList()
)

@Serializable
enum class ThemeMode { LIGHT, DARK, AUTO }

@Serializable
enum class BackgroundStyle { GRADIENT_PARTICLES, SOLID }

@Serializable
enum class AccentColorType { INDIGO, BLUE, GREEN, ORANGE }

@Serializable
enum class AnimationSpeed { NORMAL, REDUCED }

// ============================================
// WEB ENRICHMENT (on-demand suggestions)
// ============================================
@Serializable
data class WebEnrichmentResult(
    val partName: String,
    val oemNumbers: List<String> = emptyList(),
    val compatibleVehicles: List<String> = emptyList(),
    val priceRange: String? = null,
    val sourceUrl: String,
    val sourceName: String
)
