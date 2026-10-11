package com.maxrave.simpmusic.ui.component

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** Lets artwork-backed pages extend their ambient color behind app chrome. */
val LocalPageArtworkBackground = staticCompositionLocalOf<MutableState<Color?>?> { null }
