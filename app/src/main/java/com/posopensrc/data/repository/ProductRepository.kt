package com.posopensrc.data.repository

import com.posopensrc.data.local.dao.CategoryDao
import com.posopensrc.data.local.dao.ProductDao
import com.posopensrc.data.local.entity.CategoryEntity
import com.posopensrc.data.local.entity.ProductEntity
import com.posopensrc.domain.model.Category
import com.posopensrc.domain.model.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepository @Inject constructor(
    private val productDao: ProductDao,
    private val categoryDao: CategoryDao
) {

    fun getAllProducts(): Flow<List<Product>> {
        return productDao.getAllProducts().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getProductsByCategory(category: String): Flow<List<Product>> {
        return productDao.getProductsByCategory(category).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun searchProducts(query: String): Flow<List<Product>> {
        return productDao.searchProducts(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun getProductById(id: Long): Product? {
        return productDao.getProductById(id)?.toDomain()
    }

    suspend fun getProductByBarcode(barcode: String): Product? {
        return productDao.getProductByBarcode(barcode)?.toDomain()
    }

    fun getLowStockProducts(): Flow<List<Product>> {
        return productDao.getLowStockProducts().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getProductCount(): Flow<Int> {
        return productDao.getProductCount()
    }

    fun getLowStockCount(): Flow<Int> {
        return productDao.getLowStockCount()
    }

    suspend fun createProduct(
        name: String,
        price: Double,
        costPrice: Double,
        stock: Int,
        minStock: Int,
        category: String?,
        barcode: String?,
        imagePath: String?
    ): Result<Product> {
        return try {
            val now = System.currentTimeMillis()
            val entity = ProductEntity(
                name = name,
                price = price,
                costPrice = costPrice,
                stock = stock,
                minStock = minStock,
                category = category,
                barcode = barcode,
                imagePath = imagePath,
                createdAt = now,
                updatedAt = now
            )
            val id = productDao.insertProduct(entity)
            Result.success(entity.copy(id = id).toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProduct(
        id: Long,
        name: String,
        price: Double,
        costPrice: Double,
        stock: Int,
        minStock: Int,
        category: String?,
        barcode: String?,
        imagePath: String?
    ): Result<Unit> {
        return try {
            val existing = productDao.getProductById(id) ?: return Result.failure(Exception("Produk tidak ditemukan"))
            productDao.updateProduct(
                existing.copy(
                    name = name,
                    price = price,
                    costPrice = costPrice,
                    stock = stock,
                    minStock = minStock,
                    category = category,
                    barcode = barcode,
                    imagePath = imagePath,
                    updatedAt = System.currentTimeMillis()
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteProduct(id: Long): Result<Unit> {
        return try {
            productDao.deleteProduct(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun reduceStock(id: Long, quantity: Int): Result<Unit> {
        return try {
            productDao.reduceStock(id, quantity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addStock(id: Long, quantity: Int): Result<Unit> {
        return try {
            productDao.addStock(id, quantity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Categories
    fun getAllCategories(): Flow<List<Category>> {
        return categoryDao.getAllCategories().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun createCategory(name: String, icon: String?): Result<Category> {
        return try {
            val entity = CategoryEntity(name = name, icon = icon)
            val id = categoryDao.insertCategory(entity)
            Result.success(entity.copy(id = id).toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCategory(id: Long): Result<Unit> {
        return try {
            categoryDao.deleteCategory(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun ProductEntity.toDomain(): Product {
        return Product(
            id = id,
            name = name,
            price = price,
            costPrice = costPrice,
            stock = stock,
            minStock = minStock,
            category = category,
            barcode = barcode,
            imagePath = imagePath,
            isActive = isActive,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    private fun CategoryEntity.toDomain(): Category {
        return Category(
            id = id,
            name = name,
            icon = icon,
            sortOrder = sortOrder
        )
    }
}
