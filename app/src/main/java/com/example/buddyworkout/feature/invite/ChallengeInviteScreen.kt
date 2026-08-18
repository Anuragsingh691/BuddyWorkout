package com.example.buddyworkout.feature.invite

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.buddyworkout.core.ui.component.AvatarStack
import com.example.buddyworkout.core.ui.component.BwButton
import com.example.buddyworkout.core.ui.component.BwButtonVariant
import com.example.buddyworkout.core.ui.component.BwCard
import com.example.buddyworkout.core.ui.component.BwTopBar
import com.example.buddyworkout.core.ui.component.DetailRow
import com.example.buddyworkout.core.ui.component.NoticeCard
import com.example.buddyworkout.core.ui.icon.BwIcons
import com.example.buddyworkout.core.ui.preview.PreviewData
import com.example.buddyworkout.core.ui.theme.BuddyWorkoutTheme
import com.example.buddyworkout.core.ui.theme.BwColors
import com.example.buddyworkout.core.ui.theme.BwSpace

@Composable
fun ChallengeInviteScreen(
    state: ChallengeInviteUiState,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BwColors.Bg),
    ) {
        BwTopBar(
            title = "Challenge invite",
            onBack = onDecline,
            navigationIcon = BwIcons.Close,
            navigationDescription = "Dismiss",
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = BwSpace.Gutter),
        ) {
            AvatarStack(
                avatars = state.members,
                size = 60.dp,
                overlap = 16.dp,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 10.dp, bottom = 18.dp),
            )

            Text(
                text = state.headline,
                style = MaterialTheme.typography.headlineSmall,
                color = BwColors.Ink,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = state.subline,
                style = MaterialTheme.typography.bodyMedium,
                color = BwColors.Muted,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(top = BwSpace.Sm)
                    .fillMaxWidth(),
            )

            state.error?.let { message ->
                Box(Modifier.padding(top = BwSpace.Lg)) {
                    NoticeCard(
                        text = message,
                        icon = BwIcons.AlertTriangle,
                        containerColor = BwColors.DangerTint,
                        contentColor = BwColors.DangerInk,
                    )
                }
            }

            // Not in the export, which never draws the blocked case. It is the
            // only thing that explains why Accept is dead, so it stays.
            if (state.blockedByLimit) {
                Box(Modifier.padding(top = BwSpace.Lg)) {
                    NoticeCard(
                        text = "You're already in 2 active challenges. " +
                            "Finish or cancel one before joining this.",
                        icon = BwIcons.Info,
                    )
                }
            }

            BwCard(modifier = Modifier.padding(top = 18.dp)) {
                DetailRow(label = "Exercise", value = state.exercise)
                Box(Modifier.padding(top = BwSpace.Md)) {
                    DetailRow(label = "Members", value = state.memberCountLabel)
                }
                Box(Modifier.padding(top = BwSpace.Md)) {
                    DetailRow(label = "Window", value = state.windowLabel)
                }
                Box(Modifier.padding(top = BwSpace.Md)) {
                    DetailRow(label = "Ends", value = state.endsLabel)
                }
            }

            Box(Modifier.padding(bottom = BwSpace.Gutter))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = BwSpace.Gutter)
                .padding(bottom = BwSpace.Gutter),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            BwButton(
                text = "Accept & join",
                onClick = onAccept,
                enabled = state.canAccept,
                modifier = Modifier.fillMaxWidth(),
            )
            BwButton(
                text = "Decline",
                onClick = onDecline,
                variant = BwButtonVariant.DangerGhost,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 380, heightDp = 800)
@Composable
private fun ChallengeInvitePreview() = BuddyWorkoutTheme {
    ChallengeInviteScreen(PreviewData.challengeInvite, {}, {})
}

@Preview(showBackground = true, widthDp = 380, heightDp = 800)
@Composable
private fun ChallengeInviteBlockedPreview() = BuddyWorkoutTheme {
    ChallengeInviteScreen(PreviewData.challengeInvite.copy(blockedByLimit = true), {}, {})
}
