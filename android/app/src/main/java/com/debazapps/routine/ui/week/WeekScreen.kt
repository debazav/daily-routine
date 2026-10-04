@file:OptIn(ExperimentalMaterial3Api::class)

package com.debazapps.routine.ui.week

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.debazapps.routine.R
import com.debazapps.routine.ui.ReminderPermissionRequest
import com.debazapps.routine.ui.edit.RoutineEditSheet
import java.time.LocalDate

private val DAYS = (1..7).toList()

@Composable
fun WeekScreen(
    onDayClick: (dayOfWeek: Int) -> Unit,
    viewModel: WeekViewModel = viewModel(factory = WeekViewModel.Factory),
) {
    val counts by viewModel.counts.collectAsStateWithLifecycle()
    val dayNames = stringArrayResource(R.array.day_names)
    val today = remember { LocalDate.now().dayOfWeek.value }
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var permissionRequest by remember { mutableIntStateOf(0) }

    LaunchedEffect(viewModel) {
        viewModel.created.collect { dayCount ->
            permissionRequest++
            snackbarHostState.showSnackbar(
                context.resources.getQuantityString(R.plurals.msg_created_days, dayCount, dayCount),
            )
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.app_name)) }) },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::startAdd) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_routine_week))
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            // Espaço no fim para o último card não ficar atrás do botão flutuante.
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    stringResource(R.string.week_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            items(DAYS) { day ->
                DayCard(
                    name = dayNames[day - 1],
                    count = counts[day] ?: 0,
                    isToday = day == today,
                    onClick = { onDayClick(day) },
                )
            }
        }
    }

    viewModel.editor?.let { editor ->
        RoutineEditSheet(
            state = editor,
            onChange = viewModel::updateEditor,
            onSave = viewModel::save,
            onDismiss = viewModel::dismissEditor,
        )
    }

    ReminderPermissionRequest(permissionRequest)
}

@Composable
private fun DayCard(name: String, count: Int, isToday: Boolean, onClick: () -> Unit) {
    val colors = if (isToday) {
        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    } else {
        CardDefaults.cardColors()
    }
    Card(onClick = onClick, colors = colors, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(name, style = MaterialTheme.typography.titleMedium)
                Text(
                    routineCountText(count),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (isToday) {
                Text(
                    stringResource(R.string.today),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

// Em pt-BR o plural "one" cobre 0 e 1 ("0 rotina"), então o zero tem texto próprio.
@Composable
private fun routineCountText(count: Int): String =
    if (count == 0) stringResource(R.string.no_routines)
    else pluralStringResource(R.plurals.routine_count, count, count)
