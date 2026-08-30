package com.hashmimotors.app.data.backup

import android.content.Context
import android.net.Uri
import com.hashmimotors.app.data.local.CustomerDao
import com.hashmimotors.app.data.local.InvoiceDao
import com.hashmimotors.app.data.local.PartDao
import com.hashmimotors.app.data.local.SupplierDao
import com.hashmimotors.app.data.repository.CustomerRepository
import com.hashmimotors.app.data.repository.InvoiceRepository
import com.hashmimotors.app.data.repository.PartRepository
import com.hashmimotors.app.data.repository.ShopRepository
import com.hashmimotors.app.data.repository.SupplierRepository
import com.hashmimotors.app.data.repository.toDomain
import com.hashmimotors.app.domain.model.Customer
import com.hashmimotors.app.domain.model.Invoice
import com.hashmimotors.app.domain.model.Part
import com.hashmimotors.app.domain.model.Shop
import com.hashmimotors.app.domain.model.Supplier
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class AppBackup(
    val version: Int = 1,
    val exportedAt: Long = System.currentTimeMillis(),
    val shop: Shop? = null,
    val parts: List<Part> = emptyList(),
    val customers: List<Customer> = emptyList(),
    val suppliers: List<Supplier> = emptyList(),
    val invoices: List<Invoice> = emptyList()
)

@Singleton
class BackupRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val partDao: PartDao,
    private val customerDao: CustomerDao,
    private val invoiceDao: InvoiceDao,
    private val supplierDao: SupplierDao,
    private val shopRepository: ShopRepository,
    private val partRepository: PartRepository,
    private val customerRepository: CustomerRepository,
    private val invoiceRepository: InvoiceRepository,
    private val supplierRepository: SupplierRepository
) {
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true; encodeDefaults = true }

    suspend fun exportToFile(): File {
        val backup = AppBackup(
            shop = shopRepository.getShopOnce(),
            parts = partDao.getAllOnce().map { it.toDomain() },
            customers = customerDao.getAllOnce().map { it.toDomain() },
            suppliers = supplierDao.getAllOnce().map { it.toDomain() },
            invoices = invoiceDao.getAllOnce().map { it.toDomain() }
        )
        val dir = File(context.cacheDir, "backups").apply { mkdirs() }
        val file = File(dir, "hashmi_motors_backup_${System.currentTimeMillis()}.json")
        file.writeText(json.encodeToString(backup))
        return file
    }

    suspend fun importFromUri(uri: Uri): Int {
        val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.readText()
            ?: error("Could not read backup file")
        val backup = json.decodeFromString<AppBackup>(text)
        backup.shop?.let { shopRepository.saveShop(it.copy(isSetupComplete = true)) }
        if (backup.parts.isNotEmpty()) partRepository.saveAllParts(backup.parts)
        backup.customers.forEach { customerRepository.saveCustomer(it) }
        backup.suppliers.forEach { supplierRepository.saveSupplier(it) }
        backup.invoices.forEach { invoiceRepository.saveInvoice(it) }
        return backup.parts.size + backup.customers.size + backup.invoices.size + backup.suppliers.size
    }
}
