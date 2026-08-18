package com.example.buddyworkout.feature.challenge

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/** Stateful wrapper around [ChallengesScreen]. */
@Composable
fun ChallengesRoute(
    onChallengeClick: (String) -> Unit,
    onCreateChallenge: () -> Unit,
    viewModel: ChallengesViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ChallengesScreen(
        state = state,
        onTabSelect = viewModel::onTabSelect,
        onChallengeClick = onChallengeClick,
        onCreateChallenge = onCreateChallenge,
    )
}
