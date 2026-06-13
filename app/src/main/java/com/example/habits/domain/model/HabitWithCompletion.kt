package com.example.habits.domain.model

data class HabitWithCompletion(
    val habit: Habit,
    val isCompletedForPeriod: Boolean,
    val currentStreak: Int = 0,
)
