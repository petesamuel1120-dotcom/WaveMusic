package com.petedev.wavemusic.ui.component

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.petedev.wavemusic.extension.getScreenSizeInfo
import com.petedev.wavemusic.ui.theme.typo

/** Artwork and metadata share the viewport left above the measured player/navigation dock. */
data class OnlineLandscapeHeroLayout(
    val coverSize: Dp,
    val horizontalPadding: Dp,
    val columnSpacing: Dp,
    val topPadding: Dp,
    val bottomPadding: Dp,
    val titleStyle: TextStyle,
    val titleMaxLines: Int,
    val actionSpacing: Dp,
    val compact: Boolean,
)

/** Keep landscape playback controls compact beside the artwork and metadata. */
internal val LandscapeHeroActionMaxWidth = 280.dp

@Composable
fun onlineLandscapeHeroLayout(
    maxCoverSize: Dp = 280.dp,
    extraTopPadding: Dp = 0.dp,
): OnlineLandscapeHeroLayout {
    val screen = getScreenSizeInfo()
    val compact = isCompactLandscapeChrome() || screen.hDP < 500
    val horizontalPadding = pageContentHorizontalInset(if (compact) 20.dp else 32.dp)
    val columnSpacing = 24.dp
    val topPadding = WindowInsets.safeDrawing.only(WindowInsetsSides.Top)
        .asPaddingValues().calculateTopPadding() + CompactHeaderContentHeight +
        (if (compact) 4.dp else 24.dp) + extraTopPadding
    val bottomPadding = if (compact) 8.dp else 16.dp
    val availableHeight = (screen.hDP.dp - LocalPageBottomInset.current - topPadding - bottomPadding)
        .coerceAtLeast(0.dp)
    val density = LocalDensity.current
    val direction = LocalLayoutDirection.current
    val pageInsets = LocalPageContentInsets.current
    val horizontalInsets = with(density) {
        (pageInsets.getLeft(density, direction) + pageInsets.getRight(density, direction)).toDp()
    }
    val availableCoverWidth = screen.wDP.dp - horizontalInsets - horizontalPadding * 2 - columnSpacing - 220.dp
    // Let short windows use all the room above the dock, rather than a smaller phone cap.
    val coverSize = minOf(availableHeight, availableCoverWidth, maxCoverSize).coerceAtLeast(0.dp)
    val tight = coverSize < 152.dp
    return OnlineLandscapeHeroLayout(
        coverSize = coverSize,
        horizontalPadding = horizontalPadding,
        columnSpacing = columnSpacing,
        topPadding = topPadding,
        bottomPadding = bottomPadding,
        titleStyle = landscapeChromeTextStyle(when {
            tight -> typo().titleMedium.copy(fontSize = 18.sp, lineHeight = 22.sp)
            compact -> typo().titleLarge.copy(fontSize = 20.sp, lineHeight = 24.sp)
            else -> typo().headlineSmall
        }),
        titleMaxLines = if (tight) 1 else 2,
        actionSpacing = landscapeChromeSize(if (compact) 8.dp else 20.dp),
        compact = compact,
    )
}
