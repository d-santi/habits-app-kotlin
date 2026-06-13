package com.example.habits.data.reminder

import com.example.habits.domain.model.Habit

interface ReminderSchedulerContract {
    fun schedule(habit: Habit)
    fun cancel(habitId: Long)
    fun syncAll(habits: List<Habit>)
}
