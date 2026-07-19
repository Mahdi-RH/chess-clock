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
    private val control = TimeControl("Blitz", baseMillis = 180_000, incrementMillis = 2_000)

    @Test
    fun `start activates player one and tick decreases only active clock`() {
        val started = engine.reduce(ChessGameState(control), ClockAction.Start)
        val ticked = engine.reduce(started, ClockAction.Tick(1_250))

        assertEquals(Player.ONE, ticked.activePlayer)
        assertEquals(178_750, ticked.playerOneMillis)
        assertEquals(180_000, ticked.playerTwoMillis)
    }

    @Test
    fun `pressing active clock applies increment and changes player`() {
        val started = engine.reduce(ChessGameState(control), ClockAction.Start)
        val afterMove = engine.reduce(started, ClockAction.PressClock(Player.ONE))

        assertEquals(182_000, afterMove.playerOneMillis)
        assertEquals(1, afterMove.playerOneMoves)
        assertEquals(Player.TWO, afterMove.activePlayer)
    }

    @Test
    fun `pressing inactive clock has no effect`() {
        val started = engine.reduce(ChessGameState(control), ClockAction.Start)

        assertEquals(started, engine.reduce(started, ClockAction.PressClock(Player.TWO)))
    }

    @Test
    fun `clock stops at zero and finishes game`() {
        val started = engine.reduce(ChessGameState(control), ClockAction.Start)
        val finished = engine.reduce(started, ClockAction.Tick(200_000))

        assertEquals(0, finished.playerOneMillis)
        assertEquals(ClockStatus.FINISHED, finished.status)
    }

    @Test
    fun `reset restores selected time control`() {
        val started = engine.reduce(ChessGameState(control), ClockAction.Start)
        val ticked = engine.reduce(started, ClockAction.Tick(10_000))
        val reset = engine.reduce(ticked, ClockAction.Reset)

        assertEquals(ChessGameState(control), reset)
    }
}
