package com.example.habits.ui.navigation

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val HABIT_FORM = "habit_form"
    const val EDIT_HABIT = "edit_habit/{habitId}"
    const val HABIT_DETAIL = "habit_detail/{habitId}"

    fun habitDetail(habitId: Long): String = "habit_detail/$habitId"
    fun editHabit(habitId: Long): String = "edit_habit/$habitId"
}
