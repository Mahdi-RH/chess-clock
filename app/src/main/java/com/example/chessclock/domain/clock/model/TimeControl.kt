package com.example.chessclock.domain.clock.model

data class TimeControl(
    val id: Int,
    val name: String,
    val baseMillis: Long,
    val incrementMillis: Long,
) {
    init {
        require(baseMillis > 0)
        require(incrementMillis >= 0)
    }

    companion object {
        const val CUSTOM_ID = -1
    }
}
