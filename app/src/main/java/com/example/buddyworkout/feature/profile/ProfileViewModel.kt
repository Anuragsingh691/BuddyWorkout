package com.example.buddyworkout.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.buddyworkout.core.ui.component.AvatarUi
import com.example.buddyworkout.data.auth.AuthRepository
import com.example.buddyworkout.data.auth.AuthUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    /**
     * Read once at construction: Auth identity only changes across a sign-in,
     * and signing out leaves this screen immediately. The stats card stays
     * empty until challenge data exists — it is not an auth concern.
     */
    private val _state = MutableStateFlow(authRepository.currentUser.toUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    private val _signedOut = Channel<Unit>(Channel.BUFFERED)
    val signedOut: Flow<Unit> = _signedOut.receiveAsFlow()

    /**
     * [clearCredentialState] is supplied by the call site: without it Google
     * silently re-authenticates the same account on the next attempt, so
     * signing out would not actually let you switch users.
     */
    fun onSignOut(clearCredentialState: suspend () -> Unit) {
        if (_state.value.isLoading) return
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            authRepository.signOut()
            clearCredentialState()
            _state.update { it.copy(isLoading = false) }
            _signedOut.send(Unit)
        }
    }
}

private fun AuthUser?.toUiState(): ProfileUiState {
    // Google accounts always carry a display name; email sign-ups have one only
    // after registration set it, so the email is the fallback label.
    val label = this?.displayName ?: this?.email.orEmpty()
    return ProfileUiState(
        name = label,
        email = this?.email.orEmpty(),
        avatar = AvatarUi(initials = initialsOf(label), key = this?.uid ?: label),
    )
}

/** First letter of each of the first two words, e.g. "Anurag S." -> "AS". */
private fun initialsOf(label: String): String = label
    .trim()
    .split(Regex("\\s+"))
    .filter { it.isNotBlank() }
    .take(2)
    .map { it.first().uppercaseChar() }
    .joinToString("")
