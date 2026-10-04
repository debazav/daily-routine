package com.debazapps.routine.ui.week

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.debazapps.routine.RoutineApp
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
import java.time.LocalDate

class WeekViewModel(private val repository: RoutineRepository) : ViewModel() {
    /** Quantidade de rotinas por dia (1 = segunda); dias sem rotina não aparecem no mapa. */
    val counts: StateFlow<Map<Int, Int>> = repository.observeCountsByDay()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    var editor by mutableStateOf<EditorState?>(null)
        private set

    private val _created = Channel<Int>(Channel.BUFFERED)
    /** Emite em quantos dias a rotina foi criada. */
    val created: Flow<Int> = _created.receiveAsFlow()

    /** Abre o formulário de vários dias, já com o dia de hoje marcado. */
    fun startAdd() {
        editor = EditorState.new(days = setOf(LocalDate.now().dayOfWeek.value))
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
        val days = current.days.orEmpty()
        viewModelScope.launch {
            // O dia passado a toRoutine é ignorado: saveForDays cria uma cópia para cada dia marcado.
            when (val result = repository.saveForDays(current.toRoutine(dayOfWeek = 1), days)) {
                is SaveResult.Invalid -> {
                    editor = current.copy(error = result.error)
                }
                is SaveResult.Saved -> {
                    editor = null
                    _created.send(result.routines.size)
                }
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { WeekViewModel((this[APPLICATION_KEY] as RoutineApp).container.repository) }
        }
    }
}
