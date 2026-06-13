package com.example.habits.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.habits.HabitFlowApplication
import com.example.habits.data.repository.AuthRepository
import com.example.habits.data.repository.HabitRepository
import com.example.habits.ui.auth.LoginViewModel
import com.example.habits.ui.auth.RegisterViewModel
import com.example.habits.ui.habit.HomeViewModel
import com.example.habits.ui.splash.SplashViewModel

class HabitFlowViewModelFactory(
    val authRepository: AuthRepository,
    val habitRepository: HabitRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(SplashViewModel::class.java) -> {
                SplashViewModel(authRepository) as T
            }
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                LoginViewModel(authRepository) as T
            }
            modelClass.isAssignableFrom(RegisterViewModel::class.java) -> {
                RegisterViewModel(authRepository) as T
            }
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(authRepository, habitRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    companion object {
        fun from(application: HabitFlowApplication): HabitFlowViewModelFactory {
            return HabitFlowViewModelFactory(
                authRepository = application.authRepository,
                habitRepository = application.habitRepository,
            )
        }
    }
}
