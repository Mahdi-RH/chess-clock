package com.example.chessclock.presentation.clock

import com.example.chessclock.domain.clock.model.ChessGameState
import com.example.chessclock.domain.clock.model.ClockStatus
import com.example.chessclock.domain.clock.model.Player
import com.example.chessclock.domain.clock.model.TimeControl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class ClockUiStateMapperTest {
    private val timeFormatter = ClockTimeFormatter(Locale.US)
    private val mapper = DefaultClockUiStateMapper(timeFormatter)

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

        assertTrue(uiState.playerTwo.hasTimedOut)
        assertFalse(uiState.playerTwo.isEnabled)
        assertFalse(uiState.canStart)
        assertTrue(uiState.canReset)
        assertTrue(uiState.canSelectTimeControl)
    }

    @Test
    fun `maps custom time control to selected custom UI state`() {
        val customControl = TimeControl(
            name = "Custom",
            baseMillis = 60_000,
            incrementMillis = 0
        )
        val domainState = ChessGameState(timeControl = customControl)

        val uiState = mapper.map(domainState)

        assertTrue(uiState.isCustomTimeControlSelected)
        assertTrue(uiState.availableTimeControls.none { it.isSelected })
    }
}
