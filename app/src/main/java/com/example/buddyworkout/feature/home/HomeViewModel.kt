package com.example.buddyworkout.feature.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.buddyworkout.core.ui.component.AvatarUi
import com.example.buddyworkout.data.auth.AuthRepository
import com.example.buddyworkout.data.challenge.ChallengeRepository
import com.example.buddyworkout.data.user.UserRepository
import com.example.buddyworkout.feature.challenge.toChallengeUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "HomeViewModel"

@HiltViewModel
class HomeViewModel @Inject constructor(
    authRepository: AuthRepository,
    userRepository: UserRepository,
    challengeRepository: ChallengeRepository,
) : ViewModel() {

    private val uid = authRepository.currentUser?.uid.orEmpty()

    private val _state = MutableStateFlow(
        HomeUiState(greeting = greetingFor(authRepository.currentUser?.displayName)),
    )
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            userRepository.observeProfile()
                .catch { Log.w(TAG, "Profile listen failed", it) }
                .collect { profile ->
                    if (profile == null) return@collect
                    _state.update {
                        it.copy(
                            greeting = greetingFor(profile.displayName),
                            avatar = AvatarUi(initialsOf(profile.displayName), key = profile.uid),
                        )
                    }
                }
        }
        viewModelScope.launch {
            challengeRepository.observeMyChallenges()
                .catch { cause ->
                    Log.w(TAG, "Challenge listen failed", cause)
                    _state.update { it.copy(isLoading = false, error = "Couldn't load your challenges.") }
                }
                .collect { challenges ->
                    // `now` is read per emission rather than held: a card's
                    // "ends in 3 days" is only as fresh as its last update.
                    val now = System.currentTimeMillis()
                    _state.update { current ->
                        current.copy(
                            isLoading = false,
                            error = null,
                            activeChallenges = challenges
                                .filterNot { it.challenge.status.isOver }
                                .map { it.toChallengeUi(uid, now) },
                        )
                    }
                }
        }
    }
}

/** e.g. `"Hi, Anurag 👋"` — the app-bar title. */
private fun greetingFor(displayName: String?): String {
    val first = displayName?.trim()?.substringBefore(' ')?.takeIf { it.isNotBlank() }
    return if (first == null) "Hi there 👋" else "Hi, $first 👋"
}

private fun initialsOf(name: String): String = name
    .trim()
    .split(Regex("\\s+"))
    .filter { it.isNotBlank() }
    .take(2)
    .map { it.first().uppercaseChar() }
    .joinToString("")
