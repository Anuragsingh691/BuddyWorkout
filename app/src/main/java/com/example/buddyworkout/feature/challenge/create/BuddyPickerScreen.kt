package com.example.buddyworkout.feature.challenge.create

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.buddyworkout.core.ui.component.Avatar
import com.example.buddyworkout.core.ui.component.BwButton
import com.example.buddyworkout.core.ui.component.BwTextField
import com.example.buddyworkout.core.ui.component.BwTopBar
import com.example.buddyworkout.core.ui.component.Pill
import com.example.buddyworkout.core.ui.component.SectionTitle
import com.example.buddyworkout.core.ui.icon.BwIcons
import com.example.buddyworkout.core.ui.model.BuddyUi
import com.example.buddyworkout.core.ui.preview.PreviewData
import com.example.buddyworkout.core.ui.theme.BuddyWorkoutTheme
import com.example.buddyworkout.core.ui.theme.BwColors
import com.example.buddyworkout.core.ui.theme.BwSize
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
        BwTopBar(
            title = "Add buddies",
            onBack = onBack,
            actions = { Pill(text = state.countLabel) },
        )

        BwTextField(
            value = state.query,
            onValueChange = onQueryChange,
            placeholder = state.searchHint,
            trailingIcon = BwIcons.Search,
            modifier = Modifier.padding(horizontal = BwSpace.Gutter, vertical = BwSpace.Xs),
        )

        if (state.selected.isNotEmpty()) {
            SelectedChips(
                selected = state.selected,
                onRemove = onToggleBuddy,
                modifier = Modifier.padding(horizontal = BwSpace.Gutter, vertical = BwSpace.Md),
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(horizontal = BwSpace.Gutter),
        ) {
            item { SectionTitle(text = "All buddies · ${state.buddies.size}") }
            items(state.buddies.size) { index ->
                val buddy = state.buddies[index]
                BuddyRow(
                    buddy = buddy,
                    // A full roster still lets you deselect, just not add more.
                    enabled = buddy.selected || !state.atLimit,
                    onClick = { onToggleBuddy(buddy.uid) },
                    divider = index < state.buddies.lastIndex,
                )
            }
        }

        BwButton(
            text = state.doneLabel,
            onClick = onDone,
            enabled = state.selectedCount > 0,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = BwSpace.Gutter)
                .padding(bottom = BwSpace.Gutter, top = BwSpace.Sm),
        )
    }
}

@Composable
private fun SelectedChips(
    selected: List<BuddyUi>,
    onRemove: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(BwSpace.Sm)) {
        selected.forEach { buddy ->
            Row(
                modifier = Modifier
                    .background(BwColors.PrimaryTint, RoundedCornerShape(30.dp))
                    .clickable { onRemove(buddy.uid) }
                    .padding(start = 5.dp, end = 8.dp, top = 5.dp, bottom = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Avatar(buddy.avatar, size = 26.dp)
                Text(
                    text = buddy.name.substringBefore(' '),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.5.sp),
                    color = BwColors.PrimaryDark,
                )
                Icon(
                    imageVector = BwIcons.Close,
                    contentDescription = "Remove ${buddy.name}",
                    tint = BwColors.PrimaryDark,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
    }
}

@Composable
private fun BuddyRow(
    buddy: BuddyUi,
    enabled: Boolean,
    onClick: () -> Unit,
    divider: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled, onClick = onClick)
                .padding(vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(BwSpace.Md),
        ) {
            Avatar(buddy.avatar, size = BwSize.IconButton)
            Text(
                text = buddy.name,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    fontWeight = if (buddy.selected) FontWeight.Bold else FontWeight.SemiBold,
                ),
                color = if (enabled) BwColors.Ink else BwColors.Placeholder,
                modifier = Modifier.weight(1f),
            )
            SelectionDot(selected = buddy.selected)
        }
        if (divider) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .size(1.dp)
                    .background(BwColors.Line),
            )
        }
    }
}

@Composable
private fun SelectionDot(selected: Boolean) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .then(
                if (selected) {
                    Modifier.background(BwColors.Primary, CircleShape)
                } else {
                    Modifier.border(2.dp, BwColors.Line, CircleShape)
                }
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Icon(
                imageVector = BwIcons.Check,
                contentDescription = "Selected",
                tint = Color.White,
                modifier = Modifier.size(13.dp),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 380, heightDp = 800)
@Composable
private fun BuddyPickerPreview() = BuddyWorkoutTheme {
    BuddyPickerScreen(PreviewData.buddyPicker, {}, {}, {}, {})
}
