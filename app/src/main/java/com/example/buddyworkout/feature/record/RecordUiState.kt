package com.example.buddyworkout.feature.record

data class RecordUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    /** e.g. `"Pushups"` — names the exercise in the app bar. */
    val exercise: String = "Pushups",
    val reps: Int = 0,
    /** e.g. `"Good form"`, or null when nothing is being judged. */
    val formLabel: String? = null,
    /** e.g. `"04:12"`. */
    val elapsedLabel: String = "00:00",
    /** e.g. `"PUSHUP CHALLENGE · 1st of 4"` — the strip over the camera. */
    val challengeLabel: String = "",
    /** e.g. `"128 total · +12 this session"`. */
    val totalsLabel: String = "",
    val isRunning: Boolean = false,
    val hasCameraPermission: Boolean = false,
    /** e.g. `"Position yourself in frame"`, shown when the pose is not readable. */
    val guidance: String? = null,
)
