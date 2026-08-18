package com.example.buddyworkout.feature.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/** Stateful wrapper around [RegisterScreen]. */
@Composable
fun RegisterRoute(
    onRegistered: () -> Unit,
    onSignInClick: () -> Unit,
    onBack: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.registered.collect { onRegistered() }
    }

    RegisterScreen(
        state = state,
        onNameChange = viewModel::onNameChange,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onCreateAccount = viewModel::onCreateAccount,
        onSignInClick = onSignInClick,
        onBack = onBack,
    )
}
