package com.example.buddyworkout.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterUiState())
    val state: StateFlow<RegisterUiState> = _state.asStateFlow()

    private val _registered = Channel<Unit>(Channel.BUFFERED)
    val registered: Flow<Unit> = _registered.receiveAsFlow()

    fun onNameChange(value: String) = _state.update { it.copy(name = value) }

    fun onEmailChange(value: String) = _state.update { it.copy(email = value) }

    fun onPasswordChange(value: String) = _state.update { it.copy(password = value) }

    fun onCreateAccount() {
        val form = _state.value
        // `canSubmit` already enforces the 6-character minimum, so a short
        // password never reaches the network.
        if (!form.canSubmit) return
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            authRepository.register(form.name, form.email, form.password).fold(
                onSuccess = {
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
