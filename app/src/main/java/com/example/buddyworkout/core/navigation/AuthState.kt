package com.example.buddyworkout.core.navigation

import kotlinx.coroutines.flow.Flow

/**
 * Whether a signed-in session exists. [Loading] means Firebase has not yet
 * restored the persisted session — the app shows a blank themed screen rather
 * than guessing, which is what avoids a flash of the login screen on launch.
 */
enum class AuthState { Loading, SignedOut, SignedIn }

/** Single source of truth for the session, observed above the NavHost. */
interface SessionSource {
    val authState: Flow<AuthState>
}
