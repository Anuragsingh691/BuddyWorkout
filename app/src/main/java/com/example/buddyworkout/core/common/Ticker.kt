package com.example.buddyworkout.core.common

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A clock that emits the current time, now and then again every second.
 *
 * Injected rather than written inline so it can be replaced in a test. The real
 * one never completes, and `runTest` drains the scheduler on the way out — an
 * endless loop on virtual time hangs the whole suite rather than failing it,
 * which is exactly what happened before this existed.
 */
@Singleton
open class Ticker @Inject constructor() {
    open fun seconds(): Flow<Long> = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(INTERVAL_MILLIS)
        }
    }

    private companion object {
        /** The export's countdown shows seconds, so once a second. */
        const val INTERVAL_MILLIS = 1_000L
    }
}
