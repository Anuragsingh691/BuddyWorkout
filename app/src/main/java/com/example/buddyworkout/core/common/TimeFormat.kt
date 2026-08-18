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
