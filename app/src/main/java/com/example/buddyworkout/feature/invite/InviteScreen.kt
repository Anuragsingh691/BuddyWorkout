package com.example.buddyworkout.feature.invite

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.buddyworkout.core.ui.component.Avatar
import com.example.buddyworkout.core.ui.component.BwButton
import com.example.buddyworkout.core.ui.component.BwButtonVariant
import com.example.buddyworkout.core.ui.component.BwCard
import com.example.buddyworkout.core.ui.component.BwTopBar
import com.example.buddyworkout.core.ui.component.Pill
import com.example.buddyworkout.core.ui.component.SectionTitle
import com.example.buddyworkout.core.ui.icon.BwIcons
import com.example.buddyworkout.core.ui.model.BuddyUi
import com.example.buddyworkout.core.ui.preview.PreviewData
import com.example.buddyworkout.core.ui.theme.BuddyWorkoutTheme
import com.example.buddyworkout.core.ui.theme.BwColors
import com.example.buddyworkout.core.ui.theme.BwRadius
import com.example.buddyworkout.core.ui.theme.BwSize
import com.example.buddyworkout.core.ui.theme.BwSpace

@Composable
fun InviteScreen(
    state: InviteUiState,
    onCopyLink: () -> Unit,
    onShareLink: () -> Unit,
    onChallengeBuddy: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BwColors.Bg),
    ) {
        BwTopBar(title = "Invite buddies", onBack = onBack)
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(BwSpace.Gutter),
        ) {
            item { ShareCard(state = state, onCopyLink = onCopyLink, onShareLink = onShareLink) }
            item { SectionTitle(text = "Your buddies · ${state.buddies.size}") }
            items(state.buddies.size) { index ->
                BuddyRow(
                    buddy = state.buddies[index],
                    onChallenge = { onChallengeBuddy(state.buddies[index].uid) },
                    // `.lb` draws a rule under every row but the last.
                    divider = index < state.buddies.lastIndex,
                )
            }
        }
    }
}

@Composable
private fun ShareCard(
    state: InviteUiState,
    onCopyLink: () -> Unit,
    onShareLink: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BwCard(modifier = modifier, containerColor = BwColors.PrimaryTint) {
        Text(
            text = "Share your invite link",
            style = MaterialTheme.typography.titleSmall,
            color = BwColors.PrimaryDark,
        )
        Text(
            text = "Friends who tap it join your buddy list automatically.",
            style = MaterialTheme.typography.bodyMedium,
            color = BwColors.PrimaryDark,
            modifier = Modifier.padding(top = BwSpace.Xs),
        )

        // The export shows no copy button; the link field itself is the
        // affordance, so tapping it copies rather than losing the action.
        Row(
            modifier = Modifier
                .padding(top = BwSpace.Md)
                .fillMaxWidth()
                .height(BwSize.TextField)
                .background(BwColors.Surface, RoundedCornerShape(BwRadius.Control))
                .clickable(onClick = onCopyLink)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = BwIcons.Link,
                contentDescription = null,
                tint = BwColors.Primary,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = state.link,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                color = BwColors.Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
        }

        if (state.copied) {
            Text(
                text = "Copied to clipboard",
                style = MaterialTheme.typography.bodySmall,
                color = BwColors.PrimaryDark,
                modifier = Modifier.padding(top = 6.dp),
            )
        }

        BwButton(
            text = "Share invite link",
            onClick = onShareLink,
            icon = BwIcons.Share,
            height = BwSize.ButtonCompact,
            modifier = Modifier
                .padding(top = BwSpace.Md)
                .fillMaxWidth(),
        )
    }
}

@Composable
private fun BuddyRow(
    buddy: BuddyUi,
    onChallenge: () -> Unit,
    divider: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = BwSpace.Md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(BwSpace.Md),
        ) {
            Avatar(buddy.avatar)
            Column(Modifier.weight(1f)) {
                Text(
                    text = buddy.name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = BwColors.Ink,
                )
                buddy.subtitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = BwColors.Muted,
                    )
                }
            }
            if (buddy.inChallenge) {
                Pill(text = "Active")
            } else {
                BwButton(
                    text = "Challenge",
                    onClick = onChallenge,
                    variant = BwButtonVariant.Tint,
                    height = BwSize.Chip,
                    compact = true,
                )
            }
        }
        if (divider) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(BwColors.Line),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 380, heightDp = 800)
@Composable
private fun InviteScreenPreview() = BuddyWorkoutTheme {
    InviteScreen(PreviewData.invite, {}, {}, {}, {})
}

@Preview(showBackground = true, widthDp = 380, heightDp = 800)
@Composable
private fun InviteScreenCopiedPreview() = BuddyWorkoutTheme {
    InviteScreen(PreviewData.invite.copy(copied = true), {}, {}, {}, {})
}
