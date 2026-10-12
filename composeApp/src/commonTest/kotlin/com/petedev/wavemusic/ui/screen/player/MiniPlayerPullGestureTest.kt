package com.petedev.wavemusic.ui.screen.player

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MiniPlayerPullGestureTest {
    @Test
    fun upwardPullRemainsReversibleUntilRelease() {
        val pull = MiniPlayerPullGesture(1000f)
        assertEquals(0.28f, pull.drag(-200f), 0.0001f)
        assertEquals(0.14f, pull.drag(100f), 0.0001f)
        assertEquals(0f, pull.drag(100f))
        assertFalse(pull.shouldExpand())
        assertFalse(pull.shouldDismissPlayback(80f))
        assertEquals(0.42f, pull.drag(-300f), 0.0001f)
        assertTrue(pull.shouldExpand())
    }

    @Test
    fun returningBelowTheStartingPointDoesNotStopPlayback() {
        val pull = MiniPlayerPullGesture(1000f)
        pull.drag(-300f)
        assertEquals(0f, pull.drag(600f))
        assertFalse(pull.shouldExpand())
        assertFalse(pull.shouldDismissPlayback(80f))
        pull.drag(100f)
        assertFalse(pull.shouldDismissPlayback(80f))
    }

    @Test
    fun downwardDismissStillWorksForADownwardOnlyGesture() {
        val pull = MiniPlayerPullGesture(1000f)
        assertEquals(0f, pull.drag(79f))
        assertFalse(pull.shouldDismissPlayback(80f))
        pull.drag(2f)
        assertTrue(pull.shouldDismissPlayback(80f))
        assertFalse(pull.shouldExpand())
    }

    @Test
    fun fullExpansionCanStillBeReversedAndProgressIsBounded() {
        val pull = MiniPlayerPullGesture(1000f)
        assertEquals(1f, pull.drag(-2000f))
        assertEquals(0.7f, pull.drag(500f), 0.0001f)
        assertEquals(0f, pull.drag(500f))
        assertFalse(pull.shouldExpand())
        assertFalse(pull.shouldDismissPlayback(80f))
        assertTrue(MiniPlayerPullGesture(0f).drag(-1f).isFinite())
    }
}
