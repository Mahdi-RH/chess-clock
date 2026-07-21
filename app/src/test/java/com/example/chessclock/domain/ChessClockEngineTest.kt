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
    fun givenInitialState_whenStartActionAndTick_thenActivePlayerTimeDecreases() {
        val started = engine.reduce(ChessGameState.initial(control), ClockAction.Start)
        val ticked = engine.reduce(started, ClockAction.Tick(1_250))

        assertEquals(Player.ONE, ticked.activePlayer)
        assertEquals(178_750, ticked.playerOneMillis)
        assertEquals(180_000, ticked.playerTwoMillis)
    }

    @Test
    fun givenRunningGame_whenActivePlayerPressesClock_thenIncrementIsAppliedAndPlayerChanges() {
        val started = engine.reduce(ChessGameState.initial(control), ClockAction.Start)
        val afterMove = engine.reduce(started, ClockAction.PressClock(Player.ONE))

        assertEquals(182_000, afterMove.playerOneMillis)
        assertEquals(1, afterMove.playerOneMoves)
        assertEquals(Player.TWO, afterMove.activePlayer)
    }

    @Test
    fun givenRunningGame_whenInactivePlayerPressesClock_thenStateRemainsUnchanged() {
        val started = engine.reduce(ChessGameState.initial(control), ClockAction.Start)

        assertEquals(started, engine.reduce(started, ClockAction.PressClock(Player.TWO)))
    }

    @Test
    fun givenRunningGame_whenTimeExpires_thenGameFinishesAndTimeIsZero() {
        val started = engine.reduce(ChessGameState.initial(control), ClockAction.Start)
        val finished = engine.reduce(started, ClockAction.Tick(200_000))

        assertEquals(0, finished.playerOneMillis)
        assertEquals(ClockStatus.FINISHED, finished.status)
    }

    @Test
    fun givenGameWithProgress_whenReset_thenStateReturnsToInitialConfiguration() {
        val started = engine.reduce(ChessGameState.initial(control), ClockAction.Start)
        val ticked = engine.reduce(started, ClockAction.Tick(10_000))
        val reset = engine.reduce(ticked, ClockAction.Reset)

        assertEquals(ChessGameState.initial(control), reset)
    }
}
