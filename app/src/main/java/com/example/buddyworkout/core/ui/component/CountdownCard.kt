package com.example.buddyworkout.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.unit.sp
import com.example.buddyworkout.core.ui.icon.BwIcons
import com.example.buddyworkout.core.ui.theme.BuddyWorkoutTheme
import com.example.buddyworkout.core.ui.theme.BwColors

/**
 * The "ENDS IN hh:mm:ss" header on the live challenge detail, with the member
 * stack on the right and the auto-close note underneath.
 */
@Composable
fun CountdownCard(
    remaining: String,
    members: List<AvatarUi>,
    modifier: Modifier = Modifier,
    note: String? = "Auto-declares the winner at the deadline. No early finish.",
) {
    BwCard(modifier = modifier, containerColor = BwColors.PrimaryTint) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column {
                Text(
                    text = "ENDS IN",
                    color = BwColors.PrimaryDark,
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.5.sp),
                )
                Text(
                    text = remaining,
                    color = BwColors.PrimaryDark,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 30.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp,
                    ),
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                AvatarStack(members)
                Text(
                    text = "${members.size} members",
                    color = BwColors.PrimaryDark,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }
        if (note != null) {
            NoticeCard(
                text = note,
                icon = BwIcons.Info,
                containerColor = BwColors.Surface,
                contentColor = BwColors.PrimaryDark,
                cornerRadius = 10.dp,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 340)
@Composable
private fun CountdownCardPreview() = BuddyWorkoutTheme {
    PreviewColumn {
        CountdownCard(
            remaining = "02:14:53",
            members = listOf(AvatarUi("RK"), AvatarUi("PM"), AvatarUi("SV"), AvatarUi("AS")),
        )
    }
}
