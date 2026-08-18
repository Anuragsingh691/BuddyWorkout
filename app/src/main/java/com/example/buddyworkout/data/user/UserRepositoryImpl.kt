package com.example.buddyworkout.data.user

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.example.buddyworkout.core.common.decodeSampledBitmap
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
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
private const val PHOTO_PATH = "profile.jpg"

/** Longest edge of the uploaded avatar, per `02-firebase-design.md` §6. */
private const val PHOTO_MAX_PX = 512

private const val PHOTO_QUALITY = 85

@Singleton
class UserRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
) : UserRepository {

    override suspend fun ensureProfile(name: String?, phone: String?): Result<Unit> = runCatching {
        val user = auth.currentUser ?: error("ensureProfile called while signed out")
        val document = firestore.collection(USERS).document(user.uid)

        if (document.get().await().exists()) return@runCatching

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

    override suspend fun uploadPhoto(uri: String): Result<String> = runCatching {
        val uid = auth.currentUser?.uid ?: error("uploadPhoto called while signed out")
        val bytes = withContext(Dispatchers.IO) { compress(Uri.parse(uri)) }

        val reference = storage.reference.child("$USERS/$uid/$PHOTO_PATH")
        reference.putBytes(bytes).await()
        val url = reference.downloadUrl.await().toString()

        firestore.collection(USERS).document(uid).update("photoUrl", url).await()
        url
    }

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

    /** Scales to [PHOTO_MAX_PX] and re-encodes as JPEG before upload. */
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

        return ByteArrayOutputStream().use { out ->
            scaled.compress(Bitmap.CompressFormat.JPEG, PHOTO_QUALITY, out)
            out.toByteArray()
        }
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
