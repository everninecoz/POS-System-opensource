package com.posopensrc.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.posopensrc.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Query("SELECT * FROM users WHERE is_active = 1 ORDER BY full_name ASC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: Long): UserEntity?

    @Query("SELECT * FROM users WHERE username = :username AND is_active = 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET is_active = 0, updated_at = :timestamp WHERE id = :id")
    suspend fun deleteUser(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE users SET pin_hash = :newPinHash, updated_at = :timestamp WHERE id = :id")
    suspend fun updatePin(id: Long, newPinHash: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE users SET password_hash = :newPasswordHash, updated_at = :timestamp WHERE id = :id")
    suspend fun updatePassword(id: Long, newPasswordHash: String, timestamp: Long = System.currentTimeMillis())
}
