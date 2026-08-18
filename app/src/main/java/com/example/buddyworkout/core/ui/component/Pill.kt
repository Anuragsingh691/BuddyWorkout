package com.example.buddyworkout.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.buddyworkout.core.ui.theme.BuddyWorkoutTheme
import com.example.buddyworkout.core.ui.theme.BwColors
import com.example.buddyworkout.core.ui.theme.BwRadius

/** Status chips (`.pill`). [Live] prefixes a dot, matching the "● Live" chip. */
enum class PillTone { Green, Amber, Live, Neutral }

@Composable
fun Pill(
    text: String,
    modifier: Modifier = Modifier,
    tone: PillTone = PillTone.Green,
) {
    val background = when (tone) {
        PillTone.Green -> BwColors.PrimaryTint
        PillTone.Amber -> BwColors.AmberTint
        PillTone.Live -> BwColors.DangerTint
        PillTone.Neutral -> BwColors.Bg
    }
    val content = when (tone) {
        PillTone.Green -> BwColors.PrimaryDark
        PillTone.Amber -> BwColors.AmberInk
        PillTone.Live -> BwColors.DangerInk
        PillTone.Neutral -> BwColors.Muted
    }
    Row(
        modifier = modifier
            .background(background, RoundedCornerShape(BwRadius.Pill))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            text = if (tone == PillTone.Live) "● $text" else text,
            color = content,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@Preview(showBackground = true, widthDp = 340)
@Composable
private fun PillPreview() = BuddyWorkoutTheme {
    PreviewColumn {
        Pill("Active")
        Pill("1st", tone = PillTone.Green)
        Pill("2nd", tone = PillTone.Amber)
        Pill("Live", tone = PillTone.Live)
    }
}
