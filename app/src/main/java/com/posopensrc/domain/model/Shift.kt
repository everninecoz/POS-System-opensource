package com.posopensrc.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Shift(
    val id: Long = 0,
    val userId: Long,
    val openingBalance: Double,
    val closingBalance: Double? = null,
    val actualBalance: Double? = null,
    val totalSales: Double = 0.0,
    val totalTransactions: Int = 0,
    val isOpen: Boolean = true,
    val openedAt: Long = System.currentTimeMillis(),
    val closedAt: Long? = null,
    val notes: String? = null
) {
    val duration: Long get() {
        val endTime = closedAt ?: System.currentTimeMillis()
        return endTime - openedAt
    }

    val expectedBalance: Double get() = openingBalance + totalSales

    val difference: Double get() {
        val actual = actualBalance ?: closingBalance ?: 0.0
        return actual - expectedBalance
    }
}
