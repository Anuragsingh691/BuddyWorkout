package com.example.buddyworkout.core.navigation

import com.example.buddyworkout.core.ui.component.BwNavItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RoutesTest {

    @Test fun `each bottom nav item maps to its root route`() {
        assertEquals(Home, BwNavItem.Home.route())
        assertEquals(Challenges, BwNavItem.Challenges.route())
        assertEquals(Profile, BwNavItem.Profile.route())
    }

    @Test fun `root destinations select their tab`() {
        assertEquals(BwNavItem.Home, bottomBarTabFor(Home::class.qualifiedName))
        assertEquals(BwNavItem.Challenges, bottomBarTabFor(Challenges::class.qualifiedName))
        assertEquals(BwNavItem.Profile, bottomBarTabFor(Profile::class.qualifiedName))
    }

    @Test fun `non-root destinations hide the bottom bar`() {
        assertNull(bottomBarTabFor(Login::class.qualifiedName))
        assertNull(bottomBarTabFor(Create::class.qualifiedName))
        assertNull(bottomBarTabFor(ChallengeDetail::class.qualifiedName))
        assertNull(bottomBarTabFor(Record::class.qualifiedName))
    }

    @Test fun `parameterised routes match despite their argument suffix`() =
        assertNull(bottomBarTabFor("${ChallengeDetail::class.qualifiedName}/{id}"))

    @Test fun `null destination hides the bottom bar`() = assertNull(bottomBarTabFor(null))
}
