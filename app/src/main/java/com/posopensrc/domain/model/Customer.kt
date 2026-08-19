package com.posopensrc.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Customer(
    val id: Long = 0,
    val name: String,
    val phone: String? = null,
    val email: String? = null,
    val address: String? = null,
    val totalPurchases: Double = 0.0,
    val purchaseCount: Int = 0,
    val loyaltyPoints: Int = 0,
    val notes: String? = null,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
