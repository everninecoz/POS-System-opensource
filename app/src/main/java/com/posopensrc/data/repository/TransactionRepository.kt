package com.posopensrc.data.repository

import com.posopensrc.core.utils.AppConstants
import com.posopensrc.core.utils.DateTimeUtils
import com.posopensrc.data.local.dao.TransactionDao
import com.posopensrc.data.local.entity.TransactionEntity
import com.posopensrc.domain.model.DashboardStats
import com.posopensrc.domain.model.PaymentMethod
import com.posopensrc.domain.model.SalesReport
import com.posopensrc.domain.model.Transaction
import com.posopensrc.domain.model.TransactionItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao,
    private val productDao: com.posopensrc.data.local.dao.ProductDao
) {

    private val json = Json { ignoreUnknownKeys = true }

    fun getAllTransactions(): Flow<List<Transaction>> {
        return transactionDao.getAllTransactions().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getAllTransactionsIncludingVoided(): Flow<List<Transaction>> {
        return transactionDao.getAllTransactionsIncludingVoided().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getTransactionsByDateRange(startTime: Long, endTime: Long): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByDateRange(startTime, endTime).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun getTransactionById(id: Long): Transaction? {
        return transactionDao.getTransactionById(id)?.toDomain()
    }

    suspend fun createTransaction(
        userId: Long,
        customerName: String?,
        customerId: Long? = null,
        shiftId: Long? = null,
        items: List<TransactionItem>,
        subtotal: Double,
        taxPercentage: Double,
        discount: Double,
        paymentMethod: PaymentMethod,
        amountPaid: Double,
        notes: String?
    ): Result<Transaction> {
        return try {
            val taxAmount = subtotal * (taxPercentage / 100)
            val total = subtotal + taxAmount - discount
            val changeAmount = if (paymentMethod == PaymentMethod.CASH) amountPaid - total else 0.0

            // Generate invoice number
            val lastId = transactionDao.getLastTransactionId() ?: 0
            val sequence = (lastId + 1).toInt()
            val invoiceNumber = DateTimeUtils.generateInvoiceNumber(sequence)

            val itemsJson = json.encodeToString(items)

            val entity = TransactionEntity(
                invoiceNumber = invoiceNumber,
                userId = userId,
                customerName = customerName,
                customerId = customerId,
                shiftId = shiftId,
                items = itemsJson,
                subtotal = subtotal,
                taxPercentage = taxPercentage,
                taxAmount = taxAmount,
                discount = discount,
                total = total,
                paymentMethod = paymentMethod.value,
                amountPaid = amountPaid,
                changeAmount = changeAmount,
                notes = notes
            )

            val id = transactionDao.insertTransaction(entity)

            // Reduce stock for each item
            items.forEach { item ->
                productDao.reduceStock(item.productId, item.quantity)
            }

            Result.success(entity.copy(id = id).toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun voidTransaction(transactionId: Long, reason: String?): Result<Unit> {
        return try {
            val transaction = transactionDao.getTransactionById(transactionId)
                ?: return Result.failure(Exception("Transaksi tidak ditemukan"))

            if (transaction.isVoided) {
                return Result.failure(Exception("Transaksi sudah dibatalkan"))
            }

            // Void the transaction
            transactionDao.voidTransaction(transactionId, reason)

            // Restore stock for each item
            val items = try {
                json.decodeFromString<List<TransactionItem>>(transaction.items)
            } catch (e: Exception) {
                emptyList()
            }

            items.forEach { item ->
                productDao.addStock(item.productId, item.quantity)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getTodaySales(): Flow<Double> {
        val (start, end) = getTodayRange()
        return transactionDao.getTotalSalesByDateRange(start, end).map { it ?: 0.0 }
    }

    fun getTodayTransactionCount(): Flow<Int> {
        val (start, end) = getTodayRange()
        return transactionDao.getTransactionCountByDateRange(start, end)
    }

    fun getWeekSales(): Flow<Double> {
        val (start, end) = getWeekRange()
        return transactionDao.getTotalSalesByDateRange(start, end).map { it ?: 0.0 }
    }

    fun getMonthSales(): Flow<Double> {
        val (start, end) = getMonthRange()
        return transactionDao.getTotalSalesByDateRange(start, end).map { it ?: 0.0 }
    }

    fun getVoidedTotalByDateRange(startTime: Long, endTime: Long): Flow<Double> {
        return transactionDao.getVoidedTotalByDateRange(startTime, endTime).map { it ?: 0.0 }
    }

    fun getVoidedCountByDateRange(startTime: Long, endTime: Long): Flow<Int> {
        return transactionDao.getVoidedCountByDateRange(startTime, endTime)
    }

    fun getSalesReport(startDate: Long, endDate: Long): Flow<SalesReport> {
        return combine(
            transactionDao.getTotalSalesByDateRange(startDate, endDate),
            transactionDao.getTransactionCountByDateRange(startDate, endDate),
            transactionDao.getSalesByPaymentMethod(startDate, endDate, AppConstants.PAYMENT_CASH),
            transactionDao.getSalesByPaymentMethod(startDate, endDate, AppConstants.PAYMENT_QRIS),
            transactionDao.getSalesByPaymentMethod(startDate, endDate, AppConstants.PAYMENT_TRANSFER)
        ) { totalSales, transactionCount, cashSales, qrisSales, transferSales ->
            SalesReport(
                startDate = startDate,
                endDate = endDate,
                totalSales = totalSales ?: 0.0,
                totalTransactions = transactionCount,
                cashSales = cashSales ?: 0.0,
                qrisSales = qrisSales ?: 0.0,
                transferSales = transferSales ?: 0.0,
                averageTransaction = if (transactionCount > 0) (totalSales ?: 0.0) / transactionCount else 0.0
            )
        }
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

    private fun TransactionEntity.toDomain(): Transaction {
        val itemsList = try {
            json.decodeFromString<List<TransactionItem>>(items)
        } catch (e: Exception) {
            emptyList()
        }

        return Transaction(
            id = id,
            invoiceNumber = invoiceNumber,
            userId = userId,
            customerName = customerName,
            customerId = customerId,
            shiftId = shiftId,
            items = itemsList,
            subtotal = subtotal,
            taxPercentage = taxPercentage,
            taxAmount = taxAmount,
            discount = discount,
            total = total,
            paymentMethod = paymentMethod,
            amountPaid = amountPaid,
            changeAmount = changeAmount,
            notes = notes,
            isVoided = isVoided,
            voidReason = voidReason,
            voidedAt = voidedAt,
            createdAt = createdAt
        )
    }
}
