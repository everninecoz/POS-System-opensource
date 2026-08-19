package com.posopensrc.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ProfitLossReport(
    val startDate: Long,
    val endDate: Long,
    val totalRevenue: Double,
    val totalCost: Double,
    val grossProfit: Double,
    val totalDiscounts: Double,
    val totalTax: Double,
    val netProfit: Double,
    val profitMargin: Double,
    val transactionCount: Int,
    val averageTransactionValue: Double,
    val topSellingProducts: List<TopSellingProduct>,
    val salesByPaymentMethod: Map<String, Double>,
    val dailyBreakdown: List<DailyBreakdown>
)

@Serializable
data class TopSellingProduct(
    val productId: Long,
    val productName: String,
    val totalQuantity: Int,
    val totalRevenue: Double,
    val totalCost: Double,
    val profit: Double
)

@Serializable
data class DailyBreakdown(
    val date: Long,
    val revenue: Double,
    val cost: Double,
    val profit: Double,
    val transactionCount: Int
)
