package com.example.habits.data.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.habits.domain.model.Frequency
import com.example.habits.domain.model.Habit
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

class ReminderScheduler(
    private val context: Context,
) : ReminderSchedulerContract {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun schedule(habit: Habit) {
        cancel(habit.id)
        if (!habit.reminderEnabled) return

        val hour = habit.reminderHour ?: return
        val minute = habit.reminderMinute ?: return

        val triggerAtMillis = nextTriggerMillis(habit.frequency, hour, minute)
        val pendingIntent = createPendingIntent(habit.id)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent,
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent,
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent,
            )
        }
    }

    override fun cancel(habitId: Long) {
        alarmManager.cancel(createPendingIntent(habitId))
    }

    override fun syncAll(habits: List<Habit>) {
        habits.forEach { habit ->
            if (habit.reminderEnabled) {
                schedule(habit)
            } else {
                cancel(habit.id)
            }
        }
    }

    fun rescheduleAfterTrigger(habitId: Long, frequency: Frequency, hour: Int, minute: Int) {
        val triggerAtMillis = nextTriggerMillis(frequency, hour, minute, skipCurrent = true)
        val pendingIntent = createPendingIntent(habitId)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent,
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent,
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent,
            )
        }
    }

    private fun createPendingIntent(habitId: Long): PendingIntent {
        val intent = Intent(context, HabitReminderReceiver::class.java).apply {
            putExtra(HabitReminderReceiver.EXTRA_HABIT_ID, habitId)
        }
        return PendingIntent.getBroadcast(
            context,
            habitId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    companion object {
        fun nextTriggerMillis(
            frequency: Frequency,
            hour: Int,
            minute: Int,
            referenceTimeMillis: Long = System.currentTimeMillis(),
            skipCurrent: Boolean = false,
            zone: ZoneId = ZoneId.systemDefault(),
        ): Long {
            val referenceDateTime = Instant.ofEpochMilli(referenceTimeMillis).atZone(zone).toLocalDateTime()
            val targetDate = when (frequency) {
                Frequency.DAILY -> {
                    var date = referenceDateTime.toLocalDate()
                    var candidate = LocalDateTime.of(date, java.time.LocalTime.of(hour, minute))
                    if (skipCurrent || !candidate.isAfter(referenceDateTime)) {
                        date = date.plusDays(1)
                        candidate = LocalDateTime.of(date, java.time.LocalTime.of(hour, minute))
                    }
                    candidate
                }
                Frequency.WEEKLY -> {
                    var date = referenceDateTime.toLocalDate()
                        .with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY))
                    var candidate = LocalDateTime.of(date, java.time.LocalTime.of(hour, minute))
                    if (skipCurrent || !candidate.isAfter(referenceDateTime)) {
                        date = date.plusWeeks(1)
                        candidate = LocalDateTime.of(date, java.time.LocalTime.of(hour, minute))
                    }
                    candidate
                }
            }
            return targetDate.atZone(zone).toInstant().toEpochMilli()
        }
    }
}
