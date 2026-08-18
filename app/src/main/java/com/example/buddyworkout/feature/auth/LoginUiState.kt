package com.example.buddyworkout.feature.auth

data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val email: String = "",
    val password: String = "",
) {
    val canSubmit: Boolean get() = email.isNotBlank() && password.isNotBlank() && !isLoading
}
