package com.posopensrc.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Transaction(
    val id: Long = 0,
    val invoiceNumber: String,
    val userId: Long,
    val customerName: String? = null,
    val customerId: Long? = null,
    val shiftId: Long? = null,
    val items: List<TransactionItem>,
    val subtotal: Double,
    val taxPercentage: Double = 0.0,
    val taxAmount: Double = 0.0,
    val discount: Double = 0.0,
    val total: Double,
    val paymentMethod: String,
    val amountPaid: Double,
    val changeAmount: Double = 0.0,
    val notes: String? = null,
    val isVoided: Boolean = false,
    val voidReason: String? = null,
    val voidedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
data class TransactionItem(
    val productId: Long,
    val productName: String,
    val quantity: Int,
    val price: Double,
    val subtotal: Double
)

enum class PaymentMethod(val value: String, val displayName: String) {
    CASH("cash", "Tunai"),
    QRIS("qris", "QRIS"),
    TRANSFER("transfer", "Transfer");

    companion object {
        fun fromString(value: String): PaymentMethod = when (value) {
            "qris" -> QRIS
            "transfer" -> TRANSFER
            else -> CASH
        }
    }
}
