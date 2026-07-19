package com.example.chessclock.presentation.clock

import com.example.chessclock.domain.clock.model.ChessGameState
import com.example.chessclock.domain.clock.model.ClockStatus
import com.example.chessclock.domain.clock.model.Player

class ClockUiStateMapper {
    fun map(state: ChessGameState): ClockUiState {
        val isRunning = state.status == ClockStatus.RUNNING
        return ClockUiState(
            playerOne = PlayerClockUiState(
                player = Player.ONE,
                formattedTime = ClockTimeFormatter.format(state.playerOneMillis),
                moveCount = state.playerOneMoves,
                isActive = isRunning && state.activePlayer == Player.ONE,
                isFinished = state.status == ClockStatus.FINISHED && state.playerOneMillis == 0L,
            ),
            playerTwo = PlayerClockUiState(
                player = Player.TWO,
                formattedTime = ClockTimeFormatter.format(state.playerTwoMillis),
                moveCount = state.playerTwoMoves,
                isActive = isRunning && state.activePlayer == Player.TWO,
                isFinished = state.status == ClockStatus.FINISHED && state.playerTwoMillis == 0L,
            ),
            selectedTimeControl = state.timeControl,
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
