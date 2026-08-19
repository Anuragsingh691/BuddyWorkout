package com.example.buddyworkout.feature.challenge

import com.example.buddyworkout.core.ui.model.ChallengeUi
import com.example.buddyworkout.data.challenge.MAX_ACTIVE_CHALLENGES

data class ChallengesUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    /** 0 = Active, 1 = Past. */
    val selectedTab: Int = 0,
    val active: List<ChallengeUi> = emptyList(),
    val completed: List<ChallengeUi> = emptyList(),
) {
    val visible: List<ChallengeUi> get() = if (selectedTab == 0) active else completed

    /** e.g. `["Active · 2", "Past · 3"]`. */
    val tabLabels: List<String>
        get() = listOf("Active · ${active.size}", "Past · ${completed.size}")

    val atChallengeLimit: Boolean get() = active.size >= MAX_ACTIVE_CHALLENGES

    val limitMessage: String
        get() = "You're in ${active.size} of $MAX_ACTIVE_CHALLENGES active challenges. " +
            "Finish one to create a new one."

    val emptyMessage: String
        get() = if (selectedTab == 0) {
            "No active challenges. Create one from Home."
        } else {
            "Nothing finished yet — your completed challenges will land here."
        }
}
