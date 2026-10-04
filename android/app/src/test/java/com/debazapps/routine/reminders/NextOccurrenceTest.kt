package com.debazapps.routine.reminders

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class NextOccurrenceTest {
    private val saoPaulo = ZoneId.of("America/Sao_Paulo")

    private fun at(date: String, time: String, zone: ZoneId = saoPaulo) =
        ZonedDateTime.of(LocalDate.parse(date), LocalTime.parse(time), zone)

    private val monday = 1
    private val friday = 5
    private val sunday = 7
    private val nineAm = 9 * 60

    // 2026-09-28 é uma segunda-feira.

    @Test
    fun `mais tarde no mesmo dia`() {
        assertEquals(at("2026-09-28", "09:00"), nextOccurrence(monday, nineAm, at("2026-09-28", "08:00")))
    }

    @Test
    fun `horario ja passou hoje vai para a semana seguinte`() {
        assertEquals(at("2026-10-05", "09:00"), nextOccurrence(monday, nineAm, at("2026-09-28", "10:00")))
    }

    @Test
    fun `exatamente agora vai para a semana seguinte`() {
        // É o caso do ReminderReceiver reagendando no instante em que o alarme dispara.
        assertEquals(at("2026-10-05", "09:00"), nextOccurrence(monday, nineAm, at("2026-09-28", "09:00")))
    }

    @Test
    fun `outro dia mais adiante na semana`() {
        assertEquals(at("2026-10-02", "07:30"), nextOccurrence(friday, 7 * 60 + 30, at("2026-09-28", "10:00")))
    }

    @Test
    fun `domingo a noite para segunda`() {
        assertEquals(at("2026-10-05", "06:00"), nextOccurrence(monday, 6 * 60, at("2026-10-04", "22:00")))
    }

    @Test
    fun `horario inexistente por horario de verao e empurrado para frente`() {
        // Em Nova York, 08/03/2026 pula de 02:00 para 03:00.
        val newYork = ZoneId.of("America/New_York")
        assertEquals(
            at("2026-03-08", "03:30", newYork),
            nextOccurrence(sunday, 2 * 60 + 30, at("2026-03-07", "12:00", newYork)),
        )
    }
}
