package com.posopensrc.core.security

import org.mindrot.jbcrypt.BCrypt

object PasswordUtils {

    fun hashPassword(password: String): String {
        return BCrypt.hashpw(password, BCrypt.gensalt(10))
    }

    fun verifyPassword(password: String, hashed: String): Boolean {
        return try {
            BCrypt.checkpw(password, hashed)
        } catch (e: Exception) {
            false
        }
    }

    fun hashPin(pin: String): String {
        return BCrypt.hashpw(pin, BCrypt.gensalt(10))
    }

    fun verifyPin(pin: String, hashed: String): Boolean {
        return try {
            BCrypt.checkpw(pin, hashed)
        } catch (e: Exception) {
            false
        }
    }
}
