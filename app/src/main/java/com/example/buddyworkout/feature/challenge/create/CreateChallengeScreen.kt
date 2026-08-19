package com.example.buddyworkout.feature.challenge.create

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.buddyworkout.core.ui.component.AvatarStack
import com.example.buddyworkout.core.ui.component.BwButton
import com.example.buddyworkout.core.ui.component.BwButtonVariant
import com.example.buddyworkout.core.ui.component.BwCard
import com.example.buddyworkout.core.ui.component.BwTopBar
import com.example.buddyworkout.core.ui.component.NoticeCard
import com.example.buddyworkout.core.ui.icon.BwIcons
import com.example.buddyworkout.core.ui.preview.PreviewData
import com.example.buddyworkout.core.ui.theme.BuddyWorkoutTheme
import com.example.buddyworkout.core.ui.theme.BwColors
import com.example.buddyworkout.core.ui.theme.BwRadius
import com.example.buddyworkout.core.ui.theme.BwSize
import com.example.buddyworkout.core.ui.theme.BwSpace

/** Gap between stacked `.field` blocks in the export. */
private val FieldGap = 14.dp

@Composable
fun CreateChallengeScreen(
    state: CreateChallengeUiState,
    onBack: () -> Unit,
    onPickBuddies: () -> Unit,
    onSelectDuration: (Int) -> Unit,
    onPickStart: () -> Unit,
    onPickEnd: () -> Unit,
    onCreate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BwColors.Bg),
    ) {
        BwTopBar(title = "Create challenge", onBack = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = BwSpace.Gutter),
        ) {
            state.limitNotice?.let { notice ->
                BwCard(containerColor = BwColors.PrimaryTint) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(
                            imageVector = BwIcons.Users,
                            contentDescription = null,
                            tint = BwColors.PrimaryDark,
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            text = notice,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                            ),
                            color = BwColors.PrimaryDark,
                        )
                    }
                }
            }

            state.error?.let { message ->
                Box(Modifier.padding(top = FieldGap)) {
                    NoticeCard(
                        text = message,
                        icon = BwIcons.AlertTriangle,
                        containerColor = BwColors.DangerTint,
                        contentColor = BwColors.DangerInk,
                    )
                }
            }

            FieldLabel("Buddies", hint = state.buddiesHint, modifier = Modifier.padding(top = FieldGap))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 56.dp)
                    .border(BwSize.Border, BwColors.Line, RoundedCornerShape(BwRadius.Control))
                    .background(BwColors.Surface, RoundedCornerShape(BwRadius.Control))
                    .clickable(onClick = onPickBuddies)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (state.selectedBuddies.isNotEmpty()) {
                    AvatarStack(
                        avatars = state.selectedBuddies.map { it.avatar },
                        size = 34.dp,
                    )
                }
                Text(
                    text = state.buddiesLabel,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = if (state.selectedBuddies.isEmpty()) BwColors.Placeholder else BwColors.Ink,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = if (state.selectedBuddies.isEmpty()) 0.dp else 12.dp),
                )
                Text(
                    text = "Add",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = BwColors.PrimaryDark,
                )
                Icon(
                    imageVector = BwIcons.ChevronRight,
                    contentDescription = null,
                    tint = BwColors.PrimaryDark,
                    modifier = Modifier
                        .padding(start = BwSpace.Xs)
                        .size(18.dp),
                )
            }

            FieldLabel("Duration", modifier = Modifier.padding(top = FieldGap))
            DurationChips(
                durations = state.durations,
                selectedIndex = state.selectedDurationIndex,
                onSelect = onSelectDuration,
                onCustom = onPickStart,
            )

            Row(
                modifier = Modifier
                    .padding(top = FieldGap)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(BwSpace.Md),
            ) {
                WindowField(
                    label = "Starts",
                    value = state.startLabel,
                    icon = BwIcons.Calendar,
                    onClick = onPickStart,
                    modifier = Modifier.weight(1f),
                )
                WindowField(
                    label = "Ends",
                    value = state.endLabel.ifBlank { "Not set" },
                    icon = BwIcons.Clock,
                    onClick = onPickEnd,
                    placeholder = state.endLabel.isBlank(),
                    modifier = Modifier.weight(1f),
                )
            }
            if (state.durationLabel.isNotBlank()) {
                Text(
                    text = state.durationLabel,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                    color = BwColors.Muted,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }

            FieldLabel("Exercise", modifier = Modifier.padding(top = FieldGap))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                BwButton(
                    text = "Pushups",
                    onClick = {},
                    height = BwSize.ButtonSmall,
                    modifier = Modifier.weight(1f),
                )
                // Disabled rather than absent: the export shows the roadmap.
                BwButton(
                    text = "Squats · soon",
                    onClick = {},
                    variant = BwButtonVariant.Outline,
                    enabled = false,
                    height = BwSize.ButtonSmall,
                    modifier = Modifier.weight(1f),
                )
            }

            Box(Modifier.height(BwSpace.Gutter))
        }

        BwButton(
            text = if (state.isLoading) "Creating…" else "Create challenge",
            onClick = onCreate,
            enabled = state.canCreate,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = BwSpace.Gutter)
                .padding(bottom = BwSpace.Gutter),
        )
    }
}

@Composable
private fun FieldLabel(text: String, modifier: Modifier = Modifier, hint: String? = null) {
    Row(modifier.padding(bottom = 6.dp)) {
        Text(text, color = BwColors.LabelInk, style = MaterialTheme.typography.labelLarge)
        if (hint != null) {
            Text(
                text = " $hint",
                color = BwColors.Placeholder,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Normal),
            )
        }
    }
}

@Composable
private fun DurationChips(
    durations: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    onCustom: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Chunked rather than a flow row: the six chips wrap to a predictable 3+3
    // at every phone width, and FlowRow is still experimental.
    val rows = (durations.indices.map { it to durations[it] } + (-1 to "Custom")).chunked(3)
    Column(modifier, verticalArrangement = Arrangement.spacedBy(BwSpace.Sm)) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(BwSpace.Sm)) {
                row.forEach { (index, label) ->
                    val custom = index == -1
                    val active = index == selectedIndex
                    Box(
                        modifier = Modifier
                            .border(
                                width = BwSize.Border,
                                color = if (active) BwColors.Primary else BwColors.Line,
                                shape = RoundedCornerShape(BwRadius.Pill),
                            )
                            .background(
                                color = if (active) BwColors.PrimaryTint else Color.Transparent,
                                shape = RoundedCornerShape(BwRadius.Pill),
                            )
                            .clickable { if (custom) onCustom() else onSelect(index) }
                            .padding(horizontal = 13.dp, vertical = 7.dp),
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.5.sp,
                                fontWeight = if (active) FontWeight.Bold else FontWeight.SemiBold,
                            ),
                            color = when {
                                active -> BwColors.PrimaryDark
                                custom -> BwColors.Muted
                                else -> BwColors.Ink
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WindowField(
    label: String,
    value: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: Boolean = false,
) {
    Column(modifier) {
        FieldLabel(label)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(BwSize.TextField)
                .border(BwSize.Border, BwColors.Line, RoundedCornerShape(BwRadius.Control))
                .background(BwColors.Surface, RoundedCornerShape(BwRadius.Control))
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(BwSpace.Sm),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = BwColors.Placeholder,
                modifier = Modifier.size(17.dp),
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                color = if (placeholder) BwColors.Placeholder else BwColors.Ink,
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 380, heightDp = 800)
@Composable
private fun CreateChallengeFilledPreview() = BuddyWorkoutTheme {
    CreateChallengeScreen(PreviewData.createChallenge, {}, {}, {}, {}, {}, {})
}

@Preview(showBackground = true, widthDp = 380, heightDp = 800)
@Composable
private fun CreateChallengeEmptyPreview() = BuddyWorkoutTheme {
    CreateChallengeScreen(CreateChallengeUiState(), {}, {}, {}, {}, {}, {})
}
