package com.posopensrc.data.repository

import com.posopensrc.data.local.dao.CustomerDao
import com.posopensrc.data.local.entity.CustomerEntity
import com.posopensrc.domain.model.Customer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomerRepository @Inject constructor(
    private val customerDao: CustomerDao
) {

    fun getAllCustomers(): Flow<List<Customer>> {
        return customerDao.getAllCustomers().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun searchCustomers(query: String): Flow<List<Customer>> {
        return customerDao.searchCustomers(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun getCustomerById(id: Long): Customer? {
        return customerDao.getCustomerById(id)?.toDomain()
    }

    suspend fun getCustomerByPhone(phone: String): Customer? {
        return customerDao.getCustomerByPhone(phone)?.toDomain()
    }

    fun getCustomerCount(): Flow<Int> {
        return customerDao.getCustomerCount()
    }

    suspend fun createCustomer(
        name: String,
        phone: String?,
        email: String?,
        address: String?,
        notes: String?
    ): Result<Customer> {
        return try {
            val entity = CustomerEntity(
                name = name,
                phone = phone,
                email = email,
                address = address,
                notes = notes
            )
            val id = customerDao.insertCustomer(entity)
            Result.success(entity.copy(id = id).toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateCustomer(customer: Customer): Result<Unit> {
        return try {
            customerDao.updateCustomer(customer.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCustomer(id: Long): Result<Unit> {
        return try {
            customerDao.deleteCustomer(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateCustomerPurchase(customerId: Long, amount: Double): Result<Unit> {
        return try {
            val points = (amount / 1000).toInt() // 1 point per 1000 spent
            customerDao.updateCustomerPurchase(customerId, amount, points)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getTopCustomers(limit: Int = 10): Flow<List<Customer>> {
        return customerDao.getTopCustomers(limit).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    private fun CustomerEntity.toDomain(): Customer {
        return Customer(
            id = id,
            name = name,
            phone = phone,
            email = email,
            address = address,
            totalPurchases = totalPurchases,
            purchaseCount = purchaseCount,
            loyaltyPoints = loyaltyPoints,
            notes = notes,
            isActive = isActive,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    private fun Customer.toEntity(): CustomerEntity {
        return CustomerEntity(
            id = id,
            name = name,
            phone = phone,
            email = email,
            address = address,
            totalPurchases = totalPurchases,
            purchaseCount = purchaseCount,
            loyaltyPoints = loyaltyPoints,
            notes = notes,
            isActive = isActive,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}
