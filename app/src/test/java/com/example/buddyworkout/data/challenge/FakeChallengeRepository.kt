package com.example.buddyworkout.data.challenge

import com.example.buddyworkout.feature.challenge.create.ChallengeWindow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** Records what each command was called with and replays a canned outcome. */
class FakeChallengeRepository : ChallengeRepository {

    val challenges = MutableStateFlow<List<ChallengeWithParticipants>>(emptyList())
    val challenge = MutableStateFlow<ChallengeWithParticipants?>(null)

    var createCalls: Int = 0
    var lastWindow: ChallengeWindow? = null
    var createResult: Result<String> = Result.success("new-id")

    var cancelCalls: Int = 0
    var lastCancelledId: String? = null
    var cancelResult: Result<Unit> = Result.success(Unit)

    override fun observeMyChallenges(): Flow<List<ChallengeWithParticipants>> = challenges

    override fun observeChallenge(challengeId: String): Flow<ChallengeWithParticipants?> = challenge

    override suspend fun createChallenge(window: ChallengeWindow): Result<String> {
        createCalls++
        lastWindow = window
        return createResult
    }

    override suspend fun cancelChallenge(challengeId: String): Result<Unit> {
        cancelCalls++
        lastCancelledId = challengeId
        return cancelResult
    }
}
