package com.example.buddyworkout.data.challenge

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private const val CHALLENGES = "challenges"
private const val PARTICIPANTS = "participants"

/**
 * How far back the list reaches. Active challenges are capped at two, but a
 * long-lived account accumulates finished ones, and each challenge on screen
 * costs a participants listener.
 */
private const val HISTORY_LIMIT = 30L

@Singleton
class ChallengeRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) : ChallengeRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeMyChallenges(): Flow<List<ChallengeWithParticipants>> {
        val uid = auth.currentUser?.uid ?: return flowOf(emptyList())

        // One query for active and finished alike, split by status in the
        // ViewModel. Adding `status ==` here would need a second composite
        // index for no benefit at this size.
        return firestore.collection(CHALLENGES)
            .whereArrayContains("memberUids", uid)
            .orderBy("endAt", Query.Direction.DESCENDING)
            .limit(HISTORY_LIMIT)
            .asFlow()
            .flatMapLatest { snapshot ->
                val challenges = snapshot.documents.map { it.toChallenge() }
                if (challenges.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    // One participants listener per challenge. `combine` waits
                    // for every one to emit, so the list arrives complete
                    // rather than filling in card by card.
                    combine(challenges.map { withParticipants(it) }) { it.toList() }
                }
            }
    }

    override fun observeChallenge(challengeId: String): Flow<ChallengeWithParticipants?> =
        firestore.collection(CHALLENGES).document(challengeId)
            .asFlow()
            .let { documents ->
                combine(documents, participantsOf(challengeId)) { document, participants ->
                    if (!document.exists()) null
                    else ChallengeWithParticipants(document.toChallenge(), participants)
                }
            }

    private fun withParticipants(challenge: Challenge): Flow<ChallengeWithParticipants> =
        participantsOf(challenge.id).map { ChallengeWithParticipants(challenge, it) }

    private fun participantsOf(challengeId: String): Flow<List<Participant>> =
        firestore.collection(CHALLENGES).document(challengeId).collection(PARTICIPANTS)
            .asFlow()
            .map { snapshot -> snapshot.documents.map { it.toParticipant() } }
}

/**
 * A Firestore query as a Flow.
 *
 * Fails the flow on a rejected listen rather than swallowing it — the contract
 * every consumer handles with `.catch`.
 */
private fun Query.asFlow(): Flow<QuerySnapshot> = callbackFlow {
    val registration = addSnapshotListener { snapshot, error ->
        when {
            error != null -> close(error)
            snapshot != null -> trySend(snapshot)
        }
    }
    awaitClose { registration.remove() }
}

private fun com.google.firebase.firestore.DocumentReference.asFlow(): Flow<DocumentSnapshot> =
    callbackFlow {
        val registration = addSnapshotListener { snapshot, error ->
            when {
                error != null -> close(error)
                snapshot != null -> trySend(snapshot)
            }
        }
        awaitClose { registration.remove() }
    }

private fun DocumentSnapshot.toChallenge() = Challenge(
    id = id,
    title = getString("title") ?: "Pushup challenge",
    exercise = getString("exercise") ?: "pushups",
    creatorUid = getString("creatorUid").orEmpty(),
    memberUids = memberUids(),
    startAtMillis = getTimestamp("startAt").millis(),
    endAtMillis = getTimestamp("endAt").millis(),
    status = ChallengeStatus.from(getString("status")),
    winnerUid = getString("winnerUid"),
)

private fun DocumentSnapshot.toParticipant() = Participant(
    uid = id,
    displayName = getString("displayName").orEmpty(),
    photoUrl = getString("photoUrl"),
    totalReps = getLong("totalReps")?.toInt() ?: 0,
    lastActiveAtMillis = getTimestamp("lastActiveAt")?.toDate()?.time,
)

private fun Timestamp?.millis(): Long = this?.toDate()?.time ?: 0L

/** Firestore hands arrays back untyped; anything that is not a string is dropped. */
private fun DocumentSnapshot.memberUids(): List<String> =
    (get("memberUids") as? List<*>).orEmpty().filterIsInstance<String>()
