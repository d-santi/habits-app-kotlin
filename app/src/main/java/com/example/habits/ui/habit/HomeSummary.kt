package com.example.habits.ui.habit

data class HomeSummary(
    val completedCount: Int = 0,
    val totalCount: Int = 0,
    val bestStreak: Int = 0,
    val hasWeeklyHabits: Boolean = false,
)
