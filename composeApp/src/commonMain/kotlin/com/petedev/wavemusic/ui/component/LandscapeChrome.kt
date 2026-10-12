package com.petedev.wavemusic.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.dp
import com.petedev.wavemusic.Platform
import com.petedev.wavemusic.extension.getScreenSizeInfo
import com.petedev.wavemusic.getPlatform

/** Use the full window dimensions so opening the keyboard cannot change orientation. */
@Composable
fun isCompactLandscapeChrome(): Boolean {
    if (getPlatform() != Platform.Android) return false
    val screen = getScreenSizeInfo()
    return screen.wDP > screen.hDP
}

private const val LandscapeChromeScale = 1.2f
// PageTopAppBar scales its title slot through density; nested sizing helpers must
// recognize that scale so two-line titles keep their full subtitle height.
private val LocalLandscapeChromeScaledDensity = staticCompositionLocalOf { false }

@Composable
fun landscapeChromeSize(size: Dp): Dp =
    if (isCompactLandscapeChrome() && !LocalLandscapeChromeScaledDensity.current) size * LandscapeChromeScale else size

@Composable
fun landscapeChromeTextStyle(style: TextStyle): TextStyle {
    if (!isCompactLandscapeChrome() || LocalLandscapeChromeScaledDensity.current) return style
    return style.copy(
        fontSize = if (style.fontSize.isSpecified) style.fontSize * LandscapeChromeScale else style.fontSize,
        lineHeight = if (style.lineHeight.isSpecified) style.lineHeight * LandscapeChromeScale else style.lineHeight,
    )
}

/** Scale all visual dimensions and text consistently inside a compact landscape header title. */
@Composable
fun LandscapeChromeScaledContent(content: @Composable () -> Unit) {
    val density = LocalDensity.current
    if (!isCompactLandscapeChrome() || LocalLandscapeChromeScaledDensity.current) {
        content()
    } else {
        CompositionLocalProvider(
            LocalLandscapeChromeScaledDensity provides true,
            LocalDensity provides Density(
                density = density.density * LandscapeChromeScale,
                fontScale = density.fontScale,
            ),
            content = content,
        )
    }
}

@Composable
fun headerGlassControlSize(): Dp = landscapeChromeSize(if (isCompactLandscapeChrome()) 38.dp else 48.dp)

@Composable
fun headerGlassIconSize(): Dp = landscapeChromeSize(if (isCompactLandscapeChrome()) 22.dp else 24.dp)
