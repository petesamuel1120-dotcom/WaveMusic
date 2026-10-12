package com.petedev.wavemusic.expect.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.petedev.wavemusic.cast.CastIconButton
import com.petedev.wavemusic.cast.isCastAvailable

@Composable
actual fun PlatformCastButton(
    modifier: Modifier,
    tint: Color,
) {
    CastIconButton(modifier = modifier, tint = tint)
}

actual fun isPlatformCastAvailable(): Boolean = isCastAvailable()
