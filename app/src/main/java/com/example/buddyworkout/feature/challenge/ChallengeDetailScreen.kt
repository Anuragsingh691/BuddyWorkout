package com.example.buddyworkout.feature.challenge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.buddyworkout.core.ui.component.BwButton
import com.example.buddyworkout.core.ui.component.BwButtonVariant
import com.example.buddyworkout.core.ui.component.BwCard
import com.example.buddyworkout.core.ui.component.BwTopBar
import com.example.buddyworkout.core.ui.component.CountdownCard
import com.example.buddyworkout.core.ui.component.DetailRow
import com.example.buddyworkout.core.ui.component.LeaderboardRow
import com.example.buddyworkout.core.ui.component.Pill
import com.example.buddyworkout.core.ui.component.PillTone
import com.example.buddyworkout.core.ui.component.RowTone
import com.example.buddyworkout.core.ui.component.SectionTitle
import com.example.buddyworkout.core.ui.preview.PreviewData
import com.example.buddyworkout.core.ui.theme.BuddyWorkoutTheme
import com.example.buddyworkout.core.ui.theme.BwColors
import com.example.buddyworkout.core.ui.theme.BwSpace

@Composable
fun ChallengeDetailScreen(
    state: ChallengeDetailUiState,
    onBack: () -> Unit,
    onRecordWorkout: () -> Unit,
    onSeeWinner: () -> Unit,
    onCancelChallenge: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BwColors.Bg),
    ) {
        BwTopBar(
            title = state.title,
            overline = "CHALLENGE",
            onBack = onBack,
            actions = {
                Pill(
                    text = if (state.isCompleted) "Ended" else "Live",
                    tone = if (state.isCompleted) PillTone.Neutral else PillTone.Live,
                )
            },
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(BwSpace.Gutter),
            verticalArrangement = Arrangement.spacedBy(BwSpace.Md),
        ) {
            item { CountdownCard(remaining = state.remaining, members = state.members) }

            item {
                BwCard(modifier = Modifier.fillMaxWidth()) {
                    DetailRow(label = "Starts", value = state.startsAt)
                    DetailRow(label = "Ends", value = state.endsAt)
                    DetailRow(label = "Exercise", value = "Pushups")
                }
            }

            item { SectionTitle(text = "Leaderboard", hint = "Live") }

            item {
                BwCard(modifier = Modifier.fillMaxWidth()) {
                    state.leaderboard.forEachIndexed { index, participant ->
                        LeaderboardRow(
                            avatar = participant.avatar,
                            name = participant.name,
                            rank = participant.rank,
                            subtitle = participant.subtitle,
                            value = participant.reps,
                            tone = when {
                                state.isCompleted && participant.rank == 1 -> RowTone.Gold
                                participant.isMe -> RowTone.Highlight
                                else -> RowTone.Plain
                            },
                            showDivider = index != state.leaderboard.lastIndex,
                        )
                    }
                }
            }

            item {
                if (state.isCompleted) {
                    BwButton(
                        text = "See the winner",
                        onClick = onSeeWinner,
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    BwButton(
                        text = "Record workout",
                        onClick = onRecordWorkout,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            if (state.isCreator && !state.isCompleted) {
                item {
                    BwButton(
                        text = "Cancel challenge",
                        onClick = onCancelChallenge,
                        variant = BwButtonVariant.DangerGhost,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 380, heightDp = 900)
@Composable
private fun ChallengeDetailPreview() = BuddyWorkoutTheme {
    ChallengeDetailScreen(PreviewData.challengeDetail, {}, {}, {}, {})
}

@Preview(showBackground = true, widthDp = 380, heightDp = 900)
@Composable
private fun ChallengeDetailCompletedPreview() = BuddyWorkoutTheme {
    ChallengeDetailScreen(
        PreviewData.challengeDetail.copy(isCompleted = true, remaining = "Ended"),
        {}, {}, {}, {},
    )
}
