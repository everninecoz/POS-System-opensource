package com.posopensrc.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "shifts",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["user_id"]),
        Index(value = ["is_open"]),
        Index(value = ["opened_at"])
    ]
)
data class ShiftEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "user_id")
    val userId: Long,
    @ColumnInfo(name = "opening_balance")
    val openingBalance: Double,
    @ColumnInfo(name = "closing_balance")
    val closingBalance: Double? = null,
    @ColumnInfo(name = "actual_balance")
    val actualBalance: Double? = null,
    @ColumnInfo(name = "total_sales")
    val totalSales: Double = 0.0,
    @ColumnInfo(name = "total_transactions")
    val totalTransactions: Int = 0,
    @ColumnInfo(name = "is_open")
    val isOpen: Boolean = true,
    @ColumnInfo(name = "opened_at")
    val openedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "closed_at")
    val closedAt: Long? = null,
    val notes: String? = null
)
