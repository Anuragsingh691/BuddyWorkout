package com.example.buddyworkout.core.common

private const val SECOND = 1_000L
private const val MINUTE = 60 * SECOND
private const val HOUR = 60 * MINUTE
private const val DAY = 24 * HOUR

/**
 * Renders a remaining duration for challenge countdowns: `"2d 14h left"`,
 * `"45m left"`, `"Ended"`.
 *
 * Composables never format durations themselves, so every countdown string in
 * the app originates here.
 */
fun formatRemaining(millisRemaining: Long): String {
    if (millisRemaining <= 0) return "Ended"

    val days = millisRemaining / DAY
    val hours = (millisRemaining % DAY) / HOUR
    val minutes = (millisRemaining % HOUR) / MINUTE

    return when {
        days > 0 && hours > 0 -> "${days}d ${hours}h left"
        days > 0 -> "${days}d left"
        hours > 0 && minutes > 0 -> "${hours}h ${minutes}m left"
        hours > 0 -> "${hours}h left"
        minutes > 0 -> "${minutes}m left"
        else -> "< 1m left"
    }
}

/**
 * Coarse remaining time for a challenge card: `"ends in 3 days"`,
 * `"ends in 5 hours"`, `"ended"`.
 *
 * Separate from [formatRemaining] because the mockups print the same fact two
 * ways — a sentence on the card, a clock on the detail screen. Always rounds
 * down: promising more time than remains is the worse error.
 */
fun formatEndsIn(millisRemaining: Long): String {
    if (millisRemaining <= 0) return "ended"

    val days = millisRemaining / DAY
    val hours = millisRemaining / HOUR
    val minutes = millisRemaining / MINUTE

    return when {
        days > 0 -> "ends in $days ${plural(days, "day")}"
        hours > 0 -> "ends in $hours ${plural(hours, "hour")}"
        minutes > 0 -> "ends in $minutes ${plural(minutes, "minute")}"
        else -> "ends in under a minute"
    }
}

/**
 * The challenge detail clock: `"02:14:53"`.
 *
 * Days roll into the hour field rather than adding a segment, so the layout
 * never reflows as a long challenge counts down.
 */
fun formatCountdown(millisRemaining: Long): String {
    val safe = millisRemaining.coerceAtLeast(0)
    val hours = safe / HOUR
    val minutes = (safe % HOUR) / MINUTE
    val seconds = (safe % MINUTE) / SECOND
    return "%02d:%02d:%02d".format(hours, minutes, seconds)
}

private fun plural(count: Long, noun: String) = if (count == 1L) noun else "${noun}s"

/**
 * A challenge window edge: `"17 Jun · 6:00 PM"`.
 *
 * `java.util.Calendar` rather than `java.time`, which needs core-library
 * desugaring at minSdk 24 — the same reason [CalendarMonth] does its own
 * arithmetic. [timeZone] is a parameter so the formatting is testable without
 * depending on wherever the test happens to run.
 */
fun formatWindowLabel(
    millis: Long,
    timeZone: java.util.TimeZone = java.util.TimeZone.getDefault(),
): String {
    val calendar = java.util.Calendar.getInstance(timeZone).apply { timeInMillis = millis }

    val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
    val month = SHORT_MONTHS[calendar.get(java.util.Calendar.MONTH)]
    // HOUR (not HOUR_OF_DAY) is 0-11, and both midnight and midday read as 12.
    val hour = calendar.get(java.util.Calendar.HOUR).let { if (it == 0) 12 else it }
    val minute = calendar.get(java.util.Calendar.MINUTE)
    val meridiem = if (calendar.get(java.util.Calendar.AM_PM) == java.util.Calendar.AM) "AM" else "PM"

    return "$day $month · $hour:%02d $meridiem".format(minute)
}

private val SHORT_MONTHS = listOf(
    "Jan", "Feb", "Mar", "Apr", "May", "Jun",
    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
)
