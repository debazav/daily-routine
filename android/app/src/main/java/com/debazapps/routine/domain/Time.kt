package com.debazapps.routine.domain

import java.util.Locale

/** 570 -> "09:30" */
fun formatTime(minutes: Int): String =
    String.format(Locale.ROOT, "%02d:%02d", minutes / 60, minutes % 60)

fun formatRange(startMinutes: Int, endMinutes: Int): String =
    "${formatTime(startMinutes)} – ${formatTime(endMinutes)}"

const val LAST_MINUTE_OF_DAY = 23 * 60 + 59
