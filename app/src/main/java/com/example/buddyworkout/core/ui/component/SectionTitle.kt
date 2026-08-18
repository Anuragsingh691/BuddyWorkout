package com.example.buddyworkout.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.buddyworkout.core.ui.theme.BuddyWorkoutTheme
import com.example.buddyworkout.core.ui.theme.BwColors

/**
 * Section heading (`.sec-title`), optionally with the muted trailing hint the
 * mockups use for things like "· tap to open →".
 */
@Composable
fun SectionTitle(
    text: String,
    modifier: Modifier = Modifier,
    hint: String? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text, color = BwColors.Ink, style = MaterialTheme.typography.titleSmall)
            if (hint != null) {
                Text(
                    text = " $hint",
                    color = BwColors.Muted,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal),
                )
            }
        }
        trailing?.invoke()
    }
}

@Preview(showBackground = true, widthDp = 340)
@Composable
private fun SectionTitlePreview() = BuddyWorkoutTheme {
    PreviewColumn {
        SectionTitle("Active challenges", hint = "· tap to open →")
        SectionTitle("Your buddies · 4")
        SectionTitle("Live leaderboard", trailing = { Pill("Live", tone = PillTone.Live) })
    }
}
