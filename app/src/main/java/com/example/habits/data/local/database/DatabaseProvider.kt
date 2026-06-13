package com.example.habits.data.local.database

import android.content.Context
import androidx.room.Room
import com.example.habits.data.local.database.HabitFlowMigrations.MIGRATION_1_2

object DatabaseProvider {
    @Volatile
    private var instance: HabitFlowDatabase? = null

    fun get(context: Context): HabitFlowDatabase {
        return instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                HabitFlowDatabase::class.java,
                "habitflow.db",
            )
                .addMigrations(MIGRATION_1_2)
                .build().also { instance = it }
        }
    }
}
