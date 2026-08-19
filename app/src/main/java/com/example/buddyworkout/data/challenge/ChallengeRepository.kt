package com.example.buddyworkout.data.challenge

import com.example.buddyworkout.feature.challenge.create.ChallengeWindow
import kotlinx.coroutines.flow.Flow

/**
 * Read access to the challenges the signed-in user is a member of.
 *
 * Commands live elsewhere: creating, joining and cancelling arrive with the
 * write path, and several of them go through Cloud Functions rather than the
 * client (`docs/architecture/03-data-model.md` §4).
 */
interface ChallengeRepository {

    /**
     * Every challenge the user belongs to, newest deadline first, with each
     * one's participants attached.
     *
     * **Fails the flow** when Firestore rejects a listen — see
     * [com.example.buddyworkout.data.user.UserRepository.observeProfile]. Collect
     * with `.catch`.
     */
    fun observeMyChallenges(): Flow<List<ChallengeWithParticipants>>

    /** One challenge and its live leaderboard, or null if it is gone. */
    fun observeChallenge(challengeId: String): Flow<ChallengeWithParticipants?>

    /**
     * Creates a challenge containing only the creator and returns its id.
     *
     * Members arrive by opening the share link, so no write here touches
     * another user's document — which is what keeps this possible under rules
     * that let a user write only their own data.
     *
     * Fails with [ChallengeLimitReached] when the creator is already in
     * [MAX_ACTIVE_CHALLENGES]. That check is client-side and therefore
     * client-trusted: enforcing it properly was the job of the Cloud Function
     * this build does without (`02-firebase-design.md` §5).
     */
    suspend fun createChallenge(window: ChallengeWindow): Result<String>

    /**
     * Adds the signed-in user to a challenge they have the link to.
     *
     * Fails with [ChallengeFull] at the member cap, [AlreadyJoined] if they are
     * already in, [ChallengeOver] once it has ended, and
     * [ChallengeLimitReached] when they are at their own limit.
     */
    suspend fun joinChallenge(challengeId: String): Result<Unit>

    /**
     * Adds someone else to a challenge. Creator only, which rules enforce.
     *
     * Unlike joining, the added person does not consent and their own
     * two-challenge limit cannot be checked: reading another user's challenges
     * is exactly what the `list` rule forbids. Another consequence of having
     * no Cloud Function to do this server-side.
     */
    suspend fun addMember(challengeId: String, person: NewMember): Result<Unit>

    /**
     * Voids a challenge: no winner, and it stops occupying a slot for every
     * member. Creator only, which rules enforce.
     */
    suspend fun cancelChallenge(challengeId: String): Result<Unit>
}

/** Enough of someone's profile to seed their leaderboard row. */
data class NewMember(val uid: String, val displayName: String, val photoUrl: String?)

/** The user is already in as many challenges as the rules allow. */
class ChallengeLimitReached : Exception("Already in the maximum number of challenges")

/** The challenge already holds [MAX_MEMBERS]. */
class ChallengeFull : Exception("Challenge is full")

/** The user is already a member — the invite screen should not have offered. */
class AlreadyJoined : Exception("Already a member")

/** Cancelled, completed, or past its deadline. */
class ChallengeOver : Exception("Challenge has ended")

/** A challenge holds 2-4 people including its creator. */
const val MAX_MEMBERS = 4

/** A challenge holds 2-4 members and a person may be in two at once. */
const val MAX_ACTIVE_CHALLENGES = 2
