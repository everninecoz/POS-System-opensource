package com.posopensrc.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.posopensrc.data.local.entity.TransactionItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionItemDao {

    @Query("SELECT * FROM transaction_items WHERE transaction_id = :transactionId")
    fun getItemsByTransactionId(transactionId: Long): Flow<List<TransactionItemEntity>>

    @Query("SELECT * FROM transaction_items WHERE transaction_id = :transactionId")
    suspend fun getItemsByTransactionIdSync(transactionId: Long): List<TransactionItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<TransactionItemEntity>)

    @Query("UPDATE transaction_items SET is_voided = 1, void_reason = :reason WHERE id = :id")
    suspend fun voidItem(id: Long, reason: String? = null)

    @Query("SELECT SUM(cost_price * quantity) FROM transaction_items WHERE is_voided = 0 AND created_at >= :startTime AND created_at <= :endTime")
    fun getTotalCostByDateRange(startTime: Long, endTime: Long): Flow<Double?>

    @Query("SELECT product_id, SUM(quantity) as totalQuantity FROM transaction_items WHERE is_voided = 0 AND created_at >= :startTime AND created_at <= :endTime GROUP BY product_id ORDER BY totalQuantity DESC")
    fun getTopSellingProducts(startTime: Long, endTime: Long): Flow<List<ProductSalesSummary>>

    @Query("SELECT * FROM transaction_items WHERE product_id = :productId AND is_voided = 0 ORDER BY created_at DESC LIMIT :limit")
    fun getRecentSalesByProduct(productId: Long, limit: Int = 10): Flow<List<TransactionItemEntity>>
}

data class ProductSalesSummary(
    @androidx.room.ColumnInfo(name = "product_id")
    val productId: Long,
    @androidx.room.ColumnInfo(name = "totalQuantity")
    val totalQuantity: Int
)
