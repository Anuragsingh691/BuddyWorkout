package com.example.buddyworkout.data.user

import kotlinx.coroutines.flow.Flow

/**
 * The `users/{uid}` document, as `docs/architecture/03-data-model.md` §2
 * defines it. `inviteCode` is deliberately absent — nothing reads it until the
 * invite flow lands, and generating it needs collision handling that belongs
 * with that work.
 */
data class UserProfile(
    val uid: String,
    val displayName: String = "",
    val email: String = "",
    val phone: String? = null,
    /** Storage download URL, null until a photo has been uploaded. */
    val photoUrl: String? = null,
    val activeChallengeCount: Int = 0,
)

/**
 * Reads and writes the signed-in user's profile.
 *
 * An interface for the same reason [com.example.buddyworkout.data.auth.AuthRepository]
 * is one: it lets the ViewModels be tested against a fake with no Firebase and
 * no Android context.
 */
interface UserRepository {

    /**
     * Creates `users/{uid}` if it does not exist yet, and does nothing if it
     * does.
     *
     * Called after *every* successful sign-in, not just registration: Google
     * accounts never pass through the register screen, and accounts created
     * before this existed have no document at all.
     *
     * [name] and [phone] come from the registration form; both are null on a
     * plain sign-in, where the display name falls back to what Auth knows.
     */
    suspend fun ensureProfile(name: String? = null, phone: String? = null): Result<Unit>

    /**
     * Compresses the image at [uri], stores it at `users/{uid}/profile.jpg`,
     * and patches the download URL onto the profile.
     *
     * Registration does not fail when this does — a flaky connection should
     * cost the avatar, not the account.
     */
    suspend fun uploadPhoto(uri: String): Result<String>

    /** The signed-in user's profile, or null while signed out. */
    fun observeProfile(): Flow<UserProfile?>
}
