package com.maxrave.simpmusic.ui.component

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maxrave.simpmusic.expect.ui.PlatformBackdrop
import com.maxrave.simpmusic.ui.icon.ArrowBackIosNew
import com.maxrave.simpmusic.ui.icon.Close
import com.maxrave.simpmusic.ui.icon.MoreVert
import com.maxrave.simpmusic.ui.icon.Search
import com.maxrave.simpmusic.ui.icon.SimpIcons
import com.maxrave.simpmusic.ui.theme.typo
import dev.chrisbanes.haze.HazeState

@Composable
fun PlaylistPageHeader(
    title: String,
    showTitle: Boolean,
    hazeState: HazeState,
    backdrop: PlatformBackdrop,
    tint: Color,
    contentColor: Color,
    onBack: () -> Unit,
    onSearch: () -> Unit,
    onMore: () -> Unit,
    modifier: Modifier = Modifier,
    searchActive: Boolean = false,
    searchContent: (@Composable () -> Unit)? = null,
    searchVisible: Boolean = false,
    showSurface: Boolean = showTitle,
    stickyTitle: StickyPlaylistTitleState? = null,
    animateStickyTitle: Boolean = true,
    extraActions: @Composable RowScope.() -> Unit = {},
) {
    PageActionHeader(
        title = title,
        showTitle = showTitle,
        hazeState = hazeState,
        backdrop = backdrop,
        tint = tint,
        contentColor = contentColor,
        onBack = onBack,
        modifier = modifier,
        showSurface = showSurface,
        centerContent = searchContent,
        centerContentVisible = searchVisible,
        stickyTitle = stickyTitle,
        animateStickyTitle = animateStickyTitle,
    ) {
        extraActions()
        IconButton(onClick = onSearch, modifier = Modifier.size(headerGlassControlSize())) {
            Icon(
                imageVector = if (searchActive) SimpIcons.Close else SimpIcons.Search,
                contentDescription = if (searchActive) "Close playlist search" else "Search in playlist",
                tint = contentColor,
            )
        }
        IconButton(onClick = onMore, modifier = Modifier.size(headerGlassControlSize())) {
            Icon(
                imageVector = SimpIcons.MoreVert,
                contentDescription = "More",
                tint = contentColor,
            )
        }
    }
}

/** One viewport overlay: artwork and scrolled content share the same controls. */
@Composable
fun PageActionHeader(
    title: String,
    showTitle: Boolean,
    hazeState: HazeState,
    backdrop: PlatformBackdrop,
    tint: Color,
    contentColor: Color,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    showSurface: Boolean = showTitle,
    centerContent: (@Composable () -> Unit)? = null,
    centerContentVisible: Boolean = false,
    centerTitle: Boolean = false,
    stickyTitle: StickyPlaylistTitleState? = null,
    animateStickyTitle: Boolean = true,
    actions: @Composable RowScope.() -> Unit,
) {
    FrostedHeader(
        hazeState = hazeState,
        backdrop = backdrop,
        tint = tint,
        modifier = modifier.fillMaxWidth(),
        showSurface = showSurface,
        pinInLandscape = centerContentVisible,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
                    )
                    .height(CompactHeaderContentHeight)
                    .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LiquidGlassIconButton(
                backdrop = backdrop,
                imageVector = SimpIcons.ArrowBackIosNew,
                tint = contentColor,
                modifier = Modifier.size(headerGlassControlSize()),
                showFlatContainer = false,
                onClick = onBack,
            )
            Box(
                modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (stickyTitle != null && !centerContentVisible) {
                    stickyTitle.Target(title)
                }
                // A landscape hero keeps its title in the metadata column. Still measure
                // the target for the docking threshold, then render the ordinary toolbar title.
                if (stickyTitle == null || !animateStickyTitle || centerContentVisible) {
                    if (centerContent != null) {
                        AnimatedLandscapeHeaderSearch(
                            title = title,
                            searchVisible = centerContentVisible,
                            contentColor = contentColor,
                            searchField = centerContent,
                            titleVisible = showTitle,
                        )
                    } else {
                        androidx.compose.animation.AnimatedVisibility(
                            visible = showTitle,
                            enter = fadeIn(),
                            exit = fadeOut(),
                        ) {
                            Text(
                                text = title,
                                style = typo().titleMedium.let {
                                    if (isCompactLandscapeChrome()) it.copy(fontSize = 16.sp, lineHeight = 20.sp) else it
                                },
                                color = contentColor,
                                maxLines = 1,
                                textAlign = if (centerTitle) TextAlign.Center else TextAlign.Start,
                                overflow = if (centerTitle) TextOverflow.Ellipsis else TextOverflow.Clip,
                                modifier = if (centerTitle) Modifier.fillMaxWidth()
                                    else Modifier.fillMaxWidth().basicMarquee(iterations = Int.MAX_VALUE),
                            )
                        }
                    }
                }
            }
            LiquidGlassButtonGroup(
                backdrop = backdrop,
                modifier = Modifier.height(headerGlassControlSize()),
                verticalAlignment = Alignment.CenterVertically,
                showFlatContainer = false,
            ) {
                actions()
            }
        }
        if (stickyTitle != null) {
            with(stickyTitle) {
                Title(title, contentColor, visible = animateStickyTitle && !centerContentVisible)
            }
        }
    }
}
