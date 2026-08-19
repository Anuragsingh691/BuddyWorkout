package com.example.buddyworkout.feature.auth

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Stateful wrapper around [RegisterScreen]. The photo picker is launched from
 * here rather than from the screen: `rememberLauncherForActivityResult` needs
 * an ActivityResultRegistryOwner, which `@Preview` does not provide.
 */
@Composable
fun RegisterRoute(
    onRegistered: () -> Unit,
    onBack: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        // Null when the picker was dismissed — leave any earlier pick in place.
        if (uri != null) viewModel.onPhotoSelected(uri.toString())
    }

    LaunchedEffect(Unit) {
        viewModel.registered.collect { onRegistered() }
    }

    RegisterScreen(
        state = state,
        onNameChange = viewModel::onNameChange,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onPhoneChange = viewModel::onPhoneChange,
        onPickPhoto = {
            photoPicker.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
            )
        },
        onCreateAccount = viewModel::onCreateAccount,
        onBack = onBack,
    )
}
