package com.example.buddyworkout.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.example.buddyworkout.core.ui.icon.BwIcons
import com.example.buddyworkout.core.ui.theme.BuddyWorkoutTheme
import com.example.buddyworkout.core.ui.theme.BwColors
import com.example.buddyworkout.core.ui.theme.BwRadius
import com.example.buddyworkout.core.ui.theme.BwSize

/**
 * An active challenge as it appears on Home and the Challenges tab: dumbbell
 * thumb, generic title, member/deadline summary, and a progress bar tinted by
 * the user's standing.
 */
@Composable
fun ChallengeCard(
    title: String,
    summary: String,
    progress: Float,
    stat: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accent: Color = BwColors.Primary,
    trailing: (@Composable () -> Unit)? = null,
) {
    BwCard(modifier = modifier, onClick = onClick) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(BwSize.Thumb)
                    .background(
                        color = if (accent == BwColors.Primary) BwColors.PrimaryTint else BwColors.AmberTint,
                        shape = RoundedCornerShape(BwRadius.Thumb),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(BwIcons.Dumbbell, null, tint = accent, modifier = Modifier.size(22.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = BwColors.Ink,
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = summary,
                    color = BwColors.Muted,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
                )
            }
            if (trailing != null) {
                trailing()
            } else {
                Icon(BwIcons.ChevronRight, null, tint = BwColors.Placeholder, modifier = Modifier.size(20.dp))
            }
        }
        Row(
            modifier = Modifier.padding(top = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ProgressBar(progress = progress, accent = accent, modifier = Modifier.weight(1f))
            Text(
                text = stat,
                color = BwColors.Muted,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            )
        }
    }
}

/** 8dp fully-rounded progress track (`.row > div[height:8px]`). */
@Composable
fun ProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    accent: Color = BwColors.Primary,
) {
    Box(
        modifier = modifier
            .height(8.dp)
            .background(BwColors.Line, RoundedCornerShape(8.dp)),
    ) {
        Box(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .background(accent, RoundedCornerShape(8.dp)),
        )
    }
}

@Preview(showBackground = true, widthDp = 340)
@Composable
private fun ChallengeCardPreview() = BuddyWorkoutTheme {
    PreviewColumn {
        ChallengeCard(
            title = "Pushup challenge",
            summary = "with Rohit, Priya +2 · ends in 3 days",
            progress = 0.64f,
            stat = "128 reps · 1st",
            onClick = {},
        )
        ChallengeCard(
            title = "Pushup challenge",
            summary = "with Sahil, Neha +1 · ends in 6 days",
            progress = 0.38f,
            stat = "42 reps",
            onClick = {},
            accent = BwColors.Amber,
            trailing = { Pill("2nd", tone = PillTone.Amber) },
        )
    }
}
