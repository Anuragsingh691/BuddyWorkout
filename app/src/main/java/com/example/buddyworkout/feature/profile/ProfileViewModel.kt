package com.example.buddyworkout.feature.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.ui.graphics.asImageBitmap
import com.example.buddyworkout.core.common.decodeAvatar
import com.example.buddyworkout.core.ui.component.AvatarUi
import com.example.buddyworkout.data.auth.AuthRepository
import com.example.buddyworkout.data.auth.AuthUser
import com.example.buddyworkout.data.user.UserProfile
import com.example.buddyworkout.data.user.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "ProfileViewModel"

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    userRepository: UserRepository,
) : ViewModel() {

    /**
     * Seeded from Auth so the screen has a name to draw immediately, then
     * replaced by the Firestore document once it arrives. Auth is the faster
     * of the two and knows less; the document is the record.
     */
    private val _state = MutableStateFlow(authRepository.currentUser.toUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    private val _signedOut = Channel<Unit>(Channel.BUFFERED)
    val signedOut: Flow<Unit> = _signedOut.receiveAsFlow()

    init {
        viewModelScope.launch {
            userRepository.observeAvatar()
                .catch { Log.w(TAG, "Avatar listen failed", it) }
                .collect { bytes ->
                    val photo = bytes?.let { decodeAvatar(it)?.asImageBitmap() }
                    _state.update { it.copy(avatar = it.avatar.copy(photo = photo)) }
                }
        }
        viewModelScope.launch {
            userRepository.observeProfile()
                // A rejected listen must not take the app down. Firestore
                // delivers one whenever rules refuse the read — including on
                // sign-out, when the listen is re-evaluated with no auth. The
                // screen keeps the identity Auth already gave it and says so.
                .catch { cause ->
                    Log.w(TAG, "Profile listen failed", cause)
                    _state.update { it.copy(error = "Couldn't load your profile.") }
                }
                .collect { profile ->
                    if (profile != null) _state.update { it.merge(profile) }
                }
        }
    }

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

private fun ProfileUiState.merge(profile: UserProfile) = copy(
    name = profile.displayName.ifBlank { name },
    email = profile.email.ifBlank { email },
    avatar = avatar.copy(
        initials = initialsOf(profile.displayName.ifBlank { name }),
        key = profile.uid,
    ),
    stats = listOf(
        StatUi("Phone", profile.phone ?: "Not set"),
    ),
)

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
