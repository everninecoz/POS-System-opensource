package com.posopensrc.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.posopensrc.data.local.entity.CustomerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {

    @Query("SELECT * FROM customers WHERE is_active = 1 ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE is_active = 1 AND (name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%') ORDER BY name ASC")
    fun searchCustomers(query: String): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getCustomerById(id: Long): CustomerEntity?

    @Query("SELECT * FROM customers WHERE phone = :phone AND is_active = 1")
    suspend fun getCustomerByPhone(phone: String): CustomerEntity?

    @Query("SELECT COUNT(*) FROM customers WHERE is_active = 1")
    fun getCustomerCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity): Long

    @Update
    suspend fun updateCustomer(customer: CustomerEntity)

    @Query("UPDATE customers SET is_active = 0, updated_at = :timestamp WHERE id = :id")
    suspend fun deleteCustomer(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE customers SET total_purchases = total_purchases + :amount, purchase_count = purchase_count + 1, loyalty_points = loyalty_points + :points, updated_at = :timestamp WHERE id = :id")
    suspend fun updateCustomerPurchase(id: Long, amount: Double, points: Int, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT SUM(total_purchases) FROM customers WHERE is_active = 1")
    fun getTotalCustomerSpending(): Flow<Double?>

    @Query("SELECT * FROM customers WHERE is_active = 1 ORDER BY total_purchases DESC LIMIT :limit")
    fun getTopCustomers(limit: Int = 10): Flow<List<CustomerEntity>>
}
