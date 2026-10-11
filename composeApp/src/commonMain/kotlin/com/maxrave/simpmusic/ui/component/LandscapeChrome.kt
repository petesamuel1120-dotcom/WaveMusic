package com.maxrave.simpmusic.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.maxrave.simpmusic.Platform
import com.maxrave.simpmusic.extension.getScreenSizeInfo
import com.maxrave.simpmusic.getPlatform

/** Use the full window dimensions so opening the keyboard cannot change orientation. */
@Composable
fun isCompactLandscapeChrome(): Boolean {
    if (getPlatform() != Platform.Android) return false
    val screen = getScreenSizeInfo()
    return screen.wDP > screen.hDP
}

@Composable
fun headerGlassControlSize(): Dp = if (isCompactLandscapeChrome()) 38.dp else 48.dp
