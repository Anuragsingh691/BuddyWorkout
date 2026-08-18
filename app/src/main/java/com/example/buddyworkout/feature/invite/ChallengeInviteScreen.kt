package com.example.buddyworkout.feature.invite

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.buddyworkout.core.ui.component.BwButton
import com.example.buddyworkout.core.ui.component.BwButtonVariant
import com.example.buddyworkout.core.ui.component.BwCard
import com.example.buddyworkout.core.ui.component.BwTopBar
import com.example.buddyworkout.core.ui.component.CountdownCard
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
        BwTopBar(title = "Challenge invite")

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(BwSpace.Gutter),
            verticalArrangement = Arrangement.spacedBy(BwSpace.Md),
        ) {
            Text(
                text = state.headline,
                style = MaterialTheme.typography.headlineSmall,
                color = BwColors.Ink,
            )
            Text(
                text = "Join the challenge and every rep you log counts towards the leaderboard.",
                style = MaterialTheme.typography.bodyLarge,
                color = BwColors.Muted,
            )

            state.error?.let { message ->
                NoticeCard(
                    text = message,
                    icon = BwIcons.AlertTriangle,
                    containerColor = BwColors.DangerTint,
                    contentColor = BwColors.DangerInk,
                )
            }

            if (state.blockedByLimit) {
                NoticeCard(
                    text = "You're already in 2 active challenges. Finish or cancel one before joining this.",
                    icon = BwIcons.Info,
                )
            }

            CountdownCard(remaining = state.remaining, members = state.members)

            BwCard(modifier = Modifier.fillMaxWidth()) {
                DetailRow(label = "Exercise", value = "Pushups")
                DetailRow(label = "Starts", value = state.startsAt)
                DetailRow(label = "Ends", value = state.endsAt)
            }

            Spacer(Modifier.height(BwSpace.Xs))

            BwButton(
                text = "Accept & join",
                onClick = onAccept,
                enabled = state.canAccept,
                modifier = Modifier.fillMaxWidth(),
            )
            BwButton(
                text = "Not now",
                onClick = onDecline,
                variant = BwButtonVariant.Outline,
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
