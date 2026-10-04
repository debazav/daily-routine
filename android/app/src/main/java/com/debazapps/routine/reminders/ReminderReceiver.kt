package com.debazapps.routine.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.debazapps.routine.RoutineApp
import kotlinx.coroutines.launch

/** Disparado pelo alarme de uma rotina: mostra a notificação e agenda a da semana seguinte. */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val routineId = intent.getLongExtra(EXTRA_ROUTINE_ID, -1L)
        if (routineId < 0) return

        val app = context.applicationContext as RoutineApp
        val pending = goAsync()
        app.appScope.launch {
            try {
                // A rotina pode ter sido excluída depois que o alarme foi agendado.
                val routine = app.container.dao.getById(routineId) ?: return@launch
                Notifications.showReminder(context, routine)
                app.container.scheduler.schedule(routine)
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val EXTRA_ROUTINE_ID = "routine_id"
    }
}
