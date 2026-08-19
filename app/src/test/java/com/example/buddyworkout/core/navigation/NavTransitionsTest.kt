package com.example.buddyworkout.core.navigation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The transitions themselves need an instrumented test to observe; the routing
 * decision behind them is pure, and is what actually goes wrong.
 */
class NavTransitionsTest {

    private val home = Home::class.qualifiedName
    private val challenges = Challenges::class.qualifiedName
    private val profile = Profile::class.qualifiedName
    private val detail = "${ChallengeDetail::class.qualifiedName}/{id}"

    @Test fun `moving between two tab roots is a tab switch`() {
        assertTrue(isTabSwitch(home, challenges))
        assertTrue(isTabSwitch(challenges, profile))
        assertTrue(isTabSwitch(profile, home))
    }

    @Test fun `reselecting the current tab is a tab switch`() =
        assertTrue(isTabSwitch(home, home))

    @Test fun `pushing off a tab root is not a tab switch`() {
        assertFalse(isTabSwitch(home, detail))
        assertFalse(isTabSwitch(home, Invite::class.qualifiedName))
        assertFalse(isTabSwitch(profile, Gallery::class.qualifiedName))
    }

    @Test fun `returning to a tab root from a pushed screen is not a tab switch`() {
        assertFalse(isTabSwitch(detail, home))
        assertFalse(isTabSwitch(Login::class.qualifiedName, home))
    }

    @Test fun `two non-root destinations are not a tab switch`() =
        assertFalse(isTabSwitch(Create::class.qualifiedName, BuddyPicker::class.qualifiedName))

    @Test fun `an unknown destination is never a tab switch`() {
        assertFalse(isTabSwitch(null, home))
        assertFalse(isTabSwitch(home, null))
        assertFalse(isTabSwitch(null, null))
    }
}
