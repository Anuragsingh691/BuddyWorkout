package com.example.buddyworkout.feature.profile

import com.example.buddyworkout.data.auth.AuthUser
import com.example.buddyworkout.data.auth.FakeAuthRepository
import com.example.buddyworkout.data.user.FakeUserRepository
import com.example.buddyworkout.data.user.UserProfile
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
    private val users = FakeUserRepository()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    @Test fun `renders the signed-in user`() = runTest(dispatcher) {
        repo.currentUser = AuthUser(
            uid = "u1",
            displayName = "Anurag S.",
            email = "anurag@example.com",
            photoUrl = null,
        )

        val vm = ProfileViewModel(repo, users)

        assertEquals("Anurag S.", vm.state.value.name)
        assertEquals("anurag@example.com", vm.state.value.email)
        assertEquals("AS", vm.state.value.avatar.initials)
    }

    @Test fun `falls back to the email when Google returns no display name`() =
        runTest(dispatcher) {
            repo.currentUser = AuthUser("u1", displayName = null, email = "anurag@example.com", photoUrl = null)

            val vm = ProfileViewModel(repo, users)

            assertEquals("anurag@example.com", vm.state.value.name)
            assertEquals("A", vm.state.value.avatar.initials)
        }

    @Test fun `a single-word name yields one initial`() = runTest(dispatcher) {
        repo.currentUser = AuthUser("u1", displayName = "Anurag", email = "a@b.com", photoUrl = null)

        val vm = ProfileViewModel(repo, users)

        assertEquals("A", vm.state.value.avatar.initials)
    }

    @Test fun `stats stay empty until challenge data exists`() = runTest(dispatcher) {
        repo.currentUser = AuthUser("u1", "Anurag S.", "anurag@example.com", null)

        val vm = ProfileViewModel(repo, users)

        assertTrue(vm.state.value.stats.isEmpty())
    }

    @Test fun `signing out clears the firebase session and the stored credential`() =
        runTest(dispatcher) {
            repo.currentUser = AuthUser("u1", "Anurag S.", "anurag@example.com", null)
            val vm = ProfileViewModel(repo, users)
            val events = mutableListOf<Unit>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.signedOut.collect { events += it } }

            var cleared = false
            vm.onSignOut { cleared = true }
            testScheduler.advanceUntilIdle()

            assertEquals(1, repo.signOutCalls)
            assertTrue("Google would silently re-authenticate otherwise", cleared)
            assertEquals(1, events.size)
        }

    @Test fun `the Firestore document replaces what Auth guessed`() = runTest(dispatcher) {
        val vm = ProfileViewModel(repo, users)
        users.profile.value = UserProfile(
            uid = "u1",
            displayName = "Anurag Shishodia",
            email = "anurag@example.com",
            phone = "+91 98765 43210",
            photoUrl = "https://example.test/profile.jpg",
        )
        testScheduler.advanceUntilIdle()

        assertEquals("Anurag Shishodia", vm.state.value.name)
        assertEquals("AS", vm.state.value.avatar.initials)
        assertEquals("+91 98765 43210", vm.state.value.stats.first { it.label == "Phone" }.value)
        assertEquals("Uploaded", vm.state.value.stats.first { it.label == "Profile photo" }.value)
    }

    @Test fun `a profile with no photo says so`() = runTest(dispatcher) {
        val vm = ProfileViewModel(repo, users)
        users.profile.value = UserProfile(uid = "u1", displayName = "Anurag S.")
        testScheduler.advanceUntilIdle()

        assertEquals("None", vm.state.value.stats.first { it.label == "Profile photo" }.value)
        assertEquals("Not set", vm.state.value.stats.first { it.label == "Phone" }.value)
    }
}
