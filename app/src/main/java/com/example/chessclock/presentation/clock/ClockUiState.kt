package com.example.chessclock.presentation.clock

import com.example.chessclock.domain.clock.model.Player
import com.example.chessclock.domain.clock.model.TimeControl

data class ClockUiState(
    val playerOne: PlayerClockUiState,
    val playerTwo: PlayerClockUiState,
    val selectedTimeControl: TimeControl,
    val isRunning: Boolean,
    val canStart: Boolean,
    val canPause: Boolean,
    val canReset: Boolean,
    val canSelectTimeControl: Boolean,
)

data class PlayerClockUiState(
    val player: Player,
    val formattedTime: String,
    val moveCount: Int,
    val isActive: Boolean,
    val isFinished: Boolean,
) {
    val isEnabled: Boolean
        get() = isActive && !isFinished
}
