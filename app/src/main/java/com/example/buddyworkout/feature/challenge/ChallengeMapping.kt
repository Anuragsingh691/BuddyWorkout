package com.example.buddyworkout.feature.challenge

import com.example.buddyworkout.core.common.formatEndsIn
import com.example.buddyworkout.core.ui.component.AvatarUi
import com.example.buddyworkout.core.ui.model.ChallengeUi
import com.example.buddyworkout.core.ui.model.ParticipantUi
import com.example.buddyworkout.data.challenge.ChallengeWithParticipants
import com.example.buddyworkout.data.challenge.Participant

/** How many buddies a card names before falling back to "+N". */
private const val NAMED_BUDDIES = 2

private const val MINUTE = 60_000L
private const val HOUR = 60 * MINUTE

/**
 * Domain to UI, kept pure and given [nowMillis] rather than reading a clock so
 * every branch below is directly testable.
 *
 * Lives in `feature/challenge` because that is where it is most at home, and
 * Home imports it: the alternative was putting a data-layer dependency into
 * `core/ui`, which is meant to know nothing about repositories.
 */
fun ChallengeWithParticipants.toChallengeUi(myUid: String, nowMillis: Long): ChallengeUi {
    val mine = participant(myUid)
    val others = ranked.filter { it.uid != myUid }

    return ChallengeUi(
        id = challenge.id,
        title = challenge.title,
        summary = summaryOf(others, nowMillis),
        progress = progressAt(nowMillis),
        stat = "${mine?.totalReps ?: 0} reps",
        members = ranked.map { it.toAvatar() },
        remaining = formatEndsIn(challenge.endAtMillis - nowMillis),
        isCompleted = challenge.status.isOver,
        rank = rankOf(myUid),
    )
}

fun ChallengeWithParticipants.toParticipantUi(
    myUid: String,
    nowMillis: Long,
): List<ParticipantUi> = ranked.mapIndexed { index, person ->
    ParticipantUi(
        uid = person.uid,
        name = person.displayName,
        avatar = person.toAvatar(),
        rank = index + 1,
        // Bare number: the detail leaderboard puts the unit in its own column.
        reps = person.totalReps.toString(),
        subtitle = person.lastActiveAtMillis?.let { formatLastActive(nowMillis - it) },
        isMe = person.uid == myUid,
    )
}

/** e.g. `"with Rohit, Priya +1 · ends in 3 days"`. */
private fun ChallengeWithParticipants.summaryOf(
    others: List<Participant>,
    nowMillis: Long,
): String {
    val deadline = formatEndsIn(challenge.endAtMillis - nowMillis)
    if (others.isEmpty()) return deadline

    val named = others.take(NAMED_BUDDIES).joinToString(", ") { it.displayName.substringBefore(' ') }
    val extra = others.size - NAMED_BUDDIES
    val people = if (extra > 0) "$named +$extra" else named
    return "with $people · $deadline"
}

/**
 * Elapsed fraction of the challenge window.
 *
 * A zero-length window reads as finished rather than dividing by zero — a
 * challenge whose start and end coincide has no time left in it.
 */
private fun ChallengeWithParticipants.progressAt(nowMillis: Long): Float {
    val span = challenge.endAtMillis - challenge.startAtMillis
    if (span <= 0L) return 1f
    return ((nowMillis - challenge.startAtMillis).toFloat() / span).coerceIn(0f, 1f)
}

/** e.g. `"Active now"`, `"2 min ago"`, `"3 hr ago"`. */
private fun formatLastActive(sinceMillis: Long): String = when {
    sinceMillis < MINUTE -> "Active now"
    sinceMillis < HOUR -> "${sinceMillis / MINUTE} min ago"
    sinceMillis < 24 * HOUR -> "${sinceMillis / HOUR} hr ago"
    else -> "${sinceMillis / (24 * HOUR)} d ago"
}

private fun Participant.toAvatar() = AvatarUi(
    initials = initialsOf(displayName),
    key = uid,
)

/** First letter of each of the first two words, e.g. "Rohit Kumar" -> "RK". */
private fun initialsOf(name: String): String = name
    .trim()
    .split(Regex("\\s+"))
    .filter { it.isNotBlank() }
    .take(2)
    .map { it.first().uppercaseChar() }
    .joinToString("")
