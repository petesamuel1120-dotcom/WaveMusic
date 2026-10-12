package com.petedev.wavemusic.ui.theme

import androidx.compose.runtime.Immutable
import com.petedev.wavemusic.domain.manager.LiquidGlassIntensity

/** A continuous material curve; the midpoint preserves the original effect stack. */
@Immutable
data class LiquidGlassAppearance(
    val blurMultiplier: Float,
    val lensHeightMultiplier: Float,
    val lensAmountMultiplier: Float,
    val tintMultiplier: Float,
    val reflectionMultiplier: Float,
    val motionMultiplier: Float,
) {
    fun scrimAlpha(alpha: Float, isDark: Boolean): Float {
        val scaled = alpha * tintMultiplier
        // Bright navigation capsules must retain contrast for their dark labels over artwork.
        val readable = if (!isDark && alpha >= 0.5f) maxOf(scaled, alpha * 0.9f) else scaled
        return readable.coerceIn(0f, 1f)
    }

    companion object {
        fun fromIntensity(intensity: Float): LiquidGlassAppearance {
            val value = LiquidGlassIntensity.normalize(intensity)
            fun curve(frosted: Float, liquid: Float): Float =
                if (value <= 0.5f) frosted + (1f - frosted) * value * 2f
                else 1f + (liquid - 1f) * (value - 0.5f) * 2f
            // Keep the frosted half of the slider from applying strong optical distortion to
            // fine text and image edges. Those details shimmer as they move behind a lens; blur
            // and tint still ramp linearly, while lens, reflection, and motion ease in gently.
            fun opticalCurve(frosted: Float, liquid: Float): Float =
                if (value <= 0.5f) {
                    val progress = value * 2f
                    frosted + (1f - frosted) * progress * progress
                } else {
                    1f + (liquid - 1f) * (value - 0.5f) * 2f
                }
            // The frosted/default range is diffusion only. Even a shallow lens moves bright
            // artwork edges across the rim as the page scrolls. Introduce refraction smoothly
            // above that range, preserving the midpoint and full-liquid material.
            fun refractionCurve(liquid: Float): Float =
                if (value <= 0.5f) {
                    val progress = ((value - LiquidGlassIntensity.Default) /
                        (0.5f - LiquidGlassIntensity.Default)).coerceIn(0f, 1f)
                    progress * progress
                } else {
                    1f + (liquid - 1f) * (value - 0.5f) * 2f
                }
            return LiquidGlassAppearance(
                blurMultiplier = curve(3f, 0.35f),
                lensHeightMultiplier = refractionCurve(1.35f),
                lensAmountMultiplier = refractionCurve(2.4f),
                tintMultiplier = curve(2f, 0.7f),
                reflectionMultiplier = opticalCurve(0f, 1.5f),
                motionMultiplier = opticalCurve(0.2f, 1.4f),
            )
        }
    }
}
