package com.example.buddyworkout.data.auth

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.example.buddyworkout.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

/**
 * Credential Manager needs an Activity context, so this is a plain suspend
 * function called from the composable rather than something a ViewModel or
 * repository holds — that is what keeps an Activity out of both.
 *
 * The server client ID is the *web* OAuth client (`client_type` 3), which the
 * google-services plugin generates as `default_web_client_id`. Passing the
 * Android client here fails at runtime with a developer error.
 */
suspend fun getGoogleIdToken(context: Context): Result<String> = try {
    val option = GetGoogleIdOption.Builder()
        .setServerClientId(context.getString(R.string.default_web_client_id))
        // false so a first-time user is offered their accounts rather than
        // being told there is no credential.
        .setFilterByAuthorizedAccounts(false)
        .setAutoSelectEnabled(false)
        .build()

    val response = CredentialManager.create(context).getCredential(
        context = context,
        request = GetCredentialRequest.Builder().addCredentialOption(option).build(),
    )

    val credential = response.credential
    if (credential is CustomCredential &&
        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
    ) {
        Result.success(GoogleIdTokenCredential.createFrom(credential.data).idToken)
    } else {
        Result.failure(AuthException(AuthError.Unknown))
    }
} catch (e: GetCredentialCancellationException) {
    Result.failure(AuthException(AuthError.Cancelled))
} catch (e: NoCredentialException) {
    Result.failure(AuthException(AuthError.NoGoogleAccount))
} catch (e: GetCredentialException) {
    Result.failure(AuthException(AuthError.Unknown))
}

/**
 * Forgets the selected account. Without this, signing out then signing back in
 * silently reuses the same Google account and the user cannot switch.
 */
suspend fun clearGoogleCredentialState(context: Context) {
    runCatching {
        CredentialManager.create(context).clearCredentialState(ClearCredentialStateRequest())
    }
}
