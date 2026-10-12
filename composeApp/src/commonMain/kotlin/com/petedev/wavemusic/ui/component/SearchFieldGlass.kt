package com.petedev.wavemusic.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.petedev.wavemusic.expect.ui.PlatformBackdrop
import com.petedev.wavemusic.ui.theme.LocalAppIsDarkTheme
import com.petedev.wavemusic.ui.theme.LocalLiquidGlassAppearance
import com.petedev.wavemusic.ui.theme.LocalLiquidGlassEnabled

/** Keep the search field recognizable when the frosted end of the curve removes reflections. */
@Composable
internal fun Modifier.searchFieldGlass(backdrop: PlatformBackdrop): Modifier {
    if (!LocalLiquidGlassEnabled.current) return this
    val appearance = LocalLiquidGlassAppearance.current
    val visibility = (1f - appearance.reflectionMultiplier).coerceIn(0f, 1f)
    val glass = liquidGlass(backdrop, CircleShape, interactive = false)
    if (visibility == 0f) return glass
    val isDark = LocalAppIsDarkTheme.current
    val contrastColor = if (isDark) Color.White else Color.Black
    return glass
        .background(contrastColor.copy(alpha = visibility * if (isDark) 0.12f else 0.06f), CircleShape)
        .border(1.dp, contrastColor.copy(alpha = visibility * if (isDark) 0.18f else 0.14f), CircleShape)
}
