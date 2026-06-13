package com.example.habits.data.util

import com.example.habits.domain.model.CheckIn
import com.example.habits.domain.model.Frequency
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class StreakCalculatorTest {

    private val zone = ZoneId.systemDefault()

    @Test
    fun dailyStreak_countsConsecutiveDays() {
        val today = LocalDate.of(2026, 6, 10)
        val todayNoon = today.atTime(12, 0).atZone(zone).toInstant().toEpochMilli()
        val yesterdayNoon = today.minusDays(1).atTime(12, 0).atZone(zone).toInstant().toEpochMilli()
        val checkIns = listOf(
            checkIn(todayNoon),
            checkIn(yesterdayNoon),
        )

        val streak = StreakCalculator.calculate(
            frequency = Frequency.DAILY,
            checkIns = checkIns,
            referenceTimeMillis = todayNoon,
            zone = zone,
        )

        assertEquals(2, streak)
    }

    @Test
    fun dailyStreak_pendingToday_doesNotBreakStreak() {
        val today = LocalDate.of(2026, 6, 10)
        val todayNoon = today.atTime(12, 0).atZone(zone).toInstant().toEpochMilli()
        val yesterdayNoon = today.minusDays(1).atTime(12, 0).atZone(zone).toInstant().toEpochMilli()
        val dayBeforeNoon = today.minusDays(2).atTime(12, 0).atZone(zone).toInstant().toEpochMilli()
        val checkIns = listOf(
            checkIn(yesterdayNoon),
            checkIn(dayBeforeNoon),
        )

        val streak = StreakCalculator.calculate(
            frequency = Frequency.DAILY,
            checkIns = checkIns,
            referenceTimeMillis = todayNoon,
            zone = zone,
        )

        assertEquals(2, streak)
    }

    @Test
    fun dailyStreak_gapBreaksStreak() {
        val today = LocalDate.of(2026, 6, 10)
        val todayNoon = today.atTime(12, 0).atZone(zone).toInstant().toEpochMilli()
        val threeDaysAgo = today.minusDays(3).atTime(12, 0).atZone(zone).toInstant().toEpochMilli()
        val checkIns = listOf(
            checkIn(todayNoon),
            checkIn(threeDaysAgo),
        )

        val streak = StreakCalculator.calculate(
            frequency = Frequency.DAILY,
            checkIns = checkIns,
            referenceTimeMillis = todayNoon,
            zone = zone,
        )

        assertEquals(1, streak)
    }

    @Test
    fun weeklyStreak_countsConsecutiveWeeks() {
        val wednesday = LocalDate.of(2026, 6, 10)
        val thisWeek = wednesday.atTime(10, 0).atZone(zone).toInstant().toEpochMilli()
        val lastWeek = wednesday.minusWeeks(1).atTime(10, 0).atZone(zone).toInstant().toEpochMilli()
        val checkIns = listOf(
            checkIn(thisWeek),
            checkIn(lastWeek),
        )

        val streak = StreakCalculator.calculate(
            frequency = Frequency.WEEKLY,
            checkIns = checkIns,
            referenceTimeMillis = thisWeek,
            zone = zone,
        )

        assertEquals(2, streak)
    }

    private fun checkIn(timestamp: Long): CheckIn {
        return CheckIn(id = timestamp, habitId = 1, completedAt = timestamp)
    }
}
