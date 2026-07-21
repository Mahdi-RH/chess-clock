package com.example.chessclock.formatting

import com.example.chessclock.presentation.clock.ClockTimeFormatter
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class ClockTimeFormatterTest {

    private val formatter = ClockTimeFormatter(Locale.US)

    @Test
    fun `given time of at least one minute when formatted then tenths are omitted`() {
        assertEquals("3:02", formatter.format(182_000))
    }

    @Test
    fun `given time below one minute when formatted then tenths are included`() {
        assertEquals("0:59.9", formatter.format(59_950))
    }

    @Test
    fun `given zero milliseconds when formatted then zero time with tenths is returned`() {
        assertEquals("0:00.0", formatter.format(0))
    }
}
