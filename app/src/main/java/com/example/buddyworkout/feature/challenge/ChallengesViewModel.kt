package com.example.buddyworkout.feature.challenge

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.buddyworkout.data.auth.AuthRepository
import com.example.buddyworkout.data.challenge.ChallengeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "ChallengesViewModel"

@HiltViewModel
class ChallengesViewModel @Inject constructor(
    authRepository: AuthRepository,
    challengeRepository: ChallengeRepository,
) : ViewModel() {

    private val uid = authRepository.currentUser?.uid.orEmpty()

    private val _state = MutableStateFlow(ChallengesUiState(isLoading = true))
    val state: StateFlow<ChallengesUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            challengeRepository.observeMyChallenges()
                .catch { cause ->
                    Log.w(TAG, "Challenge listen failed", cause)
                    _state.update { it.copy(isLoading = false, error = "Couldn't load your challenges.") }
                }
                .collect { challenges ->
                    val now = System.currentTimeMillis()
                    val (over, running) = challenges.partition { it.challenge.status.isOver }
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = null,
                            active = running.map { entry -> entry.toChallengeUi(uid, now) },
                            completed = over.map { entry -> entry.toChallengeUi(uid, now) },
                        )
                    }
                }
        }
    }

    fun onTabSelect(index: Int) = _state.update { it.copy(selectedTab = index) }
}
