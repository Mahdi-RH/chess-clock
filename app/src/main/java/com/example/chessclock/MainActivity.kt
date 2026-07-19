package com.example.chessclock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.chessclock.presentation.theme.ChessClockTheme
import com.example.chessclock.presentation.clock.components.ChessClockRoute

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChessClockTheme {
                ChessClockRoute()
            }
        }
    }
}
