package com.example.buddyworkout.feature.challenge.create

import com.example.buddyworkout.core.common.formatWindowLabel
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

private const val HOUR = 60 * 60 * 1000L
private const val DAY = 24 * HOUR

class ChallengeDurationTest {

    @Test fun `the presets are the ones the export offers`() {
        assertEquals(
            listOf("5 hours", "12 hours", "1 day", "3 days", "1 week"),
            ChallengeDuration.PRESETS.map { it.label },
        )
    }

    @Test fun `each preset carries the span its label claims`() {
        assertEquals(5 * HOUR, ChallengeDuration.PRESETS[0].millis)
        assertEquals(12 * HOUR, ChallengeDuration.PRESETS[1].millis)
        assertEquals(DAY, ChallengeDuration.PRESETS[2].millis)
        assertEquals(3 * DAY, ChallengeDuration.PRESETS[3].millis)
        assertEquals(7 * DAY, ChallengeDuration.PRESETS[4].millis)
    }

    @Test fun `a window runs from now to now plus the duration`() {
        val now = 1_000_000_000L
        val window = ChallengeDuration.PRESETS[1].windowFrom(now)

        assertEquals(now, window.startMillis)
        assertEquals(now + 12 * HOUR, window.endMillis)
    }

    @Test fun `the summary names the span, as the export prints it`() {
        assertEquals("12-hour challenge", ChallengeDuration.PRESETS[1].summary)
        assertEquals("5-hour challenge", ChallengeDuration.PRESETS[0].summary)
        assertEquals("1-day challenge", ChallengeDuration.PRESETS[2].summary)
        assertEquals("3-day challenge", ChallengeDuration.PRESETS[3].summary)
        assertEquals("1-week challenge", ChallengeDuration.PRESETS[4].summary)
    }
}

class WindowLabelTest {

    private val utc = TimeZone.getTimeZone("UTC")

    /** 17 June 2026, 18:00 UTC — built rather than hard-coded as a constant. */
    private val evening = Calendar.getInstance(utc).apply {
        clear()
        set(2026, Calendar.JUNE, 17, 18, 0, 0)
    }.timeInMillis

    @Test fun `a window edge reads as day, month and a 12-hour clock`() =
        assertEquals("17 Jun · 6:00 PM", formatWindowLabel(evening, utc))

    @Test fun `morning is AM and midnight is twelve, not zero`() {
        assertEquals("18 Jun · 6:00 AM", formatWindowLabel(evening + 12 * HOUR, utc))
        assertEquals("18 Jun · 12:00 AM", formatWindowLabel(evening + 6 * HOUR, utc))
    }

    @Test fun `midday is twelve PM, not zero PM`() =
        assertEquals("18 Jun · 12:00 PM", formatWindowLabel(evening + 18 * HOUR, utc))

    @Test fun `minutes are zero padded`() =
        assertEquals("17 Jun · 6:05 PM", formatWindowLabel(evening + 5 * 60_000L, utc))
}
