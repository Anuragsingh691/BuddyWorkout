package com.example.buddyworkout.core.ui.preview

import com.example.buddyworkout.core.ui.component.AvatarUi
import com.example.buddyworkout.core.ui.model.BuddyUi
import com.example.buddyworkout.core.ui.model.ChallengeUi
import com.example.buddyworkout.core.ui.model.ParticipantUi
import com.example.buddyworkout.core.ui.theme.avatarColorFor
import com.example.buddyworkout.feature.auth.LoginUiState
import com.example.buddyworkout.feature.auth.RegisterUiState
import com.example.buddyworkout.feature.challenge.ChallengeDetailUiState
import com.example.buddyworkout.feature.challenge.ChallengesUiState
import com.example.buddyworkout.feature.challenge.create.BuddyPickerUiState
import com.example.buddyworkout.feature.challenge.create.CreateChallengeUiState
import com.example.buddyworkout.feature.challenge.create.DateTimePickerUiState
import com.example.buddyworkout.feature.home.HomeUiState
import com.example.buddyworkout.feature.invite.ChallengeInviteUiState
import com.example.buddyworkout.feature.invite.InviteUiState
import com.example.buddyworkout.feature.profile.ProfileUiState
import com.example.buddyworkout.feature.profile.StatUi
import com.example.buddyworkout.feature.record.RecordUiState
import com.example.buddyworkout.feature.result.WinnerUiState

/**
 * Hardcoded state with two consumers: `@Preview` functions (permanently) and
 * the NavHost while screens have no ViewModel yet (temporarily).
 *
 * Lives in `main`, not `debug`, because previews compile against `main`.
 */
object PreviewData {

    val me = AvatarUi("AS", color = avatarColorFor("AS"))
    val people = listOf(
        AvatarUi("RK", color = avatarColorFor("RK")),
        AvatarUi("PM", color = avatarColorFor("PM")),
        AvatarUi("SV", color = avatarColorFor("SV")),
        me,
    )

    val challenges = listOf(
        ChallengeUi(
            id = "c1",
            title = "Pushup challenge",
            summary = "with Rohit, Priya +2 · ends in 3 days",
            progress = 0.64f,
            stat = "128 reps",
            members = people,
            remaining = "3 days left",
            rank = 1,
        ),
        ChallengeUi(
            id = "c2",
            title = "Pushup challenge",
            summary = "with Sahil, Neha +1 · ends in 6 days",
            progress = 0.38f,
            stat = "42 reps",
            members = people.take(2),
            remaining = "6 days left",
            rank = 2,
        ),
    )

    val completedChallenges = listOf(
        ChallengeUi(
            id = "c0",
            title = "Pushup challenge",
            summary = "3 buddies · won by Rahul",
            progress = 1f,
            stat = "204 reps",
            members = people.take(3),
            remaining = "Ended",
            isCompleted = true,
        ),
    )

    val leaderboard = listOf(
        ParticipantUi("u1", "Rahul K.", people[0], rank = 1, reps = "204 reps", subtitle = "Avg form 94%"),
        ParticipantUi("u2", "Priya M.", people[1], rank = 2, reps = "186 reps", subtitle = "Avg form 91%"),
        ParticipantUi("u4", "You", me, rank = 3, reps = "128 reps", subtitle = "Avg form 88%", isMe = true),
        ParticipantUi("u3", "Sameer V.", people[2], rank = 4, reps = "96 reps", subtitle = "Avg form 85%"),
    )

    val buddies = listOf(
        BuddyUi("u1", "Rahul K.", people[0], subtitle = "In 1 active challenge", selected = true),
        BuddyUi("u2", "Priya M.", people[1], subtitle = "Free", selected = true),
        BuddyUi("u3", "Sameer V.", people[2], subtitle = "In 2 active challenges"),
    )

    val login = LoginUiState(email = "anurag@example.com", password = "secret123")

    val register = RegisterUiState(
        name = "Anurag Shishodia",
        email = "anurag@example.com",
        password = "secret123",
    )

    val home = HomeUiState(
        greeting = "Hi, Anurag 👋",
        avatar = me,
        activeChallenges = challenges,
    )

    val profile = ProfileUiState(
        name = "Anurag S.",
        email = "anurag@example.com",
        avatar = me,
        stats = listOf(
            StatUi("Challenges won", "3"),
            StatUi("Total reps", "1,482"),
            StatUi("Best session", "62 reps"),
        ),
        showGallery = true,
    )

    val challengesTab = ChallengesUiState(active = challenges, completed = completedChallenges)

    val challengeDetail = ChallengeDetailUiState(
        remaining = "2d 14h left",
        startsAt = "Mon 18 Aug, 06:00",
        endsAt = "Wed 20 Aug, 06:00",
        members = people,
        leaderboard = leaderboard,
        isCreator = true,
    )

    val createChallenge = CreateChallengeUiState(
        selectedBuddies = buddies.filter { it.selected },
        startLabel = "Now",
        endLabel = "Wed 20 Aug, 06:00",
        durationLabel = "2 days",
    )

    val buddyPicker = BuddyPickerUiState(buddies = buddies)

    val dateTimePicker = DateTimePickerUiState(
        presets = listOf("6 hours", "12 hours", "1 day", "3 days", "1 week", "2 weeks"),
        selectedPresetIndex = 3,
        startLabel = "Now (Mon 18 Aug, 06:00)",
        endLabel = "Thu 21 Aug, 06:00",
    )

    val invite = InviteUiState(
        link = "commworkout.app/i/anurag",
        buddies = listOf(
            BuddyUi("b1", "Rohit Kumar", AvatarUi("RK", color = avatarColorFor("RK")),
                subtitle = "In 1 challenge", inChallenge = true),
            BuddyUi("b2", "Priya Mehta", AvatarUi("PM", color = avatarColorFor("PM")),
                subtitle = "In 1 challenge", inChallenge = true),
            BuddyUi("b3", "Sahil Verma", AvatarUi("SV", color = avatarColorFor("SV")),
                subtitle = "No challenge yet"),
            BuddyUi("b4", "Neha Arora", AvatarUi("NA", color = avatarColorFor("NA")),
                subtitle = "No challenge yet"),
        ),
    )

    val challengeInvite = ChallengeInviteUiState(
        headline = "Rahul K. invited you",
        members = people.take(3),
        remaining = "2d 14h left",
        startsAt = "Mon 18 Aug, 06:00",
        endsAt = "Wed 20 Aug, 06:00",
    )

    val record = RecordUiState(
        reps = 17,
        formLabel = "Good form",
        elapsedLabel = "04:12",
        isRunning = true,
        hasCameraPermission = true,
    )

    val winner = WinnerUiState(
        winnerName = "Rahul K.",
        detail = "204 reps over 3 days",
        leaderboard = leaderboard,
    )
}
