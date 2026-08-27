package com.hashmimotors.app.data.repository

import com.hashmimotors.app.data.local.SupplierDao
import com.hashmimotors.app.data.local.SupplierEntity
import com.hashmimotors.app.domain.model.Supplier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupplierRepository @Inject constructor(
    private val supplierDao: SupplierDao
) {
    fun getAllSuppliers(): Flow<List<Supplier>> = supplierDao.getAll().map { list ->
        list.map { it.toDomain() }
    }

    suspend fun getSupplierById(id: String): Supplier? = supplierDao.getById(id)?.toDomain()

    suspend fun saveSupplier(supplier: Supplier) {
        supplierDao.insert(supplier.toEntity())
    }

    suspend fun deleteSupplier(supplier: Supplier) {
        supplierDao.delete(supplier.toEntity())
    }
}

fun SupplierEntity.toDomain() = Supplier(
    id = id, name = name, phone = phone, email = email, address = address,
    gstin = gstin, paymentTerms = paymentTerms, notes = notes
)

fun Supplier.toEntity() = SupplierEntity(
    id = id, name = name, phone = phone, email = email, address = address,
    gstin = gstin, paymentTerms = paymentTerms, notes = notes
)
