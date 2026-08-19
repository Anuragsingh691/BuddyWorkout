package com.example.buddyworkout.feature.challenge

import com.example.buddyworkout.core.ui.component.AvatarUi
import com.example.buddyworkout.core.ui.model.ParticipantUi
import com.example.buddyworkout.data.challenge.MAX_MEMBERS

data class ChallengeDetailUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val title: String = "Pushup challenge",
    /** e.g. `"2d 14h left"` or `"Ended"` — produced by `formatRemaining`. */
    val remaining: String = "",
    val members: List<AvatarUi> = emptyList(),
    val leaderboard: List<ParticipantUi> = emptyList(),
    val isCreator: Boolean = false,
    val isCompleted: Boolean = false,
    /** Members already in, against the 4-person cap. */
    val memberCount: Int = 0,
    val addBuddy: AddBuddyUiState = AddBuddyUiState(),
) {
    /** No point sharing a link into a full or finished challenge. */
    val canAddBuddies: Boolean get() = !isCompleted && memberCount < MAX_MEMBERS
}

/**
 * The "add a buddy by email" sheet.
 *
 * Two steps on purpose: look the address up and show who it resolved to, then
 * confirm. Adding someone spends one of their two challenge slots without
 * asking them, so the creator should at least see the name first.
 */
data class AddBuddyUiState(
    val isOpen: Boolean = false,
    val email: String = "",
    val isSearching: Boolean = false,
    val found: FoundBuddyUi? = null,
    val error: String? = null,
) {
    val canSearch: Boolean get() = email.isNotBlank() && !isSearching
    val canAdd: Boolean get() = found != null && !isSearching
}

/** Somebody a search matched, shown before they are added. */
data class FoundBuddyUi(
    val uid: String,
    val name: String,
    val email: String,
    val photoUrl: String?,
)
