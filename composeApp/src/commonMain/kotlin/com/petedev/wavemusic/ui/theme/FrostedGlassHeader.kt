package com.petedev.wavemusic.ui.theme

import androidx.compose.runtime.compositionLocalOf
import com.petedev.wavemusic.domain.manager.FrostedGlassHeaderStyle

/** Style changes invalidate the material consumers without recapturing the scrolling page. */
val LocalFrostedGlassHeaderStyle = compositionLocalOf { FrostedGlassHeaderStyle.Default }
