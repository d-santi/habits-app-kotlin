package com.example.habits.ui.habit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.habits.data.repository.AuthRepository
import com.example.habits.data.repository.HabitRepository
import com.example.habits.data.repository.HabitResult
import com.example.habits.domain.model.CheckIn
import com.example.habits.domain.model.Habit
import com.example.habits.domain.model.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HabitDetailUiState(
    val habit: Habit? = null,
    val loadState: UiState<Unit> = UiState.Loading,
    val deleteState: UiState<Unit> = UiState.Idle,
)

class HabitDetailViewModel(
    private val habitId: Long,
    private val authRepository: AuthRepository,
    private val habitRepository: HabitRepository,
) : ViewModel() {
    companion object {
        fun provideFactory(
            habitId: Long,
            authRepository: AuthRepository,
            habitRepository: HabitRepository,
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return HabitDetailViewModel(habitId, authRepository, habitRepository) as T
                }
            }
        }
    }
    private val _uiState = MutableStateFlow(HabitDetailUiState())
    val uiState: StateFlow<HabitDetailUiState> = _uiState.asStateFlow()

    val checkIns: StateFlow<List<CheckIn>> = habitRepository.observeCheckIns(habitId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    init {
        loadHabit()
    }

    private fun loadHabit() {
        viewModelScope.launch {
            _uiState.update { it.copy(loadState = UiState.Loading) }
            val userId = authRepository.loggedInUserId.first()
            if (userId == null) {
                _uiState.update { it.copy(loadState = UiState.Error("Sesión no válida")) }
                return@launch
            }

            val habit = habitRepository.getHabit(habitId, userId)
            if (habit == null) {
                _uiState.update { it.copy(loadState = UiState.Error("Hábito no encontrado")) }
            } else {
                _uiState.update { it.copy(habit = habit, loadState = UiState.Success(Unit)) }
            }
        }
    }

    fun deleteHabit(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(deleteState = UiState.Loading) }
            val userId = authRepository.loggedInUserId.first()
            if (userId == null) {
                _uiState.update { it.copy(deleteState = UiState.Error("Sesión no válida")) }
                return@launch
            }

            when (val result = habitRepository.deleteHabit(habitId, userId)) {
                is HabitResult.Success -> {
                    _uiState.update { it.copy(deleteState = UiState.Success(Unit)) }
                    onSuccess()
                }
                is HabitResult.Error -> {
                    _uiState.update { it.copy(deleteState = UiState.Error(result.message)) }
                }
            }
        }
    }
}
