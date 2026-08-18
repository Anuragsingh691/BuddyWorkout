package com.example.buddyworkout.feature.auth

data class RegisterUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val name: String = "",
    val email: String = "",
    val password: String = "",
    /** Optional in the mockups, and nothing persists it yet. */
    val phone: String = "",
    /**
     * Content URI of the picked profile photo, held as a String so the state
     * stays free of Android types. Shown locally only — uploading it waits on
     * the profile slice.
     */
    val photoUri: String? = null,
) {
    val canSubmit: Boolean
        get() = name.isNotBlank() && email.isNotBlank() && password.length >= 6 && !isLoading
}
