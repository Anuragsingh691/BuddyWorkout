package com.example.buddyworkout.feature.profile

import com.example.buddyworkout.data.auth.AuthUser
import com.example.buddyworkout.data.auth.FakeAuthRepository
import com.example.buddyworkout.core.common.BusyTracker
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ProfileViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val repo = FakeAuthRepository()
    private val users = FakeUserRepository()
    private val busy = BusyTracker()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    @Test fun `renders the signed-in user`() = runTest(dispatcher) {
        repo.currentUser = AuthUser(
            uid = "u1",
            displayName = "Anurag S.",
            email = "anurag@example.com",
            photoUrl = null,
        )

        val vm = ProfileViewModel(repo, users, busy)

        assertEquals("Anurag S.", vm.state.value.name)
        assertEquals("anurag@example.com", vm.state.value.email)
        assertEquals("AS", vm.state.value.avatar.initials)
    }

    @Test fun `falls back to the email when Google returns no display name`() =
        runTest(dispatcher) {
            repo.currentUser = AuthUser("u1", displayName = null, email = "anurag@example.com", photoUrl = null)

            val vm = ProfileViewModel(repo, users, busy)

            assertEquals("anurag@example.com", vm.state.value.name)
            assertEquals("A", vm.state.value.avatar.initials)
        }

    @Test fun `a single-word name yields one initial`() = runTest(dispatcher) {
        repo.currentUser = AuthUser("u1", displayName = "Anurag", email = "a@b.com", photoUrl = null)

        val vm = ProfileViewModel(repo, users, busy)

        assertEquals("A", vm.state.value.avatar.initials)
    }

    @Test fun `stats stay empty until challenge data exists`() = runTest(dispatcher) {
        repo.currentUser = AuthUser("u1", "Anurag S.", "anurag@example.com", null)

        val vm = ProfileViewModel(repo, users, busy)

        assertTrue(vm.state.value.stats.isEmpty())
    }

    @Test fun `signing out clears the firebase session and the stored credential`() =
        runTest(dispatcher) {
            repo.currentUser = AuthUser("u1", "Anurag S.", "anurag@example.com", null)
            val vm = ProfileViewModel(repo, users, busy)
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
        val vm = ProfileViewModel(repo, users, busy)
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
    }

    @Test fun `a profile with no phone says so`() = runTest(dispatcher) {
        val vm = ProfileViewModel(repo, users, busy)
        users.profile.value = UserProfile(uid = "u1", displayName = "Anurag S.")
        testScheduler.advanceUntilIdle()

        assertEquals("Not set", vm.state.value.stats.first { it.label == "Phone" }.value)
    }

    @Test fun `no stored avatar leaves the initials showing`() = runTest(dispatcher) {
        val vm = ProfileViewModel(repo, users, busy)
        users.profile.value = UserProfile(uid = "u1", displayName = "Anurag Shishodia")
        testScheduler.advanceUntilIdle()

        assertNull(vm.state.value.avatar.photo)
        assertEquals("AS", vm.state.value.avatar.initials)
    }

    @Test fun `undecodable avatar bytes fall back to initials rather than crashing`() =
        runTest(dispatcher) {
            val vm = ProfileViewModel(repo, users, busy)
            // BitmapFactory is stubbed in JVM tests, so any bytes decode to
            // null here — which is exactly the junk-data path being asserted.
            users.avatar.value = byteArrayOf(1, 2, 3)
            users.profile.value = UserProfile(uid = "u1", displayName = "Anurag Shishodia")
            testScheduler.advanceUntilIdle()

            assertNull(vm.state.value.avatar.photo)
            assertEquals("AS", vm.state.value.avatar.initials)
        }

    @Test fun `a later profile update keeps the avatar that was already decoded`() =
        runTest(dispatcher) {
            val vm = ProfileViewModel(repo, users, busy)
            users.profile.value = UserProfile(uid = "u1", displayName = "Anurag Shishodia")
            testScheduler.advanceUntilIdle()
            val before = vm.state.value.avatar.photo

            users.profile.value = UserProfile(uid = "u1", displayName = "Anurag S.")
            testScheduler.advanceUntilIdle()

            // merge() rebuilds the avatar for the new name; the photo must
            // survive that rebuild rather than being dropped on every edit.
            assertEquals(before, vm.state.value.avatar.photo)
            assertEquals("AS", vm.state.value.avatar.initials)
        }

    @Test fun `a rejected profile listen is reported, not fatal`() = runTest(dispatcher) {
        // Firestore rejects the listen on a rules failure, and signing out
        // rejects it too. Neither may take the app down with it.
        repo.currentUser = AuthUser("u1", "Anurag S.", "anurag@example.com", null)
        users.observeError = IllegalStateException("PERMISSION_DENIED")

        val vm = ProfileViewModel(repo, users, busy)
        testScheduler.advanceUntilIdle()

        assertNotNull(vm.state.value.error)
        // The Auth-seeded identity survives, so the screen still renders.
        assertEquals("Anurag S.", vm.state.value.name)
        assertFalse(vm.state.value.isLoading)
    }

    @Test fun `signing out raises the loader for the whole operation`() = runTest(dispatcher) {
        val vm = ProfileViewModel(repo, users, busy)
        var busyWhileClearing = false

        vm.onSignOut {
            // The credential clear is the slow half and happens outside any
            // repository, so it must still be covered.
            busyWhileClearing = busy.isBusy.value
        }
        testScheduler.advanceUntilIdle()

        assertTrue(busyWhileClearing)
        assertFalse(busy.isBusy.value)
    }
}
