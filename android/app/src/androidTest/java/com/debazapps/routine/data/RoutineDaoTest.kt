package com.debazapps.routine.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoutineDaoTest {
    private lateinit var db: AppDatabase
    private lateinit var dao: RoutineDao

    @Before
    fun setUp() {
        // Banco em memória: não toca nos dados reais do app (diferente dos testes da versão web).
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase::class.java).build()
        dao = db.routineDao()
    }

    @After
    fun tearDown() = db.close()

    private fun routine(day: Int, title: String, startHour: Int) =
        Routine(dayOfWeek = day, title = title, startMinutes = startHour * 60, endMinutes = startHour * 60 + 30)

    @Test
    fun listsRoutinesOfTheDayOrderedByStart() = runTest {
        dao.insert(routine(1, "Tarde", 14))
        dao.insert(routine(1, "Manhã", 8))
        dao.insert(routine(2, "Outro dia", 9))

        assertEquals(listOf("Manhã", "Tarde"), dao.observeByDay(1).first().map { it.title })
        assertEquals(listOf("Manhã", "Tarde"), dao.getByDay(1).map { it.title })
    }

    @Test
    fun countsRoutinesPerDay() = runTest {
        dao.insert(routine(1, "A", 8))
        dao.insert(routine(1, "B", 9))
        dao.insert(routine(7, "C", 10))

        val counts = dao.observeCountsByDay().first().associate { it.dayOfWeek to it.count }
        assertEquals(mapOf(1 to 2, 7 to 1), counts)
    }

    @Test
    fun updatesAndDeletes() = runTest {
        val id = dao.insert(routine(3, "Original", 8))
        dao.update(routine(3, "Editada", 8).copy(id = id))
        assertEquals("Editada", dao.getById(id)?.title)

        dao.delete(dao.getById(id)!!)
        assertNull(dao.getById(id))
    }
}
