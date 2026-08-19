package com.posopensrc.domain.model

data class Product(
    val id: Long = 0,
    val name: String,
    val price: Double,
    val costPrice: Double = 0.0,
    val stock: Int = 0,
    val minStock: Int = 5,
    val category: String? = null,
    val barcode: String? = null,
    val imagePath: String? = null,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val isLowStock: Boolean
        get() = stock <= minStock

    val profit: Double
        get() = price - costPrice

    val profitPercentage: Double
        get() = if (costPrice > 0) ((price - costPrice) / costPrice) * 100 else 0.0
}
