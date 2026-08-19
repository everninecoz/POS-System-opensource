package com.posopensrc.domain.model

data class DashboardStats(
    val todaySales: Double = 0.0,
    val todayTransactions: Int = 0,
    val totalProducts: Int = 0,
    val lowStockCount: Int = 0,
    val weekSales: Double = 0.0,
    val monthSales: Double = 0.0
)

data class SalesReport(
    val startDate: Long,
    val endDate: Long,
    val totalSales: Double,
    val totalTransactions: Int,
    val cashSales: Double,
    val qrisSales: Double,
    val transferSales: Double,
    val averageTransaction: Double
)
