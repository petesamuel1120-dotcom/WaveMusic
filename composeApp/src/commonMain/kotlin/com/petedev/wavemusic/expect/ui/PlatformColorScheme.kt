package com.petedev.wavemusic.expect.ui

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Wallpaper-based (Material You) color scheme, or null when the platform can't provide one. */
@Composable
expect fun platformDynamicColorScheme(isDark: Boolean): ColorScheme?

/** Whether this platform can derive a color scheme from the system wallpaper. */
expect fun isWallpaperDynamicColorSupported(): Boolean

/** Keeps Android system bars in sync with the app theme; no-op on desktop. */
@Composable
expect fun SystemBarAppearanceEffect(
    isDark: Boolean,
    statusBarColor: Color,
    forceWhiteStatusBarIcons: Boolean = false,
)
