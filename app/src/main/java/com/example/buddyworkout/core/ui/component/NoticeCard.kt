package com.example.buddyworkout.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.buddyworkout.core.ui.icon.BwIcons
import com.example.buddyworkout.core.ui.theme.BuddyWorkoutTheme
import com.example.buddyworkout.core.ui.theme.BwColors
import com.example.buddyworkout.core.ui.theme.BwRadius

/**
 * Inline icon + message strip. Used for the amber "you're at the challenge
 * limit" warning and the white "auto-declares the winner" note inside the
 * countdown card.
 */
@Composable
fun NoticeCard(
    text: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    containerColor: Color = BwColors.AmberTint,
    contentColor: Color = BwColors.AmberInk,
    cornerRadius: androidx.compose.ui.unit.Dp = BwRadius.Card,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(containerColor, RoundedCornerShape(cornerRadius))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(icon, null, tint = contentColor, modifier = Modifier.size(16.dp))
        Text(
            text = text,
            color = contentColor,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 12.5.sp,
                lineHeight = 17.5.sp,
                fontWeight = FontWeight.SemiBold,
            ),
        )
    }
}

@Preview(showBackground = true, widthDp = 340)
@Composable
private fun NoticeCardPreview() = BuddyWorkoutTheme {
    PreviewColumn {
        NoticeCard(
            text = "You're in 2 of 2 active challenges. Finish one to create a new one.",
            icon = BwIcons.AlertTriangle,
        )
        NoticeCard(
            text = "Auto-declares the winner at the deadline. No early finish.",
            icon = BwIcons.Info,
            containerColor = BwColors.Surface,
            contentColor = BwColors.PrimaryDark,
            cornerRadius = 10.dp,
        )
    }
}
