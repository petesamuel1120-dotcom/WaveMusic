package com.petedev.wavemusic.ui.screen.other

import com.petedev.wavemusic.ui.component.rememberPageListState

import com.petedev.wavemusic.ui.component.pageContentHorizontalInset
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.petedev.wavemusic.ui.component.PageTopAppBar
import com.petedev.wavemusic.ui.component.pageHeaderContentHeight
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.petedev.wavemusic.domain.data.model.browse.album.Track
import com.petedev.wavemusic.domain.data.model.home.Content
import com.petedev.wavemusic.domain.utils.toSongEntity
import com.petedev.wavemusic.domain.utils.toTrack
import com.petedev.wavemusic.Platform
import com.petedev.wavemusic.getPlatform
import com.petedev.wavemusic.ui.component.auroraGradientBackground
import com.petedev.wavemusic.ui.component.CenterLoadingBox
import com.petedev.wavemusic.ui.component.EndOfPage
import com.petedev.wavemusic.ui.component.FrostedHeader
import com.petedev.wavemusic.ui.component.LocalSharedHeaderHosted
import com.petedev.wavemusic.ui.component.LocalSharedHeaderState
import com.petedev.wavemusic.ui.component.HeaderScrollOffsetTracker
import com.petedev.wavemusic.ui.component.frostedHeaderSource
import com.petedev.wavemusic.expect.ui.rememberBackdrop
import com.petedev.wavemusic.ui.theme.LocalLiquidGlassEnabled
import com.petedev.wavemusic.ui.component.HomeContentCard
import com.petedev.wavemusic.ui.component.MoodMomentAndGenreHomeItem
import com.petedev.wavemusic.ui.component.NowPlayingBottomSheet
import com.petedev.wavemusic.ui.component.RippleIconButton
import com.petedev.wavemusic.ui.component.isListenAgainSong
import com.petedev.wavemusic.ui.component.homeCardAspectRatio
import com.petedev.wavemusic.ui.icon.ArrowBackIosNew
import com.petedev.wavemusic.ui.icon.WaveIcons
import com.petedev.wavemusic.ui.navigation.destination.home.MoodDestination
import com.petedev.wavemusic.ui.theme.desktopPanelDark
import com.petedev.wavemusic.ui.theme.typo
import com.petedev.wavemusic.viewModel.BrowseUIState
import com.petedev.wavemusic.viewModel.BrowseViewModel
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import org.koin.compose.viewmodel.koinViewModel

private val RowGap = 10.dp

// The mini-player's visible surface has a slightly wider visual edge than the page gutter.
private val BrowseRowSideInset = 4.dp

// The thumbnail height a justified row aims for; rows land a little above or below it.
private val TargetThumbnailHeight = 180.dp

// HomeContentCard uses the shared 12dp inset on both sides when filling a grid cell.
private val CardHorizontalPadding = 24.dp

// The width a mood button asks for, as the old grid's cells did.
private val MoodCellWidth = 170.dp

/**
 * The page a home section's "More" endpoint opens when YouTube names no album, playlist, artist or
 * podcast behind it. Every shelf is flattened into justified rows — each card keeps its own shape
 * (16:9 video, square otherwise), every thumbnail in a row is the same height, and the number of
 * cards per row follows the shapes in it — drawn with Home's own cards, so an item opens and plays
 * exactly as it does on Home. It uses the app-wide theme-based aurora backdrop.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowseScreen(
    innerPadding: PaddingValues,
    navController: NavController,
    browseId: String,
    params: String?,
    title: String?,
    viewModel: BrowseViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isListenAgainMorePage = title?.trim()?.equals("Listen again", ignoreCase = true) == true
    val headerHazeState = rememberHazeState()
    val listState = rememberPageListState()
    val density = LocalDensity.current
    val initialHeaderHeight = innerPadding.calculateTopPadding() + pageHeaderContentHeight()
    var topAppBarHeight by remember(initialHeaderHeight) { mutableStateOf(initialHeaderHeight) }
    val topAppBarHeightPx = with(density) { topAppBarHeight.toPx() }
    val sharedHeaderState = LocalSharedHeaderState.current
    val previousHeaderOffset = remember(sharedHeaderState) { sharedHeaderState?.headerOffsetPx ?: 0f }
    val headerOffsetTracker = remember(listState) { HeaderScrollOffsetTracker() }
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val pageEntry = LocalLifecycleOwner.current as? NavBackStackEntry
    val isCurrentPage = currentBackStackEntry?.id == pageEntry?.id
    var track by remember { mutableStateOf<Track?>(null) }
    var bottomSheetShow by remember { mutableStateOf(false) }

    DisposableEffect(sharedHeaderState) {
        onDispose { sharedHeaderState?.updateHeaderOffset(previousHeaderOffset) }
    }
    LaunchedEffect(listState, sharedHeaderState, topAppBarHeightPx, isCurrentPage) {
        val headerState = sharedHeaderState ?: return@LaunchedEffect
        if (!isCurrentPage) return@LaunchedEffect
        snapshotFlow {
            listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset
        }.collect { (index, offset) ->
            val newOffset = headerOffsetTracker.update(
                index = index,
                offset = offset,
                currentHeaderOffsetPx = headerState.headerOffsetPx,
                maxHeightPx = topAppBarHeightPx,
            )
            headerState.updateHeaderOffset(newOffset)
        }
    }

    // Match the theme-based aurora used by Home and Library. Browse destinations paint their own
    // opaque page background, so they cannot rely on the app-wide aurora behind the NavHost.
    val backgroundColor = MaterialTheme.colorScheme.background
    val isLightTheme = backgroundColor.luminance() > 0.5f
    val pageBackground =
        if (getPlatform() == Platform.Desktop) {
            if (isLightTheme) MaterialTheme.colorScheme.surfaceContainer else desktopPanelDark
        } else {
            backgroundColor
        }
    LaunchedEffect(browseId, params) {
        viewModel.getBrowsePage(browseId, params)
    }
    val frostedBackdrop = rememberBackdrop(pageBackground)

    if (bottomSheetShow) {
        NowPlayingBottomSheet(
            onDismiss = { bottomSheetShow = false },
            song = track?.toSongEntity(),
            navController = navController,
        )
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(pageBackground)
            .auroraGradientBackground(isLightTheme),
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .frostedHeaderSource(frostedBackdrop)
                .then(
                    if (LocalLiquidGlassEnabled.current) {
                        Modifier.background(pageBackground).auroraGradientBackground(isLightTheme)
                    } else {
                        Modifier
                    },
                )
                .hazeSource(headerHazeState),
        ) {
            Crossfade(targetState = uiState) { state ->
                when (state) {
                    is BrowseUIState.Success -> {
                        BoxWithConstraints(Modifier.fillMaxSize()) {
                            val rowSideInset = pageContentHorizontalInset(BrowseRowSideInset)
                            val cardContentWidth = maxWidth - rowSideInset * 2f
                            val visibleContents =
                                remember(state.page.contents, isListenAgainMorePage) {
                                    if (isListenAgainMorePage) {
                                        state.page.contents.filter(Content::isListenAgainSong)
                                    } else {
                                        state.page.contents
                                    }
                                }
                            val cardRows =
                                remember(visibleContents, cardContentWidth) {
                                    justifyRows(visibleContents, cardContentWidth)
                                }
                            val moodColumns =
                                maxOf(
                                    1,
                                    ((cardContentWidth + RowGap) / (MoodCellWidth + RowGap)).toInt(),
                                )
                            val moodRows =
                                remember(state.page.moods, moodColumns) {
                                    state.page.moods.chunked(moodColumns)
                                }
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                // Padding rather than a spacer item, so the first item is a real row and
                                // the glow hands off once a row has scrolled by, as on Mix for you.
                                contentPadding = PaddingValues(top = topAppBarHeight),
                                verticalArrangement = Arrangement.spacedBy(if (isListenAgainMorePage) 4.dp else RowGap),
                            ) {
                                items(moodRows) { row ->
                                    Row(
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = rowSideInset),
                                        horizontalArrangement = Arrangement.spacedBy(RowGap),
                                    ) {
                                        row.forEach { mood ->
                                            Box(Modifier.weight(1f)) {
                                                MoodMomentAndGenreHomeItem(
                                                    title = mood.title,
                                                    stripeColor = mood.stripeColor,
                                                    fillMaxWidth = true,
                                                ) {
                                                    navController.navigate(MoodDestination(mood.params))
                                                }
                                            }
                                        }
                                        repeat(moodColumns - row.size) {
                                            Spacer(Modifier.weight(1f))
                                        }
                                    }
                                }
                                items(cardRows) { row ->
                                    Row(
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = rowSideInset),
                                        horizontalArrangement = Arrangement.spacedBy(RowGap),
                                    ) {
                                        row.forEach { card ->
                                            Box(Modifier.width(card.width)) {
                                                HomeContentCard(
                                                    temp = card.content,
                                                    navController = navController,
                                                    viewModel = viewModel,
                                                    onLongClick = {
                                                        track = it.toTrack()
                                                        bottomSheetShow = true
                                                    },
                                                    fillMaxWidth = true,
                                                    compactTrackLayout = isListenAgainMorePage,
                                                )
                                            }
                                        }
                                    }
                                }
                                item {
                                    EndOfPage()
                                }
                            }
                        }
                    }

                    is BrowseUIState.Error -> {
                        LaunchedEffect(state) {
                            viewModel.makeToast(state.message)
                        }
                    }

                    BrowseUIState.Loading -> {
                        CenterLoadingBox(
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .padding(15.dp),
                        )
                    }
                }
            }
        }
        FrostedHeader(
            hazeState = headerHazeState,
            backdrop = frostedBackdrop,
            shareHeader = true,
            tint = pageBackground,
            modifier = Modifier.fillMaxWidth().onGloballyPositioned { coordinates ->
                topAppBarHeight = with(density) { coordinates.size.height.toDp() }
            },
        ) {
            PageTopAppBar(
                    title = {
                        Text(
                            text = (uiState as? BrowseUIState.Success)?.page?.title ?: title.orEmpty(),
                            style = typo().titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier =
                                if (LocalLiquidGlassEnabled.current && !LocalSharedHeaderHosted.current) Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight(
                                        align = Alignment.CenterVertically,
                                    ).basicMarquee(
                                        iterations = Int.MAX_VALUE,
                                        animationMode = MarqueeAnimationMode.Immediately,
                                    ).focusable()
                                else Modifier,
                        )
                    },
                    navigationIcon = {
                        Box(Modifier.padding(horizontal = 5.dp)) {
                            RippleIconButton(
                                WaveIcons.ArrowBackIosNew,
                                Modifier
                                    .size(com.petedev.wavemusic.ui.component.landscapeChromeSize(32.dp)),
                                true,
                                tint = MaterialTheme.colorScheme.onSurface,
                            ) {
                                navController.navigateUp()
                            }
                        }
                    },
                    colors =
                        TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                        ),
            )
        }
    }
}

private data class JustifiedCard(
    val content: Content,
    val width: Dp,
)

/**
 * Packs [contents] into rows that fill [rowWidth] exactly, each card keeping its own thumbnail shape,
 * so every thumbnail in a row is the same height and the number of cards per row follows the shapes
 * in it. A row closes when the next card would take its height below [TargetThumbnailHeight] —
 * with or without that card, whichever lands closer. The last row keeps the target height instead
 * of stretching to fill.
 */
// ponytail: greedy one-pass line breaking; a globally balanced break (Knuth–Plass style) only if
// uneven row heights ever show.
private fun justifyRows(
    contents: List<Content>,
    rowWidth: Dp,
): List<List<JustifiedCard>> {
    val target = TargetThumbnailHeight.value
    val rows = mutableListOf<List<JustifiedCard>>()
    var current = emptyList<Pair<Content, Float>>()

    // The thumbnail height at which these cards exactly fill the row.
    fun heightOf(row: List<Pair<Content, Float>>): Float {
        val fixed = CardHorizontalPadding.value * row.size + RowGap.value * (row.size - 1)
        return ((rowWidth.value - fixed) / row.sumOf { it.second.toDouble() }.toFloat()).coerceAtLeast(1f)
    }

    fun close(
        row: List<Pair<Content, Float>>,
        height: Float,
    ) {
        rows.add(row.map { (content, ratio) -> JustifiedCard(content, (CardHorizontalPadding.value + ratio * height).dp) })
    }

    for (content in contents) {
        val candidate = current + (content to content.homeCardAspectRatio())
        val height = heightOf(candidate)
        when {
            height > target -> {
                current = candidate
            }

            current.isNotEmpty() && heightOf(current) - target < target - height -> {
                close(current, heightOf(current))
                current = listOf(candidate.last())
            }

            else -> {
                close(candidate, height)
                current = emptyList()
            }
        }
    }
    if (current.isNotEmpty()) close(current, minOf(target, heightOf(current)))
    return rows
}
