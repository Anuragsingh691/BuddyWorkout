package com.example.buddyworkout.feature.profile

import com.example.buddyworkout.core.ui.component.AvatarUi

/** One label/value pair in the profile stats card. Display-ready strings only. */
data class StatUi(val label: String, val value: String)

data class ProfileUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val name: String = "",
    val email: String = "",
    val avatar: AvatarUi = AvatarUi(""),
    val stats: List<StatUi> = emptyList(),
    /** Debug builds surface the component gallery from here. */
    val showGallery: Boolean = false,
)
