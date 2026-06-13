package com.example.habits.data.util

import java.security.MessageDigest
import java.security.SecureRandom

object PasswordHasher {
    private const val SALT_LENGTH = 16

    fun generateSalt(): String {
        val salt = ByteArray(SALT_LENGTH)
        SecureRandom().nextBytes(salt)
        return salt.toHex()
    }

    fun hash(password: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest("$salt$password".toByteArray(Charsets.UTF_8))
        return hashBytes.toHex()
    }

    fun verify(password: String, salt: String, expectedHash: String): Boolean {
        return hash(password, salt) == expectedHash
    }

    private fun ByteArray.toHex(): String {
        return joinToString(separator = "") { byte ->
            "%02x".format(byte)
        }
    }
}
