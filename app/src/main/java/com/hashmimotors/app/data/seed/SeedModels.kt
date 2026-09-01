package com.hashmimotors.app.data.seed

import kotlinx.serialization.Serializable

/**
 * Data classes for the bundled seed/reference datasets
 * (loaded from the JSON assets under app/src/main/assets/seed/).
 *
 * All values are factual, publicly-known information (vehicle makes/models,
 * standard replacement-part names, HSN codes and GST rates). Nothing here is
 * copied from any proprietary source.
 */
@Serializable
data class SeedVehicle(
    val make: String,
    val model: String,
    val variants: List<String> = emptyList(),
    val yearFrom: Int,
    val yearTo: Int,
    val fuelTypes: List<String> = emptyList(),
    val bodyType: String? = null
)

@Serializable
data class SeedCategory(
    val name: String,
    val icon: String = "build",
    val defaultHsn: String? = null,
    val defaultGstPercent: Double = 0.0
)

@Serializable
data class SeedPartReference(
    val name: String,
    val category: String,
    val hsnCode: String? = null,
    val gstPercent: Double = 0.0,
    val brands: List<String> = emptyList(),
    val keywords: List<String> = emptyList()
)

/**
 * A curated starter-catalog entry that is imported into the parts table on
 * first launch (so the inventory isn't empty out of the box).
 */
@Serializable
data class SeedStarterPart(
    val name: String,
    val category: String,
    val mrp: Double = 0.0,
    val sellingPrice: Double = 0.0,
    val costPrice: Double? = null,
    val stockQty: Int = 0,
    val reorderLevel: Int = 5,
    val hsnCode: String? = null,
    val gstPercent: Double = 0.0,
    val brand: String? = null,
    val keywords: List<String> = emptyList()
)
