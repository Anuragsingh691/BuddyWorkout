package com.example.buddyworkout.feature.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.buddyworkout.BuildConfig
import com.example.buddyworkout.data.auth.clearGoogleCredentialState

/** Stateful wrapper around [ProfileScreen]. */
@Composable
fun ProfileRoute(
    onSignedOut: () -> Unit,
    onOpenGallery: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.signedOut.collect { onSignedOut() }
    }

    ProfileScreen(
        // The component gallery is a debug-build affordance, not app state.
        state = state.copy(showGallery = BuildConfig.DEBUG),
        onSignOut = { viewModel.onSignOut { clearGoogleCredentialState(context) } },
        onOpenGallery = onOpenGallery,
    )
}
