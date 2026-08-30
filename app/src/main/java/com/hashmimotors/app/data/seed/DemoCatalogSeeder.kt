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
            notes = "Genuine OEM replacement quality",
            active = true,
            createdAt = now,
            updatedAt = now
        )

        val parts = listOf(
            part("Front Brake Pad Set (Swift/Dzire)", "BRK-PAD-001", "Bosch", "Brakes", 1850.0, 1490.0, 12, "8901234000001", listOf("55810-63J10", "55810M68K10"), "8708"),
            part("Rear Brake Shoe Pair (WagonR/Alto)", "BRK-SHO-002", "TVS", "Brakes", 980.0, 820.0, 8, "8901234000002", listOf("04495-80J00"), "8708"),
            part("Engine Oil Filter (Maruti K-Series)", "FLT-OIL-003", "Mann", "Filters", 320.0, 260.0, 24, "8901234000003", listOf("16510-84A00", "16510M68K00"), "8421"),
            part("Air Filter Element (Hyundai i20/Creta)", "FLT-AIR-004", "Bosch", "Filters", 540.0, 430.0, 18, "8901234000004", listOf("28113-1R100", "13780-80L00"), "8421"),
            part("Cabin AC Pollen Filter", "FLT-CAB-005", "Mann", "Filters", 410.0, 340.0, 10, "8901234000005", listOf("95861-M68K00"), "8421"),
            part("Spark Plug Iridium Single", "ENG-SPK-006", "NGK", "Engine", 290.0, 240.0, 30, "8901234000006", listOf("ILZKR7B-11S", "KR6A-10"), "8511"),
            part("Timing Belt Kit (Tata Nexon/Punch)", "ENG-BLT-007", "Gates", "Belts & Hoses", 4200.0, 3650.0, 3, "8901234000007", listOf("12761-63J00"), "4010"),
            part("Radiator Coolant Green 1L", "OIL-CLT-008", "Castrol", "Oils & Fluids", 380.0, 320.0, 16, "8901234000008", listOf("COOL-GRN-1L"), "3820"),
            part("Castrol Magnatec 15W40 Engine Oil 3.5L", "OIL-ENG-009", "Castrol", "Oils & Fluids", 1650.0, 1420.0, 20, "8901234000009", listOf("15W40-3.5L"), "2710"),
            part("Front Shock Absorber LH (Swift)", "SUS-SHK-010", "Monroe", "Suspension", 2850.0, 2390.0, 4, "8901234000010", listOf("41602-M74R00"), "8708"),
            part("Front Shock Absorber RH (Swift)", "SUS-SHK-011", "Monroe", "Suspension", 2850.0, 2390.0, 4, "8901234000011", listOf("41601-M74R00"), "8708"),
            part("Wiper Blade Frameless 22 inch", "BDY-WIP-012", "Bosch", "Body", 450.0, 360.0, 14, "8901234000012", listOf("WIP-22"), "8512"),
            part("Headlamp Assembly Crystal RH (Swift)", "ELC-HLP-013", "Hella", "Electrical", 6200.0, 5450.0, 2, "8901234000013", listOf("35101-M68K00"), "8512"),
            part("Exide Mileage Car Battery 35Ah", "ELC-BAT-014", "Exide", "Electrical", 4800.0, 4150.0, 5, "8901234000014", listOf("DIN35R-EX"), "8507"),
            part("Clutch Plate & Cover Set (Maruti 1.2L)", "ENG-CLU-015", "Valeo", "Engine", 2100.0, 1780.0, 6, "8901234000015", listOf("30210-M68K00"), "8708"),
            part("Side Mirror Assembly LH Manual (Alto)", "BDY-MIR-016", "Lumax", "Body", 780.0, 620.0, 9, "8901234000016", listOf("87910-M68K00"), "7009"),
            part("Fuel Filter In-Line Diesel", "FLT-FUL-017", "Bosch", "Filters", 850.0, 720.0, 11, "8901234000017", listOf("15410-68K00"), "8421"),
            part("Brake Fluid DOT 4 500ml", "OIL-DOT-018", "Brembo", "Oils & Fluids", 240.0, 195.0, 22, "8901234000018", listOf("DOT4-500"), "3819"),
            part("Horn Set Symphony High/Low", "ELC-HRN-019", "Roots", "Electrical", 950.0, 790.0, 15, "8901234000019", listOf("ROOTS-W90"), "8512"),
            part("Tie Rod End Outer (Maruti/Hyundai)", "SUS-ROD-020", "Rane", "Suspension", 680.0, 560.0, 8, "8901234000020", listOf("48810-60B00"), "8708")
        )
        partDao.insertAll(parts)

        val vehicles = vehicleDao.getAllOnce()
        val swift = vehicles.firstOrNull { it.make.equals("Maruti", ignoreCase = true) && it.model.contains("Swift", ignoreCase = true) }
        val alto = vehicles.firstOrNull { it.make.equals("Maruti", ignoreCase = true) && it.model.contains("Alto", ignoreCase = true) }

        val fitments = mutableListOf<FitmentEntity>()
        if (swift != null) {
            parts.take(10).forEach { p ->
                fitments.add(
                    FitmentEntity(
                        id = UUID.randomUUID().toString(),
                        partId = p.id,
                        vehicleId = swift.id,
                        position = "Front / Engine",
                        notes = "Direct OEM fit"
                    )
                )
            }
        }
        if (alto != null) {
            parts.drop(5).take(10).forEach { p ->
                fitments.add(
                    FitmentEntity(
                        id = UUID.randomUUID().toString(),
                        partId = p.id,
                        vehicleId = alto.id,
                        position = "Standard",
                        notes = "Compatible"
                    )
                )
            }
        }
        if (fitments.isNotEmpty()) {
            fitmentDao.insertAll(fitments)
        }

        return parts.size
    }
}
