package com.example.buddyworkout.data.challenge

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
}
