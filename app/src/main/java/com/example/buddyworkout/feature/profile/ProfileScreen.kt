package com.example.buddyworkout.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.buddyworkout.core.ui.component.Avatar
import com.example.buddyworkout.core.ui.component.BwButton
import com.example.buddyworkout.core.ui.component.BwButtonVariant
import com.example.buddyworkout.core.ui.component.BwCard
import com.example.buddyworkout.core.ui.component.BwTopBar
import com.example.buddyworkout.core.ui.component.DetailRow
import com.example.buddyworkout.core.ui.preview.PreviewData
import com.example.buddyworkout.core.ui.theme.BuddyWorkoutTheme
import com.example.buddyworkout.core.ui.theme.BwColors
import com.example.buddyworkout.core.ui.theme.BwSpace

@Composable
fun ProfileScreen(
    state: ProfileUiState,
    onSignOut: () -> Unit,
    onOpenGallery: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BwColors.Bg),
    ) {
        BwTopBar(title = "Profile")

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(BwSpace.Gutter),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(BwSpace.Md),
        ) {
            Spacer(Modifier.height(BwSpace.Sm))
            Avatar(avatar = state.avatar, size = 84.dp, ring = true)

            Text(
                text = state.name,
                style = MaterialTheme.typography.headlineSmall,
                color = BwColors.Ink,
            )
            Text(
                text = state.email,
                style = MaterialTheme.typography.bodyMedium,
                color = BwColors.Muted,
            )

            Spacer(Modifier.height(BwSpace.Sm))

            BwCard(modifier = Modifier.fillMaxWidth()) {
                state.stats.forEachIndexed { index, stat ->
                    DetailRow(label = stat.label, value = stat.value)
                    if (index != state.stats.lastIndex) Spacer(Modifier.height(BwSpace.Sm))
                }
            }

            Spacer(Modifier.height(BwSpace.Sm))

            BwButton(
                text = "Sign out",
                onClick = onSignOut,
                variant = BwButtonVariant.DangerGhost,
                modifier = Modifier.fillMaxWidth(),
            )

            if (state.showGallery) {
                BwButton(
                    text = "Component gallery (debug)",
                    onClick = onOpenGallery,
                    variant = BwButtonVariant.Outline,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 380, heightDp = 800)
@Composable
private fun ProfileScreenPreview() = BuddyWorkoutTheme {
    ProfileScreen(PreviewData.profile, {}, {})
}
