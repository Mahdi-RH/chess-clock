package com.example.chessclock.presentation.clock

import com.example.chessclock.domain.clock.model.Player
import com.example.chessclock.domain.clock.model.TimeControl

data class ClockUiState(
    val playerOne: PlayerClockUiState,
    val playerTwo: PlayerClockUiState,
    val selectedTimeControl: TimeControl,
    val availableTimeControls: List<TimeControlUiState>,
    val isRunning: Boolean,
    val canStart: Boolean,
    val canPause: Boolean,
    val canReset: Boolean,
    val canSelectTimeControl: Boolean,
)
data class TimeControlUiState(
    val timeControl: TimeControl,
    val displayName: String,
    val isSelected: Boolean,
)

data class PlayerClockUiState(
    val player: Player,
    val formattedTime: String,
    val moveCount: Int,
    val isActive: Boolean,
    val hasTimedOut: Boolean,
) {
    val isEnabled: Boolean
        get() = isActive && !hasTimedOut
}
