package com.example.buddyworkout.feature.challenge

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.buddyworkout.core.ui.component.BwButton
import com.example.buddyworkout.core.ui.component.BwIconButton
import com.example.buddyworkout.core.ui.component.BwTopBar
import com.example.buddyworkout.core.ui.component.CountdownCard
import com.example.buddyworkout.core.ui.component.LeaderboardRow
import com.example.buddyworkout.core.ui.component.Pill
import com.example.buddyworkout.core.ui.component.PillTone
import com.example.buddyworkout.core.ui.component.RowTone
import com.example.buddyworkout.core.ui.component.SectionTitle
import com.example.buddyworkout.core.ui.icon.BwIcons
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
    onShareLink: () -> Unit = {},
    onOpenAddBuddy: () -> Unit = {},
    onAddBuddyEmailChange: (String) -> Unit = {},
    onSearchBuddy: () -> Unit = {},
    onConfirmAddBuddy: () -> Unit = {},
    onDismissAddBuddy: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var menuOpen by rememberSaveable { mutableStateOf(false) }
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BwColors.Bg),
    ) {
        BwTopBar(
            title = state.title,
            onBack = onBack,
            actions = {
                // The export draws the overflow button but never says what is
                // in it. Growing the challenge is the only action that has no
                // other home, so it lives here.
                Box {
                    BwIconButton(
                        icon = BwIcons.MoreVertical,
                        onClick = { menuOpen = true },
                        contentDescription = "More",
                    )
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(
                            text = { Text("Add by email") },
                            enabled = state.canAddBuddies,
                            onClick = {
                                menuOpen = false
                                onOpenAddBuddy()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Share invite link") },
                            enabled = state.canAddBuddies,
                            onClick = {
                                menuOpen = false
                                onShareLink()
                            },
                        )
                    }
                }
            },
        )

        AddBuddyDialog(
            state = state.addBuddy,
            onEmailChange = onAddBuddyEmailChange,
            onSearch = onSearchBuddy,
            onConfirm = onConfirmAddBuddy,
            onDismiss = onDismissAddBuddy,
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = BwSpace.Gutter),
        ) {
            item {
                CountdownCard(
                    remaining = state.remaining,
                    members = state.members,
                    // The deadline note only makes sense while the clock runs.
                    note = if (state.isCompleted) {
                        null
                    } else {
                        "Auto-declares the winner at the deadline. No early finish."
                    },
                )
            }
            item {
                SectionTitle(
                    text = if (state.isCompleted) "Final leaderboard" else "Live leaderboard",
                    modifier = Modifier.padding(top = 18.dp, bottom = 6.dp),
                    trailing = {
                        if (!state.isCompleted) Pill("Live", tone = PillTone.Live)
                    },
                )
            }
            // Bare on the background, not boxed: the export gives only the
            // viewer's own row a container.
            items(state.leaderboard.size) { index ->
                val participant = state.leaderboard[index]
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
                    // The export's highlighted row is a rounded card, so it
                    // carries no rule of its own.
                    showDivider = index != state.leaderboard.lastIndex && !participant.isMe,
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = BwSpace.Gutter)
                .padding(bottom = BwSpace.Gutter),
        ) {
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
                    icon = BwIcons.Video,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            if (state.isCreator && !state.isCompleted) {
                // A line of text, not a button: the export keeps the destructive
                // action deliberately quiet.
                Column(
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Cancel challenge",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = BwColors.Danger,
                        modifier = Modifier.clickable(onClick = onCancelChallenge),
                    )
                    Text(
                        text = "Creator only · voids it, no winner, frees a slot",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = BwColors.Muted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 2.dp),
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
