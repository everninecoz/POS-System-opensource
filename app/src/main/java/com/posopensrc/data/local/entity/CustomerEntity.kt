package com.posopensrc.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "customers",
    indices = [
        Index(value = ["phone"], unique = true),
        Index(value = ["name"])
    ]
)
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String? = null,
    val email: String? = null,
    val address: String? = null,
    @ColumnInfo(name = "total_purchases")
    val totalPurchases: Double = 0.0,
    @ColumnInfo(name = "purchase_count")
    val purchaseCount: Int = 0,
    @ColumnInfo(name = "loyalty_points")
    val loyaltyPoints: Int = 0,
    val notes: String? = null,
    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
