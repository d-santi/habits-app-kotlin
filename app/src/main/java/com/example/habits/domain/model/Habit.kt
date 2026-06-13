package com.example.habits.domain.model

data class Habit(
    val id: Long,
    val userId: Long,
    val name: String,
    val description: String,
    val frequency: Frequency,
    val createdAt: Long,
    val reminderEnabled: Boolean = false,
    val reminderHour: Int? = null,
    val reminderMinute: Int? = null,
)
