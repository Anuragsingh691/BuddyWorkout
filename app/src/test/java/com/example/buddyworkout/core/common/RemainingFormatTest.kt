package com.example.buddyworkout.core.common

import org.junit.Assert.assertEquals
import org.junit.Test

private const val SECOND = 1_000L
private const val MINUTE = 60 * SECOND
private const val HOUR = 60 * MINUTE
private const val DAY = 24 * HOUR

/**
 * The card summary and the detail countdown print the same remaining time two
 * different ways, so each gets its own formatter.
 */
class EndsInFormatTest {

    @Test fun `the card summary is coarse, as the export prints it`() {
        assertEquals("ends in 3 days", formatEndsIn(3 * DAY))
        assertEquals("ends in 1 day", formatEndsIn(DAY))
        assertEquals("ends in 5 hours", formatEndsIn(5 * HOUR))
        assertEquals("ends in 1 hour", formatEndsIn(HOUR))
        assertEquals("ends in 20 minutes", formatEndsIn(20 * MINUTE))
        assertEquals("ends in 1 minute", formatEndsIn(MINUTE))
    }

    @Test fun `a part-day rounds down rather than promising more time than there is`() =
        assertEquals("ends in 2 days", formatEndsIn(2 * DAY + 23 * HOUR))

    @Test fun `under a minute is not rounded up to one`() =
        assertEquals("ends in under a minute", formatEndsIn(30 * SECOND))

    @Test fun `a passed deadline reads as ended`() {
        assertEquals("ended", formatEndsIn(0))
        assertEquals("ended", formatEndsIn(-HOUR))
    }
}

class CountdownFormatTest {

    @Test fun `the detail clock is zero-padded hours, minutes and seconds`() {
        assertEquals("02:14:53", formatCountdown(2 * HOUR + 14 * MINUTE + 53 * SECOND))
        assertEquals("00:00:09", formatCountdown(9 * SECOND))
    }

    @Test fun `days roll into the hour field rather than adding a segment`() =
        assertEquals("26:00:00", formatCountdown(DAY + 2 * HOUR))

    @Test fun `a passed deadline is all zeroes, never negative`() {
        assertEquals("00:00:00", formatCountdown(0))
        assertEquals("00:00:00", formatCountdown(-HOUR))
    }
}
