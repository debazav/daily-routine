package com.debazapps.routine.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.debazapps.routine.domain.ValidationError
import com.debazapps.routine.reminders.ReminderScheduler
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoutineRepositoryTest {
    private lateinit var db: AppDatabase
    private lateinit var scheduler: ReminderScheduler
    private lateinit var repository: RoutineRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        scheduler = ReminderScheduler(context)
        repository = RoutineRepository(db, scheduler)
    }

    @After
    fun tearDown() {
        // Os alarmes são reais; não deixar lembretes de teste agendados no aparelho.
        kotlinx.coroutines.runBlocking { db.routineDao().getAll() }.forEach { scheduler.cancel(it.id) }
        db.close()
    }

    private fun routine(title: String, start: Int, end: Int, day: Int = 1) =
        Routine(dayOfWeek = day, title = title, startMinutes = start * 60, endMinutes = end * 60)

    @Test
    fun createsOneCopyPerSelectedDay() = runTest {
        val result = repository.saveForDays(routine("Academia", 7, 8), setOf(5, 1, 3))

        assertTrue(result is SaveResult.Saved)
        val saved = (result as SaveResult.Saved).routines
        assertEquals(listOf(1, 3, 5), saved.map { it.dayOfWeek })
        assertEquals(3, saved.map { it.id }.distinct().size)
        assertEquals(3, db.routineDao().getAll().size)
    }

    @Test
    fun conflictOnAnyDaySavesNothingAndReportsTheDay() = runTest {
        repository.save(routine("Reunião", 9, 10, day = 3))

        val result = repository.saveForDays(routine("Estudo", 9, 11), setOf(1, 3, 5))

        val error = (result as SaveResult.Invalid).error as ValidationError.Conflict
        assertEquals(listOf(3), error.others.map { it.dayOfWeek })
        assertEquals("Reunião", error.others.single().title)
        assertEquals(1, db.routineDao().getAll().size)
    }

    @Test
    fun reportsConflictsFromEveryDay() = runTest {
        repository.save(routine("A", 9, 10, day = 1))
        repository.save(routine("B", 10, 11, day = 2))

        val result = repository.saveForDays(routine("Novo", 9, 11), setOf(1, 2, 3))

        val error = (result as SaveResult.Invalid).error as ValidationError.Conflict
        assertEquals(listOf(1, 2), error.others.map { it.dayOfWeek })
    }

    @Test
    fun noDaysSelectedIsRejected() = runTest {
        val result = repository.saveForDays(routine("Algo", 9, 10), emptySet())
        assertEquals(SaveResult.Invalid(ValidationError.NoDaysSelected), result)
    }
}
