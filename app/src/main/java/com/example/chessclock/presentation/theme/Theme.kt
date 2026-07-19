package com.example.chessclock.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ChessClockColors = darkColorScheme(
    primary = ClockGreenLight,
    onPrimary = ClockDark,
    secondary = ClockGreen,
    background = ClockDark,
    onBackground = Color.White,
    surface = ClockSurface,
    onSurface = Color.White,
    error = ClockRed,
)

@Composable
fun ChessClockTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ChessClockColors,
        typography = Typography,
        content = content,
    )
}
