package com.maxrave.simpmusic.ui.component

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.maxrave.simpmusic.Platform
import com.maxrave.simpmusic.getPlatform

/** Shared horizontal edge used by the mini-player and page content. */
val AppContentHorizontalInset: Dp
    @Composable get() = pageContentHorizontalInset(12.dp)

/** Pages already reserve the camera's safe edge; landscape needs no second outer gutter. */
@Composable
fun pageContentHorizontalInset(portraitInset: Dp): Dp {
    val window = LocalWindowInfo.current.containerSize
    return if (getPlatform() == Platform.Android && window.width > window.height) 0.dp else portraitInset
}

/** Landscape cutout insets for page content whose background draws edge to edge. */
val LocalPageContentInsets = staticCompositionLocalOf { WindowInsets(0, 0, 0, 0) }

/** Mirror the camera's actual safe edge so landscape content stays centered in either rotation. */
@Composable
fun landscapeContentWindowInsets(): WindowInsets {
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val cutout = WindowInsets.displayCutout
    val edge = maxOf(cutout.getLeft(density, layoutDirection), cutout.getRight(density, layoutDirection))
    return WindowInsets(left = edge, right = edge)
}
