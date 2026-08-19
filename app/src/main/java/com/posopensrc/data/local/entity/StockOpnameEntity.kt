package com.posopensrc.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "stock_opnames",
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
        Index(value = ["created_at"])
    ]
)
data class StockOpnameEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "user_id")
    val userId: Long,
    val notes: String? = null,
    @ColumnInfo(name = "total_items")
    val totalItems: Int = 0,
    @ColumnInfo(name = "items_with_difference")
    val itemsWithDifference: Int = 0,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "stock_opname_items",
    foreignKeys = [
        ForeignKey(
            entity = StockOpnameEntity::class,
            parentColumns = ["id"],
            childColumns = ["opname_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["product_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["opname_id"]),
        Index(value = ["product_id"])
    ]
)
data class StockOpnameItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "opname_id")
    val opnameId: Long,
    @ColumnInfo(name = "product_id")
    val productId: Long,
    @ColumnInfo(name = "product_name")
    val productName: String,
    @ColumnInfo(name = "system_stock")
    val systemStock: Int,
    @ColumnInfo(name = "physical_stock")
    val physicalStock: Int,
    val difference: Int,
    val notes: String? = null,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
