package com.example.buddyworkout.data.auth

import com.example.buddyworkout.core.common.BusyTracker
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val busy: BusyTracker,
) : AuthRepository {

    override val currentUser: AuthUser?
        get() = auth.currentUser?.toAuthUser()

    override suspend fun signIn(email: String, password: String): Result<Unit> = guarded(busy) {
        auth.signInWithEmailAndPassword(email.trim(), password).await()
    }

    override suspend fun register(name: String, email: String, password: String): Result<Unit> =
        guarded(busy) {
            val created = auth.createUserWithEmailAndPassword(email.trim(), password).await()
            // Without this the Profile screen would show an empty name until the
            // next cold start, since Auth has no display name of its own.
            created.user
                ?.updateProfile(
                    UserProfileChangeRequest.Builder().setDisplayName(name.trim()).build()
                )
                ?.await()
        }

    override suspend fun signInWithGoogleIdToken(idToken: String): Result<Unit> = guarded(busy) {
        auth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null)).await()
    }

    override suspend fun signOut() {
        busy.track { auth.signOut() }
    }
}

private fun FirebaseUser.toAuthUser() = AuthUser(
    uid = uid,
    displayName = displayName?.takeIf { it.isNotBlank() },
    email = email,
    photoUrl = photoUrl?.toString(),
)

/**
 * Runs an Auth call and converts its failure into an [AuthError].
 * [CancellationException] is rethrown: swallowing it would leave a cancelled
 * ViewModel scope looking like a failed sign-in.
 */
private suspend fun guarded(
    busy: BusyTracker,
    block: suspend () -> Unit,
): Result<Unit> = try {
    // Tracked here rather than at each call site so every command added later
    // raises the loader without anyone remembering to ask for it.
    busy.track(block)
    Result.success(Unit)
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    Result.failure(AuthException(e.toAuthError()))
}

private fun Exception.toAuthError(): AuthError = when (this) {
    is FirebaseNetworkException -> AuthError.Network
    is FirebaseAuthWeakPasswordException -> AuthError.WeakPassword
    is FirebaseAuthUserCollisionException -> AuthError.EmailInUse
    // With email-enumeration protection on, a wrong password and an unknown
    // account both arrive as an invalid credential — which is the point.
    is FirebaseAuthInvalidCredentialsException -> AuthError.InvalidCredentials
    is FirebaseAuthInvalidUserException -> AuthError.InvalidCredentials
    else -> AuthError.Unknown
}
