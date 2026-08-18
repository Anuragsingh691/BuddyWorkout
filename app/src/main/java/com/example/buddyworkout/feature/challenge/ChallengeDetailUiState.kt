package com.example.buddyworkout.feature.challenge

import com.example.buddyworkout.core.ui.component.AvatarUi
import com.example.buddyworkout.core.ui.model.ParticipantUi

data class ChallengeDetailUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val title: String = "Pushup challenge",
    /** e.g. `"2d 14h left"` or `"Ended"` — produced by `formatRemaining`. */
    val remaining: String = "",
    /** e.g. `"Mon 18 Aug, 06:00"`. */
    val startsAt: String = "",
    val endsAt: String = "",
    val members: List<AvatarUi> = emptyList(),
    val leaderboard: List<ParticipantUi> = emptyList(),
    val isCreator: Boolean = false,
    val isCompleted: Boolean = false,
)
