package com.example.buddyworkout.core.common

import org.junit.Assert.assertEquals
import org.junit.Test

private const val SECOND = 1_000L
private const val MINUTE = 60 * SECOND
private const val HOUR = 60 * MINUTE
private const val DAY = 24 * HOUR

class TimeFormatTest {

    @Test fun `days and hours shown when more than a day remains`() =
        assertEquals("2d 14h left", formatRemaining(2 * DAY + 14 * HOUR))

    @Test fun `hours and minutes shown when less than a day remains`() =
        assertEquals("3h 20m left", formatRemaining(3 * HOUR + 20 * MINUTE))

    @Test fun `minutes only when less than an hour remains`() =
        assertEquals("45m left", formatRemaining(45 * MINUTE))

    @Test fun `under a minute reads as almost over rather than zero`() =
        assertEquals("< 1m left", formatRemaining(30 * SECOND))

    @Test fun `zero is ended`() = assertEquals("Ended", formatRemaining(0))

    @Test fun `negative is ended rather than a negative duration`() =
        assertEquals("Ended", formatRemaining(-5 * HOUR))

    @Test fun `whole days do not print a zero hour component`() =
        assertEquals("3d left", formatRemaining(3 * DAY))

    @Test fun `whole hours do not print a zero minute component`() =
        assertEquals("5h left", formatRemaining(5 * HOUR))
}
