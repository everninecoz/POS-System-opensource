package com.posopensrc.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Discount(
    val id: Long = 0,
    val name: String,
    val code: String? = null,
    val description: String? = null,
    val discountType: String, // "percentage" or "fixed"
    val discountValue: Double,
    val minPurchase: Double = 0.0,
    val maxDiscount: Double? = null,
    val buyQuantity: Int? = null,
    val getQuantity: Int? = null,
    val productId: Long? = null,
    val categoryId: Long? = null,
    val usageLimit: Int? = null,
    val usageCount: Int = 0,
    val isActive: Boolean = true,
    val validFrom: Long = System.currentTimeMillis(),
    val validUntil: Long = Long.MAX_VALUE,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val isValid: Boolean
        get() {
            val now = System.currentTimeMillis()
            return isActive && validFrom <= now && validUntil >= now &&
                    (usageLimit == null || usageCount < usageLimit)
        }

    fun calculateDiscount(amount: Double): Double {
        if (amount < minPurchase) return 0.0

        return when (discountType) {
            "percentage" -> {
                val discount = amount * (discountValue / 100)
                if (maxDiscount != null) minOf(discount, maxDiscount) else discount
            }
            "fixed" -> minOf(discountValue, amount)
            else -> 0.0
        }
    }
}

@Serializable
enum class DiscountType(val value: String, val displayName: String) {
    PERCENTAGE("percentage", "Persen (%)"),
    FIXED("fixed", "Nominal (Rp)");

    companion object {
        fun fromString(value: String): DiscountType = when (value) {
            "fixed" -> FIXED
            else -> PERCENTAGE
        }
    }
}
