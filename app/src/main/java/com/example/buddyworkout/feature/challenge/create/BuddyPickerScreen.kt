package com.example.buddyworkout.feature.challenge.create

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.buddyworkout.core.ui.component.BwButton
import com.example.buddyworkout.core.ui.component.BwCard
import com.example.buddyworkout.core.ui.component.BwTextField
import com.example.buddyworkout.core.ui.component.BwTopBar
import com.example.buddyworkout.core.ui.component.LeaderboardRow
import com.example.buddyworkout.core.ui.component.NoticeCard
import com.example.buddyworkout.core.ui.component.RowTone
import com.example.buddyworkout.core.ui.icon.BwIcons
import com.example.buddyworkout.core.ui.preview.PreviewData
import com.example.buddyworkout.core.ui.theme.BuddyWorkoutTheme
import com.example.buddyworkout.core.ui.theme.BwColors
import com.example.buddyworkout.core.ui.theme.BwSpace

@Composable
fun BuddyPickerScreen(
    state: BuddyPickerUiState,
    onQueryChange: (String) -> Unit,
    onToggleBuddy: (String) -> Unit,
    onDone: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BwColors.Bg),
    ) {
        BwTopBar(title = "Add buddies", onBack = onBack)

        BwTextField(
            value = state.query,
            onValueChange = onQueryChange,
            placeholder = "Search your buddies",
            trailingIcon = BwIcons.Search,
            modifier = Modifier.padding(horizontal = BwSpace.Gutter, vertical = BwSpace.Sm),
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(horizontal = BwSpace.Gutter),
            verticalArrangement = Arrangement.spacedBy(BwSpace.Md),
        ) {
            if (state.atLimit) {
                item {
                    NoticeCard(
                        text = "That's $MAX_BUDDIES buddies — the most a challenge holds. Deselect someone to swap.",
                        icon = BwIcons.Info,
                    )
                }
            }

            item {
                BwCard(modifier = Modifier.fillMaxWidth()) {
                    state.buddies.forEachIndexed { index, buddy ->
                        LeaderboardRow(
                            avatar = buddy.avatar,
                            name = buddy.name,
                            subtitle = buddy.subtitle,
                            tone = if (buddy.selected) RowTone.Highlight else RowTone.Plain,
                            showDivider = index != state.buddies.lastIndex,
                            onClick = { onToggleBuddy(buddy.uid) },
                            trailing = {
                                if (buddy.selected) {
                                    Icon(
                                        imageVector = BwIcons.Check,
                                        contentDescription = "Selected",
                                        tint = BwColors.Primary,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                            },
                        )
                    }
                }
            }
        }

        BwButton(
            text = state.doneLabel,
            onClick = onDone,
            enabled = state.selectedCount > 0,
            modifier = Modifier
                .fillMaxWidth()
                .padding(BwSpace.Gutter),
        )
    }
}

@Preview(showBackground = true, widthDp = 380, heightDp = 800)
@Composable
private fun BuddyPickerPreview() = BuddyWorkoutTheme {
    BuddyPickerScreen(PreviewData.buddyPicker, {}, {}, {}, {})
}
