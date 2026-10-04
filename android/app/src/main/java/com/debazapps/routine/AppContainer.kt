package com.debazapps.routine

import android.content.Context
import com.debazapps.routine.data.AppDatabase
import com.debazapps.routine.data.RoutineDao
import com.debazapps.routine.data.RoutineRepository
import com.debazapps.routine.reminders.ReminderScheduler

/** Injeção de dependência manual: uma instância de cada, criada na [RoutineApp]. */
class AppContainer(context: Context) {
    private val database = AppDatabase.build(context)
    val dao: RoutineDao = database.routineDao()
    val scheduler = ReminderScheduler(context)
    val repository = RoutineRepository(database, scheduler)
}
