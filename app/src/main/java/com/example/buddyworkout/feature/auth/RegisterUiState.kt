package com.example.buddyworkout.feature.auth

data class RegisterUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val name: String = "",
    val email: String = "",
    val password: String = "",
) {
    val canSubmit: Boolean
        get() = name.isNotBlank() && email.isNotBlank() && password.length >= 6 && !isLoading
}
