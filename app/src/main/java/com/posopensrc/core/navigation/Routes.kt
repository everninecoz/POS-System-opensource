package com.posopensrc.core.navigation

object Routes {
    const val LOGIN = "login"
    const val HOME = "home"

    // Products
    const val PRODUCTS = "products"
    const val ADD_PRODUCT = "products/add"
    const val EDIT_PRODUCT = "products/edit/{productId}"

    // POS
    const val POS = "pos"

    // Reports
    const val REPORTS = "reports"
    const val PROFIT_LOSS = "reports/profit-loss"

    // Settings
    const val SETTINGS = "settings"

    // Customers
    const val CUSTOMERS = "customers"

    // Shifts
    const val SHIFTS = "shifts"

    // Discounts
    const val DISCOUNTS = "discounts"

    // Stock Opname
    const val STOCK_OPNAME = "stock-opname"

    // Void Log
    const val VOID_LOG = "void-log"

    // User Management (Admin only)
    const val USER_MANAGEMENT = "user-management"

    // Backup
    const val BACKUP = "backup"

    // Users (Admin only)
    const val USERS = "users"
    const val ADD_USER = "users/add"
    const val EDIT_USER = "users/edit/{userId}"

    fun editProduct(productId: Long): String = "products/edit/$productId"
    fun editUser(userId: Long): String = "users/edit/$userId"
}
