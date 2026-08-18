package com.example.buddyworkout.feature.invite

data class InviteUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    /** The full shareable URL, e.g. `"https://commworkout.app/i/9F3KQ2"`. */
    val link: String = "",
    /** Just the code, shown large so it can be read aloud. */
    val code: String = "",
    val copied: Boolean = false,
)
