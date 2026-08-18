package com.example.buddyworkout.feature.result

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.buddyworkout.core.ui.component.BwButton
import com.example.buddyworkout.core.ui.component.BwCard
import com.example.buddyworkout.core.ui.component.BwTopBar
import com.example.buddyworkout.core.ui.component.LeaderboardRow
import com.example.buddyworkout.core.ui.component.RowTone
import com.example.buddyworkout.core.ui.component.SectionTitle
import com.example.buddyworkout.core.ui.component.WinnerBanner
import com.example.buddyworkout.core.ui.preview.PreviewData
import com.example.buddyworkout.core.ui.theme.BuddyWorkoutTheme
import com.example.buddyworkout.core.ui.theme.BwColors
import com.example.buddyworkout.core.ui.theme.BwSpace

@Composable
fun WinnerScreen(
    state: WinnerUiState,
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BwColors.Bg),
    ) {
        BwTopBar(title = "Results", overline = "PUSHUP CHALLENGE")

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(BwSpace.Gutter),
            verticalArrangement = Arrangement.spacedBy(BwSpace.Md),
        ) {
            WinnerBanner(winner = state.winnerName, detail = state.detail)

            SectionTitle(text = "Final leaderboard")

            BwCard(modifier = Modifier.fillMaxWidth()) {
                state.leaderboard.forEachIndexed { index, participant ->
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
                        showDivider = index != state.leaderboard.lastIndex,
                    )
                }
            }

            BwButton(
                text = "Back to home",
                onClick = onBackToHome,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 380, heightDp = 800)
@Composable
private fun WinnerScreenPreview() = BuddyWorkoutTheme {
    WinnerScreen(PreviewData.winner, {})
}
