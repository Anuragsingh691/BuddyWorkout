package com.example.buddyworkout.feature.challenge

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/** Stateful wrapper around [ChallengeDetailScreen]. */
@Composable
fun ChallengeDetailRoute(
    onBack: () -> Unit,
    onRecordWorkout: () -> Unit,
    onSeeWinner: () -> Unit,
    onCancelChallenge: () -> Unit,
    viewModel: ChallengeDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ChallengeDetailScreen(
        state = state,
        onBack = onBack,
        onRecordWorkout = onRecordWorkout,
        onSeeWinner = onSeeWinner,
        onCancelChallenge = onCancelChallenge,
    )
}
