package com.example.buddyworkout.feature.challenge

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.buddyworkout.core.common.Ticker
import com.example.buddyworkout.core.common.formatCountdown
import com.example.buddyworkout.data.auth.AuthRepository
import com.example.buddyworkout.data.challenge.AlreadyJoined
import com.example.buddyworkout.data.challenge.ChallengeFull
import com.example.buddyworkout.data.challenge.ChallengeOver
import com.example.buddyworkout.data.challenge.ChallengeRepository
import com.example.buddyworkout.data.challenge.NewMember
import com.example.buddyworkout.data.user.UserProfile
import com.example.buddyworkout.data.user.UserRepository
import com.example.buddyworkout.data.challenge.ChallengeWithParticipants
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

private const val TAG = "ChallengeDetailViewModel"

/** How often the countdown redraws. The export shows seconds, so once a second. */
private const val TICK_MILLIS = 1_000L

@HiltViewModel
class ChallengeDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    authRepository: AuthRepository,
    private val challengeRepository: ChallengeRepository,
    private val userRepository: UserRepository,
    ticker: Ticker,
) : ViewModel() {

    private val uid = authRepository.currentUser?.uid.orEmpty()
    /**
     * Read by argument name rather than `toRoute`, which cannot rebuild a route
     * from a plain SavedStateHandle and hands back a null argument in a unit
     * test. The route class still defines the contract; only the extraction
     * differs, and this keeps the ViewModel testable.
     */
    private val challengeId: String =
        requireNotNull(savedStateHandle["id"]) { "ChallengeDetail route has no id" }

    private val _state = MutableStateFlow(ChallengeDetailUiState(isLoading = true))
    val state: StateFlow<ChallengeDetailUiState> = _state.asStateFlow()

    private val _cancelled = Channel<Unit>(Channel.BUFFERED)
    val cancelled: Flow<Unit> = _cancelled.receiveAsFlow()

    init {
        viewModelScope.launch {
            // The document and the clock are combined so the countdown ticks
            // without re-reading Firestore every second.
            combine(challengeRepository.observeChallenge(challengeId), ticker.seconds()) { entry, now ->
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
            memberCount = entry.challenge.memberUids.size,
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

    fun onOpenAddBuddy() = _state.update { it.copy(addBuddy = AddBuddyUiState(isOpen = true)) }

    fun onDismissAddBuddy() = _state.update { it.copy(addBuddy = AddBuddyUiState()) }

    fun onAddBuddyEmailChange(value: String) = _state.update {
        // Clear the previous match: the address on screen and the person shown
        // under it must never disagree.
        it.copy(addBuddy = it.addBuddy.copy(email = value, found = null, error = null))
    }

    fun onSearchBuddy() {
        val form = _state.value.addBuddy
        if (!form.canSearch) return
        updateAddBuddy { it.copy(isSearching = true, error = null, found = null) }

        viewModelScope.launch {
            userRepository.findByEmail(form.email).fold(
                onSuccess = { profile ->
                    val problem = profile.rejectionReason()
                    updateAddBuddy {
                        it.copy(
                            isSearching = false,
                            error = problem,
                            found = if (problem == null && profile != null) {
                                FoundBuddyUi(
                                    uid = profile.uid,
                                    name = profile.displayName.ifBlank { profile.email },
                                    email = profile.email,
                                    photoUrl = profile.photoUrl,
                                )
                            } else {
                                null
                            },
                        )
                    }
                },
                onFailure = { cause ->
                    Log.w(TAG, "Buddy lookup failed", cause)
                    updateAddBuddy {
                        it.copy(isSearching = false, error = "Couldn't look that up. Try again.")
                    }
                },
            )
        }
    }

    fun onConfirmAddBuddy() {
        val found = _state.value.addBuddy.found ?: return
        updateAddBuddy { it.copy(isSearching = true, error = null) }

        viewModelScope.launch {
            challengeRepository.addMember(
                challengeId,
                NewMember(found.uid, found.name, found.photoUrl),
            ).fold(
                // The listener delivers the new member, so the sheet just
                // closes rather than editing the leaderboard itself.
                onSuccess = { _state.update { it.copy(addBuddy = AddBuddyUiState()) } },
                onFailure = { cause ->
                    Log.w(TAG, "Add member failed", cause)
                    updateAddBuddy { it.copy(isSearching = false, error = cause.addMessage()) }
                },
            )
        }
    }

    private fun UserProfile?.rejectionReason(): String? = when {
        this == null -> "No account uses that email."
        uid == this@ChallengeDetailViewModel.uid -> "That's you — you're already in."
        uid in _state.value.leaderboard.map { it.uid } -> "They're already in this challenge."
        else -> null
    }

    private fun updateAddBuddy(block: (AddBuddyUiState) -> AddBuddyUiState) =
        _state.update { it.copy(addBuddy = block(it.addBuddy)) }
}

private fun Throwable.addMessage(): String = when (this) {
    is ChallengeFull -> "This challenge is full."
    is AlreadyJoined -> "They're already in this challenge."
    is ChallengeOver -> "This challenge has ended."
    else -> "Couldn't add them. Check your connection and try again."
}
