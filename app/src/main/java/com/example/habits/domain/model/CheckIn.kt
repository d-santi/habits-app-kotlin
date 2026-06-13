package com.example.habits.domain.model

data class CheckIn(
    val id: Long,
    val habitId: Long,
    val completedAt: Long,
)
