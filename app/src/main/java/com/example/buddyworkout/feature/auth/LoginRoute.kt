package com.example.buddyworkout.feature.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.buddyworkout.data.auth.getGoogleIdToken

/**
 * Stateful wrapper around [LoginScreen]. Navigation is driven by the
 * ViewModel's one-shot event rather than by the tap, so the app only leaves
 * this screen once Firebase has actually accepted the credentials.
 */
@Composable
fun LoginRoute(
    onSignedIn: () -> Unit,
    onRegisterClick: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    // Credential Manager needs the Activity context, which is why the token
    // request is built here and handed to the ViewModel as a lambda.
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.signedIn.collect { onSignedIn() }
    }

    LoginScreen(
        state = state,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onSignIn = viewModel::onSignIn,
        onGoogleSignIn = { viewModel.onGoogleSignIn { getGoogleIdToken(context) } },
        onRegisterClick = onRegisterClick,
    )
}
