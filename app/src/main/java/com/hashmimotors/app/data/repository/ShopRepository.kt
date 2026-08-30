package com.hashmimotors.app.data.repository

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
    private val shopDao: ShopDao
) {
    fun getShop(): Flow<Shop?> = shopDao.get().map { it?.toDomain() }

    suspend fun getShopOnce(): Shop? = shopDao.getOnce()?.toDomain()

    suspend fun saveShop(shop: Shop) {
        shopDao.upsert(shop.toEntity())
    }

    /**
     * Atomically increment invoice counter and return the next formatted number.
     * Follows the Indian Financial Year (e.g. HM/2026-27/000001).
     */
    suspend fun getNextInvoiceNumber(): String {
        val shop = shopDao.getOnce() ?: ShopEntity(
            id = "default",
            name = "Hashmi Motors",
            address = "", city = "", state = "", stateCode = "", pincode = "",
            phone = "", email = null, gstin = "", pan = null,
            gstMode = GstMode.COMPOSITION.name,
            invoicePrefix = "HM",
            invoiceCounter = 0,
            logoPath = null, signaturePath = null,
            footerText = "Thank you for your business!",
            isSetupComplete = false
        )
        val nextCounter = shop.invoiceCounter + 1
        val fy = FinancialYear.label(System.currentTimeMillis())
        val invoiceNo = "${shop.invoicePrefix}/$fy/${nextCounter.toString().padStart(6, '0')}"
        shopDao.upsert(shop.copy(invoiceCounter = nextCounter))
        return invoiceNo
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
