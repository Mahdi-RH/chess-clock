package com.example.chessclock.domain.clock.engine

import com.example.chessclock.domain.clock.model.Player
import com.example.chessclock.domain.clock.model.TimeControl

sealed interface ClockAction {
    data object Start : ClockAction
    data object Pause : ClockAction
    data object Reset : ClockAction
    data class Tick(val elapsedMillis: Long) : ClockAction
    data class PressClock(val player: Player) : ClockAction
    data class SelectTimeControl(val timeControl: TimeControl) : ClockAction
}