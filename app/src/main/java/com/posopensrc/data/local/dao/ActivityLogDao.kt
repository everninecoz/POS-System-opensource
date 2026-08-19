package com.posopensrc.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.posopensrc.data.local.entity.ActivityLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityLogDao {

    @Query("SELECT * FROM activity_logs ORDER BY created_at DESC LIMIT :limit")
    fun getRecentLogs(limit: Int = 100): Flow<List<ActivityLogEntity>>

    @Query("SELECT * FROM activity_logs WHERE user_id = :userId ORDER BY created_at DESC")
    fun getLogsByUser(userId: Long): Flow<List<ActivityLogEntity>>

    @Query("SELECT * FROM activity_logs WHERE created_at >= :startTime AND created_at <= :endTime ORDER BY created_at DESC")
    fun getLogsByDateRange(startTime: Long, endTime: Long): Flow<List<ActivityLogEntity>>

    @Insert
    suspend fun insertLog(log: ActivityLogEntity)

    @Query("DELETE FROM activity_logs")
    suspend fun clearAllLogs()
}
