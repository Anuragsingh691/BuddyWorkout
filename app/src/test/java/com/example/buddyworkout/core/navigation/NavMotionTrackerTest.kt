package com.example.buddyworkout.core.navigation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The scrim has to darken the outgoing screen on a push and lighten the
 * returning one on a pop, so it needs to know which of the two is happening.
 * Deciding that from back-stack *depth* is the obvious approach and it is
 * wrong — see the `popUpTo(0)` cases below.
 */
class NavMotionTrackerTest {

    private val home = Home::class.qualifiedName
    private val challenges = Challenges::class.qualifiedName
    private val login = Login::class.qualifiedName
    private val detail = "${ChallengeDetail::class.qualifiedName}/{id}"

    private val tracker = NavMotionTracker()

    @Test fun `the first destination is not a pop`() =
        assertFalse(tracker.onDestination("e1", home).isPop)

    @Test fun `an unseen entry is a push`() {
        tracker.onDestination("e1", home)

        assertFalse(tracker.onDestination("e2", detail).isPop)
    }

    @Test fun `returning to an entry already seen is a pop`() {
        tracker.onDestination("e1", home)
        tracker.onDestination("e2", detail)

        assertTrue(tracker.onDestination("e1", home).isPop)
    }

    @Test fun `a cleared stack rebuilds the same route as a push, not a pop`() {
        // Sign-in and the winner screen both `navigate(Home) { popUpTo(0) }`.
        // The stack shrinks, but this is a push: Home is a brand-new entry.
        tracker.onDestination("e1", home)
        tracker.onDestination("e2", detail)
        tracker.onDestination("e3", "${Winner::class.qualifiedName}/{challengeId}")

        assertFalse(tracker.onDestination("e4", home).isPop)
    }

    @Test fun `a push away from a tab root dims the screen underneath`() {
        tracker.onDestination("e1", home)

        assertTrue(tracker.onDestination("e2", detail).dims)
    }

    @Test fun `hopping between tabs never dims`() {
        tracker.onDestination("e1", home)

        assertFalse(tracker.onDestination("e2", challenges).dims)
    }

    @Test fun `leaving the auth screens dims`() {
        tracker.onDestination("e1", login)

        assertTrue(tracker.onDestination("e2", home).dims)
    }

    @Test fun `an unknown destination is treated as a push that dims`() {
        val motion = tracker.onDestination(null, null)

        assertFalse(motion.isPop)
        assertTrue(motion.dims)
    }
}
