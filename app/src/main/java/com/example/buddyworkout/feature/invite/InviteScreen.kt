package com.example.buddyworkout.feature.invite

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.example.buddyworkout.core.ui.component.BwButton
import com.example.buddyworkout.core.ui.component.BwButtonVariant
import com.example.buddyworkout.core.ui.component.BwCard
import com.example.buddyworkout.core.ui.component.BwIconButton
import com.example.buddyworkout.core.ui.component.BwTopBar
import com.example.buddyworkout.core.ui.component.NoticeCard
import com.example.buddyworkout.core.ui.component.SectionTitle
import com.example.buddyworkout.core.ui.icon.BwIcons
import com.example.buddyworkout.core.ui.preview.PreviewData
import com.example.buddyworkout.core.ui.theme.BuddyWorkoutTheme
import com.example.buddyworkout.core.ui.theme.BwColors
import com.example.buddyworkout.core.ui.theme.BwSpace

@Composable
fun InviteScreen(
    state: InviteUiState,
    onCopyLink: () -> Unit,
    onShareLink: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BwColors.Bg),
    ) {
        BwTopBar(title = "Invite buddies", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(BwSpace.Gutter),
            verticalArrangement = Arrangement.spacedBy(BwSpace.Md),
        ) {
            NoticeCard(
                text = "Anyone with this link can join you. It never expires — share it in any chat.",
                icon = BwIcons.Info,
            )

            SectionTitle(text = "Your invite code")

            BwCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = state.code,
                    style = MaterialTheme.typography.headlineMedium,
                    color = BwColors.Primary,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = BwSpace.Sm),
                ) {
                    Text(
                        text = state.link,
                        style = MaterialTheme.typography.bodyMedium,
                        color = BwColors.Muted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    BwIconButton(
                        icon = BwIcons.Link,
                        onClick = onCopyLink,
                        contentDescription = "Copy link",
                        tinted = true,
                    )
                }
            }

            if (state.copied) {
                Text(
                    text = "Copied to clipboard",
                    style = MaterialTheme.typography.bodyMedium,
                    color = BwColors.Primary,
                )
            }

            BwButton(
                text = "Share link",
                onClick = onShareLink,
                icon = BwIcons.Share,
                modifier = Modifier.fillMaxWidth(),
            )
            BwButton(
                text = "Copy link",
                onClick = onCopyLink,
                variant = BwButtonVariant.Outline,
                icon = BwIcons.Link,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 380, heightDp = 800)
@Composable
private fun InviteScreenPreview() = BuddyWorkoutTheme {
    InviteScreen(PreviewData.invite, {}, {}, {})
}
