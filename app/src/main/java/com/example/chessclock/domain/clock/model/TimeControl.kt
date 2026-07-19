package com.example.chessclock.domain.clock.model

data class TimeControl(
    val name: String,
    val baseMillis: Long,
    val incrementMillis: Long,
) {
    init {
        require(baseMillis > 0)
        require(incrementMillis >= 0)
    }

    companion object {
        val presets = listOf(
            TimeControl("Bullet", 60_000, 0),  //
            TimeControl("Blitz", 3 * 60_000, 2_000),
            TimeControl("Rapid", 10 * 60_000, 0),
            TimeControl("Classical", 30 * 60_000, 0),
        )
    }
}