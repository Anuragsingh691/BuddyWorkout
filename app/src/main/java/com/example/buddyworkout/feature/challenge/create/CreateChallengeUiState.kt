package com.example.buddyworkout.feature.challenge.create

import com.example.buddyworkout.core.ui.model.BuddyUi

/** The duration shortcuts the export offers above the exact start/end fields. */
val DURATION_PRESETS = listOf("5 hours", "12 hours", "1 day", "3 days", "1 week")

data class CreateChallengeUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedBuddies: List<BuddyUi> = emptyList(),
    /** e.g. `"Group pushup challenge · you have 1 of 2 active"`; null hides the banner. */
    val limitNotice: String? = null,
    val durations: List<String> = DURATION_PRESETS,
    /** Index into [durations]; -1 when the window was set by hand. */
    val selectedDurationIndex: Int = -1,
    /** e.g. `"17 Jun · 6:00 PM"`. */
    val startLabel: String = "Now",
    /** Empty until the user picks an end. */
    val endLabel: String = "",
    /** e.g. `"12-hour challenge"`. */
    val durationLabel: String = "",
) {
    /** e.g. `"Rohit, Priya"` — first names, as the export prints them. */
    val buddiesLabel: String
        get() = if (selectedBuddies.isEmpty()) {
            "No buddies yet"
        } else {
            selectedBuddies.joinToString(", ") { it.name.substringBefore(' ') }
        }

    /** e.g. `"· 2 of 3 added"`, shown beside the Buddies label. */
    val buddiesHint: String get() = "· ${selectedBuddies.size} of $MAX_BUDDIES added"

    /**
     * Buddies are deliberately not required: a challenge starts with just its
     * creator and fills up from the share link. Requiring them here would make
     * the screen unusable until the buddy list exists.
     */
    val canCreate: Boolean
        get() = selectedDurationIndex >= 0 && !isLoading
}
