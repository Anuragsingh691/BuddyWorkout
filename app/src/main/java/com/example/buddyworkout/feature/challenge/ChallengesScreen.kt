package com.example.buddyworkout.feature.challenge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.buddyworkout.core.ui.component.AvatarStack
import com.example.buddyworkout.core.ui.component.BwSegmentedTabs
import com.example.buddyworkout.core.ui.component.BwTopBar
import com.example.buddyworkout.core.ui.component.ChallengeCard
import com.example.buddyworkout.core.ui.component.Pill
import com.example.buddyworkout.core.ui.component.PillTone
import com.example.buddyworkout.core.ui.preview.PreviewData
import com.example.buddyworkout.core.ui.theme.BuddyWorkoutTheme
import com.example.buddyworkout.core.ui.theme.BwColors
import com.example.buddyworkout.core.ui.theme.BwSpace

@Composable
fun ChallengesScreen(
    state: ChallengesUiState,
    onTabSelect: (Int) -> Unit,
    onChallengeClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BwColors.Bg),
    ) {
        BwTopBar(title = "Challenges")

        BwSegmentedTabs(
            options = listOf("Active", "Completed"),
            selectedIndex = state.selectedTab,
            onSelect = onTabSelect,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = BwSpace.Gutter, vertical = BwSpace.Sm),
        )

        if (state.visible.isEmpty()) {
            Text(
                text = state.emptyMessage,
                style = MaterialTheme.typography.bodyLarge,
                color = BwColors.Muted,
                modifier = Modifier.padding(BwSpace.Gutter),
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(BwSpace.Gutter),
                verticalArrangement = Arrangement.spacedBy(BwSpace.Md),
            ) {
                items(state.visible.size) { index ->
                    val challenge = state.visible[index]
                    ChallengeCard(
                        title = challenge.title,
                        summary = challenge.summary,
                        progress = challenge.progress,
                        stat = challenge.stat,
                        onClick = { onChallengeClick(challenge.id) },
                        accent = if (challenge.isCompleted) BwColors.Muted else BwColors.Primary,
                        trailing = {
                            if (challenge.isCompleted) {
                                Pill(text = "Ended", tone = PillTone.Neutral)
                            } else {
                                AvatarStack(avatars = challenge.members)
                            }
                        },
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 380, heightDp = 800)
@Composable
private fun ChallengesActivePreview() = BuddyWorkoutTheme {
    ChallengesScreen(PreviewData.challengesTab, {}, {})
}

@Preview(showBackground = true, widthDp = 380, heightDp = 800)
@Composable
private fun ChallengesCompletedPreview() = BuddyWorkoutTheme {
    ChallengesScreen(PreviewData.challengesTab.copy(selectedTab = 1), {}, {})
}
