package com.example.habits.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.habits.ui.HabitFlowViewModelFactory
import com.example.habits.ui.auth.LoginScreen
import com.example.habits.ui.auth.LoginViewModel
import com.example.habits.ui.auth.RegisterScreen
import com.example.habits.ui.auth.RegisterViewModel
import com.example.habits.ui.habit.HabitDetailScreen
import com.example.habits.ui.habit.HabitDetailViewModel
import com.example.habits.ui.habit.HabitFormScreen
import com.example.habits.ui.habit.HabitFormViewModel
import com.example.habits.ui.habit.HomeScreen
import com.example.habits.ui.habit.HomeViewModel
import com.example.habits.ui.splash.SplashScreen
import com.example.habits.ui.splash.SplashViewModel

@Composable
fun HabitFlowApp(
    viewModelFactory: HabitFlowViewModelFactory,
    navController: NavHostController = rememberNavController(),
    pendingHabitId: Long? = null,
) {
    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
    ) {
        composable(Routes.SPLASH) {
            val viewModel: SplashViewModel = viewModel(factory = viewModelFactory)
            SplashScreen(
                viewModel = viewModel,
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.LOGIN) {
            val viewModel: LoginViewModel = viewModel(factory = viewModelFactory)
            LoginScreen(
                viewModel = viewModel,
                onNavigateToRegister = { navController.navigate(Routes.REGISTER) },
                onLoginSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.REGISTER) {
            val viewModel: RegisterViewModel = viewModel(factory = viewModelFactory)
            RegisterScreen(
                viewModel = viewModel,
                onNavigateToLogin = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.HOME) {
            val viewModel: HomeViewModel = viewModel(factory = viewModelFactory)
            var deepLinkHandled by remember { mutableStateOf(false) }
            LaunchedEffect(pendingHabitId) {
                if (pendingHabitId != null && !deepLinkHandled) {
                    navController.navigate(Routes.habitDetail(pendingHabitId))
                    deepLinkHandled = true
                }
            }
            HomeScreen(
                viewModel = viewModel,
                onAddHabit = { navController.navigate(Routes.HABIT_FORM) },
                onHabitClick = { habitId ->
                    navController.navigate(Routes.habitDetail(habitId))
                },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.HABIT_FORM) {
            val viewModel: HabitFormViewModel = viewModel(
                factory = HabitFormViewModel.provideFactory(
                    habitId = null,
                    authRepository = viewModelFactory.authRepository,
                    habitRepository = viewModelFactory.habitRepository,
                ),
            )
            HabitFormScreen(
                viewModel = viewModel,
                isEditMode = false,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }

        composable(
            route = Routes.EDIT_HABIT,
            arguments = listOf(
                navArgument("habitId") { type = NavType.LongType },
            ),
        ) { backStackEntry ->
            val habitId = backStackEntry.arguments?.getLong("habitId") ?: return@composable
            val viewModel: HabitFormViewModel = viewModel(
                factory = HabitFormViewModel.provideFactory(
                    habitId = habitId,
                    authRepository = viewModelFactory.authRepository,
                    habitRepository = viewModelFactory.habitRepository,
                ),
            )
            HabitFormScreen(
                viewModel = viewModel,
                isEditMode = true,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }

        composable(
            route = Routes.HABIT_DETAIL,
            arguments = listOf(
                navArgument("habitId") { type = NavType.LongType },
            ),
        ) { backStackEntry ->
            val habitId = backStackEntry.arguments?.getLong("habitId") ?: return@composable
            val viewModel: HabitDetailViewModel = viewModel(
                factory = HabitDetailViewModel.provideFactory(
                    habitId = habitId,
                    authRepository = viewModelFactory.authRepository,
                    habitRepository = viewModelFactory.habitRepository,
                ),
            )
            HabitDetailScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onEdit = { navController.navigate(Routes.editHabit(habitId)) },
                onDeleted = {
                    navController.popBackStack()
                },
            )
        }
    }
}
