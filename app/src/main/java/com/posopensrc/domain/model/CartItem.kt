package com.posopensrc.domain.model

data class CartItem(
    val product: Product,
    val quantity: Int = 1
) {
    val subtotal: Double
        get() = product.price * quantity

    val totalCost: Double
        get() = product.costPrice * quantity

    val totalProfit: Double
        get() = subtotal - totalCost
}
