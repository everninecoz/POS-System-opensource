package com.posopensrc.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.posopensrc.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Query("SELECT * FROM products WHERE is_active = 1 ORDER BY name ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE is_active = 1 AND category = :category ORDER BY name ASC")
    fun getProductsByCategory(category: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE is_active = 1 AND (name LIKE '%' || :query || '%' OR barcode LIKE '%' || :query || '%') ORDER BY name ASC")
    fun searchProducts(query: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Long): ProductEntity?

    @Query("SELECT * FROM products WHERE barcode = :barcode AND is_active = 1")
    suspend fun getProductByBarcode(barcode: String): ProductEntity?

    @Query("SELECT * FROM products WHERE stock <= min_stock AND is_active = 1 ORDER BY stock ASC")
    fun getLowStockProducts(): Flow<List<ProductEntity>>

    @Query("SELECT COUNT(*) FROM products WHERE is_active = 1")
    fun getProductCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM products WHERE stock <= min_stock AND is_active = 1")
    fun getLowStockCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Query("UPDATE products SET is_active = 0, updated_at = :timestamp WHERE id = :id")
    suspend fun deleteProduct(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE products SET stock = stock - :quantity, updated_at = :timestamp WHERE id = :id AND stock >= :quantity")
    suspend fun reduceStock(id: Long, quantity: Int, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE products SET stock = stock + :quantity, updated_at = :timestamp WHERE id = :id")
    suspend fun addStock(id: Long, quantity: Int, timestamp: Long = System.currentTimeMillis())
}
