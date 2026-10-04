package com.debazapps.routine.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Uma atividade que se repete toda semana.
 *
 * Horários são guardados em minutos desde 00:00 (ex.: 09:30 = 570), para que ordenar e
 * comparar sejam operações numéricas.
 */
@Entity(tableName = "routines", indices = [Index("dayOfWeek")])
data class Routine(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** 1 = segunda … 7 = domingo, igual a [java.time.DayOfWeek.getValue]. */
    val dayOfWeek: Int,
    val title: String,
    val startMinutes: Int,
    val endMinutes: Int,
    val description: String? = null,
)

data class DayCount(val dayOfWeek: Int, val count: Int)
