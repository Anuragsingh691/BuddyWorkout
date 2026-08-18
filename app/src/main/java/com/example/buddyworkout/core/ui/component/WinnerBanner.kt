package com.example.buddyworkout.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.buddyworkout.core.ui.theme.BuddyWorkoutTheme
import com.example.buddyworkout.core.ui.theme.BwColors
import com.example.buddyworkout.core.ui.theme.BwRadius

/** Green "challenge ended, X wins" banner at the top of the result screen. */
@Composable
fun WinnerBanner(
    winner: String,
    detail: String,
    modifier: Modifier = Modifier,
    overline: String = "🏆 Challenge ended",
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(BwColors.Primary, RoundedCornerShape(BwRadius.Card))
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = overline,
            color = BwColors.Surface.copy(alpha = 0.9f),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
        )
        Text(
            text = winner,
            color = BwColors.Surface,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
            modifier = Modifier.padding(top = 4.dp),
        )
        Text(
            text = detail,
            color = BwColors.Surface.copy(alpha = 0.9f),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
        )
    }
}

@Preview(showBackground = true, widthDp = 340)
@Composable
private fun WinnerBannerPreview() = BuddyWorkoutTheme {
    PreviewColumn {
        WinnerBanner(winner = "Anurag wins!", detail = "203 pushups · most reps")
    }
}
