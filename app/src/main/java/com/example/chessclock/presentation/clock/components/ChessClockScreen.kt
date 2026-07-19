package com.example.chessclock.presentation.clock.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.tooling.preview.Preview
import com.example.chessclock.domain.clock.model.ChessGameState
import com.example.chessclock.domain.clock.model.Player
import com.example.chessclock.presentation.clock.ClockUiAction
import com.example.chessclock.presentation.clock.ClockUiState
import com.example.chessclock.presentation.clock.ClockUiStateMapper
import com.example.chessclock.presentation.theme.ChessClockTheme
import com.example.chessclock.presentation.theme.ClockDark
import com.example.chessclock.presentation.theme.Spacing


@Composable
fun ChessClockScreen(
    state: ClockUiState,
    onAction: (ClockUiAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showCustomDialog by remember { mutableStateOf(false) }  // shall we persist state of dialog when process is killed?

    Surface(modifier = modifier.fillMaxSize(), color = ClockDark) { // ClockDark?
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(Spacing.small),
        ) {
            PlayerClock(
                player = state.playerTwo.player,
                formattedTime = state.playerTwo.formattedTime,
                moveCount = state.playerTwo.moveCount,
                isActive = state.playerTwo.isActive,
                isFinished = state.playerTwo.isFinished,
                enabled = state.playerTwo.isEnabled,
                onClick = { onAction(ClockUiAction.PressPlayer(Player.TWO)) },
                modifier = Modifier.weight(1f).rotate(180f),
            )

            GameControls(
                state = state,
                onAction = onAction,
                onCustomClick = { showCustomDialog = true },
            )

            PlayerClock(
                player = state.playerOne.player,
                formattedTime = state.playerOne.formattedTime,
                moveCount = state.playerOne.moveCount,
                isActive = state.playerOne.isActive,
                isFinished = state.playerOne.isFinished,
                enabled = state.playerOne.isEnabled,
                onClick = { onAction(ClockUiAction.PressPlayer(Player.ONE)) },
                modifier = Modifier.weight(1f),
            )
        }
    }

    if (showCustomDialog) {
        CustomTimeControlDialog(
            onDismiss = { showCustomDialog = false },
            onConfirm = {
                onAction(ClockUiAction.SelectTimeControl(it))
                showCustomDialog = false
            },
        )
    }
}







@Preview(showBackground = true, heightDp = 800)
@Composable
private fun ChessClockPreview() {
    ChessClockTheme {
        ChessClockScreen(
            state = ClockUiStateMapper().map(
                ChessGameState()
            ),
            onAction = {},
        )
    }
}
