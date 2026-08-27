package com.hashmimotors.app.ui.promotions

import com.hashmimotors.app.domain.model.Part
import java.util.UUID

/**
 * Represents a promotional offer that can be applied to parts.
 */
data class Promotion(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val emoji: String,
    val discountPercent: Int,
    val applicableCategoryId: String? = null,
    val applicableBrand: String? = null,
    val minQty: Int = 1,
    val validFrom: Long = System.currentTimeMillis(),
    val validUntil: Long = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000,
    val active: Boolean = true
)

/**
 * Sample promotional offers for the demo.
 * In production, these would be loaded from the database.
 */
object SamplePromotions {
    val offers = listOf(
        Promotion(
            title = "Monsoon Service",
            description = "20% off on all Brake parts",
            emoji = "🌧️",
            discountPercent = 20,
            applicableCategoryId = null,
            applicableBrand = null
        ),
        Promotion(
            title = "Filter Festival",
            description = "Buy 2 Oil Filters, Get 10% off",
            emoji = "🎯",
            discountPercent = 10
        ),
        Promotion(
            title = "Battery Bonanza",
            description = "Flat ₹500 off on Bosch Batteries",
            emoji = "🔋",
            discountPercent = 15,
            applicableBrand = "Bosch"
        ),
        Promotion(
            title = "Headlight Upgrade",
            description = "15% off on all Headlight Assemblies",
            emoji = "💡",
            discountPercent = 15
        )
    )

    /**
     * Returns true if a part qualifies for the given promotion.
     */
    fun isApplicable(promotion: Promotion, part: Part): Boolean {
        if (!promotion.active) return false
        val now = System.currentTimeMillis()
        if (now !in promotion.validFrom..promotion.validUntil) return false
        if (promotion.applicableCategoryId != null && part.categoryId != promotion.applicableCategoryId) return false
        if (promotion.applicableBrand != null && part.brand != promotion.applicableBrand) return false
        return true
    }

    /**
     * Calculate the discounted price for a part under a promotion.
     */
    fun applyDiscount(part: Part, promotion: Promotion): Double {
        return part.sellingPrice * (1 - promotion.discountPercent / 100.0)
    }
}
