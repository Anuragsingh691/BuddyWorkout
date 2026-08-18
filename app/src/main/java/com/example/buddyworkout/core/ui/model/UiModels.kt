package com.example.buddyworkout.core.ui.model

import com.example.buddyworkout.core.ui.component.AvatarUi

/**
 * A challenge as a screen renders it. Every field is display-ready: the data
 * layer formats, the composable only places.
 */
data class ChallengeUi(
    val id: String,
    val title: String,
    /** e.g. `"4 buddies · 2d 14h left"`. */
    val summary: String,
    /** Elapsed fraction of the challenge window, `0f`–`1f`. */
    val progress: Float,
    /** e.g. `"128 reps"` — the viewer's own total. */
    val stat: String,
    val members: List<AvatarUi>,
    /** e.g. `"2d 14h left"`, or `"Ended"`. */
    val remaining: String,
    val isCompleted: Boolean = false,
    /** The viewer's standing, 1-based; null before anyone has logged a rep. */
    val rank: Int? = null,
) {
    /** [rank] as the ordinal the mockups print beside the rep count. */
    val rankLabel: String?
        get() = rank?.let { "$it${ordinalSuffix(it)}" }

    /**
     * Whether the card takes the green treatment. A challenge nobody has
     * logged into yet reads as neutral rather than as losing, so it stays
     * green until a standing exists to say otherwise.
     */
    val isLeading: Boolean get() = rank == null || rank == 1
}

/** "st"/"nd"/"rd"/"th", with the 11-13 exception English requires. */
private fun ordinalSuffix(n: Int): String = when {
    n % 100 in 11..13 -> "th"
    n % 10 == 1 -> "st"
    n % 10 == 2 -> "nd"
    n % 10 == 3 -> "rd"
    else -> "th"
}

/** One row of a leaderboard. */
data class ParticipantUi(
    val uid: String,
    val name: String,
    val avatar: AvatarUi,
    val rank: Int,
    /** e.g. `"128 reps"`. */
    val reps: String,
    /** e.g. `"Avg form 92%"`, or null when there is nothing to add. */
    val subtitle: String? = null,
    val isMe: Boolean = false,
)

/** A selectable buddy in the create flow. */
data class BuddyUi(
    val uid: String,
    val name: String,
    val avatar: AvatarUi,
    /** e.g. `"In 1 active challenge"`. */
    val subtitle: String? = null,
    val selected: Boolean = false,
)
