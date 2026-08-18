package com.example.buddyworkout.core.ui.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.buddyworkout.core.ui.component.ActionTile
import com.example.buddyworkout.core.ui.component.Avatar
import com.example.buddyworkout.core.ui.component.AvatarStack
import com.example.buddyworkout.core.ui.component.AvatarUi
import com.example.buddyworkout.core.ui.component.BwBottomNav
import com.example.buddyworkout.core.ui.component.BwButton
import com.example.buddyworkout.core.ui.component.BwButtonVariant
import com.example.buddyworkout.core.ui.component.BwCard
import com.example.buddyworkout.core.ui.component.BwIconButton
import com.example.buddyworkout.core.ui.component.BwNavItem
import com.example.buddyworkout.core.ui.component.BwSegmentedTabs
import com.example.buddyworkout.core.ui.component.BwTextField
import com.example.buddyworkout.core.ui.component.BwTopBar
import com.example.buddyworkout.core.ui.component.CameraOverlay
import com.example.buddyworkout.core.ui.component.ChallengeCard
import com.example.buddyworkout.core.ui.component.CountdownCard
import com.example.buddyworkout.core.ui.component.DetailRow
import com.example.buddyworkout.core.ui.component.LeaderboardRow
import com.example.buddyworkout.core.ui.component.NoticeCard
import com.example.buddyworkout.core.ui.component.Pill
import com.example.buddyworkout.core.ui.component.PillTone
import com.example.buddyworkout.core.ui.component.RowTone
import com.example.buddyworkout.core.ui.component.SectionTitle
import com.example.buddyworkout.core.ui.component.WinnerBanner
import com.example.buddyworkout.core.ui.icon.BwIcons
import com.example.buddyworkout.core.ui.theme.BuddyWorkoutTheme
import com.example.buddyworkout.core.ui.theme.BwColors
import com.example.buddyworkout.core.ui.theme.BwSize
import com.example.buddyworkout.core.ui.theme.BwSpace

// Swatches pinned to the ones the Figma frames use for these four people.
private val People = listOf(
    AvatarUi("RK", color = BwColors.Avatar[0]),
    AvatarUi("PM", color = BwColors.Avatar[2]),
    AvatarUi("SV", color = BwColors.Avatar[3]),
    AvatarUi("AS", color = BwColors.Avatar[5]),
)

/**
 * Runnable catalogue of every component in `core/ui/component`, for eyeballing
 * the library against the Figma frames before screens are built on top of it.
 *
 * This is scaffolding for the design-system phase — it is replaced by the real
 * `NavHost` once the screens land.
 */
@Composable
fun ComponentGallery() {
    var email by remember { mutableStateOf("anurag@gmail.com") }
    var password by remember { mutableStateOf("hunter2000") }
    var phone by remember { mutableStateOf("") }
    var tab by remember { mutableIntStateOf(0) }
    var nav by remember { mutableStateOf(BwNavItem.Home) }

    Scaffold(
        containerColor = BwColors.Bg,
        bottomBar = { BwBottomNav(nav, { nav = it }) },
    ) { inner ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner),
            contentPadding = PaddingValues(
                start = BwSpace.Gutter,
                end = BwSpace.Gutter,
                bottom = 32.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(BwSpace.Md),
        ) {
            item {
                BwTopBar(
                    title = "Design system",
                    overline = "BuddyWorkout",
                    actions = {
                        BwIconButton(BwIcons.Bell, {}, "Notifications")
                        Avatar(People[3], size = BwSize.IconButton)
                    },
                )
            }

            group("Buttons") {
                BwButton("Log in", {}, Modifier.fillMaxWidth())
                BwButton("Continue with Google", {}, Modifier.fillMaxWidth(), BwButtonVariant.Outline, BwIcons.Google)
                BwButton("Record workout", {}, Modifier.fillMaxWidth(), icon = BwIcons.Video)
                BwButton("Create challenge · limit reached", {}, Modifier.fillMaxWidth(), BwButtonVariant.Outline, BwIcons.Lock, enabled = false)
                Row(horizontalArrangement = Arrangement.spacedBy(BwSpace.Md)) {
                    BwButton("Challenge", {}, variant = BwButtonVariant.Tint, height = BwSize.Chip)
                    BwButton("Decline", {}, variant = BwButtonVariant.DangerGhost)
                }
            }

            group("Text fields") {
                BwTextField(email, { email = it }, label = "Email")
                BwTextField(password, { password = it }, label = "Password", trailingIcon = BwIcons.Eye, isPassword = true)
                BwTextField(phone, { phone = it }, label = "Phone", labelHint = "(optional)", placeholder = "+91 ·····")
            }

            group("Avatars") {
                Row(horizontalArrangement = Arrangement.spacedBy(BwSpace.Sm)) {
                    People.forEach { Avatar(it) }
                }
                AvatarStack(People)
                AvatarStack(People, size = 60.dp)
            }

            group("Pills & section headers") {
                Row(horizontalArrangement = Arrangement.spacedBy(BwSpace.Sm)) {
                    Pill("Active")
                    Pill("1st")
                    Pill("2nd", tone = PillTone.Amber)
                    Pill("Live", tone = PillTone.Live)
                }
                SectionTitle("Active challenges", hint = "· tap to open →")
                SectionTitle("Live leaderboard", trailing = { Pill("Live", tone = PillTone.Live) })
                BwSegmentedTabs(listOf("Active · 2", "Past · 3"), tab, { tab = it })
            }

            group("Home tiles") {
                Row(horizontalArrangement = Arrangement.spacedBy(BwSpace.Md)) {
                    ActionTile("Invite buddies", "Share your link", BwIcons.PersonAdd, {}, Modifier.weight(1f))
                    ActionTile("Create challenge", "Up to 4 buddies", BwIcons.Plus, {}, Modifier.weight(1f), filled = false)
                }
            }

            group("Challenge cards") {
                ChallengeCard("Pushup challenge", "with Rohit, Priya +2 · ends in 3 days", 0.64f, "128 reps · 1st", {})
                ChallengeCard(
                    title = "Pushup challenge",
                    summary = "with Sahil, Neha +1 · ends in 6 days",
                    progress = 0.38f,
                    stat = "42 reps",
                    onClick = {},
                    accent = BwColors.Amber,
                    trailing = { Pill("2nd", tone = PillTone.Amber) },
                )
            }

            group("Cards & notices") {
                BwCard {
                    Column(verticalArrangement = Arrangement.spacedBy(BwSpace.Md)) {
                        DetailRow("Exercise", "Pushups")
                        DetailRow("Members", "4 people")
                        DetailRow("Window", "17 Jun 6PM → 18 Jun 6AM")
                        DetailRow("Avg form score", "92%", valueColor = BwColors.PrimaryDark)
                    }
                }
                NoticeCard(
                    "You're in 2 of 2 active challenges. Finish one to create a new one.",
                    BwIcons.AlertTriangle,
                )
            }

            group("Countdown & winner") {
                CountdownCard("02:14:53", People)
                WinnerBanner("Anurag wins!", "203 pushups · most reps")
            }

            group("Leaderboard") {
                LeaderboardRow(People[3], "You", rank = 1, subtitle = "Active now", value = "128", tone = RowTone.Highlight)
                LeaderboardRow(People[0], "Rohit Kumar", rank = 2, subtitle = "2 min ago", value = "119")
                LeaderboardRow(People[1], "Priya Mehta", rank = 3, subtitle = "18 min ago", value = "64")
                LeaderboardRow(People[2], "Sahil Verma", rank = 4, subtitle = "1 hr ago", value = "40", showDivider = false)
                LeaderboardRow(People[3], "Anurag (you)", rank = 1, subtitle = "Finished 17 Jun · 8:40pm", value = "203", tone = RowTone.Gold)
                LeaderboardRow(
                    avatar = AvatarUi("NK"),
                    name = "Neha Kapoor",
                    subtitle = "Not in a challenge",
                    showDivider = false,
                    trailing = { BwButton("Challenge", {}, variant = BwButtonVariant.Tint, height = BwSize.Chip) },
                )
            }

            group("Camera overlay") {
                CameraOverlay(
                    reps = 12,
                    formLabel = "Good form",
                    footerLabel = "PUSHUP CHALLENGE · 1st of 4",
                    footerValue = "128 total · +12 this session",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp),
                )
            }
        }
    }
}

/** One labelled block of the catalogue. */
private fun LazyListScope.group(
    title: String,
    content: @Composable () -> Unit,
) {
    item {
        Column {
            Spacer(Modifier.height(BwSpace.Sm))
            Text(
                text = title.uppercase(),
                color = BwColors.Muted,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = BwSpace.Sm),
            )
            Column(verticalArrangement = Arrangement.spacedBy(BwSpace.Md)) { content() }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 2400)
@Composable
private fun ComponentGalleryPreview() = BuddyWorkoutTheme { ComponentGallery() }
