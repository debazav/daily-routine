package com.debazapps.routine.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.edit
import com.debazapps.routine.R
import com.debazapps.routine.RoutineApp
import com.debazapps.routine.reminders.Notifications

private const val PREFS = "reminder_prefs"
private const val KEY_EXACT_ALARM_DISMISSED = "exact_alarm_prompt_dismissed"

/**
 * A cada mudança de [trigger] (exceto 0), pede em sequência:
 * 1. permissão de notificação (Android 13+), pelo diálogo do sistema;
 * 2. acesso a alarmes exatos (Android 12+), explicando antes de abrir as configurações.
 *
 * O Android para de mostrar o diálogo de notificação sozinho após duas recusas; o de alarme exato
 * deixa de aparecer quando o usuário toca em "Agora não".
 */
@Composable
fun ReminderPermissionRequest(trigger: Int) {
    val context = LocalContext.current
    var showExactAlarmDialog by remember { mutableStateOf(false) }

    fun offerExactAlarms() {
        val scheduler = (context.applicationContext as RoutineApp).container.scheduler
        if (!scheduler.canScheduleExact() && !exactAlarmPromptDismissed(context)) showExactAlarmDialog = true
    }

    val notificationLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { offerExactAlarms() }

    LaunchedEffect(trigger) {
        if (trigger == 0) return@LaunchedEffect
        if (Notifications.hasPermission(context)) {
            offerExactAlarms()
        } else {
            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    if (showExactAlarmDialog) {
        AlertDialog(
            onDismissRequest = { showExactAlarmDialog = false },
            title = { Text(stringResource(R.string.exact_alarm_title)) },
            text = { Text(stringResource(R.string.exact_alarm_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showExactAlarmDialog = false
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        context.startActivity(
                            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${context.packageName}")),
                        )
                    }
                }) { Text(stringResource(R.string.open_settings)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showExactAlarmDialog = false
                    setExactAlarmPromptDismissed(context)
                }) { Text(stringResource(R.string.not_now)) }
            },
        )
    }
}

private fun exactAlarmPromptDismissed(context: Context): Boolean =
    context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_EXACT_ALARM_DISMISSED, false)

private fun setExactAlarmPromptDismissed(context: Context) =
    context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit { putBoolean(KEY_EXACT_ALARM_DISMISSED, true) }
