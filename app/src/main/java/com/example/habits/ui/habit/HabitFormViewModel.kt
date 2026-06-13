package com.example.habits.ui.habit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.habits.data.repository.AuthRepository
import com.example.habits.data.repository.HabitRepository
import com.example.habits.data.repository.HabitResult
import com.example.habits.domain.model.Frequency
import com.example.habits.domain.model.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HabitFormUiState(
    val name: String = "",
    val description: String = "",
    val frequency: Frequency = Frequency.DAILY,
    val reminderEnabled: Boolean = false,
    val reminderHour: Int = 9,
    val reminderMinute: Int = 0,
    val nameError: String? = null,
    val reminderError: String? = null,
    val loadState: UiState<Unit> = UiState.Idle,
    val submitState: UiState<Unit> = UiState.Idle,
)

class HabitFormViewModel(
    private val habitId: Long?,
    private val authRepository: AuthRepository,
    private val habitRepository: HabitRepository,
) : ViewModel() {
    private val isEditMode = habitId != null

    private val _uiState = MutableStateFlow(
        HabitFormUiState(loadState = if (isEditMode) UiState.Loading else UiState.Success(Unit)),
    )
    val uiState: StateFlow<HabitFormUiState> = _uiState.asStateFlow()

    init {
        if (isEditMode && habitId != null) {
            loadHabit(habitId)
        }
    }

    companion object {
        fun provideFactory(
            habitId: Long?,
            authRepository: AuthRepository,
            habitRepository: HabitRepository,
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return HabitFormViewModel(habitId, authRepository, habitRepository) as T
                }
            }
        }
    }

    private fun loadHabit(habitId: Long) {
        viewModelScope.launch {
            val userId = authRepository.loggedInUserId.first()
            if (userId == null) {
                _uiState.update { it.copy(loadState = UiState.Error("Sesión no válida")) }
                return@launch
            }

            val habit = habitRepository.getHabit(habitId, userId)
            if (habit == null) {
                _uiState.update { it.copy(loadState = UiState.Error("Hábito no encontrado")) }
            } else {
                _uiState.update {
                    it.copy(
                        name = habit.name,
                        description = habit.description,
                        frequency = habit.frequency,
                        reminderEnabled = habit.reminderEnabled,
                        reminderHour = habit.reminderHour ?: 9,
                        reminderMinute = habit.reminderMinute ?: 0,
                        loadState = UiState.Success(Unit),
                    )
                }
            }
        }
    }

    fun onNameChange(value: String) {
        _uiState.update { it.copy(name = value, nameError = null, submitState = UiState.Idle) }
    }

    fun onDescriptionChange(value: String) {
        _uiState.update { it.copy(description = value, submitState = UiState.Idle) }
    }

    fun onFrequencyChange(frequency: Frequency) {
        _uiState.update { it.copy(frequency = frequency, submitState = UiState.Idle) }
    }

    fun onReminderEnabledChange(enabled: Boolean) {
        _uiState.update {
            it.copy(
                reminderEnabled = enabled,
                reminderError = null,
                submitState = UiState.Idle,
            )
        }
    }

    fun onReminderTimeChange(hour: Int, minute: Int) {
        _uiState.update {
            it.copy(
                reminderHour = hour,
                reminderMinute = minute,
                reminderError = null,
                submitState = UiState.Idle,
            )
        }
    }

    fun saveHabit(onSuccess: () -> Unit) {
        val current = _uiState.value
        if (current.name.isBlank()) {
            _uiState.update { it.copy(nameError = "El nombre del hábito no puede estar vacío") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(submitState = UiState.Loading) }
            val userId = authRepository.loggedInUserId.first()
            if (userId == null) {
                _uiState.update {
                    it.copy(submitState = UiState.Error("Sesión no válida. Inicia sesión nuevamente."))
                }
                return@launch
            }

            val result = if (isEditMode && habitId != null) {
                habitRepository.updateHabit(
                    habitId = habitId,
                    userId = userId,
                    name = current.name,
                    description = current.description,
                    frequency = current.frequency,
                    reminderEnabled = current.reminderEnabled,
                    reminderHour = if (current.reminderEnabled) current.reminderHour else null,
                    reminderMinute = if (current.reminderEnabled) current.reminderMinute else null,
                )
            } else {
                habitRepository.createHabit(
                    userId = userId,
                    name = current.name,
                    description = current.description,
                    frequency = current.frequency,
                    reminderEnabled = current.reminderEnabled,
                    reminderHour = if (current.reminderEnabled) current.reminderHour else null,
                    reminderMinute = if (current.reminderEnabled) current.reminderMinute else null,
                )
            }

            when (result) {
                is HabitResult.Success -> {
                    habitRepository.syncAllReminders(userId)
                    _uiState.update { it.copy(submitState = UiState.Success(Unit)) }
                    onSuccess()
                }
                is HabitResult.Error -> {
                    _uiState.update {
                        it.copy(
                            submitState = UiState.Error(result.message),
                            nameError = if (result.message.contains("nombre")) result.message else it.nameError,
                            reminderError = if (result.message.contains("recordatorio")) {
                                result.message
                            } else {
                                it.reminderError
                            },
                        )
                    }
                }
            }
        }
    }
}
