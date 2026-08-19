package com.example.buddyworkout.core.common

private val MONTH_NAMES = listOf(
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December",
)

private val MONTH_LENGTHS = intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)

/**
 * One month of a calendar grid, as the date sheet draws it.
 *
 * Deliberately plain arithmetic rather than `java.time`: `minSdk` is 24, which
 * would need core-library desugaring, and the app already does its date work
 * this way — see [formatRemaining]. Being pure also makes the awkward parts
 * (leap years, which weekday a month opens on) directly testable.
 *
 * @param month 1-based, so 6 is June.
 */
data class CalendarMonth(val year: Int, val month: Int) {

    val label: String get() = "${MONTH_NAMES[month - 1]} $year"

    val daysInMonth: Int
        get() = if (month == 2 && isLeapYear(year)) 29 else MONTH_LENGTHS[month - 1]

    /** Empty cells before the 1st, in a grid whose first column is Sunday. */
    val leadingBlanks: Int get() = dayOfWeek(year, month, 1)

    /** The grid itself: blanks as nulls, then every day of the month. */
    val cells: List<Int?>
        get() = List(leadingBlanks) { null } + (1..daysInMonth).toList()

    fun previous(): CalendarMonth =
        if (month == 1) CalendarMonth(year - 1, 12) else CalendarMonth(year, month - 1)

    fun next(): CalendarMonth =
        if (month == 12) CalendarMonth(year + 1, 1) else CalendarMonth(year, month + 1)

    /** e.g. `"17 Jun"` — how the sheet's confirm button prints a date. */
    fun shortDate(day: Int): String = "$day ${MONTH_NAMES[month - 1].take(3)}"
}

private fun isLeapYear(year: Int): Boolean =
    year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)

/**
 * Sakamoto's algorithm — 0 is Sunday.
 *
 * January and February are treated as months 13 and 14 of the previous year,
 * which is what the `year - 1` step below is doing; it keeps the leap day at
 * the end of the year where the arithmetic wants it.
 */
private fun dayOfWeek(year: Int, month: Int, day: Int): Int {
    val offsets = intArrayOf(0, 3, 2, 5, 0, 3, 5, 1, 4, 6, 2, 4)
    val y = if (month < 3) year - 1 else year
    return (y + y / 4 - y / 100 + y / 400 + offsets[month - 1] + day) % 7
}
