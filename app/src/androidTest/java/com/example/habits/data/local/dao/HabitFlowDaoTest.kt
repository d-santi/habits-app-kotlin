package com.example.habits.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.habits.data.local.database.HabitFlowDatabase
import com.example.habits.data.local.entity.CheckInEntity
import com.example.habits.data.local.entity.HabitEntity
import com.example.habits.data.local.entity.UserEntity
import com.example.habits.domain.model.Frequency
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HabitFlowDaoTest {

    private lateinit var database: HabitFlowDatabase
    private lateinit var userDao: UserDao
    private lateinit var habitDao: HabitDao
    private lateinit var checkInDao: CheckInDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, HabitFlowDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        userDao = database.userDao()
        habitDao = database.habitDao()
        checkInDao = database.checkInDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun updateHabit_persistsReminderFields() = runBlocking {
        val userId = userDao.insert(
            UserEntity(username = "user1", passwordHash = "hash", passwordSalt = "salt"),
        )
        val habitId = habitDao.insert(
            HabitEntity(
                userId = userId,
                name = "Leer",
                description = "",
                frequency = Frequency.DAILY.name,
                createdAt = System.currentTimeMillis(),
            ),
        )

        habitDao.update(
            habitDao.getById(habitId, userId)!!.copy(
                reminderEnabled = true,
                reminderHour = 9,
                reminderMinute = 0,
            ),
        )

        val updated = habitDao.getById(habitId, userId)
        assertEquals(true, updated?.reminderEnabled)
        assertEquals(9, updated?.reminderHour)
        assertEquals(0, updated?.reminderMinute)
    }

    @Test
    fun deleteHabit_cascadesCheckIns() = runBlocking {
        val userId = userDao.insert(
            UserEntity(username = "user1", passwordHash = "hash", passwordSalt = "salt"),
        )
        val habitId = habitDao.insert(
            HabitEntity(
                userId = userId,
                name = "Leer",
                description = "",
                frequency = Frequency.DAILY.name,
                createdAt = System.currentTimeMillis(),
            ),
        )
        checkInDao.insert(CheckInEntity(habitId = habitId, completedAt = System.currentTimeMillis()))

        habitDao.delete(habitId, userId)

        assertNull(habitDao.getById(habitId, userId))
        assertEquals(0, checkInDao.countInPeriod(habitId, 0, Long.MAX_VALUE))
    }
}
