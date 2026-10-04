package com.debazapps.routine.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineDao {
    @Query("SELECT * FROM routines WHERE dayOfWeek = :dayOfWeek ORDER BY startMinutes")
    fun observeByDay(dayOfWeek: Int): Flow<List<Routine>>

    @Query("SELECT dayOfWeek, COUNT(*) AS count FROM routines GROUP BY dayOfWeek")
    fun observeCountsByDay(): Flow<List<DayCount>>

    @Query("SELECT * FROM routines WHERE dayOfWeek = :dayOfWeek ORDER BY startMinutes")
    suspend fun getByDay(dayOfWeek: Int): List<Routine>

    @Query("SELECT * FROM routines WHERE id = :id")
    suspend fun getById(id: Long): Routine?

    @Query("SELECT * FROM routines")
    suspend fun getAll(): List<Routine>

    @Insert
    suspend fun insert(routine: Routine): Long

    @Update
    suspend fun update(routine: Routine)

    @Delete
    suspend fun delete(routine: Routine)
}
