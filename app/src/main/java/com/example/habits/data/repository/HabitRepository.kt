package com.example.habits.data.repository

import com.example.habits.data.local.dao.CheckInDao
import com.example.habits.data.local.dao.HabitDao
import com.example.habits.data.local.entity.CheckInEntity
import com.example.habits.data.local.entity.HabitEntity
import com.example.habits.data.mapper.toDomain
import com.example.habits.data.mapper.toEntityValue
import com.example.habits.data.reminder.ReminderSchedulerContract
import com.example.habits.data.util.PeriodUtils
import com.example.habits.data.util.StreakCalculator
import com.example.habits.domain.model.CheckIn
import com.example.habits.domain.model.Frequency
import com.example.habits.domain.model.Habit
import com.example.habits.domain.model.HabitWithCompletion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

sealed class HabitResult {
    data object Success : HabitResult()
    data class Error(val message: String) : HabitResult()
}

class HabitRepository(
    private val habitDao: HabitDao,
    private val checkInDao: CheckInDao,
    private val reminderScheduler: ReminderSchedulerContract,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeHabitsWithCompletion(userId: Long): Flow<List<HabitWithCompletion>> {
        return habitDao.observeByUserId(userId).flatMapLatest { habits ->
            if (habits.isEmpty()) {
                flowOf(emptyList())
            } else {
                val completionFlows = habits.map { habit ->
                    val domainHabit = habit.toDomain()
                    val period = PeriodUtils.getPeriodRange(domainHabit.frequency)
                    checkInDao.observeByHabitId(habit.id).combine(flowOf(domainHabit)) { checkIns, mappedHabit ->
                        val domainCheckIns = checkIns.map { it.toDomain() }
                        val isCompleted = domainCheckIns.any { checkIn ->
                            checkIn.completedAt >= period.startInclusive &&
                                checkIn.completedAt < period.endExclusive
                        }
                        val streak = StreakCalculator.calculate(mappedHabit.frequency, domainCheckIns)
                        HabitWithCompletion(
                            habit = mappedHabit,
                            isCompletedForPeriod = isCompleted,
                            currentStreak = streak,
                        )
                    }
                }
                combine(completionFlows) { it.toList() }
            }
        }
    }

    suspend fun createHabit(
        userId: Long,
        name: String,
        description: String,
        frequency: Frequency,
        reminderEnabled: Boolean,
        reminderHour: Int?,
        reminderMinute: Int?,
    ): HabitResult {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) {
            return HabitResult.Error("El nombre del hábito no puede estar vacío")
        }
        if (reminderEnabled && (reminderHour == null || reminderMinute == null)) {
            return HabitResult.Error("Selecciona una hora para el recordatorio")
        }

        return withContext(Dispatchers.IO) {
            val habitId = habitDao.insert(
                HabitEntity(
                    userId = userId,
                    name = trimmedName,
                    description = description.trim(),
                    frequency = frequency.toEntityValue(),
                    createdAt = System.currentTimeMillis(),
                    reminderEnabled = reminderEnabled,
                    reminderHour = reminderHour,
                    reminderMinute = reminderMinute,
                ),
            )
            habitDao.getByHabitId(habitId)?.toDomain()?.let { reminderScheduler.schedule(it) }
            HabitResult.Success
        }
    }

    suspend fun updateHabit(
        habitId: Long,
        userId: Long,
        name: String,
        description: String,
        frequency: Frequency,
        reminderEnabled: Boolean,
        reminderHour: Int?,
        reminderMinute: Int?,
    ): HabitResult {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) {
            return HabitResult.Error("El nombre del hábito no puede estar vacío")
        }
        if (reminderEnabled && (reminderHour == null || reminderMinute == null)) {
            return HabitResult.Error("Selecciona una hora para el recordatorio")
        }

        return withContext(Dispatchers.IO) {
            val existing = habitDao.getById(habitId, userId)
                ?: return@withContext HabitResult.Error("Hábito no encontrado")

            val updated = existing.copy(
                name = trimmedName,
                description = description.trim(),
                frequency = frequency.toEntityValue(),
                reminderEnabled = reminderEnabled,
                reminderHour = if (reminderEnabled) reminderHour else null,
                reminderMinute = if (reminderEnabled) reminderMinute else null,
            )
            habitDao.update(updated)
            reminderScheduler.schedule(updated.toDomain())
            HabitResult.Success
        }
    }

    suspend fun checkInHabit(habit: Habit): HabitResult {
        if (habit.userId <= 0) {
            return HabitResult.Error("Sesión no válida")
        }

        return withContext(Dispatchers.IO) {
            val period = PeriodUtils.getPeriodRange(habit.frequency)
            val existingCount = checkInDao.countInPeriod(
                habitId = habit.id,
                periodStart = period.startInclusive,
                periodEnd = period.endExclusive,
            )
            if (existingCount > 0) {
                return@withContext HabitResult.Success
            }

            checkInDao.insert(
                CheckInEntity(
                    habitId = habit.id,
                    completedAt = System.currentTimeMillis(),
                ),
            )
            HabitResult.Success
        }
    }

    suspend fun getHabit(habitId: Long, userId: Long): Habit? {
        return withContext(Dispatchers.IO) {
            habitDao.getById(habitId, userId)?.toDomain()
        }
    }

    fun observeCheckIns(habitId: Long): Flow<List<CheckIn>> {
        return checkInDao.observeByHabitId(habitId).map { checkIns ->
            checkIns.map { it.toDomain() }
        }
    }

    suspend fun deleteHabit(habitId: Long, userId: Long): HabitResult {
        return withContext(Dispatchers.IO) {
            val habit = habitDao.getById(habitId, userId)
                ?: return@withContext HabitResult.Error("Hábito no encontrado")
            reminderScheduler.cancel(habit.id)
            habitDao.delete(habit.id, userId)
            HabitResult.Success
        }
    }

    suspend fun syncAllReminders(userId: Long) {
        withContext(Dispatchers.IO) {
            val habits = habitDao.getAllByUserId(userId).map { it.toDomain() }
            reminderScheduler.syncAll(habits)
        }
    }
}
