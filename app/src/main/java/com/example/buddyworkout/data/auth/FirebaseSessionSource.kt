package com.example.buddyworkout.data.auth

import com.example.buddyworkout.core.navigation.AuthState
import com.example.buddyworkout.core.navigation.SessionSource
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Bridges Firebase's `AuthStateListener` to a Flow. The listener fires once
 * immediately on registration with the restored session, which is what moves
 * the app out of [AuthState.Loading].
 */
@Singleton
class FirebaseSessionSource @Inject constructor(
    private val auth: FirebaseAuth,
) : SessionSource {

    override val authState: Flow<AuthState> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(
                if (firebaseAuth.currentUser != null) AuthState.SignedIn else AuthState.SignedOut
            )
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }.distinctUntilChanged()
}
