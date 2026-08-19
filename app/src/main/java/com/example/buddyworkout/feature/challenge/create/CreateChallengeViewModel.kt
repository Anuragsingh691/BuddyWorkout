package com.example.buddyworkout.feature.challenge.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.buddyworkout.core.common.formatWindowLabel
import com.example.buddyworkout.data.challenge.ChallengeLimitReached
import com.example.buddyworkout.data.challenge.ChallengeRepository
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

/**
 * Shared by all three screens of the create flow, scoped to the `CreateGraph`
 * nested graph — which is the reason that graph exists (spec §2.5). It replaces
 * the UI-local state the NavHost was holding.
 */
@HiltViewModel
class CreateChallengeViewModel @Inject constructor(
    private val challengeRepository: ChallengeRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(CreateChallengeUiState())
    val state: StateFlow<CreateChallengeUiState> = _state.asStateFlow()

    private val _created = Channel<String>(Channel.BUFFERED)

    /** Emits the new challenge's id, so the caller can open it. */
    val created: Flow<String> = _created.receiveAsFlow()

    init {
        onSelectDuration(DEFAULT_DURATION)
    }

    fun onSelectDuration(index: Int) {
        val duration = ChallengeDuration.PRESETS.getOrNull(index) ?: return
        val window = duration.windowFrom(System.currentTimeMillis())
        _state.update {
            it.copy(
                selectedDurationIndex = index,
                startLabel = formatWindowLabel(window.startMillis),
                endLabel = formatWindowLabel(window.endMillis),
                durationLabel = duration.summary,
                error = null,
            )
        }
    }

    fun onCreate() {
        val form = _state.value
        if (!form.canCreate) return
        val duration = ChallengeDuration.PRESETS.getOrNull(form.selectedDurationIndex) ?: return

        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            // The window is recomputed at submit rather than reused from the
            // form: the user may have sat on this screen for a while, and a
            // challenge should start when it is created, not when it was typed.
            val window = duration.windowFrom(System.currentTimeMillis())
            challengeRepository.createChallenge(window).fold(
                onSuccess = { id ->
                    _state.update { it.copy(isLoading = false, error = null) }
                    _created.send(id)
                },
                onFailure = { cause ->
                    _state.update { it.copy(isLoading = false, error = cause.createMessage()) }
                },
            )
        }
    }
}

/** The export shows 12 hours preselected. */
private const val DEFAULT_DURATION = 1

private fun Throwable.createMessage(): String = when (this) {
    is ChallengeLimitReached ->
        "You're already in 2 active challenges. Finish or cancel one first."
    else -> "Couldn't create the challenge. Check your connection and try again."
}
