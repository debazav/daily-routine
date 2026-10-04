package com.debazapps.routine.domain

import com.debazapps.routine.data.Routine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RoutineValidatorTest {
    private fun String.toMinutes() = split(":").let { (h, m) -> h.toInt() * 60 + m.toInt() }

    private fun routine(start: String, end: String, id: Long = 0, title: String = "Rotina", day: Int = 1) =
        Routine(id = id, dayOfWeek = day, title = title, startMinutes = start.toMinutes(), endMinutes = end.toMinutes())

    private val existing = routine("09:00", "10:00", id = 1, title = "Existente")

    @Test
    fun `titulo em branco e rejeitado`() {
        assertEquals(ValidationError.EmptyTitle, RoutineValidator.validate(routine("08:00", "09:00", title = "   "), emptyList()))
    }

    @Test
    fun `inicio igual ao fim e rejeitado`() {
        assertEquals(ValidationError.StartNotBeforeEnd, RoutineValidator.validate(routine("09:00", "09:00"), emptyList()))
    }

    @Test
    fun `inicio depois do fim e rejeitado`() {
        assertEquals(ValidationError.StartNotBeforeEnd, RoutineValidator.validate(routine("11:00", "10:00"), emptyList()))
    }

    @Test
    fun `sobreposicao parcial e conflito`() {
        assertEquals(ValidationError.Conflict(listOf(existing)), RoutineValidator.validate(routine("09:30", "10:30"), listOf(existing)))
        assertEquals(ValidationError.Conflict(listOf(existing)), RoutineValidator.validate(routine("08:30", "09:30"), listOf(existing)))
    }

    @Test
    fun `rotina dentro de outra e conflito`() {
        assertEquals(ValidationError.Conflict(listOf(existing)), RoutineValidator.validate(routine("09:15", "09:45"), listOf(existing)))
    }

    @Test
    fun `rotina que envolve outra e conflito`() {
        assertEquals(ValidationError.Conflict(listOf(existing)), RoutineValidator.validate(routine("08:00", "11:00"), listOf(existing)))
    }

    @Test
    fun `horarios encostados nao conflitam`() {
        assertNull(RoutineValidator.validate(routine("10:00", "11:00"), listOf(existing)))
        assertNull(RoutineValidator.validate(routine("08:00", "09:00"), listOf(existing)))
    }

    @Test
    fun `editar uma rotina nao conflita com ela mesma`() {
        assertNull(RoutineValidator.validate(existing.copy(endMinutes = "10:30".toMinutes()), listOf(existing)))
    }

    @Test
    fun `outro dia da semana nao conflita`() {
        assertNull(RoutineValidator.validate(routine("09:00", "10:00", day = 2), listOf(existing)))
    }

    @Test
    fun `lista todas as rotinas sobrepostas`() {
        val other = routine("10:00", "11:00", id = 2, title = "Outra")
        assertEquals(
            ValidationError.Conflict(listOf(existing, other)),
            RoutineValidator.validate(routine("09:30", "10:30"), listOf(existing, other)),
        )
    }
}
