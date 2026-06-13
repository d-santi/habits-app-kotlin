package com.example.habits.data.repository

import com.example.habits.data.local.dao.UserDao
import com.example.habits.data.local.entity.UserEntity
import com.example.habits.data.local.preferences.SessionStorage
import com.example.habits.data.util.PasswordHasher
import kotlinx.coroutines.flow.Flow

sealed class AuthResult {
    data object Success : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class AuthRepository(
    private val userDao: UserDao,
    private val sessionStorage: SessionStorage,
) {
    val loggedInUserId: Flow<Long?> = sessionStorage.loggedInUserId

    suspend fun register(username: String, password: String): AuthResult {
        val trimmedUsername = username.trim()
        if (trimmedUsername.isEmpty()) {
            return AuthResult.Error("El nombre de usuario no puede estar vacío")
        }
        if (password.length < 4) {
            return AuthResult.Error("La contraseña debe tener al menos 4 caracteres")
        }

        val existingUser = userDao.getByUsername(trimmedUsername)
        if (existingUser != null) {
            return AuthResult.Error("Este nombre de usuario ya está registrado")
        }

        val salt = PasswordHasher.generateSalt()
        val hash = PasswordHasher.hash(password, salt)
        val userId = userDao.insert(
            UserEntity(
                username = trimmedUsername,
                passwordHash = hash,
                passwordSalt = salt,
            ),
        )
        sessionStorage.setLoggedInUserId(userId)
        return AuthResult.Success
    }

    suspend fun login(username: String, password: String): AuthResult {
        val trimmedUsername = username.trim()
        if (trimmedUsername.isEmpty() || password.isEmpty()) {
            return AuthResult.Error("Usuario y contraseña son obligatorios")
        }

        val user = userDao.getByUsername(trimmedUsername)
            ?: return AuthResult.Error("Usuario o contraseña incorrectos")

        val isValid = PasswordHasher.verify(password, user.passwordSalt, user.passwordHash)
        if (!isValid) {
            return AuthResult.Error("Usuario o contraseña incorrectos")
        }

        sessionStorage.setLoggedInUserId(user.id)
        return AuthResult.Success
    }

    suspend fun logout() {
        sessionStorage.clearSession()
    }
}
