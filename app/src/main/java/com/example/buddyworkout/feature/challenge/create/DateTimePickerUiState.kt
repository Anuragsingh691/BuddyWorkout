package com.example.buddyworkout.feature.challenge.create

import com.example.buddyworkout.core.common.CalendarMonth

/** Which half of the sheet the segmented control is showing. */
enum class DateTimeMode { Date, Time }

data class DateTimePickerUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    /** `"Set start"` or `"Set end"`. */
    val title: String = "Set start",
    val mode: DateTimeMode = DateTimeMode.Date,
    val month: CalendarMonth = CalendarMonth(2026, 6),
    val selectedDay: Int? = null,
    /** 12-hour clock, 1..12. */
    val hour: Int = 6,
    val minute: Int = 0,
    val isPm: Boolean = true,
) {
    val hourLabel: String get() = hour.toString().padStart(2, '0')
    val minuteLabel: String get() = minute.toString().padStart(2, '0')
    val meridiem: String get() = if (isPm) "PM" else "AM"

    /** e.g. `"6:00 PM"` — the hour is unpadded here, as the export prints it. */
    val timeLabel: String get() = "$hour:$minuteLabel $meridiem"

    /** e.g. `"Set · 17 Jun, 6:00 PM"`. */
    val confirmLabel: String
        get() = selectedDay?.let { "Set · ${month.shortDate(it)}, $timeLabel" } ?: "Set"

    val canSave: Boolean get() = selectedDay != null && !isLoading
}
