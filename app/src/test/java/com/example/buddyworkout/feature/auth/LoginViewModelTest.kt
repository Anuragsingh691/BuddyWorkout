package com.example.buddyworkout.feature.auth

import com.example.buddyworkout.data.auth.AuthError
import com.example.buddyworkout.data.auth.FakeAuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
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

/**
 * Event collectors run on an [UnconfinedTestDispatcher]: a `backgroundScope.launch`
 * on the standard dispatcher never starts its body under `advanceUntilIdle`, so the
 * one-shot navigation events would look lost when they were merely uncollected.
 */
class LoginViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val repo = FakeAuthRepository()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = LoginViewModel(repo)

    private fun LoginViewModel.fillValidCredentials() {
        onEmailChange("anurag@example.com")
        onPasswordChange("secret123")
    }

    @Test fun `typing updates the fields and enables submit`() = runTest(dispatcher) {
        val vm = viewModel()
        assertFalse(vm.state.value.canSubmit)

        vm.fillValidCredentials()

        assertEquals("anurag@example.com", vm.state.value.email)
        assertEquals("secret123", vm.state.value.password)
        assertTrue(vm.state.value.canSubmit)
    }

    @Test fun `successful sign-in passes the credentials through and signals navigation`() =
        runTest(dispatcher) {
            val vm = viewModel()
            val events = mutableListOf<Unit>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.signedIn.collect { events += it } }

            vm.fillValidCredentials()
            vm.onSignIn()
            testScheduler.advanceUntilIdle()

            assertEquals("anurag@example.com", repo.lastEmail)
            assertEquals("secret123", repo.lastPassword)
            assertEquals(1, events.size)
            assertFalse(vm.state.value.isLoading)
        }

    @Test fun `wrong password surfaces a message, clears loading, and does not navigate`() =
        runTest(dispatcher) {
            repo.failWith(AuthError.InvalidCredentials)
            val vm = viewModel()
            val events = mutableListOf<Unit>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.signedIn.collect { events += it } }

            vm.fillValidCredentials()
            vm.onSignIn()
            testScheduler.advanceUntilIdle()

            assertEquals("That email and password don't match.", vm.state.value.error)
            assertFalse(vm.state.value.isLoading)
            assertTrue(events.isEmpty())
        }

    @Test fun `a network failure reports the connection, not a credential problem`() =
        runTest(dispatcher) {
            repo.failWith(AuthError.Network)
            val vm = viewModel()

            vm.fillValidCredentials()
            vm.onSignIn()
            testScheduler.advanceUntilIdle()

            assertEquals("No connection. Check your network and try again.", vm.state.value.error)
        }

    @Test fun `the form is loading while the command is in flight`() = runTest(dispatcher) {
        repo.hang = true
        val vm = viewModel()

        vm.fillValidCredentials()
        vm.onSignIn()
        testScheduler.advanceUntilIdle()

        assertTrue(vm.state.value.isLoading)
        assertFalse("a loading form must not be resubmittable", vm.state.value.canSubmit)
    }

    @Test fun `a second tap while loading does not sign in twice`() = runTest(dispatcher) {
        repo.hang = true
        val vm = viewModel()

        vm.fillValidCredentials()
        vm.onSignIn()
        vm.onSignIn()
        testScheduler.advanceUntilIdle()

        assertEquals(1, repo.signInCalls)
    }

    @Test fun `retrying clears the previous error`() = runTest(dispatcher) {
        repo.failWith(AuthError.InvalidCredentials)
        val vm = viewModel()
        vm.fillValidCredentials()
        vm.onSignIn()
        testScheduler.advanceUntilIdle()
        assertTrue(vm.state.value.error != null)

        repo.hang = true
        vm.onSignIn()
        testScheduler.advanceUntilIdle()

        assertNull(vm.state.value.error)
    }

    @Test fun `google sign-in exchanges the id token for a session`() = runTest(dispatcher) {
        val vm = viewModel()
        val events = mutableListOf<Unit>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.signedIn.collect { events += it } }

        vm.onGoogleSignIn { Result.success("google-id-token") }
        testScheduler.advanceUntilIdle()

        assertEquals("google-id-token", repo.lastIdToken)
        assertEquals(1, events.size)
    }

    @Test fun `dismissing the google sheet is silent`() = runTest(dispatcher) {
        val vm = viewModel()
        val events = mutableListOf<Unit>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.signedIn.collect { events += it } }

        vm.onGoogleSignIn {
            Result.failure(com.example.buddyworkout.data.auth.AuthException(AuthError.Cancelled))
        }
        testScheduler.advanceUntilIdle()

        assertNull("a dismissed sheet is not an error", vm.state.value.error)
        assertFalse(vm.state.value.isLoading)
        assertTrue(events.isEmpty())
    }

    @Test fun `a failing google token request reports an error`() = runTest(dispatcher) {
        val vm = viewModel()

        vm.onGoogleSignIn {
            Result.failure(com.example.buddyworkout.data.auth.AuthException(AuthError.Unknown))
        }
        testScheduler.advanceUntilIdle()

        assertEquals("Something went wrong. Please try again.", vm.state.value.error)
        assertFalse(vm.state.value.isLoading)
    }

    @Test fun `google sign-in is not started twice`() = runTest(dispatcher) {
        repo.hang = true
        val vm = viewModel()

        vm.onGoogleSignIn { Result.success("t1") }
        vm.onGoogleSignIn { Result.success("t2") }
        testScheduler.advanceUntilIdle()

        assertEquals("t1", repo.lastIdToken)
    }

    @Test fun `signedIn is one-shot, not replayed to a later collector`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.fillValidCredentials()
        vm.onSignIn()
        testScheduler.advanceUntilIdle()

        // A collector that subscribes after the fact still receives the buffered
        // event exactly once — and never a second time.
        val received = mutableListOf<Unit>()
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.signedIn.collect { received += it } }
        testScheduler.advanceUntilIdle()
        job.cancel()

        assertEquals(1, received.size)
    }
}
