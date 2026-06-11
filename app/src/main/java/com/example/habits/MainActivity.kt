package com.example.habits

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.habits.ui.HabitFlowViewModelFactory
import com.example.habits.ui.navigation.HabitFlowApp
import com.example.habits.ui.theme.HabitsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val application = application as HabitFlowApplication
        val viewModelFactory = HabitFlowViewModelFactory.from(application)
        val pendingHabitId = intent.getLongExtra(EXTRA_HABIT_ID, -1L).takeIf { it > 0 }

        setContent {
            HabitsTheme {
                HabitFlowApp(
                    viewModelFactory = viewModelFactory,
                    pendingHabitId = pendingHabitId,
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    companion object {
        const val EXTRA_HABIT_ID = "extra_habit_id"
    }
}
