package com.example.chessclock.formatting

import com.example.chessclock.presentation.clock.ClockTimeFormatter
import junit.framework.TestCase.assertEquals
import org.junit.Test

class ClockTimeFormatterTest {

    @Test fun `formats normal time without tenths`() {
        assertEquals("3:02", ClockTimeFormatter.format(182_000))
    }

    @Test
    fun `formats time below one minute with tenths`() {
        assertEquals("0:59.9", ClockTimeFormatter.format(59_950))
    }

    @Test fun `formats zero`() {
        assertEquals("0:00.0", ClockTimeFormatter.format(0))
    }
}