package com.debazapps.routine.reminders

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.debazapps.routine.MainActivity
import com.debazapps.routine.R
import com.debazapps.routine.data.Routine
import com.debazapps.routine.domain.formatRange

object Notifications {
    private const val CHANNEL_ID = "routine_reminders"

    fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.channel_reminders_name),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply { description = context.getString(R.string.channel_reminders_description) }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    /** Antes do Android 13 notificações não precisam de permissão em tempo de execução. */
    fun hasPermission(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission") // verificado em hasPermission()
    fun showReminder(context: Context, routine: Routine) {
        if (!hasPermission(context)) return

        // Mesmo intent do ícone do launcher: traz o app para a frente sem empilhar outra tela.
        val openApp = PendingIntent.getActivity(
            context,
            routine.id.toInt(),
            Intent(context, MainActivity::class.java)
                .setAction(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_LAUNCHER)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val text = context.getString(R.string.reminder_text, formatRange(routine.startMinutes, routine.endMinutes))
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(routine.title)
            .setContentText(text)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(openApp)
            .setAutoCancel(true)
        routine.description?.let { builder.setStyle(NotificationCompat.BigTextStyle().bigText("$text\n$it")) }

        NotificationManagerCompat.from(context).notify(routine.id.toInt(), builder.build())
    }
}
