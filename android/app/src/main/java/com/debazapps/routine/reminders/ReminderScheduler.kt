package com.debazapps.routine.reminders

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.debazapps.routine.data.Routine
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.temporal.TemporalAdjusters

/** Agenda um alarme por rotina para o próximo início dela; o [ReminderReceiver] reagenda a semana seguinte. */
class ReminderScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    /** No Android 12+ o usuário precisa liberar "Alarmes e lembretes"; sem isso usamos um alarme inexato. */
    fun canScheduleExact(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()

    @SuppressLint("ScheduleExactAlarm") // verificado em canScheduleExact()
    fun schedule(routine: Routine) {
        val triggerAt = nextOccurrence(routine.dayOfWeek, routine.startMinutes, ZonedDateTime.now())
            .toInstant()
            .toEpochMilli()
        val operation = pendingIntent(routine.id)
        if (canScheduleExact()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, operation)
        } else {
            alarmManager.setWindow(AlarmManager.RTC_WAKEUP, triggerAt, INEXACT_WINDOW_MS, operation)
        }
    }

    fun rescheduleAll(routines: List<Routine>) = routines.forEach(::schedule)

    fun cancel(routineId: Long) = alarmManager.cancel(pendingIntent(routineId))

    private fun pendingIntent(routineId: Long): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java)
            .putExtra(ReminderReceiver.EXTRA_ROUTINE_ID, routineId)
        return PendingIntent.getBroadcast(
            context,
            routineId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private companion object {
        const val INEXACT_WINDOW_MS = 5 * 60 * 1000L
    }
}

/** Próximo instante estritamente depois de [now] que cai em [dayOfWeek] (1 = segunda) às [startMinutes]. */
fun nextOccurrence(dayOfWeek: Int, startMinutes: Int, now: ZonedDateTime): ZonedDateTime {
    val time = LocalTime.of(startMinutes / 60, startMinutes % 60)
    val date = now.toLocalDate().with(TemporalAdjusters.nextOrSame(DayOfWeek.of(dayOfWeek)))
    val candidate = ZonedDateTime.of(date, time, now.zone)
    return if (candidate.isAfter(now)) candidate else ZonedDateTime.of(date.plusWeeks(1), time, now.zone)
}
