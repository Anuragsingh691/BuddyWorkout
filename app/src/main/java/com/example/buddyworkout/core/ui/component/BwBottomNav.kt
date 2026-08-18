package com.example.buddyworkout.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.buddyworkout.core.ui.icon.BwIcons
import com.example.buddyworkout.core.ui.theme.BuddyWorkoutTheme
import com.example.buddyworkout.core.ui.theme.BwColors
import com.example.buddyworkout.core.ui.theme.BwSize

/** The three root destinations shown in the bottom bar (`.nav`). */
enum class BwNavItem(val label: String, val icon: ImageVector) {
    Home("Home", BwIcons.Home),
    Challenges("Challenges", BwIcons.List),
    Profile("Profile", BwIcons.Person),
}

@Composable
fun BwBottomNav(
    selected: BwNavItem,
    onSelect: (BwNavItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth().background(BwColors.Surface)) {
        HorizontalDivider(thickness = 1.dp, color = BwColors.Line)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(BwSize.BottomNav)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            BwNavItem.entries.forEach { item ->
                val active = item == selected
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                    modifier = Modifier
                        .clickable { onSelect(item) }
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = if (active) BwColors.Primary else BwColors.Placeholder,
                        modifier = Modifier.size(22.dp),
                    )
                    Text(
                        text = item.label,
                        color = if (active) BwColors.Primary else BwColors.Placeholder,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 340)
@Composable
private fun BwBottomNavPreview() = BuddyWorkoutTheme {
    Column {
        BwBottomNav(BwNavItem.Home, {})
        BwBottomNav(BwNavItem.Challenges, {})
    }
}
