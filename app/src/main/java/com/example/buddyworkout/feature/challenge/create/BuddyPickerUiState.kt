package com.example.buddyworkout.feature.challenge.create

import com.example.buddyworkout.core.ui.model.BuddyUi

/** A challenge holds 2–4 members including the creator, so at most 3 buddies. */
const val MAX_BUDDIES = 3

data class BuddyPickerUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val query: String = "",
    val buddies: List<BuddyUi> = emptyList(),
) {
    val selectedCount: Int get() = buddies.count { it.selected }
    val atLimit: Boolean get() = selectedCount >= MAX_BUDDIES
    val doneLabel: String get() = if (selectedCount == 0) "Done" else "Done ($selectedCount)"
}
