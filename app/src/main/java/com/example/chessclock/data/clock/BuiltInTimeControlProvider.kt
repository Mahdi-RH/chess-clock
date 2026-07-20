package com.example.chessclock.data.clock

import com.example.chessclock.domain.clock.model.TimeControl
import com.example.chessclock.domain.clock.provider.TimeControlProvider
import javax.inject.Inject

class BuiltInTimeControlProvider @Inject constructor() : TimeControlProvider {
    override fun getTimeControls(): List<TimeControl> =
        BuiltInTimeControls.all

    override fun getDefaultTimeControl(): TimeControl =
        BuiltInTimeControls.default

}