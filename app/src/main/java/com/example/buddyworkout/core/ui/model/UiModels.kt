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
)

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
