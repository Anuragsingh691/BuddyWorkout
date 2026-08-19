package com.example.buddyworkout.feature.home

import com.example.buddyworkout.core.ui.component.AvatarUi
import com.example.buddyworkout.core.ui.model.ChallengeUi

data class HomeUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    /** Small line above the greeting in the app bar, e.g. `"Welcome back"`. */
    val overline: String = "Welcome back",
    /** e.g. `"Hi, Anurag 👋"` — the app-bar title. */
    val greeting: String = "",
    /** The signed-in user, shown as a disc at the end of the app bar. */
    val avatar: AvatarUi? = null,
    val activeChallenges: List<ChallengeUi> = emptyList(),
)
