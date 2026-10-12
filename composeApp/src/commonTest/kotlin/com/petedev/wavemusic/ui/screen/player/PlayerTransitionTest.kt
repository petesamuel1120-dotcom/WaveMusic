package com.petedev.wavemusic.ui.screen.player

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.TargetBasedAnimation
import androidx.compose.animation.core.VectorConverter
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PlayerTransitionTest {
    private fun transition(spec: AnimationSpec<Float>, from: Float, to: Float, velocity: Float = 0f) =
        TargetBasedAnimation(spec, Float.VectorConverter, from, to, velocity)

    @Test
    fun openingFinishesWithLessThanHalfAPixelToTravel() {
        for (start in listOf(0f, 0.25f, 0.75f, 0.98f)) {
            val animation = transition(playerExpandSpringSpec(), start, 1f)
            val beforeFinish = animation.getValueFromNanos(animation.durationNanos - 1)
            val finish = animation.getValueFromNanos(animation.durationNanos)
            assertEquals(1f, finish)
            assertTrue(abs(finish - beforeFinish) * 3840f < 0.5f, "Visible finish jump from $start")
        }
    }

    @Test
    fun openingDeceleratesThroughItsLastDisplayedFrame() {
        val animation = transition(playerExpandSpringSpec(), 0f, 1f)
        for (frameNanos in listOf(8_333_333L, 16_666_667L, 33_333_333L)) {
            val before = animation.getValueFromNanos(animation.durationNanos - 2 * frameNanos)
            val last = animation.getValueFromNanos(animation.durationNanos - frameNanos)
            val finalStep = 1f - last
            val precedingStep = last - before
            // Subpixel rounding is invisible; a last-frame acceleration of several pixels is not.
            assertTrue((finalStep - precedingStep) * 3840f < 0.5f, "Accelerated at finish for $frameNanos")
        }
    }

    @Test
    fun openingFromRestMovesForwardWithoutOvershoot() {
        val animation = transition(playerExpandSpringSpec(), 0f, 1f)
        var previous = 0f
        var time = 0L
        while (time < animation.durationNanos) {
            val progress = animation.getValueFromNanos(time)
            assertTrue(progress in previous..1f, "Reversed or overshot at $time")
            previous = progress
            time += 8_333_333L
        }
    }

    @Test
    fun closingAndInterruptedReturnsAlsoSettleBelowHalfAPixel() {
        for ((from, to, velocity) in listOf(Triple(1f, 0f, 0f), Triple(0.6f, 0f, -1f), Triple(0.6f, 1f, -1f))) {
            val spec = if (to == 1f) playerExpandSpringSpec() else playerCollapseSpringSpec()
            val animation = transition(spec, from, to, velocity)
            assertEquals(from, animation.getValueFromNanos(0))
            val beforeFinish = animation.getValueFromNanos(animation.durationNanos - 1)
            assertTrue(abs(to - beforeFinish) * 3840f < 0.5f, "Visible settling jump for $from -> $to")
        }
    }
}
