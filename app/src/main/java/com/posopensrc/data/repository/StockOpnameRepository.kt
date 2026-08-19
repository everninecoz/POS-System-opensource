package com.posopensrc.data.repository

import com.posopensrc.data.local.dao.ProductDao
import com.posopensrc.data.local.dao.StockOpnameDao
import com.posopensrc.data.local.entity.StockOpnameEntity
import com.posopensrc.data.local.entity.StockOpnameItemEntity
import com.posopensrc.domain.model.StockOpname
import com.posopensrc.domain.model.StockOpnameItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StockOpnameRepository @Inject constructor(
    private val stockOpnameDao: StockOpnameDao,
    private val productDao: ProductDao
) {

    fun getAllStockOpnames(): Flow<List<StockOpname>> {
        return stockOpnameDao.getAllStockOpnames().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun getStockOpnameById(id: Long): StockOpname? {
        val opname = stockOpnameDao.getStockOpnameById(id) ?: return null
        val items = stockOpnameDao.getStockOpnameItemsSync(id)
        return opname.toDomain(items)
    }

    fun getStockOpnameItems(opnameId: Long): Flow<List<StockOpnameItem>> {
        return stockOpnameDao.getStockOpnameItems(opnameId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun createStockOpname(
        userId: Long,
        notes: String?,
        items: List<Pair<Long, Int>> // Pair(productId, physicalStock)
    ): Result<StockOpname> {
        return try {
            var totalItems = 0
            var itemsWithDifference = 0
            val opnameItems = mutableListOf<StockOpnameItemEntity>()

            for ((productId, physicalStock) in items) {
                val product = productDao.getProductById(productId) ?: continue
                val difference = physicalStock - product.stock

                opnameItems.add(
                    StockOpnameItemEntity(
                        opnameId = 0, // Will be set after insert
                        productId = productId,
                        productName = product.name,
                        systemStock = product.stock,
                        physicalStock = physicalStock,
                        difference = difference
                    )
                )

                totalItems++
                if (difference != 0) itemsWithDifference++
            }

            // Insert opname header
            val opnameEntity = StockOpnameEntity(
                userId = userId,
                notes = notes,
                totalItems = totalItems,
                itemsWithDifference = itemsWithDifference
            )
            val opnameId = stockOpnameDao.insertStockOpname(opnameEntity)

            // Insert opname items with the correct opnameId
            val itemsWithOpnameId = opnameItems.map { it.copy(opnameId = opnameId) }
            stockOpnameDao.insertStockOpnameItems(itemsWithOpnameId)

            // Update product stock based on opname
            for (item in opnameItems) {
                if (item.difference != 0) {
                    // Adjust stock to match physical count
                    val currentProduct = productDao.getProductById(item.productId)
                    if (currentProduct != null) {
                        val newStock = item.physicalStock
                        productDao.updateProduct(currentProduct.copy(stock = newStock))
                    }
                }
            }

            val resultOpname = opnameEntity.copy(id = opnameId)
            Result.success(resultOpname.toDomain(itemsWithOpnameId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getStockOpnameCount(): Flow<Int> {
        return stockOpnameDao.getStockOpnameCount()
    }

    suspend fun getLatestStockOpname(): StockOpname? {
        val opname = stockOpnameDao.getLatestStockOpname() ?: return null
        val items = stockOpnameDao.getStockOpnameItemsSync(opname.id)
        return opname.toDomain(items)
    }

    private fun StockOpnameEntity.toDomain(items: List<StockOpnameItemEntity> = emptyList()): StockOpname {
        return StockOpname(
            id = id,
            userId = userId,
            notes = notes,
            totalItems = totalItems,
            itemsWithDifference = itemsWithDifference,
            createdAt = createdAt,
            items = items.map { it.toDomain() }
        )
    }

    private fun StockOpnameItemEntity.toDomain(): StockOpnameItem {
        return StockOpnameItem(
            id = id,
            opnameId = opnameId,
            productId = productId,
            productName = productName,
            systemStock = systemStock,
            physicalStock = physicalStock,
            difference = difference,
            notes = notes,
            createdAt = createdAt
        )
    }
}
