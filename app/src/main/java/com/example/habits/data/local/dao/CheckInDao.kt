package com.example.habits.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.habits.data.local.entity.CheckInEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CheckInDao {
    @Insert
    suspend fun insert(checkIn: CheckInEntity): Long

    @Query(
        """
        SELECT COUNT(*) FROM check_ins
        WHERE habitId = :habitId
        AND completedAt >= :periodStart
        AND completedAt < :periodEnd
        """,
    )
    suspend fun countInPeriod(habitId: Long, periodStart: Long, periodEnd: Long): Int

    @Query(
        """
        SELECT * FROM check_ins
        WHERE habitId = :habitId
        ORDER BY completedAt DESC
        """,
    )
    fun observeByHabitId(habitId: Long): Flow<List<CheckInEntity>>
}
