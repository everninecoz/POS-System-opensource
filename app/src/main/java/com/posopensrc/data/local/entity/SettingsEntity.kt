package com.posopensrc.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey
    val id: Long = 1,
    @ColumnInfo(name = "store_name")
    val storeName: String = "Warung Saya",
    @ColumnInfo(name = "store_address")
    val storeAddress: String? = null,
    @ColumnInfo(name = "store_phone")
    val storePhone: String? = null,
    @ColumnInfo(name = "store_logo")
    val storeLogo: String? = null,
    @ColumnInfo(name = "tax_percentage")
    val taxPercentage: Double = 0.0,
    @ColumnInfo(name = "receipt_footer")
    val receiptFooter: String = "Terima kasih atas kunjungan Anda!",
    val currency: String = "Rp",
    val language: String = "id",
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
