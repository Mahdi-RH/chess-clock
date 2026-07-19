package com.example.chessclock.presentation.clock

import com.example.chessclock.domain.clock.model.Player
import com.example.chessclock.domain.clock.model.TimeControl
import kotlinx.collections.immutable.ImmutableList

data class ClockUiState(
    val playerOne: PlayerClockUiState,
    val playerTwo: PlayerClockUiState,
    val availableTimeControls: ImmutableList<TimeControlUiState>,
    val isCustomTimeControlSelected: Boolean,
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
