package com.example.habits

import android.app.Application
import com.example.habits.data.local.database.DatabaseProvider
import com.example.habits.data.local.preferences.SessionDataStore
import com.example.habits.data.reminder.NotificationHelper
import com.example.habits.data.reminder.ReminderScheduler
import com.example.habits.data.repository.AuthRepository
import com.example.habits.data.repository.HabitRepository

class HabitFlowApplication : Application() {
    val sessionDataStore: SessionDataStore by lazy { SessionDataStore(this) }

    val notificationHelper: NotificationHelper by lazy { NotificationHelper(this) }

    val reminderScheduler: ReminderScheduler by lazy { ReminderScheduler(this) }

    val authRepository: AuthRepository by lazy {
        val database = DatabaseProvider.get(this)
        AuthRepository(
            userDao = database.userDao(),
            sessionStorage = sessionDataStore,
        )
    }

    val habitRepository: HabitRepository by lazy {
        val database = DatabaseProvider.get(this)
        HabitRepository(
            habitDao = database.habitDao(),
            checkInDao = database.checkInDao(),
            reminderScheduler = reminderScheduler,
        )
    }

    override fun onCreate() {
        super.onCreate()
        notificationHelper
    }
}
