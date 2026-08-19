package com.example.buddyworkout.feature.invite

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.buddyworkout.core.common.formatWindowLabel
import com.example.buddyworkout.data.auth.AuthRepository
import com.example.buddyworkout.data.challenge.AlreadyJoined
import com.example.buddyworkout.data.challenge.ChallengeFull
import com.example.buddyworkout.data.challenge.ChallengeLimitReached
import com.example.buddyworkout.data.challenge.ChallengeOver
import com.example.buddyworkout.data.challenge.ChallengeRepository
import com.example.buddyworkout.data.challenge.ChallengeWithParticipants
import com.example.buddyworkout.data.challenge.MAX_ACTIVE_CHALLENGES
import com.example.buddyworkout.feature.challenge.toParticipantUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "ChallengeInviteViewModel"

@HiltViewModel
class ChallengeInviteViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    authRepository: AuthRepository,
    private val challengeRepository: ChallengeRepository,
) : ViewModel() {

    private val uid = authRepository.currentUser?.uid.orEmpty()

    /**
     * Read by argument name rather than `toRoute`, which cannot rebuild a route
     * from a plain SavedStateHandle and hands back a null argument in a unit
     * test. The route class still defines the contract; only the extraction
     * differs, and this keeps the ViewModel testable.
     */
    /** The link carries the challenge's id; there is no separate invite code. */
    private val challengeId: String =
        requireNotNull(savedStateHandle["code"]) { "ChallengeInvite route has no code" }

    private val _state = MutableStateFlow(ChallengeInviteUiState(isLoading = true))
    val state: StateFlow<ChallengeInviteUiState> = _state.asStateFlow()

    private val _joined = Channel<String>(Channel.BUFFERED)

    /** Emits the challenge id once membership is real. */
    val joined: Flow<String> = _joined.receiveAsFlow()

    init {
        viewModelScope.launch {
            // The viewer's own challenges are needed too: accepting spends one
            // of their slots, and the screen should say so before they tap
            // rather than fail afterwards.
            combine(
                challengeRepository.observeChallenge(challengeId),
                challengeRepository.observeMyChallenges(),
            ) { invite, mine -> invite to mine }
                .catch { cause ->
                    Log.w(TAG, "Invite listen failed", cause)
                    _state.update {
                        it.copy(isLoading = false, error = "Couldn't load this invite.")
                    }
                }
                .collect { (invite, mine) ->
                    val activeCount = mine.count {
                        !it.challenge.status.isOver &&
                            it.challenge.endAtMillis > System.currentTimeMillis()
                    }
                    _state.update { it.render(invite, activeCount) }
                }
        }
    }

    fun onAccept() {
        if (_state.value.isLoading) return
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            challengeRepository.joinChallenge(challengeId).fold(
                onSuccess = {
                    _state.update { it.copy(isLoading = false) }
                    _joined.send(challengeId)
                },
                onFailure = { cause ->
                    // Already a member is not a failure worth stopping on: the
                    // destination is the same either way.
                    if (cause is AlreadyJoined) {
                        _state.update { it.copy(isLoading = false) }
                        _joined.send(challengeId)
                    } else {
                        _state.update { it.copy(isLoading = false, error = cause.joinMessage()) }
                    }
                },
            )
        }
    }

    private fun ChallengeInviteUiState.render(
        invite: ChallengeWithParticipants?,
        activeCount: Int,
    ): ChallengeInviteUiState {
        if (invite == null) {
            return copy(isLoading = false, error = "This challenge no longer exists.")
        }
        val now = System.currentTimeMillis()
        val creator = invite.participants.firstOrNull { it.uid == invite.challenge.creatorUid }
        val inviter = creator?.displayName?.substringBefore(' ').orEmpty().ifBlank { "A buddy" }

        return copy(
            isLoading = false,
            error = null,
            headline = "$inviter invited you to a group pushup challenge",
            members = invite.toParticipantUi(uid, now).map { it.avatar },
            windowLabel = "${formatWindowLabel(invite.challenge.startAtMillis)} → " +
                formatWindowLabel(invite.challenge.endAtMillis),
            blockedByLimit = activeCount >= MAX_ACTIVE_CHALLENGES &&
                uid !in invite.challenge.memberUids,
        )
    }
}

private fun Throwable.joinMessage(): String = when (this) {
    is ChallengeLimitReached ->
        "You're already in 2 active challenges. Finish or cancel one first."
    is ChallengeFull -> "This challenge is full."
    is ChallengeOver -> "This challenge has already ended."
    else -> "Couldn't join. Check your connection and try again."
}
