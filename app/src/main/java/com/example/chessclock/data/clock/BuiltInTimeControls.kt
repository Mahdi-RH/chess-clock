package com.example.chessclock.data.clock

import com.example.chessclock.domain.clock.model.TimeControl

object BuiltInTimeControls {
    private val bullet = TimeControl(
        id = 1,
        name = "Bullet",
        baseMillis = 60_000L,
        incrementMillis = 0L,
    )
    private val blitz = TimeControl(
        id = 2,
        name = "Blitz",
        baseMillis = 3 * 60_000L,
        incrementMillis = 2_000
    )

    private val rapid = TimeControl(
        id = 3,
        name = "Rapid",
        baseMillis = 10 * 60_000L,
        incrementMillis = 0
    )

    private val classical = TimeControl(
        id = 4,
        name = "Classical", baseMillis = 30 * 60_000,
        incrementMillis = 0
    )

    val all = listOf(
        bullet,
        blitz,
        rapid,
        classical
    )

    val default = blitz
}