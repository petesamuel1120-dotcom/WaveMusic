package com.petedev.wavemusic.ui.component

import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.petedev.wavemusic.domain.manager.DataStoreManager
import com.petedev.wavemusic.ui.theme.LocalReduceVisualEffects

val LocalAppBackgroundPreset = staticCompositionLocalOf { DataStoreManager.APP_BACKGROUND_AURORA }

/** Draws the shared aurora with a soft glow throughout the Home and Library pages. */
@Composable
fun Modifier.auroraGradientBackground(isLightTheme: Boolean, brightenTop: Boolean = true): Modifier {
    AppBackgroundPalette.colorFor(LocalAppBackgroundPreset.current, isLightTheme)?.let { color ->
        return background(color)
    }
    if (LocalReduceVisualEffects.current) {
        // One gradient preserves the aurora colors without seven full-screen translucent passes.
        return background(
            Brush.verticalGradient(
                if (isLightTheme) listOf(Color(0xFFF4F2FA), Color(0xFFEFF5F8))
                else listOf(Color(0xFF18152C), Color(0xFF101F25)),
            ),
        )
    }
    return drawWithCache {
        // White surfaces wash out low-opacity pigment, so light mode needs a clearer pastel glow.
        val strength = if (isLightTheme) 0.12f else 0.09f
        val topGlowHeight = size.height / 3f
        val topGlowColor = if (isLightTheme) Color.White else Color(0xFFB2BBDE)
        val topGlowStrength = if (isLightTheme) 0.04f else 0.08f
        val topGlow = Brush.verticalGradient(
            0f to topGlowColor.copy(alpha = topGlowStrength),
            0.35f to topGlowColor.copy(alpha = topGlowStrength * 0.65f),
            1f to topGlowColor.copy(alpha = 0f),
            startY = 0f,
            endY = topGlowHeight.coerceAtLeast(1f),
        )

        // Author the glows in portrait coordinates in both orientations. Using the landscape
        // width for circular radii made all six blooms overlap in the short viewport, increasing
        // brightness. Stretch this same portrait field to fit, preserving its relative shades.
        val gradientSize = Size(
            width = size.minDimension.coerceAtLeast(1f),
            height = size.maxDimension.coerceAtLeast(1f),
        )
        val scaleX = size.width / gradientSize.width
        val scaleY = size.height / gradientSize.height

        fun glow(color: Color, x: Float, y: Float, radius: Float, intensity: Float = 1f) =
            Brush.radialGradient(
                0f to color.copy(alpha = strength * intensity),
                0.48f to color.copy(alpha = strength * intensity * 0.38f),
                1f to Color.Transparent,
                center = Offset(gradientSize.width * x, gradientSize.height * y),
                radius = gradientSize.width * radius,
            )

        val violet = glow(Color(0xFF963DE0), 0.08f, 0.08f, 1.05f)
        val blue = glow(Color(0xFF2856E8), 0.77f, 0.10f, 1.00f)
        val teal = glow(Color(0xFF00A99D), 0.12f, 0.47f, 0.78f)
        val indigo = glow(Color(0xFF4B36D1), 0.78f, 0.43f, 0.80f)
        // The upper glows fade out before the bottom of a tall phone viewport. Carry the same
        // colors through the lower shelves with broad, quieter blooms instead of fading to black.
        val lowerTeal = glow(Color(0xFF00A99D), 0.14f, 0.86f, 1.12f, intensity = 0.70f)
        val lowerIndigo = glow(Color(0xFF4B36D1), 0.84f, 0.90f, 1.15f, intensity = 0.70f)

        onDrawBehind {
            withTransform({ scale(scaleX, scaleY, pivot = Offset.Zero) }) {
                drawRect(violet, size = gradientSize)
                drawRect(blue, size = gradientSize)
                drawRect(teal, size = gradientSize)
                drawRect(indigo, size = gradientSize)
                drawRect(lowerTeal, size = gradientSize)
                drawRect(lowerIndigo, size = gradientSize)
            }
            if (brightenTop) {
                // Use viewport height so the brighter upper third stays consistent in landscape.
                // The glow reaches transparency at its lower edge, avoiding a visible band.
                drawRect(topGlow, size = Size(size.width, topGlowHeight))
            }
        }
    }
}
