package com.example.buddyworkout.core.navigation

import com.example.buddyworkout.core.common.BusyTracker
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Holds the session state the navigation gate reads. Deliberately the only
 * ViewModel above the NavHost: screens get their own later.
 */
@HiltViewModel
class AppViewModel @Inject constructor(
    sessionSource: SessionSource,
    busyTracker: BusyTracker,
) : ViewModel() {

    /** True while any command is in flight; drives the app-wide loader. */
    val isBusy: StateFlow<Boolean> = busyTracker.isBusy

    val state: StateFlow<AuthState> = sessionSource.authState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AuthState.Loading,
    )
}
