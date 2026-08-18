package com.example.buddyworkout.feature.result

import com.example.buddyworkout.core.ui.model.ParticipantUi

data class WinnerUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val winnerName: String = "",
    /** e.g. `"204 reps over 3 days"`. */
    val detail: String = "",
    val leaderboard: List<ParticipantUi> = emptyList(),
)
