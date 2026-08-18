package com.example.buddyworkout.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.buddyworkout.core.ui.theme.BuddyWorkoutTheme
import com.example.buddyworkout.core.ui.theme.BwColors
import com.example.buddyworkout.core.ui.theme.BwRadius
import com.example.buddyworkout.core.ui.theme.BwSize

/**
 * Bordered white container (`.card`). Pass [containerColor] for the tinted
 * variants — those drop the border, as they do in the mockups.
 */
@Composable
fun BwCard(
    modifier: Modifier = Modifier,
    containerColor: Color = BwColors.Surface,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(BwRadius.Card)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (containerColor == BwColors.Surface) {
                    Modifier.border(BwSize.Border, BwColors.Line, shape)
                } else {
                    Modifier
                }
            )
            .background(containerColor, shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(16.dp),
        content = content,
    )
}

/** Label-on-the-left, value-on-the-right row used inside cards. */
@Composable
fun DetailRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = BwColors.Ink,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = BwColors.Muted, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = value,
            color = valueColor,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
        )
    }
}

@Preview(showBackground = true, widthDp = 340)
@Composable
private fun BwCardPreview() = BuddyWorkoutTheme {
    PreviewColumn {
        BwCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                DetailRow("Exercise", "Pushups")
                DetailRow("Members", "4 people")
                DetailRow("Window", "17 Jun 6PM → 18 Jun 6AM")
                DetailRow("Ends", "Auto at deadline")
            }
        }
        BwCard(containerColor = BwColors.PrimaryTint) {
            DetailRow("Avg form score", "92%", valueColor = BwColors.PrimaryDark)
        }
    }
}
