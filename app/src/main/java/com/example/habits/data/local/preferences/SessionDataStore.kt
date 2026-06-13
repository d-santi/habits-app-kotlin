package com.example.habits.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "session_preferences",
)

class SessionDataStore(private val context: Context) : SessionStorage {
    private val loggedInUserIdKey = longPreferencesKey("logged_in_user_id")

    override val loggedInUserId: Flow<Long?> = context.sessionDataStore.data.map { preferences ->
        preferences[loggedInUserIdKey]
    }

    override suspend fun setLoggedInUserId(userId: Long) {
        context.sessionDataStore.edit { preferences ->
            preferences[loggedInUserIdKey] = userId
        }
    }

    override suspend fun clearSession() {
        context.sessionDataStore.edit { preferences ->
            preferences.remove(loggedInUserIdKey)
        }
    }
}
