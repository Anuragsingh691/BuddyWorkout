package com.example.buddyworkout.feature.challenge.create

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.buddyworkout.core.ui.component.AvatarStack
import com.example.buddyworkout.core.ui.component.BwButton
import com.example.buddyworkout.core.ui.component.BwCard
import com.example.buddyworkout.core.ui.component.BwTopBar
import com.example.buddyworkout.core.ui.component.NoticeCard
import com.example.buddyworkout.core.ui.icon.BwIcons
import com.example.buddyworkout.core.ui.preview.PreviewData
import com.example.buddyworkout.core.ui.theme.BuddyWorkoutTheme
import com.example.buddyworkout.core.ui.theme.BwColors
import com.example.buddyworkout.core.ui.theme.BwSpace

@Composable
fun CreateChallengeScreen(
    state: CreateChallengeUiState,
    onBack: () -> Unit,
    onPickBuddies: () -> Unit,
    onPickWindow: () -> Unit,
    onCreate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BwColors.Bg),
    ) {
        BwTopBar(title = "New challenge", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(BwSpace.Gutter),
            verticalArrangement = Arrangement.spacedBy(BwSpace.Md),
        ) {
            NoticeCard(
                text = "Pushups only for now. Everyone gets the same window, and the winner is whoever logs the most reps.",
                icon = BwIcons.Info,
            )

            state.error?.let { message ->
                NoticeCard(
                    text = message,
                    icon = BwIcons.AlertTriangle,
                    containerColor = BwColors.DangerTint,
                    contentColor = BwColors.DangerInk,
                )
            }

            PickerRow(
                icon = BwIcons.Users,
                label = "Buddies",
                value = state.buddiesLabel,
                onClick = onPickBuddies,
                trailing = {
                    if (state.selectedBuddies.isNotEmpty()) {
                        AvatarStack(avatars = state.selectedBuddies.map { it.avatar })
                    }
                },
            )

            PickerRow(
                icon = BwIcons.Calendar,
                label = "Starts",
                value = state.startLabel,
                onClick = onPickWindow,
            )

            PickerRow(
                icon = BwIcons.Clock,
                label = "Ends",
                value = state.endLabel.ifBlank { "Not set" },
                onClick = onPickWindow,
            )

            if (state.durationLabel.isNotBlank()) {
                Text(
                    text = "Runs for ${state.durationLabel}.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = BwColors.Muted,
                )
            }

            BwButton(
                text = if (state.isLoading) "Creating…" else "Create challenge",
                onClick = onCreate,
                enabled = state.canCreate,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun PickerRow(
    icon: ImageVector,
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
) {
    BwCard(modifier = modifier.fillMaxWidth(), onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = BwColors.Primary,
                modifier = Modifier.size(20.dp),
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = BwSpace.Md),
            ) {
                Text(label, style = MaterialTheme.typography.titleSmall, color = BwColors.Ink)
                Text(value, style = MaterialTheme.typography.bodyMedium, color = BwColors.Muted)
            }
            trailing?.invoke()
            Icon(
                imageVector = BwIcons.ChevronRight,
                contentDescription = null,
                tint = BwColors.Placeholder,
                modifier = Modifier
                    .padding(start = BwSpace.Sm)
                    .size(20.dp),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 380, heightDp = 800)
@Composable
private fun CreateChallengeFilledPreview() = BuddyWorkoutTheme {
    CreateChallengeScreen(PreviewData.createChallenge, {}, {}, {}, {})
}

@Preview(showBackground = true, widthDp = 380, heightDp = 800)
@Composable
private fun CreateChallengeEmptyPreview() = BuddyWorkoutTheme {
    CreateChallengeScreen(CreateChallengeUiState(), {}, {}, {}, {})
}
