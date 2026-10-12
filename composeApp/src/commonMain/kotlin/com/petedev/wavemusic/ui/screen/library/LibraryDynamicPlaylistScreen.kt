package com.petedev.wavemusic.ui.screen.library

import com.petedev.wavemusic.ui.component.rememberPageListState

import androidx.compose.foundation.clickable

import com.petedev.wavemusic.ui.component.pageContentHorizontalInset
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.border
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import com.petedev.wavemusic.ui.component.AmoledAlertDialog
import com.petedev.wavemusic.ui.component.ActionButton
import com.petedev.wavemusic.ui.component.AppDialogTheme
import com.petedev.wavemusic.ui.component.DialogTextButton as TextButton
import com.petedev.wavemusic.ui.component.EndOfModalBottomSheet
import com.petedev.wavemusic.ui.component.PageTopAppBar
import com.petedev.wavemusic.ui.component.StickyPlaylistTitleState
import com.petedev.wavemusic.ui.component.pageHeaderContentHeight
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.toRoute
import com.petedev.wavemusic.common.Config
import com.petedev.wavemusic.domain.data.entities.ArtistEntity
import com.petedev.wavemusic.domain.data.entities.SongEntity
import com.petedev.wavemusic.domain.extension.now
import com.petedev.wavemusic.domain.mediaservice.handler.PlaylistType
import com.petedev.wavemusic.domain.mediaservice.handler.QueueData
import com.petedev.wavemusic.domain.manager.DataStoreManager.Values.TRUE
import com.petedev.wavemusic.domain.utils.LocalResource
import com.petedev.wavemusic.domain.utils.toArrayListTrack
import com.petedev.wavemusic.domain.utils.toTrack
import com.petedev.wavemusic.extension.getStringBlocking
import com.petedev.wavemusic.extension.consumePointerInputWhen
import com.petedev.wavemusic.ui.component.SearchBarExit
import com.petedev.wavemusic.ui.component.SearchBarEnter
import com.petedev.wavemusic.ui.component.AddToPlaylistModalBottomSheet
import com.petedev.wavemusic.ui.component.ArtistFullWidthItems
import com.petedev.wavemusic.ui.component.FrostedHeader
import com.petedev.wavemusic.ui.component.FavoritePlaylistHero
import com.petedev.wavemusic.ui.component.ArtistLikedSongsHero
import com.petedev.wavemusic.ui.component.DownloadedPlaylistHero
import com.petedev.wavemusic.ui.component.LocalSharedHeaderHosted
import com.petedev.wavemusic.ui.component.LocalSharedHeaderState
import com.petedev.wavemusic.ui.component.SLIDE_V2_FADE_OUT_MILLIS
import com.petedev.wavemusic.ui.component.HeaderScrollDirectionTracker
import com.petedev.wavemusic.ui.component.HeaderScrollOffsetTracker
import com.petedev.wavemusic.ui.component.frostedHeaderSource
import com.petedev.wavemusic.ui.component.NowPlayingBottomSheet
import com.petedev.wavemusic.ui.component.PlaylistFullWidthItems
import com.petedev.wavemusic.ui.component.RippleIconButton
import com.petedev.wavemusic.ui.component.SearchBarBackHandler
import com.petedev.wavemusic.ui.component.SearchBarScrollDismissEffect
import com.petedev.wavemusic.ui.component.SearchResultsScrollEffect
import com.petedev.wavemusic.ui.component.FilteredPlaylistList
import com.petedev.wavemusic.ui.component.EndOfPage
import com.petedev.wavemusic.ui.component.InPageSearchBarHeight
import com.petedev.wavemusic.ui.component.PlaylistSearchBar
import com.petedev.wavemusic.ui.component.AnimatedLandscapeHeaderSearch
import com.petedev.wavemusic.ui.component.headerGlassControlSize
import com.petedev.wavemusic.ui.component.isCompactLandscapeChrome
import com.petedev.wavemusic.ui.component.dialogMotionSource
import com.petedev.wavemusic.ui.component.rememberSurfaceDarkColors
import com.petedev.wavemusic.ui.component.SongFullWidthItems
import com.petedev.wavemusic.ui.component.selection.SelectedSongsBottomSheet
import com.petedev.wavemusic.ui.component.selection.SongSelectionTopAppBar
import com.petedev.wavemusic.ui.component.selection.rememberSongSelectionState
import com.petedev.wavemusic.ui.icon.ArrowBackIosNew
import com.petedev.wavemusic.ui.icon.Close
import com.petedev.wavemusic.ui.icon.CheckCircle
import com.petedev.wavemusic.ui.icon.History
import com.petedev.wavemusic.ui.icon.PlayArrow
import com.petedev.wavemusic.ui.icon.PlayCircle
import com.petedev.wavemusic.ui.icon.KeyboardArrowDown
import com.petedev.wavemusic.ui.icon.Search
import com.petedev.wavemusic.ui.icon.Shuffle
import com.petedev.wavemusic.ui.icon.WaveIcons
import com.petedev.wavemusic.ui.navigation.destination.list.AlbumDestination
import com.petedev.wavemusic.ui.navigation.destination.list.ArtistDestination
import com.petedev.wavemusic.ui.navigation.destination.library.LibraryDynamicPlaylistDestination
import com.petedev.wavemusic.ui.navigation.destination.home.SettingsDestination
import com.petedev.wavemusic.ui.screen.home.analytics.monthFullName
import com.petedev.wavemusic.ui.theme.typo
import com.petedev.wavemusic.ui.theme.LocalLiquidGlassEnabled
import com.petedev.wavemusic.ui.theme.LocalAppIsDarkTheme
import com.petedev.wavemusic.viewModel.AnalyticsUiState
import com.petedev.wavemusic.viewModel.AnalyticsViewModel
import com.petedev.wavemusic.viewModel.LibraryDynamicPlaylistViewModel
import com.petedev.wavemusic.viewModel.MostPlayedPeriod
import com.petedev.wavemusic.viewModel.SongSelectionViewModel
import com.petedev.wavemusic.viewModel.SharedViewModel
import com.petedev.wavemusic.viewModel.UIEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import com.petedev.wavemusic.expect.ui.rememberBackdrop
import com.petedev.wavemusic.ui.screen.home.analytics.formatNumericSpan
import com.petedev.wavemusic.ui.screen.home.analytics.labelRes
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import com.petedev.wavemusic.resources.Res
import com.petedev.wavemusic.resources.album_length
import com.petedev.wavemusic.resources.artists
import com.petedev.wavemusic.resources.all_time
import com.petedev.wavemusic.resources.downloaded
import com.petedev.wavemusic.resources.date_range
import com.petedev.wavemusic.resources.favorite
import com.petedev.wavemusic.resources.followed
import com.petedev.wavemusic.resources.liked_songs
import com.petedev.wavemusic.resources.n_track
import com.petedev.wavemusic.resources.n_plays
import com.petedev.wavemusic.resources.lower_plays
import com.petedev.wavemusic.resources.most_played
import com.petedev.wavemusic.resources.most_played_enable_local_tracking
import com.petedev.wavemusic.resources.settings
import com.petedev.wavemusic.resources.playlist
import com.petedev.wavemusic.resources.search
import com.petedev.wavemusic.resources.remove_all_downloads_confirmation
import com.petedev.wavemusic.resources.seconds
import com.petedev.wavemusic.resources.today
import com.petedev.wavemusic.resources.this_month
import com.petedev.wavemusic.resources.this_week
import com.petedev.wavemusic.resources.this_year
import com.petedev.wavemusic.resources.wrapped
import com.petedev.wavemusic.resources.wrapped_recap_month
import com.petedev.wavemusic.resources.wrapped_recap_month_year
import com.petedev.wavemusic.resources.wrapped_recap_subtitle
import com.petedev.wavemusic.resources.your_top_albums
import com.petedev.wavemusic.resources.your_top_artists
import com.petedev.wavemusic.resources.your_top_tracks
import com.petedev.wavemusic.resources.warning
import com.petedev.wavemusic.resources.yes
import com.petedev.wavemusic.resources.cancel

@Composable
@ExperimentalMaterial3Api
fun LibraryDynamicPlaylistScreen(
    innerPadding: PaddingValues,
    navController: NavController,
    type: String,
    sharedHeaderHeight: Dp,
    headerTint: Color,
    viewModel: LibraryDynamicPlaylistViewModel = koinViewModel(),
    sharedViewModel: SharedViewModel = koinInject(),
    slideV2TitleAlpha: State<Float>? = null,
    freezeStickyTitle: Boolean = false,
) {
    if (type == CONTINUE_LISTENING_ROUTE_TYPE) {
        ContinueListeningScreen(innerPadding, navController, headerTint, sharedViewModel)
        return
    }
    if (type == YOUR_EPISODES_ROUTE_TYPE) {
        YourEpisodesScreen(innerPadding, navController, headerTint, viewModel, sharedViewModel)
        return
    }
    val dynamicType = remember(type) { LibraryDynamicPlaylistType.toType(type) }
    val hasPlaylistHero =
        dynamicType == LibraryDynamicPlaylistType.Favorite ||
            dynamicType == LibraryDynamicPlaylistType.Downloaded ||
            dynamicType is LibraryDynamicPlaylistType.ArtistLiked
    val hasStickyTitleHandoff =
        dynamicType == LibraryDynamicPlaylistType.Favorite ||
            dynamicType == LibraryDynamicPlaylistType.Downloaded ||
            dynamicType is LibraryDynamicPlaylistType.ArtistLiked
    val onArtistTitleClick: (() -> Unit)? =
        (dynamicType as? LibraryDynamicPlaylistType.ArtistLiked)?.let { artist ->
            { navController.navigate(ArtistDestination(channelId = artist.channelId)) }
        }
    val useGlassHeaderButtons = LocalLiquidGlassEnabled.current
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDynamicPlaylistType =
        currentBackStackEntry?.let { entry ->
            if (entry.destination.hasRoute(LibraryDynamicPlaylistDestination::class)) {
                entry.toRoute<LibraryDynamicPlaylistDestination>().type
            } else {
                null
            }
        }
    // AnalyticsViewModel starts several database queries during init. Most Played and the other
    // local playlists do not use any of them, so only create it for analytics-backed routes.
    val analyticsViewModel =
        if (
            dynamicType is LibraryDynamicPlaylistType.TopTracks ||
                dynamicType is LibraryDynamicPlaylistType.TopArtists ||
                dynamicType is LibraryDynamicPlaylistType.TopAlbums
        ) {
            koinViewModel<AnalyticsViewModel>()
        } else {
            null
        }
    val nowPlayingVideoId by viewModel.nowPlayingVideoId.collectAsStateWithLifecycle()
    val localTrackingEnabled by remember(sharedViewModel) {
        sharedViewModel.getLocalTrackingEnabled().map { it == TRUE }
    }.collectAsStateWithLifecycle(initialValue = false)

    var chosenSong: SongEntity? by remember { mutableStateOf(null) }
    var showBottomSheet by rememberSaveable { mutableStateOf(false) }
    var showSearchBar by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    var completedSearchQuery by remember { mutableStateOf("") }
    val lazyState = rememberPageListState()
    val searchResultsState = rememberPageListState()
    val headerHideAfterDockPx = with(LocalDensity.current) { 240.dp.toPx() }
    val headerDirectionThresholdPx = with(LocalDensity.current) { 8.dp.toPx() }
    val headerDirectionTracker = remember(lazyState, dynamicType, headerDirectionThresholdPx) {
        HeaderScrollDirectionTracker(headerDirectionThresholdPx)
    }
    val sharedHeaderState = LocalSharedHeaderState.current
    val fallbackPageTitleTransitionAlpha by animateFloatAsState(
        targetValue =
            if (hasStickyTitleHandoff && sharedHeaderState?.pageTransitionInProgress == true) {
                0f
            } else {
                1f
            },
        animationSpec = tween(SLIDE_V2_FADE_OUT_MILLIS, easing = FastOutSlowInEasing),
        label = "pageTitleTransitionAlpha",
    )
    var hideHeader by remember(lazyState, dynamicType) { mutableStateOf(false) }
    val heroKey = when (dynamicType) {
        LibraryDynamicPlaylistType.Favorite -> "favorite_playlist_hero"
        LibraryDynamicPlaylistType.Downloaded -> "downloaded_playlist_hero"
        LibraryDynamicPlaylistType.MostPlayed -> "most_played_period_selector"
        is LibraryDynamicPlaylistType.ArtistLiked -> "artist_liked_hero"
        else -> "unused_playlist_hero"
    }
    val stickyTitle = remember(lazyState, dynamicType) { StickyPlaylistTitleState(lazyState, heroKey) }
    val searchFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

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
        onKeyboardDismissed = { showSearchBar = false },
    )
    val searchHeaderHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + pageHeaderContentHeight()
    val headerHeightPx = with(LocalDensity.current) { searchHeaderHeight.roundToPx() }
    val titleHandoffDistancePx = with(LocalDensity.current) { 8.dp.roundToPx() }
    val showPlaylistTitle by remember(hasPlaylistHero, stickyTitle, useGlassHeaderButtons) {
        derivedStateOf {
            query.isNotBlank() || !hasPlaylistHero ||
                if (useGlassHeaderButtons) stickyTitle.isDocked else stickyTitle.isSourceOutOfViewport
        }
    }
    val showScrolledHeaderSurface by remember(lazyState) {
        derivedStateOf {
            lazyState.firstVisibleItemIndex > 0 || lazyState.firstVisibleItemScrollOffset > 0
        }
    }
    // Typed queries use a separate list; only reveal the playlist when its empty field opens.
    SearchResultsScrollEffect(
        state = lazyState,
        searchVisible = showSearchBar && query.isBlank(),
        hasQuery = false,
        firstResultIndex = if (hasPlaylistHero || dynamicType == LibraryDynamicPlaylistType.MostPlayed) 3 else 2,
        headerHeight = searchHeaderHeight,
        scrollOnQueryChange = false,
    )

    val selectionState = rememberSongSelectionState()
    val selectionViewModel: SongSelectionViewModel = koinViewModel()
    var showSelectionSheet by rememberSaveable { mutableStateOf(false) }
    var showSelectionAddToPlaylist by rememberSaveable { mutableStateOf(false) }
    var showRemoveAllDownloadsDialog by rememberSaveable { mutableStateOf(false) }

    // The hosted header includes the status-bar inset; use its full height so it can clear the
    // status bar completely after the playlist title has handed off to the toolbar.
    val sharedHeaderHeightPx = with(LocalDensity.current) { sharedHeaderHeight.toPx() }
    val flatHeader = !LocalLiquidGlassEnabled.current

    LaunchedEffect(
        lazyState,
        stickyTitle,
        headerHideAfterDockPx,
        if (flatHeader) false else showPlaylistTitle,
        showSearchBar,
        selectionState.isActive,
        flatHeader,
    ) {
        val offsetTracker = HeaderScrollOffsetTracker()
        if (showSearchBar || selectionState.isActive) {
            sharedHeaderState?.resetHeaderOffset()
        } else {
            snapshotFlow {
                val index = lazyState.firstVisibleItemIndex
                val offset = lazyState.firstVisibleItemScrollOffset
                val distancePastDockPx =
                    if (hasPlaylistHero) {
                        stickyTitle.distancePastDockPx
                    } else if (index > 2) {
                        headerHideAfterDockPx
                    } else if (index == 2) {
                        offset.toFloat()
                    } else {
                        0f
                    }
                Triple(index, offset, distancePastDockPx)
            }.collect { (index, offset, distancePastDockPx) ->
                if (flatHeader) {
                    sharedHeaderState?.updateHeaderOffset(
                        offsetTracker.update(
                            index = index,
                            offset = offset,
                            currentHeaderOffsetPx = sharedHeaderState?.headerOffsetPx ?: 0f,
                            maxHeightPx = sharedHeaderHeightPx,
                        ),
                    )
                } else if (!showPlaylistTitle || (index == 0 && offset == 0) || distancePastDockPx < headerHideAfterDockPx) {
                    sharedHeaderState?.resetHeaderOffset()
                } else {
                    val newOffset = offsetTracker.update(
                        index = index,
                        offset = offset,
                        currentHeaderOffsetPx = sharedHeaderState?.headerOffsetPx ?: 0f,
                        maxHeightPx = sharedHeaderHeightPx,
                        forceShowAtTop = false,
                    )
                    sharedHeaderState?.updateHeaderOffset(newOffset)
                }
            }
        }
    }

    val favorite by viewModel.listFavoriteSong.collectAsStateWithLifecycle()
    var tempFavorite by remember { mutableStateOf(emptyList<SongEntity>()) }
    val followed by viewModel.listFollowedArtist.collectAsStateWithLifecycle()
    var tempFollowed by remember { mutableStateOf(emptyList<ArtistEntity>()) }
    val mostPlayed by viewModel.listMostPlayedSong.collectAsStateWithLifecycle()
    val mostPlayedPlayCounts by viewModel.mostPlayedPlayCounts.collectAsStateWithLifecycle()
    val mostPlayedPeriod by viewModel.mostPlayedPeriod.collectAsStateWithLifecycle()
    var tempMostPlayed by remember { mutableStateOf(emptyList<SongEntity>()) }
    val downloaded by viewModel.listDownloadedSong.collectAsStateWithLifecycle()
    var tempDownloaded by remember { mutableStateOf(emptyList<SongEntity>()) }
    val monthlyRecap by viewModel.listMonthlyRecapSong.collectAsStateWithLifecycle()
    var tempMonthlyRecap by remember { mutableStateOf(emptyList<SongEntity>()) }
    val artistLiked by viewModel.listArtistLikedSong.collectAsStateWithLifecycle()
    val artistLikedName by viewModel.artistLikedName.collectAsStateWithLifecycle()
    val artistLikedThumbnail by viewModel.artistLikedThumbnail.collectAsStateWithLifecycle()
    var tempArtistLiked by remember { mutableStateOf(emptyList<SongEntity>()) }
    val analyticsState =
        if (analyticsViewModel != null) {
            analyticsViewModel.analyticsUIState.collectAsStateWithLifecycle()
        } else {
            remember { mutableStateOf(AnalyticsUiState()) }
        }
    val analyticsUIState by analyticsState
    var tempTopTracks by remember { mutableStateOf(analyticsUIState.topTracks.data ?: emptyList()) }
    var tempTopArtists by remember { mutableStateOf(analyticsUIState.topArtists.data ?: emptyList()) }
    var tempTopAlbums by remember { mutableStateOf(analyticsUIState.topAlbums.data ?: emptyList()) }
    val routeHasRows =
        when (dynamicType) {
            LibraryDynamicPlaylistType.Favorite -> favorite.isNotEmpty()
            LibraryDynamicPlaylistType.Followed -> followed.isNotEmpty()
            LibraryDynamicPlaylistType.MostPlayed -> mostPlayed.isNotEmpty()
            LibraryDynamicPlaylistType.Downloaded -> downloaded.isNotEmpty()
            is LibraryDynamicPlaylistType.MonthlyRecap -> monthlyRecap.isNotEmpty()
            is LibraryDynamicPlaylistType.ArtistLiked -> artistLiked.isNotEmpty()
            is LibraryDynamicPlaylistType.TopTracks -> !analyticsUIState.topTracks.data.isNullOrEmpty()
            is LibraryDynamicPlaylistType.TopArtists -> !analyticsUIState.topArtists.data.isNullOrEmpty()
            is LibraryDynamicPlaylistType.TopAlbums -> !analyticsUIState.topAlbums.data.isNullOrEmpty()
        }
    var hasPresentedRows by remember(type) { mutableStateOf(false) }
    LaunchedEffect(routeHasRows) {
        if (routeHasRows) hasPresentedRows = true
    }
    val hazeState = rememberHazeState()
    val searchBackdrop = rememberBackdrop(headerTint)
    val inlineLandscapeSearch = isCompactLandscapeChrome()
    val playlistSearchField: @Composable () -> Unit = {
        PlaylistSearchBar(
            backdrop = searchBackdrop,
            query = query,
            onQueryChange = { query = it },
            onSearch = {
                keyboardController?.hide()
                showSearchBar = false
            },
            expanded = showSearchBar,
            onExpandedChange = { showSearchBar = it },
            focusRequester = searchFocusRequester,
        )
    }

    // Load only the list this route displays. The previous eager setup opened four Room flows and
    // initialized analytics at once, competing with the page transition and first frame.
    LaunchedEffect(type) {
        viewModel.observe(dynamicType)
        if (dynamicType is LibraryDynamicPlaylistType.MonthlyRecap) {
            viewModel.getMonthlyRecapSong(dynamicType)
        }
        if (dynamicType is LibraryDynamicPlaylistType.ArtistLiked) {
            viewModel.getArtistLikedSong(dynamicType)
        }
        // A top list opened from an older period. This screen gets its own analytics view model,
        // which starts on the latest period, so without this every older period opened as the
        // newest one.
        dynamicType.topListRange()?.let { (start, end) -> analyticsViewModel?.showRange(start, end) }
    }

    LaunchedEffect(
        query,
        favorite,
        followed,
        mostPlayed,
        downloaded,
        monthlyRecap,
        artistLiked,
        analyticsUIState,
    ) {
        when (dynamicType) {
            LibraryDynamicPlaylistType.Favorite ->
                tempFavorite = withContext(Dispatchers.Default) { favorite.filter { it.matches(query) } }
            LibraryDynamicPlaylistType.Followed ->
                tempFollowed = withContext(Dispatchers.Default) {
                    followed.filter { it.name.contains(query, ignoreCase = true) }
                }

            LibraryDynamicPlaylistType.MostPlayed ->
                tempMostPlayed = withContext(Dispatchers.Default) { mostPlayed.filter { it.matches(query) } }
            LibraryDynamicPlaylistType.Downloaded ->
                tempDownloaded = withContext(Dispatchers.Default) { downloaded.filter { it.matches(query) } }
            is LibraryDynamicPlaylistType.MonthlyRecap ->
                tempMonthlyRecap = withContext(Dispatchers.Default) { monthlyRecap.filter { it.matches(query) } }

            is LibraryDynamicPlaylistType.ArtistLiked ->
                tempArtistLiked = withContext(Dispatchers.Default) { artistLiked.filter { it.matches(query) } }

            is LibraryDynamicPlaylistType.TopTracks ->
                tempTopTracks = withContext(Dispatchers.Default) {
                    analyticsUIState.topTracks.data?.filter { it.second.matches(query) } ?: emptyList()
                }

            is LibraryDynamicPlaylistType.TopArtists ->
                tempTopArtists = withContext(Dispatchers.Default) {
                    analyticsUIState.topArtists.data
                        ?.filter { it.second.name.contains(query, ignoreCase = true) }
                        ?: emptyList()
                }

            is LibraryDynamicPlaylistType.TopAlbums ->
                tempTopAlbums = withContext(Dispatchers.Default) {
                    analyticsUIState.topAlbums.data
                        ?.filter { matchesQuery(it.second.title, it.second.artistName, query) }
                        ?: emptyList()
                }
        }
        completedSearchQuery = query
    }

    // The original playlist and its hero retain their position while the result list narrows.
    val playlistRows: LazyListScope.(Boolean) -> Unit = { filtered ->
        val type = dynamicType
        if (type == LibraryDynamicPlaylistType.MostPlayed && !filtered && mostPlayed.isEmpty() && !localTrackingEnabled) {
            item(key = "most_played_local_tracking_hint") {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = stringResource(Res.string.most_played_enable_local_tracking),
                        style = typo().bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    TextButton(onClick = { navController.navigate(SettingsDestination) }) {
                        Text(stringResource(Res.string.settings))
                    }
                }
            }
        }
        if (type == LibraryDynamicPlaylistType.Followed) {
            items(
                if (filtered) {
                    tempFollowed
                } else {
                    followed
                },
                key = { it.channelId },
            ) { artist ->
                ArtistFullWidthItems(
                    artist,
                    startContentPadding = pageContentHorizontalInset(11.dp),
                    endContentPadding = pageContentHorizontalInset(11.dp),
                    onClickListener = {
                        navController.navigate(
                            ArtistDestination(
                                channelId = artist.channelId,
                            ),
                        )
                    },
                )
            }
        } else if (type is LibraryDynamicPlaylistType.TopArtists) {
            when (analyticsUIState.topArtists) {
                is LocalResource.Success if (!analyticsUIState.topArtists.data.isNullOrEmpty()) -> {
                    val data = analyticsUIState.topArtists.data ?: emptyList()
                    items(
                        if (filtered) {
                            tempTopArtists
                        } else {
                            data
                        },
                        key = { it.first.hashCode() },
                    ) { artist ->
                        ArtistFullWidthItems(
                            artist.second,
                            startContentPadding = pageContentHorizontalInset(11.dp),
                            endContentPadding = pageContentHorizontalInset(11.dp),
                            rightView = {
                                Box(Modifier.padding(horizontal = 8.dp)) {
                                    Text(
                                        text = "${artist.first.playCount} ${stringResource(Res.string.lower_plays)}",
                                        style = typo().bodySmall,
                                    )
                                }
                            },
                            onClickListener = {
                                navController.navigate(
                                    ArtistDestination(
                                        channelId = artist.second.channelId,
                                    ),
                                )
                            },
                        )
                    }
                }

                else -> {}
            }
        } else if (type is LibraryDynamicPlaylistType.TopAlbums) {
            when (analyticsUIState.topAlbums) {
                is LocalResource.Success if (!analyticsUIState.topAlbums.data.isNullOrEmpty()) -> {
                    val data = analyticsUIState.topAlbums.data ?: emptyList()
                    items(
                        if (filtered) {
                            tempTopAlbums
                        } else {
                            data
                        },
                        key = { it.first.hashCode() },
                    ) { album ->
                        PlaylistFullWidthItems(
                            album.second,
                            startContentPadding = pageContentHorizontalInset(11.dp),
                            endContentPadding = pageContentHorizontalInset(11.dp),
                            rightView = {
                                Box(Modifier.padding(horizontal = 8.dp)) {
                                    Text(
                                        text = "${album.first.playCount} ${stringResource(Res.string.lower_plays)}",
                                        style = typo().bodySmall,
                                    )
                                }
                            },
                            onClickListener = {
                                navController.navigate(
                                    AlbumDestination(
                                        browseId = album.second.browseId,
                                    ),
                                )
                            },
                        )
                    }
                }

                else -> {}
            }
        } else if (type is LibraryDynamicPlaylistType.TopTracks) {
            when (analyticsUIState.topTracks) {
                is LocalResource.Success if (!analyticsUIState.topTracks.data.isNullOrEmpty()) -> {
                    val data = analyticsUIState.topTracks.data ?: emptyList()
                    items(
                        if (filtered) {
                            tempTopTracks
                        } else {
                            data
                        },
                        key = { it.second.videoId },
                    ) { song ->
                        SongFullWidthItems(
                            songEntity = song.second,
                            startContentPadding = pageContentHorizontalInset(11.dp),
                            endContentPadding = pageContentHorizontalInset(11.dp),
                            isPlaying = song.second.videoId == nowPlayingVideoId,
                            showFavoriteButton = true,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .then(if (hasPresentedRows) Modifier.animateItem(placementSpec = null) else Modifier),
                            onMoreClickListener = {
                                chosenSong = song.second
                                showBottomSheet = true
                            },
                            onClickListener = { videoId ->
                                val targetList = data.map { it.second }
                                val playTrack = song.second
                                with(sharedViewModel) {
                                    setQueueData(
                                        QueueData.Data(
                                            listTracks = targetList.toArrayListTrack(),
                                            firstPlayedTrack = playTrack.toTrack(),
                                            playlistId = null,
                                            playlistName = getStringBlocking(Res.string.your_top_tracks),
                                            playlistType = PlaylistType.RADIO,
                                            continuation = null,
                                        ),
                                    )
                                    loadMediaItem(
                                        playTrack.toTrack(),
                                        Config.PLAYLIST_CLICK,
                                        targetList.indexOf(playTrack).coerceAtLeast(0),
                                    )
                                }
                            },
                            onAddToQueue = {
                                sharedViewModel.addListToQueue(
                                    arrayListOf(song.second.toTrack()),
                                )
                            },
                            selectionMode = selectionState.isActive,
                            isSelected = selectionState.isSelected(song.second.videoId),
                            onLongClick = { selectionState.start(it) },
                            onSelectToggle = { selectionState.toggle(it) },
                            rightView = {
                                Column(
                                    modifier = Modifier.wrapContentWidth(),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Text(
                                        text = "${song.first.totalListeningTime} ${stringResource(Res.string.seconds)}",
                                        style = typo().bodySmall,
                                    )
                                    Text(
                                        text = "${song.first.playCount} ${stringResource(Res.string.lower_plays)}",
                                        style = typo().bodySmall,
                                    )
                                }
                            },
                        )
                    }
                }

                else -> {}
            }
        } else {
            items(
                when (type) {
                    LibraryDynamicPlaylistType.Downloaded -> {
                        if (filtered) {
                            tempDownloaded
                        } else {
                            downloaded
                        }
                    }

                    LibraryDynamicPlaylistType.Favorite -> {
                        if (filtered) {
                            tempFavorite
                        } else {
                            favorite
                        }
                    }

                    LibraryDynamicPlaylistType.MostPlayed -> {
                        if (filtered) {
                            tempMostPlayed
                        } else {
                            mostPlayed
                        }
                    }

                    // Kept as an explicit branch rather than an `else`: the three cases above are
                    // reachable only because the `if` chain around this block has already ruled
                    // out every other object, and an `else` here would silently swallow the next
                    // case someone adds to the sealed class.
                    is LibraryDynamicPlaylistType.MonthlyRecap -> {
                        if (filtered) {
                            tempMonthlyRecap
                        } else {
                            monthlyRecap
                        }
                    }

                    is LibraryDynamicPlaylistType.ArtistLiked -> {
                        if (filtered) {
                            tempArtistLiked
                        } else {
                            artistLiked
                        }
                    }
                },
                key = { it.videoId },
            ) { song ->
                val mostPlayedRank =
                    if (type == LibraryDynamicPlaylistType.MostPlayed) {
                        mostPlayed.indexOf(song).takeIf { it >= 0 }?.plus(1)
                    } else {
                        null
                    }
                SongFullWidthItems(
                    songEntity = song,
                    leadingRank = mostPlayedRank,
                    startContentPadding = pageContentHorizontalInset(if (type == LibraryDynamicPlaylistType.MostPlayed) 3.dp else 11.dp),
                    endContentPadding = pageContentHorizontalInset(11.dp),
                    isPlaying = song.videoId == nowPlayingVideoId,
                    showFavoriteButton = type != LibraryDynamicPlaylistType.Favorite,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .then(if (hasPresentedRows) Modifier.animateItem(placementSpec = null) else Modifier),
                    onMoreClickListener = {
                        chosenSong = song
                        showBottomSheet = true
                    },
                    onClickListener = { videoId ->
                        viewModel.playSong(videoId, type = type)
                    },
                    onAddToQueue = {
                        sharedViewModel.addListToQueue(
                            arrayListOf(song.toTrack()),
                        )
                    },
                    onArtistClick =
                        (type as? LibraryDynamicPlaylistType.ArtistLiked)?.let { artistLikedType ->
                            {
                                navController.navigate(
                                    ArtistDestination(channelId = artistLikedType.channelId),
                                )
                            }
                        },
                    selectionMode = selectionState.isActive,
                    isSelected = selectionState.isSelected(song.videoId),
                    onLongClick = { selectionState.start(it) },
                    onSelectToggle = { selectionState.toggle(it) },
                    rightView =
                        if (type == LibraryDynamicPlaylistType.MostPlayed) {
                            {
                                Text(
                                    text = pluralStringResource(
                                        Res.plurals.n_plays,
                                        mostPlayedPlayCounts[song.videoId] ?: 0,
                                        mostPlayedPlayCounts[song.videoId] ?: 0,
                                    ),
                                    style = typo().bodySmall,
                                    maxLines = 1,
                                )
                            }
                        } else {
                            null
                        },
                )
            }
        }
        item {
            EndOfPage(bottomPaddingAlreadyApplied = innerPadding.calculateBottomPadding())
        }
    }
    val layoutDirection = LocalLayoutDirection.current
    // Short download lists otherwise hit the end before their tall hero can scroll above the IME.
    val downloadedSearchImePadding =
        if (
            dynamicType == LibraryDynamicPlaylistType.Downloaded &&
                showSearchBar &&
                query.isBlank()
        ) {
            WindowInsets.ime.asPaddingValues().calculateBottomPadding()
        } else {
            0.dp
        }
    FilteredPlaylistList(
        state = lazyState,
        searchState = searchResultsState,
        filtering = query.isNotBlank(),
        completedQuery = completedSearchQuery,
        contentPadding = PaddingValues(
            start = innerPadding.calculateStartPadding(layoutDirection),
            top = innerPadding.calculateTopPadding(),
            end = innerPadding.calculateEndPadding(layoutDirection),
            bottom = innerPadding.calculateBottomPadding() + downloadedSearchImePadding,
        ),
        searchPadding = PaddingValues(
            start = innerPadding.calculateStartPadding(layoutDirection),
            end = innerPadding.calculateEndPadding(layoutDirection),
            top = searchHeaderHeight + (if (showSearchBar && !inlineLandscapeSearch) InPageSearchBarHeight else 0.dp) + 8.dp,
            bottom = innerPadding.calculateBottomPadding(),
        ),
        modifier = Modifier
            .consumePointerInputWhen(
                dynamicType == LibraryDynamicPlaylistType.MostPlayed && currentDynamicPlaylistType != type,
            )
            .then(stickyTitle.viewportModifier)
            .frostedHeaderSource(searchBackdrop, auroraBackground = headerTint)
            .hazeSource(hazeState),
        header = {
            item {
                Spacer(Modifier.height(pageHeaderContentHeight() + 8.dp))
            }
            item {
                AnimatedVisibility(showSearchBar && !inlineLandscapeSearch) {
                    Spacer(Modifier.height(InPageSearchBarHeight))
                }
            }
            if (dynamicType == LibraryDynamicPlaylistType.MostPlayed) {
                item(key = "most_played_period_selector") {
                    MostPlayedPeriodSelector(
                        selectedPeriod = mostPlayedPeriod,
                        onPeriodSelected = viewModel::setMostPlayedPeriod,
                    )
                }
            }
            if (dynamicType == LibraryDynamicPlaylistType.Favorite) {
                item(key = "favorite_playlist_hero") {
                    val controllerState by sharedViewModel.controllerState.collectAsStateWithLifecycle()
                    val queueData by sharedViewModel.getQueueDataState().collectAsStateWithLifecycle()
                    val favoriteDownloadState by viewModel.favoriteDownloadState.collectAsStateWithLifecycle()
                    val favoriteQueueName = "${stringResource(Res.string.playlist)} ${stringResource(Res.string.favorite)}"
                    val isFavoriteQueue = queueData?.data?.playlistName == favoriteQueueName
                    FavoritePlaylistHero(
                        thumbnail = favorite.firstOrNull()?.thumbnails,
                        trackCount = favorite.size,
                        isPlaying = controllerState.isPlaying && isFavoriteQueue,
                        downloadState = favoriteDownloadState,
                        backgroundColor = headerTint,
                        onPlayPause = {
                            if (isFavoriteQueue) {
                                sharedViewModel.onUIEvent(UIEvent.PlayPause)
                            } else {
                                viewModel.playAll(LibraryDynamicPlaylistType.Favorite)
                            }
                        },
                        onShuffle = { viewModel.shuffle(LibraryDynamicPlaylistType.Favorite) },
                        onDownload = viewModel::downloadFavorites,
                        stickyTitle = stickyTitle,
                    )
                }
            }
            if (dynamicType == LibraryDynamicPlaylistType.Downloaded) {
                item(key = "downloaded_playlist_hero") {
                    val controllerState by sharedViewModel.controllerState.collectAsStateWithLifecycle()
                    val queueData by sharedViewModel.getQueueDataState().collectAsStateWithLifecycle()
                    val downloadedQueueName = "${stringResource(Res.string.playlist)} ${stringResource(Res.string.downloaded)}"
                    val isDownloadedQueue = queueData?.data?.playlistName == downloadedQueueName
                    DownloadedPlaylistHero(
                        thumbnail = downloaded.firstOrNull()?.thumbnails,
                        trackCount = downloaded.size,
                        isPlaying = controllerState.isPlaying && isDownloadedQueue,
                        backgroundColor = headerTint,
                        onPlayPause = {
                            if (isDownloadedQueue) {
                                sharedViewModel.onUIEvent(UIEvent.PlayPause)
                            } else {
                                viewModel.playAll(LibraryDynamicPlaylistType.Downloaded)
                            }
                        },
                        onShuffle = { viewModel.shuffle(LibraryDynamicPlaylistType.Downloaded) },
                        onAddToQueue = {
                            sharedViewModel.addListToQueue(downloaded.toArrayListTrack())
                        },
                        onRemoveAllDownloads = { showRemoveAllDownloadsDialog = true },
                        stickyTitle = stickyTitle,
                    )
                }
            }
            if (dynamicType is LibraryDynamicPlaylistType.ArtistLiked) {
                item(key = "artist_liked_hero") {
                    ArtistLikedSongsHero(
                        title = artistLikedName.orEmpty(),
                        thumbnail = artistLikedThumbnail,
                        trackCount = artistLiked.size,
                        backgroundColor = headerTint,
                        onShuffle = { viewModel.shuffle(dynamicType) },
                        stickyTitle = stickyTitle,
                        onArtistClick = { onArtistTitleClick?.invoke() },
                    )
                }
            }
        },
        rows = playlistRows,
    )
    if (showSelectionSheet) {
        val selectedIds = selectionState.selected.toList()
        val addToFavoriteAction: () -> Unit = {
            selectionViewModel.addToFavorite(selectedIds)
            selectionState.exit()
        }
        val removeFromFavoriteAction: () -> Unit = {
            selectionViewModel.removeFromFavorite(selectedIds)
            selectionState.exit()
        }
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
            onAddToFavorite = addToFavoriteAction,
            onRemoveFromFavorite = removeFromFavoriteAction,
        )
    }
    if (showSelectionAddToPlaylist) {
        val selectedIds = selectionState.selected.toList()
        val localPlaylists by selectionViewModel.listLocalPlaylist.collectAsStateWithLifecycle()
        val availableSongs = (favorite + mostPlayed + downloaded + monthlyRecap + artistLiked + analyticsUIState.topTracks.data.orEmpty().map { it.second }).filter { it.videoId in selectedIds }
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
    if (showBottomSheet) {
        NowPlayingBottomSheet(
            onDismiss = {
                showBottomSheet = false
                chosenSong = null
            },
            navController = navController,
            song = chosenSong ?: return,
            showFavoriteAction = dynamicType == LibraryDynamicPlaylistType.Favorite,
        )
    }
    if (showRemoveAllDownloadsDialog) {
        AmoledAlertDialog(
            title = { Text(stringResource(Res.string.warning)) },
            text = { Text(stringResource(Res.string.remove_all_downloads_confirmation)) },
            onDismissRequest = { showRemoveAllDownloadsDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRemoveAllDownloadsDialog = false
                        viewModel.removeAllDownloads()
                    },
                ) {
                    Text(stringResource(Res.string.yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveAllDownloadsDialog = false }) {
                    Text(stringResource(Res.string.cancel))
                }
            },
        )
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val type = dynamicType
        val headerTitle = if (type is LibraryDynamicPlaylistType.ArtistLiked) artistLikedName.orEmpty() else type.title()
        val isSongType =
            type != LibraryDynamicPlaylistType.Followed &&
                type !is LibraryDynamicPlaylistType.TopArtists &&
                type !is LibraryDynamicPlaylistType.TopAlbums
        // Counts always come from the unfiltered lists, so the subtitle keeps reporting the
        // library total while the user is typing in the search bar.
        val subtitle =
            when (type) {
                LibraryDynamicPlaylistType.Favorite,
                LibraryDynamicPlaylistType.MostPlayed,
                LibraryDynamicPlaylistType.Downloaded -> null
                LibraryDynamicPlaylistType.Followed ->
                    "${followed.size} ${stringResource(Res.string.artists)}"
                is LibraryDynamicPlaylistType.MonthlyRecap ->
                    stringResource(Res.string.wrapped_recap_subtitle)
                is LibraryDynamicPlaylistType.ArtistLiked ->
                    pluralStringResource(Res.plurals.n_track, artistLiked.size, artistLiked.size)
                is LibraryDynamicPlaylistType.TopTracks,
                is LibraryDynamicPlaylistType.TopArtists,
                is LibraryDynamicPlaylistType.TopAlbums -> topListSubtitle(type)
                else -> null
            }
        Box {
            FrostedHeader(
                hazeState = hazeState,
                backdrop = searchBackdrop,
                shareHeader = true,
                showSurface =
                    if (type == LibraryDynamicPlaylistType.Followed) {
                        showScrolledHeaderSurface || showSearchBar || selectionState.isActive
                    } else {
                        showPlaylistTitle || showSearchBar || selectionState.isActive
                    },
                modifier = Modifier.fillMaxWidth(),
                tint = headerTint,
            ) {
            // Selection replaces the normal header; flat shared chrome does not paint an
            // opaque surface over the old title and buttons.
            if (!selectionState.isActive) {
            PageTopAppBar(
                backdrop = searchBackdrop,
                title = {
                    if (hasPlaylistHero && query.isBlank() && !(inlineLandscapeSearch && showSearchBar)) {
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            if (hasStickyTitleHandoff) stickyTitle.Target(headerTitle, textAlign = TextAlign.Center)
                            if ((!useGlassHeaderButtons || !hasStickyTitleHandoff) && showPlaylistTitle) {
                                Text(
                                    text = headerTitle,
                                    style = typo().titleMedium,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                        .then(onArtistTitleClick?.let { Modifier.clickable(onClick = it) } ?: Modifier)
                                        .alpha(slideV2TitleAlpha?.value ?: fallbackPageTitleTransitionAlpha),
                                )
                            }
                        }
                    } else if (inlineLandscapeSearch) {
                        AnimatedLandscapeHeaderSearch(
                            title = headerTitle,
                            subtitle = subtitle,
                            titleVisible = showPlaylistTitle,
                            searchVisible = showSearchBar,
                            contentColor = MaterialTheme.colorScheme.onBackground,
                            searchField = playlistSearchField,
                        )
                    } else {
                        // Liquid Glass lifts the title and count as the hero leaves the toolbar;
                        // flat chrome fades the title in at the toolbar position.
                        // Retain content during the reverse fade so scrolling back never pops it out.
                        androidx.compose.animation.AnimatedVisibility(
                            visible = showPlaylistTitle,
                            enter = if (useGlassHeaderButtons) {
                                fadeIn(tween(220, easing = FastOutSlowInEasing)) +
                                    slideInVertically(tween(220, easing = FastOutSlowInEasing)) { titleHandoffDistancePx }
                            } else {
                                fadeIn(tween(220, easing = FastOutSlowInEasing))
                            },
                            exit = if (useGlassHeaderButtons) {
                                fadeOut(tween(160, easing = FastOutSlowInEasing)) +
                                    slideOutVertically(tween(160, easing = FastOutSlowInEasing)) { titleHandoffDistancePx }
                            } else {
                                fadeOut(tween(160, easing = FastOutSlowInEasing))
                            },
                        ) {
                            Column(
                                modifier = onArtistTitleClick?.let { Modifier.clickable(onClick = it) } ?: Modifier,
                                horizontalAlignment =
                                    if (type is LibraryDynamicPlaylistType.ArtistLiked || LocalSharedHeaderHosted.current || !LocalLiquidGlassEnabled.current) Alignment.CenterHorizontally
                                    else Alignment.Start,
                            ) {
                                Text(
                                    text = headerTitle,
                                    style = typo().titleMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                if (subtitle != null) {
                                    Text(
                                        text = subtitle,
                                        style = typo().bodySmall,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                    }
                },
                navigationIcon = {
                    RippleIconButton(
                        WaveIcons.ArrowBackIosNew,
                        Modifier.size(if (useGlassHeaderButtons || hasPlaylistHero) headerGlassControlSize() else 32.dp),
                        fillMaxSize = !useGlassHeaderButtons && !hasPlaylistHero,
                        tint = MaterialTheme.colorScheme.onBackground,
                    ) { navController.navigateUp() }
                },
                actions = {
                    if (isSongType && !hasPlaylistHero && type != LibraryDynamicPlaylistType.MostPlayed) {
                        RippleIconButton(
                            if (useGlassHeaderButtons) WaveIcons.PlayArrow else WaveIcons.PlayCircle,
                            Modifier.size(48.dp),
                            fillMaxSize = !useGlassHeaderButtons,
                            tint = MaterialTheme.colorScheme.onBackground,
                        ) {
                            if (type is LibraryDynamicPlaylistType.TopTracks) {
                                val data = analyticsUIState.topTracks.data
                                if (!data.isNullOrEmpty()) {
                                    val first = data.first().second
                                    sharedViewModel.setQueueData(
                                        QueueData.Data(
                                            listTracks = data.map { it.second }.toArrayListTrack(),
                                            firstPlayedTrack = first.toTrack(),
                                            playlistId = null,
                                            playlistName = getStringBlocking(Res.string.your_top_tracks),
                                            playlistType = PlaylistType.RADIO,
                                            continuation = null,
                                        ),
                                    )
                                    sharedViewModel.loadMediaItem(
                                        first.toTrack(),
                                        Config.PLAYLIST_CLICK,
                                        0,
                                    )
                                }
                            } else {
                                viewModel.playAll(type)
                            }
                        }
                        RippleIconButton(
                            WaveIcons.Shuffle,
                            Modifier.size(if (useGlassHeaderButtons) 48.dp else 32.dp),
                            fillMaxSize = !useGlassHeaderButtons,
                            tint = MaterialTheme.colorScheme.onBackground,
                        ) {
                            if (type is LibraryDynamicPlaylistType.TopTracks) {
                                val data = analyticsUIState.topTracks.data
                                if (!data.isNullOrEmpty()) {
                                    val shuffled = data.shuffled()
                                    val first = shuffled.first().second
                                    sharedViewModel.setQueueData(
                                        QueueData.Data(
                                            listTracks = shuffled.map { it.second }.toArrayListTrack(),
                                            firstPlayedTrack = first.toTrack(),
                                            playlistId = null,
                                            playlistName = getStringBlocking(Res.string.your_top_tracks),
                                            playlistType = PlaylistType.RADIO,
                                            continuation = null,
                                        ),
                                    )
                                    sharedViewModel.loadMediaItem(
                                        first.toTrack(),
                                        Config.PLAYLIST_CLICK,
                                        0,
                                    )
                                }
                            } else {
                                viewModel.shuffle(type)
                            }
                        }
                    }
                    Box {
                        val searchIcon = if (showSearchBar || query.isNotEmpty()) WaveIcons.Close else WaveIcons.Search
                        val toggleSearch: () -> Unit = {
                            if (showSearchBar) {
                                keyboardController?.hide()
                                showSearchBar = false
                            } else if (query.isNotEmpty()) {
                                query = ""
                            } else {
                                showSearchBar = true
                            }
                        }
                        RippleIconButton(
                            searchIcon,
                            Modifier.size(if (useGlassHeaderButtons || hasPlaylistHero) headerGlassControlSize() else 32.dp),
                            fillMaxSize = !useGlassHeaderButtons && !hasPlaylistHero,
                            tint = MaterialTheme.colorScheme.onBackground,
                            onClick = toggleSearch,
                        )
                    }
                },
                keepControlContainers = hasPlaylistHero,
                modifier = Modifier,
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                    ),
            )
            }
            if (hasStickyTitleHandoff && useGlassHeaderButtons) {
                with(stickyTitle) {
                    Title(
                        headerTitle,
                        MaterialTheme.colorScheme.onBackground,
                        visible = query.isBlank() && !selectionState.isActive && !(inlineLandscapeSearch && showSearchBar),
                        transitionAlpha = fallbackPageTitleTransitionAlpha,
                        transitionAlphaState = slideV2TitleAlpha,
                        freezePosition = freezeStickyTitle,
                        ignoreViewportTranslation = slideV2TitleAlpha != null,
                        onClick = onArtistTitleClick,
                    )
                }
            }
            // Keep the replacement in the same header slot so the content below stays put.
            if (selectionState.isActive) {
                SongSelectionTopAppBar(
                    state = selectionState,
                    onSelectAll = {
                        val visible =
                            when (type) {
                                is LibraryDynamicPlaylistType.TopTracks ->
                                    (
                                        if (query.isNotEmpty()) {
                                            tempTopTracks
                                        } else {
                                            analyticsUIState.topTracks.data ?: emptyList()
                                        }
                                    ).map { it.second.videoId }

                                LibraryDynamicPlaylistType.Downloaded ->
                                    (if (query.isNotEmpty()) tempDownloaded else downloaded)
                                        .map { it.videoId }

                                LibraryDynamicPlaylistType.Favorite ->
                                    (if (query.isNotEmpty()) tempFavorite else favorite)
                                        .map { it.videoId }

                                LibraryDynamicPlaylistType.MostPlayed ->
                                    (if (query.isNotEmpty()) tempMostPlayed else mostPlayed)
                                        .map { it.videoId }

                                is LibraryDynamicPlaylistType.ArtistLiked ->
                                    (if (query.isNotEmpty()) tempArtistLiked else artistLiked)
                                        .map { it.videoId }

                                else -> emptyList()
                            }
                        selectionState.toggleSelectAll(visible)
                    },
                    onOpenActions = { showSelectionSheet = true },
                    backdrop = searchBackdrop,
                )
            }
            }
        }
        androidx.compose.animation.AnimatedVisibility(
            visible = showSearchBar && !inlineLandscapeSearch,
            enter = SearchBarEnter,
            exit = SearchBarExit,
        ) {
            playlistSearchField()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MostPlayedPeriodSelector(
    selectedPeriod: MostPlayedPeriod,
    onPeriodSelected: (MostPlayedPeriod) -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    Box(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Box(Modifier.align(Alignment.CenterEnd)) {
            OutlinedButton(
                onClick = { expanded = true },
                shape = RoundedCornerShape(50.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
            ) {
                Text(stringResource(selectedPeriod.labelResource()))
                Icon(
                    imageVector = WaveIcons.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
        }
    }
    if (expanded) {
        AppDialogTheme {
            ModalBottomSheet(
                onDismissRequest = { expanded = false },
                sheetState = sheetState,
                containerColor = Color.Transparent,
                contentColor = Color.Transparent,
                dragHandle = null,
                scrimColor = Color.Black.copy(alpha = 0.5f),
                contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
            ) {
                val colors = rememberSurfaceDarkColors()
                val sheetContainerColor =
                    if (LocalAppIsDarkTheme.current) {
                        MaterialTheme.colorScheme.surfaceContainerHigh
                    } else {
                        colors.container
                    }
                val shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                Card(
                    modifier = Modifier
                        .dialogMotionSource(clearPreviousOrigin = true)
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .border(1.dp, colors.disabled.copy(alpha = 0.45f), shape),
                    shape = shape,
                    colors = CardDefaults.cardColors(containerColor = sheetContainerColor),
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Spacer(Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.width(60.dp).height(4.dp),
                            colors = CardDefaults.cardColors(containerColor = colors.handle.copy(alpha = 0.45f)),
                            shape = RoundedCornerShape(50),
                        ) {}
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = stringResource(Res.string.date_range),
                            style = typo().titleMedium,
                            color = colors.content,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
                        )
                        HorizontalDivider(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                            color = colors.disabled.copy(alpha = 0.55f),
                        )
                        MostPlayedPeriod.values().forEach { period ->
                            val selected = period == selectedPeriod
                            ActionButton(
                                icon = if (selected) WaveIcons.CheckCircle else WaveIcons.History,
                                iconColor = if (selected) MaterialTheme.colorScheme.primary else Color.Unspecified,
                                text = period.labelResource(),
                                textColor = if (selected) MaterialTheme.colorScheme.primary else null,
                            ) {
                                scope.launch {
                                    sheetState.hide()
                                    expanded = false
                                    onPeriodSelected(period)
                                }
                            }
                        }
                        EndOfModalBottomSheet()
                    }
                }
            }
        }
    }
}

private fun MostPlayedPeriod.labelResource(): StringResource =
    when (this) {
        MostPlayedPeriod.TODAY -> Res.string.today
        MostPlayedPeriod.THIS_WEEK -> Res.string.this_week
        MostPlayedPeriod.THIS_MONTH -> Res.string.this_month
        MostPlayedPeriod.THIS_YEAR -> Res.string.this_year
        MostPlayedPeriod.ALL_TIME -> Res.string.all_time
    }

sealed class LibraryDynamicPlaylistType {
    data object Favorite : LibraryDynamicPlaylistType()

    data object Followed : LibraryDynamicPlaylistType()

    data object MostPlayed : LibraryDynamicPlaylistType()

    data object Downloaded : LibraryDynamicPlaylistType()

    /**
     * The three top lists carry the period they were opened for, so the list on screen is the one
     * the user was looking at rather than whatever period this screen's own view model starts on.
     *
     * Both ends null means no period was given — the deep link and anything else that names the
     * list without a range — and the list follows the navigator's current period, as it always has.
     * [dayRange] is only the NAME the Analytics dropdown had selected, kept for the header; the
     * dates alone decide what is loaded.
     */
    data class TopTracks(
        val start: LocalDate? = null,
        val end: LocalDate? = null,
        val dayRange: AnalyticsUiState.DayRange? = null,
    ) : LibraryDynamicPlaylistType()

    data class TopArtists(
        val start: LocalDate? = null,
        val end: LocalDate? = null,
        val dayRange: AnalyticsUiState.DayRange? = null,
    ) : LibraryDynamicPlaylistType()

    data class TopAlbums(
        val start: LocalDate? = null,
        val end: LocalDate? = null,
        val dayRange: AnalyticsUiState.DayRange? = null,
    ) : LibraryDynamicPlaylistType()

    /**
     * One calendar month's top songs — the Wrapped tab's "Recap January".
     *
     * The only case carrying data, which is the whole reason [title] exists: every other entry
     * names itself with a fixed [StringResource], and a month cannot.
     *
     * [month] is 1..12, matching `kotlinx.datetime.Month.number`, so the round trip stays readable
     * in a URL and needs no enum lookup to serialise.
     */
    data class MonthlyRecap(
        val year: Int,
        val month: Int,
    ) : LibraryDynamicPlaylistType()

    /**
     * Liked songs crediting one artist — opened from the "Liked songs" row on that artist's page
     * (issue #2524).
     *
     * Carries only the channel id: the header's artist name is looked up by the view model, since a
     * name can hold any character and would have to survive the route string.
     */
    data class ArtistLiked(
        val channelId: String,
    ) : LibraryDynamicPlaylistType()

    /**
     * The fixed name, for the callers that resolve a resource without composition.
     *
     * [MonthlyRecap] has no honest answer here — its name is assembled from a month and sometimes
     * a year — so it falls back to the tab it belongs to. Nothing renders that fallback as a
     * heading: the screen prints [title], which knows the month. It exists because the `when` must
     * cover the sealed hierarchy, and because
     * [com.petedev.wavemusic.viewModel.LibraryDynamicPlaylistViewModel] still needs *some* answer
     * on the path where it names a queue — that view model resolves the recap's real name itself.
     */
    fun name(): StringResource =
        when (this) {
            Favorite -> Res.string.favorite
            Followed -> Res.string.followed
            MostPlayed -> Res.string.most_played
            Downloaded -> Res.string.downloaded
            is TopAlbums -> Res.string.your_top_albums
            is TopArtists -> Res.string.your_top_artists
            is TopTracks -> Res.string.your_top_tracks
            is MonthlyRecap -> Res.string.wrapped
            is ArtistLiked -> Res.string.liked_songs
        }

    /**
     * What the header actually shows.
     *
     * A `@Composable` rather than a [StringResource] because "Recap January" is a format string
     * plus an argument, and a resource reference cannot carry the argument. The year is printed
     * only when it is not the current one — two Januaries a year apart would otherwise be the same
     * playlist as far as the reader can tell.
     */
    @Composable
    fun title(): String =
        when (this) {
            is MonthlyRecap ->
                if (year == now().date.year) {
                    stringResource(Res.string.wrapped_recap_month, monthFullName(Month(month)))
                } else {
                    stringResource(
                        Res.string.wrapped_recap_month_year,
                        monthFullName(Month(month)),
                        year.toString(),
                    )
                }

            else -> stringResource(name())
        }

    /** The span a top list was opened for, or null when it follows the navigator's own period. */
    fun topListRange(): Pair<LocalDate, LocalDate>? {
        // Named apart from the properties on purpose: inside each branch `this` is smart-cast, so a
        // local called `start` would sit one character away from the property it is reading.
        val (rangeStart, rangeEnd) =
            when (this) {
                is TopTracks -> Pair(start, end)
                is TopArtists -> Pair(start, end)
                is TopAlbums -> Pair(start, end)
                else -> return null
            }
        return if (rangeStart != null && rangeEnd != null) Pair(rangeStart, rangeEnd) else null
    }

    /** The Analytics dropdown range a top list was opened from, or null. */
    fun topListDayRange(): AnalyticsUiState.DayRange? =
        when (this) {
            is TopTracks -> dayRange
            is TopArtists -> dayRange
            is TopAlbums -> dayRange
            else -> null
        }

    // `_LAST_30_DAYS_2026-09-01_2026-10-01`, or nothing. The dates go LAST because they contain no
    // underscore while the enum name does, which is what lets the parser read them off the end.
    private fun periodSuffix(): String {
        val (start, end) = topListRange() ?: return ""
        val range = topListDayRange()?.let { "_${it.name}" }.orEmpty()
        return "${range}_${start}_$end"
    }

    // For serialization and navigation
    fun toStringParams(): String =
        when (this) {
            Favorite -> "favorite"
            Followed -> "followed"
            MostPlayed -> "most_played"
            Downloaded -> "downloaded"
            is TopAlbums -> TOP_ALBUMS + periodSuffix()
            is TopArtists -> TOP_ARTISTS + periodSuffix()
            is TopTracks -> TOP_TRACKS + periodSuffix()
            // Zero-padded so the strings sort the way the months do, which makes a list of these
            // readable in a log or a deep link without parsing it back.
            is MonthlyRecap -> "${RECAP_PREFIX}${year}_${month.toString().padStart(2, '0')}"
            is ArtistLiked -> ARTIST_LIKED_PREFIX + channelId
        }

    companion object {
        private const val RECAP_PREFIX = "recap_"
        private const val ARTIST_LIKED_PREFIX = "artist_liked_"
        private const val TOP_TRACKS = "top_tracks"
        private const val TOP_ARTISTS = "top_artists"
        private const val TOP_ALBUMS = "top_albums"

        fun toType(input: String): LibraryDynamicPlaylistType =
            when (input) {
                "favorite" -> Favorite
                "followed" -> Followed
                "most_played" -> MostPlayed
                "downloaded" -> Downloaded
                TOP_ALBUMS -> TopAlbums()
                TOP_ARTISTS -> TopArtists()
                TOP_TRACKS -> TopTracks()
                else ->
                    parseTopList(input)
                        ?: parseMonthlyRecap(input)
                        ?: parseArtistLiked(input)
                        ?: throw IllegalArgumentException("Unknown type: $input")
            }

        /**
         * `artist_liked_UC…` back into an [ArtistLiked], or null. The remainder is taken whole —
         * channel ids contain `_` and `-` — and only has to be non-blank: an id matching no song
         * just opens an empty list.
         */
        private fun parseArtistLiked(input: String): ArtistLiked? =
            input
                .takeIf { it.startsWith(ARTIST_LIKED_PREFIX) }
                ?.removePrefix(ARTIST_LIKED_PREFIX)
                ?.takeIf { it.isNotBlank() }
                ?.let(::ArtistLiked)

        /**
         * `top_tracks_2026-08-01_2026-08-31` back into a top list with its period, or null.
         *
         * Validated for the same reason as [parseMonthlyRecap] — this can arrive through the deep
         * link from outside the app — and a start after its end is rejected rather than handed to a
         * query that would quietly return nothing.
         */
        private fun parseTopList(input: String): LibraryDynamicPlaylistType? {
            val key = listOf(TOP_TRACKS, TOP_ARTISTS, TOP_ALBUMS).firstOrNull { input.startsWith("${it}_") } ?: return null
            // Read from the RIGHT: the two dates are last and never contain an underscore, while the
            // range name before them is an enum name that does (`LAST_30_DAYS`).
            val parts = input.removePrefix("${key}_").split("_")
            if (parts.size < 2) return null
            val start = runCatching { LocalDate.parse(parts[parts.size - 2]) }.getOrNull() ?: return null
            val end = runCatching { LocalDate.parse(parts.last()) }.getOrNull() ?: return null
            if (start > end) return null
            val rangeName = parts.dropLast(2).joinToString("_")
            val dayRange =
                if (rangeName.isEmpty()) {
                    null
                } else {
                    runCatching { AnalyticsUiState.DayRange.valueOf(rangeName) }.getOrNull() ?: return null
                }
            return when (key) {
                TOP_TRACKS -> TopTracks(start, end, dayRange)
                TOP_ARTISTS -> TopArtists(start, end, dayRange)
                else -> TopAlbums(start, end, dayRange)
            }
        }

        /**
         * `recap_2026_01` back into a [MonthlyRecap], or null for anything else.
         *
         * Validated rather than trusted: this arrives from a persisted navigation argument and,
         * through the `simpmusic://library?type=` deep link, from outside the app entirely. A month
         * of 0 or 13 would reach `Month(month)` and throw somewhere far away from here.
         */
        private fun parseMonthlyRecap(input: String): MonthlyRecap? {
            if (!input.startsWith(RECAP_PREFIX)) return null
            val parts = input.removePrefix(RECAP_PREFIX).split("_")
            if (parts.size != 2) return null
            val year = parts[0].toIntOrNull() ?: return null
            val month = parts[1].toIntOrNull()?.takeIf { it in 1..12 } ?: return null
            return MonthlyRecap(year = year, month = month)
        }
    }
}

/**
 * `Last 30 days (1/9-1/10/2026)` — the period a top list was opened on, named exactly as the
 * Analytics dropdown names it so the two screens read the same. Null when the list names no period,
 * which keeps the header to a single line.
 */
@Composable
private fun topListSubtitle(type: LibraryDynamicPlaylistType): String? {
    val (start, end) = type.topListRange() ?: return null
    val span = formatNumericSpan(start, end)
    val dayRange = type.topListDayRange() ?: return span
    return "${stringResource(dayRange.labelRes())} ($span)"
}

/**
 * Whether a title or any of its artists contains [query].
 *
 * Artist names count as well as the title: people look for "everything by X" at least as often as
 * for one track, and typing an artist into a search box that only reads titles looks broken.
 *
 * [artists] is a converted `List<String>`, so every entry is checked — a featured artist matches
 * as readily as the lead.
 */
private fun matchesQuery(
    title: String,
    artists: List<String>?,
    query: String,
): Boolean =
    title.contains(query, ignoreCase = true) ||
        artists?.any { it.contains(query, ignoreCase = true) } == true

private fun SongEntity.matches(query: String): Boolean = matchesQuery(title, artistName, query)
