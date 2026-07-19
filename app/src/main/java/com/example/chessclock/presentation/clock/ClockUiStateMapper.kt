package com.example.chessclock.presentation.clock

import com.example.chessclock.domain.clock.model.ChessGameState
import com.example.chessclock.domain.clock.model.ClockStatus
import com.example.chessclock.domain.clock.model.Player
import com.example.chessclock.domain.clock.model.TimeControl
import kotlinx.collections.immutable.toImmutableList

interface ClockUiStateMapper {
    fun map(state: ChessGameState): ClockUiState
}

class DefaultClockUiStateMapper(
    private val timeFormatter: ClockTimeFormatter,
) : ClockUiStateMapper {
    override fun map(state: ChessGameState): ClockUiState {
        val isRunning = state.status == ClockStatus.RUNNING
        val isPresetSelected = TimeControl.presets.any { it == state.timeControl }
        
        return ClockUiState(
            playerOne = PlayerClockUiState(
                player = Player.ONE,
                formattedTime = timeFormatter.format(state.playerOneMillis),
                moveCount = state.playerOneMoves,
                isActive = isRunning && state.activePlayer == Player.ONE,
                hasTimedOut = state.status == ClockStatus.FINISHED && state.playerOneMillis == 0L,
            ),
            playerTwo = PlayerClockUiState(
                player = Player.TWO,
                formattedTime = timeFormatter.format(state.playerTwoMillis),
                moveCount = state.playerTwoMoves,
                isActive = isRunning && state.activePlayer == Player.TWO,
                hasTimedOut = state.status == ClockStatus.FINISHED && state.playerTwoMillis == 0L,
            ),
            availableTimeControls = TimeControl.presets.map { control ->
                TimeControlUiState(
                    timeControl = control,
                    displayName = "${control.name} ${timeFormatter.formatTimeControl(control)}",
                    isSelected = state.timeControl == control
                )
            }.toImmutableList(),
            isCustomTimeControlSelected = !isPresetSelected,
            isRunning = isRunning,
            canStart = state.status == ClockStatus.READY || state.status == ClockStatus.PAUSED,
            canPause = isRunning,
            canReset = state.status != ClockStatus.READY ||
                state.playerOneMillis != state.timeControl.baseMillis ||
                state.playerTwoMillis != state.timeControl.baseMillis,
            canSelectTimeControl = !isRunning,
        )
    }
}
