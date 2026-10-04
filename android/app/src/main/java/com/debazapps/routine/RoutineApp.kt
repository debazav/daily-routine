package com.debazapps.routine

import android.app.Application
import com.debazapps.routine.reminders.Notifications
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class RoutineApp : Application() {
    lateinit var container: AppContainer
        private set

    /** Vive enquanto o processo existir; usado pelos receivers, que não têm um escopo próprio. */
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        Notifications.createChannel(this)
        // "Forçar parada" apaga os alarmes do app e nenhum broadcast avisa; reagendar ao subir o processo cobre isso.
        appScope.launch { container.scheduler.rescheduleAll(container.dao.getAll()) }
    }
}
