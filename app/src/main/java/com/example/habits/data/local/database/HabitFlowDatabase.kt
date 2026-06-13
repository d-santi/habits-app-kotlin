package com.example.habits.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.habits.data.local.dao.CheckInDao
import com.example.habits.data.local.dao.HabitDao
import com.example.habits.data.local.dao.UserDao
import com.example.habits.data.local.entity.CheckInEntity
import com.example.habits.data.local.entity.HabitEntity
import com.example.habits.data.local.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        HabitEntity::class,
        CheckInEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
abstract class HabitFlowDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun habitDao(): HabitDao
    abstract fun checkInDao(): CheckInDao
}
