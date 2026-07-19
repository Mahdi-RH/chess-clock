package com.example.chessclock.presentation.clock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chessclock.domain.clock.engine.ChessClockEngine
import com.example.chessclock.domain.clock.engine.ClockAction
import com.example.chessclock.domain.clock.model.ChessGameState
import com.example.chessclock.domain.clock.model.ClockStatus
import com.example.chessclock.domain.clock.model.Player
import com.example.chessclock.domain.clock.model.TimeControl
import com.example.chessclock.domain.time.TimeProvider
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
    private val dispatcher: CoroutineDispatcher,
    private val timeProvider: TimeProvider,
) : ViewModel() {
    
    private var lastTickMillis = timeProvider.getElapsedRealtime()
    private val gameState = MutableStateFlow(ChessGameState())
    
    val state: StateFlow<ClockUiState> = gameState
        .map(uiStateMapper::map)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = uiStateMapper.map(gameState.value),
        )
        
    private var timerJob: Job? = null

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
                val now = timeProvider.getElapsedRealtime()
                if (gameState.value.status == ClockStatus.RUNNING) {
                    dispatch(ClockAction.Tick(now - lastTickMillis))
                } else {
                    break
                }
                lastTickMillis = now
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
            dispatch(ClockAction.Tick(now - lastTickMillis))
        }
        lastTickMillis = now
    }

    private fun dispatch(action: ClockAction) {
        gameState.update { engine.reduce(it, action) }
    }

    private companion object {
        val TICK_INTERVAL = 100.milliseconds
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
