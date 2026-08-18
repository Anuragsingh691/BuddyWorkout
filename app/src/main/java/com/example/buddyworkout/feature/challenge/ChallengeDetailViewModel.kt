package com.example.buddyworkout.feature.challenge

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.buddyworkout.core.common.formatCountdown
import com.example.buddyworkout.core.navigation.ChallengeDetail
import com.example.buddyworkout.data.auth.AuthRepository
import com.example.buddyworkout.data.challenge.ChallengeRepository
import com.example.buddyworkout.data.challenge.ChallengeWithParticipants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import androidx.navigation.toRoute
import javax.inject.Inject

private const val TAG = "ChallengeDetailViewModel"

/** How often the countdown redraws. The export shows seconds, so once a second. */
private const val TICK_MILLIS = 1_000L

@HiltViewModel
class ChallengeDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    authRepository: AuthRepository,
    private val challengeRepository: ChallengeRepository,
) : ViewModel() {

    private val uid = authRepository.currentUser?.uid.orEmpty()
    private val challengeId = savedStateHandle.toRoute<ChallengeDetail>().id

    private val _state = MutableStateFlow(ChallengeDetailUiState(isLoading = true))
    val state: StateFlow<ChallengeDetailUiState> = _state.asStateFlow()

    private val _cancelled = Channel<Unit>(Channel.BUFFERED)
    val cancelled: Flow<Unit> = _cancelled.receiveAsFlow()

    init {
        viewModelScope.launch {
            // The document and the clock are combined so the countdown ticks
            // without re-reading Firestore every second.
            combine(challengeRepository.observeChallenge(challengeId), ticker()) { entry, now ->
                entry to now
            }
                .catch { cause ->
                    Log.w(TAG, "Challenge listen failed", cause)
                    _state.update { it.copy(isLoading = false, error = "Couldn't load this challenge.") }
                }
                .collect { (entry, now) -> _state.update { it.render(entry, now) } }
        }
    }

    private fun ChallengeDetailUiState.render(
        entry: ChallengeWithParticipants?,
        now: Long,
    ): ChallengeDetailUiState {
        if (entry == null) {
            return copy(isLoading = false, error = "This challenge no longer exists.")
        }
        val over = entry.challenge.status.isOver
        return copy(
            isLoading = false,
            error = null,
            title = entry.challenge.title,
            remaining = if (over) "Ended" else formatCountdown(entry.challenge.endAtMillis - now),
            members = entry.ranked.map { person ->
                entry.toParticipantUi(uid, now).first { it.uid == person.uid }.avatar
            },
            leaderboard = entry.toParticipantUi(uid, now),
            isCreator = entry.challenge.creatorUid == uid,
            isCompleted = over,
        )
    }

    fun onCancelChallenge() {
        if (_state.value.isLoading) return
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            challengeRepository.cancelChallenge(challengeId).fold(
                onSuccess = {
                    // No navigation and no local status edit: the listener is
                    // still open, so the cancelled document arrives on its own
                    // and re-renders the screen as ended.
                    _state.update { it.copy(isLoading = false) }
                    _cancelled.send(Unit)
                },
                onFailure = {
                    Log.w(TAG, "Cancel failed", it)
                    _state.update {
                        it.copy(isLoading = false, error = "Couldn't cancel the challenge.")
                    }
                },
            )
        }
    }
}

/** Emits the current time immediately, then once a second. */
private fun ticker(): Flow<Long> = flow {
    while (true) {
        emit(System.currentTimeMillis())
        delay(TICK_MILLIS)
    }
}