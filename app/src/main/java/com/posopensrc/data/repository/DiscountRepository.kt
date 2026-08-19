package com.posopensrc.data.repository

import com.posopensrc.data.local.dao.DiscountDao
import com.posopensrc.data.local.entity.DiscountEntity
import com.posopensrc.domain.model.Discount
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiscountRepository @Inject constructor(
    private val discountDao: DiscountDao
) {

    fun getAllDiscounts(): Flow<List<Discount>> {
        return discountDao.getAllDiscounts().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getAllActiveDiscounts(): Flow<List<Discount>> {
        return discountDao.getAllActiveDiscounts().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getValidDiscounts(): Flow<List<Discount>> {
        return discountDao.getValidDiscounts().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun getDiscountById(id: Long): Discount? {
        return discountDao.getDiscountById(id)?.toDomain()
    }

    suspend fun getDiscountByCode(code: String): Discount? {
        return discountDao.getDiscountByCode(code)?.toDomain()
    }

    suspend fun getDiscountsForProduct(productId: Long): List<Discount> {
        return discountDao.getDiscountsForProduct(productId).map { it.toDomain() }
    }

    suspend fun getDiscountsForCategory(categoryId: Long): List<Discount> {
        return discountDao.getDiscountsForCategory(categoryId).map { it.toDomain() }
    }

    suspend fun createDiscount(
        name: String,
        code: String?,
        description: String?,
        discountType: String,
        discountValue: Double,
        minPurchase: Double,
        maxDiscount: Double?,
        buyQuantity: Int?,
        getQuantity: Int?,
        productId: Long?,
        categoryId: Long?,
        usageLimit: Int?,
        validFrom: Long,
        validUntil: Long
    ): Result<Discount> {
        return try {
            val entity = DiscountEntity(
                name = name,
                code = code?.uppercase(),
                description = description,
                discountType = discountType,
                discountValue = discountValue,
                minPurchase = minPurchase,
                maxDiscount = maxDiscount,
                buyQuantity = buyQuantity,
                getQuantity = getQuantity,
                productId = productId,
                categoryId = categoryId,
                usageLimit = usageLimit,
                validFrom = validFrom,
                validUntil = validUntil
            )
            val id = discountDao.insertDiscount(entity)
            Result.success(entity.copy(id = id).toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateDiscount(discount: Discount): Result<Unit> {
        return try {
            discountDao.updateDiscount(discount.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteDiscount(id: Long): Result<Unit> {
        return try {
            discountDao.deleteDiscount(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun applyDiscount(discountId: Long): Result<Unit> {
        return try {
            discountDao.incrementUsageCount(discountId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getActiveDiscountCount(): Flow<Int> {
        return discountDao.getActiveDiscountCount()
    }

    // Calculate best discount for a given amount
    suspend fun calculateBestDiscount(amount: Long, productIds: List<Long> = emptyList()): Discount? {
        val validDiscounts = discountDao.getValidDiscounts()

        // This is a simplified version - in real app you'd want to check all conditions
        return null // TODO: Implement best discount calculation
    }

    private fun DiscountEntity.toDomain(): Discount {
        return Discount(
            id = id,
            name = name,
            code = code,
            description = description,
            discountType = discountType,
            discountValue = discountValue,
            minPurchase = minPurchase,
            maxDiscount = maxDiscount,
            buyQuantity = buyQuantity,
            getQuantity = getQuantity,
            productId = productId,
            categoryId = categoryId,
            usageLimit = usageLimit,
            usageCount = usageCount,
            isActive = isActive,
            validFrom = validFrom,
            validUntil = validUntil,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    private fun Discount.toEntity(): DiscountEntity {
        return DiscountEntity(
            id = id,
            name = name,
            code = code,
            description = description,
            discountType = discountType,
            discountValue = discountValue,
            minPurchase = minPurchase,
            maxDiscount = maxDiscount,
            buyQuantity = buyQuantity,
            getQuantity = getQuantity,
            productId = productId,
            categoryId = categoryId,
            usageLimit = usageLimit,
            usageCount = usageCount,
            isActive = isActive,
            validFrom = validFrom,
            validUntil = validUntil,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}
