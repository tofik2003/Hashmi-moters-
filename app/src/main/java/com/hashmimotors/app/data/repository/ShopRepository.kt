package com.hashmimotors.app.data.repository

import androidx.room.withTransaction
import com.hashmimotors.app.data.local.HashmiDatabase
import com.hashmimotors.app.data.local.ShopDao
import com.hashmimotors.app.data.local.ShopEntity
import com.hashmimotors.app.domain.model.GstMode
import com.hashmimotors.app.domain.model.Shop
import com.hashmimotors.app.domain.money.FinancialYear
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShopRepository @Inject constructor(
    private val db: HashmiDatabase,
    private val shopDao: ShopDao
) {
    fun getShop(): Flow<Shop?> = shopDao.get().map { it?.toDomain() }

    suspend fun getShopOnce(): Shop? = shopDao.getOnce()?.toDomain()

    suspend fun saveShop(shop: Shop) {
        shopDao.upsert(shop.toEntity())
    }

    /**
     * Reserve the next invoice number.
     *
     * The counter bump and the read-back happen inside one transaction, so two
     * bills created at the same moment can never be handed the same number
     * (`invoices.invoiceNo` carries a UNIQUE index, so a duplicate would abort
     * the whole sale).
     */
    suspend fun claimNextInvoiceNumber(): String = db.withTransaction {
        val shop = shopDao.getOnce() ?: DEFAULT_SHOP_ENTITY
        shopDao.upsert(shop)
        shopDao.incrementInvoiceCounter()
        val claimed = shopDao.getOnce() ?: shop.copy(invoiceCounter = shop.invoiceCounter + 1)
        "${claimed.invoicePrefix}/${financialYearFor(System.currentTimeMillis())}" +
            "/${claimed.invoiceCounter.toString().padStart(6, '0')}"
    }

    companion object {
        /**
         * Financial-year label used in invoice numbers, e.g. "2026-27".
         * GST numbering follows the 1 April - 31 March financial year; see
         * [FinancialYear] for the pure, unit-tested implementation.
         */
        fun financialYearFor(epochMillis: Long): String = FinancialYear.label(epochMillis)

        private val DEFAULT_SHOP_ENTITY = ShopEntity(
            id = "default",
            name = "Hashmi Motors",
            address = "", city = "", state = "", stateCode = "", pincode = "",
            phone = "", email = null, gstin = "", pan = null,
            gstMode = GstMode.COMPOSITION.name,
            invoicePrefix = "HM",
            invoiceCounter = 0,
            logoPath = null, signaturePath = null,
            footerText = "Thank you!",
            isSetupComplete = false
        )
    }
}

fun ShopEntity.toDomain() = Shop(
    id = id,
    name = name,
    address = address,
    city = city,
    state = state,
    stateCode = stateCode,
    pincode = pincode,
    phone = phone,
    email = email,
    gstin = gstin,
    pan = pan,
    gstMode = runCatching { GstMode.valueOf(gstMode) }.getOrDefault(GstMode.COMPOSITION),
    invoicePrefix = invoicePrefix,
    invoiceCounter = invoiceCounter,
    logoPath = logoPath,
    signaturePath = signaturePath,
    footerText = footerText,
    isSetupComplete = isSetupComplete
)

fun Shop.toEntity() = ShopEntity(
    id = id,
    name = name,
    address = address,
    city = city,
    state = state,
    stateCode = stateCode,
    pincode = pincode,
    phone = phone,
    email = email,
    gstin = gstin,
    pan = pan,
    gstMode = gstMode.name,
    invoicePrefix = invoicePrefix,
    invoiceCounter = invoiceCounter,
    logoPath = logoPath,
    signaturePath = signaturePath,
    footerText = footerText,
    isSetupComplete = isSetupComplete
)
