package com.hashmimotors.app.data.seed

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Loads the bundled reference datasets from app assets and exposes them for
 * seeding the local database and powering quick-add suggestions.
 *
 * Data is read-only and cached in memory after the first access.
 */
@Singleton
class ReferenceDataRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    val vehicles: List<SeedVehicle> by lazy { loadList("seed/vehicles.json", SeedVehicle.serializer()) }
    val categories: List<SeedCategory> by lazy { loadList("seed/categories.json", SeedCategory.serializer()) }
    val partsReference: List<SeedPartReference> by lazy { loadList("seed/parts_reference.json", SeedPartReference.serializer()) }
    val starterParts: List<SeedStarterPart> by lazy { loadList("seed/starter_parts.json", SeedStarterPart.serializer()) }

    /**
     * Lightweight search over the common-parts reference catalog.
     * Matches name, category, brands and keywords (case-insensitive).
     */
    fun searchParts(query: String, limit: Int = 12): List<SeedPartReference> {
        if (query.isBlank()) return emptyList()
        val q = query.trim().lowercase()
        return partsReference.filter { ref ->
            ref.name.lowercase().contains(q) ||
                ref.category.lowercase().contains(q) ||
                ref.brands.any { it.lowercase().contains(q) } ||
                ref.keywords.any { it.lowercase().contains(q) }
        }.take(limit)
    }

    /**
     * Returns the default HSN code / GST rate for a category name, if known.
     */
    fun defaultsForCategory(categoryName: String): SeedCategory? =
        categories.firstOrNull { it.name.equals(categoryName, ignoreCase = true) }

    private fun <T> loadList(path: String, serializer: KSerializer<T>): List<T> {
        return runCatching {
            context.assets.open(path).bufferedReader().use { reader ->
                json.decodeFromString(ListSerializer(serializer), reader.readText())
            }
        }.getOrElse {
            emptyList()
        }
    }
}
