package com.hashmimotors.app.data.repository

import com.hashmimotors.app.data.local.CategoryDao
import com.hashmimotors.app.data.local.CategoryEntity
import com.hashmimotors.app.domain.model.Category
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao
) {
    fun getAllCategories(): Flow<List<Category>> = categoryDao.getAll().map { list ->
        list.map { it.toDomain() }
    }

    suspend fun getCategoryById(id: String): Category? = categoryDao.getById(id)?.toDomain()

    suspend fun getAllCategoriesOnce(): List<Category> =
        categoryDao.getAllOnce().map { it.toDomain() }

    /**
     * Resolve a category by name (case-insensitive). Creates it if it doesn't exist yet.
     * Used by bulk CSV import where categories are referenced by name.
     */
    suspend fun getOrCreateCategory(name: String): Category? {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return null
        val existing = categoryDao.getAllOnce().firstOrNull {
            it.name.equals(trimmed, ignoreCase = true)
        }
        if (existing != null) return existing.toDomain()
        val created = Category(
            id = java.util.UUID.randomUUID().toString(),
            name = trimmed,
            icon = "build",
            sortOrder = 999
        )
        categoryDao.insert(created.toEntity())
        return created
    }

    suspend fun ensureSeeded() {
        if (categoryDao.count() == 0) {
            val defaults = listOf(
                "Engine" to "car", "Brakes" to "disc", "Suspension" to "shock",
                "Electrical" to "bolt", "Body" to "car", "Filters" to "filter",
                "Oils & Fluids" to "oil", "Belts & Hoses" to "chain",
                "Accessories" to "star", "Tools" to "build"
            )
            categoryDao.insertAll(defaults.mapIndexed { i, (name, icon) ->
                CategoryEntity(
                    id = java.util.UUID.randomUUID().toString(),
                    name = name,
                    icon = icon,
                    sortOrder = i
                )
            })
        }
    }
}

fun CategoryEntity.toDomain() = Category(id = id, name = name, icon = icon, sortOrder = sortOrder)
fun Category.toEntity() = CategoryEntity(id = id, name = name, icon = icon, sortOrder = sortOrder)
