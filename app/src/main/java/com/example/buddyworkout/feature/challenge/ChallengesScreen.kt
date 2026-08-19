package com.example.buddyworkout.feature.challenge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.buddyworkout.core.ui.component.BwButton
import com.example.buddyworkout.core.ui.component.BwButtonVariant
import com.example.buddyworkout.core.ui.component.BwCard
import com.example.buddyworkout.core.ui.component.BwIconButton
import com.example.buddyworkout.core.ui.component.BwSegmentedTabs
import com.example.buddyworkout.core.ui.component.BwTopBar
import com.example.buddyworkout.core.ui.component.ChallengeCard
import com.example.buddyworkout.core.ui.component.Pill
import com.example.buddyworkout.core.ui.component.PillTone
import com.example.buddyworkout.core.ui.icon.BwIcons
import com.example.buddyworkout.core.ui.preview.PreviewData
import com.example.buddyworkout.core.ui.theme.BuddyWorkoutTheme
import com.example.buddyworkout.core.ui.theme.BwColors
import com.example.buddyworkout.core.ui.theme.BwSpace

@Composable
fun ChallengesScreen(
    state: ChallengesUiState,
    onTabSelect: (Int) -> Unit,
    onChallengeClick: (String) -> Unit,
    onCreateChallenge: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BwColors.Bg),
    ) {
        BwTopBar(
            title = "Challenges",
            actions = {
                BwIconButton(
                    icon = if (state.atChallengeLimit) BwIcons.Lock else BwIcons.Plus,
                    onClick = onCreateChallenge,
                    contentDescription = "Create challenge",
                    tinted = true,
                    tint = BwColors.PrimaryDark,
                    // The export dims the locked control rather than hiding it.
                    modifier = Modifier.alpha(if (state.atChallengeLimit) 0.45f else 1f),
                )
            },
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = BwSpace.Gutter),
        ) {
            if (state.atChallengeLimit) {
                BwCard(containerColor = BwColors.AmberTint) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(
                            imageVector = BwIcons.AlertTriangle,
                            contentDescription = null,
                            tint = BwColors.AmberInk,
                            modifier = Modifier.size(20.dp),
                        )
                        Text(
                            text = state.limitMessage,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                            ),
                            color = BwColors.AmberInk,
                        )
                    }
                }
            }
            BwSegmentedTabs(
                options = state.tabLabels,
                selectedIndex = state.selectedTab,
                onSelect = onTabSelect,
                modifier = Modifier.padding(top = BwSpace.Lg),
            )
        }

        if (state.visible.isEmpty()) {
            Text(
                text = state.emptyMessage,
                style = MaterialTheme.typography.bodyLarge,
                color = BwColors.Muted,
                modifier = Modifier
                    .weight(1f)
                    .padding(BwSpace.Gutter),
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
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
                        accent = if (challenge.isLeading) BwColors.Primary else BwColors.Amber,
                        trailing = {
                            when {
                                challenge.isCompleted -> Pill("Ended", tone = PillTone.Neutral)
                                challenge.rankLabel != null -> Pill(
                                    text = challenge.rankLabel!!,
                                    tone = if (challenge.isLeading) PillTone.Green else PillTone.Amber,
                                )
                            }
                        },
                    )
                }
            }
        }

        BwButton(
            text = if (state.atChallengeLimit) "Create challenge · limit reached" else "Create challenge",
            onClick = onCreateChallenge,
            variant = if (state.atChallengeLimit) BwButtonVariant.Outline else BwButtonVariant.Primary,
            icon = if (state.atChallengeLimit) BwIcons.Lock else null,
            enabled = !state.atChallengeLimit,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = BwSpace.Gutter)
                .padding(bottom = BwSpace.Gutter),
        )
    }
}

@Preview(showBackground = true, widthDp = 380, heightDp = 800)
@Composable
private fun ChallengesActivePreview() = BuddyWorkoutTheme {
    ChallengesScreen(PreviewData.challengesTab, {}, {}, {})
}

@Preview(showBackground = true, widthDp = 380, heightDp = 800)
@Composable
private fun ChallengesPastPreview() = BuddyWorkoutTheme {
    ChallengesScreen(PreviewData.challengesTab.copy(selectedTab = 1), {}, {}, {})
}

@Composable
@Preview(showBackground = true, widthDp = 380, heightDp = 800)
private fun ChallengesUnderLimitPreview() = BuddyWorkoutTheme {
    ChallengesScreen(
        PreviewData.challengesTab.copy(active = PreviewData.challenges.take(1)),
        {}, {}, {},
    )
}
