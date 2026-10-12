package com.petedev.wavemusic.ui.component

import org.jetbrains.compose.resources.stringResource
import com.petedev.wavemusic.resources.more
import com.petedev.wavemusic.resources.Res

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.lazy.LazyListState
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
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.petedev.wavemusic.expect.ui.PlatformBackdrop
import com.petedev.wavemusic.ui.icon.ArrowBackIosNew
import com.petedev.wavemusic.ui.icon.Close
import com.petedev.wavemusic.ui.icon.MoreVert
import com.petedev.wavemusic.ui.icon.Search
import com.petedev.wavemusic.ui.icon.WaveIcons
import com.petedev.wavemusic.ui.theme.LocalLiquidGlassEnabled
import com.petedev.wavemusic.ui.theme.typo
import dev.chrisbanes.haze.HazeState

/** Keep the requested extra 15dp consistent on online and local playlist pages. */
val PlaylistHeaderTitleClearance: Dp = 56.dp * 1.2f + 15.dp

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
    selectionContent: (@Composable () -> Unit)? = null,
    selectionActive: Boolean = false,
    showSurface: Boolean = showTitle,
    stickyTitle: StickyPlaylistTitleState? = null,
    scrollState: LazyListState? = null,
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
        // Flat playlist chrome must stay readable over artwork, even before the title docks.
        showSurface = showSurface || !LocalLiquidGlassEnabled.current,
        centerContent = searchContent,
        centerContentVisible = searchVisible,
        selectionContent = selectionContent,
        selectionActive = selectionActive,
        stickyTitle = stickyTitle,
        scrollState = scrollState,
        animateStickyTitle = animateStickyTitle,
        hideAfterDock = 240.dp,
    ) {
        extraActions()
        IconButton(onClick = onSearch, modifier = Modifier.size(headerGlassControlSize())) {
            Icon(
                imageVector = if (searchActive) WaveIcons.Close else WaveIcons.Search,
                contentDescription = if (searchActive) "Close playlist search" else "Search in playlist",
                tint = contentColor,
                modifier = Modifier.size(headerGlassIconSize()),
            )
        }
        IconButton(onClick = onMore, modifier = Modifier.size(headerGlassControlSize())) {
            Icon(
                imageVector = WaveIcons.MoreVert,
                contentDescription = stringResource(Res.string.more),
                tint = contentColor,
                modifier = Modifier.size(headerGlassIconSize()),
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
    selectionContent: (@Composable () -> Unit)? = null,
    selectionActive: Boolean = false,
    centerTitle: Boolean = false,
    stickyTitle: StickyPlaylistTitleState? = null,
    scrollState: LazyListState? = null,
    animateStickyTitle: Boolean = true,
    hideAfterDock: Dp = 180.dp,
    actions: @Composable RowScope.() -> Unit,
) {
    val density = LocalDensity.current
    val liquidGlassEnabled = LocalLiquidGlassEnabled.current
    val headerTitleVisible = showTitle &&
        (liquidGlassEnabled || stickyTitle == null || stickyTitle.isSourceOutOfViewport)
    val sharedHeaderState = LocalSharedHeaderState.current
    val pageTitleTransitionAlpha by animateFloatAsState(
        targetValue = if (sharedHeaderState?.pageTransitionInProgress == true) 0f else 1f,
        animationSpec = tween(SLIDE_V2_FADE_OUT_MILLIS, easing = FastOutSlowInEasing),
        label = "pageTitleTransitionAlpha",
    )
    val useStickyTitleHandoff = LocalLiquidGlassEnabled.current && animateStickyTitle && stickyTitle != null
    val pinHeaderButtons = selectionActive || LocalSharedHeaderState.current?.buttonsPinned == true
    val fixedGlassBackdrop = pinHeaderButtons && LocalLiquidGlassEnabled.current
    val fixedBackdropHeight = with(density) { WindowInsets.safeDrawing.getTop(this).toDp() } +
        CompactHeaderContentHeight + HeaderBackdropExtensionHeight
    val hideAfterDockPx = with(density) { hideAfterDock.toPx() }
    var headerHeightPx by remember { mutableFloatStateOf(0f) }
    val maxOffsetPx = if (headerHeightPx > 0f) headerHeightPx else with(density) { 150.dp.toPx() }

    var headerOffsetPx by remember(stickyTitle) { mutableFloatStateOf(0f) }
    val offsetTracker = remember(stickyTitle, pinHeaderButtons) { HeaderScrollOffsetTracker() }

    LaunchedEffect(stickyTitle, scrollState, if (liquidGlassEnabled) showTitle else false, centerContentVisible, pinHeaderButtons, liquidGlassEnabled) {
        val titleState = stickyTitle
        val flatList = scrollState ?: titleState?.list
        if (pinHeaderButtons || (liquidGlassEnabled && titleState == null) || (!liquidGlassEnabled && flatList == null) || centerContentVisible) {
            headerOffsetPx = 0f
            if (!liquidGlassEnabled) sharedHeaderState?.resetHeaderOffset()
        } else if (!liquidGlassEnabled) {
            val flatOffsetTracker = HeaderScrollOffsetTracker()
            sharedHeaderState?.updateHeaderOffset(headerOffsetPx)
            snapshotFlow {
                flatList!!.firstVisibleItemIndex to flatList.firstVisibleItemScrollOffset
            }.collect { (index, offset) ->
                headerOffsetPx = flatOffsetTracker.update(
                    index = index,
                    offset = offset,
                    currentHeaderOffsetPx = headerOffsetPx,
                    maxHeightPx = maxOffsetPx,
                )
                sharedHeaderState?.updateHeaderOffset(headerOffsetPx)
            }
        } else {
            snapshotFlow {
                val rawDistance = titleState!!.distancePastDockPx
                val untranslatedDistance = rawDistance - headerOffsetPx
                Triple(
                    titleState.list.firstVisibleItemIndex,
                    titleState.list.firstVisibleItemScrollOffset,
                    untranslatedDistance,
                )
            }.collect { (index, offset, distancePastDock) ->
                if (!showTitle) {
                    headerOffsetPx = 0f
                } else if (distancePastDock < hideAfterDockPx) {
                    headerOffsetPx = 0f
                    offsetTracker.update(index, offset, 0f, maxOffsetPx, forceShowAtTop = false)
                } else {
                    headerOffsetPx = offsetTracker.update(
                        index = index,
                        offset = offset,
                        currentHeaderOffsetPx = headerOffsetPx,
                        maxHeightPx = maxOffsetPx,
                        forceShowAtTop = false,
                    )
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .onSizeChanged { headerHeightPx = it.height.toFloat() }
            .graphicsLayer {
                translationY = if (pinHeaderButtons || (liquidGlassEnabled && !showTitle && stickyTitle != null && !centerContentVisible)) 0f else headerOffsetPx
            },
    ) {
        FrostedHeader(
            hazeState = hazeState,
            backdrop = backdrop,
            tint = tint,
            modifier = Modifier.fillMaxWidth(),
            shareHeader = true,
            showSurface = selectionActive || (!fixedGlassBackdrop &&
                (showSurface || (pinHeaderButtons && !LocalLiquidGlassEnabled.current))),
        ) {
            val headerContentVisible = LocalSharedHeaderContentVisible.current
            if (selectionActive && selectionContent != null) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    selectionContent()
                }
            } else {
                CompositionLocalProvider(
                    LocalMinimumInteractiveComponentSize provides Dp.Unspecified,
                    LocalHeaderControlSize provides headerGlassControlSize(),
                    LocalHeaderIconSize provides headerGlassIconSize(),
                ) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .windowInsetsPadding(
                                WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
                            )
                            .height(CompactHeaderContentHeight)
                            .padding(horizontal = AppContentHorizontalInset),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LiquidGlassIconButton(
                        backdrop = backdrop,
                        imageVector = WaveIcons.ArrowBackIosNew,
                        tint = contentColor,
                        modifier = Modifier.size(headerGlassControlSize()).sharedHeaderControlsMorph(SharedHeaderControl.Navigation),
                        showFlatContainer = false,
                        onClick = onBack,
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp)
                            .graphicsLayer { alpha = if (headerContentVisible) pageTitleTransitionAlpha else 0f },
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        if (stickyTitle != null && !centerContentVisible) {
                            stickyTitle.Target(
                                title,
                                textAlign = if (centerTitle) TextAlign.Center else TextAlign.Start,
                                style = landscapeChromeTextStyle(
                                    typo().titleMedium.let {
                                        if (isCompactLandscapeChrome()) it.copy(fontSize = 16.sp, lineHeight = 20.sp) else it
                                    },
                                ),
                            )
                        }
                        if (!useStickyTitleHandoff || centerContentVisible) {
                            if (centerContent != null) {
                                AnimatedLandscapeHeaderSearch(
                                    title = title,
                                    searchVisible = centerContentVisible,
                                    contentColor = contentColor,
                                    searchField = centerContent,
                                    titleVisible = headerTitleVisible,
                                )
                            } else {
                                if (headerTitleVisible) {
                                    Text(
                                        text = title,
                                        style = landscapeChromeTextStyle(
                                            typo().titleMedium.let {
                                                if (isCompactLandscapeChrome()) it.copy(fontSize = 16.sp, lineHeight = 20.sp) else it
                                            },
                                        ),
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
                        modifier = Modifier.height(headerGlassControlSize()).sharedHeaderControlsMorph(SharedHeaderControl.Actions),
                        verticalAlignment = Alignment.CenterVertically,
                        showFlatContainer = false,
                    ) {
                        actions()
                    }
                }
                }
                if (stickyTitle != null) {
                    with(stickyTitle) {
                        Title(
                            title,
                            contentColor,
                            visible = useStickyTitleHandoff && !centerContentVisible && headerContentVisible,
                            transitionAlpha = pageTitleTransitionAlpha,
                        )
                    }
                }
            }
        }
    }
}
