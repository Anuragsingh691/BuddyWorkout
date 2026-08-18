package com.example.buddyworkout.data.user

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.example.buddyworkout.core.common.BusyTracker
import com.example.buddyworkout.core.common.trackCatching
import com.example.buddyworkout.core.common.decodeSampledBitmap
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton

private const val USERS = "users"
private const val MEDIA = "media"
private const val AVATAR = "avatar"
private const val AVATAR_FIELD = "jpeg"

/** Longest edge of the uploaded avatar, per `02-firebase-design.md` §6. */
private const val PHOTO_MAX_PX = 512

private const val PHOTO_QUALITY = 85

/**
 * Ceiling for the stored blob. A Firestore document may not exceed 1 MiB, and
 * exceeding it is a hard rejection rather than a slow write, so the encoder
 * steps quality down until it fits with room to spare.
 */
private const val AVATAR_MAX_BYTES = 300 * 1024

/** Quality steps tried in order when the first encode comes out too large. */
private val QUALITY_STEPS = listOf(PHOTO_QUALITY, 70, 55, 40)

@Singleton
class UserRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val busy: BusyTracker,
) : UserRepository {

    override suspend fun ensureProfile(name: String?, phone: String?): Result<Unit> =
        busy.trackCatching {
        val user = auth.currentUser ?: error("ensureProfile called while signed out")
        val document = firestore.collection(USERS).document(user.uid)

        if (document.get().await().exists()) return@trackCatching

        document.set(
            mapOf(
                // The form's name wins; Auth's is the fallback for a plain
                // sign-in, where there is no form.
                "displayName" to (name?.trim()?.takeIf { it.isNotBlank() }
                    ?: user.displayName.orEmpty()),
                "email" to user.email.orEmpty(),
                "phone" to phone?.trim()?.takeIf { it.isNotBlank() },
                // Google hands us an avatar already; a photo picked during
                // registration overwrites it a moment later.
                "photoUrl" to user.photoUrl?.toString(),
                "activeChallengeCount" to 0,
                "createdAt" to FieldValue.serverTimestamp(),
            )
        ).await()
    }

    override suspend fun saveAvatar(uri: String): Result<Unit> = busy.trackCatching {
        val uid = auth.currentUser?.uid ?: error("saveAvatar called while signed out")
        val bytes = withContext(Dispatchers.IO) { compress(Uri.parse(uri)) }

        avatarDocument(uid).set(
            mapOf(
                AVATAR_FIELD to Blob.fromBytes(bytes),
                "updatedAt" to FieldValue.serverTimestamp(),
            )
        ).await()
    }

    override fun observeAvatar(): Flow<ByteArray?> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val registration = avatarDocument(uid).addSnapshotListener { snapshot, error ->
            when {
                error != null -> close(error)
                snapshot == null || !snapshot.exists() -> trySend(null)
                else -> trySend(snapshot.getBlob(AVATAR_FIELD)?.toBytes())
            }
        }
        awaitClose { registration.remove() }
    }

    private fun avatarDocument(uid: String) =
        firestore.collection(USERS).document(uid).collection(MEDIA).document(AVATAR)

    override fun observeProfile(): Flow<UserProfile?> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val registration = firestore.collection(USERS).document(uid)
            .addSnapshotListener { snapshot, error ->
                when {
                    error != null -> close(error)
                    // Absent rather than null: the document may not have been
                    // written yet on a first run.
                    snapshot == null || !snapshot.exists() -> trySend(null)
                    else -> trySend(snapshot.toProfile(uid))
                }
            }
        awaitClose { registration.remove() }
    }

    /**
     * Scales to [PHOTO_MAX_PX] and encodes as JPEG, stepping quality down until
     * the result fits [AVATAR_MAX_BYTES].
     */
    private fun compress(uri: Uri): ByteArray {
        val decoded = decodeSampledBitmap(context, uri, PHOTO_MAX_PX)
            ?: error("Could not read the selected image")

        val longest = maxOf(decoded.width, decoded.height)
        // inSampleSize only halves, so the decode lands near the target rather
        // than on it; this pass is what actually pins the longest edge.
        val scaled = if (longest > PHOTO_MAX_PX) {
            val ratio = PHOTO_MAX_PX.toFloat() / longest
            Bitmap.createScaledBitmap(
                decoded,
                (decoded.width * ratio).toInt().coerceAtLeast(1),
                (decoded.height * ratio).toInt().coerceAtLeast(1),
                true,
            )
        } else {
            decoded
        }

        var encoded = ByteArray(0)
        for (quality in QUALITY_STEPS) {
            encoded = ByteArrayOutputStream().use { out ->
                scaled.compress(Bitmap.CompressFormat.JPEG, quality, out)
                out.toByteArray()
            }
            if (encoded.size <= AVATAR_MAX_BYTES) return encoded
        }
        // Every step was still too large: a photo this dense at 512px is not
        // realistic, and writing it would fail the document size limit.
        error("Image is too large to store even at reduced quality")
    }
}

private fun com.google.firebase.firestore.DocumentSnapshot.toProfile(uid: String) = UserProfile(
    uid = uid,
    displayName = getString("displayName").orEmpty(),
    email = getString("email").orEmpty(),
    phone = getString("phone"),
    photoUrl = getString("photoUrl"),
    activeChallengeCount = getLong("activeChallengeCount")?.toInt() ?: 0,
)
