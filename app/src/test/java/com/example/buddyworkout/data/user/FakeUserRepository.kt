package com.example.buddyworkout.data.user

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow

/** Records what each command was called with and replays a canned outcome. */
class FakeUserRepository : UserRepository {

    var ensureCalls: Int = 0
    var lastName: String? = null
    var lastPhone: String? = null

    var avatarCalls: Int = 0
    var lastUri: String? = null

    var ensureResult: Result<Unit> = Result.success(Unit)
    var avatarResult: Result<Unit> = Result.success(Unit)

    val profile = MutableStateFlow<UserProfile?>(null)

    /** Set to make [observeProfile] fail the way a rejected listen does. */
    var observeError: Throwable? = null

    override suspend fun ensureProfile(name: String?, phone: String?): Result<Unit> {
        ensureCalls++
        lastName = name
        lastPhone = phone
        return ensureResult
    }

    override suspend fun saveAvatar(uri: String): Result<Unit> {
        avatarCalls++
        lastUri = uri
        return avatarResult
    }

    val avatar = MutableStateFlow<ByteArray?>(null)

    override fun observeAvatar(): Flow<ByteArray?> = avatar

    override fun observeProfile(): Flow<UserProfile?> =
        observeError?.let { flow<UserProfile?> { throw it } } ?: profile
}
