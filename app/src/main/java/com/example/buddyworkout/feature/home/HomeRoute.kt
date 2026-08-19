package com.example.buddyworkout.feature.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/** Stateful wrapper around [HomeScreen]. */
@Composable
fun HomeRoute(
    onCreateChallenge: () -> Unit,
    onInviteBuddies: () -> Unit,
    onChallengeClick: (String) -> Unit,
    onNotifications: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    HomeScreen(
        state = state,
        onCreateChallenge = onCreateChallenge,
        onInviteBuddies = onInviteBuddies,
        onChallengeClick = onChallengeClick,
        onNotifications = onNotifications,
    )
}
