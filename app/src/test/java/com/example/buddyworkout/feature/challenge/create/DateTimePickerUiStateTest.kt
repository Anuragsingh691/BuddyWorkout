package com.example.buddyworkout.feature.challenge.create

import com.example.buddyworkout.core.common.CalendarMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DateTimePickerUiStateTest {

    private val june = CalendarMonth(2026, 6)

    @Test fun `the confirm button reads as the export draws it`() {
        val state = DateTimePickerUiState(
            month = june,
            selectedDay = 17,
            hour = 6,
            minute = 0,
            isPm = true,
        )

        assertEquals("Set · 17 Jun, 6:00 PM", state.confirmLabel)
    }

    @Test fun `the boxes pad to two digits but the sentence does not`() {
        val state = DateTimePickerUiState(hour = 6, minute = 5)

        assertEquals("06", state.hourLabel)
        assertEquals("05", state.minuteLabel)
        assertEquals("6:05 PM", state.timeLabel)
    }

    @Test fun `midday and midnight keep their 12`() {
        assertEquals("12:00 PM", DateTimePickerUiState(hour = 12, isPm = true).timeLabel)
        assertEquals("12:00 AM", DateTimePickerUiState(hour = 12, isPm = false).timeLabel)
    }

    @Test fun `nothing can be saved until a day is picked`() {
        assertFalse(DateTimePickerUiState(selectedDay = null).canSave)
        assertEquals("Set", DateTimePickerUiState(selectedDay = null).confirmLabel)
        assertTrue(DateTimePickerUiState(selectedDay = 17).canSave)
    }

    @Test fun `a save in flight blocks a second one`() =
        assertFalse(DateTimePickerUiState(selectedDay = 17, isLoading = true).canSave)
}
