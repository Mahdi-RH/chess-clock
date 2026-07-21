package com.example.chessclock.presentation.clock

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.chessclock.domain.clock.engine.StandardChessClockEngine
import com.example.chessclock.domain.clock.model.Player
import com.example.chessclock.domain.clock.model.TimeControl
import com.example.chessclock.domain.clock.provider.TimeControlProvider
import com.example.chessclock.domain.time.TimeProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class ChessClockViewModelTest {

    private lateinit var dispatcher: TestDispatcher
    private lateinit var timeProvider: FakeTimeProvider
    private val createdViewModels = mutableListOf<ChessClockViewModel>()

    @Before
    fun setUp() {
        dispatcher = StandardTestDispatcher()
        Dispatchers.setMain(dispatcher)
        timeProvider = FakeTimeProvider()
    }

    @After
    fun tearDown() {
        createdViewModels.forEach { it.viewModelScope.cancel() }
        createdViewModels.clear()
        Dispatchers.resetMain()
    }

    @Test
    fun `given game actions when state changes then complete state is persisted`() = runTest(dispatcher.scheduler) {
        val savedState = SavedStateHandle()
        val viewModel = createViewModel(savedState)
        val collection = viewModel.state.launchIn(backgroundScope)

        viewModel.onAction(ClockUiAction.Start)
        timeProvider.nowMillis = 1_250L
        viewModel.onAction(ClockUiAction.PressPlayer(Player.ONE))
        viewModel.onAction(ClockUiAction.Pause)
        runCurrent()

        assertEquals(DEFAULT_CONTROL.id, savedState["time_control_id"])
        assertEquals(DEFAULT_CONTROL.name, savedState["time_control_name"])
        assertEquals(DEFAULT_CONTROL.baseMillis, savedState["base_millis"])
        assertEquals(DEFAULT_CONTROL.incrementMillis, savedState["increment_millis"])
        assertEquals(180_750L, savedState["player_one_millis"])
        assertEquals(180_000L, savedState["player_two_millis"])
        assertEquals(Player.TWO.name, savedState["active_player"])
        assertEquals("PAUSED", savedState["clock_status"])
        assertEquals(1, savedState["player_one_moves"])
        assertEquals(0, savedState["player_two_moves"])
        assertEquals(1_250L, savedState["last_transition_millis"])

        collection.cancel()
    }

    @Test
    fun `given a paused saved game when ViewModel is recreated then state is restored unchanged`() = runTest(dispatcher.scheduler) {
        val savedState = SavedStateHandle(pausedGameState())
        timeProvider.nowMillis = 50_000L

        val viewModel = createViewModel(savedState)
        val collection = viewModel.state.launchIn(backgroundScope)
        runCurrent()

        assertEquals("2:00", viewModel.state.value.playerOne.formattedTime)
        assertEquals("2:30", viewModel.state.value.playerTwo.formattedTime)
        assertEquals(3, viewModel.state.value.playerOne.moveCount)
        assertEquals(2, viewModel.state.value.playerTwo.moveCount)
        assertFalse(viewModel.state.value.isRunning)
        assertTrue(viewModel.state.value.canStart)
        assertEquals(Player.TWO.name, savedState["active_player"])

        collection.cancel()
    }

    @Test
    fun `given a running saved game when ViewModel is recreated then elapsed time is deducted and timer resumes`() = runTest(dispatcher.scheduler) {
        val savedState = SavedStateHandle(runningGameState(lastTransitionMillis = 10_000L))
        timeProvider.nowMillis = 15_000L

        val viewModel = createViewModel(savedState)
        val collection = viewModel.state.launchIn(backgroundScope)
        runCurrent()

        assertEquals("1:55", viewModel.state.value.playerOne.formattedTime)
        assertTrue(viewModel.state.value.isRunning)
        assertEquals(115_000L, savedState["player_one_millis"])
        assertEquals(15_000L, savedState["last_transition_millis"])

        timeProvider.nowMillis = 15_100L
        advanceTimeBy(100.milliseconds)
        runCurrent()
        viewModel.viewModelScope.cancel()

        assertEquals(114_900L, savedState["player_one_millis"])
        assertEquals(15_100L, savedState["last_transition_millis"])

        collection.cancel()
    }

    @Test
    fun `given a checkpoint from before reboot when ViewModel is recreated then saved time is not deducted`() =
        runTest(dispatcher.scheduler) {
            val savedState = SavedStateHandle(runningGameState(lastTransitionMillis = 500_000L))
            timeProvider.nowMillis = 1_000L

            val viewModel = createViewModel(savedState)
            val collection = viewModel.state.launchIn(backgroundScope)
            runCurrent()
            viewModel.viewModelScope.cancel()

            assertEquals("2:00", viewModel.state.value.playerOne.formattedTime)
            assertEquals(120_000L, savedState["player_one_millis"])
            assertEquals(1_000L, savedState["last_transition_millis"])

            collection.cancel()
        }

    private fun createViewModel(savedStateHandle: SavedStateHandle) =
        ChessClockViewModel(
            engine = StandardChessClockEngine(),
            uiStateMapper = DefaultClockUiStateMapper(ClockTimeFormatter(Locale.US)),
            dispatcher = dispatcher,
            timeProvider = timeProvider,
            timeControlProvider = FakeTimeControlProvider,
            savedStateHandle = savedStateHandle,
        ).also(createdViewModels::add)

    private fun pausedGameState() = runningGameState(lastTransitionMillis = 10_000L) + mapOf(
        "player_two_millis" to 150_000L,
        "clock_status" to "PAUSED",
        "active_player" to Player.TWO.name,
        "player_one_moves" to 3,
        "player_two_moves" to 2,
    )

    private fun runningGameState(lastTransitionMillis: Long): Map<String, Any> = mapOf(
        "time_control_id" to DEFAULT_CONTROL.id,
        "time_control_name" to DEFAULT_CONTROL.name,
        "base_millis" to DEFAULT_CONTROL.baseMillis,
        "increment_millis" to DEFAULT_CONTROL.incrementMillis,
        "player_one_millis" to 120_000L,
        "player_two_millis" to 150_000L,
        "active_player" to Player.ONE.name,
        "clock_status" to "RUNNING",
        "player_one_moves" to 3,
        "player_two_moves" to 2,
        "last_transition_millis" to lastTransitionMillis,
    )

    private class FakeTimeProvider(
        var nowMillis: Long = 0L,
    ) : TimeProvider {
        override fun getElapsedRealtime(): Long = nowMillis
    }

    private object FakeTimeControlProvider : TimeControlProvider {
        override fun getTimeControls(): List<TimeControl> = listOf(DEFAULT_CONTROL)
        override fun getDefaultTimeControl(): TimeControl = DEFAULT_CONTROL
    }

    private companion object {
        val DEFAULT_CONTROL = TimeControl(
            id = 2,
            name = "Blitz",
            baseMillis = 180_000L,
            incrementMillis = 2_000L,
        )
    }
}
