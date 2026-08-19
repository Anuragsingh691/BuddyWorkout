package com.example.buddyworkout.core.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SampleSizeTest {

    @Test fun `a large photo is halved until it is near the target`() {
        // 4000px down to 1000px: still above 512, and the next halving would
        // drop under it.
        assertEquals(4, sampleSizeFor(longestEdge = 4000, targetPx = 512))
    }

    @Test fun `an image already at the target is not sampled`() {
        assertEquals(1, sampleSizeFor(longestEdge = 512, targetPx = 512))
        assertEquals(1, sampleSizeFor(longestEdge = 300, targetPx = 512))
    }

    @Test fun `the decoded image is never smaller than the target`() {
        for (edge in listOf(513, 1024, 1025, 2000, 4000, 8000)) {
            val decoded = edge / sampleSizeFor(edge, targetPx = 512)
            assertTrue("$edge decoded to $decoded, under the 512 target", decoded >= 512)
        }
    }

    @Test fun `the sample size is always a power of two, as BitmapFactory requires`() {
        for (edge in listOf(0, 1, 513, 999, 4000, 12000)) {
            val sample = sampleSizeFor(edge, targetPx = 512)
            assertTrue("$sample is not a power of two", sample > 0 && sample and (sample - 1) == 0)
        }
    }

    @Test fun `unreadable bounds do not sample and do not hang`() {
        assertEquals(1, sampleSizeFor(longestEdge = 0, targetPx = 512))
        assertEquals(1, sampleSizeFor(longestEdge = -1, targetPx = 512))
    }

    @Test fun `a nonsense target does not spin forever`() =
        assertEquals(1, sampleSizeFor(longestEdge = 4000, targetPx = 0))
}
