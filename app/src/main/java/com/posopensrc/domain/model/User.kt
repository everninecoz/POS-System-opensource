package com.posopensrc.domain.model

data class User(
    val id: Long = 0,
    val username: String,
    val fullName: String,
    val role: String = "kasir",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val isAdmin: Boolean
        get() = role == "admin"

    val displayName: String
        get() = fullName.ifEmpty { username }
}

enum class UserRole(val value: String) {
    ADMIN("admin"),
    KASIR("kasir");

    companion object {
        fun fromString(value: String): UserRole = when (value) {
            "admin" -> ADMIN
            else -> KASIR
        }
    }
}
