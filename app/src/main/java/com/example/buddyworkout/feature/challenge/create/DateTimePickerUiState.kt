package com.example.buddyworkout.feature.challenge.create

data class DateTimePickerUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    /** Preset durations, e.g. `"6 hours"`, `"1 day"`, `"1 week"`. */
    val presets: List<String> = emptyList(),
    /** -1 when nothing is chosen yet. */
    val selectedPresetIndex: Int = -1,
    /** e.g. `"Now (Mon 18 Aug, 06:00)"`. */
    val startLabel: String = "",
    /** Empty until a preset is chosen. */
    val endLabel: String = "",
) {
    val canSave: Boolean get() = selectedPresetIndex >= 0 && !isLoading
}
