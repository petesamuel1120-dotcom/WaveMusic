package com.maxrave.simpmusic.ui.screen.library

import simpmusic.composeapp.generated.resources.download
import simpmusic.composeapp.generated.resources.pause_action
import simpmusic.composeapp.generated.resources.play_action
import simpmusic.composeapp.generated.resources.shuffle
import simpmusic.composeapp.generated.resources.sort_playlist

import com.maxrave.simpmusic.ui.component.pageContentHorizontalInset
import com.maxrave.simpmusic.ui.component.AmoledAlertDialog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import com.maxrave.simpmusic.ui.component.DownloadingIndicator
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import com.maxrave.simpmusic.ui.component.DialogTextButton as TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.kmpalette.rememberPaletteState
import com.maxrave.common.LOCAL_PLAYLIST_ID
import com.maxrave.domain.data.entities.DownloadState
import com.maxrave.domain.data.entities.LocalPlaylistEntity
import com.maxrave.domain.data.entities.PairSongLocalPlaylist
import com.maxrave.domain.data.entities.SongEntity
import com.maxrave.domain.utils.FilterState
import com.maxrave.domain.utils.toTrack
import com.maxrave.logger.Logger
import com.maxrave.simpmusic.Platform
import com.maxrave.simpmusic.expect.ui.layerBackdrop
import com.maxrave.simpmusic.expect.ui.rememberBackdrop
import com.maxrave.simpmusic.expect.ui.toImageBitmap
import com.maxrave.simpmusic.extension.artworkScrimBrush
import com.maxrave.simpmusic.extension.displayNameRes
import com.maxrave.simpmusic.extension.getScreenSizeInfo
import com.maxrave.simpmusic.extension.toImmersiveBackground
import com.maxrave.simpmusic.getPlatform
import com.maxrave.simpmusic.ui.component.SearchBarExit
import com.maxrave.simpmusic.ui.component.SearchBarEnter
import com.maxrave.simpmusic.ui.component.AddToPlaylistModalBottomSheet
import com.maxrave.simpmusic.ui.component.CenterLoadingBox
import com.maxrave.simpmusic.ui.component.DraggableItem
import com.maxrave.simpmusic.ui.component.EndOfPage
import com.maxrave.simpmusic.ui.component.pageEndSpacing
import com.maxrave.simpmusic.ui.component.PlaylistPageHeader
import com.maxrave.simpmusic.ui.component.LocalPlaylistArtworkPlaceholder
import com.maxrave.simpmusic.ui.component.LocalPlaylistHeroArtwork
import com.maxrave.simpmusic.ui.component.LoadingDialog
import com.maxrave.simpmusic.ui.component.LocalPlaylistBottomSheet
import com.maxrave.simpmusic.ui.component.NowPlayingBottomSheet
import com.maxrave.simpmusic.ui.component.InPageSearchBarHeight
import com.maxrave.simpmusic.ui.component.PlaylistSearchBar
import com.maxrave.simpmusic.ui.component.isCompactLandscapeChrome
import com.maxrave.simpmusic.ui.component.onlineLandscapeHeroLayout
import com.maxrave.simpmusic.ui.component.rememberPlaylistCoverRequest
import com.maxrave.simpmusic.ui.component.SharedHeaderContentHeight
import com.maxrave.simpmusic.ui.component.SongFullWidthItems
import com.maxrave.simpmusic.ui.component.SearchBarBackHandler
import com.maxrave.simpmusic.ui.component.SearchBarScrollDismissEffect
import com.maxrave.simpmusic.ui.component.selection.SelectedSongsBottomSheet
import com.maxrave.simpmusic.ui.component.selection.SongSelectionAction
import com.maxrave.simpmusic.ui.component.selection.SongSelectionTopAppBar
import com.maxrave.simpmusic.ui.component.frostedHeaderSource
import com.maxrave.simpmusic.ui.component.selection.rememberSongSelectionState
import com.maxrave.simpmusic.ui.component.SortPlaylistBottomSheet
import com.maxrave.simpmusic.ui.component.SuggestItems
import com.maxrave.simpmusic.ui.component.painterPlaylistThumbnail
import com.maxrave.simpmusic.ui.component.playlistTitleGradient
import com.maxrave.simpmusic.ui.component.rememberDragDropState
import com.maxrave.simpmusic.ui.icon.Delete
import com.maxrave.simpmusic.ui.icon.DownloadForOffline
import com.maxrave.simpmusic.ui.icon.Pause
import com.maxrave.simpmusic.ui.icon.PlayArrow
import com.maxrave.simpmusic.ui.icon.Shuffle
import com.maxrave.simpmusic.ui.icon.SimpIcons
import com.maxrave.simpmusic.ui.icon.Sort
import com.maxrave.simpmusic.ui.theme.LocalAppColorScheme
import com.maxrave.simpmusic.ui.theme.LocalAppIsDarkTheme
import com.maxrave.simpmusic.ui.theme.seed
import com.maxrave.simpmusic.ui.theme.typo
import com.maxrave.simpmusic.viewModel.LocalPlaylistUIEvent
import com.maxrave.simpmusic.viewModel.LocalPlaylistViewModel
import com.maxrave.simpmusic.viewModel.SharedViewModel
import com.maxrave.simpmusic.viewModel.SongSelectionViewModel
import com.maxrave.simpmusic.viewModel.UIEvent
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import simpmusic.composeapp.generated.resources.Res
import simpmusic.composeapp.generated.resources.album_length
import simpmusic.composeapp.generated.resources.baseline_downloaded
import simpmusic.composeapp.generated.resources.cancel
import simpmusic.composeapp.generated.resources.created_at
import simpmusic.composeapp.generated.resources.downloaded
import simpmusic.composeapp.generated.resources.downloading
import simpmusic.composeapp.generated.resources.reload
import simpmusic.composeapp.generated.resources.remove_from_playlist
import simpmusic.composeapp.generated.resources.sort_by
import simpmusic.composeapp.generated.resources.suggest
import simpmusic.composeapp.generated.resources.suggestions
import simpmusic.composeapp.generated.resources.sync_playlist_warning
import simpmusic.composeapp.generated.resources.unsync_playlist_warning
import simpmusic.composeapp.generated.resources.warning
import simpmusic.composeapp.generated.resources.yes
import simpmusic.composeapp.generated.resources.your_playlist

private const val TAG = "LocalPlaylistScreen"

@ExperimentalFoundationApi
@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalCoroutinesApi::class,
)
@Composable
fun LocalPlaylistScreen(
    id: Long,
    sharedViewModel: SharedViewModel = koinInject(),
    viewModel: LocalPlaylistViewModel = koinViewModel(),
    navController: NavController,
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val infiniteTransition = rememberInfiniteTransition(label = "rotation")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(5000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
        label = "rotation",
    )

    val lazyState = rememberLazyListState()
    val firstItemVisible by remember {
        derivedStateOf {
            lazyState.firstVisibleItemIndex == 0
        }
    }
    val downloadState by viewModel.uiState.map { it.downloadState }.collectAsState(
        initial = DownloadState.STATE_NOT_DOWNLOADED,
    )
    var shouldHideTopBar by rememberSaveable { mutableStateOf(false) }
    val selectionState = rememberSongSelectionState()
    val selectionViewModel: SongSelectionViewModel = koinViewModel()
    var showSelectionSheet by rememberSaveable { mutableStateOf(false) }
    var showSelectionAddToPlaylist by rememberSaveable { mutableStateOf(false) }
    val shouldShowSuggestions =
        !uiState.ytPlaylistId.isNullOrEmpty() &&
            uiState.syncState == LocalPlaylistEntity.YouTubeSyncState.Synced

    val playingTrack by sharedViewModel.nowPlayingState
        .mapLatest {
            it?.songEntity
        }.collectAsState(initial = null)
    val isPlaying by sharedViewModel.controllerState.map { it.isPlaying }.collectAsState(initial = false)

    val queueData by sharedViewModel.getQueueDataState().collectAsStateWithLifecycle()
    val playingPlaylistId by remember {
        derivedStateOf {
            queueData?.data?.playlistId
        }
    }

    val suggestedTracks by viewModel.uiState.map { it.suggestions?.songs ?: emptyList() }.collectAsState(initial = emptyList())
    val suggestedVideoIds = remember(suggestedTracks) { suggestedTracks.mapTo(mutableSetOf()) { it.videoId } }
    val suggestionsLoading by viewModel.loading.collectAsStateWithLifecycle()
    var showSyncAlertDialog by rememberSaveable { mutableStateOf(false) }
    var showUnsyncAlertDialog by rememberSaveable { mutableStateOf(false) }
    var firstTimeGetLocalPlaylist by rememberSaveable {
        mutableStateOf(false)
    }

    var currentItem by remember {
        mutableStateOf<SongEntity?>(null)
    }

    var itemBottomSheetShow by remember {
        mutableStateOf(false)
    }
    var playlistBottomSheetShow by remember {
        mutableStateOf(false)
    }

    var sortBottomSheetShow by remember {
        mutableStateOf(false)
    }

    var changingOrder by remember {
        mutableStateOf(false)
    }

    val trackPagingItems: LazyPagingItems<Pair<SongEntity, PairSongLocalPlaylist>> = viewModel.tracksPagingState.collectAsLazyPagingItems()
    LaunchedEffect(Unit) {
        snapshotFlow {
            trackPagingItems.loadState
        }.collectLatest {
            Logger.d("PlaylistScreen", "loadState: ${trackPagingItems.loadState}")
            viewModel.setLazyTrackPagingItems(trackPagingItems)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            firstTimeGetLocalPlaylist = true
        }
    }

    val onPlaylistItemClick: (videoId: String) -> Unit = { videoId ->
        viewModel.onUIEvent(
            LocalPlaylistUIEvent.ItemClick(
                videoId = videoId,
            ),
        )
    }
    val onItemMoreClick: (videoId: String) -> Unit = { videoId ->
        currentItem =
            if (videoId in suggestedVideoIds) {
                null
            } else {
                trackPagingItems.itemSnapshotList.findLast { it?.first?.videoId == videoId }?.first
            }
        if (currentItem != null) {
            itemBottomSheetShow = true
        }
    }
    val onPlaylistMoreClick: () -> Unit = {
        playlistBottomSheetShow = true
    }

    LaunchedEffect(uiState.id, uiState.syncState, uiState.ytPlaylistId) {
        if (shouldShowSuggestions) {
            viewModel.getSuggestions(uiState.id)
        } else {
            viewModel.removeListSuggestion()
        }
    }

    LaunchedEffect(key1 = id) {
        if (id != uiState.id) {
            Logger.w("PlaylistScreen", "new id: $id")
            viewModel.setOffset(0)
            viewModel.removeListSuggestion()
            viewModel.updatePlaylistState(id, true)
            delay(100)
            firstTimeGetLocalPlaylist = true
        }
    }
    LaunchedEffect(key1 = firstItemVisible) {
        shouldHideTopBar = !firstItemVisible
    }
    val paletteState = rememberPaletteState()
    val hazeState = rememberHazeState()
    var artworkBitmap by remember {
        mutableStateOf<Pair<String, ImageBitmap>?>(null)
    }
    // Track which thumbnail we've already extracted a palette from.
    // Prevents palette flash when LazyColumn recycles the header item on scroll —
    // AsyncImage re-mount fires onSuccess again, but we skip the regenerate.
    var paletteGeneratedFor by remember {
        mutableStateOf<String?>(null)
    }
    // Synced empty playlists may inherit YouTube's dark generic cover. Prefer the generated
    // title artwork until the playlist has tracks, while keeping user-selected local covers.
    val currentThumbnail =
        uiState.thumbnail?.takeUnless {
            uiState.trackCount == 0 && it.startsWith("http", ignoreCase = true)
        }

    LaunchedEffect(artworkBitmap, currentThumbnail) {
        val (thumbnail, bm) = artworkBitmap ?: return@LaunchedEffect
        if (thumbnail == currentThumbnail && paletteGeneratedFor != thumbnail) {
            paletteState.generate(bm)
            paletteGeneratedFor = thumbnail
        }
    }

    // Apple Music-inspired immersive treatment. Which header is used depends on the window's
    // aspect ratio alone, not on the platform: a portrait window (a phone held upright, or a
    // narrow desktop window) gets the edge-to-edge artwork header, a landscape one gets the
    // side-by-side header. Everything else on the page — the palette background, the row
    // dividers, the blurred top bar — is shared by both.
    val screenInfo = getScreenSizeInfo()
    val isPortrait = screenInfo.wDP < screenInfo.hDP
    val landscapeHero = onlineLandscapeHeroLayout()
    val landscapeCoverRequest = rememberPlaylistCoverRequest(currentThumbnail)
    val isDark = LocalAppIsDarkTheme.current
    val appColorScheme = LocalAppColorScheme.current ?: MaterialTheme.colorScheme
    val artworkPalette = paletteState.palette.takeIf { paletteGeneratedFor == currentThumbnail }
    val titleColors = remember(uiState.title) { playlistTitleGradient(uiState.title) }
    // Keep a title-derived tint while the cover palette is loading. In light mode, playlists
    // without artwork use the same soft tint so the empty-cover header doesn't look dark.
    val mutedPaletteBg =
        run {
            val palette = artworkPalette
            if (palette != null) {
                palette.toImmersiveBackground(isDark = isDark)
            } else {
                val base =
                    if (titleColors.size >= 2) {
                        lerp(titleColors[0], titleColors[1], 0.5f)
                    } else {
                        titleColors.firstOrNull() ?: Color.Black
                    }
                if (isDark) lerp(base, Color.Black, 0.3f) else lerp(base, Color.White, 0.78f)
            }
        }
    val artworkPageBackgroundTarget =
        if (!currentThumbnail.isNullOrBlank() || !isDark) mutedPaletteBg else Color.Transparent
    val artworkPageBackground by key(currentThumbnail) {
        animateColorAsState(
            targetValue = artworkPageBackgroundTarget,
            animationSpec = tween(420),
            label = "localPlaylistArtworkBackground",
        )
    }
    // With no cover, the hero is a two-color title gradient rather than an image palette. Carry
    // that same gradient through the scrolling page so the area below the hero doesn't fall back
    // to a neutral surface. Reuse the hero's light-mode wash to keep it soft behind text and rows.
    val emptyPlaylistBackgroundBrush =
        remember(currentThumbnail, uiState.title, isDark) {
            if (currentThumbnail.isNullOrBlank() && !isDark) {
                Brush.linearGradient(titleColors.map { lerp(it, Color.White, 0.72f) })
            } else {
                null
            }
        }
    // Use one animated color for the header fade and page surface so they transition together
    // instead of briefly showing different playlist colors while the palette is being applied.
    val pageChromeTint =
        if (!currentThumbnail.isNullOrBlank() || !isDark) artworkPageBackground else appColorScheme.background
    val frostedBackdrop = rememberBackdrop(pageChromeTint)

    // Loading dialog
    val showLoadingDialog by viewModel.showLoadingDialog.collectAsStateWithLifecycle()
    if (showLoadingDialog.first) {
        LoadingDialog(
            true,
            showLoadingDialog.second,
        )
    }
    val coroutineScope = rememberCoroutineScope()
    val dragDropState =
        rememberDragDropState(lazyState) { from, to ->
            coroutineScope.launch {
                Logger.d(TAG, "onMove from $from to $to")
                viewModel.changeLocalPlaylistItemPosition(from - 1, to - 1)
                trackPagingItems.refresh()
            }
        }
    var overscrollJob by remember { mutableStateOf<Job?>(null) }
    var showSearchBar by rememberSaveable { mutableStateOf(false) }
    val headerTextColor = if (isDark) Color.White else appColorScheme.onBackground
    val headerSubtextColor = if (isDark) Color(0xC4FFFFFF) else appColorScheme.onBackground.copy(alpha = 0.7f)
    val headerControlColor = if (isDark) Color.White else appColorScheme.onSurface
    val headerControlSurfaceColor = headerControlColor.copy(alpha = 0.12f)

    val searchResultsState = rememberLazyListState()
    val searchFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val dismissSearch = {
        keyboardController?.hide()
        showSearchBar = false
        viewModel.setSearchQuery("")
    }
    SearchBarScrollDismissEffect(
        state = lazyState,
        visible = showSearchBar,
        onDismiss = { showSearchBar = false },
    )
    SearchBarScrollDismissEffect(
        state = searchResultsState,
        visible = showSearchBar,
        onDismiss = { showSearchBar = false },
    )
    SearchBarBackHandler(
        visible = showSearchBar,
        onDismiss = { showSearchBar = false },
        // Keep a submitted filter in place when Enter or Back hides the keyboard.
        onKeyboardDismissed = { showSearchBar = false },
    )
    val searchHeaderHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + SharedHeaderContentHeight
    val inlineLandscapeSearch = isCompactLandscapeChrome()
    val playlistSearchField: @Composable () -> Unit = {
        PlaylistSearchBar(
            backdrop = frostedBackdrop,
            query = searchQuery,
            onQueryChange = { viewModel.setSearchQuery(it) },
            onSearch = {
                viewModel.setSearchQuery(searchQuery.trim())
                keyboardController?.hide()
            },
            expanded = showSearchBar,
            onExpandedChange = { showSearchBar = it },
            focusRequester = searchFocusRequester,
        )
    }
//    Box {
    LazyColumn(
        modifier =
            Modifier
                .fillMaxWidth()
                .then(
                    emptyPlaylistBackgroundBrush?.let { Modifier.background(it) }
                        ?: Modifier.background(artworkPageBackground),
                )
                .frostedHeaderSource(
                    frostedBackdrop,
                    auroraBackground = pageChromeTint.takeIf { currentThumbnail.isNullOrBlank() },
                )
                .hazeSource(hazeState)
                .pointerInput(changingOrder) {
                    if (!changingOrder) return@pointerInput
                    val onDrag: (change: androidx.compose.ui.input.pointer.PointerInputChange, offset: Offset) -> Unit =
                        { change, offset ->
                            Logger.d(TAG, "onDrag $offset")
                            change.consume()
                            dragDropState.onDrag(offset = offset)

                            if (overscrollJob?.isActive != true) {
                                dragDropState
                                    .checkForOverScroll()
                                    .takeIf { it != 0f }
                                    ?.let {
                                        overscrollJob =
                                            coroutineScope.launch {
                                                dragDropState.state.animateScrollBy(
                                                    it * 1.3f,
                                                    tween(easing = FastOutLinearInEasing),
                                                )
                                            }
                                    }
                                    ?: run { overscrollJob?.cancel() }
                            }
                        }
                    val onDragStart: (Offset) -> Unit = { offset ->
                        Logger.d(TAG, "onDragStart $offset")
                        dragDropState.onDragStart(offset)
                    }
                    val onDragEnd: () -> Unit = {
                        Logger.d(TAG, "onDragEnd")
                        dragDropState.onDragInterrupted(true)
                        overscrollJob?.cancel()
                    }
                    val onDragCancel: () -> Unit = {
                        Logger.d(TAG, "onDragCancel")
                        dragDropState.onDragInterrupted()
                        overscrollJob?.cancel()
                    }
                    if (getPlatform() == Platform.Desktop) {
                        // Desktop: normal drag (click-and-drag), no long press needed
                        detectDragGestures(
                            onDrag = onDrag,
                            onDragStart = onDragStart,
                            onDragEnd = onDragEnd,
                            onDragCancel = onDragCancel,
                        )
                    } else {
                        // Android: long press then drag (touch-friendly)
                        detectDragGesturesAfterLongPress(
                            onDrag = onDrag,
                            onDragStart = onDragStart,
                            onDragEnd = onDragEnd,
                            onDragCancel = onDragCancel,
                        )
                    }
                },
        state = lazyState,
    ) {
        item(contentType = "header") {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .background(Color.Transparent),
            ) {
                Column(
                    Modifier
                        .background(Color.Transparent),
                ) {
                    Column(
                        horizontalAlignment = Alignment.Start,
                    ) {
                        if (isPortrait) {
                            // Apple Music-style: edge-to-edge artwork + liquid glass buttons.
                            // Glass buttons MUST be siblings of the backdrop source (not children)
                            // to avoid render feedback loop / RuntimeShader crash.
                            val artworkBackdrop = rememberBackdrop(Color.Black)
                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .height((screenInfo.hDP / 2).dp),
                            ) {
                                // Inner Box — backdrop SOURCE (artwork + overlays only, NO glass)
                                Box(modifier = Modifier.fillMaxSize().layerBackdrop(artworkBackdrop)) {
                                    if (currentThumbnail.isNullOrBlank()) {
                                        LocalPlaylistHeroArtwork(Modifier.fillMaxSize())
                                    } else AsyncImage(
                                        model =
                                            ImageRequest
                                                .Builder(LocalPlatformContext.current)
                                                .data(currentThumbnail)
                                                .diskCachePolicy(CachePolicy.ENABLED)
                                                .memoryCachePolicy(CachePolicy.ENABLED)
                                                .diskCacheKey(currentThumbnail)
                                                .memoryCacheKey(currentThumbnail)
                                                .crossfade(false)
                                                .build(),
                                        placeholder = painterPlaylistThumbnail(uiState.title, style = typo().labelMedium, 250.dp to 250.dp),
                                        error = painterPlaylistThumbnail(uiState.title, style = typo().labelMedium, 250.dp to 250.dp),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        onSuccess = {
                                            artworkBitmap =
                                                currentThumbnail?.let { thumbnail ->
                                                    thumbnail to it.result.image.toImageBitmap()
                                                }
                                        },
                                        modifier = Modifier.fillMaxSize(),
                                    )
                                    // Scrim spans 70% of the artwork (not a fixed 200dp): the shorter the
                                    // ramp, the steeper the alpha, and a steep ramp is what makes the fade
                                    // read as an edge. See artworkScrimBrush for the curve itself.
                                    Box(
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .height((screenInfo.hDP * 0.35f).dp)
                                                .align(Alignment.BottomCenter)
                                                .background(artworkScrimBrush(artworkPageBackground)),
                                    )
                                    Column(
                                        modifier =
                                            Modifier
                                                .align(Alignment.BottomCenter)
                                                .fillMaxWidth()
                                                                .padding(horizontal = 16.dp)
                                                .padding(bottom = 16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                    ) {
                                        Text(
                                            text = uiState.title,
                                            style = typo().titleLarge,
                                            color = headerTextColor,
                                            maxLines = 2,
                                            textAlign = TextAlign.Center,
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = stringResource(Res.string.your_playlist),
                                            style = typo().titleSmall,
                                            color = headerTextColor,
                                            textAlign = TextAlign.Center,
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text =
                                                stringResource(
                                                    Res.string.created_at,
                                                    uiState.inLibrary?.format(
                                                        LocalDateTime.Format {
                                                            hour()
                                                            char(':')
                                                            minute()
                                                            chars(" - ")
                                                            day()
                                                            char(' ')
                                                            monthName(
                                                                MonthNames.ENGLISH_FULL,
                                                            )
                                                            char(' ')
                                                            year()
                                                        },
                                                    ) ?: "",
                                                ),
                                            style = typo().bodyMedium,
                                            color = headerSubtextColor,
                                            textAlign = TextAlign.Center,
                                        )
                                    }
                                }
                                // Back button + right pill (More only) overlays — liquid glass, siblings of source
                            }
                        } else {
                            // Apple Music desktop header: back and the overlay actions on their own
                            // top row, then a square artwork with the text column and the action
                            // cluster laid out beside it rather than under it.
                            // Built exactly like the portrait branch, which renders correctly on both
                            // platforms: the backdrop SOURCE is the content column — artwork included,
                            // so the recorded layer holds something to refract — and the glass buttons
                            // are SIBLINGS placed with align(), never children of the source (that
                            // nesting is the render-feedback loop that kills the RuntimeShader).
                            val headerBackdrop = rememberBackdrop(pageChromeTint)
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .layerBackdrop(headerBackdrop)
                                            .padding(
                                                start = landscapeHero.horizontalPadding,
                                                end = landscapeHero.horizontalPadding,
                                                top = landscapeHero.topPadding,
                                                bottom = landscapeHero.bottomPadding,
                                            ),
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(landscapeHero.columnSpacing),
                                        verticalAlignment = Alignment.Top,
                                    ) {
                                        if (currentThumbnail.isNullOrBlank()) {
                                            LocalPlaylistArtworkPlaceholder(
                                                modifier = Modifier.size(landscapeHero.coverSize),
                                            )
                                        } else AsyncImage(
                                            model = landscapeCoverRequest,
                                            placeholder = painterPlaylistThumbnail(uiState.title, style = typo().labelMedium, 250.dp to 250.dp),
                                            error = painterPlaylistThumbnail(uiState.title, style = typo().labelMedium, 250.dp to 250.dp),
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            onSuccess = {
                                                artworkBitmap =
                                                    currentThumbnail?.let { thumbnail ->
                                                        thumbnail to it.result.image.toImageBitmap()
                                                    }
                                            },
                                            modifier =
                                                Modifier
                                                    .size(landscapeHero.coverSize)
                                                    .clip(RoundedCornerShape(8.dp)),
                                        )
                                        Column(
                                            modifier = Modifier.weight(1f),
                                        ) {
                                            Text(
                                                text = uiState.title,
                                                style = landscapeHero.titleStyle,
                                                color = headerTextColor,
                                                maxLines = landscapeHero.titleMaxLines,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = stringResource(Res.string.your_playlist),
                                                style = if (landscapeHero.compact) typo().bodyMedium else typo().titleMedium,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                // The app accent, standing in for the brand red Apple uses here.
                                                color = seed,
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text =
                                                    stringResource(
                                                        Res.string.created_at,
                                                        uiState.inLibrary?.format(
                                                            LocalDateTime.Format {
                                                                hour()
                                                                char(':')
                                                                minute()
                                                                chars(" - ")
                                                                day()
                                                                char(' ')
                                                                monthName(
                                                                    MonthNames.ENGLISH_FULL,
                                                                )
                                                                char(' ')
                                                                year()
                                                            },
                                                        ) ?: "",
                                                    ),
                                                style = typo().bodyMedium,
                                                color = headerSubtextColor,
                                                textAlign = TextAlign.Start,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                            Spacer(modifier = Modifier.height(landscapeHero.actionSpacing))
                                            // Apple Music-style action row: [Shuffle][Play pill][Download][Suggest]
                                            // centered cluster. Suggest (AI) only shows when synced with YouTube.
                                            val isThisPlaying =
                                                isPlaying && playingPlaylistId == LOCAL_PLAYLIST_ID + uiState.id
                                            Row(
                                                modifier =
                                                    Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = if (landscapeHero.compact) 0.dp else 8.dp),
                                                horizontalArrangement =
                                                    Arrangement.spacedBy(
                                                        12.dp,
                                                        Alignment.Start,
                                                    ),
                                                verticalAlignment = Alignment.CenterVertically,
                                            ) {
                                                Box(
                                                    modifier =
                                                        Modifier
                                                            .size(if (landscapeHero.compact) 40.dp else 48.dp)
                                                            .clip(CircleShape)
                                                            .background(headerControlSurfaceColor)
                                                            .clickable {
                                                                viewModel.onUIEvent(LocalPlaylistUIEvent.ShuffleClick)
                                                            },
                                                    contentAlignment = Alignment.Center,
                                                ) {
                                                    Icon(
                                                        imageVector = SimpIcons.Shuffle,
                                                        contentDescription = stringResource(Res.string.shuffle),
                                                        tint = headerControlColor,
                                                        modifier = Modifier.size(22.dp),
                                                    )
                                                }
                                                Box(
                                                    modifier =
                                                        Modifier
                                                            .height(if (landscapeHero.compact) 40.dp else 48.dp)
                                                            .widthIn(min = 110.dp)
                                                            .clip(CircleShape)
                                                            .background(Color.White)
                                                            .clickable {
                                                                if (isThisPlaying) {
                                                                    sharedViewModel.onUIEvent(UIEvent.PlayPause)
                                                                } else {
                                                                    viewModel.onUIEvent(LocalPlaylistUIEvent.PlayClick)
                                                                }
                                                            }.padding(horizontal = 20.dp),
                                                    contentAlignment = Alignment.Center,
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(
                                                            imageVector =
                                                                if (isThisPlaying) {
                                                                    SimpIcons.Pause
                                                                } else {
                                                                    SimpIcons.PlayArrow
                                                                },
                                                            contentDescription = null,
                                                            tint = Color.Black,
                                                            modifier = Modifier.size(22.dp),
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(
                                                            text = if (isThisPlaying) stringResource(Res.string.pause_action) else stringResource(Res.string.play_action),
                                                            color = Color.Black,
                                                            style = typo().labelLarge,
                                                        )
                                                    }
                                                }
                                                Box(
                                                    modifier =
                                                        Modifier
                                                            .size(if (landscapeHero.compact) 40.dp else 48.dp)
                                                            .clip(CircleShape)
                                                            .background(headerControlSurfaceColor),
                                                    contentAlignment = Alignment.Center,
                                                ) {
                                                    Crossfade(targetState = downloadState) { state ->
                                                        when (state) {
                                                            DownloadState.STATE_DOWNLOADED -> {
                                                                Box(
                                                                    modifier =
                                                                        Modifier
                                                                            .fillMaxSize()
                                                                            .clickable {
                                                                                viewModel.makeToast(
                                                                                    runBlocking { getString(Res.string.downloaded) },
                                                                                )
                                                                            },
                                                                    contentAlignment = Alignment.Center,
                                                                ) {
                                                                    Icon(
                                                                        painter = painterResource(Res.drawable.baseline_downloaded),
                                                                        tint = Color(0xFF00A0CB),
                                                                        contentDescription = "",
                                                                        modifier = Modifier.size(22.dp),
                                                                    )
                                                                }
                                                            }

                                                            DownloadState.STATE_DOWNLOADING -> {
                                                                Box(
                                                                    modifier =
                                                                        Modifier
                                                                            .fillMaxSize()
                                                                            .clickable {
                                                                                viewModel.makeToast(
                                                                                    runBlocking { getString(Res.string.downloading) },
                                                                                )
                                                                            },
                                                                    contentAlignment = Alignment.Center,
                                                                ) {
                                                                    DownloadingIndicator(
                                                                        modifier = Modifier.size(28.dp),
                                                                    )
                                                                }
                                                            }

                                                            else -> {
                                                                Box(
                                                                    modifier =
                                                                        Modifier
                                                                            .fillMaxSize()
                                                                            .clickable {
                                                                                Logger.w("PlaylistScreen", "downloadState: $downloadState")
                                                                                viewModel.downloadFullPlaylist()
                                                                            },
                                                                    contentAlignment = Alignment.Center,
                                                                ) {
                                                                    Icon(
                                                                        imageVector = SimpIcons.DownloadForOffline,
                                                                         tint = headerControlColor,
                                                                        contentDescription = stringResource(Res.string.download),
                                                                        modifier = Modifier.size(22.dp),
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight(),
                        ) {
                            Column(Modifier.padding(horizontal = pageContentHorizontalInset(28.dp))) {
                                if (isPortrait) {
                                    // Apple Music-style action row: [Shuffle][Play pill][Download][Suggest]
                                    // centered cluster. Suggest (AI) only shows when synced with YouTube.
                                    val isThisPlaying =
                                        isPlaying && playingPlaylistId == LOCAL_PLAYLIST_ID + uiState.id
                                    Row(
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp),
                                        horizontalArrangement =
                                            Arrangement.spacedBy(
                                                12.dp,
                                                Alignment.CenterHorizontally,
                                            ),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Box(
                                            modifier =
                                                Modifier
                                                    .size(48.dp)
                                                    .clip(CircleShape)
                                                    .background(headerControlSurfaceColor)
                                                    .clickable {
                                                        viewModel.onUIEvent(LocalPlaylistUIEvent.ShuffleClick)
                                                    },
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Icon(
                                                imageVector = SimpIcons.Shuffle,
                                                contentDescription = stringResource(Res.string.shuffle),
                                                tint = headerControlColor,
                                                modifier = Modifier.size(22.dp),
                                            )
                                        }
                                        Box(
                                            modifier =
                                                Modifier
                                                    .height(48.dp)
                                                    .widthIn(min = 110.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.White)
                                                    .clickable {
                                                        if (isThisPlaying) {
                                                            sharedViewModel.onUIEvent(UIEvent.PlayPause)
                                                        } else {
                                                            viewModel.onUIEvent(LocalPlaylistUIEvent.PlayClick)
                                                        }
                                                    }.padding(horizontal = 20.dp),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector =
                                                        if (isThisPlaying) {
                                                            SimpIcons.Pause
                                                        } else {
                                                            SimpIcons.PlayArrow
                                                        },
                                                    contentDescription = null,
                                                    tint = Color.Black,
                                                    modifier = Modifier.size(22.dp),
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = if (isThisPlaying) stringResource(Res.string.pause_action) else stringResource(Res.string.play_action),
                                                    color = Color.Black,
                                                    style = typo().labelLarge,
                                                )
                                            }
                                        }
                                        Box(
                                            modifier =
                                                Modifier
                                                    .size(48.dp)
                                                    .clip(CircleShape)
                                                    .background(headerControlSurfaceColor),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Crossfade(targetState = downloadState) { state ->
                                                when (state) {
                                                    DownloadState.STATE_DOWNLOADED -> {
                                                        Box(
                                                            modifier =
                                                                Modifier
                                                                    .fillMaxSize()
                                                                    .clickable {
                                                                        viewModel.makeToast(
                                                                            runBlocking { getString(Res.string.downloaded) },
                                                                        )
                                                                    },
                                                            contentAlignment = Alignment.Center,
                                                        ) {
                                                            Icon(
                                                                painter = painterResource(Res.drawable.baseline_downloaded),
                                                                tint = Color(0xFF00A0CB),
                                                                contentDescription = "",
                                                                modifier = Modifier.size(22.dp),
                                                            )
                                                        }
                                                    }

                                                    DownloadState.STATE_DOWNLOADING -> {
                                                        Box(
                                                            modifier =
                                                                Modifier
                                                                    .fillMaxSize()
                                                                    .clickable {
                                                                        viewModel.makeToast(
                                                                            runBlocking { getString(Res.string.downloading) },
                                                                        )
                                                                    },
                                                            contentAlignment = Alignment.Center,
                                                        ) {
                                                            DownloadingIndicator(
                                                                modifier = Modifier.size(28.dp),
                                                            )
                                                        }
                                                    }

                                                    else -> {
                                                        Box(
                                                            modifier =
                                                                Modifier
                                                                    .fillMaxSize()
                                                                    .clickable {
                                                                        Logger.w("PlaylistScreen", "downloadState: $downloadState")
                                                                        viewModel.downloadFullPlaylist()
                                                                    },
                                                            contentAlignment = Alignment.Center,
                                                        ) {
                                                            Icon(
                                                                imageVector = SimpIcons.DownloadForOffline,
                                                                 tint = headerControlColor,
                                                                contentDescription = stringResource(Res.string.download),
                                                                modifier = Modifier.size(22.dp),
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                Text(
                                    text =
                                        stringResource(
                                            Res.string.album_length,
                                            (uiState.trackCount).toString(),
                                            "",
                                        ),
                                    color = headerSubtextColor,
                                    style = typo().bodyMedium,
                                    modifier = Modifier.padding(vertical = 8.dp),
                                )
                                Column {
                                    Row(horizontalArrangement = Arrangement.SpaceBetween) {
                                        // Match the Apple Music action buttons: flat white@12% pill, white content.
                                        Row(
                                            modifier =
                                                Modifier
                                                    .clip(CircleShape)
                                                    .background(headerControlSurfaceColor)
                                                    .clickable {
                                                        sortBottomSheetShow = true
                                                    }.padding(vertical = 8.dp, horizontal = 14.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Icon(
                                                imageVector = SimpIcons.Sort,
                                                contentDescription = stringResource(Res.string.sort_playlist),
                                                tint = headerControlColor,
                                                modifier = Modifier.size(20.dp),
                                            )
                                            Spacer(modifier = Modifier.size(6.dp))
                                            Text(
                                                text =
                                                    stringResource(Res.string.sort_by) + ": " +
                                                        stringResource(
                                                            uiState.filterState.displayNameRes(),
                                                        ),
                                                style = typo().bodySmall,
                                                color = headerControlColor,
                                            )
                                        }
                                        Spacer(Modifier.weight(1f))
                                        AnimatedVisibility(
                                            visible =
                                                uiState.filterState == FilterState.CustomOrder,
                                            enter = fadeIn(),
                                            exit = fadeOut(),
                                        ) {
                                            TextButton(
                                                onClick = {
                                                    changingOrder = !changingOrder
                                                },
                                            ) {
                                                Text(
                                                    text =
                                                        if (changingOrder) {
                                                            "Done"
                                                        } else {
                                                            "Change order"
                                                        },
                                                    style = typo().bodySmall,
                                                    color = headerControlColor,
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        items(count = trackPagingItems.itemCount, key = { index ->
            trackPagingItems[index]?.second?.id ?: "playlist_track_$index"
        }) { index ->
            val pair = trackPagingItems[index]
            val item = pair?.first
            val rowKey = pair?.second?.id ?: "playlist_track_$index"
            if (item != null && item.videoId !in suggestedVideoIds) {
                val content = @Composable { mod: Modifier ->
                    if (playingTrack?.videoId == item.videoId && isPlaying) {
                        SongFullWidthItems(
                            forceDark = isDark,
                            contentColorOverride = appColorScheme.onSurface,
                            subtitleColorOverride = appColorScheme.onSurfaceVariant,
                            isPlaying = true,
                            showFavoriteButton = true,
                            shouldShowDragHandle = changingOrder,
                            songEntity = item,
                            startContentPadding = pageContentHorizontalInset(11.dp),
                            endContentPadding = pageContentHorizontalInset(11.dp),
                            onMoreClickListener = { onItemMoreClick(it) },
                            onClickListener = {
                                Logger.w("PlaylistScreen", "index: $index")
                                onPlaylistItemClick(it)
                            },
                            onAddToQueue = {
                                sharedViewModel.addListToQueue(
                                    arrayListOf(item.toTrack()),
                                )
                            },
                            onRemoveFromPlaylist =
                                if (changingOrder) {
                                    null
                                } else {
                                    { _ -> viewModel.deleteItem(uiState.id, item) }
                                },
                            selectionMode = selectionState.isActive,
                            isSelected = selectionState.isSelected(item.videoId),
                            // No long press while reordering: that gesture already belongs to drag.
                            onLongClick = if (changingOrder) null else ({ selectionState.start(it) }),
                            onSelectToggle = { selectionState.toggle(it) },
                            modifier = mod,
                        )
                    } else {
                        SongFullWidthItems(
                            forceDark = isDark,
                            contentColorOverride = appColorScheme.onSurface,
                            subtitleColorOverride = appColorScheme.onSurfaceVariant,
                            isPlaying = false,
                            showFavoriteButton = true,
                            shouldShowDragHandle = changingOrder,
                            songEntity = item,
                            startContentPadding = pageContentHorizontalInset(11.dp),
                            endContentPadding = pageContentHorizontalInset(11.dp),
                            onMoreClickListener = { onItemMoreClick(it) },
                            onClickListener = {
                                Logger.w("PlaylistScreen", "index: $index")
                                onPlaylistItemClick(it)
                            },
                            onAddToQueue = {
                                sharedViewModel.addListToQueue(
                                    arrayListOf(item.toTrack()),
                                )
                            },
                            onRemoveFromPlaylist =
                                if (changingOrder) {
                                    null
                                } else {
                                    { _ -> viewModel.deleteItem(uiState.id, item) }
                                },
                            selectionMode = selectionState.isActive,
                            isSelected = selectionState.isSelected(item.videoId),
                            // No long press while reordering: that gesture already belongs to drag.
                            onLongClick = if (changingOrder) null else ({ selectionState.start(it) }),
                            onSelectToggle = { selectionState.toggle(it) },
                            modifier = mod,
                        )
                    }
                }
                if (changingOrder) {
                    DraggableItem(
                        dragDropState,
                        index + 1,
                        Modifier.animateItem(),
                        itemKey = rowKey,
                    ) {
                        content(Modifier)
                    }
                } else {
                    Column(modifier = Modifier.animateItem(placementSpec = null)) {
                        content(Modifier)
                        if (index < trackPagingItems.itemCount - 1) {
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 68.dp, end = 12.dp),
                                thickness = 0.5.dp,
                                color = appColorScheme.onSurface.copy(alpha = 0.12f),
                            )
                        }
                    }
                }
            }
        }
        trackPagingItems.apply {
            item {
                AnimatedVisibility(loadState.refresh is LoadState.Loading || loadState.append is LoadState.Loading) {
                    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Spacer(modifier = Modifier.height(15.dp))
                        CenterLoadingBox(modifier = Modifier.size(80.dp))
                        Spacer(modifier = Modifier.height(15.dp))
                    }
                }
            }
        }
        if (shouldShowSuggestions) {
            item(key = "playlist_suggestions") {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .animateContentSize()
                            .padding(horizontal = pageContentHorizontalInset(28.dp)),
                ) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(Res.string.suggestions),
                            color = headerTextColor,
                            style = typo().titleLarge,
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(
                            onClick = { viewModel.reloadSuggestion() },
                            modifier = Modifier.padding(horizontal = 8.dp),
                        ) {
                            Text(
                                text = stringResource(Res.string.reload),
                                color = headerControlColor,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Crossfade(targetState = suggestionsLoading && suggestedTracks.isEmpty()) { isLoading ->
                        if (isLoading) {
                            CenterLoadingBox(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .height(200.dp),
                            )
                        } else {
                            Column {
                                suggestedTracks.forEach { track ->
                                    SuggestItems(
                                        forceDark = isDark,
                                        contentColorOverride = appColorScheme.onSurface,
                                        subtitleColorOverride = appColorScheme.onSurfaceVariant,
                                        track = track,
                                        isPlaying = playingTrack?.videoId == track.videoId,
                                        onAddClickListener = {
                                            viewModel.addSuggestTrackToListTrack(track)
                                        },
                                        onClickListener = {
                                            viewModel.onUIEvent(LocalPlaylistUIEvent.SuggestionsItemClick(track.videoId))
                                        },
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(
                        color = Color.Gray,
                        thickness = 0.5.dp,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
        item {
            EndOfPage()
        }
    }
    if (showSelectionSheet) {
        val selectedIds = selectionState.selected.toList()
        val removeLabel = stringResource(Res.string.remove_from_playlist)
        SelectedSongsBottomSheet(
            count = selectedIds.size,
            selectedVideoIds = selectedIds,
            onDismiss = { showSelectionSheet = false },
            onPlayNext = {
                selectionViewModel.playNext(selectedIds)
                selectionState.exit()
            },
            onAddToQueue = {
                selectionViewModel.addToQueue(selectedIds)
                selectionState.exit()
            },
            onAddToPlaylist = { showSelectionAddToPlaylist = true },
            onDownload = {
                selectionViewModel.download(selectedIds)
                selectionState.exit()
            },
            onAddToFavorite = {
                selectionViewModel.addToFavorite(selectedIds)
                selectionState.exit()
            },
            onRemoveFromFavorite = {
                selectionViewModel.removeFromFavorite(selectedIds)
                selectionState.exit()
            },
            extraActions =
                listOf(
                    SongSelectionAction(
                        icon = SimpIcons.Delete,
                        label = removeLabel,
                        tint = Color.Red,
                    ) {
                        selectionViewModel.removeFromLocalPlaylist(uiState.id, selectedIds)
                        selectionState.exit()
                    },
                ),
        )
    }
    if (showSelectionAddToPlaylist) {
        val selectedIds = selectionState.selected.toList()
        val localPlaylists by selectionViewModel.listLocalPlaylist.collectAsStateWithLifecycle()
        val availableSongs = (trackPagingItems.itemSnapshotList.items.map { it.first }).filter { it.videoId in selectedIds }
        val playlistsLoading by selectionViewModel.playlistListLoading.collectAsStateWithLifecycle()
        val youTubePlaylists by selectionViewModel.listYouTubePlaylist.collectAsStateWithLifecycle()
        LaunchedEffect(Unit) { selectionViewModel.refreshYouTubePlaylists() }
        AddToPlaylistModalBottomSheet(
            isBottomSheetVisible = true,
            listLocalPlaylist = localPlaylists,
            listYouTubePlaylist = youTubePlaylists,
            isPlaylistListLoading = playlistsLoading,
            onDismiss = { showSelectionAddToPlaylist = false },
            onClick = { playlist ->
                selectionViewModel.addToPlaylist(playlist.id, selectedIds, availableSongs)
                selectionState.exit()
            },
            onYTPlaylistClick = { playlist ->
                selectionViewModel.addToYouTubePlaylist(playlist, selectedIds, availableSongs)
                selectionState.exit()
            },
            onCreatePlaylist = { name ->
                selectionViewModel.createPlaylistWithSelectedSongs(name, selectedIds, availableSongs)
                selectionState.exit()
            },
        )
    }
    if (itemBottomSheetShow && currentItem != null) {
        val track = currentItem ?: return
        NowPlayingBottomSheet(
            onDelete = { viewModel.deleteItem(uiState.id, track) },
            showAddToPlaylist = false,
            showFavoriteAction = false,
            onDismiss = {
                itemBottomSheetShow = false
                currentItem = null
            },
            navController = navController,
            song = track,
        )
    }
    if (playlistBottomSheetShow) {
        Logger.w("PlaylistScreen", "PlaylistBottomSheet")
        LocalPlaylistBottomSheet(
            isBottomSheetVisible = playlistBottomSheetShow,
            onDismiss = { playlistBottomSheetShow = false },
            title = uiState.title,
            ytPlaylistId = uiState.ytPlaylistId,
            onEditTitle =
                { newTitle ->
                    viewModel.updatePlaylistTitle(newTitle, uiState.id)
                },
            onEditThumbnail =
                { thumbUri ->
                    viewModel.updatePlaylistThumbnail(thumbUri, uiState.id)
                },
            onAddToQueue = {
                viewModel.addAllToQueue()
            },
            onSync = {
                if (uiState.syncState == LocalPlaylistEntity.YouTubeSyncState.Synced) {
                    showUnsyncAlertDialog = true
                } else {
                    showSyncAlertDialog = true
                }
            },
            onUpdatePlaylist = {
                viewModel.updateListTrackSynced(uiState.id)
            },
            onDelete = {
                viewModel.deletePlaylist(uiState.id)
                navController.navigateUp()
            },
        )
    }
    if (showSyncAlertDialog) {
        AmoledAlertDialog(
            title = { Text(text = stringResource(Res.string.warning)) },
            text = { Text(text = stringResource(Res.string.sync_playlist_warning)) },
            onDismissRequest = { showSyncAlertDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.syncPlaylistWithYouTubePlaylist(uiState.id)
                    showSyncAlertDialog = false
                }) {
                    Text(text = stringResource(Res.string.yes))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showSyncAlertDialog = false
                }) {
                    Text(text = stringResource(Res.string.cancel))
                }
            },
        )
    }
    if (showUnsyncAlertDialog) {
        AmoledAlertDialog(
            title = { Text(text = stringResource(Res.string.warning)) },
            text = { Text(text = stringResource(Res.string.unsync_playlist_warning)) },
            onDismissRequest = { showUnsyncAlertDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.unsyncPlaylistWithYouTubePlaylist(uiState.id)
                    showUnsyncAlertDialog = false
                }) {
                    Text(text = stringResource(Res.string.yes))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showUnsyncAlertDialog = false
                }) {
                    Text(text = stringResource(Res.string.cancel))
                }
            },
        )
    }
    // Database results stay separate from the paged reader, preserving its reorder
    // and scroll state. An empty query leaves the playlist visible beneath the field.
    AnimatedVisibility(
        visible = searchQuery.isNotBlank(),
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        LazyColumn(
            state = searchResultsState,
            modifier = Modifier
                .fillMaxSize()
                .background(pageChromeTint)
                .frostedHeaderSource(
                    frostedBackdrop,
                    auroraBackground = pageChromeTint.takeIf { currentThumbnail.isNullOrBlank() },
                )
                .hazeSource(hazeState),
            contentPadding = PaddingValues(
                top = searchHeaderHeight + (if (inlineLandscapeSearch) 0.dp else InPageSearchBarHeight) + 8.dp,
                bottom = pageEndSpacing(),
            ),
        ) {
            items(
                searchResults.filterNot { (song, _) -> song.videoId in suggestedVideoIds },
                key = { (song, pair) -> song.videoId + "_" + pair.position },
            ) { (song, _) ->
                SongFullWidthItems(
                    forceDark = isDark,
                    contentColorOverride = appColorScheme.onSurface,
                    subtitleColorOverride = appColorScheme.onSurfaceVariant,
                    isPlaying = playingTrack?.videoId == song.videoId && isPlaying,
                    showFavoriteButton = true,
                    songEntity = song,
                    startContentPadding = pageContentHorizontalInset(11.dp),
                    endContentPadding = pageContentHorizontalInset(11.dp),
                    onMoreClickListener = { onItemMoreClick(it) },
                    onClickListener = { onPlaylistItemClick(it) },
                    onAddToQueue = { sharedViewModel.addListToQueue(arrayListOf(song.toTrack())) },
                    selectionMode = selectionState.isActive,
                    isSelected = selectionState.isSelected(song.videoId),
                    onLongClick = { selectionState.start(it) },
                    onSelectToggle = { selectionState.toggle(it) },
                    // Required, no default — the paged list passes it down through `mod`.
                    modifier = Modifier,
                )
                HorizontalDivider(modifier = Modifier.padding(start = 68.dp, end = 12.dp))
            }
        }
    }
    AnimatedVisibility(
        modifier = Modifier.padding(top = searchHeaderHeight),
        visible = showSearchBar && !inlineLandscapeSearch,
        enter = SearchBarEnter,
        exit = SearchBarExit,
    ) {
        playlistSearchField()
    }
    if (sortBottomSheetShow) {
        SortPlaylistBottomSheet(
            selectedState = uiState.filterState,
            onDismiss = { sortBottomSheetShow = false },
            onSortChanged = {
                viewModel.onUIEvent(LocalPlaylistUIEvent.ChangeFilter(it))
                sortBottomSheetShow = false
            },
        )
    }
    PlaylistPageHeader(
        title = uiState.title,
        scrollState = lazyState,
        showTitle = shouldHideTopBar || showSearchBar || searchQuery.isNotBlank(),
        hazeState = hazeState,
        backdrop = frostedBackdrop,
        tint = pageChromeTint,
        contentColor = headerControlColor,
        onBack = { navController.navigateUp() },
        onSearch = {
            if (showSearchBar || searchQuery.isNotBlank()) dismissSearch() else showSearchBar = true
        },
        searchActive = showSearchBar || searchQuery.isNotBlank(),
        searchContent = playlistSearchField.takeIf { inlineLandscapeSearch },
        searchVisible = showSearchBar && inlineLandscapeSearch,
        selectionContent = {
            SongSelectionTopAppBar(
                state = selectionState,
                onSelectAll = {
                    // peek() rather than get(): reading an item is what asks Paging to load its page,
                    // and select-all must not drag the whole playlist into memory.
                    selectionState.toggleSelectAll(
                        (0 until trackPagingItems.itemCount).mapNotNull {
                            trackPagingItems.peek(it)?.first?.videoId?.takeUnless { videoId -> videoId in suggestedVideoIds }
                        },
                    )
                },
                onOpenActions = { showSelectionSheet = true },
                backdrop = frostedBackdrop,
                windowInsets = WindowInsets(0),
            )
        },
        selectionActive = selectionState.isActive,
        showSurface = false,
        onMore = onPlaylistMoreClick,
    )
}
