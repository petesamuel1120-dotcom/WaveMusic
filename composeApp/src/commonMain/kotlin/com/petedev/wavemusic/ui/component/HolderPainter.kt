package com.petedev.wavemusic.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import com.petedev.wavemusic.ui.theme.LocalForceDarkText
import com.petedev.wavemusic.ui.theme.LocalIsDarkTheme

/**
 * Plain neutral artwork placeholder for loading, missing and failed images.
 * Callers supply the size and shape for square covers, circular portraits and video thumbnails.
 */
@Composable
@Suppress("UNUSED_PARAMETER") // Keep the existing video call sites compatible; all shapes use the same fill.
fun rememberHolderPainter(isVideo: Boolean = false): Painter {
    val isDark = LocalForceDarkText.current || LocalIsDarkTheme.current
    val color = if (isDark) Color(0xFF242424) else Color(0xFFE6E6E6)
    return remember(color) { ColorPainter(color) }
}
