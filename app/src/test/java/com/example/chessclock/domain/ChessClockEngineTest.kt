package com.example.chessclock.domain

import com.example.chessclock.domain.clock.engine.ClockAction
import com.example.chessclock.domain.clock.engine.StandardChessClockEngine
import com.example.chessclock.domain.clock.model.ChessGameState
import com.example.chessclock.domain.clock.model.ClockStatus
import com.example.chessclock.domain.clock.model.Player
import com.example.chessclock.domain.clock.model.TimeControl
import org.junit.Assert.assertEquals
import org.junit.Test

class ChessClockEngineTest {
    private val engine = StandardChessClockEngine()
    private val control = TimeControl(1, "Blitz", baseMillis = 180_000L, incrementMillis = 2_000L)

    @Test
    fun `given an initial game when started and ticked then Player One time decreases`() {
        val started = engine.reduce(ChessGameState.initial(control), ClockAction.Start)
        val ticked = engine.reduce(started, ClockAction.Tick(1_250))

        assertEquals(Player.ONE, ticked.activePlayer)
        assertEquals(178_750, ticked.playerOneMillis)
        assertEquals(180_000, ticked.playerTwoMillis)
    }

    @Test
    fun `given a running game when active player presses clock then increment is applied and turn changes`() {
        val started = engine.reduce(ChessGameState.initial(control), ClockAction.Start)
        val afterMove = engine.reduce(started, ClockAction.PressClock(Player.ONE))

        assertEquals(182_000, afterMove.playerOneMillis)
        assertEquals(1, afterMove.playerOneMoves)
        assertEquals(Player.TWO, afterMove.activePlayer)
    }

    @Test
    fun `given a running game when inactive player presses clock then state remains unchanged`() {
        val started = engine.reduce(ChessGameState.initial(control), ClockAction.Start)

        assertEquals(started, engine.reduce(started, ClockAction.PressClock(Player.TWO)))
    }

    @Test
    fun `given a running game when active player time expires then game finishes with zero time`() {
        val started = engine.reduce(ChessGameState.initial(control), ClockAction.Start)
        val finished = engine.reduce(started, ClockAction.Tick(200_000))

        assertEquals(0, finished.playerOneMillis)
        assertEquals(ClockStatus.FINISHED, finished.status)
    }

    @Test
    fun `given a game in progress when reset then initial configuration is restored`() {
        val started = engine.reduce(ChessGameState.initial(control), ClockAction.Start)
        val ticked = engine.reduce(started, ClockAction.Tick(10_000))
        val reset = engine.reduce(ticked, ClockAction.Reset)

        assertEquals(ChessGameState.initial(control), reset)
    }
}
