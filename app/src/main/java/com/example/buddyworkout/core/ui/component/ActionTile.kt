package com.example.buddyworkout.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.buddyworkout.core.ui.icon.BwIcons
import com.example.buddyworkout.core.ui.theme.BuddyWorkoutTheme
import com.example.buddyworkout.core.ui.theme.BwColors
import com.example.buddyworkout.core.ui.theme.BwRadius

/**
 * The two large shortcuts on Home (`.action`). [filled] is the solid green tile;
 * the other is the tinted one.
 */
@Composable
fun ActionTile(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    filled: Boolean = true,
) {
    val container = if (filled) BwColors.Primary else BwColors.PrimaryTint
    val content = if (filled) BwColors.Surface else BwColors.PrimaryDark
    val iconContainer = if (filled) BwColors.Surface.copy(alpha = 0.18f) else BwColors.Surface

    Column(
        modifier = modifier
            .defaultMinSize(minHeight = 128.dp)
            .background(container, RoundedCornerShape(BwRadius.Tile))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .background(iconContainer, RoundedCornerShape(BwRadius.Control)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, tint = if (filled) BwColors.Surface else BwColors.Primary, modifier = Modifier.size(24.dp))
        }
        Column(Modifier.padding(top = 12.dp)) {
            Text(title, color = content, style = MaterialTheme.typography.titleMedium)
            Text(
                text = description,
                color = if (filled) BwColors.Surface.copy(alpha = 0.85f) else BwColors.PrimaryDark,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 340)
@Composable
private fun ActionTilePreview() = BuddyWorkoutTheme {
    PreviewColumn {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ActionTile("Invite buddies", "Share your link", BwIcons.PersonAdd, {}, Modifier.weight(1f))
            ActionTile("Create challenge", "Up to 4 buddies", BwIcons.Plus, {}, Modifier.weight(1f), filled = false)
        }
    }
}
