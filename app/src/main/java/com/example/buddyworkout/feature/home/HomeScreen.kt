package com.example.buddyworkout.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.buddyworkout.core.ui.component.ActionTile
import com.example.buddyworkout.core.ui.component.AvatarStack
import com.example.buddyworkout.core.ui.component.BwIconButton
import com.example.buddyworkout.core.ui.component.BwTopBar
import com.example.buddyworkout.core.ui.component.ChallengeCard
import com.example.buddyworkout.core.ui.component.NoticeCard
import com.example.buddyworkout.core.ui.component.SectionTitle
import com.example.buddyworkout.core.ui.icon.BwIcons
import com.example.buddyworkout.core.ui.preview.PreviewData
import com.example.buddyworkout.core.ui.theme.BuddyWorkoutTheme
import com.example.buddyworkout.core.ui.theme.BwColors
import com.example.buddyworkout.core.ui.theme.BwSpace

@Composable
fun HomeScreen(
    state: HomeUiState,
    onCreateChallenge: () -> Unit,
    onInviteBuddies: () -> Unit,
    onChallengeClick: (String) -> Unit,
    onNotifications: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BwColors.Bg),
    ) {
        BwTopBar(
            title = "BuddyWorkout",
            actions = {
                BwIconButton(
                    icon = BwIcons.Bell,
                    onClick = onNotifications,
                    contentDescription = "Notifications",
                )
            },
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(BwSpace.Gutter),
            verticalArrangement = Arrangement.spacedBy(BwSpace.Md),
        ) {
            item {
                Text(
                    text = state.greeting,
                    style = MaterialTheme.typography.headlineMedium,
                    color = BwColors.Ink,
                )
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(BwSpace.Md)) {
                    ActionTile(
                        title = "Create challenge",
                        description = "Pick buddies and a deadline",
                        icon = BwIcons.Plus,
                        onClick = onCreateChallenge,
                        modifier = Modifier.weight(1f),
                    )
                    ActionTile(
                        title = "Invite buddies",
                        description = "Share your invite link",
                        icon = BwIcons.PersonAdd,
                        onClick = onInviteBuddies,
                        filled = false,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            if (state.atChallengeLimit) {
                item {
                    NoticeCard(
                        text = "You're in 2 active challenges — the maximum. Finish or cancel one to start another.",
                        icon = BwIcons.Info,
                    )
                }
            }

            item { SectionTitle(text = "Active challenges", hint = state.slotsLabel) }

            if (state.activeChallenges.isEmpty()) {
                item {
                    Text(
                        text = "No active challenges yet. Create one and pull a buddy in.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = BwColors.Muted,
                    )
                }
            } else {
                items(state.activeChallenges.size) { index ->
                    val challenge = state.activeChallenges[index]
                    ChallengeCard(
                        title = challenge.title,
                        summary = challenge.summary,
                        progress = challenge.progress,
                        stat = challenge.stat,
                        onClick = { onChallengeClick(challenge.id) },
                        trailing = { AvatarStack(avatars = challenge.members) },
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 380, heightDp = 800)
@Composable
private fun HomeScreenPreview() = BuddyWorkoutTheme {
    HomeScreen(PreviewData.home, {}, {}, {}, {})
}

@Preview(showBackground = true, widthDp = 380, heightDp = 800)
@Composable
private fun HomeScreenEmptyPreview() = BuddyWorkoutTheme {
    HomeScreen(PreviewData.home.copy(activeChallenges = emptyList(), atChallengeLimit = false), {}, {}, {}, {})
}
