package com.hashmimotors.app.data.seed

import kotlinx.serialization.Serializable

/**
 * Data classes for the bundled seed/reference datasets
 * (loaded from app/src/main/assets/seed/*.json).
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
