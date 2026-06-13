package com.example.habits.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habits.data.repository.AuthRepository
import com.example.habits.data.repository.AuthResult
import com.example.habits.domain.model.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegisterUiState(
    val username: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val usernameError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val submitState: UiState<Unit> = UiState.Idle,
)

class RegisterViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onUsernameChange(value: String) {
        _uiState.update {
            it.copy(username = value, usernameError = null, submitState = UiState.Idle)
        }
    }

    fun onPasswordChange(value: String) {
        _uiState.update {
            it.copy(password = value, passwordError = null, submitState = UiState.Idle)
        }
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.update {
            it.copy(confirmPassword = value, confirmPasswordError = null, submitState = UiState.Idle)
        }
    }

    fun register(onSuccess: () -> Unit) {
        val current = _uiState.value
        var hasError = false

        if (current.username.isBlank()) {
            _uiState.update { it.copy(usernameError = "El usuario es obligatorio") }
            hasError = true
        }
        if (current.password.length < 4) {
            _uiState.update { it.copy(passwordError = "La contraseña debe tener al menos 4 caracteres") }
            hasError = true
        }
        if (current.confirmPassword != current.password) {
            _uiState.update { it.copy(confirmPasswordError = "Las contraseñas no coinciden") }
            hasError = true
        }
        if (hasError) return

        viewModelScope.launch {
            _uiState.update { it.copy(submitState = UiState.Loading) }
            when (val result = authRepository.register(current.username, current.password)) {
                is AuthResult.Success -> {
                    _uiState.update { it.copy(submitState = UiState.Success(Unit)) }
                    onSuccess()
                }
                is AuthResult.Error -> {
                    _uiState.update {
                        it.copy(
                            submitState = UiState.Error(result.message),
                            usernameError = result.message,
                        )
                    }
                }
            }
        }
    }
}
