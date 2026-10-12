package com.petedev.wavemusic.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/** Video feeds use true black in dark mode and the app surface in light mode. */
@Composable
fun videoPlaybackBackgroundColor(): Color {
    val appBackground = MaterialTheme.colorScheme.background
    return if (appBackground.luminance() > 0.5f) appBackground else Color.Black
}
