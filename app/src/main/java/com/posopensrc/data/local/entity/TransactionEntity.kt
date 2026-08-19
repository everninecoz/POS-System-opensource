package com.posopensrc.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["invoice_number"], unique = true),
        Index(value = ["user_id"]),
        Index(value = ["created_at"])
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "invoice_number")
    val invoiceNumber: String,
    @ColumnInfo(name = "user_id")
    val userId: Long,
    @ColumnInfo(name = "customer_name")
    val customerName: String? = null,
    val items: String, // JSON string
    val subtotal: Double,
    @ColumnInfo(name = "tax_percentage")
    val taxPercentage: Double = 0.0,
    @ColumnInfo(name = "tax_amount")
    val taxAmount: Double = 0.0,
    val discount: Double = 0.0,
    val total: Double,
    @ColumnInfo(name = "payment_method")
    val paymentMethod: String,
    @ColumnInfo(name = "amount_paid")
    val amountPaid: Double,
    @ColumnInfo(name = "change_amount")
    val changeAmount: Double = 0.0,
    val notes: String? = null,
    @ColumnInfo(name = "is_voided")
    val isVoided: Boolean = false,
    @ColumnInfo(name = "void_reason")
    val voidReason: String? = null,
    @ColumnInfo(name = "voided_at")
    val voidedAt: Long? = null,
    @ColumnInfo(name = "customer_id")
    val customerId: Long? = null,
    @ColumnInfo(name = "shift_id")
    val shiftId: Long? = null,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
