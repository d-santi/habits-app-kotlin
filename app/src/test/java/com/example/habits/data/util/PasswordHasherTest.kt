package com.example.habits.data.util

import com.example.habits.domain.model.Frequency
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class PasswordHasherTest {

    @Test
    fun hash_samePasswordAndSalt_producesSameHash() {
        val salt = PasswordHasher.generateSalt()
        val hash1 = PasswordHasher.hash("secret123", salt)
        val hash2 = PasswordHasher.hash("secret123", salt)
        assertEquals(hash1, hash2)
    }

    @Test
    fun hash_differentPasswords_producesDifferentHashes() {
        val salt = PasswordHasher.generateSalt()
        val hash1 = PasswordHasher.hash("password1", salt)
        val hash2 = PasswordHasher.hash("password2", salt)
        assertNotEquals(hash1, hash2)
    }

    @Test
    fun verify_correctPassword_returnsTrue() {
        val salt = PasswordHasher.generateSalt()
        val hash = PasswordHasher.hash("myPassword", salt)
        assertTrue(PasswordHasher.verify("myPassword", salt, hash))
    }

    @Test
    fun verify_incorrectPassword_returnsFalse() {
        val salt = PasswordHasher.generateSalt()
        val hash = PasswordHasher.hash("myPassword", salt)
        assertFalse(PasswordHasher.verify("wrongPassword", salt, hash))
    }
}

class PeriodUtilsTest {

    private val zone = ZoneId.systemDefault()

    @Test
    fun dailyPeriod_containsTimestampWithinSameDay() {
        val date = LocalDate.of(2026, 6, 9)
        val noon = date.atTime(12, 0).atZone(zone).toInstant().toEpochMilli()
        val period = PeriodUtils.getPeriodRange(Frequency.DAILY, noon)

        assertTrue(noon >= period.startInclusive)
        assertTrue(noon < period.endExclusive)
    }

    @Test
    fun dailyPeriod_excludesNextDayTimestamp() {
        val date = LocalDate.of(2026, 6, 9)
        val nextDay = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val noon = date.atTime(12, 0).atZone(zone).toInstant().toEpochMilli()
        val period = PeriodUtils.getPeriodRange(Frequency.DAILY, noon)

        assertFalse(nextDay >= period.startInclusive && nextDay < period.endExclusive)
    }

    @Test
    fun weeklyPeriod_containsTimestampWithinSameWeek() {
        val wednesday = LocalDate.of(2026, 6, 10)
        val timestamp = wednesday.atTime(10, 0).atZone(zone).toInstant().toEpochMilli()
        val period = PeriodUtils.getPeriodRange(Frequency.WEEKLY, timestamp)

        assertTrue(timestamp >= period.startInclusive)
        assertTrue(timestamp < period.endExclusive)
    }

    @Test
    fun weeklyPeriod_excludesPreviousWeekTimestamp() {
        val wednesday = LocalDate.of(2026, 6, 10)
        val previousWeek = wednesday.minusWeeks(1).atTime(10, 0).atZone(zone).toInstant().toEpochMilli()
        val timestamp = wednesday.atTime(10, 0).atZone(zone).toInstant().toEpochMilli()
        val period = PeriodUtils.getPeriodRange(Frequency.WEEKLY, timestamp)

        assertFalse(
            previousWeek >= period.startInclusive && previousWeek < period.endExclusive,
        )
    }
}
