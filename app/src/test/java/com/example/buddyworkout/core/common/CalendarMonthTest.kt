package com.example.buddyworkout.core.common

import org.junit.Assert.assertEquals
import org.junit.Test

class CalendarMonthTest {

    @Test fun `the export's month reads as drawn`() {
        val june = CalendarMonth(2026, 6)

        assertEquals("June 2026", june.label)
        assertEquals(30, june.daysInMonth)
        // 1 June 2026 is a Monday, so exactly one blank precedes it in a grid
        // whose first column is Sunday — matching frame 7.
        assertEquals(1, june.leadingBlanks)
        assertEquals(31, june.cells.size)
        assertEquals(null, june.cells.first())
        assertEquals(1, june.cells[1])
        assertEquals(30, june.cells.last())
    }

    @Test fun `month lengths follow the calendar`() {
        assertEquals(31, CalendarMonth(2026, 1).daysInMonth)
        assertEquals(28, CalendarMonth(2026, 2).daysInMonth)
        assertEquals(31, CalendarMonth(2026, 3).daysInMonth)
        assertEquals(30, CalendarMonth(2026, 4).daysInMonth)
        assertEquals(31, CalendarMonth(2026, 12).daysInMonth)
    }

    @Test fun `February follows the full leap rule`() {
        assertEquals(29, CalendarMonth(2024, 2).daysInMonth)
        assertEquals(28, CalendarMonth(2100, 2).daysInMonth)
        assertEquals(29, CalendarMonth(2000, 2).daysInMonth)
    }

    @Test fun `a month starting on Sunday has no leading blanks`() =
        assertEquals(0, CalendarMonth(2026, 2).leadingBlanks)

    @Test fun `stepping back from January lands in the previous December`() {
        val january = CalendarMonth(2026, 1)

        assertEquals(CalendarMonth(2025, 12), january.previous())
        assertEquals("December 2025", january.previous().label)
    }

    @Test fun `stepping forward from December lands in the next January`() {
        val december = CalendarMonth(2026, 12)

        assertEquals(CalendarMonth(2027, 1), december.next())
    }

    @Test fun `days are formatted the short way the sheet prints them`() {
        assertEquals("17 Jun", CalendarMonth(2026, 6).shortDate(17))
        assertEquals("1 Jan", CalendarMonth(2026, 1).shortDate(1))
    }
}
