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
    val selected: List<BuddyUi> get() = buddies.filter { it.selected }
    val selectedCount: Int get() = selected.size
    val atLimit: Boolean get() = selectedCount >= MAX_BUDDIES

    /** e.g. `"2 / 3"` — the pill in the app bar. */
    val countLabel: String get() = "$selectedCount / $MAX_BUDDIES"

    /** e.g. `"Search 48 buddies"`. */
    val searchHint: String get() = "Search ${buddies.size} buddies"

    /** e.g. `"Add 2 buddies"`. */
    val doneLabel: String
        get() = when (selectedCount) {
            0 -> "Add buddies"
            1 -> "Add 1 buddy"
            else -> "Add $selectedCount buddies"
        }
}
