package com.debazapps.routine.data

import androidx.room.withTransaction
import com.debazapps.routine.domain.RoutineValidator
import com.debazapps.routine.domain.ValidationError
import com.debazapps.routine.reminders.ReminderScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoutineRepository(
    private val database: AppDatabase,
    private val scheduler: ReminderScheduler,
) {
    private val dao = database.routineDao()

    fun observeByDay(dayOfWeek: Int): Flow<List<Routine>> = dao.observeByDay(dayOfWeek)

    fun observeCountsByDay(): Flow<Map<Int, Int>> =
        dao.observeCountsByDay().map { counts -> counts.associate { it.dayOfWeek to it.count } }

    /** Valida, grava (insere se `id == 0`, senão atualiza) e agenda o lembrete. */
    suspend fun save(routine: Routine): SaveResult = saveForDays(routine, setOf(routine.dayOfWeek))

    /**
     * Cria uma cópia de [routine] em cada dia de [days] (o `dayOfWeek` de [routine] é ignorado).
     * Tudo ou nada: se o horário estiver ocupado em qualquer dia, nenhuma é gravada e o erro lista
     * os conflitos de todos os dias. Uma rotina existente (`id != 0`) só pode ser salva em um dia.
     */
    suspend fun saveForDays(routine: Routine, days: Set<Int>): SaveResult {
        require(routine.id == 0L || days.size <= 1) { "Uma rotina existente pertence a um único dia" }
        if (days.isEmpty()) return SaveResult.Invalid(ValidationError.NoDaysSelected)

        val normalized = routine.copy(
            title = routine.title.trim(),
            description = routine.description?.trim()?.takeIf { it.isNotEmpty() },
        )
        RoutineValidator.validateFields(normalized)?.let { return SaveResult.Invalid(it) }

        val result = database.withTransaction {
            val candidates = days.sorted().map { normalized.copy(dayOfWeek = it) }
            val conflicts = candidates.flatMap { RoutineValidator.findConflicts(it, dao.getByDay(it.dayOfWeek)) }
            if (conflicts.isNotEmpty()) {
                SaveResult.Invalid(ValidationError.Conflict(conflicts))
            } else {
                val saved = candidates.map {
                    if (it.id == 0L) {
                        it.copy(id = dao.insert(it))
                    } else {
                        dao.update(it)
                        it
                    }
                }
                SaveResult.Saved(saved, created = normalized.id == 0L)
            }
        }
        if (result is SaveResult.Saved) result.routines.forEach(scheduler::schedule)
        return result
    }

    suspend fun delete(routine: Routine) {
        dao.delete(routine)
        scheduler.cancel(routine.id)
    }
}

sealed interface SaveResult {
    data class Saved(val routines: List<Routine>, val created: Boolean) : SaveResult
    data class Invalid(val error: ValidationError) : SaveResult
}
