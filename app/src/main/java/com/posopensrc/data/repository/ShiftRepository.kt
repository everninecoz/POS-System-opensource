package com.posopensrc.data.repository

import com.posopensrc.data.local.dao.ShiftDao
import com.posopensrc.data.local.entity.ShiftEntity
import com.posopensrc.domain.model.Shift
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShiftRepository @Inject constructor(
    private val shiftDao: ShiftDao
) {

    fun getCurrentOpenShift(): Flow<Shift?> {
        return shiftDao.getCurrentOpenShift().map { entity ->
            entity?.toDomain()
        }
    }

    fun getCurrentShiftByUser(userId: Long): Flow<Shift?> {
        return shiftDao.getCurrentShiftByUser(userId).map { entity ->
            entity?.toDomain()
        }
    }

    fun getAllShifts(): Flow<List<Shift>> {
        return shiftDao.getAllShifts().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getShiftsByDateRange(startTime: Long, endTime: Long): Flow<List<Shift>> {
        return shiftDao.getShiftsByDateRange(startTime, endTime).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun getShiftById(id: Long): Shift? {
        return shiftDao.getShiftById(id)?.toDomain()
    }

    suspend fun openShift(userId: Long, openingBalance: Double): Result<Shift> {
        return try {
            // Check if there's already an open shift
            val currentShift = shiftDao.getShiftById(
                shiftDao.getCurrentOpenShift().map { it?.id ?: 0 }.let { 0 }
            )
            if (currentShift != null && currentShift.isOpen) {
                return Result.failure(Exception("Masih ada shift yang terbuka"))
            }

            val entity = ShiftEntity(
                userId = userId,
                openingBalance = openingBalance,
                isOpen = true
            )
            val id = shiftDao.insertShift(entity)
            Result.success(entity.copy(id = id).toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun closeShift(
        shiftId: Long,
        closingBalance: Double,
        actualBalance: Double,
        notes: String?
    ): Result<Shift> {
        return try {
            val shift = shiftDao.getShiftById(shiftId)
                ?: return Result.failure(Exception("Shift tidak ditemukan"))

            shiftDao.closeShift(
                id = shiftId,
                closingBalance = closingBalance,
                actualBalance = actualBalance,
                totalSales = shift.totalSales,
                totalTransactions = shift.totalTransactions,
                notes = notes
            )

            Result.success(shift.copy(
                closingBalance = closingBalance,
                actualBalance = actualBalance,
                isOpen = false,
                closedAt = System.currentTimeMillis(),
                notes = notes
            ).toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addSaleToShift(shiftId: Long, amount: Double): Result<Unit> {
        return try {
            shiftDao.addSaleToShift(shiftId, amount)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getTotalSalesByDateRange(startTime: Long, endTime: Long): Flow<Double?> {
        return shiftDao.getTotalSalesByDateRange(startTime, endTime)
    }

    fun getTotalProfitByDateRange(startTime: Long, endTime: Long): Flow<Double?> {
        return shiftDao.getTotalProfitByDateRange(startTime, endTime)
    }

    private fun ShiftEntity.toDomain(): Shift {
        return Shift(
            id = id,
            userId = userId,
            openingBalance = openingBalance,
            closingBalance = closingBalance,
            actualBalance = actualBalance,
            totalSales = totalSales,
            totalTransactions = totalTransactions,
            isOpen = isOpen,
            openedAt = openedAt,
            closedAt = closedAt,
            notes = notes
        )
    }
}
