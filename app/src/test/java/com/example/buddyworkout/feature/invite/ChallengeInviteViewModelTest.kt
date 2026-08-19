package com.example.buddyworkout.feature.invite

import androidx.lifecycle.SavedStateHandle
import com.example.buddyworkout.data.auth.AuthUser
import com.example.buddyworkout.data.auth.FakeAuthRepository
import com.example.buddyworkout.data.challenge.AlreadyJoined
import com.example.buddyworkout.data.challenge.Challenge
import com.example.buddyworkout.data.challenge.ChallengeFull
import com.example.buddyworkout.data.challenge.ChallengeStatus
import com.example.buddyworkout.data.challenge.ChallengeWithParticipants
import com.example.buddyworkout.data.challenge.FakeChallengeRepository
import com.example.buddyworkout.data.challenge.Participant
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private const val DAY = 24 * 60 * 60 * 1000L
private const val ME = "me"

class ChallengeInviteViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val auth = FakeAuthRepository()
    private val repo = FakeChallengeRepository()

    @Before fun setUp() {
        Dispatchers.setMain(dispatcher)
        auth.currentUser = AuthUser(ME, "Anurag S.", "anurag@example.com", null)
    }

    @After fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = ChallengeInviteViewModel(
        SavedStateHandle(mapOf("code" to "c1")),
        auth,
        repo,
    )

    private fun invite(
        members: List<String> = listOf("rohit"),
        endAt: Long = System.currentTimeMillis() + 3 * DAY,
        status: ChallengeStatus = ChallengeStatus.Active,
    ) = ChallengeWithParticipants(
        Challenge(
            id = "c1",
            creatorUid = "rohit",
            memberUids = members,
            startAtMillis = System.currentTimeMillis() - DAY,
            endAtMillis = endAt,
            status = status,
        ),
        members.map { Participant(it, if (it == "rohit") "Rohit Kumar" else "Someone $it") },
    )

    @Test fun `the headline names whoever created it`() = runTest(dispatcher) {
        val vm = viewModel()
        repo.challenge.value = invite()
        testScheduler.advanceUntilIdle()

        assertEquals("Rohit invited you to a group pushup challenge", vm.state.value.headline)
        assertFalse(vm.state.value.isLoading)
    }

    @Test fun `a creator with no name still reads sensibly`() = runTest(dispatcher) {
        val vm = viewModel()
        repo.challenge.value = ChallengeWithParticipants(
            invite().challenge,
            listOf(Participant("rohit", "")),
        )
        testScheduler.advanceUntilIdle()

        assertEquals("A buddy invited you to a group pushup challenge", vm.state.value.headline)
    }

    @Test fun `a missing challenge says so rather than showing a blank invite`() =
        runTest(dispatcher) {
            val vm = viewModel()
            repo.challenge.value = null
            testScheduler.advanceUntilIdle()

            assertEquals("This challenge no longer exists.", vm.state.value.error)
        }

    @Test fun `being at the limit blocks accepting before it is tapped`() = runTest(dispatcher) {
        repo.challenges.value = listOf(invite(members = listOf(ME)), invite(members = listOf(ME)))
        val vm = viewModel()
        repo.challenge.value = invite()
        testScheduler.advanceUntilIdle()

        assertTrue(vm.state.value.blockedByLimit)
        assertFalse(vm.state.value.canAccept)
    }

    @Test fun `a challenge already joined is not blocked by the limit`() = runTest(dispatcher) {
        // Both active challenges include this one, so the slot is already
        // spent — reporting it as blocked would be wrong.
        repo.challenges.value = listOf(invite(members = listOf(ME)), invite(members = listOf(ME)))
        val vm = viewModel()
        repo.challenge.value = invite(members = listOf("rohit", ME))
        testScheduler.advanceUntilIdle()

        assertFalse(vm.state.value.blockedByLimit)
    }

    @Test fun `a finished challenge does not count against the limit`() = runTest(dispatcher) {
        repo.challenges.value = listOf(
            invite(members = listOf(ME), endAt = System.currentTimeMillis() - DAY),
            invite(members = listOf(ME), status = ChallengeStatus.Cancelled),
        )
        val vm = viewModel()
        repo.challenge.value = invite()
        testScheduler.advanceUntilIdle()

        assertFalse(vm.state.value.blockedByLimit)
    }

    @Test fun `accepting joins and hands back the id`() = runTest(dispatcher) {
        val vm = viewModel()
        val joined = mutableListOf<String>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.joined.collect { joined += it }
        }
        repo.challenge.value = invite()
        testScheduler.advanceUntilIdle()

        vm.onAccept()
        testScheduler.advanceUntilIdle()

        assertEquals(1, repo.joinCalls)
        assertEquals(listOf("c1"), joined)
    }

    @Test fun `already being a member opens the challenge instead of erroring`() =
        runTest(dispatcher) {
            repo.joinResult = Result.failure(AlreadyJoined())
            val vm = viewModel()
            val joined = mutableListOf<String>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                vm.joined.collect { joined += it }
            }
            repo.challenge.value = invite()
            testScheduler.advanceUntilIdle()

            vm.onAccept()
            testScheduler.advanceUntilIdle()

            assertEquals(listOf("c1"), joined)
            assertNull(vm.state.value.error)
        }

    @Test fun `a full challenge says which problem it is`() = runTest(dispatcher) {
        repo.joinResult = Result.failure(ChallengeFull())
        val vm = viewModel()
        repo.challenge.value = invite()
        testScheduler.advanceUntilIdle()

        vm.onAccept()
        testScheduler.advanceUntilIdle()

        assertEquals("This challenge is full.", vm.state.value.error)
    }
}
