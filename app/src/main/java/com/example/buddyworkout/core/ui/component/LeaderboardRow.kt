package com.example.buddyworkout.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.buddyworkout.core.ui.theme.BuddyWorkoutTheme
import com.example.buddyworkout.core.ui.theme.BwColors
import com.example.buddyworkout.core.ui.theme.BwRadius

/**
 * Row treatments for `.lb`: [Plain] is a divider row, [Highlight] marks "you" on
 * the live board, [Gold] marks the winner on the final board.
 */
enum class RowTone { Plain, Highlight, Gold }

/**
 * One person in a list — used by the live leaderboard, the final leaderboard and
 * the buddy lists. [rank] and [value] are omitted for buddy rows; [trailing]
 * carries the pill or "Challenge" button those rows show instead.
 */
@Composable
fun LeaderboardRow(
    avatar: AvatarUi,
    name: String,
    modifier: Modifier = Modifier,
    rank: Int? = null,
    subtitle: String? = null,
    value: String? = null,
    tone: RowTone = RowTone.Plain,
    showDivider: Boolean = true,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val container = when (tone) {
        RowTone.Plain -> Color.Transparent
        RowTone.Highlight -> BwColors.PrimaryTint
        RowTone.Gold -> BwColors.AmberTint
    }
    val rankColor = when (tone) {
        RowTone.Plain -> BwColors.Muted
        RowTone.Highlight -> BwColors.PrimaryDark
        RowTone.Gold -> BwColors.Amber
    }
    val valueColor = when (tone) {
        RowTone.Plain -> BwColors.Ink
        RowTone.Highlight -> BwColors.PrimaryDark
        RowTone.Gold -> BwColors.AmberInk
    }
    val subtitleColor = if (tone == RowTone.Highlight) BwColors.PrimaryDark else BwColors.Muted

    Column(modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (tone == RowTone.Plain) {
                        Modifier
                    } else {
                        Modifier.background(container, RoundedCornerShape(BwRadius.Control))
                    }
                )
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
                .padding(
                    horizontal = if (tone == RowTone.Plain) 4.dp else 12.dp,
                    vertical = 12.dp,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (rank != null) {
                Text(
                    text = rank.toString(),
                    color = rankColor,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                    modifier = Modifier.width(26.dp),
                )
            }
            Avatar(avatar)
            Column(Modifier.weight(1f)) {
                Text(
                    text = name,
                    color = BwColors.Ink,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontSize = 14.5.sp,
                        fontWeight = if (tone == RowTone.Plain) FontWeight.Bold else FontWeight.ExtraBold,
                    ),
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        color = subtitleColor,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                    )
                }
            }
            if (value != null) {
                Text(
                    text = value,
                    color = valueColor,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                )
            }
            trailing?.let {
                Box(contentAlignment = Alignment.Center) { it() }
            }
        }
        if (showDivider && tone == RowTone.Plain) {
            HorizontalDivider(thickness = 1.dp, color = BwColors.Line)
        }
    }
}

@Preview(showBackground = true, widthDp = 340)
@Composable
private fun LeaderboardRowPreview() = BuddyWorkoutTheme {
    PreviewColumn {
        LeaderboardRow(AvatarUi("AS"), "You", rank = 1, subtitle = "Active now", value = "128", tone = RowTone.Highlight)
        LeaderboardRow(AvatarUi("RK"), "Rohit Kumar", rank = 2, subtitle = "2 min ago", value = "119")
        LeaderboardRow(AvatarUi("PM"), "Priya Mehta", rank = 3, subtitle = "18 min ago", value = "64", showDivider = false)
        LeaderboardRow(AvatarUi("AS"), "Anurag (you)", rank = 1, subtitle = "Finished 17 Jun · 8:40pm", value = "203", tone = RowTone.Gold)
        LeaderboardRow(
            avatar = AvatarUi("SV"),
            name = "Sahil Verma",
            subtitle = "Not in a challenge",
            trailing = { BwButton("Challenge", {}, variant = BwButtonVariant.Tint, height = 34.dp) },
            showDivider = false,
        )
    }
}
