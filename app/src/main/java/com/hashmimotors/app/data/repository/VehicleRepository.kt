package com.hashmimotors.app.data.repository

import com.hashmimotors.app.data.local.FitmentDao
import com.hashmimotors.app.data.local.FitmentEntity
import com.hashmimotors.app.data.local.VehicleDao
import com.hashmimotors.app.data.local.VehicleEntity
import com.hashmimotors.app.data.seed.ReferenceDataRepository
import com.hashmimotors.app.domain.model.Fitment
import com.hashmimotors.app.domain.model.Vehicle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VehicleRepository @Inject constructor(
    private val vehicleDao: VehicleDao,
    private val fitmentDao: FitmentDao,
    private val referenceDataRepository: ReferenceDataRepository
) {
    fun getAllVehicles(): Flow<List<Vehicle>> = vehicleDao.getAll().map { list ->
        list.map { it.toDomain() }
    }

    fun getByMake(make: String): Flow<List<Vehicle>> = vehicleDao.getByMake(make).map { list ->
        list.map { it.toDomain() }
    }

    suspend fun getVehicleById(id: String): Vehicle? = vehicleDao.getById(id)?.toDomain()

    suspend fun ensureSeeded() {
        if (vehicleDao.count() == 0) {
            val seeded = referenceDataRepository.vehicles.map { seed ->
                VehicleEntity(
                    id = java.util.UUID.randomUUID().toString(),
                    make = seed.make,
                    model = seed.model,
                    variants = seed.variants,
                    yearFrom = seed.yearFrom,
                    yearTo = seed.yearTo,
                    fuelTypes = seed.fuelTypes,
                    bodyType = seed.bodyType
                )
            }
            vehicleDao.insertAll(if (seeded.isNotEmpty()) seeded else legacySeed())
        }
    }

    /**
     * Legacy hardcoded seed — kept only as a fallback in case the bundled
     * assets are unavailable at runtime.
     */
    private fun legacySeed(): List<VehicleEntity> {
        val vehicles = mutableListOf<VehicleEntity>()
        val id = { java.util.UUID.randomUUID().toString() }
        val v = { make: String, model: String, variants: List<String>, from: Int, to: Int, fuels: List<String>, body: String ->
            VehicleEntity(
                id = id(), make = make, model = model, variants = variants,
                yearFrom = from, yearTo = to, fuelTypes = fuels, bodyType = body
            )
        }
        // Maruti
        vehicles += v("Maruti", "Swift", listOf("LXI","VXI","ZXI","ZXI+"), 2014, 2025, listOf("Petrol","CNG"), "Hatchback")
        vehicles += v("Maruti", "Baleno", listOf("Sigma","Delta","Zeta","Alpha"), 2015, 2025, listOf("Petrol","CNG"), "Hatchback")
        vehicles += v("Maruti", "Dzire", listOf("LXI","VXI","ZXI","ZXI+"), 2017, 2025, listOf("Petrol","CNG"), "Sedan")
        vehicles += v("Maruti", "Vitara Brezza", listOf("LXI","VXI","ZXI","ZXI+"), 2016, 2025, listOf("Petrol","CNG"), "Compact SUV")
        vehicles += v("Maruti", "Ertiga", listOf("LXI","VXI","ZXI","ZXI+"), 2018, 2025, listOf("Petrol","CNG"), "MPV")
        vehicles += v("Maruti", "Wagon R", listOf("LXI","VXI","ZXI"), 2019, 2025, listOf("Petrol","CNG"), "Hatchback")
        vehicles += v("Maruti", "Alto K10", listOf("STD","LXI","VXI"), 2022, 2025, listOf("Petrol","CNG"), "Hatchback")
        // Hyundai
        vehicles += v("Hyundai", "Creta", listOf("E","EX","S","SX","SX(O)"), 2015, 2025, listOf("Petrol","Diesel"), "Compact SUV")
        vehicles += v("Hyundai", "Venue", listOf("E","S","S+","SX","SX(O)"), 2019, 2025, listOf("Petrol","Diesel"), "Compact SUV")
        vehicles += v("Hyundai", "Verna", listOf("EX","S","SX","SX(O)"), 2017, 2025, listOf("Petrol","Diesel"), "Sedan")
        vehicles += v("Hyundai", "i20", listOf("Magna","Sportz","Asta","Asta(O)"), 2020, 2025, listOf("Petrol","Diesel"), "Hatchback")
        vehicles += v("Hyundai", "Grand i10 Nios", listOf("Era","Magna","Sportz","Asta"), 2019, 2025, listOf("Petrol","CNG"), "Hatchback")
        vehicles += v("Hyundai", "Aura", listOf("E","S","SX","SX+"), 2020, 2025, listOf("Petrol","CNG"), "Sedan")
        // Tata
        vehicles += v("Tata", "Nexon", listOf("XE","XM","XZ","XZ+","Fearless","Creative"), 2017, 2025, listOf("Petrol","Diesel","EV"), "Compact SUV")
        vehicles += v("Tata", "Punch", listOf("Pure","Adventure","Accomplished","Creative"), 2021, 2025, listOf("Petrol","CNG","EV"), "Micro SUV")
        vehicles += v("Tata", "Harrier", listOf("XE","XM","XZ","XZ+","Dark Edition"), 2019, 2025, listOf("Diesel"), "SUV")
        vehicles += v("Tata", "Safari", listOf("XE","XM","XZ","XZ+","Accomplished"), 2021, 2025, listOf("Diesel"), "SUV")
        vehicles += v("Tata", "Altroz", listOf("XE","XM","XZ","XZ+","Dark Edition"), 2020, 2025, listOf("Petrol","Diesel","CNG"), "Hatchback")
        vehicles += v("Tata", "Tiago", listOf("XE","XM","XT","XZ"), 2016, 2025, listOf("Petrol","CNG","EV"), "Hatchback")
        // Mahindra
        vehicles += v("Mahindra", "Scorpio", listOf("S3","S5","S7","S9","S11","Z4","Z6","Z8"), 2014, 2025, listOf("Diesel","Petrol"), "SUV")
        vehicles += v("Mahindra", "XUV300", listOf("W4","W6","W8","W8(O)"), 2019, 2025, listOf("Petrol","Diesel"), "Compact SUV")
        vehicles += v("Mahindra", "XUV400", listOf("EC","EL"), 2023, 2025, listOf("EV"), "Compact SUV")
        vehicles += v("Mahindra", "Thar", listOf("AX","LX","AX(O)","LX(O)"), 2010, 2025, listOf("Petrol","Diesel"), "SUV")
        vehicles += v("Mahindra", "Bolero", listOf("B4","B6","B6(O)"), 2010, 2025, listOf("Diesel"), "SUV")
        // Honda
        vehicles += v("Honda", "City", listOf("SV","V","VX","ZX"), 2014, 2025, listOf("Petrol","Diesel"), "Sedan")
        vehicles += v("Honda", "Amaze", listOf("E","S","VX","ZX"), 2018, 2025, listOf("Petrol","Diesel"), "Sedan")
        vehicles += v("Honda", "Jazz", listOf("V","VX","ZX"), 2015, 2020, listOf("Petrol","Diesel"), "Hatchback")
        vehicles += v("Honda", "WR-V", listOf("SV","VX","ZX"), 2017, 2020, listOf("Petrol","Diesel"), "Crossover")
        // Toyota
        vehicles += v("Toyota", "Fortuner", listOf("4x2","4x4","Legender","GR-S"), 2016, 2025, listOf("Petrol","Diesel"), "SUV")
        vehicles += v("Toyota", "Innova Crysta", listOf("GX","VX","ZX","ZX(O)"), 2016, 2022, listOf("Petrol","Diesel"), "MPV")
        vehicles += v("Toyota", "Innova Hycross", listOf("GX","VX","ZX","ZX(O)"), 2022, 2025, listOf("Petrol","Hybrid"), "MPV")
        vehicles += v("Toyota", "Urban Cruiser Hyryder", listOf("E","S","G","V"), 2022, 2025, listOf("Petrol","Hybrid","CNG"), "Compact SUV")
        vehicles += v("Toyota", "Glanza", listOf("E","S","G","V"), 2019, 2025, listOf("Petrol","CNG"), "Hatchback")
        vehicles += v("Toyota", "Taisor", listOf("E","S","G","V"), 2024, 2025, listOf("Petrol","CNG"), "Compact SUV")
        // Kia
        vehicles += v("Kia", "Seltos", listOf("HTE","HTK","HTX","HTX+","GTX","GTX+","X-Line"), 2019, 2025, listOf("Petrol","Diesel"), "Compact SUV")
        vehicles += v("Kia", "Sonet", listOf("HTE","HTK","HTX","HTX+","GTX","GTX+"), 2020, 2025, listOf("Petrol","Diesel"), "Compact SUV")
        vehicles += v("Kia", "Carens", listOf("Premium","Prestige","Prestige Plus","Luxury","Luxury Plus"), 2022, 2025, listOf("Petrol","Diesel"), "MPV")
        // Others
        vehicles += v("Renault", "Kwid", listOf("RXE","RXL","RXT","Climber"), 2015, 2025, listOf("Petrol"), "Hatchback")
        vehicles += v("Renault", "Triber", listOf("RXE","RXL","RXT","RXZ"), 2019, 2025, listOf("Petrol","CNG"), "MPV")
        vehicles += v("Renault", "Kiger", listOf("RXE","RXL","RXT","RXZ"), 2021, 2025, listOf("Petrol","CNG"), "Compact SUV")
        vehicles += v("Nissan", "Magnite", listOf("XE","XL","XV","XV Premium"), 2020, 2025, listOf("Petrol","CNG"), "Compact SUV")
        vehicles += v("Volkswagen", "Polo", listOf("Trendline","Comfortline","Highline","GT"), 2014, 2022, listOf("Petrol","Diesel"), "Hatchback")
        vehicles += v("Volkswagen", "Virtus", listOf("Comfortline","Highline","Topline","GT"), 2022, 2025, listOf("Petrol"), "Sedan")
        vehicles += v("Skoda", "Slavia", listOf("Active","Ambition","Style","Monte Carlo"), 2022, 2025, listOf("Petrol"), "Sedan")
        vehicles += v("Skoda", "Kushaq", listOf("Active","Ambition","Style","Monte Carlo","Lava Blue"), 2021, 2025, listOf("Petrol"), "Compact SUV")
        vehicles += v("MG", "Hector", listOf("Style","Super","Sharp","Savvy","Plus"), 2019, 2025, listOf("Petrol","Diesel","Hybrid"), "SUV")
        vehicles += v("MG", "ZS EV", listOf("Excite","Exclusive","Essence"), 2020, 2025, listOf("EV"), "Compact SUV")
        vehicles += v("MG", "Windsor EV", listOf("Excite","Exclusive","Essence"), 2024, 2025, listOf("EV"), "Crossover")
        vehicles += v("Jeep", "Compass", listOf("Sport","Longitude","Limited","Model S","Black Pack"), 2017, 2025, listOf("Petrol","Diesel"), "SUV")
        vehicles += v("Jeep", "Meridian", listOf("Limited","Model S","Overland"), 2022, 2025, listOf("Diesel"), "SUV")
        vehicles += v("Citroen", "C3", listOf("Live","Feel","Shine","Shine (O)"), 2022, 2025, listOf("Petrol","CNG"), "Hatchback")
        vehicles += v("Citroen", "C3 Aircross", listOf("You","Plus","Max"), 2023, 2025, listOf("Petrol"), "Compact SUV")
        vehicles += v("Force", "Gurkha", listOf("Xplorer","Xpedition"), 2021, 2025, listOf("Diesel"), "SUV")

        return vehicles
    }

    fun getFitmentsForVehicle(vehicleId: String): Flow<List<Fitment>> =
        fitmentDao.getForVehicle(vehicleId).map { list -> list.map { it.toDomain() } }

    fun getFitmentsForPart(partId: String): Flow<List<Fitment>> =
        fitmentDao.getForPart(partId).map { list -> list.map { it.toDomain() } }

    suspend fun addFitment(fitment: Fitment) {
        fitmentDao.insert(fitment.toEntity())
    }

    suspend fun deleteFitment(fitment: Fitment) {
        fitmentDao.delete(fitment.toEntity())
    }
}

fun VehicleEntity.toDomain() = Vehicle(
    id = id, make = make, model = model, variants = variants,
    yearFrom = yearFrom, yearTo = yearTo, fuelTypes = fuelTypes, bodyType = bodyType
)
fun Vehicle.toEntity() = VehicleEntity(
    id = id, make = make, model = model, variants = variants,
    yearFrom = yearFrom, yearTo = yearTo, fuelTypes = fuelTypes, bodyType = bodyType
)

fun FitmentEntity.toDomain() = Fitment(
    id = id, partId = partId, vehicleId = vehicleId, position = position, notes = notes
)
fun Fitment.toEntity() = FitmentEntity(
    id = id, partId = partId, vehicleId = vehicleId, position = position, notes = notes
)
