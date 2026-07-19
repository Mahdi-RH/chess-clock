package com.example.chessclock.presentation.clock

import com.example.chessclock.domain.clock.model.Player
import com.example.chessclock.domain.clock.model.TimeControl

sealed interface ClockUiAction {
    data object Start : ClockUiAction
    data object Pause : ClockUiAction
    data object Reset : ClockUiAction
    data class PressPlayer(val player: Player) : ClockUiAction
    data class SelectTimeControl(
        val timeControl: TimeControl,
    ) : ClockUiAction
}