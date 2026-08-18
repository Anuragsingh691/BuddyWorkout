package com.example.buddyworkout.feature.result

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.buddyworkout.core.ui.component.BwButton
import com.example.buddyworkout.core.ui.component.BwCard
import com.example.buddyworkout.core.ui.component.BwTopBar
import com.example.buddyworkout.core.ui.component.DetailRow
import com.example.buddyworkout.core.ui.component.LeaderboardRow
import com.example.buddyworkout.core.ui.component.RowTone
import com.example.buddyworkout.core.ui.component.SectionTitle
import com.example.buddyworkout.core.ui.component.WinnerBanner
import com.example.buddyworkout.core.ui.preview.PreviewData
import com.example.buddyworkout.core.ui.theme.BwColors
import com.example.buddyworkout.core.ui.theme.BuddyWorkoutTheme
import com.example.buddyworkout.core.ui.theme.BwSpace

@Composable
fun WinnerScreen(
    state: WinnerUiState,
    onRematch: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BwColors.Bg),
    ) {
        BwTopBar(title = state.title, onBack = onBack)

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = BwSpace.Gutter),
        ) {
            item { WinnerBanner(winner = state.winnerName, detail = state.detail) }
            item { SectionTitle(text = "Final leaderboard", modifier = Modifier.padding(top = 20.dp)) }
            items(state.leaderboard.size) { index ->
                val participant = state.leaderboard[index]
                LeaderboardRow(
                    avatar = participant.avatar,
                    name = participant.name,
                    rank = participant.rank,
                    subtitle = participant.subtitle,
                    value = participant.reps,
                    tone = when {
                        participant.rank == 1 -> RowTone.Gold
                        participant.isMe -> RowTone.Highlight
                        else -> RowTone.Plain
                    },
                    // The winner's row is a rounded card, so it needs no rule.
                    showDivider = index != state.leaderboard.lastIndex && participant.rank != 1,
                )
            }
            item { SectionTitle(text = "Your activity", modifier = Modifier.padding(top = 20.dp)) }
            item {
                BwCard {
                    DetailRow(label = "Best session", value = state.bestSession)
                    Box(Modifier.padding(top = BwSpace.Md)) {
                        DetailRow(label = "Sessions logged", value = state.sessionsLogged)
                    }
                    Box(Modifier.padding(top = BwSpace.Md)) {
                        DetailRow(
                            label = "Avg form score",
                            value = state.avgFormScore,
                            valueColor = BwColors.PrimaryDark,
                        )
                    }
                }
            }
            item { Box(Modifier.padding(bottom = BwSpace.Gutter)) }
        }

        BwButton(
            text = "Rematch",
            onClick = onRematch,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = BwSpace.Gutter)
                .padding(bottom = BwSpace.Gutter),
        )
    }
}

@Preview(showBackground = true, widthDp = 380, heightDp = 900)
@Composable
private fun WinnerScreenPreview() = BuddyWorkoutTheme {
    WinnerScreen(PreviewData.winner, {}, {})
}
