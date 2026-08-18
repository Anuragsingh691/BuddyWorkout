package com.example.buddyworkout.feature.challenge.create

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Stateful wrapper around [CreateChallengeScreen].
 *
 * The ViewModel is passed in rather than resolved here: it is scoped to the
 * create graph's back stack entry so all three screens of the flow share one,
 * and only the NavHost knows that entry.
 */
@Composable
fun CreateChallengeRoute(
    viewModel: CreateChallengeViewModel,
    onCreated: (String) -> Unit,
    onPickBuddies: () -> Unit,
    onPickWindow: () -> Unit,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.created.collect { onCreated(it) }
    }

    CreateChallengeScreen(
        state = state,
        onBack = onBack,
        onPickBuddies = onPickBuddies,
        onSelectDuration = viewModel::onSelectDuration,
        onPickStart = onPickWindow,
        onPickEnd = onPickWindow,
        onCreate = viewModel::onCreate,
    )
}
