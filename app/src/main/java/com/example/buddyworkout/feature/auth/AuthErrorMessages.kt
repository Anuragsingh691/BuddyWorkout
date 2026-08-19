package com.example.buddyworkout.feature.auth

import com.example.buddyworkout.data.auth.AuthError

/**
 * The user-facing copy for each failure. [AuthError.Cancelled] has none — a
 * dismissed Google sheet is a decision, not a problem — so callers must treat
 * null as "show nothing".
 */
internal fun AuthError.message(): String? = when (this) {
    AuthError.InvalidCredentials -> "That email and password don't match."
    AuthError.EmailInUse -> "That email already has an account. Try signing in."
    AuthError.WeakPassword -> "Use at least 6 characters for your password."
    AuthError.Network -> "No connection. Check your network and try again."
    AuthError.NoGoogleAccount -> "No Google account on this device. Add one, or use email."
    AuthError.Cancelled -> null
    AuthError.Unknown -> "Something went wrong. Please try again."
}
