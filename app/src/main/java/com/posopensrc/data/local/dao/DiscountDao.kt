package com.posopensrc.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.posopensrc.data.local.entity.DiscountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DiscountDao {

    @Query("SELECT * FROM discounts WHERE is_active = 1 ORDER BY name ASC")
    fun getAllActiveDiscounts(): Flow<List<DiscountEntity>>

    @Query("SELECT * FROM discounts ORDER BY created_at DESC")
    fun getAllDiscounts(): Flow<List<DiscountEntity>>

    @Query("SELECT * FROM discounts WHERE id = :id")
    suspend fun getDiscountById(id: Long): DiscountEntity?

    @Query("SELECT * FROM discounts WHERE code = :code AND is_active = 1")
    suspend fun getDiscountByCode(code: String): DiscountEntity?

    @Query("SELECT * FROM discounts WHERE is_active = 1 AND valid_from <= :now AND valid_until >= :now")
    fun getValidDiscounts(now: Long = System.currentTimeMillis()): Flow<List<DiscountEntity>>

    @Query("SELECT * FROM discounts WHERE is_active = 1 AND product_id = :productId AND valid_from <= :now AND valid_until >= :now")
    suspend fun getDiscountsForProduct(productId: Long, now: Long = System.currentTimeMillis()): List<DiscountEntity>

    @Query("SELECT * FROM discounts WHERE is_active = 1 AND category_id = :categoryId AND valid_from <= :now AND valid_until >= :now")
    suspend fun getDiscountsForCategory(categoryId: Long, now: Long = System.currentTimeMillis()): List<DiscountEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiscount(discount: DiscountEntity): Long

    @Update
    suspend fun updateDiscount(discount: DiscountEntity)

    @Query("UPDATE discounts SET is_active = 0, updated_at = :timestamp WHERE id = :id")
    suspend fun deleteDiscount(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE discounts SET usage_count = usage_count + 1, updated_at = :timestamp WHERE id = :id")
    suspend fun incrementUsageCount(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM discounts WHERE is_active = 1")
    fun getActiveDiscountCount(): Flow<Int>
}
