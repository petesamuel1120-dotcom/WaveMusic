package com.maxrave.simpmusic.ui.screen.other

import com.maxrave.simpmusic.ui.component.pageContentHorizontalInset
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.toRoute
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.kmpalette.rememberPaletteState
import com.maxrave.domain.data.entities.DownloadState
import com.maxrave.domain.data.model.browse.album.Track
import com.maxrave.domain.utils.LiveStreamType
import com.maxrave.domain.utils.toSongEntity
import com.maxrave.logger.Logger
import com.maxrave.simpmusic.ui.component.frostedHeaderSource
import com.maxrave.simpmusic.ui.component.rememberHolderPainter
import com.maxrave.simpmusic.expect.ui.layerBackdrop
import com.maxrave.simpmusic.expect.ui.rememberBackdrop
import com.maxrave.simpmusic.expect.ui.toImageBitmap
import com.maxrave.simpmusic.extension.artworkScrimBrush
import com.maxrave.simpmusic.extension.consumePointerInputWhen
import com.maxrave.simpmusic.extension.getScreenSizeInfo
import com.maxrave.simpmusic.extension.getStringBlocking
import com.maxrave.simpmusic.extension.toImmersiveBackground
import com.maxrave.simpmusic.ui.component.AddToPlaylistModalBottomSheet
import com.maxrave.simpmusic.ui.component.CenterLoadingBox
import com.maxrave.simpmusic.ui.component.DescriptionView
import com.maxrave.simpmusic.ui.component.FilteredPlaylistList
import com.maxrave.simpmusic.ui.component.HeartCheckBox
import com.maxrave.simpmusic.ui.component.PlaylistPageHeader
import com.maxrave.simpmusic.ui.component.PlaylistArtworkCache
import com.maxrave.simpmusic.ui.component.LocalPageContentInsets
import com.maxrave.simpmusic.ui.component.LocalPageArtworkBackground
import com.maxrave.simpmusic.ui.component.onlineLandscapeHeroLayout
import com.maxrave.simpmusic.ui.component.GlassBackdropBox
import com.maxrave.simpmusic.ui.component.StickyPlaylistTitleState
import com.maxrave.simpmusic.ui.component.LoadingDialog
import com.maxrave.simpmusic.ui.component.NowPlayingBottomSheet
import com.maxrave.simpmusic.ui.component.PlaylistBottomSheet
import com.maxrave.simpmusic.ui.component.PlaylistGlassActions
import com.maxrave.simpmusic.ui.component.OnlinePlaylistActions
import com.maxrave.simpmusic.ui.component.LocalPlaylistHeroArtwork
import com.maxrave.simpmusic.ui.component.painterPlaylistThumbnail
import com.maxrave.simpmusic.ui.component.rememberPlaylistCoverRequest
import com.maxrave.simpmusic.ui.component.toPlaylistCoverThumbnailUrl
import com.maxrave.simpmusic.ui.component.SearchBarEnter
import com.maxrave.simpmusic.ui.component.SearchBarExit
import com.maxrave.simpmusic.ui.component.SearchBarBackHandler
import com.maxrave.simpmusic.ui.component.SearchBarScrollDismissEffect
import com.maxrave.simpmusic.ui.component.SearchResultsScrollEffect
import com.maxrave.simpmusic.ui.component.EndOfPage
import com.maxrave.simpmusic.ui.component.PlaylistSearchBar
import com.maxrave.simpmusic.ui.component.InPageSearchBarHeight
import com.maxrave.simpmusic.ui.component.SharedHeaderContentHeight
import com.maxrave.simpmusic.ui.component.CompactHeaderContentHeight
import com.maxrave.simpmusic.ui.component.HeaderBackdropExtensionHeight
import com.maxrave.simpmusic.ui.component.headerGlassControlSize
import com.maxrave.simpmusic.ui.component.isCompactLandscapeChrome
import com.maxrave.simpmusic.ui.component.SongFullWidthItems
import com.maxrave.simpmusic.ui.component.SuggestItems
import com.maxrave.simpmusic.ui.component.selection.SelectedSongsBottomSheet
import com.maxrave.simpmusic.ui.component.selection.SongSelectionAction
import com.maxrave.simpmusic.ui.component.selection.SongSelectionTopAppBar
import com.maxrave.simpmusic.ui.component.selection.rememberSongSelectionState
import com.maxrave.simpmusic.ui.icon.Shuffle
import com.maxrave.simpmusic.ui.icon.Delete
import com.maxrave.simpmusic.ui.icon.SimpIcons
import com.maxrave.simpmusic.ui.navigation.destination.list.ArtistDestination
import com.maxrave.simpmusic.ui.navigation.destination.list.PlaylistDestination
import com.maxrave.simpmusic.ui.navigation.destination.library.LibraryDestination
import com.maxrave.simpmusic.ui.theme.LocalAppColorScheme
import com.maxrave.simpmusic.ui.theme.LocalAppIsDarkTheme
import com.maxrave.simpmusic.ui.theme.LocalLiquidGlassEnabled
import com.maxrave.simpmusic.ui.theme.seed
import com.maxrave.simpmusic.ui.theme.typo
import com.maxrave.simpmusic.viewModel.ListState
import com.maxrave.simpmusic.viewModel.LibraryViewModel
import com.maxrave.simpmusic.viewModel.PlaylistUIEvent
import com.maxrave.simpmusic.viewModel.PlaylistUIState
import com.maxrave.simpmusic.viewModel.PlaylistViewModel
import com.maxrave.simpmusic.viewModel.SharedViewModel
import com.maxrave.simpmusic.viewModel.SongSelectionViewModel
import com.maxrave.simpmusic.viewModel.UIEvent
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import simpmusic.composeapp.generated.resources.Res
import simpmusic.composeapp.generated.resources.baseline_downloaded
import simpmusic.composeapp.generated.resources.downloaded
import simpmusic.composeapp.generated.resources.downloading
import simpmusic.composeapp.generated.resources.error
import simpmusic.composeapp.generated.resources.n_track
import simpmusic.composeapp.generated.resources.no_description
import simpmusic.composeapp.generated.resources.playlist
import simpmusic.composeapp.generated.resources.radio
import simpmusic.composeapp.generated.resources.remove_from_playlist
import simpmusic.composeapp.generated.resources.retry
import simpmusic.composeapp.generated.resources.search
import simpmusic.composeapp.generated.resources.reload
import simpmusic.composeapp.generated.resources.suggestions
import simpmusic.composeapp.generated.resources.unlimited

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PlaylistScreen(
    viewModel: PlaylistViewModel = koinViewModel(),
    sharedViewModel: SharedViewModel = koinInject(),
    playlistId: String,
    isYourYouTubePlaylist: Boolean,
    navController: NavController,
) {
    // Home shelves navigate with the browseEndpoint id, which is "VL" + the playlist id
    // (HomeParser reads title.runs[0].navigationEndpoint.browseEndpoint.browseId). Every radio
    // prefix check and the watch endpoint expect the bare id, so normalise once on the way in
    // rather than stripping "VL" again at each consumer.
    val id = playlistId.removePrefix("VL")
    val isYouTubeLikedMusic = id == "LM"
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentPlaylist =
        currentBackStackEntry?.let { entry ->
            if (entry.destination.hasRoute(PlaylistDestination::class)) {
                entry.toRoute<PlaylistDestination>()
            } else {
                null
            }
        }
    val isCurrentPlaylist =
        currentPlaylist?.let {
            it.playlistId.removePrefix("VL") == id && it.isYourYouTubePlaylist == isYourYouTubePlaylist
        } == true
    val tag = "PlaylistScreen"

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val continuation by viewModel.continuation.collectAsStateWithLifecycle()
    val downloadState by viewModel.downloadState.collectAsStateWithLifecycle()
    val liked by viewModel.liked.collectAsStateWithLifecycle()
    val tracks by viewModel.tracks.collectAsStateWithLifecycle()
    val playlistEntity by viewModel.playlistEntity.collectAsStateWithLifecycle()
    val suggestionState by viewModel.suggestions.collectAsStateWithLifecycle()
    var hasPresentedTracks by remember(id) { mutableStateOf(false) }
    LaunchedEffect(id, tracks.isNotEmpty()) {
        if (tracks.isNotEmpty()) hasPresentedTracks = true
    }
    val tracksListState by viewModel.tracksListState.collectAsStateWithLifecycle()
    val likedMusicRefresh by sharedViewModel.youTubeLikedPlaylistRefresh.collectAsStateWithLifecycle()
    var handledLikedMusicRefreshVersion by remember(id) {
        mutableStateOf(likedMusicRefresh.revision)
    }

    var showSearchBar by rememberSaveable { mutableStateOf(false) }
    val searchFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    val lazyState = rememberLazyListState()
    val searchResultsState = rememberLazyListState()
    val stickyTitle = remember(lazyState, id) { StickyPlaylistTitleState(lazyState, "playlist_hero") }
    val firstItemVisible by remember {
        derivedStateOf {
            lazyState.firstVisibleItemIndex == 0
        }
    }
    var shouldHideTopBar by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    val togglePlaylistSearch = {
        if (showSearchBar || query.isNotBlank()) {
            keyboardController?.hide()
            query = ""
            showSearchBar = false
        } else {
            showSearchBar = true
        }
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
        onKeyboardDismissed = {
            // Keep the submitted query when the IME closes the search field. The close button
            // explicitly clears it; pressing Enter should leave the filtered playlist in place.
            showSearchBar = false
        },
    )
    val selectionState = rememberSongSelectionState()
    val selectionViewModel: SongSelectionViewModel = koinViewModel()
    val libraryBackStackEntry = navController.previousBackStackEntry
    val libraryViewModel =
        if (libraryBackStackEntry?.destination?.hasRoute(LibraryDestination::class) == true) {
            koinViewModel<LibraryViewModel>(viewModelStoreOwner = libraryBackStackEntry)
        } else {
            null
        }
    var showSelectionSheet by rememberSaveable { mutableStateOf(false) }
    var showSelectionAddToPlaylist by rememberSaveable { mutableStateOf(false) }

    val filterQuery = query.trim()
    val isLoadedUserPlaylist =
        isYourYouTubePlaylist &&
            !isYouTubeLikedMusic &&
            uiState.data?.let { it.id.removePrefix("VL") == id && !it.isRadio } == true
    val playlistTrackIds =
        remember(playlistEntity?.tracks, tracks, uiState.data?.trackCount, isLoadedUserPlaylist, suggestionState.tracks) {
            when {
                !isLoadedUserPlaylist || uiState.data?.trackCount == 0 -> emptySet()
                else -> tracks.mapTo(mutableSetOf()) { it.videoId }
            }
        }
    val suggestionTracks = remember(suggestionState, playlistTrackIds) {
        suggestionState.tracks.filterNot { it.videoId in playlistTrackIds }.distinctBy { it.videoId }
    }
    val suggestedVideoIds = remember(suggestionTracks) { suggestionTracks.mapTo(mutableSetOf()) { it.videoId } }
    val visibleTracks = remember(tracks, suggestedVideoIds, playlistTrackIds, isLoadedUserPlaylist, uiState.data?.trackCount) {
        tracks.filterNot {
            LiveStreamType.isLiveStream(it) ||
                it.videoId in suggestedVideoIds ||
                (isLoadedUserPlaylist && uiState.data?.trackCount == 0 && it.videoId !in playlistTrackIds)
        }
    }
    var filteredSearchQuery by remember { mutableStateOf("") }
    var filteredSearchResults by remember { mutableStateOf(emptyList<Track>()) }
    LaunchedEffect(visibleTracks, filterQuery) {
        if (filterQuery.isEmpty()) {
            filteredSearchQuery = ""
            filteredSearchResults = emptyList()
        } else {
            val results =
                withContext(Dispatchers.Default) {
                    visibleTracks.filter {
                        it.title.contains(filterQuery, ignoreCase = true) ||
                            it.artists?.joinToString(", ")?.contains(filterQuery, ignoreCase = true) == true
                    }
                }
            filteredSearchResults = results
            filteredSearchQuery = filterQuery
        }
    }
    val filteredTrack =
        when {
            filterQuery.isEmpty() -> visibleTracks
            filteredSearchQuery == filterQuery -> filteredSearchResults
            else -> emptyList()
        }

    LaunchedEffect(uiState) {
        Logger.d(tag, "uiState hash: ${uiState.hashCode()}")
        Logger.d(tag, "uiState data: ${uiState.data}")
    }

    LaunchedEffect(showSearchBar) {
        if (showSearchBar) {
            // Let the bar settle before starting playlist-wide pagination so loading work cannot
            // compete with the opening motion.
            delay(320)
            viewModel.getFullTracks {}
        }
    }

    val searchHeaderHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + SharedHeaderContentHeight
    // Typed queries have their own list, independent of the artwork and original scroll position.
    SearchResultsScrollEffect(
        state = lazyState,
        searchVisible = showSearchBar && filterQuery.isEmpty(),
        hasQuery = false,
        firstResultIndex = 1,
        headerHeight = searchHeaderHeight,
        scrollOnQueryChange = false,
    )

    val appIsDark = LocalAppIsDarkTheme.current
    val appColorScheme = LocalAppColorScheme.current ?: MaterialTheme.colorScheme
    val headerTextColor = if (appIsDark) Color.White else appColorScheme.onBackground
    val headerSubtextColor =
        if (appIsDark) Color(0xC4FFFFFF) else appColorScheme.onBackground.copy(alpha = 0.7f)
    // The screen content is force-dark over artwork, but header controls should track the user's
    // selected app theme just like the mini-player and navigation bar.
    val headerControlColor = if (appIsDark) Color.White else appColorScheme.onSurface

    val shouldStartPaginate =
        remember {
            derivedStateOf {
                tracksListState != ListState.PAGINATION_EXHAUST &&
                    (
                        lazyState.layoutInfo.visibleItemsInfo
                            .lastOrNull()
                            ?.index ?: -9
                    ) >= (lazyState.layoutInfo.totalItemsCount - 6)
            }
        }

    // Recheck pagination when a continuation finishes as well as when the scroll threshold
    // changes. If the first batch is shorter than the viewport, the threshold stays true after
    // each append; keying only on it leaves the remaining YouTube Music tracks unloaded.
    LaunchedEffect(shouldStartPaginate.value, tracksListState) {
        Logger.d(tag, "shouldStartPaginate: ${shouldStartPaginate.value}")
        Logger.d(tag, "tracksListState: $tracksListState")
        Logger.d(tag, "Continuation: $continuation")
        if (shouldStartPaginate.value && tracksListState == ListState.IDLE) {
            viewModel.getContinuationTrack(
                id,
                continuation,
            )
        }
    }

    val queueData by sharedViewModel.getQueueDataState().collectAsStateWithLifecycle()
    val playingPlaylistId by remember {
        derivedStateOf {
            queueData?.data?.playlistId
        }
    }

    val playingTrack by sharedViewModel.nowPlayingState
        .mapLatest {
            it?.songEntity
        }.collectAsState(initial = null)
    val isPlaying by sharedViewModel.controllerState.map { it.isPlaying }.collectAsState(initial = false)
    val isPlaylistPlaying = isPlaying && playingPlaylistId?.removePrefix("VL") == id
    val canRemoveFromPlaylist = isLoadedUserPlaylist
    val displayedTrackCount =
        uiState.data?.let { data ->
            if (canRemoveFromPlaylist) maxOf(data.trackCount, visibleTracks.size) else data.trackCount
        } ?: 0
    val onPlaylistPlayPause: () -> Unit = {
        if (isPlaylistPlaying) {
            sharedViewModel.onUIEvent(UIEvent.PlayPause)
        } else {
            viewModel.onUIEvent(PlaylistUIEvent.PlayAll)
        }
    }
    val onPlaylistShuffle: () -> Unit = { viewModel.onUIEvent(PlaylistUIEvent.Shuffle) }
    val onPlaylistDownload: () -> Unit = {
        when (downloadState) {
            DownloadState.STATE_DOWNLOADED -> viewModel.makeToast(getStringBlocking(Res.string.downloaded))
            DownloadState.STATE_PREPARING, DownloadState.STATE_DOWNLOADING ->
                viewModel.makeToast(getStringBlocking(Res.string.downloading))
            else -> viewModel.onUIEvent(PlaylistUIEvent.Download)
        }
    }

    var currentItem by remember {
        mutableStateOf<Track?>(null)
    }

    var itemBottomSheetShow by remember {
        mutableStateOf(false)
    }
    var playlistBottomSheetShow by remember {
        mutableStateOf(false)
    }

    val onPlaylistItemClick: (videoId: String) -> Unit = { videoId ->
        viewModel.onUIEvent(
            PlaylistUIEvent.ItemClick(
                videoId = videoId,
            ),
        )
    }
    val onItemMoreClick: (videoId: String) -> Unit = { videoId ->
        if (videoId !in suggestedVideoIds) {
            currentItem = tracks.firstOrNull { it.videoId == videoId }
            if (currentItem != null) {
                itemBottomSheetShow = true
            }
        }
    }
    val onPlaylistMoreClick: () -> Unit = {
        playlistBottomSheetShow = true
    }

    LaunchedEffect(key1 = id) {
        if (id != uiState.data?.id) {
            Logger.w(tag, "new id: $id")
            viewModel.getData(id)
        }
    }
    LaunchedEffect(id, isLoadedUserPlaylist, tracks) {
        if (isLoadedUserPlaylist) viewModel.getSuggestions(id)
    }
    LaunchedEffect(id, likedMusicRefresh.revision) {
        if (isYouTubeLikedMusic && likedMusicRefresh.revision != handledLikedMusicRefreshVersion) {
            handledLikedMusicRefreshVersion = likedMusicRefresh.revision
            viewModel.refreshYouTubeLikedPlaylist(
                videoId = likedMusicRefresh.videoId,
                isLiked = likedMusicRefresh.isLiked,
                currentlyPlayingSong = playingTrack,
            )
        }
    }
    LaunchedEffect(key1 = firstItemVisible) {
        shouldHideTopBar = !firstItemVisible
    }
    val playlistThumbnail = (uiState as? PlaylistUIState.Success)?.data?.thumbnail
    val isEmptyUserPlaylist = isLoadedUserPlaylist && uiState.data?.trackCount == 0
    val currentThumbnail =
        if (isYouTubeLikedMusic) {
            (
                tracks.firstOrNull()?.thumbnails
                    ?.filter { it.url.isNotBlank() }
                    ?.maxByOrNull { it.width.toLong() * it.height }
                    ?.url ?: playlistThumbnail
            )?.toPlaylistCoverThumbnailUrl()
        } else {
            playlistThumbnail?.takeUnless {
                isEmptyUserPlaylist && it.startsWith("http", ignoreCase = true)
            }
        }
    val paletteState = rememberPaletteState()
    val hazeState = rememberHazeState()
    val cachedArtwork = remember(currentThumbnail) {
        PlaylistArtworkCache.get(currentThumbnail)
    }
    val cachedArtworkPainter = cachedArtwork?.bitmap?.let { bitmap ->
        remember(bitmap) { BitmapPainter(bitmap) }
    }
    var artworkBitmap by remember(currentThumbnail) {
        mutableStateOf(
            cachedArtwork?.let { artwork ->
                currentThumbnail?.let { thumbnail -> thumbnail to artwork.bitmap }
            },
        )
    }
    // Restore the sampled colors with the decoded cover when this destination is reopened.
    var cachedArtworkPalette by remember(currentThumbnail) {
        mutableStateOf(cachedArtwork?.palette)
    }
    var paletteGeneratedFor by remember(currentThumbnail) {
        mutableStateOf(currentThumbnail.takeIf { cachedArtwork?.palette != null })
    }
    val likedMusicCoverRequest =
        if (isYouTubeLikedMusic) rememberPlaylistCoverRequest(currentThumbnail) else null

    var artworkLoadFailed by remember(currentThumbnail) { mutableStateOf(false) }
    LaunchedEffect(currentThumbnail) {
        PlaylistArtworkCache.touch(currentThumbnail)
    }
    LaunchedEffect(artworkBitmap, currentThumbnail) {
        val (thumbnail, bm) = artworkBitmap ?: return@LaunchedEffect
        if (thumbnail == currentThumbnail && paletteGeneratedFor != thumbnail) {
            paletteState.generate(bm)
            val palette = paletteState.palette
            if (palette != null) {
                cachedArtworkPalette = palette
                paletteGeneratedFor = thumbnail
                PlaylistArtworkCache.put(thumbnail, bm, palette)
            } else if (paletteState.state is com.kmpalette.PaletteResult.Error) {
                artworkLoadFailed = true
            }
        }
    }

    // Apple Music-inspired immersive treatment. Which header is used depends on the window's
    // aspect ratio alone, not on the platform: a portrait window (a phone held upright, or a
    // narrow desktop window) gets the edge-to-edge artwork header, a landscape one gets the
    // side-by-side header. Everything else on the page — the palette background, the row
    // dividers and the palette-colored scrolling top bar — is shared by both.
    val screenInfo = getScreenSizeInfo()
    val isPortrait = screenInfo.wDP < screenInfo.hDP
    val landscapeHero = onlineLandscapeHeroLayout()
    // Apple Music-style page background from the artwork's dominant tone (see UIExt.toImmersiveBackground).
    // A remembered palette can still belong to the previous playlist while the next cover loads.
    // Only let a palette affect this page after it has been generated from this exact thumbnail.
    val artworkPalette =
        cachedArtworkPalette ?: paletteState.palette.takeIf { paletteGeneratedFor == currentThumbnail }
    val mutedPaletteBg = artworkPalette.toImmersiveBackground(isDark = appIsDark)
    // Coil can display the cover before palette extraction finishes. Reveal both with one
    // progress value so the artwork never arrives ahead of its ambient page color.
    val artworkReveal by key(currentThumbnail) {
        animateFloatAsState(
            targetValue = if (paletteGeneratedFor == currentThumbnail || artworkLoadFailed) 1f else 0f,
            animationSpec = tween(420, easing = FastOutSlowInEasing),
            label = "playlistArtworkReveal",
        )
    }
    val artworkPageBackground =
        if (!isYouTubeLikedMusic && !currentThumbnail.isNullOrBlank()) {
            lerp(if (appIsDark) Color.Black else Color.White, mutedPaletteBg, artworkReveal)
        } else if (!isYouTubeLikedMusic && isEmptyUserPlaylist) {
            // Give the neutral empty-playlist banner an opaque destination for its bottom fade.
            appColorScheme.background
        } else {
            Color.Transparent
        }
    val playlistContentColor = if (appIsDark) Color.White else appColorScheme.onSurface
    val playlistSubtitleColor = if (appIsDark) Color(0xC4FFFFFF) else appColorScheme.onSurfaceVariant
    val dividerColor =
        if (appIsDark) {
            Color.White.copy(alpha = 0.12f)
        } else {
            appColorScheme.outlineVariant.copy(alpha = 0.7f)
        }
    // The cover, header scrim, page surface, and sampled glass tint share this transition.
    val pageChromeTint =
        if (!isYouTubeLikedMusic && !currentThumbnail.isNullOrBlank()) artworkPageBackground else appColorScheme.background
    val playlistSearchBackdrop = rememberBackdrop(pageChromeTint)
    val selectionBackdropHeight =
        with(LocalDensity.current) { WindowInsets.statusBars.getTop(this).toDp() } +
            CompactHeaderContentHeight + HeaderBackdropExtensionHeight
    val pageArtworkBackgroundSink = LocalPageArtworkBackground.current
    SideEffect {
        pageArtworkBackgroundSink?.value = artworkPageBackground.takeUnless { it == Color.Transparent }
    }
    val inlineLandscapeSearch = isCompactLandscapeChrome()
    val playlistSearchField: @Composable () -> Unit = {
        PlaylistSearchBar(
            backdrop = playlistSearchBackdrop,
            query = query,
            onQueryChange = { query = it },
            onSearch = {
                query = query.trim()
                keyboardController?.hide()
            },
            expanded = showSearchBar,
            onExpandedChange = { showSearchBar = it },
            focusRequester = searchFocusRequester,
        )
    }

    // Loading dialog
    val showLoadingDialog by viewModel.showLoadingDialog.collectAsStateWithLifecycle()
    if (showLoadingDialog.first) {
        LoadingDialog(
            true,
            showLoadingDialog.second,
        )
    }
    val playlistReady = (uiState as? PlaylistUIState.Success)?.data != null
    val playlistReveal = animateFloatAsState(
        targetValue = if (playlistReady) 1f else 0f,
        animationSpec = tween(durationMillis = 360, easing = FastOutSlowInEasing),
        label = "Playlist loading reveal",
    )
    // Overlay the compact toolbar on the scrolling list; keep it anchored to the viewport top.
    Box(
        Modifier
            .fillMaxSize()
            .background(artworkPageBackground)
            .consumePointerInputWhen(isYouTubeLikedMusic && !isCurrentPlaylist),
    ) {
    // Fade the single loaded page through a graphics layer. Cached destinations start at full
    // opacity; pagination and metadata changes do not replay the loading reveal.
    when (val state = uiState) {
            is PlaylistUIState.Success -> {
                val data = state.data
                Logger.d(tag, "data: $data")
                if (data == null) return@Box
                Box(
                    Modifier.fillMaxSize().graphicsLayer {
                        alpha = playlistReveal.value
                        translationY = (1f - playlistReveal.value) * 10.dp.toPx()
                    },
                ) {
                FilteredPlaylistList(
                    state = lazyState,
                    searchState = searchResultsState,
                    filtering = filterQuery.isNotEmpty(),
                    completedQuery = filteredSearchQuery,
                    contentPadding = PaddingValues(),
                    searchPadding = PaddingValues(
                        top = searchHeaderHeight + (if (showSearchBar && !inlineLandscapeSearch) InPageSearchBarHeight else 0.dp) + 8.dp,
                    ),
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(artworkPageBackground)
                            .windowInsetsPadding(LocalPageContentInsets.current)
                            .then(stickyTitle.viewportModifier)
                            // The search field and frosted toolbar are sibling overlays sampling
                            // the scrolled playlist and artwork without capturing themselves.
                            .frostedHeaderSource(
                                playlistSearchBackdrop,
                                auroraBackground = pageChromeTint.takeIf { isYouTubeLikedMusic || currentThumbnail.isNullOrBlank() },
                            )
                            // Haze is a source only; the frosted surface itself is bounded to the
                            // compact toolbar, so playlist rows remain sharp.
                            .then(
                                if (LocalLiquidGlassEnabled.current || showSearchBar) {
                                    Modifier.hazeSource(hazeState)
                                } else {
                                    Modifier
                                },
                            ),
                    header = {
                    item(key = "playlist_hero", contentType = "header") {
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
                                        // Keep the artwork header independent from the scrolling list's
                                        // backdrop pipeline. A scrolling backdrop source here can smear
                                        // the playlist rows on some devices while the header moves away.
                                        val artworkBackdrop =
                                            rememberBackdrop(
                                                if (isYouTubeLikedMusic) appColorScheme.background else Color.Black,
                                            )
                                        Box(
                                            modifier =
                                                Modifier
                                                    .fillMaxWidth()
                                                    .then(
                                                        if (isYouTubeLikedMusic) {
                                                            Modifier.wrapContentHeight()
                                                        } else {
                                                            Modifier.height((screenInfo.hDP / 2).dp)
                                                        },
                                                    ),
                                        ) {
                                            // The glass controls below are siblings of this source so the
                                            // backdrop samples the header without creating a render loop.
                                            Box(
                                                modifier =
                                                    Modifier
                                                        .fillMaxSize()
                                                        .then(
                                                            if (isYouTubeLikedMusic && LocalLiquidGlassEnabled.current) {
                                                                // The small liked-song cover leaves aurora behind the controls.
                                                                // Capture that same page background, rather than a solid fill.
                                                                Modifier.frostedHeaderSource(artworkBackdrop, auroraBackground = pageChromeTint)
                                                            } else {
                                                                Modifier.layerBackdrop(artworkBackdrop)
                                                            },
                                                        ),
                                            ) {
                                                if (!isYouTubeLikedMusic) {
                                                    if (isEmptyUserPlaylist) {
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
                                                        placeholder = cachedArtworkPainter ?: painterPlaylistThumbnail(data.title, typo().labelMedium, 250.dp to 250.dp),
                                                        error = cachedArtworkPainter ?: painterPlaylistThumbnail(data.title, typo().labelMedium, 250.dp to 250.dp),
                                                        fallback = cachedArtworkPainter ?: painterPlaylistThumbnail(data.title, typo().labelMedium, 250.dp to 250.dp),
                                                        contentDescription = null,
                                                        contentScale = ContentScale.Crop,
                                                        onSuccess = {
                                                            artworkBitmap =
                                                                currentThumbnail?.let { thumbnail ->
                                                                    thumbnail to it.result.image.toImageBitmap()
                                                                }
                                                        },
                                                        onError = { artworkLoadFailed = true },
                                                        modifier = Modifier.fillMaxSize().graphicsLayer { alpha = artworkReveal },
                                                    )
                                                    // Scrim spans 70% of the artwork (not a fixed 200dp): the
                                                    // shorter the ramp, the steeper the alpha, and a steep ramp
                                                    // is what makes the fade read as an edge. See
                                                    // artworkScrimBrush for the curve itself.
                                                    Box(
                                                        modifier =
                                                            Modifier
                                                                .fillMaxWidth()
                                                                .height((screenInfo.hDP * 0.35f).dp)
                                                                .align(Alignment.BottomCenter)
                                                                .background(artworkScrimBrush(artworkPageBackground)),
                                                    )
                                                }
                                                Column(
                                                    modifier =
                                                        Modifier
                                                            .align(Alignment.BottomCenter)
                                                            .fillMaxWidth()
                                                            .padding(horizontal = 16.dp)
                                                            .padding(
                                                                top =
                                                                    if (isYouTubeLikedMusic) {
                                                                        WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 80.dp
                                                                    } else {
                                                                        0.dp
                                                                    },
                                                                // Reserve the sibling glass action row beneath the metadata.
                                                                bottom = if (isYouTubeLikedMusic) 88.dp else 16.dp,
                                                            ),
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                ) {
                                                    if (isYouTubeLikedMusic) {
                                                        // Leave the glass toolbar its own strip, then show the
                                                        // newest liked track above the playlist's metadata.
                                                        AsyncImage(
                                                            model = likedMusicCoverRequest,
                                                            placeholder = rememberHolderPainter(),
                                                            error = rememberHolderPainter(),
                                                            contentDescription = null,
                                                            contentScale = ContentScale.Crop,
                                                            modifier = Modifier.size(180.dp).clip(RoundedCornerShape(12.dp)),
                                                        )
                                                        Spacer(modifier = Modifier.height(20.dp))
                                                    }
                                                    Text(
                                                        text = data.title,
                                                        modifier = stickyTitle.sourceModifier(if (isYouTubeLikedMusic) typo().titleMedium else typo().titleLarge),
                                                        style = if (isYouTubeLikedMusic) typo().titleMedium else typo().titleLarge,
                                                        color = headerTextColor,
                                                        maxLines = 2,
                                                        textAlign = TextAlign.Center,
                                                    )
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    CompositionLocalProvider(
                                                        LocalMinimumInteractiveComponentSize provides Dp.Unspecified,
                                                    ) {
                                                        TextButton(
                                                            modifier =
                                                                Modifier
                                                                    .wrapContentHeight()
                                                                    .defaultMinSize(minHeight = 1.dp, minWidth = 1.dp),
                                                            contentPadding = PaddingValues(vertical = 1.dp),
                                                            onClick = {
                                                                if (data.author.id.isNotEmpty()) {
                                                                    navController.navigate(
                                                                        ArtistDestination(
                                                                            data.author.id,
                                                                        ),
                                                                    )
                                                                }
                                                            },
                                                        ) {
                                                            Text(
                                                                text = data.author.name,
                                                                style = typo().titleSmall,
                                                                color = headerTextColor,
                                                                textAlign = TextAlign.Center,
                                                            )
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = "${
                                                            if (data.isRadio) {
                                                                stringResource(Res.string.radio)
                                                            } else {
                                                                stringResource(Res.string.playlist)
                                                            }
                                                        } • ${data.year}${
                                                            if (isYouTubeLikedMusic) {
                                                                " • ${pluralStringResource(Res.plurals.n_track, data.trackCount, data.trackCount)}"
                                                            } else {
                                                                ""
                                                            }
                                                        }",
                                                        style = if (isYouTubeLikedMusic) typo().bodySmall else typo().bodyMedium,
                                                        color = headerSubtextColor,
                                                        textAlign = TextAlign.Center,
                                                    )
                                                }
                                            }
                                            if (isYouTubeLikedMusic) {
                                                PlaylistGlassActions(
                                                    backdrop = artworkBackdrop,
                                                    isPlaying = isPlaylistPlaying,
                                                    downloadState = downloadState,
                                                    contentColor = headerControlColor,
                                                    accentColor = appColorScheme.primary,
                                                    onPlayPause = onPlaylistPlayPause,
                                                    onShuffle = onPlaylistShuffle,
                                                    onDownload = onPlaylistDownload,
                                                    modifier =
                                                        Modifier
                                                            .align(Alignment.BottomCenter)
                                                            .padding(horizontal = 16.dp, vertical = 16.dp),
                                                )
                                            }
                                        }
                                    } else {
                                        // Apple Music desktop header: back and the overlay actions on
                                        // their own top row, then a square artwork with the text column
                                        // and the action cluster laid out beside it rather than under it.
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(
                                                start = landscapeHero.horizontalPadding,
                                                end = landscapeHero.horizontalPadding,
                                                top = landscapeHero.topPadding,
                                                bottom = landscapeHero.bottomPadding,
                                            ),
                                            horizontalArrangement = Arrangement.spacedBy(landscapeHero.columnSpacing),
                                            verticalAlignment = Alignment.Top,
                                        ) {
                                            if (!isYouTubeLikedMusic && isEmptyUserPlaylist) {
                                                LocalPlaylistHeroArtwork(
                                                    Modifier
                                                        .size(landscapeHero.coverSize)
                                                        .clip(RoundedCornerShape(8.dp)),
                                                )
                                            } else AsyncImage(
                                                model = if (isYouTubeLikedMusic) likedMusicCoverRequest
                                                    else ImageRequest.Builder(LocalPlatformContext.current)
                                                        .data(currentThumbnail)
                                                        .diskCachePolicy(CachePolicy.ENABLED)
                                                        .memoryCachePolicy(CachePolicy.ENABLED)
                                                        .diskCacheKey(currentThumbnail)
                                                        .memoryCacheKey(currentThumbnail)
                                                        .crossfade(false).build(),
                                                placeholder = cachedArtworkPainter ?: painterPlaylistThumbnail(data.title, typo().labelMedium, 250.dp to 250.dp),
                                                error = cachedArtworkPainter ?: painterPlaylistThumbnail(data.title, typo().labelMedium, 250.dp to 250.dp),
                                                fallback = cachedArtworkPainter ?: painterPlaylistThumbnail(data.title, typo().labelMedium, 250.dp to 250.dp),
                                                contentDescription = if (isYouTubeLikedMusic) "Latest liked song cover" else null,
                                                contentScale = ContentScale.Crop,
                                                onSuccess = {
                                                    artworkBitmap = currentThumbnail?.let { thumbnail ->
                                                        thumbnail to it.result.image.toImageBitmap()
                                                    }
                                                },
                                                onError = { artworkLoadFailed = true },
                                                modifier = Modifier.size(landscapeHero.coverSize)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .graphicsLayer { alpha = if (isYouTubeLikedMusic) 1f else artworkReveal },
                                            )
                                            Column(Modifier.weight(1f)) {
                                                Text(
                                                    text = data.title,
                                                    modifier = Modifier.fillMaxWidth().then(
                                                        stickyTitle.sourceModifier(landscapeHero.titleStyle, color = headerTextColor, textAlign = androidx.compose.ui.text.style.TextAlign.Start),
                                                    ),
                                                    style = landscapeHero.titleStyle,
                                                    color = headerTextColor,
                                                    maxLines = landscapeHero.titleMaxLines,
                                                    overflow = TextOverflow.Ellipsis,
                                                )
                                                Spacer(Modifier.height(4.dp))
                                                Text(
                                                    text = data.author.name,
                                                    style = if (landscapeHero.compact) typo().bodyMedium else typo().titleMedium,
                                                    color = seed,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.fillMaxWidth().clickable {
                                                        if (data.author.id.isNotEmpty()) {
                                                            navController.navigate(ArtistDestination(data.author.id))
                                                        }
                                                    },
                                                )
                                                Spacer(Modifier.height(6.dp))
                                                Text(
                                                    text = "${
                                                        if (data.isRadio) stringResource(Res.string.radio)
                                                        else stringResource(Res.string.playlist)
                                                    } • ${data.year}${
                                                        if (isYouTubeLikedMusic) {
                                                            " • ${pluralStringResource(Res.plurals.n_track, data.trackCount, data.trackCount)}"
                                                        } else ""
                                                    }",
                                                    style = typo().labelMedium,
                                                    color = headerSubtextColor,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                )
                                                Spacer(Modifier.height(landscapeHero.actionSpacing))
                                                if (isYouTubeLikedMusic) {
                                                    // Capture the page aurora separately from the glass controls,
                                                    // matching the portrait actions without a solid black strip.
                                                    GlassBackdropBox(
                                                        backgroundColor = appColorScheme.background,
                                                        auroraBackground = pageChromeTint,
                                                    ) { actionBackdrop ->
                                                        PlaylistGlassActions(
                                                            backdrop = actionBackdrop,
                                                            isPlaying = isPlaylistPlaying,
                                                            downloadState = downloadState,
                                                            contentColor = headerControlColor,
                                                            accentColor = appColorScheme.primary,
                                                            onPlayPause = onPlaylistPlayPause,
                                                            onShuffle = onPlaylistShuffle,
                                                            onDownload = onPlaylistDownload,
                                                        )
                                                    }
                                                } else {
                                                    OnlinePlaylistActions(
                                                        isPlaying = isPlaylistPlaying,
                                                        downloadState = downloadState,
                                                        backgroundColor = artworkPageBackground,
                                                        contentColor = headerControlColor,
                                                        accentColor = appColorScheme.primary,
                                                        showSecondaryActions = !data.isRadio,
                                                        onPlayPause = onPlaylistPlayPause,
                                                        onShuffle = onPlaylistShuffle,
                                                        onDownload = onPlaylistDownload,
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalAlignment = Alignment.Start,
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    if (!isYouTubeLikedMusic) Box(
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .wrapContentHeight(),
                                    ) {
                                        Column(Modifier.padding(horizontal = pageContentHorizontalInset(28.dp))) {
                                            if (isPortrait) {
                                                OnlinePlaylistActions(
                                                    isPlaying = isPlaylistPlaying,
                                                    downloadState = downloadState,
                                                    backgroundColor = artworkPageBackground,
                                                    contentColor = headerControlColor,
                                                    accentColor = appColorScheme.primary,
                                                    showSecondaryActions = !data.isRadio,
                                                    onPlayPause = onPlaylistPlayPause,
                                                    onShuffle = onPlaylistShuffle,
                                                    onDownload = onPlaylistDownload,
                                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                                )
                                            }
                                            val uriHandler = LocalUriHandler.current
                                            val playlistDescription = state.data.description
                                            if (playlistDescription
                                                    ?.trim()
                                                    ?.equals("Created by Wave Music", ignoreCase = true) != true
                                            ) {
                                                DescriptionView(
                                                    modifier =
                                                        Modifier
                                                            .padding(
                                                                top = 8.dp,
                                                            ),
                                                    text =
                                                        playlistDescription.let {
                                                            if (!it.isNullOrEmpty()) {
                                                                it
                                                            } else {
                                                                stringResource(Res.string.no_description)
                                                            }
                                                        },
                                                    textColor = playlistSubtitleColor,
                                                    limitLine = 3,
                                                    onTimeClicked = {},
                                                    onURLClicked = { url ->
                                                        uriHandler.openUri(url)
                                                    },
                                                )
                                            }
                                            Text(
                                                text =
                                                    if (data.isRadio) {
                                                        stringResource(Res.string.unlimited)
                                                    } else {
                                                        pluralStringResource(
                                                            Res.plurals.n_track,
                                                            displayedTrackCount,
                                                            displayedTrackCount,
                                                        )
                                                    },
                                                color = headerSubtextColor,
                                                style = typo().bodyMedium,
                                                modifier = Modifier.padding(vertical = 8.dp),
                                            )
                                        }
                                    }

                            }
                        }
                        }
                    }
                    },
                    rows = { filtered ->
                    val displayedTracks = if (filtered) filteredTrack else visibleTracks
                    items(count = displayedTracks.size, key = { index ->
                        val item = displayedTracks.getOrNull(index)
                        item?.videoId ?: "missing_item_$index"
                    }) { index ->
                        val item = displayedTracks.getOrNull(index)
                        if (item != null) {
                            Column(
                                modifier =
                                    if (hasPresentedTracks && query.isNotBlank()) {
                                        Modifier.animateItem(
                                            fadeInSpec = null,
                                            placementSpec = null,
                                            fadeOutSpec = null,
                                        )
                                    } else if (hasPresentedTracks) {
                                        Modifier.animateItem(placementSpec = null)
                                    } else {
                                        Modifier
                                    },
                            ) {
                                if (playingTrack?.videoId == item.videoId && isPlaying) {
                                    SongFullWidthItems(
                                        forceDark = appIsDark,
                                        contentColorOverride = playlistContentColor,
                                        subtitleColorOverride = playlistSubtitleColor,
                                        isPlaying = true,
                                        showFavoriteButton = true,
                                        track = item,
                                        startContentPadding = pageContentHorizontalInset(3.dp),
                                        endContentPadding = pageContentHorizontalInset(11.dp),
                                        onMoreClickListener = { onItemMoreClick(it) },
                                        onClickListener = {
                                            Logger.w("PlaylistScreen", "index: $index")
                                            onPlaylistItemClick(it)
                                        },
                                        onAddToQueue = {
                                            sharedViewModel.addListToQueue(
                                                arrayListOf(item),
                                            )
                                        },
                                        onRemoveFromPlaylist =
                                            if (canRemoveFromPlaylist && item.videoId !in suggestedVideoIds) {
                                                { videoId -> viewModel.removeYourYouTubePlaylistItem(id, videoId) }
                                            } else {
                                                null
                                            },
                                        selectionMode = selectionState.isActive,
                                        isSelected = selectionState.isSelected(item.videoId),
                                        onLongClick = { selectionState.start(it) },
                                        onSelectToggle = { selectionState.toggle(it) },
                                        modifier = Modifier,
                                    )
                                } else {
                                    SongFullWidthItems(
                                        forceDark = appIsDark,
                                        contentColorOverride = playlistContentColor,
                                        subtitleColorOverride = playlistSubtitleColor,
                                        isPlaying = false,
                                        showFavoriteButton = true,
                                        track = item,
                                        startContentPadding = pageContentHorizontalInset(3.dp),
                                        endContentPadding = pageContentHorizontalInset(11.dp),
                                        onMoreClickListener = { onItemMoreClick(it) },
                                        onClickListener = {
                                            Logger.w("PlaylistScreen", "index: $index")
                                            onPlaylistItemClick(it)
                                        },
                                        onAddToQueue = {
                                            sharedViewModel.addListToQueue(
                                                arrayListOf(item),
                                            )
                                        },
                                        onRemoveFromPlaylist =
                                            if (canRemoveFromPlaylist && item.videoId !in suggestedVideoIds) {
                                                { videoId -> viewModel.removeYourYouTubePlaylistItem(id, videoId) }
                                            } else {
                                                null
                                            },
                                        selectionMode = selectionState.isActive,
                                        isSelected = selectionState.isSelected(item.videoId),
                                        onLongClick = { selectionState.start(it) },
                                        onSelectToggle = { selectionState.toggle(it) },
                                        modifier = Modifier,
                                    )
                                }
                                if (index < displayedTracks.size - 1) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(start = 68.dp, end = 12.dp),
                                        thickness = 0.5.dp,
                                        color = dividerColor,
                                    )
                                }
                            }
                        }
                    }
                    if (!filtered && canRemoveFromPlaylist && (suggestionTracks.isNotEmpty() || suggestionState.isLoading)) {
                        item(key = "playlist_suggestions", contentType = "playlist_suggestions") {
                            Column(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = pageContentHorizontalInset(12.dp)),
                            ) {
                                Spacer(Modifier.height(24.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = stringResource(Res.string.suggestions),
                                        color = playlistContentColor,
                                        style = typo().titleLarge,
                                        modifier = Modifier.weight(1f),
                                    )
                                    if (suggestionState.reloadParams != null) {
                                        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
                                            TextButton(
                                                onClick = viewModel::reloadSuggestions,
                                                modifier = Modifier.padding(horizontal = 8.dp),
                                            ) {
                                                Text(
                                                    text = stringResource(Res.string.reload),
                                                    color = playlistContentColor,
                                                )
                                            }
                                        }
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                                if (suggestionTracks.isEmpty() && suggestionState.isLoading) {
                                    CenterLoadingBox(
                                        modifier = Modifier.fillMaxWidth().height(120.dp),
                                    )
                                } else {
                                    suggestionTracks.forEach { track ->
                                        SuggestItems(
                                            forceDark = appIsDark,
                                            contentColorOverride = appColorScheme.onSurface,
                                            subtitleColorOverride = appColorScheme.onSurfaceVariant,
                                            track = track,
                                            isPlaying = playingTrack?.videoId == track.videoId && isPlaying,
                                            onAddClickListener = { viewModel.addSuggestedTrackToPlaylist(id, track) },
                                            onClickListener = { viewModel.playSuggestedTrack(track, suggestionTracks) },
                                        )
                                    }
                                }
                                Spacer(Modifier.height(12.dp))
                                HorizontalDivider(color = dividerColor, thickness = 0.5.dp)
                                Spacer(Modifier.height(8.dp))
                            }
                        }
                    }
                    when (tracksListState) {
                        ListState.IDLE -> {
                            // DO NOTHING
                            item {
                                EndOfPage()
                            }
                        }

                        ListState.LOADING, ListState.PAGINATING -> {
                            item {
                                Box(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth(),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    CenterLoadingBox(
                                        modifier = Modifier.size(80.dp),
                                    )
                                }
                            }
                            item {
                                EndOfPage()
                            }
                        }

                        ListState.ERROR -> {
                            item(contentType = "pagination_error") {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Text(
                                        text = stringResource(Res.string.error),
                                        style = typo().bodyMedium,
                                    )
                                    TextButton(
                                        onClick = {
                                            viewModel.getContinuationTrack(id, continuation)
                                        },
                                    ) {
                                        Text(stringResource(Res.string.retry))
                                    }
                                }
                            }
                            item {
                                EndOfPage()
                            }
                        }

                        ListState.PAGINATION_EXHAUST -> {
                            item {
                                EndOfPage()
                            }
                        }
                    }
                    },
                )

                AnimatedVisibility(
                    modifier =
                        Modifier
                            .align(Alignment.TopCenter)
                            .windowInsetsPadding(LocalPageContentInsets.current)
                            .padding(
                                top =
                                    searchHeaderHeight,
                            ),
                    visible = showSearchBar && !inlineLandscapeSearch,
                    enter = SearchBarEnter,
                    exit = SearchBarExit,
                ) {
                    playlistSearchField()
                }

                if (showSelectionSheet) {
                    val selectedIds = selectionState.selected.toList()
                    val selectedPlaylistTrackIds = selectedIds.filter {
                        it in playlistTrackIds && it !in suggestedVideoIds
                    }
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
                            if (canRemoveFromPlaylist && selectedPlaylistTrackIds.isNotEmpty()) {
                                listOf(
                                    SongSelectionAction(
                                        icon = SimpIcons.Delete,
                                        label = removeLabel,
                                        tint = Color.Red,
                                    ) {
                                        viewModel.removeYourYouTubePlaylistItems(id, selectedPlaylistTrackIds)
                                        selectionState.exit()
                                    },
                                )
                            } else {
                                emptyList()
                            },
                    )
                }
                if (showSelectionAddToPlaylist) {
                    val selectedIds = selectionState.selected.toList()
                    val localPlaylists by selectionViewModel.listLocalPlaylist.collectAsStateWithLifecycle()
                    val availableSongs = (filteredTrack.map { it.toSongEntity() }).filter { it.videoId in selectedIds }
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
                    currentItem?.toSongEntity()?.let { track ->
                        NowPlayingBottomSheet(
                            onRemoveFromPlaylist =
                                if (canRemoveFromPlaylist && currentItem?.videoId?.let { it !in suggestedVideoIds } == true) {
                                    { viewModel.removeYourYouTubePlaylistItem(id, track.videoId) }
                                } else {
                                    null
                                },
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
                }
                if (playlistBottomSheetShow) {
                    Logger.w("PlaylistScreen", "PlaylistBottomSheet")
                    val addToQueue = {
                        viewModel.getFullTracks { track ->
                            sharedViewModel.addListToQueue(
                                track.toCollection(arrayListOf()),
                            )
                        }
                    }
                    PlaylistBottomSheet(
                        onDismiss = { playlistBottomSheetShow = false },
                        playlistId = data.id,
                        playlistName = data.title,
                        isYourYouTubePlaylist = isYourYouTubePlaylist && !data.isRadio,
                        onSaveToLocal = {
                            viewModel.getFullTracks { track ->
                                viewModel.saveToLocal(track)
                            }
                        },
                        onEditTitle = { newTitle ->
                            viewModel.updatePlaylistTitle(newTitle, data.id)
                        },
                        onDelete = {
                            viewModel.deleteYourYouTubePlaylist(data.id) {
                                libraryViewModel?.removeYouTubePlaylistFromCache(data.id)
                                navController.navigateUp()
                            }
                        },
                        onAddToQueue = if (data.isRadio) null else addToQueue,
                    )
                }
                PlaylistPageHeader(
                    title = data.title,
                    showTitle = stickyTitle.isDocked || showSearchBar || filterQuery.isNotEmpty(),
                    stickyTitle = stickyTitle,
                    hazeState = hazeState,
                    backdrop = playlistSearchBackdrop,
                    tint = pageChromeTint,
                    contentColor = headerControlColor,
                    onBack = { navController.navigateUp() },
                    onSearch = togglePlaylistSearch,
                    onMore = onPlaylistMoreClick,
                    searchActive = showSearchBar || query.isNotBlank(),
                    searchContent = playlistSearchField.takeIf { inlineLandscapeSearch },
                    searchVisible = showSearchBar && inlineLandscapeSearch,
                    selectionContent = {
                        SongSelectionTopAppBar(
                            state = selectionState,
                            onSelectAll = {
                                selectionState.toggleSelectAll(filteredTrack.map { it.videoId })
                            },
                            onOpenActions = { showSelectionSheet = true },
                            backdrop = playlistSearchBackdrop,
                            windowInsets = WindowInsets(0),
                        )
                    },
                    selectionActive = selectionState.isActive,
                    showSurface = false,
                    modifier = Modifier.align(Alignment.TopCenter).windowInsetsPadding(LocalPageContentInsets.current),
                    extraActions = {
                            if (!data.isRadio && !isYouTubeLikedMusic) {
                                Box(
                                    modifier = Modifier.size(headerGlassControlSize()),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    HeartCheckBox(
                                        size = 32,
                                        checked = liked,
                                        tint = headerControlColor,
                                        onStateChange = {
                                            viewModel.onUIEvent(PlaylistUIEvent.Favorite)
                                        },
                                    )
                                }
                            }
                        },
                    )
                }
            }

            is PlaylistUIState.Loading -> Unit

            is PlaylistUIState.Error -> {
                viewModel.makeToast("Error: ${state.message}")
                navController.navigateUp()
            }
    }
        // Keep just the lightweight loader around while it fades over the arriving page.
        AnimatedVisibility(
            visible = uiState is PlaylistUIState.Loading,
            modifier = Modifier.align(Alignment.Center),
            enter = fadeIn(tween(160)),
            exit = fadeOut(tween(160)),
        ) {
            CenterLoadingBox(modifier = Modifier.size(80.dp))
        }
    }
}
