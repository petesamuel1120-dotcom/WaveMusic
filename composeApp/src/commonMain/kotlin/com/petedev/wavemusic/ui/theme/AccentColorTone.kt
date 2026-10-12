package com.petedev.wavemusic.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance

/** Tames highly saturated accent seeds on dark surfaces without changing the saved color. */
fun Color.mutedForDarkTheme(): Color {
    val neutral = luminance().let { Color(it, it, it, alpha) }
    val desaturated = lerp(this, neutral, 0.30f)
    return Color(
        red = desaturated.red * 0.92f,
        green = desaturated.green * 0.92f,
        blue = desaturated.blue * 0.92f,
        alpha = alpha,
    )
}
