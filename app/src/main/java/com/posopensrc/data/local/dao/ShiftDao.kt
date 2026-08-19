package com.posopensrc.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.posopensrc.data.local.entity.ShiftEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShiftDao {

    @Query("SELECT * FROM shifts WHERE is_open = 1 ORDER BY opened_at DESC LIMIT 1")
    fun getCurrentOpenShift(): Flow<ShiftEntity?>

    @Query("SELECT * FROM shifts WHERE is_open = 1 AND user_id = :userId ORDER BY opened_at DESC LIMIT 1")
    fun getCurrentShiftByUser(userId: Long): Flow<ShiftEntity?>

    @Query("SELECT * FROM shifts ORDER BY opened_at DESC")
    fun getAllShifts(): Flow<List<ShiftEntity>>

    @Query("SELECT * FROM shifts WHERE opened_at >= :startTime AND opened_at <= :endTime ORDER BY opened_at DESC")
    fun getShiftsByDateRange(startTime: Long, endTime: Long): Flow<List<ShiftEntity>>

    @Query("SELECT * FROM shifts WHERE id = :id")
    suspend fun getShiftById(id: Long): ShiftEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShift(shift: ShiftEntity): Long

    @Update
    suspend fun updateShift(shift: ShiftEntity)

    @Query("UPDATE shifts SET is_open = 0, closing_balance = :closingBalance, actual_balance = :actualBalance, total_sales = :totalSales, total_transactions = :totalTransactions, closed_at = :closedAt, notes = :notes WHERE id = :id")
    suspend fun closeShift(
        id: Long,
        closingBalance: Double,
        actualBalance: Double,
        totalSales: Double,
        totalTransactions: Int,
        closedAt: Long = System.currentTimeMillis(),
        notes: String? = null
    )

    @Query("UPDATE shifts SET total_sales = total_sales + :amount, total_transactions = total_transactions + 1 WHERE id = :id")
    suspend fun addSaleToShift(id: Long, amount: Double)

    @Query("SELECT SUM(total_sales) FROM shifts WHERE opened_at >= :startTime AND opened_at <= :endTime")
    fun getTotalSalesByDateRange(startTime: Long, endTime: Long): Flow<Double?>

    @Query("SELECT SUM(closing_balance - opening_balance) FROM shifts WHERE closed_at IS NOT NULL AND closed_at >= :startTime AND closed_at <= :endTime")
    fun getTotalProfitByDateRange(startTime: Long, endTime: Long): Flow<Double?>
}
