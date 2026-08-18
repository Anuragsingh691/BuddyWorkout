package com.example.buddyworkout.feature.invite

import com.example.buddyworkout.core.ui.component.AvatarUi

data class ChallengeInviteUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    /** e.g. `"Rahul K. invited you"`. */
    val headline: String = "",
    val title: String = "Pushup challenge",
    val members: List<AvatarUi> = emptyList(),
    /** e.g. `"2d 14h left"`. */
    val remaining: String = "",
    val startsAt: String = "",
    val endsAt: String = "",
    /** True when the viewer is already at the 2-active-challenge cap. */
    val blockedByLimit: Boolean = false,
) {
    val canAccept: Boolean get() = !blockedByLimit && !isLoading && error == null
}
