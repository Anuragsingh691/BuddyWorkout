package com.example.buddyworkout.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.buddyworkout.data.auth.AuthError
import com.example.buddyworkout.data.auth.AuthException
import com.example.buddyworkout.data.auth.AuthRepository
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
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    private val _signedIn = Channel<Unit>(Channel.BUFFERED)

    /**
     * One-shot, not a state flag: the session gate captures its start
     * destination once, so sign-in navigates explicitly — and a state flag
     * would re-fire that navigation after a configuration change.
     */
    val signedIn: Flow<Unit> = _signedIn.receiveAsFlow()

    fun onEmailChange(value: String) = _state.update { it.copy(email = value) }

    fun onPasswordChange(value: String) = _state.update { it.copy(password = value) }

    fun onSignIn() {
        val form = _state.value
        if (!form.canSubmit) return
        // Set before launching, not inside the coroutine: the guard above is
        // what stops a double tap, and it only works if the flag is already up
        // by the time the second tap arrives.
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            complete(authRepository.signIn(form.email, form.password))
        }
    }

    /**
     * [getIdToken] is supplied by the call site because Credential Manager
     * needs an Activity context, which must not reach a ViewModel.
     */
    fun onGoogleSignIn(getIdToken: suspend () -> Result<String>) {
        if (_state.value.isLoading) return
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            complete(
                getIdToken().fold(
                    onSuccess = { authRepository.signInWithGoogleIdToken(it) },
                    onFailure = { Result.failure(it) },
                )
            )
        }
    }

    private suspend fun complete(result: Result<Unit>) = result.fold(
        onSuccess = {
            _state.update { it.copy(isLoading = false, error = null) }
            _signedIn.send(Unit)
        },
        onFailure = { cause ->
            _state.update { it.copy(isLoading = false, error = cause.userMessage()) }
        },
    )
}

/** A dismissed Google sheet maps to null, which the screens render as no error. */
internal fun Throwable.userMessage(): String? =
    ((this as? AuthException)?.error ?: AuthError.Unknown).message()
