package com.example.buddyworkout.data.auth

/** Records what each command was called with and replays a canned outcome. */
class FakeAuthRepository : AuthRepository {

    override var currentUser: AuthUser? = null

    var result: Result<Unit> = Result.success(Unit)

    var signInCalls: Int = 0
    var registerCalls: Int = 0
    var signOutCalls: Int = 0

    var lastEmail: String? = null
    var lastPassword: String? = null
    var lastName: String? = null
    var lastIdToken: String? = null

    /** Set to suspend a command indefinitely so loading state can be observed. */
    var hang: Boolean = false

    private suspend fun outcome(): Result<Unit> {
        if (hang) kotlinx.coroutines.awaitCancellation()
        return result
    }

    override suspend fun signIn(email: String, password: String): Result<Unit> {
        signInCalls++
        lastEmail = email
        lastPassword = password
        return outcome()
    }

    override suspend fun register(name: String, email: String, password: String): Result<Unit> {
        registerCalls++
        lastName = name
        lastEmail = email
        lastPassword = password
        return outcome()
    }

    override suspend fun signInWithGoogleIdToken(idToken: String): Result<Unit> {
        lastIdToken = idToken
        return outcome()
    }

    override suspend fun signOut() {
        signOutCalls++
    }

    fun failWith(error: AuthError) {
        result = Result.failure(AuthException(error))
    }
}
