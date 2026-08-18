package com.example.buddyworkout.feature.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.buddyworkout.data.auth.AuthRepository
import com.example.buddyworkout.data.user.UserRepository
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

private const val TAG = "RegisterViewModel"

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterUiState())
    val state: StateFlow<RegisterUiState> = _state.asStateFlow()

    private val _registered = Channel<Unit>(Channel.BUFFERED)
    val registered: Flow<Unit> = _registered.receiveAsFlow()

    fun onNameChange(value: String) = _state.update { it.copy(name = value) }

    fun onEmailChange(value: String) = _state.update { it.copy(email = value) }

    fun onPasswordChange(value: String) = _state.update { it.copy(password = value) }

    fun onPhoneChange(value: String) = _state.update { it.copy(phone = value) }

    /** Null when the user backed out of the photo picker. */
    fun onPhotoSelected(uri: String?) = _state.update { it.copy(photoUri = uri) }

    fun onCreateAccount() {
        val form = _state.value
        // `canSubmit` already enforces the 6-character minimum, so a short
        // password never reaches the network.
        if (!form.canSubmit) return
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            authRepository.register(form.name, form.email, form.password)
                // Only once the account exists is there a uid to write under,
                // or an authenticated caller the rules will accept.
                .mapCatching { userRepository.ensureProfile(form.name, form.phone).getOrThrow() }
                .fold(
                    onSuccess = {
                        // After the profile write, and never fatal: a photo that
                        // fails to upload costs the avatar, not the account. It
                        // is logged rather than swallowed — silence is how a
                        // decoder that returned null for every image went
                        // unnoticed until someone tried it on a real phone.
                        form.photoUri?.let { uri ->
                            userRepository.saveAvatar(uri).onFailure { cause ->
                                Log.w(TAG, "Avatar write failed", cause)
                            }
                        }
                        _state.update { it.copy(isLoading = false, error = null) }
                        _registered.send(Unit)
                    },
                    onFailure = { cause ->
                        _state.update { it.copy(isLoading = false, error = cause.userMessage()) }
                    },
                )
        }
    }
}
