package com.example.buddyworkout.core.common

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Counts the blocking operations in flight, so one app-wide loader can show
 * while any of them runs.
 *
 * A counter rather than a flag because operations overlap and nest:
 * registration is three commands in a row, and signing out is a repository
 * call plus clearing the Credential Manager state. With a boolean, whichever
 * finished first would clear the loader while the rest were still running.
 *
 * Deliberately wraps *commands* only. Firestore snapshot listeners never
 * complete, so anything driven by one would leave this stuck on forever.
 */
@Singleton
class BusyTracker @Inject constructor() {

    private val count = AtomicInteger(0)
    private val _isBusy = MutableStateFlow(false)
    val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

    suspend fun <T> track(block: suspend () -> T): T {
        enter()
        // `finally`, not a success path: an exception or a cancelled coroutine
        // must still release. A leaked count leaves the app under a permanent
        // scrim, which is worse than the missing feedback this fixes.
        try {
            return block()
        } finally {
            exit()
        }
    }

    private fun enter() {
        count.incrementAndGet()
        _isBusy.update { true }
    }

    private fun exit() {
        val remaining = count.decrementAndGet()
        if (remaining <= 0) _isBusy.update { false }
    }
}

/**
 * [BusyTracker.track] wrapped in a `runCatching`, for repositories that return
 * a `Result` rather than throwing.
 *
 * Cancellation is rethrown rather than captured as a failure: a cancelled
 * ViewModel scope is not a failed write, and swallowing it would report one.
 */
suspend fun <T> BusyTracker.trackCatching(block: suspend () -> T): Result<T> = try {
    Result.success(track(block))
} catch (e: kotlin.coroutines.cancellation.CancellationException) {
    throw e
} catch (e: Throwable) {
    Result.failure(e)
}
