package com.example.buddyworkout.feature.invite

import com.example.buddyworkout.core.ui.model.BuddyUi

data class InviteUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    /** Display-ready invite link, e.g. `"commworkout.app/i/anurag"`. */
    val link: String = "",
    val copied: Boolean = false,
    /** Everyone who has already accepted an invite from this user. */
    val buddies: List<BuddyUi> = emptyList(),
)
