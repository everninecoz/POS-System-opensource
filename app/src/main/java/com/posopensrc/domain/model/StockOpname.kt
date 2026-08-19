package com.posopensrc.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class StockOpname(
    val id: Long = 0,
    val userId: Long,
    val notes: String? = null,
    val totalItems: Int = 0,
    val itemsWithDifference: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val items: List<StockOpnameItem> = emptyList()
)

@Serializable
data class StockOpnameItem(
    val id: Long = 0,
    val opnameId: Long,
    val productId: Long,
    val productName: String,
    val systemStock: Int,
    val physicalStock: Int,
    val difference: Int,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
