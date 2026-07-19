package com.example.chessclock.formatting

import com.example.chessclock.presentation.clock.ClockTimeFormatter
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class ClockTimeFormatterTest {

    private val formatter = ClockTimeFormatter(Locale.US)

    @Test
    fun `formats normal time without tenths`() {
        assertEquals("3:02", formatter.format(182_000))
    }

    @Test
    fun `formats time below one minute with tenths`() {
        assertEquals("0:59.9", formatter.format(59_950))
    }

    @Test
    fun `formats zero`() {
        assertEquals("0:00.0", formatter.format(0))
    }
}
