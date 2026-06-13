package com.example.habits.data.local.preferences

import kotlinx.coroutines.flow.Flow

interface SessionStorage {
    val loggedInUserId: Flow<Long?>
    suspend fun setLoggedInUserId(userId: Long)
    suspend fun clearSession()
}
