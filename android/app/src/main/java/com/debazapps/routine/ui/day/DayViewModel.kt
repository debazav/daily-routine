package com.debazapps.routine.ui.day

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.debazapps.routine.RoutineApp
import com.debazapps.routine.data.Routine
import com.debazapps.routine.data.RoutineRepository
import com.debazapps.routine.data.SaveResult
import com.debazapps.routine.ui.edit.EditorState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class UiMessage { Created, Updated, Deleted }

class DayViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: RoutineRepository,
) : ViewModel() {
    val dayOfWeek: Int = checkNotNull(savedStateHandle[DAY_ARG])

    /** `null` enquanto o banco ainda não respondeu, para não piscar o estado vazio. */
    val routines: StateFlow<List<Routine>?> = repository.observeByDay(dayOfWeek)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    var editor by mutableStateOf<EditorState?>(null)
        private set

    var pendingDelete by mutableStateOf<Routine?>(null)
        private set

    private val _messages = Channel<UiMessage>(Channel.BUFFERED)
    val messages: Flow<UiMessage> = _messages.receiveAsFlow()

    fun startAdd() {
        // Sugere começar onde termina a última rotina do dia.
        val lastEnd = routines.value?.lastOrNull()?.endMinutes
        editor = if (lastEnd != null) EditorState.new(start = lastEnd) else EditorState.new()
    }

    fun startEdit(routine: Routine) {
        editor = EditorState(
            id = routine.id,
            title = routine.title,
            startMinutes = routine.startMinutes,
            endMinutes = routine.endMinutes,
            description = routine.description.orEmpty(),
        )
    }

    /** Qualquer edição limpa o erro exibido. */
    fun updateEditor(change: (EditorState) -> EditorState) {
        editor = editor?.let(change)?.copy(error = null)
    }

    fun dismissEditor() {
        editor = null
    }

    fun save() {
        val current = editor ?: return
        viewModelScope.launch {
            when (val result = repository.save(current.toRoutine(dayOfWeek))) {
                is SaveResult.Invalid -> {
                    editor = current.copy(error = result.error)
                }
                is SaveResult.Saved -> {
                    editor = null
                    _messages.send(if (result.created) UiMessage.Created else UiMessage.Updated)
                }
            }
        }
    }

    fun requestDelete(routine: Routine) {
        pendingDelete = routine
    }

    fun cancelDelete() {
        pendingDelete = null
    }

    fun confirmDelete() {
        val routine = pendingDelete ?: return
        pendingDelete = null
        viewModelScope.launch {
            repository.delete(routine)
            _messages.send(UiMessage.Deleted)
        }
    }

    companion object {
        const val DAY_ARG = "day"

        val Factory = viewModelFactory {
            initializer {
                DayViewModel(
                    createSavedStateHandle(),
                    (this[APPLICATION_KEY] as RoutineApp).container.repository,
                )
            }
        }
    }
}
