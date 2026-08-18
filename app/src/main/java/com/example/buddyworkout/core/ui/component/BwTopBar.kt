package com.example.buddyworkout.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
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
import com.example.buddyworkout.core.ui.icon.BwIcons
import com.example.buddyworkout.core.ui.theme.BuddyWorkoutTheme
import com.example.buddyworkout.core.ui.theme.BwColors
import com.example.buddyworkout.core.ui.theme.BwRadius
import com.example.buddyworkout.core.ui.theme.BwSize

/** 38dp square icon button (`.iconbtn`); [tinted] gives it the `.tint` background. */
@Composable
fun BwIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tinted: Boolean = false,
    tint: Color = BwColors.Ink,
) {
    Box(
        modifier = modifier
            .size(BwSize.IconButton)
            .background(
                color = if (tinted) BwColors.PrimaryTint else Color.Transparent,
                shape = RoundedCornerShape(BwRadius.IconButton),
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription, tint = tint, modifier = Modifier.size(22.dp))
    }
}

/**
 * App bar (`.appbar`). [overline] carries the small "Welcome back" line above
 * the title on Home; [onBack] renders the chevron when the screen is pushed.
 */
@Composable
fun BwTopBar(
    title: String,
    modifier: Modifier = Modifier,
    overline: String? = null,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = BwSize.TopBar)
            .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (onBack != null) {
            BwIconButton(
                icon = BwIcons.ChevronLeft,
                onClick = onBack,
                contentDescription = "Back",
                // offset, not padding: Compose rejects negative padding at
                // measure time. The shift optically aligns the chevron with
                // the 16dp gutter without changing the row's layout.
                modifier = Modifier.offset(x = (-8).dp),
            )
        }
        Column(Modifier.weight(1f)) {
            if (overline != null) {
                Text(
                    text = overline,
                    color = BwColors.Muted,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                )
            }
            Text(title, color = BwColors.Ink, style = MaterialTheme.typography.titleLarge)
        }
        actions()
    }
}

@Preview(showBackground = true, widthDp = 340)
@Composable
private fun BwTopBarPreview() = BuddyWorkoutTheme {
    Column {
        BwTopBar("Create account", onBack = {})
        BwTopBar(
            title = "Hi, Anurag 👋",
            overline = "Welcome back",
            actions = {
                BwIconButton(BwIcons.Bell, {}, "Notifications")
                Avatar(AvatarUi("AS"), size = 38.dp)
            },
        )
        BwTopBar(
            title = "Pushup challenge",
            onBack = {},
            actions = { BwIconButton(BwIcons.MoreVertical, {}, "More") },
        )
    }
}
