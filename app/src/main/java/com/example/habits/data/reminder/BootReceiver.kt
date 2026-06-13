package com.example.habits.data.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.habits.data.local.database.DatabaseProvider
import com.example.habits.data.mapper.toDomain
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            return
        }

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = DatabaseProvider.get(context)
                val habits = database.habitDao().getAllWithRemindersEnabled()
                    .map { it.toDomain() }
                ReminderScheduler(context).syncAll(habits)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
