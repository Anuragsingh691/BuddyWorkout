package com.example.buddyworkout.feature.record

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.buddyworkout.core.ui.component.BwButton
import com.example.buddyworkout.core.ui.component.BwButtonVariant
import com.example.buddyworkout.core.ui.component.BwTopBar
import com.example.buddyworkout.core.ui.component.CameraOverlay
import com.example.buddyworkout.core.ui.component.NoticeCard
import com.example.buddyworkout.core.ui.icon.BwIcons
import com.example.buddyworkout.core.ui.preview.PreviewData
import com.example.buddyworkout.core.ui.theme.BuddyWorkoutTheme
import com.example.buddyworkout.core.ui.theme.BwColors
import com.example.buddyworkout.core.ui.theme.BwSpace

/**
 * Renders the overlay only — no CameraX, no ML Kit. The `cameraPreview` slot
 * stays empty, showing the dark camera surface. The pose pipeline is a later
 * step; this screen already has the shape it will need.
 */
@Composable
fun RecordScreen(
    state: RecordUiState,
    onGrantCameraPermission: () -> Unit,
    onStopAndSave: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BwColors.Bg),
    ) {
        BwTopBar(title = "Record workout", overline = "PUSHUPS", onBack = onBack)

        CameraOverlay(
            reps = state.reps,
            formLabel = state.guidance ?: state.formLabel,
            footerLabel = "Elapsed",
            footerValue = state.elapsedLabel,
            showPoseSkeleton = state.isRunning,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = BwSpace.Gutter, vertical = BwSpace.Md),
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(BwSpace.Gutter),
            verticalArrangement = Arrangement.spacedBy(BwSpace.Md),
        ) {
            if (!state.hasCameraPermission) {
                NoticeCard(
                    text = "Camera access is needed to count your reps. Frames never leave your phone — only the count is saved.",
                    icon = BwIcons.Video,
                )
                BwButton(
                    text = "Allow camera",
                    onClick = onGrantCameraPermission,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                BwButton(
                    text = "Stop & save",
                    onClick = onStopAndSave,
                    icon = BwIcons.Stop,
                    variant = BwButtonVariant.DangerGhost,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 380, heightDp = 800)
@Composable
private fun RecordRunningPreview() = BuddyWorkoutTheme {
    RecordScreen(PreviewData.record, {}, {}, {})
}

@Preview(showBackground = true, widthDp = 380, heightDp = 800)
@Composable
private fun RecordNeedsPermissionPreview() = BuddyWorkoutTheme {
    RecordScreen(
        PreviewData.record.copy(hasCameraPermission = false, isRunning = false, reps = 0),
        {}, {}, {},
    )
}
