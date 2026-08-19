package com.example.buddyworkout.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.buddyworkout.core.ui.theme.BuddyWorkoutTheme
import com.example.buddyworkout.core.ui.theme.BwColors
import com.example.buddyworkout.core.ui.theme.BwRadius

/** Same scrim value the date sheet uses, so overlays read as one family. */
private val Scrim = Color(0xFF11161C).copy(alpha = 0.35f)

/**
 * App-wide blocking loader, shown while any command is in flight.
 *
 * Nothing in the Figma export specifies a loader, so this is invented: the
 * sheet's scrim, a card at the standard radius, and the primary green.
 *
 * Swallowing taps is half the point — it stops a second submit landing while
 * the first is still running. The other half is the re-entry guard each
 * ViewModel keeps in its own `isLoading`.
 */
@Composable
fun BwLoadingOverlay(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Scrim)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {},
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .background(BwColors.Surface, RoundedCornerShape(BwRadius.Card))
                .padding(28.dp),
        ) {
            CircularProgressIndicator(
                color = BwColors.Primary,
                trackColor = BwColors.Line,
                strokeWidth = 3.dp,
                modifier = Modifier.size(34.dp),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 340, heightDp = 400)
@Composable
private fun BwLoadingOverlayPreview() = BuddyWorkoutTheme {
    Box(Modifier.fillMaxSize().background(BwColors.Bg)) {
        BwLoadingOverlay()
    }
}
