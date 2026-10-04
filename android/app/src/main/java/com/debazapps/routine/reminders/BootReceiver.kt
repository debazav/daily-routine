package com.debazapps.routine.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.debazapps.routine.RoutineApp
import kotlinx.coroutines.launch

/**
 * Alarmes se perdem ao reiniciar o aparelho ou atualizar o app, e ficam com o horário errado se o
 * relógio/fuso mudar. Também reagenda quando o usuário libera alarmes exatos, para trocar os inexatos.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as RoutineApp
        val pending = goAsync()
        app.appScope.launch {
            try {
                app.container.scheduler.rescheduleAll(app.container.dao.getAll())
            } finally {
                pending.finish()
            }
        }
    }
}
