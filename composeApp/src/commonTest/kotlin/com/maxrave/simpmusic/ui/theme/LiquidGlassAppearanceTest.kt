package com.maxrave.simpmusic.ui.theme

import com.maxrave.domain.manager.LiquidGlassIntensity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LiquidGlassAppearanceTest {
    @Test
    fun midpointPreservesExistingMaterial() {
        assertEquals(
            LiquidGlassAppearance(1f, 1f, 1f, 1f, 1f, 1f),
            LiquidGlassAppearance.fromIntensity(0.5f),
        )
    }

    @Test
    fun defaultIntensityIsOneQuarterOfTheSliderRange() {
        assertEquals(0.25f, LiquidGlassIntensity.Default)
    }

    @Test
    fun frostedRangeHasNoMovingEdgeRefraction() {
        for (step in 0..250) {
            val appearance = LiquidGlassAppearance.fromIntensity(step / 1000f)
            assertEquals(0f, appearance.lensHeightMultiplier)
            assertEquals(0f, appearance.lensAmountMultiplier)
            assertTrue(appearance.blurMultiplier >= 2f)
        }
        val firstLiquidStep = LiquidGlassAppearance.fromIntensity(0.251f)
        assertTrue(firstLiquidStep.lensHeightMultiplier > 0f)
        assertTrue(firstLiquidStep.lensHeightMultiplier < 0.001f)
    }

    @Test
    fun endpointsRunFromFrostWithoutDistortionToClearLiquidGlass() {
        val frosted = LiquidGlassAppearance.fromIntensity(0f)
        val normal = LiquidGlassAppearance.fromIntensity(LiquidGlassIntensity.Default)
        val liquid = LiquidGlassAppearance.fromIntensity(1f)
        assertEquals(0f, frosted.lensHeightMultiplier)
        assertEquals(0f, frosted.lensAmountMultiplier)
        assertEquals(0f, frosted.reflectionMultiplier)
        // Bright source pixels should be muted by the same frost across small controls and docks.
        assertTrue(frosted.scrimAlpha(0.272f, isDark = true) >= 0.5f)
        assertTrue(frosted.blurMultiplier > normal.blurMultiplier)
        assertTrue(liquid.blurMultiplier < normal.blurMultiplier)
        assertTrue(liquid.lensAmountMultiplier > normal.lensAmountMultiplier * 2f)
        assertTrue(liquid.reflectionMultiplier > normal.reflectionMultiplier)
        assertTrue(liquid.motionMultiplier > normal.motionMultiplier)
    }

    @Test
    fun fullSliderRangeIsContinuousMonotonicAndKeepsValidScrims() {
        var previous = LiquidGlassAppearance.fromIntensity(0f)
        for (step in 0..1000) {
            val current = LiquidGlassAppearance.fromIntensity(step / 1000f)
            assertTrue(current.blurMultiplier > 0f)
            assertTrue(current.blurMultiplier <= previous.blurMultiplier)
            assertTrue(current.lensAmountMultiplier >= previous.lensAmountMultiplier)
            assertTrue(current.reflectionMultiplier >= previous.reflectionMultiplier)
            assertTrue(current.lensAmountMultiplier - previous.lensAmountMultiplier < 0.01f)
            for (scrim in listOf(0f, 0.12f, 0.5f, 0.62f, 0.84f, 1f)) {
                assertTrue(current.scrimAlpha(scrim, isDark = true) in 0f..1f)
                assertTrue(current.scrimAlpha(scrim, isDark = false) in 0f..1f)
                if (scrim >= 0.5f) assertTrue(current.scrimAlpha(scrim, isDark = false) >= scrim * 0.9f)
            }
            previous = current
        }
    }

    @Test
    fun invalidStoredValuesCannotReachRenderEffects() {
        val default = LiquidGlassAppearance.fromIntensity(LiquidGlassIntensity.Default)
        for (invalid in listOf(Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY)) {
            assertEquals(default, LiquidGlassAppearance.fromIntensity(invalid))
        }
        assertEquals(LiquidGlassAppearance.fromIntensity(0f), LiquidGlassAppearance.fromIntensity(-10f))
        assertEquals(LiquidGlassAppearance.fromIntensity(1f), LiquidGlassAppearance.fromIntensity(10f))
    }
}
