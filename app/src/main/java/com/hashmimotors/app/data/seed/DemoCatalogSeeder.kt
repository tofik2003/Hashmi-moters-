package com.hashmimotors.app.data.seed

import com.hashmimotors.app.data.local.CategoryDao
import com.hashmimotors.app.data.local.FitmentDao
import com.hashmimotors.app.data.local.FitmentEntity
import com.hashmimotors.app.data.local.PartDao
import com.hashmimotors.app.data.local.PartEntity
import com.hashmimotors.app.data.local.VehicleDao
import com.hashmimotors.app.data.repository.CategoryRepository
import com.hashmimotors.app.data.repository.VehicleRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DemoCatalogSeeder @Inject constructor(
    private val partDao: PartDao,
    private val categoryDao: CategoryDao,
    private val vehicleDao: VehicleDao,
    private val fitmentDao: FitmentDao,
    private val categoryRepository: CategoryRepository,
    private val vehicleRepository: VehicleRepository
) {
    suspend fun seedIfEmpty(): Int {
        categoryRepository.ensureSeeded()
        vehicleRepository.ensureSeeded()
        if (partDao.countSnapshot() > 0) return 0
        return seed()
    }

    suspend fun seed(): Int {
        categoryRepository.ensureSeeded()
        vehicleRepository.ensureSeeded()
        val cats = categoryDao.getAllOnce().associate { it.name.lowercase() to it.id }
        fun cat(name: String) = cats[name.lowercase()]

        val now = System.currentTimeMillis()
        fun part(
            name: String,
            sku: String,
            brand: String,
            category: String,
            mrp: Double,
            sell: Double,
            stock: Int,
            barcode: String,
            oem: List<String>,
            hsn: String
        ) = PartEntity(
            id = UUID.randomUUID().toString(),
            sku = sku,
            name = name,
            oemNumbers = oem,
            brand = brand,
            categoryId = cat(category),
            mrp = mrp,
            sellingPrice = sell,
            costPrice = sell * 0.72,
            gstPercent = 0.0,
            hsnCode = hsn,
            stockQty = stock,
            reorderLevel = 4,
            supplierId = null,
            photoPaths = emptyList(),
            barcode = barcode,
            notes = "Sample catalog item — edit or delete anytime",
            active = true,
            createdAt = now,
            updatedAt = now
        )

        val parts = listOf(
            part("Front Brake Pad Set", "BRK-PAD-001", "Bosch", "Brakes", 1850.0, 1490.0, 12, "8901234000001", listOf("55810-63J10"), "8708"),
            part("Rear Brake Shoe Pair", "BRK-SHO-002", "TVS", "Brakes", 980.0, 820.0, 8, "8901234000002", listOf("04495-80J00"), "8708"),
            part("Engine Oil Filter", "FLT-OIL-003", "Mann", "Filters", 320.0, 260.0, 24, "8901234000003", listOf("16510-84A00"), "8421"),
            part("Air Filter Element", "FLT-AIR-004", "Bosch", "Filters", 540.0, 430.0, 18, "8901234000004", listOf("13780-80L00"), "8421"),
            part("Cabin AC Filter", "FLT-CAB-005", "Mann", "Filters", 410.0, 340.0, 10, "8901234000005", listOf("95861-M68K00"), "8421"),
            part("Spark Plug Iridium", "ENG-SPK-006", "NGK", "Engine", 290.0, 240.0, 30, "8901234000006", listOf("ILZKR7B-11S"), "8511"),
            part("Timing Belt Kit", "ENG-BLT-007", "Gates", "Belts & Hoses", 4200.0, 3650.0, 3, "8901234000007", listOf("12761-63J00"), "4010"),
            part("Radiator Coolant 1L", "OIL-CLT-008", "Prestone", "Oils & Fluids", 380.0, 320.0, 16, "8901234000008", listOf("COOL-1L"), "3820"),
            part("15W40 Engine Oil 3.5L", "OIL-ENG-009", "Castrol", "Oils & Fluids", 1650.0, 1420.0, 20, "8901234000009", listOf("15W40-3.5"), "2710"),
            part("Front Shock Absorber LH", "SUS-SHK-010", "Monroe", "Suspension", 2850.0, 2390.0, 4, "8901234000010", listOf("41602-M74R00"), "8708"),
            part("Wiper Blade 22 inch", "BDY-WIP-011", "Bosch", "Body", 450.0, 360.0, 14, "8901234000011", listOf("WIP-22"), "8512"),
            part("Headlamp Assembly RH", "ELC-HLP-012", "Hella", "Electrical", 6200.0, 5450.0, 2, "8901234000012", listOf("35101-M68K00"), "8512"),
            part("Battery 35Ah", "ELC-BAT-013", "Exide", "Electrical", 4800.0, 4150.0, 5, "8901234000013", listOf("DIN35"), "8507"),
            part("Clutch Plate", "ENG-CLU-014", "Valeo", "Engine", 2100.0, 1780.0, 6, "8901234000014", listOf("30210-M68K00"), "8708"),
            part("Side Mirror Manual LH", "BDY-MIR-015", "Local", "Body", 780.0, 620.0, 9, "8901234000015", listOf("87910-M68K00"), "7009")
        )
        partDao.insertAll(parts)

        val swift = vehicleDao.getAllOnce().firstOrNull { it.make == "Maruti" && it.model == "Swift" }
        if (swift != null) {
            val fit = parts.take(8).map {
                FitmentEntity(
                    id = UUID.randomUUID().toString(),
                    partId = it.id,
                    vehicleId = swift.id,
                    position = null,
                    notes = "Sample fitment"
                )
            }
            fitmentDao.insertAll(fit)
        }
        return parts.size
    }
}
