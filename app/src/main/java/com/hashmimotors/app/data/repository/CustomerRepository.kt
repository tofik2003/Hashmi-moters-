package com.hashmimotors.app.data.repository

import com.hashmimotors.app.data.local.CustomerDao
import com.hashmimotors.app.data.local.CustomerEntity
import com.hashmimotors.app.domain.model.Customer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomerRepository @Inject constructor(
    private val customerDao: CustomerDao
) {
    fun getAllCustomers(): Flow<List<Customer>> = customerDao.getAll().map { list ->
        list.map { it.toDomain() }
    }

    fun searchCustomers(query: String): Flow<List<Customer>> = customerDao.search(query).map { list ->
        list.map { it.toDomain() }
    }

    suspend fun getCustomerById(id: String): Customer? = customerDao.getById(id)?.toDomain()

    suspend fun saveCustomer(customer: Customer) {
        customerDao.insert(customer.toEntity())
    }

    suspend fun deleteCustomer(customer: Customer) {
        customerDao.delete(customer.toEntity())
    }
}

fun CustomerEntity.toDomain() = Customer(
    id = id, name = name, phone = phone, email = email, address = address, gstin = gstin,
    totalPurchases = totalPurchases, lastVisit = lastVisit, notes = notes, createdAt = createdAt
)

fun Customer.toEntity() = CustomerEntity(
    id = id, name = name, phone = phone, email = email, address = address, gstin = gstin,
    totalPurchases = totalPurchases, lastVisit = lastVisit, notes = notes, createdAt = createdAt
)
