package com.maxrave.simpmusic.expect.ui

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
actual fun platformDynamicColorScheme(isDark: Boolean): ColorScheme? = null

actual fun isWallpaperDynamicColorSupported(): Boolean = false

@Composable
actual fun SystemBarAppearanceEffect(
    isDark: Boolean,
    statusBarColor: Color,
    forceWhiteStatusBarIcons: Boolean,
) {
    // No system bars to adapt on desktop.
}
