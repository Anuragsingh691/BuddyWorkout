package com.example.buddyworkout.feature.challenge

import androidx.lifecycle.SavedStateHandle
import com.example.buddyworkout.core.common.Ticker
import com.example.buddyworkout.data.auth.AuthUser
import com.example.buddyworkout.data.auth.FakeAuthRepository
import com.example.buddyworkout.data.challenge.Challenge
import com.example.buddyworkout.data.challenge.ChallengeFull
import com.example.buddyworkout.data.challenge.ChallengeStatus
import com.example.buddyworkout.data.challenge.ChallengeWithParticipants
import com.example.buddyworkout.data.challenge.FakeChallengeRepository
import com.example.buddyworkout.data.challenge.Participant
import com.example.buddyworkout.data.user.FakeUserRepository
import com.example.buddyworkout.data.user.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private const val DAY = 24 * 60 * 60 * 1000L
private const val ME = "me"

class AddBuddyTest {

    private val dispatcher = StandardTestDispatcher()
    private val auth = FakeAuthRepository()
    private val challenges = FakeChallengeRepository()
    private val users = FakeUserRepository()

    @Before fun setUp() {
        Dispatchers.setMain(dispatcher)
        auth.currentUser = AuthUser(ME, "Anurag S.", "anurag@example.com", null)
        challenges.challenge.value = ChallengeWithParticipants(
            Challenge(
                id = "c1",
                creatorUid = ME,
                memberUids = listOf(ME),
                startAtMillis = System.currentTimeMillis() - DAY,
                endAtMillis = System.currentTimeMillis() + DAY,
                status = ChallengeStatus.Active,
            ),
            listOf(Participant(ME, "Anurag S.")),
        )
    }

    @After fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = ChallengeDetailViewModel(
        SavedStateHandle(mapOf("id" to "c1")),
        auth,
        challenges,
        users,
        // A clock that ticks once and stops. The real one never completes, and
        // runTest drains the scheduler on the way out — an endless flow hangs
        // the suite instead of failing it.
        object : Ticker() {
            override fun seconds() = flowOf(System.currentTimeMillis())
        },
    )

    private fun rohit() = UserProfile(
        uid = "rohit",
        displayName = "Rohit Kumar",
        email = "rohit@example.com",
    )

    @Test fun `opening and dismissing resets the sheet`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onOpenAddBuddy()
        vm.onAddBuddyEmailChange("rohit@example.com")
        assertTrue(vm.state.value.addBuddy.isOpen)

        vm.onDismissAddBuddy()

        assertFalse(vm.state.value.addBuddy.isOpen)
        assertEquals("", vm.state.value.addBuddy.email)
    }

    @Test fun `a match is shown before it is added`() = runTest(dispatcher) {
        users.lookupResult = Result.success(rohit())
        val vm = viewModel()
        testScheduler.advanceUntilIdle()

        vm.onOpenAddBuddy()
        vm.onAddBuddyEmailChange("rohit@example.com")
        vm.onSearchBuddy()
        testScheduler.advanceUntilIdle()

        assertEquals("Rohit Kumar", vm.state.value.addBuddy.found?.name)
        assertTrue(vm.state.value.addBuddy.canAdd)
        // Nothing is written until the creator confirms.
        assertEquals(0, challenges.addCalls)
    }

    @Test fun `editing the address drops the previous match`() = runTest(dispatcher) {
        users.lookupResult = Result.success(rohit())
        val vm = viewModel()
        vm.onOpenAddBuddy()
        vm.onAddBuddyEmailChange("rohit@example.com")
        vm.onSearchBuddy()
        testScheduler.advanceUntilIdle()

        vm.onAddBuddyEmailChange("someone-else@example.com")

        // The address on screen and the person under it must never disagree.
        assertNull(vm.state.value.addBuddy.found)
        assertFalse(vm.state.value.addBuddy.canAdd)
    }

    @Test fun `an unknown address says so`() = runTest(dispatcher) {
        users.lookupResult = Result.success(null)
        val vm = viewModel()
        vm.onOpenAddBuddy()
        vm.onAddBuddyEmailChange("nobody@example.com")
        vm.onSearchBuddy()
        testScheduler.advanceUntilIdle()

        assertEquals("No account uses that email.", vm.state.value.addBuddy.error)
        assertNull(vm.state.value.addBuddy.found)
    }

    @Test fun `adding yourself is refused`() = runTest(dispatcher) {
        users.lookupResult = Result.success(
            UserProfile(uid = ME, displayName = "Anurag S.", email = "anurag@example.com"),
        )
        val vm = viewModel()
        vm.onOpenAddBuddy()
        vm.onAddBuddyEmailChange("anurag@example.com")
        vm.onSearchBuddy()
        testScheduler.advanceUntilIdle()

        assertEquals("That's you — you're already in.", vm.state.value.addBuddy.error)
    }

    @Test fun `someone already in the challenge is refused`() = runTest(dispatcher) {
        challenges.challenge.value = ChallengeWithParticipants(
            challenges.challenge.value!!.challenge.copy(memberUids = listOf(ME, "rohit")),
            listOf(Participant(ME, "Anurag S."), Participant("rohit", "Rohit Kumar")),
        )
        users.lookupResult = Result.success(rohit())
        val vm = viewModel()
        testScheduler.advanceUntilIdle()

        vm.onOpenAddBuddy()
        vm.onAddBuddyEmailChange("rohit@example.com")
        vm.onSearchBuddy()
        testScheduler.advanceUntilIdle()

        assertEquals("They're already in this challenge.", vm.state.value.addBuddy.error)
    }

    @Test fun `confirming adds them and closes the sheet`() = runTest(dispatcher) {
        users.lookupResult = Result.success(rohit())
        val vm = viewModel()
        testScheduler.advanceUntilIdle()

        vm.onOpenAddBuddy()
        vm.onAddBuddyEmailChange("rohit@example.com")
        vm.onSearchBuddy()
        testScheduler.advanceUntilIdle()
        vm.onConfirmAddBuddy()
        testScheduler.advanceUntilIdle()

        assertEquals(1, challenges.addCalls)
        assertEquals("rohit", challenges.lastAdded?.uid)
        assertEquals("Rohit Kumar", challenges.lastAdded?.displayName)
        assertFalse(vm.state.value.addBuddy.isOpen)
    }

    @Test fun `a full challenge keeps the sheet open and says why`() = runTest(dispatcher) {
        users.lookupResult = Result.success(rohit())
        challenges.addResult = Result.failure(ChallengeFull())
        val vm = viewModel()
        testScheduler.advanceUntilIdle()

        vm.onOpenAddBuddy()
        vm.onAddBuddyEmailChange("rohit@example.com")
        vm.onSearchBuddy()
        testScheduler.advanceUntilIdle()
        vm.onConfirmAddBuddy()
        testScheduler.advanceUntilIdle()

        assertEquals("This challenge is full.", vm.state.value.addBuddy.error)
        assertTrue(vm.state.value.addBuddy.isOpen)
    }

    @Test fun `confirming with nothing found does nothing`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onOpenAddBuddy()
        vm.onConfirmAddBuddy()
        testScheduler.advanceUntilIdle()

        assertEquals(0, challenges.addCalls)
    }
}
