package com.posopensrc.data.repository

import com.posopensrc.data.local.dao.TransactionDao
import com.posopensrc.data.local.dao.TransactionItemDao
import com.posopensrc.domain.model.DailyBreakdown
import com.posopensrc.domain.model.ProfitLossReport
import com.posopensrc.domain.model.TopSellingProduct
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfitLossRepository @Inject constructor(
    private val transactionDao: TransactionDao,
    private val transactionItemDao: TransactionItemDao
) {

    fun getProfitLossReport(startDate: Long, endDate: Long): Flow<ProfitLossReport> {
        return combine(
            transactionDao.getTotalSalesByDateRange(startDate, endDate),
            transactionDao.getTransactionCountByDateRange(startDate, endDate),
            transactionItemDao.getTotalCostByDateRange(startDate, endDate),
            transactionItemDao.getTopSellingProducts(startDate, endDate)
        ) { totalSales, transactionCount, totalCost, topSellingProducts ->
            val revenue = totalSales ?: 0.0
            val cost = totalCost ?: 0.0
            val grossProfit = revenue - cost
            val profitMargin = if (revenue > 0) (grossProfit / revenue) * 100 else 0.0
            val avgTransaction = if (transactionCount > 0) revenue / transactionCount else 0.0

            ProfitLossReport(
                startDate = startDate,
                endDate = endDate,
                totalRevenue = revenue,
                totalCost = cost,
                grossProfit = grossProfit,
                totalDiscounts = 0.0, // TODO: Calculate from transactions
                totalTax = 0.0, // TODO: Calculate from transactions
                netProfit = grossProfit,
                profitMargin = profitMargin,
                transactionCount = transactionCount,
                averageTransactionValue = avgTransaction,
                topSellingProducts = topSellingProducts.map {
                    TopSellingProduct(
                        productId = it.productId,
                        productName = "", // TODO: Join with products table
                        totalQuantity = it.totalQuantity,
                        totalRevenue = 0.0, // TODO: Calculate
                        totalCost = 0.0, // TODO: Calculate
                        profit = 0.0 // TODO: Calculate
                    )
                },
                salesByPaymentMethod = emptyMap(), // TODO: Implement
                dailyBreakdown = emptyList() // TODO: Implement
            )
        }
    }

    fun getTodayProfitLoss(): Flow<ProfitLossReport> {
        val (start, end) = getTodayRange()
        return getProfitLossReport(start, end)
    }

    fun getWeekProfitLoss(): Flow<ProfitLossReport> {
        val (start, end) = getWeekRange()
        return getProfitLossReport(start, end)
    }

    fun getMonthProfitLoss(): Flow<ProfitLossReport> {
        val (start, end) = getMonthRange()
        return getProfitLossReport(start, end)
    }

    private fun getTodayRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val start = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val end = calendar.timeInMillis

        return Pair(start, end)
    }

    private fun getWeekRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val start = calendar.timeInMillis

        val end = System.currentTimeMillis()
        return Pair(start, end)
    }

    private fun getMonthRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val start = calendar.timeInMillis

        val end = System.currentTimeMillis()
        return Pair(start, end)
    }
}
