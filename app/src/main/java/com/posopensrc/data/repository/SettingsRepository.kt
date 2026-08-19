package com.posopensrc.data.repository

import com.posopensrc.data.local.dao.SettingsDao
import com.posopensrc.data.local.entity.SettingsEntity
import com.posopensrc.domain.model.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val settingsDao: SettingsDao
) {

    fun getSettings(): Flow<Settings> {
        return settingsDao.getSettings().map { entity ->
            entity?.toDomain() ?: Settings()
        }
    }

    suspend fun getSettingsOnce(): Settings {
        return settingsDao.getSettingsOnce()?.toDomain() ?: Settings()
    }

    suspend fun updateSettings(settings: Settings): Result<Unit> {
        return try {
            val entity = SettingsEntity(
                id = 1,
                storeName = settings.storeName,
                storeAddress = settings.storeAddress,
                storePhone = settings.storePhone,
                storeLogo = settings.storeLogo,
                taxPercentage = settings.taxPercentage,
                receiptFooter = settings.receiptFooter,
                currency = settings.currency,
                language = settings.language,
                updatedAt = System.currentTimeMillis()
            )
            settingsDao.updateSettings(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateStoreInfo(
        name: String,
        address: String?,
        phone: String?
    ): Result<Unit> {
        return try {
            val existing = settingsDao.getSettingsOnce() ?: SettingsEntity()
            settingsDao.updateSettings(
                existing.copy(
                    storeName = name,
                    storeAddress = address,
                    storePhone = phone,
                    updatedAt = System.currentTimeMillis()
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateTaxPercentage(percentage: Double): Result<Unit> {
        return try {
            val existing = settingsDao.getSettingsOnce() ?: SettingsEntity()
            settingsDao.updateSettings(
                existing.copy(
                    taxPercentage = percentage,
                    updatedAt = System.currentTimeMillis()
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun SettingsEntity.toDomain(): Settings {
        return Settings(
            storeName = storeName,
            storeAddress = storeAddress,
            storePhone = storePhone,
            storeLogo = storeLogo,
            taxPercentage = taxPercentage,
            receiptFooter = receiptFooter,
            currency = currency,
            language = language
        )
    }
}
