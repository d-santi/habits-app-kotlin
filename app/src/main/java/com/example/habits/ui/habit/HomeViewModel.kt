package com.example.habits.ui.habit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habits.data.repository.AuthRepository
import com.example.habits.data.repository.HabitRepository
import com.example.habits.domain.model.Frequency
import com.example.habits.domain.model.HabitWithCompletion
import com.example.habits.domain.model.UiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val authRepository: AuthRepository,
    private val habitRepository: HabitRepository,
) : ViewModel() {
    private val _logoutState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val logoutState: StateFlow<UiState<Unit>> = _logoutState.asStateFlow()

    private val _checkInState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val checkInState: StateFlow<UiState<Unit>> = _checkInState.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val habits: StateFlow<List<HabitWithCompletion>> = authRepository.loggedInUserId
        .flatMapLatest { userId ->
            if (userId == null) {
                flowOf(emptyList())
            } else {
                habitRepository.observeHabitsWithCompletion(userId)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    val summary: StateFlow<HomeSummary> = habits
        .map { habitList ->
            HomeSummary(
                completedCount = habitList.count { it.isCompletedForPeriod },
                totalCount = habitList.size,
                bestStreak = habitList.maxOfOrNull { it.currentStreak } ?: 0,
                hasWeeklyHabits = habitList.any { it.habit.frequency == Frequency.WEEKLY },
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeSummary(),
        )

    init {
        viewModelScope.launch {
            authRepository.loggedInUserId.collect { userId ->
                if (userId != null) {
                    habitRepository.syncAllReminders(userId)
                }
            }
        }
    }

    fun checkIn(habitWithCompletion: HabitWithCompletion) {
        if (habitWithCompletion.isCompletedForPeriod) return

        viewModelScope.launch {
            _checkInState.value = UiState.Loading
            habitRepository.checkInHabit(habitWithCompletion.habit)
            _checkInState.value = UiState.Success(Unit)
        }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _logoutState.value = UiState.Loading
            authRepository.logout()
            _logoutState.value = UiState.Success(Unit)
            onSuccess()
        }
    }
}
