package com.example.habits.data.repository

import com.example.habits.data.local.dao.CheckInDao
import com.example.habits.data.local.dao.HabitDao
import com.example.habits.data.local.dao.UserDao
import com.example.habits.data.local.entity.CheckInEntity
import com.example.habits.data.local.entity.HabitEntity
import com.example.habits.data.local.entity.UserEntity
import com.example.habits.data.local.preferences.SessionStorage
import com.example.habits.data.mapper.toDomain
import com.example.habits.data.reminder.ReminderSchedulerContract
import com.example.habits.data.util.PasswordHasher
import com.example.habits.domain.model.Frequency
import com.example.habits.domain.model.Habit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AuthRepositoryTest {

    private lateinit var userDao: FakeUserDao
    private lateinit var sessionStorage: FakeSessionStorage
    private lateinit var authRepository: AuthRepository

    @Before
    fun setUp() {
        userDao = FakeUserDao()
        sessionStorage = FakeSessionStorage()
        authRepository = AuthRepository(userDao, sessionStorage)
    }

    @Test
    fun register_validUser_insertsUser() = runBlocking {
        val result = authRepository.register("testuser", "1234")
        assertTrue(result is AuthResult.Success)
        assertEquals(1, userDao.users.size)
        assertEquals(1L, sessionStorage.currentUserId)
    }

    @Test
    fun register_duplicateUsername_returnsError() = runBlocking {
        authRepository.register("testuser", "1234")
        val result = authRepository.register("testuser", "5678")
        assertTrue(result is AuthResult.Error)
        assertEquals("Este nombre de usuario ya está registrado", (result as AuthResult.Error).message)
    }

    @Test
    fun login_validCredentials_setsSession() = runBlocking {
        val salt = PasswordHasher.generateSalt()
        val hash = PasswordHasher.hash("1234", salt)
        userDao.users.add(UserEntity(id = 1, username = "testuser", passwordHash = hash, passwordSalt = salt))

        val result = authRepository.login("testuser", "1234")
        assertTrue(result is AuthResult.Success)
        assertEquals(1L, sessionStorage.currentUserId)
    }

    @Test
    fun login_invalidPassword_returnsError() = runBlocking {
        val salt = PasswordHasher.generateSalt()
        val hash = PasswordHasher.hash("1234", salt)
        userDao.users.add(UserEntity(id = 1, username = "testuser", passwordHash = hash, passwordSalt = salt))

        val result = authRepository.login("testuser", "wrong")
        assertTrue(result is AuthResult.Error)
        assertEquals("Usuario o contraseña incorrectos", (result as AuthResult.Error).message)
    }
}

class HabitRepositoryTest {

    private lateinit var habitDao: FakeHabitDao
    private lateinit var checkInDao: FakeCheckInDao
    private lateinit var reminderScheduler: FakeReminderScheduler
    private lateinit var habitRepository: HabitRepository

    @Before
    fun setUp() {
        habitDao = FakeHabitDao()
        checkInDao = FakeCheckInDao()
        reminderScheduler = FakeReminderScheduler()
        habitRepository = HabitRepository(habitDao, checkInDao, reminderScheduler)
    }

    @Test
    fun createHabit_emptyName_returnsError() = runBlocking {
        val result = habitRepository.createHabit(
            userId = 1,
            name = "  ",
            description = "desc",
            frequency = Frequency.DAILY,
            reminderEnabled = false,
            reminderHour = null,
            reminderMinute = null,
        )
        assertTrue(result is HabitResult.Error)
    }

    @Test
    fun updateHabit_validData_updatesEntity() = runBlocking {
        val habitEntity = HabitEntity(
            id = 1,
            userId = 1,
            name = "Leer",
            description = "Libros",
            frequency = Frequency.DAILY.name,
            createdAt = System.currentTimeMillis(),
        )
        habitDao.habits.add(habitEntity)

        val result = habitRepository.updateHabit(
            habitId = 1,
            userId = 1,
            name = "Leer más",
            description = "30 minutos",
            frequency = Frequency.WEEKLY,
            reminderEnabled = true,
            reminderHour = 8,
            reminderMinute = 30,
        )

        assertTrue(result is HabitResult.Success)
        assertEquals("Leer más", habitDao.habits.first().name)
        assertEquals(Frequency.WEEKLY.name, habitDao.habits.first().frequency)
        assertEquals(true, habitDao.habits.first().reminderEnabled)
    }

    @Test
    fun checkInHabit_preventsDuplicateInSamePeriod() = runBlocking {
        val habitEntity = HabitEntity(
            id = 1,
            userId = 1,
            name = "Meditar",
            description = "",
            frequency = Frequency.DAILY.name,
            createdAt = System.currentTimeMillis(),
        )
        habitDao.habits.add(habitEntity)
        val domainHabit = habitEntity.toDomain()

        val first = habitRepository.checkInHabit(domainHabit)
        val second = habitRepository.checkInHabit(domainHabit)

        assertTrue(first is HabitResult.Success)
        assertTrue(second is HabitResult.Success)
        assertEquals(1, checkInDao.checkIns.size)
    }
}

private class FakeUserDao : UserDao {
    val users = mutableListOf<UserEntity>()

    override suspend fun insert(user: UserEntity): Long {
        val id = (users.maxOfOrNull { it.id } ?: 0) + 1
        users.add(user.copy(id = id))
        return id
    }

    override suspend fun getByUsername(username: String): UserEntity? {
        return users.find { it.username == username }
    }

    override suspend fun getById(userId: Long): UserEntity? {
        return users.find { it.id == userId }
    }
}

private class FakeSessionStorage : SessionStorage {
    var currentUserId: Long? = null
        private set

    private val userIdFlow = MutableStateFlow<Long?>(null)

    override val loggedInUserId: Flow<Long?> = userIdFlow

    override suspend fun setLoggedInUserId(userId: Long) {
        currentUserId = userId
        userIdFlow.value = userId
    }

    override suspend fun clearSession() {
        currentUserId = null
        userIdFlow.value = null
    }
}

private class FakeHabitDao : HabitDao {
    val habits = mutableListOf<HabitEntity>()

    override suspend fun insert(habit: HabitEntity): Long {
        val id = (habits.maxOfOrNull { it.id } ?: 0) + 1
        habits.add(habit.copy(id = id))
        return id
    }

    override suspend fun update(habit: HabitEntity) {
        val index = habits.indexOfFirst { it.id == habit.id }
        if (index >= 0) {
            habits[index] = habit
        }
    }

    override fun observeByUserId(userId: Long): Flow<List<HabitEntity>> {
        return flowOf(habits.filter { it.userId == userId })
    }

    override suspend fun getById(habitId: Long, userId: Long): HabitEntity? {
        return habits.find { it.id == habitId && it.userId == userId }
    }

    override suspend fun getAllByUserId(userId: Long): List<HabitEntity> {
        return habits.filter { it.userId == userId }
    }

    override suspend fun getByHabitId(habitId: Long): HabitEntity? {
        return habits.find { it.id == habitId }
    }

    override suspend fun getAllWithRemindersEnabled(): List<HabitEntity> {
        return habits.filter { it.reminderEnabled }
    }

    override suspend fun delete(habitId: Long, userId: Long) {
        habits.removeAll { it.id == habitId && it.userId == userId }
    }
}

private class FakeReminderScheduler : ReminderSchedulerContract {
    val scheduled = mutableListOf<Habit>()
    val cancelled = mutableListOf<Long>()

    override fun schedule(habit: Habit) {
        scheduled.add(habit)
    }

    override fun cancel(habitId: Long) {
        cancelled.add(habitId)
    }

    override fun syncAll(habits: List<Habit>) {
        habits.forEach { schedule(it) }
    }
}

private class FakeCheckInDao : CheckInDao {
    val checkIns = mutableListOf<CheckInEntity>()

    override suspend fun insert(checkIn: CheckInEntity): Long {
        val id = (checkIns.maxOfOrNull { it.id } ?: 0) + 1
        checkIns.add(checkIn.copy(id = id))
        return id
    }

    override suspend fun countInPeriod(habitId: Long, periodStart: Long, periodEnd: Long): Int {
        return checkIns.count {
            it.habitId == habitId &&
                it.completedAt >= periodStart &&
                it.completedAt < periodEnd
        }
    }

    override fun observeByHabitId(habitId: Long): Flow<List<CheckInEntity>> {
        return flowOf(checkIns.filter { it.habitId == habitId })
    }
}
