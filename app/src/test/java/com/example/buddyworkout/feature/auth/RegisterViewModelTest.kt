package com.example.buddyworkout.feature.auth

import com.example.buddyworkout.data.auth.AuthError
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RegisterViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val repo = FakeAuthRepository()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = RegisterViewModel(repo)

    private fun RegisterViewModel.fillValidForm() {
        onNameChange("Anurag S.")
        onEmailChange("anurag@example.com")
        onPasswordChange("secret123")
    }

    @Test fun `typing updates every field`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.fillValidForm()

        assertEquals("Anurag S.", vm.state.value.name)
        assertEquals("anurag@example.com", vm.state.value.email)
        assertEquals("secret123", vm.state.value.password)
        assertTrue(vm.state.value.canSubmit)
    }

    @Test fun `the optional fields are held but never gate submission`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.fillValidForm()
        vm.onPhoneChange("+91 98765 43210")
        vm.onPhotoSelected("content://media/picked/1")

        assertEquals("+91 98765 43210", vm.state.value.phone)
        assertEquals("content://media/picked/1", vm.state.value.photoUri)
        assertTrue(vm.state.value.canSubmit)
    }

    @Test fun `a short password blocks submission before the network is touched`() =
        runTest(dispatcher) {
            val vm = viewModel()
            vm.onNameChange("Anurag S.")
            vm.onEmailChange("anurag@example.com")
            vm.onPasswordChange("12345")

            assertFalse(vm.state.value.canSubmit)

            vm.onCreateAccount()
            testScheduler.advanceUntilIdle()

            assertEquals(0, repo.registerCalls)
        }

    @Test fun `creating an account passes the display name through`() = runTest(dispatcher) {
        val vm = viewModel()
        val events = mutableListOf<Unit>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.registered.collect { events += it } }

        vm.fillValidForm()
        vm.onCreateAccount()
        testScheduler.advanceUntilIdle()

        assertEquals("Anurag S.", repo.lastName)
        assertEquals("anurag@example.com", repo.lastEmail)
        assertEquals(1, events.size)
    }

    @Test fun `a taken email points the user at signing in instead`() = runTest(dispatcher) {
        repo.failWith(AuthError.EmailInUse)
        val vm = viewModel()
        val events = mutableListOf<Unit>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.registered.collect { events += it } }

        vm.fillValidForm()
        vm.onCreateAccount()
        testScheduler.advanceUntilIdle()

        assertEquals(
            "That email already has an account. Try signing in.",
            vm.state.value.error,
        )
        assertFalse(vm.state.value.isLoading)
        assertTrue(events.isEmpty())
    }

    @Test fun `a password the server rejects is reported as weak`() = runTest(dispatcher) {
        repo.failWith(AuthError.WeakPassword)
        val vm = viewModel()

        vm.fillValidForm()
        vm.onCreateAccount()
        testScheduler.advanceUntilIdle()

        assertEquals("Use at least 6 characters for your password.", vm.state.value.error)
    }

    @Test fun `a second tap while loading does not create two accounts`() = runTest(dispatcher) {
        repo.hang = true
        val vm = viewModel()

        vm.fillValidForm()
        vm.onCreateAccount()
        vm.onCreateAccount()
        testScheduler.advanceUntilIdle()

        assertEquals(1, repo.registerCalls)
        assertTrue(vm.state.value.isLoading)
    }
}
