package com.example.chessclock.domain.clock.engine

import com.example.chessclock.domain.clock.model.ChessGameState
import com.example.chessclock.domain.clock.model.ClockStatus
import com.example.chessclock.domain.clock.model.Player

class ChessClockEngine {

    fun reduce(
        state: ChessGameState,
        action: ClockAction
    ): ChessGameState {
        return when (action) {
            ClockAction.Start -> start(state)
            ClockAction.Pause -> pause(state)
            ClockAction.Reset -> reset(state)
            is ClockAction.SelectTimeControl -> selectTimeControl(state = state, action = action)
            is ClockAction.Tick -> tick(state, action)
            is ClockAction.PressClock -> pressClock(state, action)
        }
    }


    private fun start(state: ChessGameState): ChessGameState {
        return when (state.status) {
            ClockStatus.READY, ClockStatus.PAUSED -> state.copy(
                activePlayer = state.activePlayer ?: Player.ONE,
                status = ClockStatus.RUNNING,
            )

            ClockStatus.RUNNING, ClockStatus.FINISHED -> state
        }
    }

    private fun pause(state: ChessGameState): ChessGameState {
        return if (state.status == ClockStatus.RUNNING) {
            state.copy(status = ClockStatus.PAUSED)
        } else {
            state
        }
    }

    private fun reset(state: ChessGameState): ChessGameState {
        return ChessGameState(timeControl = state.timeControl)
    }

//    private fun selectTimeControl(action: ClockAction.SelectTimeControl): ChessGameState {
//        return ChessGameState(timeControl = action.timeControl)
//    }

    private fun selectTimeControl(
        state: ChessGameState,
        action: ClockAction.SelectTimeControl,
    ): ChessGameState {
        return if (state.status == ClockStatus.RUNNING) {
            state
        } else {
            ChessGameState(timeControl = action.timeControl)
        }
    }

    private fun tick(state: ChessGameState, action: ClockAction.Tick): ChessGameState {
        if (state.status != ClockStatus.RUNNING || action.elapsedMillis <= 0) return state
        val activePlayer = state.activePlayer ?: return state
        val remaining = (state.remainingTime(activePlayer) - action.elapsedMillis).coerceAtLeast(0)
        val status = if (remaining == 0L) ClockStatus.FINISHED else state.status

        return when (activePlayer) {
            Player.ONE -> state.copy(playerOneMillis = remaining, status = status)
            Player.TWO -> state.copy(playerTwoMillis = remaining, status = status)
        }
    }

    private fun pressClock(
        state: ChessGameState,
        action: ClockAction.PressClock,
    ): ChessGameState {
        if (state.status != ClockStatus.RUNNING || state.activePlayer != action.player) return state

        return when (action.player) {
            Player.ONE -> state.copy(
                playerOneMillis = state.playerOneMillis + state.timeControl.incrementMillis,
                activePlayer = Player.TWO,
                playerOneMoves = state.playerOneMoves + 1,
            )
            Player.TWO -> state.copy(
                playerTwoMillis = state.playerTwoMillis + state.timeControl.incrementMillis,
                activePlayer = Player.ONE,
                playerTwoMoves = state.playerTwoMoves + 1,
            )
        }
    }
}
