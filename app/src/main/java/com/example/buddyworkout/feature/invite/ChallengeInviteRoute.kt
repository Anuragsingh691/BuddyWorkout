package com.example.buddyworkout.feature.invite

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/** Stateful wrapper around [ChallengeInviteScreen]. */
@Composable
fun ChallengeInviteRoute(
    onJoined: (String) -> Unit,
    onDecline: () -> Unit,
    viewModel: ChallengeInviteViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.joined.collect { onJoined(it) }
    }

    ChallengeInviteScreen(
        state = state,
        onAccept = viewModel::onAccept,
        onDecline = onDecline,
    )
}
