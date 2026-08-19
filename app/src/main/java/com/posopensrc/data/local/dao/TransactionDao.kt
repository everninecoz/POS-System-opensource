package com.posopensrc.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.posopensrc.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions WHERE is_voided = 0 ORDER BY created_at DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY created_at DESC")
    fun getAllTransactionsIncludingVoided(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE is_voided = 0 AND created_at >= :startTime AND created_at <= :endTime ORDER BY created_at DESC")
    fun getTransactionsByDateRange(startTime: Long, endTime: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE invoice_number = :invoiceNumber")
    suspend fun getTransactionByInvoice(invoiceNumber: String): TransactionEntity?

    @Query("SELECT SUM(total) FROM transactions WHERE is_voided = 0 AND created_at >= :startTime AND created_at <= :endTime")
    fun getTotalSalesByDateRange(startTime: Long, endTime: Long): Flow<Double?>

    @Query("SELECT COUNT(*) FROM transactions WHERE is_voided = 0 AND created_at >= :startTime AND created_at <= :endTime")
    fun getTransactionCountByDateRange(startTime: Long, endTime: Long): Flow<Int>

    @Query("SELECT SUM(total) FROM transactions WHERE is_voided = 0 AND created_at >= :startTime AND created_at <= :endTime AND payment_method = :method")
    fun getSalesByPaymentMethod(startTime: Long, endTime: Long, method: String): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Query("UPDATE transactions SET is_voided = 1, void_reason = :reason, voided_at = :voidedAt WHERE id = :id")
    suspend fun voidTransaction(id: Long, reason: String?, voidedAt: Long = System.currentTimeMillis())

    @Query("SELECT MAX(id) FROM transactions")
    suspend fun getLastTransactionId(): Long?

    @Query("SELECT SUM(total) FROM transactions WHERE is_voided = 1 AND created_at >= :startTime AND created_at <= :endTime")
    fun getVoidedTotalByDateRange(startTime: Long, endTime: Long): Flow<Double?>

    @Query("SELECT COUNT(*) FROM transactions WHERE is_voided = 1 AND created_at >= :startTime AND created_at <= :endTime")
    fun getVoidedCountByDateRange(startTime: Long, endTime: Long): Flow<Int>
}
