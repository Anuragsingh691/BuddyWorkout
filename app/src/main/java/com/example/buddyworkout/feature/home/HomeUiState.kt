package com.example.buddyworkout.feature.home

import com.example.buddyworkout.core.ui.model.ChallengeUi

data class HomeUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    /** e.g. `"Hey, Anurag"`. */
    val greeting: String = "",
    val activeChallenges: List<ChallengeUi> = emptyList(),
    /** True at the 2-active-challenge cap; creating another is blocked. */
    val atChallengeLimit: Boolean = false,
) {
    /** e.g. `"1 of 2"` — shown next to the section title. */
    val slotsLabel: String get() = "${activeChallenges.size} of 2"
}
