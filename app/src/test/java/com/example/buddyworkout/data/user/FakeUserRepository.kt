package com.example.buddyworkout.data.user

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow

/** Records what each command was called with and replays a canned outcome. */
class FakeUserRepository : UserRepository {

    var ensureCalls: Int = 0
    var lastName: String? = null
    var lastPhone: String? = null

    var uploadCalls: Int = 0
    var lastUri: String? = null

    var ensureResult: Result<Unit> = Result.success(Unit)
    var uploadResult: Result<String> = Result.success("https://example.test/profile.jpg")

    val profile = MutableStateFlow<UserProfile?>(null)

    /** Set to make [observeProfile] fail the way a rejected listen does. */
    var observeError: Throwable? = null

    override suspend fun ensureProfile(name: String?, phone: String?): Result<Unit> {
        ensureCalls++
        lastName = name
        lastPhone = phone
        return ensureResult
    }

    override suspend fun uploadPhoto(uri: String): Result<String> {
        uploadCalls++
        lastUri = uri
        return uploadResult
    }

    override fun observeProfile(): Flow<UserProfile?> =
        observeError?.let { flow<UserProfile?> { throw it } } ?: profile
}
