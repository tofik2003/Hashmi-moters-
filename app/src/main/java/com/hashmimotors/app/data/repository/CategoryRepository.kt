package com.hashmimotors.app.data.repository

import com.hashmimotors.app.data.local.CategoryDao
import com.hashmimotors.app.data.local.CategoryEntity
import com.hashmimotors.app.data.seed.ReferenceDataRepository
import com.hashmimotors.app.domain.model.Category
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao,
    private val referenceDataRepository: ReferenceDataRepository
) {
    fun getAllCategories(): Flow<List<Category>> = categoryDao.getAll().map { list ->
        list.map { it.toDomain() }
    }

    suspend fun getCategoryById(id: String): Category? = categoryDao.getById(id)?.toDomain()

    suspend fun ensureSeeded() {
        if (categoryDao.count() == 0) {
            val seeded = referenceDataRepository.categories.mapIndexed { i, seed ->
                CategoryEntity(
                    id = java.util.UUID.randomUUID().toString(),
                    name = seed.name,
                    icon = seed.icon,
                    sortOrder = i
                )
            }
            categoryDao.insertAll(if (seeded.isNotEmpty()) seeded else legacyDefaults())
        }
    }

    /**
     * Fallback used only if the bundled assets are unavailable.
     */
    private fun legacyDefaults(): List<CategoryEntity> {
        val defaults = listOf(
            "Engine" to "car", "Brakes" to "disc", "Suspension" to "shock",
            "Electrical" to "bolt", "Body" to "car", "Filters" to "filter",
            "Oils & Fluids" to "oil", "Belts & Hoses" to "chain",
            "Accessories" to "star", "Tools" to "build"
        )
        return defaults.mapIndexed { i, (name, icon) ->
            CategoryEntity(
                id = java.util.UUID.randomUUID().toString(),
                name = name,
                icon = icon,
                sortOrder = i
            )
        }
    }
}

fun CategoryEntity.toDomain() = Category(id = id, name = name, icon = icon, sortOrder = sortOrder)
fun Category.toEntity() = CategoryEntity(id = id, name = name, icon = icon, sortOrder = sortOrder)
