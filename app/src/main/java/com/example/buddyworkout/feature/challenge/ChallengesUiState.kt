package com.example.buddyworkout.feature.challenge

import com.example.buddyworkout.core.ui.model.ChallengeUi

data class ChallengesUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    /** 0 = Active, 1 = Completed. */
    val selectedTab: Int = 0,
    val active: List<ChallengeUi> = emptyList(),
    val completed: List<ChallengeUi> = emptyList(),
) {
    val visible: List<ChallengeUi> get() = if (selectedTab == 0) active else completed

    val emptyMessage: String
        get() = if (selectedTab == 0) {
            "No active challenges. Create one from Home."
        } else {
            "Nothing finished yet — your completed challenges will land here."
        }
}
