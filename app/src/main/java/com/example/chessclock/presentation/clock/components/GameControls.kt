package com.example.chessclock.presentation.clock.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.chessclock.R
import com.example.chessclock.domain.clock.model.ChessGameState
import com.example.chessclock.domain.clock.model.ClockStatus
import com.example.chessclock.domain.clock.model.TimeControl
import com.example.chessclock.presentation.clock.ClockTimeFormatter
import com.example.chessclock.presentation.clock.ClockUiAction
import com.example.chessclock.presentation.clock.ClockUiState
import com.example.chessclock.presentation.clock.DefaultClockUiStateMapper
import com.example.chessclock.presentation.theme.ChessClockTheme
import com.example.chessclock.presentation.theme.ClockDark
import com.example.chessclock.presentation.theme.ClockGreen
import com.example.chessclock.presentation.theme.Spacing


@Composable
fun GameControls(
    state: ClockUiState,
    onAction: (ClockUiAction) -> Unit,
    onCustomClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ClockDark)
            .padding(vertical = Spacing.smallMedium),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = Spacing.medium)
        ) {
            items(
                items = state.availableTimeControls,
                key = { uiState -> uiState.timeControl.id }
            ) { uiState ->
                FilterChip(
                    selected = uiState.isSelected,
                    enabled = state.canSelectTimeControl,
                    onClick = { onAction(ClockUiAction.SelectTimeControl(uiState.timeControl)) },
                    label = { Text(uiState.displayName) },
                )
            }
            item(key = "custom_time_control_chip") {
                FilterChip(
                    selected = state.isCustomTimeControlSelected,
                    enabled = state.canSelectTimeControl,
                    onClick = onCustomClick,
                    label = { Text(stringResource(R.string.custom_time_control)) },
                )
            }
        }
        Spacer(Modifier.height(Spacing.small))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.medium),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            OutlinedButton(
                onClick = { onAction(ClockUiAction.Reset) },
                enabled = state.canReset,
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.reset))
            }
            Button(
                onClick = {
                    if (state.isRunning) onAction(ClockUiAction.Pause)
                    else onAction(ClockUiAction.Start)
                },
                enabled = state.canStart || state.canPause,
                modifier = Modifier.weight(2f),
                colors = ButtonDefaults.buttonColors(containerColor = ClockGreen),
            ) {
                Text(if (state.isRunning) stringResource(R.string.pause) else stringResource(R.string.start))
            }
        }
    }
}


// -------------------------- Preview --------------------------

@Preview(showBackground = true, name = "Initial State")
@Composable
private fun InitialStatePreview() {
    ChessClockTheme {
        GameControls(
            state = previewState(ClockStatus.READY),
            onAction = {},
            onCustomClick = {},
        )
    }
}

@Preview(showBackground = true, name = "Running State")
@Composable
private fun RunningStatePreview() {
    ChessClockTheme {
        GameControls(
            state = previewState(ClockStatus.RUNNING),
            onAction = {},
            onCustomClick = {},
        )
    }
}

@Preview(showBackground = true, name = "Finished State")
@Composable
private fun FinishedStatePreview() {
    ChessClockTheme {
        GameControls(
            state = previewState(ClockStatus.FINISHED),
            onAction = {},
            onCustomClick = {},
        )
    }
}

private fun previewState(status: ClockStatus): ClockUiState {
    val mapper = DefaultClockUiStateMapper(ClockTimeFormatter())
    val defaultControl = TimeControl(1, "Blitz", 180_000, 2_000)
    val state = ChessGameState.initial(defaultControl).copy(status = status)
    return mapper.map(state, listOf(defaultControl))
}
