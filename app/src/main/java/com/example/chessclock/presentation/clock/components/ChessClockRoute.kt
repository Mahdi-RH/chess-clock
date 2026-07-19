package com.example.chessclock.presentation.clock.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chessclock.presentation.clock.ChessClockViewModel

@Composable
fun ChessClockRoute(viewModel: ChessClockViewModel = viewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ChessClockScreen(
        state = state,
        onAction = viewModel::onAction,
    )
}