package com.debazapps.routine.ui.edit

import com.debazapps.routine.data.Routine
import com.debazapps.routine.domain.LAST_MINUTE_OF_DAY
import com.debazapps.routine.domain.ValidationError

/** Conteúdo do formulário de adicionar/editar; `id == 0` significa rotina nova. */
data class EditorState(
    val id: Long = 0,
    val title: String = "",
    val startMinutes: Int,
    val endMinutes: Int,
    val description: String = "",
    /** Dias marcados no formulário; `null` quando o dia é fixo (formulário aberto na tela de um dia). */
    val days: Set<Int>? = null,
    val error: ValidationError? = null,
) {
    val isNew: Boolean get() = id == 0L

    fun toRoutine(dayOfWeek: Int) = Routine(
        id = id,
        dayOfWeek = dayOfWeek,
        title = title,
        startMinutes = startMinutes,
        endMinutes = endMinutes,
        description = description,
    )

    companion object {
        private const val DEFAULT_START = 8 * 60

        /** Rotina nova de uma hora, começando em [start] (08:00 se não informado). */
        fun new(start: Int = DEFAULT_START, days: Set<Int>? = null) =
            EditorState(startMinutes = start, endMinutes = minOf(start + 60, LAST_MINUTE_OF_DAY), days = days)
    }
}
