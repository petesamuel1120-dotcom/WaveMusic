package com.petedev.wavemusic.ui.component

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StickyPlaylistTitleGeometryTest {
    private fun position(y: Float) = stickyPlaylistTitlePosition(y, 100f, 200f, 160f, 120f, 200f, 64f)

    @Test fun followsBannerBeforeHeader() {
        val p = position(300f)
        assertEquals(300f, p.top)
        assertEquals(140f, p.left)
        assertEquals(120f, p.width)
        assertEquals(0f, p.progress)
    }

    @Test fun pinsAtHeaderAndStaysThereAfterBannerIsRecycled() {
        for (y in listOf(100f, 0f, -300f, Float.NEGATIVE_INFINITY)) {
            assertEquals(position(100f), position(y))
            assertEquals(100f, position(y).top)
            assertEquals(60f, position(y).left)
        }
    }

    @Test fun easesHorizontalPositionAndWidthWithoutJumpingAtEitherEnd() {
        var previous = position(164f)
        for (step in 1..640) {
            val next = position(164f - step / 10f)
            assertTrue(next.progress >= previous.progress)
            assertTrue(next.progress - previous.progress < 0.003f)
            assertTrue(next.width >= previous.width)
            assertTrue(previous.left - next.left < 0.3f)
            previous = next
        }
        assertEquals(0.5f, position(132f).progress)
    }

    @Test fun reverseScrollUsesExactlyTheSamePath() {
        val forward = (0..64).map { position(164f - it) }
        val reverse = (64 downTo 0).map { position(164f - it) }
        assertEquals(forward, reverse.reversed())
    }

    @Test fun shortLandscapeHeroStartsAtItsOwnAlignmentAndTypography() {
        val p = stickyPlaylistTitlePosition(100f, 60f, 560f, 450f, 580f, 700f, 96f, sourceStartTop = 100f)
        assertEquals(0f, p.progress, "Opening a short hero must not start its title handoff")
        assertEquals(270f, p.left)
        assertEquals(580f, p.width)
        assertEquals(100f, p.top)
    }

    @Test fun shortLandscapeHeroMovesContinuouslyAndReturnsToItsStartingPoint() {
        fun compact(y: Float) =
            stickyPlaylistTitlePosition(y, 60f, 560f, 450f, 580f, 700f, 96f, sourceStartTop = 100f)
        val forward = (0..400).map { compact(100f - it / 10f) }
        val reverse = (400 downTo 0).map { compact(100f - it / 10f) }
        assertEquals(forward, reverse.reversed())
        assertEquals(0.5f, compact(80f).progress)
        assertEquals(1f, compact(60f).progress)
        forward.zipWithNext().forEach { (previous, next) ->
            assertTrue(next.progress >= previous.progress)
            assertTrue(next.progress - previous.progress < 0.004f)
        }
        assertEquals(compact(60f), compact(Float.NEGATIVE_INFINITY))
    }

    @Test fun tallPortraitHeroKeepsItsExistingHandoffDistance() {
        for (y in listOf(500f, 156f, 108f, 60f, Float.NEGATIVE_INFINITY)) {
            val original = stickyPlaylistTitlePosition(y, 60f, 560f, 450f, 580f, 700f, 96f)
            val measured = stickyPlaylistTitlePosition(y, 60f, 560f, 450f, 580f, 700f, 96f, sourceStartTop = 500f)
            assertEquals(original, measured)
        }
    }
}
