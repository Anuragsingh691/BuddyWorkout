package com.example.buddyworkout.data.challenge

/** `challenges/{id}.status`, per `docs/architecture/03-data-model.md` §3. */
enum class ChallengeStatus {
    Scheduled, Active, Completed, Cancelled, Unknown;

    val isOver: Boolean get() = this == Completed || this == Cancelled

    companion object {
        fun from(raw: String?): ChallengeStatus = when (raw) {
            "scheduled" -> Scheduled
            "active" -> Active
            "completed" -> Completed
            "cancelled" -> Cancelled
            // A status this build does not know is not a crash: a later version
            // may add one, and old clients still have to render.
            else -> Unknown
        }
    }
}

/** A `challenges/{id}` document. Times are epoch millis; formatting is the UI's. */
data class Challenge(
    val id: String,
    val title: String = "Pushup challenge",
    val exercise: String = "pushups",
    val creatorUid: String = "",
    val memberUids: List<String> = emptyList(),
    val startAtMillis: Long = 0L,
    val endAtMillis: Long = 0L,
    val status: ChallengeStatus = ChallengeStatus.Unknown,
    val winnerUid: String? = null,
)

/** A `challenges/{id}/participants/{uid}` document. */
data class Participant(
    val uid: String,
    val displayName: String = "",
    val photoUrl: String? = null,
    val totalReps: Int = 0,
    val lastActiveAtMillis: Long? = null,
)

/**
 * A challenge with its participants, which is how every screen consumes one —
 * no card or leaderboard renders without both.
 */
data class ChallengeWithParticipants(
    val challenge: Challenge,
    val participants: List<Participant> = emptyList(),
) {
    /** Highest reps first; ties break by name so ranks do not jitter between reads. */
    val ranked: List<Participant>
        get() = participants.sortedWith(
            compareByDescending<Participant> { it.totalReps }.thenBy { it.displayName },
        )

    fun rankOf(uid: String): Int? =
        ranked.indexOfFirst { it.uid == uid }.takeIf { it >= 0 }?.plus(1)

    fun participant(uid: String): Participant? = participants.firstOrNull { it.uid == uid }
}
