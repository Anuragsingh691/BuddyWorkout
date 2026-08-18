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
     * Compresses the image at [uri] and stores it as a Firestore `Blob` at
     * `users/{uid}/media/avatar`.
     *
     * Not Cloud Storage, which is the natural home for this: a new Firebase
     * project needs the Blaze plan to get a bucket at all, and one 40KB avatar
     * does not justify a billing card. The cost is a 1 MiB document ceiling and
     * bandwidth on read; see the caller for the size guard.
     *
     * Registration does not fail when this does — a flaky connection should
     * cost the avatar, not the account.
     */
    suspend fun saveAvatar(uri: String): Result<Unit>

    /**
     * The signed-in user's avatar as JPEG bytes, or null when there is none.
     *
     * Bytes rather than a URL is what lets an avatar render with no image
     * loader in the project: they go straight to `BitmapFactory`.
     *
     * **Fails the flow** on a rejected listen, like [observeProfile].
     */
    fun observeAvatar(): Flow<ByteArray?>

    /**
     * The signed-in user's profile, or null while signed out.
     *
     * **Fails the flow** when Firestore rejects the listen — a rules failure,
     * or a sign-out that re-evaluates the listen with no auth. Collect it with
     * `.catch`: an unhandled failure here reaches the coroutine that collects
     * and takes the process with it.
     */
    fun observeProfile(): Flow<UserProfile?>
}
