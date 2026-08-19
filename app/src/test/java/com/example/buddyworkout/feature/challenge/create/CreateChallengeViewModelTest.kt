package com.example.buddyworkout.feature.challenge.create

import com.example.buddyworkout.data.challenge.ChallengeLimitReached
import com.example.buddyworkout.data.challenge.FakeChallengeRepository
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

class CreateChallengeViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val repo = FakeChallengeRepository()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = CreateChallengeViewModel(repo)

    @Test fun `opens with 12 hours chosen, as the export shows`() {
        val vm = viewModel()

        assertEquals(1, vm.state.value.selectedDurationIndex)
        assertEquals("12-hour challenge", vm.state.value.durationLabel)
        assertTrue(vm.state.value.canCreate)
    }

    @Test fun `picking a duration relabels the window`() {
        val vm = viewModel()
        vm.onSelectDuration(4)

        assertEquals("1-week challenge", vm.state.value.durationLabel)
        assertTrue(vm.state.value.startLabel.isNotBlank())
        assertTrue(vm.state.value.endLabel.isNotBlank())
    }

    @Test fun `an out-of-range duration is ignored rather than crashing`() {
        val vm = viewModel()
        vm.onSelectDuration(99)

        assertEquals(1, vm.state.value.selectedDurationIndex)
    }

    @Test fun `no buddies is fine — a challenge fills up from the link`() = runTest(dispatcher) {
        val vm = viewModel()
        val ids = mutableListOf<String>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.created.collect { ids += it } }

        assertTrue(vm.state.value.selectedBuddies.isEmpty())
        vm.onCreate()
        testScheduler.advanceUntilIdle()

        assertEquals(1, repo.createCalls)
        assertEquals(listOf("new-id"), ids)
    }

    @Test fun `the window spans the chosen duration`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onSelectDuration(2)
        vm.onCreate()
        testScheduler.advanceUntilIdle()

        val window = repo.lastWindow!!
        assertEquals(24 * 60 * 60 * 1000L, window.endMillis - window.startMillis)
    }

    @Test fun `hitting the limit says which limit, not just that it failed`() =
        runTest(dispatcher) {
            repo.createResult = Result.failure<String>(ChallengeLimitReached())
            val vm = viewModel()
            val ids = mutableListOf<String>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.created.collect { ids += it } }

            vm.onCreate()
            testScheduler.advanceUntilIdle()

            assertEquals(
                "You're already in 2 active challenges. Finish or cancel one first.",
                vm.state.value.error,
            )
            assertTrue(ids.isEmpty())
            assertFalse(vm.state.value.isLoading)
        }

    @Test fun `any other failure gets a retryable message`() = runTest(dispatcher) {
        repo.createResult = Result.failure<String>(IllegalStateException("offline"))
        val vm = viewModel()

        vm.onCreate()
        testScheduler.advanceUntilIdle()

        assertNotNull(vm.state.value.error)
        assertFalse(vm.state.value.error!!.contains("2 active"))
    }

    @Test fun `changing the duration clears a previous error`() = runTest(dispatcher) {
        repo.createResult = Result.failure<String>(ChallengeLimitReached())
        val vm = viewModel()
        vm.onCreate()
        testScheduler.advanceUntilIdle()
        assertNotNull(vm.state.value.error)

        vm.onSelectDuration(3)

        assertNull(vm.state.value.error)
    }

    @Test fun `a second tap while creating does not make two challenges`() = runTest(dispatcher) {
        val vm = viewModel()

        vm.onCreate()
        vm.onCreate()
        testScheduler.advanceUntilIdle()

        assertEquals(1, repo.createCalls)
    }
}
