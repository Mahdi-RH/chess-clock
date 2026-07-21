package com.example.chessclock.presentation.clock

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chessclock.domain.clock.engine.ChessClockEngine
import com.example.chessclock.domain.clock.engine.ClockAction
import com.example.chessclock.domain.clock.model.ChessGameState
import com.example.chessclock.domain.clock.model.ClockStatus
import com.example.chessclock.domain.clock.model.Player
import com.example.chessclock.domain.clock.model.TimeControl
import com.example.chessclock.domain.clock.provider.TimeControlProvider
import com.example.chessclock.domain.time.TimeProvider
import com.example.chessclock.di.MainDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class ChessClockViewModel @Inject constructor(
    private val engine: ChessClockEngine,
    private val uiStateMapper: ClockUiStateMapper,
    @param:MainDispatcher private val dispatcher: CoroutineDispatcher,
    private val timeProvider: TimeProvider,
    private val timeControlProvider: TimeControlProvider,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val presets = timeControlProvider.getTimeControls()
    private var lastTickMillis: Long
    private val gameState: MutableStateFlow<ChessGameState>
    private var timerJob: Job? = null

    init {
        val now = timeProvider.getElapsedRealtime()
        val initialState = restoreAndSettleGameState(now)

        gameState = MutableStateFlow(initialState)
        lastTickMillis = now
        saveGameState(initialState)
        resumeTimerIfRunning(initialState)
    }

    private fun restoreAndSettleGameState(now: Long): ChessGameState {
        val restoredState = restoreGameState()
            ?: ChessGameState.initial(timeControlProvider.getDefaultTimeControl())

        if (restoredState.status != ClockStatus.RUNNING) return restoredState

        val savedCheckpoint = savedStateHandle.get<Long>(KEY_LAST_TRANSITION_MILLIS) ?: now
        val validCheckpoint = if (savedCheckpoint <= now) savedCheckpoint else now

        return engine.reduce(
            restoredState,
            ClockAction.Tick(now - validCheckpoint),
        )
    }

    private fun resumeTimerIfRunning(state: ChessGameState) {
        if (state.status == ClockStatus.RUNNING) {
            startTimer()
        }
    }

    val state: StateFlow<ClockUiState> = gameState
        .map { uiStateMapper.map(it, presets) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = uiStateMapper.map(gameState.value, presets),
        )

    fun onAction(action: ClockUiAction) {
        when (action) {
            ClockUiAction.Start -> start()
            ClockUiAction.Pause -> pause()
            ClockUiAction.Reset -> reset()
            is ClockUiAction.PressPlayer -> pressClock(action.player)
            is ClockUiAction.SelectTimeControl -> selectTimeControl(action.timeControl)
        }
    }

    private fun start() {
        lastTickMillis = timeProvider.getElapsedRealtime()
        dispatch(ClockAction.Start)
        startTimer()
    }

    private fun pause() {
        settleElapsedTime()
        dispatch(ClockAction.Pause)
        stopTimer()
    }

    private fun reset() {
        dispatch(ClockAction.Reset)
        stopTimer()
    }

    private fun pressClock(player: Player) {
        settleElapsedTime()
        dispatch(ClockAction.PressClock(player))
        startTimer()
    }

    private fun startTimer() {
        if (timerJob?.isActive == true) return

        timerJob = viewModelScope.launch(dispatcher) {
            while (isActive) {
                delay(TICK_INTERVAL)
                if (gameState.value.status == ClockStatus.RUNNING) {
                    tickTo(timeProvider.getElapsedRealtime())
                } else {
                    break
                }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun selectTimeControl(timeControl: TimeControl) =
        dispatch(ClockAction.SelectTimeControl(timeControl))

    private fun settleElapsedTime() {
        val now = timeProvider.getElapsedRealtime()
        if (gameState.value.status == ClockStatus.RUNNING) {
            tickTo(now)
        } else {
            lastTickMillis = now
        }
    }

    private fun tickTo(now: Long) {
        val elapsedMillis = (now - lastTickMillis).coerceAtLeast(0L)
        lastTickMillis = now
        dispatch(ClockAction.Tick(elapsedMillis))
    }

    private fun dispatch(action: ClockAction) {
        gameState.update { currentState ->
            engine.reduce(currentState, action).also(::saveGameState)
        }
    }

    private fun saveGameState(state: ChessGameState) {
        savedStateHandle[KEY_TIME_CONTROL_ID] = state.timeControl.id
        savedStateHandle[KEY_TIME_CONTROL_NAME] = state.timeControl.name
        savedStateHandle[KEY_BASE_MILLIS] = state.timeControl.baseMillis
        savedStateHandle[KEY_INCREMENT_MILLIS] = state.timeControl.incrementMillis
        savedStateHandle[KEY_PLAYER_ONE_MILLIS] = state.playerOneMillis
        savedStateHandle[KEY_PLAYER_TWO_MILLIS] = state.playerTwoMillis
        savedStateHandle[KEY_ACTIVE_PLAYER] = state.activePlayer?.name
        savedStateHandle[KEY_STATUS] = state.status.name
        savedStateHandle[KEY_PLAYER_ONE_MOVES] = state.playerOneMoves
        savedStateHandle[KEY_PLAYER_TWO_MOVES] = state.playerTwoMoves
        savedStateHandle[KEY_LAST_TRANSITION_MILLIS] = lastTickMillis
    }

    private fun restoreGameState(): ChessGameState? = runCatching {
        val timeControl = TimeControl(
            id = savedStateHandle.get<Int>(KEY_TIME_CONTROL_ID) ?: return null,
            name = savedStateHandle.get<String>(KEY_TIME_CONTROL_NAME) ?: return null,
            baseMillis = savedStateHandle.get<Long>(KEY_BASE_MILLIS) ?: return null,
            incrementMillis = savedStateHandle.get<Long>(KEY_INCREMENT_MILLIS) ?: return null,
        )
        val status = savedStateHandle.get<String>(KEY_STATUS)?.let(ClockStatus::valueOf) ?: return null
        val activePlayer = savedStateHandle.get<String>(KEY_ACTIVE_PLAYER)?.let(Player::valueOf)

        ChessGameState(
            timeControl = timeControl,
            playerOneMillis = savedStateHandle.get<Long>(KEY_PLAYER_ONE_MILLIS) ?: return null,
            playerTwoMillis = savedStateHandle.get<Long>(KEY_PLAYER_TWO_MILLIS) ?: return null,
            activePlayer = activePlayer,
            status = status,
            playerOneMoves = savedStateHandle.get<Int>(KEY_PLAYER_ONE_MOVES) ?: return null,
            playerTwoMoves = savedStateHandle.get<Int>(KEY_PLAYER_TWO_MOVES) ?: return null,
        ).takeIf { state ->
            state.playerOneMillis >= 0L &&
                state.playerTwoMillis >= 0L &&
                state.playerOneMoves >= 0 &&
                state.playerTwoMoves >= 0 &&
                (state.status != ClockStatus.RUNNING || state.activePlayer != null)
        }
    }.getOrNull()

    override fun onCleared() {
        saveGameState(gameState.value)
        super.onCleared()
    }

    private companion object {
        val TICK_INTERVAL = 100.milliseconds
        const val STOP_TIMEOUT_MILLIS = 5_000L
        const val KEY_TIME_CONTROL_ID = "time_control_id"
        const val KEY_TIME_CONTROL_NAME = "time_control_name"
        const val KEY_BASE_MILLIS = "base_millis"
        const val KEY_INCREMENT_MILLIS = "increment_millis"
        const val KEY_PLAYER_ONE_MILLIS = "player_one_millis"
        const val KEY_PLAYER_TWO_MILLIS = "player_two_millis"
        const val KEY_ACTIVE_PLAYER = "active_player"
        const val KEY_STATUS = "clock_status"
        const val KEY_PLAYER_ONE_MOVES = "player_one_moves"
        const val KEY_PLAYER_TWO_MOVES = "player_two_moves"
        const val KEY_LAST_TRANSITION_MILLIS = "last_transition_millis"
    }
}
