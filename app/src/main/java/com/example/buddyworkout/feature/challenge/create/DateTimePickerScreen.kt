package com.example.buddyworkout.feature.challenge.create

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.buddyworkout.core.ui.component.BwButton
import com.example.buddyworkout.core.ui.component.BwButtonVariant
import com.example.buddyworkout.core.ui.icon.BwIcons
import com.example.buddyworkout.core.ui.preview.PreviewData
import com.example.buddyworkout.core.ui.theme.BuddyWorkoutTheme
import com.example.buddyworkout.core.ui.theme.BwColors
import com.example.buddyworkout.core.ui.theme.BwSize
import com.example.buddyworkout.core.ui.theme.BwSpace

/** Neutral fill behind the sheet's inactive segments (`#F2F3F5`). */
private val Track = Color(0xFFF2F3F5)

/** Scrim over the screen the sheet covers. */
private val Scrim = Color(0xFF11161C).copy(alpha = 0.35f)

private val WEEKDAYS = listOf("S", "M", "T", "W", "T", "F", "S")

@Composable
fun DateTimePickerScreen(
    state: DateTimePickerUiState,
    onSelectMode: (DateTimeMode) -> Unit,
    onSelectDay: (Int) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectHour: (Int) -> Unit,
    onSelectMinute: (Int) -> Unit,
    onToggleMeridiem: () -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxSize()) {
        // Tapping the scrim dismisses, as a sheet does. No ripple: the scrim is
        // a dismissal target, not a control.
        Box(
            Modifier
                .fillMaxSize()
                .background(Scrim)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onBack,
                ),
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(BwColors.Surface, RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp))
                .verticalScroll(rememberScrollState())
                .padding(start = 18.dp, end = 18.dp, top = 18.dp, bottom = 22.dp),
        ) {
            Box(
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = BwSpace.Lg)
                    .width(38.dp)
                    .height(4.dp)
                    .background(BwColors.Line, RoundedCornerShape(4.dp)),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = state.title,
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp),
                    color = BwColors.Ink,
                )
                ModeToggle(selected = state.mode, onSelect = onSelectMode)
            }

            when (state.mode) {
                DateTimeMode.Date -> MonthGrid(
                    state = state,
                    onSelectDay = onSelectDay,
                    onPreviousMonth = onPreviousMonth,
                    onNextMonth = onNextMonth,
                )
                DateTimeMode.Time -> TimeGrid(
                    state = state,
                    onSelectHour = onSelectHour,
                    onSelectMinute = onSelectMinute,
                )
            }

            Box(
                Modifier
                    .padding(top = BwSpace.Md)
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(BwColors.Line),
            )

            TimeRow(
                state = state,
                onToggleMeridiem = onToggleMeridiem,
                onEditTime = { onSelectMode(DateTimeMode.Time) },
                modifier = Modifier.padding(top = 14.dp),
            )

            Row(
                modifier = Modifier
                    .padding(top = 18.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(BwSpace.Md),
            ) {
                BwButton(
                    text = "Cancel",
                    onClick = onBack,
                    variant = BwButtonVariant.Outline,
                    height = BwSize.ButtonCompact,
                    modifier = Modifier.weight(1f),
                )
                BwButton(
                    text = state.confirmLabel,
                    onClick = onSave,
                    enabled = state.canSave,
                    height = BwSize.ButtonCompact,
                    // The export gives the confirm button the wider share.
                    modifier = Modifier.weight(1.4f),
                )
            }
        }
    }
}

/** The small Date / Time switch in the sheet's header. */
@Composable
private fun ModeToggle(
    selected: DateTimeMode,
    onSelect: (DateTimeMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .background(Track, RoundedCornerShape(10.dp))
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        DateTimeMode.entries.forEach { mode ->
            val active = mode == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (active) BwColors.Surface else Color.Transparent)
                    .clickable { onSelect(mode) }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Text(
                    text = mode.name,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 12.5.sp,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.SemiBold,
                    ),
                    color = if (active) BwColors.Ink else BwColors.Muted,
                )
            }
        }
    }
}

@Composable
private fun MonthGrid(
    state: DateTimePickerUiState,
    onSelectDay: (Int) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Row(
            modifier = Modifier
                .padding(top = BwSpace.Lg, bottom = 10.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = state.month.label,
                style = MaterialTheme.typography.titleSmall,
                color = BwColors.Ink,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Icon(
                    imageVector = BwIcons.ChevronLeft,
                    contentDescription = "Previous month",
                    tint = BwColors.Muted,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable(onClick = onPreviousMonth),
                )
                Icon(
                    imageVector = BwIcons.ChevronRight,
                    contentDescription = "Next month",
                    tint = BwColors.Ink,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable(onClick = onNextMonth),
                )
            }
        }

        Row(Modifier.fillMaxWidth()) {
            WEEKDAYS.forEach { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = BwColors.Muted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        state.month.cells.chunked(7).forEach { week ->
            Row(Modifier.fillMaxWidth()) {
                week.forEach { day ->
                    DayCell(
                        day = day,
                        selected = day != null && day == state.selectedDay,
                        onClick = { day?.let(onSelectDay) },
                        modifier = Modifier.weight(1f),
                    )
                }
                // A short final week still has to leave its columns standing.
                repeat(7 - week.size) { Box(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun DayCell(
    day: Int?,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.padding(vertical = 2.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (day == null) {
            Box(Modifier.size(34.dp))
        } else {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(if (selected) BwColors.Primary else Color.Transparent)
                    .clickable(onClick = onClick),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = day.toString(),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 13.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    ),
                    color = if (selected) BwColors.Surface else BwColors.Ink,
                )
            }
        }
    }
}

/**
 * The Time half of the sheet.
 *
 * The export never draws this tab — it only ever shows Date — so the hour and
 * minute grids are the one invented piece on this screen. They reuse the day
 * cell's shape so the two halves read as the same control.
 */
@Composable
private fun TimeGrid(
    state: DateTimePickerUiState,
    onSelectHour: (Int) -> Unit,
    onSelectMinute: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.padding(top = BwSpace.Lg)) {
        Text(
            text = "Hour",
            style = MaterialTheme.typography.titleSmall,
            color = BwColors.Ink,
        )
        NumberGrid(
            values = (1..12).toList(),
            selected = state.hour,
            label = { it.toString() },
            onSelect = onSelectHour,
        )
        Text(
            text = "Minute",
            style = MaterialTheme.typography.titleSmall,
            color = BwColors.Ink,
            modifier = Modifier.padding(top = BwSpace.Md),
        )
        NumberGrid(
            values = (0..55 step 5).toList(),
            selected = state.minute,
            label = { it.toString().padStart(2, '0') },
            onSelect = onSelectMinute,
        )
    }
}

@Composable
private fun NumberGrid(
    values: List<Int>,
    selected: Int,
    label: (Int) -> String,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.padding(top = BwSpace.Sm)) {
        values.chunked(6).forEach { row ->
            Row(Modifier.fillMaxWidth()) {
                row.forEach { value ->
                    val active = value == selected
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(2.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (active) BwColors.PrimaryTint else Track)
                            .clickable { onSelect(value) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = label(value),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = if (active) BwColors.PrimaryDark else BwColors.Ink,
                        )
                    }
                }
                repeat(6 - row.size) { Box(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun TimeRow(
    state: DateTimePickerUiState,
    onToggleMeridiem: () -> Unit,
    onEditTime: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = BwIcons.Clock,
                contentDescription = null,
                tint = BwColors.Primary,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = "Time",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = BwColors.Ink,
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TimeBox(state.hourLabel, highlighted = true, onClick = onEditTime)
            Text(
                text = ":",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = BwColors.Ink,
            )
            TimeBox(state.minuteLabel, highlighted = false, onClick = onEditTime)
            Row(
                modifier = Modifier
                    .padding(start = BwSpace.Xs)
                    .background(Track, RoundedCornerShape(10.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                listOf("AM", "PM").forEach { period ->
                    val active = period == state.meridiem
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (active) BwColors.Surface else Color.Transparent)
                            .clickable(onClick = onToggleMeridiem)
                            .padding(horizontal = 9.dp, vertical = 5.dp),
                    ) {
                        Text(
                            text = period,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                            color = if (active) BwColors.Ink else BwColors.Muted,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeBox(text: String, highlighted: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (highlighted) BwColors.PrimaryTint else Track)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
            color = if (highlighted) BwColors.PrimaryDark else BwColors.Ink,
        )
    }
}

@Preview(showBackground = true, widthDp = 380, heightDp = 800)
@Composable
private fun DateTimePickerPreview() = BuddyWorkoutTheme {
    DateTimePickerScreen(PreviewData.dateTimePicker, {}, {}, {}, {}, {}, {}, {}, {}, {})
}

@Preview(showBackground = true, widthDp = 380, heightDp = 800)
@Composable
private fun DateTimePickerTimePreview() = BuddyWorkoutTheme {
    DateTimePickerScreen(
        PreviewData.dateTimePicker.copy(mode = DateTimeMode.Time),
        {}, {}, {}, {}, {}, {}, {}, {}, {},
    )
}
