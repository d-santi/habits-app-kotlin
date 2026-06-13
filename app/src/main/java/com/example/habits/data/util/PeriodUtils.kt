package com.example.habits.data.util

import com.example.habits.domain.model.Frequency
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

data class PeriodRange(
    val startInclusive: Long,
    val endExclusive: Long,
)

object PeriodUtils {
    fun getPeriodRange(frequency: Frequency, referenceTimeMillis: Long = System.currentTimeMillis()): PeriodRange {
        val zone = ZoneId.systemDefault()
        val date = Instant.ofEpochMilli(referenceTimeMillis).atZone(zone).toLocalDate()

        return when (frequency) {
            Frequency.DAILY -> {
                val start = date.atStartOfDay(zone).toInstant().toEpochMilli()
                val end = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
                PeriodRange(start, end)
            }
            Frequency.WEEKLY -> {
                val weekStart = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                val weekEnd = weekStart.plusWeeks(1)
                val start = weekStart.atStartOfDay(zone).toInstant().toEpochMilli()
                val end = weekEnd.atStartOfDay(zone).toInstant().toEpochMilli()
                PeriodRange(start, end)
            }
        }
    }
}

object DateFormatter {
    private val formatter = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", Locale.getDefault())

    fun format(timestampMillis: Long): String {
        val zone = ZoneId.systemDefault()
        val dateTime = Instant.ofEpochMilli(timestampMillis).atZone(zone)
        return formatter.format(dateTime)
    }
}
