package com.example.chessclock.presentation.clock.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.chessclock.presentation.clock.ChessClockViewModel

@Composable
fun ChessClockRoute(viewModel: ChessClockViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ChessClockScreen(
        state = state,
        onAction = viewModel::onAction,
    )
}
