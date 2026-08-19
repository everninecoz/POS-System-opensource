package com.posopensrc.data.repository

import com.posopensrc.core.security.PasswordUtils
import com.posopensrc.data.local.dao.ActivityLogDao
import com.posopensrc.data.local.dao.UserDao
import com.posopensrc.data.local.entity.ActivityLogEntity
import com.posopensrc.data.local.entity.UserEntity
import com.posopensrc.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val userDao: UserDao,
    private val activityLogDao: ActivityLogDao
) {

    fun getAllUsers(): Flow<List<User>> {
        return userDao.getAllUsers().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun getUserById(id: Long): User? {
        return userDao.getUserById(id)?.toDomain()
    }

    suspend fun loginWithPin(pin: String): User? {
        val users = userDao.getAllUsers().map { it }.firstOrNull() ?: return null
        // We need to check all users for matching pin
        // Since Flow is async, we'll use a different approach
        return null // This will be handled in ViewModel
    }

    suspend fun verifyPin(username: String, pin: String): User? {
        val userEntity = userDao.getUserByUsername(username) ?: return null
        return if (PasswordUtils.verifyPin(pin, userEntity.pinHash)) {
            logActivity(userEntity.id, "login", "Login via PIN")
            userEntity.toDomain()
        } else {
            null
        }
    }

    suspend fun verifyPassword(username: String, password: String): User? {
        val userEntity = userDao.getUserByUsername(username) ?: return null
        val passwordHash = userEntity.passwordHash ?: return null
        return if (PasswordUtils.verifyPassword(password, passwordHash)) {
            logActivity(userEntity.id, "login", "Login via Password")
            userEntity.toDomain()
        } else {
            null
        }
    }

    suspend fun createUser(
        username: String,
        pin: String,
        password: String?,
        fullName: String,
        role: String
    ): Result<User> {
        return try {
            // Check if username already exists
            val existing = userDao.getUserByUsername(username)
            if (existing != null) {
                return Result.failure(Exception("Username sudah digunakan"))
            }

            val pinHash = PasswordUtils.hashPin(pin)
            val passwordHash = password?.let { PasswordUtils.hashPassword(it) }
            val now = System.currentTimeMillis()

            val entity = UserEntity(
                username = username,
                pinHash = pinHash,
                passwordHash = passwordHash,
                fullName = fullName,
                role = role,
                createdAt = now,
                updatedAt = now
            )

            val id = userDao.insertUser(entity)
            Result.success(entity.copy(id = id).toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUser(
        id: Long,
        fullName: String,
        role: String
    ): Result<Unit> {
        return try {
            val existing = userDao.getUserById(id) ?: return Result.failure(Exception("User tidak ditemukan"))
            userDao.updateUser(
                existing.copy(
                    fullName = fullName,
                    role = role,
                    updatedAt = System.currentTimeMillis()
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePin(id: Long, newPin: String): Result<Unit> {
        return try {
            val newPinHash = PasswordUtils.hashPin(newPin)
            userDao.updatePin(id, newPinHash)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePassword(id: Long, newPassword: String): Result<Unit> {
        return try {
            val newPasswordHash = PasswordUtils.hashPassword(newPassword)
            userDao.updatePassword(id, newPasswordHash)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteUser(id: Long): Result<Unit> {
        return try {
            userDao.deleteUser(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserCount(): Int {
        return userDao.getUserCount()
    }

    private suspend fun logActivity(userId: Long, action: String, details: String?) {
        activityLogDao.insertLog(
            ActivityLogEntity(
                userId = userId,
                action = action,
                details = details
            )
        )
    }

    private fun UserEntity.toDomain(): User {
        return User(
            id = id,
            username = username,
            fullName = fullName,
            role = role,
            isActive = isActive,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}
