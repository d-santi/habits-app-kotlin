package com.example.habits.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object HabitFlowMigrations {
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE habits ADD COLUMN reminderEnabled INTEGER NOT NULL DEFAULT 0",
            )
            db.execSQL("ALTER TABLE habits ADD COLUMN reminderHour INTEGER")
            db.execSQL("ALTER TABLE habits ADD COLUMN reminderMinute INTEGER")
        }
    }
}
