package com.example.buddyworkout.feature.challenge.create

private const val HOUR = 60 * 60 * 1000L
private const val DAY = 24 * HOUR

/** The span a challenge runs for, from creation to deadline. */
data class ChallengeWindow(val startMillis: Long, val endMillis: Long)

/**
 * One of the duration shortcuts on the create screen.
 *
 * The label is what the chip shows; [summary] is the line under the window
 * fields ("12-hour challenge"), which is the same span said differently.
 */
data class ChallengeDuration(
    val label: String,
    val millis: Long,
    val summary: String,
) {
    /** A challenge starts when it is created — there is no scheduled start yet. */
    fun windowFrom(nowMillis: Long) = ChallengeWindow(nowMillis, nowMillis + millis)

    companion object {
        val PRESETS = listOf(
            ChallengeDuration("5 hours", 5 * HOUR, "5-hour challenge"),
            ChallengeDuration("12 hours", 12 * HOUR, "12-hour challenge"),
            ChallengeDuration("1 day", DAY, "1-day challenge"),
            ChallengeDuration("3 days", 3 * DAY, "3-day challenge"),
            ChallengeDuration("1 week", 7 * DAY, "1-week challenge"),
        )
    }
}
