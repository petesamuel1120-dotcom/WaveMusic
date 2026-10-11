package com.maxrave.simpmusic.expect.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp

/** Top display-corner radii reported by the platform, or zero when unavailable. */
@Composable
expect fun rememberTopDisplayCornerRadii(): Pair<Dp, Dp>
