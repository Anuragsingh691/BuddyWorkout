package com.example.buddyworkout.feature.challenge.create

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.buddyworkout.core.ui.component.BwButton
import com.example.buddyworkout.core.ui.component.BwCard
import com.example.buddyworkout.core.ui.component.BwTopBar
import com.example.buddyworkout.core.ui.component.DetailRow
import com.example.buddyworkout.core.ui.component.SectionTitle
import com.example.buddyworkout.core.ui.icon.BwIcons
import com.example.buddyworkout.core.ui.preview.PreviewData
import com.example.buddyworkout.core.ui.theme.BuddyWorkoutTheme
import com.example.buddyworkout.core.ui.theme.BwColors
import com.example.buddyworkout.core.ui.theme.BwSpace

@Composable
fun DateTimePickerScreen(
    state: DateTimePickerUiState,
    onSelectPreset: (Int) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BwColors.Bg),
    ) {
        BwTopBar(title = "Start & end", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(BwSpace.Gutter),
            verticalArrangement = Arrangement.spacedBy(BwSpace.Md),
        ) {
            Text(
                text = "The challenge starts as soon as you create it and closes automatically at the deadline.",
                style = MaterialTheme.typography.bodyLarge,
                color = BwColors.Muted,
            )

            SectionTitle(text = "How long should it run?")

            BwCard(modifier = Modifier.fillMaxWidth()) {
                state.presets.forEachIndexed { index, preset ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectPreset(index) }
                            .padding(vertical = BwSpace.Sm),
                    ) {
                        Text(
                            text = preset,
                            style = MaterialTheme.typography.titleSmall,
                            color = if (index == state.selectedPresetIndex) BwColors.Primary else BwColors.Ink,
                            modifier = Modifier.weight(1f),
                        )
                        if (index == state.selectedPresetIndex) {
                            Icon(
                                imageVector = BwIcons.Check,
                                contentDescription = "Selected",
                                tint = BwColors.Primary,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
            }

            BwCard(modifier = Modifier.fillMaxWidth()) {
                DetailRow(label = "Starts", value = state.startLabel)
                DetailRow(
                    label = "Ends",
                    value = state.endLabel.ifBlank { "Pick a duration" },
                    valueColor = if (state.endLabel.isBlank()) BwColors.Placeholder else BwColors.Ink,
                )
            }

            BwButton(
                text = "Save",
                onClick = onSave,
                enabled = state.canSave,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 380, heightDp = 800)
@Composable
private fun DateTimePickerPreview() = BuddyWorkoutTheme {
    DateTimePickerScreen(PreviewData.dateTimePicker, {}, {}, {})
}
