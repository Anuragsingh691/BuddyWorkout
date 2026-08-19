package com.example.buddyworkout.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.buddyworkout.core.ui.icon.BwIcons
import com.example.buddyworkout.core.ui.theme.BuddyWorkoutTheme
import com.example.buddyworkout.core.ui.theme.BwColors
import com.example.buddyworkout.core.ui.theme.BwRadius
import com.example.buddyworkout.core.ui.theme.BwSize

/** The four button treatments in the mockups (`.btn.primary` … `.btn.danger-ghost`). */
enum class BwButtonVariant { Primary, Outline, Tint, DangerGhost }

/**
 * Primary action control. Pass `Modifier.fillMaxWidth()` for the full-width
 * `.btn.block` treatment.
 */
@Composable
fun BwButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: BwButtonVariant = BwButtonVariant.Primary,
    icon: ImageVector? = null,
    /** Required when [text] is blank, since the icon is then the only label. */
    contentDescription: String? = null,
    enabled: Boolean = true,
    height: Dp = if (variant == BwButtonVariant.DangerGhost) BwSize.ButtonSmall else BwSize.Button,
    /** The 34dp inline chip the mockups use for "Challenge" and the like. */
    compact: Boolean = false,
) {
    val background = when {
        !enabled -> if (variant == BwButtonVariant.Primary) BwColors.Line else Color.Transparent
        variant == BwButtonVariant.Primary -> BwColors.Primary
        variant == BwButtonVariant.Outline -> BwColors.Surface
        variant == BwButtonVariant.Tint -> BwColors.PrimaryTint
        else -> Color.Transparent
    }
    val content = when {
        !enabled -> BwColors.Placeholder
        variant == BwButtonVariant.Primary -> BwColors.Surface
        variant == BwButtonVariant.Outline -> BwColors.Ink
        variant == BwButtonVariant.Tint -> BwColors.PrimaryDark
        else -> BwColors.Danger
    }
    val weight = when (variant) {
        BwButtonVariant.Primary, BwButtonVariant.Tint -> FontWeight.Bold
        BwButtonVariant.Outline, BwButtonVariant.DangerGhost -> FontWeight.SemiBold
    }
    val shape = RoundedCornerShape(BwRadius.Control)

    Row(
        modifier = modifier
            .height(height)
            .then(
                if (variant == BwButtonVariant.Outline) {
                    Modifier.border(BwSize.Border, BwColors.Line, shape)
                } else {
                    Modifier
                }
            )
            .background(background, shape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = if (compact) 14.dp else 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = if (icon == BwIcons.Google) Color.Unspecified else content,
                modifier = Modifier.size(20.dp),
            )
        }
        // Blank text means an icon-only control, such as the camera flip on the
        // record screen; drawing the empty label would push the icon off centre.
        if (text.isNotEmpty()) {
            Text(
                text = text,
                color = content,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = weight,
                    fontSize = if (compact) 13.sp else 16.sp,
                ),
                modifier = if (icon != null) Modifier.padding(start = 10.dp) else Modifier,
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 340)
@Composable
private fun BwButtonPreview() = BuddyWorkoutTheme {
    PreviewColumn {
        BwButton("Log in", {}, Modifier.fillMaxWidth())
        BwButton("Continue with Google", {}, Modifier.fillMaxWidth(), BwButtonVariant.Outline, BwIcons.Google)
        BwButton("Challenge", {}, variant = BwButtonVariant.Tint, height = BwSize.Chip)
        BwButton("Decline", {}, Modifier.fillMaxWidth(), BwButtonVariant.DangerGhost)
        BwButton("Create challenge", {}, Modifier.fillMaxWidth(), enabled = false)
        BwButton("Record workout", {}, Modifier.fillMaxWidth(), icon = BwIcons.Video)
    }
}
