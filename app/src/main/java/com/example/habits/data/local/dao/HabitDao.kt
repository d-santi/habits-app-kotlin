package com.example.habits.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.habits.data.local.entity.HabitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Insert
    suspend fun insert(habit: HabitEntity): Long

    @Update
    suspend fun update(habit: HabitEntity)

    @Query("SELECT * FROM habits WHERE userId = :userId ORDER BY createdAt DESC")
    fun observeByUserId(userId: Long): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE id = :habitId AND userId = :userId LIMIT 1")
    suspend fun getById(habitId: Long, userId: Long): HabitEntity?

    @Query("SELECT * FROM habits WHERE userId = :userId")
    suspend fun getAllByUserId(userId: Long): List<HabitEntity>

    @Query("SELECT * FROM habits WHERE id = :habitId LIMIT 1")
    suspend fun getByHabitId(habitId: Long): HabitEntity?

    @Query("SELECT * FROM habits WHERE reminderEnabled = 1")
    suspend fun getAllWithRemindersEnabled(): List<HabitEntity>

    @Query("DELETE FROM habits WHERE id = :habitId AND userId = :userId")
    suspend fun delete(habitId: Long, userId: Long)
}
