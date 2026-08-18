package com.example.buddyworkout.feature.record

data class RecordUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val reps: Int = 0,
    /** e.g. `"Good form"`, or null when nothing is being judged. */
    val formLabel: String? = null,
    /** e.g. `"04:12"`. */
    val elapsedLabel: String = "00:00",
    val isRunning: Boolean = false,
    val hasCameraPermission: Boolean = false,
    /** e.g. `"Position yourself in frame"`, shown when the pose is not readable. */
    val guidance: String? = null,
)
