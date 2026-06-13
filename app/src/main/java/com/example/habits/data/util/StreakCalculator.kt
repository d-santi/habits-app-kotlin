package com.example.habits.data.util

import com.example.habits.domain.model.CheckIn
import com.example.habits.domain.model.Frequency
import java.time.Instant
import java.time.ZoneId

object StreakCalculator {
    fun calculate(
        frequency: Frequency,
        checkIns: List<CheckIn>,
        referenceTimeMillis: Long = System.currentTimeMillis(),
        zone: ZoneId = ZoneId.systemDefault(),
    ): Int {
        if (checkIns.isEmpty()) return 0

        val sortedCheckIns = checkIns.sortedByDescending { it.completedAt }
        var streak = 0
        var cursorMillis = referenceTimeMillis

        val currentPeriod = PeriodUtils.getPeriodRange(frequency, cursorMillis)
        val hasCurrentPeriodCheckIn = sortedCheckIns.any { checkIn ->
            checkIn.completedAt >= currentPeriod.startInclusive &&
                checkIn.completedAt < currentPeriod.endExclusive
        }

        if (!hasCurrentPeriodCheckIn) {
            cursorMillis = when (frequency) {
                Frequency.DAILY -> {
                    Instant.ofEpochMilli(cursorMillis).atZone(zone).toLocalDate()
                        .minusDays(1)
                        .atStartOfDay(zone)
                        .toInstant()
                        .toEpochMilli()
                }
                Frequency.WEEKLY -> {
                    Instant.ofEpochMilli(currentPeriod.startInclusive).atZone(zone).toLocalDate()
                        .minusWeeks(1)
                        .atStartOfDay(zone)
                        .toInstant()
                        .toEpochMilli()
                }
            }
        }

        while (true) {
            val period = PeriodUtils.getPeriodRange(frequency, cursorMillis)
            val hasCheckIn = sortedCheckIns.any { checkIn ->
                checkIn.completedAt >= period.startInclusive &&
                    checkIn.completedAt < period.endExclusive
            }
            if (!hasCheckIn) break
            streak++
            cursorMillis = when (frequency) {
                Frequency.DAILY -> {
                    Instant.ofEpochMilli(period.startInclusive).atZone(zone).toLocalDate()
                        .minusDays(1)
                        .atStartOfDay(zone)
                        .toInstant()
                        .toEpochMilli()
                }
                Frequency.WEEKLY -> {
                    Instant.ofEpochMilli(period.startInclusive).atZone(zone).toLocalDate()
                        .minusWeeks(1)
                        .atStartOfDay(zone)
                        .toInstant()
                        .toEpochMilli()
                }
            }
        }

        return streak
    }
}
