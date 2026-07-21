package com.example.chessclock.presentation.clock

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.click
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.chessclock.domain.clock.engine.ClockAction
import com.example.chessclock.domain.clock.engine.StandardChessClockEngine
import com.example.chessclock.domain.clock.model.ChessGameState
import com.example.chessclock.domain.clock.model.Player
import com.example.chessclock.domain.clock.model.TimeControl
import com.example.chessclock.presentation.clock.components.ChessClockScreen
import com.example.chessclock.presentation.theme.ChessClockTheme
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class ChessClockScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val engine = StandardChessClockEngine()
    private val mapper = DefaultClockUiStateMapper(ClockTimeFormatter(Locale.US))
    private val presets = listOf(DEFAULT_CONTROL)
    private lateinit var domainState: ChessGameState
    private lateinit var uiState: MutableState<ClockUiState>

    @Before
    fun setUp() {
        showClock(ChessGameState.initial(DEFAULT_CONTROL))
    }

    @Test
    fun givenReadyGameWhenStartIsTappedThenPlayerOneBecomesActive() {
        composeRule.onNodeWithText("Start").performClick()

        composeRule.onNodeWithTag(PLAYER_ONE_TAG)
            .assert(hasStateDescription("Player 1, 3:00, active"))
            .assertIsEnabled()
        composeRule.onNodeWithTag(PLAYER_TWO_TAG).assertIsNotEnabled()
    }

    @Test
    fun givenRunningGameWhenActiveClockIsTappedThenTurnSwitchesPlayers() {
        composeRule.onNodeWithText("Start").performClick()

        composeRule.onNodeWithTag(PLAYER_ONE_TAG).performClick()

        composeRule.onNodeWithTag(PLAYER_ONE_TAG).assertIsNotEnabled()
        composeRule.onNodeWithTag(PLAYER_TWO_TAG)
            .assert(hasStateDescription("Player 2, 3:00, active"))
            .assertIsEnabled()
    }

    @Test
    fun givenRunningGameWhenInactiveClockIsTouchedThenTurnDoesNotChange() {
        composeRule.onNodeWithText("Start").performClick()

        composeRule.onNodeWithTag(PLAYER_TWO_TAG).performTouchInput { click() }

        composeRule.onNodeWithTag(PLAYER_ONE_TAG).assertIsEnabled()
        composeRule.onNodeWithTag(PLAYER_TWO_TAG).assertIsNotEnabled()
        composeRule.runOnIdle {
            assertEquals(Player.ONE, domainState.activePlayer)
            assertEquals(0, domainState.playerTwoMoves)
        }
    }

    @Test
    fun givenRunningGameWhenPausedThenFurtherTicksDoNotChangeEitherClock() {
        composeRule.onNodeWithText("Start").performClick()
        dispatch(ClockAction.Tick(1_000L))

        composeRule.onNodeWithText("Pause").performClick()
        dispatch(ClockAction.Tick(5_000L))

        composeRule.onNodeWithTag(PLAYER_ONE_TAG).assertTextEquals(
            "Player 1",
            "2:59",
            "0 moves",
        )
        composeRule.onNodeWithTag(PLAYER_TWO_TAG).assertTextEquals(
            "Player 2",
            "3:00",
            "0 moves",
        )
    }

    @Test
    fun givenCustomDialogWhenValuesChangeThenInvalidInputIsRejectedAndValidInputIsSelected() {
        composeRule.onNodeWithText("Custom").performClick()
        composeRule.onNodeWithTag("custom_minutes").performTextReplacement("0")

        composeRule.onNodeWithTag("custom_apply").assertIsNotEnabled()

        composeRule.onNodeWithTag("custom_minutes").performTextReplacement("5")
        composeRule.onNodeWithTag("custom_increment").performTextReplacement("3")
        composeRule.onNodeWithTag("custom_apply").assertIsEnabled().performClick()

        composeRule.onNodeWithText("Custom").assertIsSelected()
        composeRule.runOnIdle {
            assertEquals(300_000L, domainState.timeControl.baseMillis)
            assertEquals(3_000L, domainState.timeControl.incrementMillis)
        }
    }

    @Test
    fun givenTimedOutPlayerWhenDisplayedThenTimeoutIsVisibleAndAccessible() {
        composeRule.onNodeWithText("Start").performClick()
        dispatch(ClockAction.Tick(DEFAULT_CONTROL.baseMillis))

        composeRule.onNodeWithText("TIME OUT").assertExists()
        composeRule.onNodeWithTag(PLAYER_ONE_TAG)
            .assert(hasStateDescription("Player 1, 0:00.0, timed out"))
            .assertIsNotEnabled()
    }

    private fun showClock(initialState: ChessGameState) {
        domainState = initialState
        uiState = mutableStateOf(mapper.map(domainState, presets))
        composeRule.setContent {
            ChessClockTheme {
                ChessClockScreen(
                    state = uiState.value,
                    onAction = ::onAction,
                )
            }
        }
    }

    private fun onAction(action: ClockUiAction) {
        val domainAction = when (action) {
            ClockUiAction.Start -> ClockAction.Start
            ClockUiAction.Pause -> ClockAction.Pause
            ClockUiAction.Reset -> ClockAction.Reset
            is ClockUiAction.PressPlayer -> ClockAction.PressClock(action.player)
            is ClockUiAction.SelectTimeControl -> ClockAction.SelectTimeControl(action.timeControl)
        }
        updateState(domainAction)
    }

    private fun dispatch(action: ClockAction) {
        composeRule.runOnIdle {
            updateState(action)
        }
    }

    private fun updateState(action: ClockAction) {
        domainState = engine.reduce(domainState, action)
        uiState.value = mapper.map(domainState, presets)
    }

    private fun hasStateDescription(expected: String) = SemanticsMatcher.expectValue(
        SemanticsProperties.StateDescription,
        expected,
    )

    private companion object {
        const val PLAYER_ONE_TAG = "player_clock_ONE"
        const val PLAYER_TWO_TAG = "player_clock_TWO"
        val DEFAULT_CONTROL = TimeControl(
            id = 1,
            name = "Blitz",
            baseMillis = 180_000L,
            incrementMillis = 2_000L,
        )
    }
}
