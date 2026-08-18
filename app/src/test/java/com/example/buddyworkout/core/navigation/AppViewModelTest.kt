package com.example.buddyworkout.core.navigation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

private class FakeSessionSource(initial: AuthState) : SessionSource {
    private val flow = MutableStateFlow(initial)
    override val authState: Flow<AuthState> = flow
    fun emit(value: AuthState) { flow.value = value }
}

class AppViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    /**
     * The state is shared with [kotlinx.coroutines.flow.SharingStarted.WhileSubscribed],
     * so it only tracks the source while something collects it — exactly as
     * Compose does via `collectAsStateWithLifecycle`. Tests must therefore hold
     * a live collector or they only ever observe the initial value.
     *
     * Do not copy this helper for a test that asserts on *emissions*. A plain
     * `backgroundScope.launch` never starts its body under `advanceUntilIdle`,
     * so a collector that appends to a list records nothing and the assertion
     * passes only when it expects nothing. It works here solely because these
     * tests read `vm.state.value` — the StateFlow's current value — which the
     * subscription keeps fresh without the collector body ever running. To
     * collect emissions, launch on `UnconfinedTestDispatcher(testScheduler)`,
     * as the auth ViewModel tests do.
     */
    private fun TestScope.collecting(vm: AppViewModel) {
        backgroundScope.launch { vm.state.collect {} }
    }

    @Test fun `starts in Loading before anything is collected`() = runTest(dispatcher) {
        val vm = AppViewModel(FakeSessionSource(AuthState.SignedIn))
        assertEquals(AuthState.Loading, vm.state.value)
    }

    @Test fun `adopts the signed-in state once collected`() = runTest(dispatcher) {
        val vm = AppViewModel(FakeSessionSource(AuthState.SignedIn))
        collecting(vm)
        testScheduler.advanceUntilIdle()
        assertEquals(AuthState.SignedIn, vm.state.value)
    }

    @Test fun `follows the source when the session ends`() = runTest(dispatcher) {
        val source = FakeSessionSource(AuthState.SignedIn)
        val vm = AppViewModel(source)
        collecting(vm)
        testScheduler.advanceUntilIdle()

        source.emit(AuthState.SignedOut)
        testScheduler.advanceUntilIdle()

        assertEquals(AuthState.SignedOut, vm.state.value)
    }
}
