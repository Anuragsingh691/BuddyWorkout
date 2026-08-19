package com.example.buddyworkout.core.ui.model

import com.example.buddyworkout.core.ui.component.AvatarUi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ChallengeUiTest {

    private fun challenge(rank: Int?) = ChallengeUi(
        id = "c1",
        title = "Pushup challenge",
        summary = "with Rohit, Priya +2 · ends in 3 days",
        progress = 0.64f,
        stat = "128 reps",
        members = listOf(AvatarUi("RK")),
        remaining = "3 days left",
        rank = rank,
    )

    @Test fun `standings read as ordinals`() {
        assertEquals("1st", challenge(1).rankLabel)
        assertEquals("2nd", challenge(2).rankLabel)
        assertEquals("3rd", challenge(3).rankLabel)
        assertEquals("4th", challenge(4).rankLabel)
        assertEquals("5th", challenge(5).rankLabel)
    }

    @Test fun `a challenge with no standing yet has no label`() =
        assertNull(challenge(null).rankLabel)

    @Test fun `leading is what tints the card green rather than amber`() {
        assertEquals(true, challenge(1).isLeading)
        assertEquals(false, challenge(2).isLeading)
        // Nothing logged yet reads as neutral, not as losing.
        assertEquals(true, challenge(null).isLeading)
    }
}
