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
import com.example.buddyworkout.core.ui.component.Avatar
import com.example.buddyworkout.core.ui.component.BwIconButton
import com.example.buddyworkout.core.ui.component.BwTopBar
import com.example.buddyworkout.core.ui.component.ChallengeCard
import com.example.buddyworkout.core.ui.component.SectionTitle
import com.example.buddyworkout.core.ui.icon.BwIcons
import com.example.buddyworkout.core.ui.preview.PreviewData
import com.example.buddyworkout.core.ui.theme.BuddyWorkoutTheme
import com.example.buddyworkout.core.ui.theme.BwColors
import com.example.buddyworkout.core.ui.theme.BwSize
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
            title = state.greeting,
            overline = state.overline,
            actions = {
                BwIconButton(
                    icon = BwIcons.Bell,
                    onClick = onNotifications,
                    contentDescription = "Notifications",
                )
                state.avatar?.let { Avatar(it, size = BwSize.IconButton) }
            },
        )
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(BwSpace.Gutter),
            verticalArrangement = Arrangement.spacedBy(BwSpace.Md),
        ) {
            item {
                // Invite leads and takes the solid tile: the export puts growing
                // the buddy list ahead of starting another challenge.
                Row(horizontalArrangement = Arrangement.spacedBy(BwSpace.Md)) {
                    ActionTile(
                        title = "Invite buddies",
                        description = "Share your invite link",
                        icon = BwIcons.PersonAdd,
                        onClick = onInviteBuddies,
                        modifier = Modifier.weight(1f),
                    )
                    ActionTile(
                        title = "Create challenge",
                        description = "Pick buddies and a deadline",
                        icon = BwIcons.Plus,
                        onClick = onCreateChallenge,
                        filled = false,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            item { SectionTitle(text = "Active challenges", hint = "· tap to open →") }
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
                        // The export prints the standing alongside the count
                        // here, and as a pill on the Challenges tab.
                        stat = listOfNotNull(challenge.stat, challenge.rankLabel).joinToString(" · "),
                        accent = if (challenge.isLeading) BwColors.Primary else BwColors.Amber,
                        onClick = { onChallengeClick(challenge.id) },
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
    HomeScreen(PreviewData.home.copy(activeChallenges = emptyList()), {}, {}, {}, {})
}
