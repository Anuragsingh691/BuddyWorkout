package com.example.buddyworkout.feature.result

import com.example.buddyworkout.core.ui.model.ParticipantUi

data class WinnerUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val title: String = "Pushup challenge",
    /** e.g. `"Anurag wins!"`. */
    val winnerName: String = "",
    /** e.g. `"203 pushups · most reps"`. */
    val detail: String = "",
    val leaderboard: List<ParticipantUi> = emptyList(),
    /** e.g. `"54 pushups"`. */
    val bestSession: String = "",
    /** e.g. `"6"`. */
    val sessionsLogged: String = "",
    /** e.g. `"92%"`. */
    val avgFormScore: String = "",
)
