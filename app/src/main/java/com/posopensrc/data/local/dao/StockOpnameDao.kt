package com.posopensrc.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.posopensrc.data.local.entity.StockOpnameEntity
import com.posopensrc.data.local.entity.StockOpnameItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StockOpnameDao {

    @Query("SELECT * FROM stock_opnames ORDER BY created_at DESC")
    fun getAllStockOpnames(): Flow<List<StockOpnameEntity>>

    @Query("SELECT * FROM stock_opnames WHERE id = :id")
    suspend fun getStockOpnameById(id: Long): StockOpnameEntity?

    @Query("SELECT * FROM stock_opname_items WHERE opname_id = :opnameId")
    fun getStockOpnameItems(opnameId: Long): Flow<List<StockOpnameItemEntity>>

    @Query("SELECT * FROM stock_opname_items WHERE opname_id = :opnameId")
    suspend fun getStockOpnameItemsSync(opnameId: Long): List<StockOpnameItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockOpname(opname: StockOpnameEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockOpnameItems(items: List<StockOpnameItemEntity>)

    @Query("SELECT COUNT(*) FROM stock_opnames")
    fun getStockOpnameCount(): Flow<Int>

    @Query("SELECT * FROM stock_opnames ORDER BY created_at DESC LIMIT 1")
    suspend fun getLatestStockOpname(): StockOpnameEntity?
}
