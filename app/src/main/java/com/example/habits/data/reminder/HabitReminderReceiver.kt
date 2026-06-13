package com.example.habits.data.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.habits.data.local.database.DatabaseProvider
import com.example.habits.data.mapper.toDomain
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HabitReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val habitId = intent.getLongExtra(EXTRA_HABIT_ID, -1L)
        if (habitId <= 0) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = DatabaseProvider.get(context)
                val habitEntity = database.habitDao().getByHabitId(habitId) ?: return@launch
                val habit = habitEntity.toDomain()
                if (!habit.reminderEnabled) return@launch

                val hour = habit.reminderHour ?: return@launch
                val minute = habit.reminderMinute ?: return@launch

                NotificationHelper(context).showHabitReminder(habit.id, habit.name)

                ReminderScheduler(context).rescheduleAfterTrigger(
                    habitId = habit.id,
                    frequency = habit.frequency,
                    hour = hour,
                    minute = minute,
                )
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val EXTRA_HABIT_ID = "extra_habit_id"
    }
}
