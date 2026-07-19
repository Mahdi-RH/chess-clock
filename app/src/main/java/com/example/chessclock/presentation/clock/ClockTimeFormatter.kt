package com.example.chessclock.presentation.clock

import com.example.chessclock.domain.clock.model.TimeControl
import java.util.Locale

class ClockTimeFormatter(private val locale: Locale = Locale.getDefault()) {
    fun format(millis: Long): String {
        val totalSeconds = millis / 1_000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        val tenths = (millis % 1_000) / 100
        return if (millis < 60_000) {
            String.format(locale, "%d:%02d.%d", minutes, seconds, tenths)
        } else {
            String.format(locale, "%d:%02d", minutes, seconds)
        }
    }

    fun formatTimeControl(timeControl: TimeControl): String {
        val baseMinutes = timeControl.baseMillis / 60_000
        val incrementSeconds = timeControl.incrementMillis / 1_000
        return "$baseMinutes+$incrementSeconds"
    }
}