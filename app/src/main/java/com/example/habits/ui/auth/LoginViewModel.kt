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

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val usernameError: String? = null,
    val passwordError: String? = null,
    val submitState: UiState<Unit> = UiState.Idle,
)

class LoginViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

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

    fun login(onSuccess: () -> Unit) {
        val current = _uiState.value
        if (current.username.isBlank()) {
            _uiState.update { it.copy(usernameError = "El usuario es obligatorio") }
            return
        }
        if (current.password.isBlank()) {
            _uiState.update { it.copy(passwordError = "La contraseña es obligatoria") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(submitState = UiState.Loading) }
            when (val result = authRepository.login(current.username, current.password)) {
                is AuthResult.Success -> {
                    _uiState.update { it.copy(submitState = UiState.Success(Unit)) }
                    onSuccess()
                }
                is AuthResult.Error -> {
                    _uiState.update {
                        it.copy(
                            submitState = UiState.Error(result.message),
                            passwordError = result.message,
                        )
                    }
                }
            }
        }
    }
}
