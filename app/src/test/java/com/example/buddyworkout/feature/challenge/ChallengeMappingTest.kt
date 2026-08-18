package com.example.buddyworkout.feature.challenge

import com.example.buddyworkout.data.challenge.Challenge
import com.example.buddyworkout.data.challenge.ChallengeStatus
import com.example.buddyworkout.data.challenge.ChallengeWithParticipants
import com.example.buddyworkout.data.challenge.Participant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

private const val HOUR = 60 * 60 * 1000L
private const val DAY = 24 * HOUR

private const val ME = "me"

class ChallengeMappingTest {

    private val now = 1_000_000_000L

    private fun challenge(
        start: Long = now - DAY,
        end: Long = now + 3 * DAY,
        status: ChallengeStatus = ChallengeStatus.Active,
    ) = Challenge(
        id = "c1",
        creatorUid = ME,
        memberUids = listOf(ME, "u1", "u2", "u3"),
        startAtMillis = start,
        endAtMillis = end,
        status = status,
    )

    private fun withPeople(
        challenge: Challenge = challenge(),
        vararg people: Participant,
    ) = ChallengeWithParticipants(challenge, people.toList())

    private fun person(uid: String, name: String, reps: Int) =
        Participant(uid = uid, displayName = name, totalReps = reps)

    @Test fun `the summary names buddies and the deadline, as the export prints it`() {
        val card = withPeople(
            people = arrayOf(
                person(ME, "Anurag Shishodia", 128),
                person("u1", "Rohit Kumar", 119),
                person("u2", "Priya Mehta", 64),
                person("u3", "Sahil Verma", 40),
            ),
        ).toChallengeUi(ME, now)

        assertEquals("with Rohit, Priya +1 · ends in 3 days", card.summary)
    }

    @Test fun `a two-person challenge names the other person and stops`() {
        val card = withPeople(
            people = arrayOf(person(ME, "Anurag S.", 10), person("u1", "Rohit Kumar", 5)),
        ).toChallengeUi(ME, now)

        assertEquals("with Rohit · ends in 3 days", card.summary)
    }

    @Test fun `a challenge with nobody else still reads sensibly`() {
        val card = withPeople(people = arrayOf(person(ME, "Anurag S.", 10))).toChallengeUi(ME, now)

        assertEquals("ends in 3 days", card.summary)
    }

    @Test fun `my reps and standing drive the stat and the accent`() {
        val leading = withPeople(
            people = arrayOf(person(ME, "Anurag S.", 128), person("u1", "Rohit K.", 119)),
        ).toChallengeUi(ME, now)

        assertEquals("128 reps", leading.stat)
        assertEquals(1, leading.rank)
        assertEquals("1st", leading.rankLabel)
        assertTrue(leading.isLeading)
    }

    @Test fun `being behind is amber, not green`() {
        val trailing = withPeople(
            people = arrayOf(person(ME, "Anurag S.", 42), person("u1", "Rohit K.", 119)),
        ).toChallengeUi(ME, now)

        assertEquals(2, trailing.rank)
        assertEquals(false, trailing.isLeading)
    }

    @Test fun `progress is the elapsed fraction of the window`() {
        // Quarter of the way through a four-day challenge.
        val card = withPeople(challenge(start = now - DAY, end = now + 3 * DAY))
            .toChallengeUi(ME, now)

        assertEquals(0.25f, card.progress, 0.001f)
    }

    @Test fun `progress never leaves zero to one, whatever the clock says`() {
        val notStarted = withPeople(challenge(start = now + DAY, end = now + 2 * DAY))
            .toChallengeUi(ME, now)
        val overdue = withPeople(challenge(start = now - 5 * DAY, end = now - DAY))
            .toChallengeUi(ME, now)

        assertEquals(0f, notStarted.progress, 0.001f)
        assertEquals(1f, overdue.progress, 0.001f)
    }

    @Test fun `a zero-length window does not divide by zero`() {
        val card = withPeople(challenge(start = now, end = now)).toChallengeUi(ME, now)

        assertEquals(1f, card.progress, 0.001f)
    }

    @Test fun `a finished challenge says so and is marked completed`() {
        val card = withPeople(
            challenge(end = now - HOUR, status = ChallengeStatus.Completed),
            person(ME, "Anurag S.", 128),
        ).toChallengeUi(ME, now)

        assertTrue(card.isCompleted)
        assertEquals("ended", card.remaining)
    }

    @Test fun `a cancelled challenge counts as over too`() {
        val card = withPeople(challenge(status = ChallengeStatus.Cancelled))
            .toChallengeUi(ME, now)

        assertTrue(card.isCompleted)
    }

    @Test fun `someone with no participant record has no standing rather than rank zero`() {
        val card = withPeople(people = arrayOf(person("u1", "Rohit K.", 119)))
            .toChallengeUi(ME, now)

        assertNull(card.rank)
        assertEquals("0 reps", card.stat)
    }

    @Test fun `the leaderboard is ordered, ranked and marks me`() {
        val rows = withPeople(
            people = arrayOf(
                person("u1", "Rohit Kumar", 119),
                person(ME, "Anurag Shishodia", 128),
                person("u2", "Priya Mehta", 64),
            ),
        ).toParticipantUi(ME, now)

        assertEquals(listOf("Anurag Shishodia", "Rohit Kumar", "Priya Mehta"), rows.map { it.name })
        assertEquals(listOf(1, 2, 3), rows.map { it.rank })
        assertEquals(listOf("128", "119", "64"), rows.map { it.reps })
        assertTrue(rows.first().isMe)
    }

    @Test fun `equal reps break by name so ranks do not jitter`() {
        val rows = withPeople(
            people = arrayOf(person("u2", "Zara", 50), person("u1", "Adam", 50)),
        ).toParticipantUi(ME, now)

        assertEquals(listOf("Adam", "Zara"), rows.map { it.name })
    }

    @Test fun `four others names two and counts the rest`() {
        val card = withPeople(
            people = arrayOf(
                person(ME, "Anurag S.", 1),
                person("u1", "Rohit Kumar", 5),
                person("u2", "Priya Mehta", 4),
                person("u3", "Sahil Verma", 3),
                person("u4", "Neha Arora", 2),
            ),
        ).toChallengeUi(ME, now)

        assertEquals("with Rohit, Priya +2 · ends in 3 days", card.summary)
    }

    @Test fun `last activity reads as a relative time on the leaderboard`() {
        val rows = ChallengeWithParticipants(
            challenge(),
            listOf(
                Participant(ME, "Anurag S.", totalReps = 128, lastActiveAtMillis = now - 30_000),
                Participant("u1", "Rohit K.", totalReps = 119, lastActiveAtMillis = now - 2 * 60_000),
                Participant("u2", "Priya M.", totalReps = 64, lastActiveAtMillis = now - 3 * HOUR),
                Participant("u3", "Sahil V.", totalReps = 40, lastActiveAtMillis = null),
            ),
        ).toParticipantUi(ME, now)

        assertEquals("Active now", rows[0].subtitle)
        assertEquals("2 min ago", rows[1].subtitle)
        assertEquals("3 hr ago", rows[2].subtitle)
        assertNull(rows[3].subtitle)
    }
}
