package com.posopensrc.domain.model

data class Settings(
    val storeName: String = "Warung Saya",
    val storeAddress: String? = null,
    val storePhone: String? = null,
    val storeLogo: String? = null,
    val taxPercentage: Double = 10.0,
    val receiptFooter: String = "Terima kasih atas kunjungan Anda!",
    val currency: String = "Rp",
    val language: String = "id"
) {
    val hasStoreInfo: Boolean
        get() = storeName.isNotBlank() && storeName != "Warung Saya"
}
