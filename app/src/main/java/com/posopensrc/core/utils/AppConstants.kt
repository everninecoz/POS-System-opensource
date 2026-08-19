package com.posopensrc.core.utils

object AppConstants {
    const val DATABASE_NAME = "pos_database"
    const val DEFAULT_TAX_PERCENTAGE = 10.0
    const val CURRENCY = "Rp"
    const val INVOICE_PREFIX = "INV"

    // User Roles
    const val ROLE_ADMIN = "admin"
    const val ROLE_KASIR = "kasir"

    // Payment Methods
    const val PAYMENT_CASH = "cash"
    const val PAYMENT_QRIS = "qris"
    const val PAYMENT_TRANSFER = "transfer"

    // Default Admin Credentials (will be hashed on first run)
    const val DEFAULT_ADMIN_USERNAME = "admin"
    const val DEFAULT_ADMIN_PIN = "123456"
    const val DEFAULT_ADMIN_PASSWORD = "admin123"
    const val DEFAULT_ADMIN_NAME = "Administrator"
}
