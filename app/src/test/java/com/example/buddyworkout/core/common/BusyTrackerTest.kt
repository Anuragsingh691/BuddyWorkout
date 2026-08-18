package com.example.buddyworkout.core.common

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BusyTrackerTest {

    private val tracker = BusyTracker()

    @Test fun `idle to begin with`() = runTest {
        assertFalse(tracker.isBusy.value)
    }

    @Test fun `busy while the block runs, idle once it returns`() = runTest {
        val started = CompletableDeferred<Unit>()
        val finish = CompletableDeferred<Unit>()

        val job = launch {
            tracker.track {
                started.complete(Unit)
                finish.await()
            }
        }

        started.await()
        assertTrue(tracker.isBusy.value)

        finish.complete(Unit)
        job.join()
        assertFalse(tracker.isBusy.value)
    }

    @Test fun `the block's value is passed through`() = runTest {
        assertEquals("done", tracker.track { "done" })
    }

    @Test fun `overlapping operations stay busy until the last one ends`() = runTest {
        val firstDone = CompletableDeferred<Unit>()
        val secondDone = CompletableDeferred<Unit>()
        val both = CompletableDeferred<Unit>()

        val first = launch { tracker.track { firstDone.await() } }
        val second = launch { tracker.track { secondDone.await() } }
        both.complete(Unit)
        both.await()

        // The inner one finishing must not clear the loader while the outer
        // operation is still running — the whole reason this is a counter.
        firstDone.complete(Unit)
        first.join()
        assertTrue(tracker.isBusy.value)

        secondDone.complete(Unit)
        second.join()
        assertFalse(tracker.isBusy.value)
    }

    @Test fun `nesting unwinds correctly`() = runTest {
        tracker.track {
            assertTrue(tracker.isBusy.value)
            tracker.track { assertTrue(tracker.isBusy.value) }
            assertTrue(tracker.isBusy.value)
        }
        assertFalse(tracker.isBusy.value)
    }

    @Test fun `a thrown block still releases`() = runTest {
        runCatching { tracker.track { error("boom") } }

        // A leaked count leaves the app under a scrim forever, which is worse
        // than the missing feedback this exists to fix.
        assertFalse(tracker.isBusy.value)
    }

    @Test fun `the exception is not swallowed`() = runTest {
        val thrown = runCatching { tracker.track { error("boom") } }.exceptionOrNull()

        assertEquals("boom", thrown?.message)
    }

    @Test fun `a cancelled operation still releases`() = runTest {
        val started = CompletableDeferred<Unit>()
        val job = launch {
            tracker.track {
                started.complete(Unit)
                awaitCancellation()
            }
        }

        started.await()
        assertTrue(tracker.isBusy.value)

        job.cancel()
        job.join()
        assertFalse(tracker.isBusy.value)
    }
}
