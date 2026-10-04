@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.debazapps.routine.ui.edit

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.debazapps.routine.R
import com.debazapps.routine.domain.LAST_MINUTE_OF_DAY
import com.debazapps.routine.domain.ValidationError
import com.debazapps.routine.domain.formatRange
import com.debazapps.routine.domain.formatTime

private enum class TimeField { Start, End }

@Composable
fun RoutineEditSheet(
    state: EditorState,
    onChange: ((EditorState) -> EditorState) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var picking by remember { mutableStateOf<TimeField?>(null) }
    val error = state.error
    val errorMessage = error?.message(showDays = state.days != null)
    val titleError = error == ValidationError.EmptyTitle
    val timeError = error is ValidationError.StartNotBeforeEnd || error is ValidationError.Conflict

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                stringResource(if (state.isNew) R.string.add_routine else R.string.edit_routine),
                style = MaterialTheme.typography.headlineSmall,
            )

            OutlinedTextField(
                value = state.title,
                onValueChange = { title -> onChange { it.copy(title = title) } },
                label = { Text(stringResource(R.string.field_title)) },
                placeholder = { Text(stringResource(R.string.field_title_hint)) },
                singleLine = true,
                isError = titleError,
                supportingText = if (titleError) {
                    { Text(errorMessage.orEmpty()) }
                } else {
                    null
                },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = Modifier.fillMaxWidth(),
            )

            state.days?.let { days ->
                DaySelector(
                    selected = days,
                    isError = error == ValidationError.NoDaysSelected,
                    errorMessage = errorMessage.orEmpty(),
                    onToggle = { day ->
                        onChange { it.copy(days = if (day in days) days - day else days + day) }
                    },
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TimeButton(
                        label = stringResource(R.string.field_start),
                        minutes = state.startMinutes,
                        isError = timeError,
                        onClick = { picking = TimeField.Start },
                        modifier = Modifier.weight(1f),
                    )
                    TimeButton(
                        label = stringResource(R.string.field_end),
                        minutes = state.endMinutes,
                        isError = timeError,
                        onClick = { picking = TimeField.End },
                        modifier = Modifier.weight(1f),
                    )
                }
                if (timeError) {
                    Text(
                        errorMessage.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp),
                    )
                }
            }

            OutlinedTextField(
                value = state.description,
                onValueChange = { description -> onChange { it.copy(description = description) } },
                label = { Text(stringResource(R.string.field_description)) },
                minLines = 2,
                maxLines = 4,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = Modifier.fillMaxWidth(),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            ) {
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
                Button(onClick = onSave) { Text(stringResource(R.string.save)) }
            }
        }

        picking?.let { field ->
            TimePickerAlert(
                initialMinutes = if (field == TimeField.Start) state.startMinutes else state.endMinutes,
                onConfirm = { minutes ->
                    onChange { current ->
                        when (field) {
                            // Mover o início leva o fim junto, mantendo a duração.
                            TimeField.Start -> {
                                val duration = current.endMinutes - current.startMinutes
                                current.copy(
                                    startMinutes = minutes,
                                    endMinutes = if (duration > 0) minOf(minutes + duration, LAST_MINUTE_OF_DAY) else current.endMinutes,
                                )
                            }
                            TimeField.End -> current.copy(endMinutes = minutes)
                        }
                    }
                    picking = null
                },
                onDismiss = { picking = null },
            )
        }
    }
}

@Composable
private fun DaySelector(
    selected: Set<Int>,
    isError: Boolean,
    errorMessage: String,
    onToggle: (day: Int) -> Unit,
) {
    val shortNames = stringArrayResource(R.array.day_names_short)
    Column {
        Text(
            stringResource(R.string.field_days),
            style = MaterialTheme.typography.labelLarge,
            color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlowRow {
            (1..7).forEach { day ->
                Row(
                    // A linha inteira (caixa + nome) é a área de toque.
                    modifier = Modifier
                        .toggleable(value = day in selected, role = Role.Checkbox, onValueChange = { onToggle(day) })
                        .padding(end = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(checked = day in selected, onCheckedChange = null)
                    Text(shortNames[day - 1], style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
        if (isError) {
            Text(
                errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp),
            )
        }
    }
}

@Composable
private fun TimeButton(
    label: String,
    minutes: Int,
    isError: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
    OutlinedCard(onClick = onClick, modifier = modifier, border = BorderStroke(1.dp, borderColor)) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(formatTime(minutes), style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
private fun TimePickerAlert(initialMinutes: Int, onConfirm: (Int) -> Unit, onDismiss: () -> Unit) {
    val pickerState = rememberTimePickerState(
        initialHour = initialMinutes / 60,
        initialMinute = initialMinutes % 60,
        is24Hour = true,
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        text = { TimePicker(state = pickerState) },
        confirmButton = {
            TextButton(onClick = { onConfirm(pickerState.hour * 60 + pickerState.minute) }) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
    )
}

/** [showDays]: no formulário de vários dias, o conflito precisa dizer em qual dia está. */
@Composable
private fun ValidationError.message(showDays: Boolean): String = when (this) {
    ValidationError.EmptyTitle -> stringResource(R.string.error_empty_title)
    ValidationError.StartNotBeforeEnd -> stringResource(R.string.error_start_after_end)
    ValidationError.NoDaysSelected -> stringResource(R.string.error_no_days)
    is ValidationError.Conflict -> if (showDays) {
        val dayNames = stringArrayResource(R.array.day_names)
        val lines = others.map {
            stringResource(R.string.conflict_day_line, dayNames[it.dayOfWeek - 1], it.title, formatRange(it.startMinutes, it.endMinutes))
        }
        stringResource(R.string.error_conflict_days, lines.joinToString("\n"))
    } else {
        val items = others.map { stringResource(R.string.conflict_item, it.title, formatRange(it.startMinutes, it.endMinutes)) }
        stringResource(R.string.error_conflict, items.joinToString(", "))
    }
}
