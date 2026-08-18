package com.example.buddyworkout.feature.profile

import com.example.buddyworkout.data.auth.AuthUser
import com.example.buddyworkout.data.auth.FakeAuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ProfileViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val repo = FakeAuthRepository()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    @Test fun `renders the signed-in user`() = runTest(dispatcher) {
        repo.currentUser = AuthUser(
            uid = "u1",
            displayName = "Anurag S.",
            email = "anurag@example.com",
            photoUrl = null,
        )

        val vm = ProfileViewModel(repo)

        assertEquals("Anurag S.", vm.state.value.name)
        assertEquals("anurag@example.com", vm.state.value.email)
        assertEquals("AS", vm.state.value.avatar.initials)
    }

    @Test fun `falls back to the email when Google returns no display name`() =
        runTest(dispatcher) {
            repo.currentUser = AuthUser("u1", displayName = null, email = "anurag@example.com", photoUrl = null)

            val vm = ProfileViewModel(repo)

            assertEquals("anurag@example.com", vm.state.value.name)
            assertEquals("A", vm.state.value.avatar.initials)
        }

    @Test fun `a single-word name yields one initial`() = runTest(dispatcher) {
        repo.currentUser = AuthUser("u1", displayName = "Anurag", email = "a@b.com", photoUrl = null)

        val vm = ProfileViewModel(repo)

        assertEquals("A", vm.state.value.avatar.initials)
    }

    @Test fun `stats stay empty until challenge data exists`() = runTest(dispatcher) {
        repo.currentUser = AuthUser("u1", "Anurag S.", "anurag@example.com", null)

        val vm = ProfileViewModel(repo)

        assertTrue(vm.state.value.stats.isEmpty())
    }

    @Test fun `signing out clears the firebase session and the stored credential`() =
        runTest(dispatcher) {
            repo.currentUser = AuthUser("u1", "Anurag S.", "anurag@example.com", null)
            val vm = ProfileViewModel(repo)
            val events = mutableListOf<Unit>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.signedOut.collect { events += it } }

            var cleared = false
            vm.onSignOut { cleared = true }
            testScheduler.advanceUntilIdle()

            assertEquals(1, repo.signOutCalls)
            assertTrue("Google would silently re-authenticate otherwise", cleared)
            assertEquals(1, events.size)
        }
}
