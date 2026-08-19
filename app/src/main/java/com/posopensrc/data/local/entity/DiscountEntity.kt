package com.posopensrc.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "discounts",
    indices = [
        Index(value = ["code"], unique = true),
        Index(value = ["is_active"]),
        Index(value = ["valid_until"])
    ]
)
data class DiscountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val code: String? = null,
    val description: String? = null,
    @ColumnInfo(name = "discount_type")
    val discountType: String, // "percentage" or "fixed"
    @ColumnInfo(name = "discount_value")
    val discountValue: Double,
    @ColumnInfo(name = "min_purchase")
    val minPurchase: Double = 0.0,
    @ColumnInfo(name = "max_discount")
    val maxDiscount: Double? = null,
    @ColumnInfo(name = "buy_quantity")
    val buyQuantity: Int? = null, // For "Buy X Get Y" promo
    @ColumnInfo(name = "get_quantity")
    val getQuantity: Int? = null, // For "Buy X Get Y" promo
    @ColumnInfo(name = "product_id")
    val productId: Long? = null, // null = applies to all products
    @ColumnInfo(name = "category_id")
    val categoryId: Long? = null, // null = applies to all categories
    @ColumnInfo(name = "usage_limit")
    val usageLimit: Int? = null,
    @ColumnInfo(name = "usage_count")
    val usageCount: Int = 0,
    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,
    @ColumnInfo(name = "valid_from")
    val validFrom: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "valid_until")
    val validUntil: Long = Long.MAX_VALUE,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
