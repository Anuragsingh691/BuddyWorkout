package com.example.buddyworkout.data.auth

/**
 * The signed-in user's identity, as Firebase Auth knows it. Deliberately not
 * the Firestore `users/{uid}` profile — that document arrives with the
 * challenge slice, and nothing here writes it.
 */
data class AuthUser(
    val uid: String,
    val displayName: String?,
    val email: String?,
    val photoUrl: String?,
)

/**
 * Why an auth command failed. Carries no user-facing copy: mapping to a
 * message is the ViewModel's job, so the data layer stays free of strings the
 * UI owns.
 */
enum class AuthError {
    InvalidCredentials,
    EmailInUse,
    WeakPassword,
    Network,
    /** No Google account on the device, or none the user was willing to share. */
    NoGoogleAccount,
    /** The user dismissed the Google sheet. Not an error worth showing. */
    Cancelled,
    Unknown,
}

/** Failure carried by the [Result] every command returns. */
class AuthException(val error: AuthError) : Exception(error.name)

/**
 * Auth *commands* only. Session state is not exposed here on purpose:
 * [com.example.buddyworkout.core.navigation.SessionSource] is the single source
 * of truth for whether a session exists, and a second auth-state listener would
 * let the nav gate and this repository disagree.
 */
interface AuthRepository {

    /** Snapshot of the current user, or null when signed out. */
    val currentUser: AuthUser?

    suspend fun signIn(email: String, password: String): Result<Unit>

    /** Creates the account and sets [name] as the Firebase display name. */
    suspend fun register(name: String, email: String, password: String): Result<Unit>

    /**
     * Exchanges a Google ID token for a Firebase session. Obtaining the token
     * needs an Activity context and therefore happens at the call site — see
     * `getGoogleIdToken`. Keeping it out of this interface is what lets the
     * ViewModels be tested without an Android context.
     */
    suspend fun signInWithGoogleIdToken(idToken: String): Result<Unit>

    suspend fun signOut()
}
