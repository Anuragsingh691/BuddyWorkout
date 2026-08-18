package com.example.buddyworkout.feature.challenge.create

import com.example.buddyworkout.core.ui.model.BuddyUi

data class CreateChallengeUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedBuddies: List<BuddyUi> = emptyList(),
    /** e.g. `"Now"` or `"Mon 18 Aug, 06:00"`. */
    val startLabel: String = "Now",
    /** Empty until the user picks an end. */
    val endLabel: String = "",
    /** e.g. `"3 days"`. */
    val durationLabel: String = "",
) {
    val buddiesLabel: String
        get() = when (selectedBuddies.size) {
            0 -> "None yet"
            1 -> "1 buddy"
            else -> "${selectedBuddies.size} buddies"
        }

    val canCreate: Boolean
        get() = selectedBuddies.isNotEmpty() && endLabel.isNotBlank() && !isLoading
}
