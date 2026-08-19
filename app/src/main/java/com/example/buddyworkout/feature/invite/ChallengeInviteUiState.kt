package com.example.buddyworkout.feature.invite

import com.example.buddyworkout.core.ui.component.AvatarUi

data class ChallengeInviteUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    /** e.g. `"Rohit invited you to a group pushup challenge"`. */
    val headline: String = "",
    val subline: String = "Most pushups before the deadline wins. Counted live by AI.",
    val exercise: String = "Pushups",
    val members: List<AvatarUi> = emptyList(),
    /** e.g. `"17 Jun 6PM → 18 Jun 6AM"`. */
    val windowLabel: String = "",
    val endsLabel: String = "Auto at deadline",
    /** True when the viewer is already at the 2-active-challenge cap. */
    val blockedByLimit: Boolean = false,
) {
    /** e.g. `"4 people"`. */
    val memberCountLabel: String get() = "${members.size} people"

    val canAccept: Boolean get() = !blockedByLimit && !isLoading && error == null
}
