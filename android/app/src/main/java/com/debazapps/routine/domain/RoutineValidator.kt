package com.debazapps.routine.domain

import com.debazapps.routine.data.Routine

sealed interface ValidationError {
    data object EmptyTitle : ValidationError
    data object StartNotBeforeEnd : ValidationError
    data object NoDaysSelected : ValidationError
    /** Rotinas já existentes que ocupam o horário; podem ser de dias diferentes. */
    data class Conflict(val others: List<Routine>) : ValidationError
}

/** Mesmas regras do backend web (server.ts): título obrigatório, início < fim, sem sobreposição no dia. */
object RoutineValidator {
    fun validate(candidate: Routine, sameDay: List<Routine>): ValidationError? =
        validateFields(candidate)
            ?: findConflicts(candidate, sameDay).takeIf { it.isNotEmpty() }?.let { ValidationError.Conflict(it) }

    /** Regras que não dependem do dia; valem igual para todos os dias escolhidos. */
    fun validateFields(candidate: Routine): ValidationError? = when {
        candidate.title.isBlank() -> ValidationError.EmptyTitle
        candidate.startMinutes >= candidate.endMinutes -> ValidationError.StartNotBeforeEnd
        else -> null
    }

    /**
     * Intervalos [início, fim) se sobrepõem se cada um começa antes do outro terminar;
     * rotinas "encostadas" (uma termina 10:00, outra começa 10:00) são permitidas.
     */
    fun findConflicts(candidate: Routine, sameDay: List<Routine>): List<Routine> =
        sameDay.filter {
            it.id != candidate.id &&
                it.dayOfWeek == candidate.dayOfWeek &&
                it.startMinutes < candidate.endMinutes &&
                it.endMinutes > candidate.startMinutes
        }
}
