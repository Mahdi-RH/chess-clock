package com.example.chessclock.domain.clock.provider

import com.example.chessclock.domain.clock.model.TimeControl

interface TimeControlProvider {
    fun getTimeControls(): List<TimeControl>
    fun getDefaultTimeControl(): TimeControl
}