package com.example.habits.data.mapper

import com.example.habits.data.local.entity.CheckInEntity
import com.example.habits.data.local.entity.HabitEntity
import com.example.habits.domain.model.CheckIn
import com.example.habits.domain.model.Frequency
import com.example.habits.domain.model.Habit

fun HabitEntity.toDomain(): Habit {
    return Habit(
        id = id,
        userId = userId,
        name = name,
        description = description,
        frequency = Frequency.valueOf(frequency),
        createdAt = createdAt,
        reminderEnabled = reminderEnabled,
        reminderHour = reminderHour,
        reminderMinute = reminderMinute,
    )
}

fun CheckInEntity.toDomain(): CheckIn {
    return CheckIn(
        id = id,
        habitId = habitId,
        completedAt = completedAt,
    )
}

fun Frequency.toEntityValue(): String = name
