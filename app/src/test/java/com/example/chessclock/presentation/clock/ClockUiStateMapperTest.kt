package com.example.chessclock.presentation.clock

import com.example.chessclock.domain.clock.model.ChessGameState
import com.example.chessclock.domain.clock.model.ClockStatus
import com.example.chessclock.domain.clock.model.Player
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClockUiStateMapperTest {
    private val mapper = ClockUiStateMapper()

    @Test
    fun `maps running domain state to active player UI state`() {
        val domainState = ChessGameState(
            playerOneMillis = 59_900,
            activePlayer = Player.ONE,
            status = ClockStatus.RUNNING,
        )

        val uiState = mapper.map(domainState)

        assertEquals("0:59.9", uiState.playerOne.formattedTime)
        assertTrue(uiState.playerOne.isActive)
        assertTrue(uiState.playerOne.isEnabled)
        assertFalse(uiState.playerTwo.isActive)
        assertTrue(uiState.isRunning)
        assertTrue(uiState.canPause)
        assertFalse(uiState.canStart)
        assertFalse(uiState.canSelectTimeControl)
    }

    @Test
    fun `maps timed out player to finished UI state`() {
        val domainState = ChessGameState(
            playerTwoMillis = 0,
            activePlayer = Player.TWO,
            status = ClockStatus.FINISHED,
        )

        val uiState = mapper.map(domainState)

        assertTrue(uiState.playerTwo.isFinished)
        assertFalse(uiState.playerTwo.isEnabled)
        assertFalse(uiState.canStart)
        assertTrue(uiState.canReset)
        assertTrue(uiState.canSelectTimeControl)
    }
}
