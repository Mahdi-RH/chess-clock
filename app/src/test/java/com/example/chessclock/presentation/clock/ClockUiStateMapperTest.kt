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
    private val presets = listOf(
        TimeControl(1, "Blitz", 180_000, 2_000),
        TimeControl(2, "Rapid", 600_000, 0)
    )

    @Test
    fun givenRunningDomainState_whenMapped_thenActivePlayerUiStateIsCorrect() {
        val domainState = ChessGameState(
            timeControl = presets[0],
            playerOneMillis = 59_900,
            playerTwoMillis = 180_000,
            activePlayer = Player.ONE,
            status = ClockStatus.RUNNING,
        )

        val uiState = mapper.map(domainState, presets)

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
    fun givenCustomTimeControl_whenMapped_thenCustomUiStateIsSelected() {
        val customControl = TimeControl(1,
            name = "Custom",
            baseMillis = 60_000,
            incrementMillis = 0
        )
        val domainState = ChessGameState.initial(customControl)

        val uiState = mapper.map(domainState, presets)

        assertTrue(uiState.isCustomTimeControlSelected)
        assertTrue(uiState.availableTimeControls.none { it.isSelected })
    }
}
