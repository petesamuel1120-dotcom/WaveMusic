package com.maxrave.simpmusic.ui.screen.library

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isUnspecified
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.maxrave.common.LibraryChipType
import com.maxrave.domain.data.entities.SongEntity
import com.maxrave.domain.data.entities.LocalPlaylistEntity
import com.maxrave.domain.data.model.searchResult.playlists.PlaylistsResult
import com.maxrave.domain.data.type.PlaylistType
import com.maxrave.domain.utils.LocalResource
import com.maxrave.simpmusic.extension.copy
import com.maxrave.simpmusic.extension.isScrollingUp
import com.maxrave.simpmusic.extension.animateScrollToTopSmoothly
import com.maxrave.simpmusic.ui.component.AddToPlaylistModalBottomSheet
import com.maxrave.simpmusic.ui.component.CreatePlaylistDialog
import com.maxrave.simpmusic.ui.component.AppContentHorizontalInset
import com.maxrave.simpmusic.ui.component.BottomNavScreen
import com.maxrave.simpmusic.ui.component.HeaderScrollDirectionTracker
import com.maxrave.simpmusic.ui.component.HeaderScrollOffsetTracker
import com.maxrave.simpmusic.ui.component.LocalSharedHeaderState
import com.maxrave.simpmusic.ui.component.navigateToTabRoot
import com.maxrave.simpmusic.ui.component.Chip
import com.maxrave.simpmusic.ui.component.CenterLoadingBox
import com.maxrave.simpmusic.ui.component.EndOfPage
import com.maxrave.simpmusic.ui.component.FavoriteArtistsGrid
import com.maxrave.simpmusic.ui.component.PreloadLibraryArtistThumbnails
import com.maxrave.simpmusic.ui.component.GridLibraryPlaylist
import com.maxrave.simpmusic.ui.component.LibraryItem
import com.maxrave.simpmusic.ui.component.LibraryItemState
import com.maxrave.simpmusic.ui.component.LibraryItemType
import com.maxrave.simpmusic.ui.component.LibraryPlaylistMenu
import com.maxrave.simpmusic.ui.component.LoadingDialog
import com.maxrave.simpmusic.ui.component.LibraryRecentlyAddedRow
import com.maxrave.simpmusic.ui.component.LibraryTilingBox
import com.maxrave.simpmusic.ui.component.NowPlayingBottomSheet
import com.maxrave.simpmusic.ui.component.YourPlaylistsShelf
import com.maxrave.simpmusic.ui.component.libraryItemContentType
import com.maxrave.simpmusic.ui.component.libraryItemKey
import com.maxrave.simpmusic.ui.component.selection.SelectedSongsBottomSheet
import com.maxrave.simpmusic.ui.component.selection.SongSelectionTopAppBar
import com.maxrave.simpmusic.ui.component.selection.rememberSongSelectionState
import com.maxrave.simpmusic.ui.icon.Groups
import com.maxrave.simpmusic.ui.icon.PeopleAlt
import com.maxrave.simpmusic.ui.navigation.destination.library.LibraryDestination
import com.maxrave.simpmusic.ui.navigation.destination.library.LibraryDynamicPlaylistDestination
import com.maxrave.simpmusic.ui.navigation.destination.list.LocalPlaylistDestination
import com.maxrave.simpmusic.ui.navigation.destination.list.PlaylistDestination
import com.maxrave.simpmusic.expect.ui.BackHandler
import com.maxrave.simpmusic.ui.theme.typo
import com.maxrave.simpmusic.viewModel.LibraryDynamicPlaylistViewModel
import com.maxrave.simpmusic.viewModel.LibraryViewModel
import com.maxrave.simpmusic.viewModel.SharedViewModel
import com.maxrave.simpmusic.viewModel.SongSelectionViewModel
import com.maxrave.simpmusic.utils.initialArtistThumbnailCount
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import simpmusic.composeapp.generated.resources.Res
import simpmusic.composeapp.generated.resources.chart
import simpmusic.composeapp.generated.resources.artists
import simpmusic.composeapp.generated.resources.downloaded_playlists
import simpmusic.composeapp.generated.resources.favorite_playlists
import simpmusic.composeapp.generated.resources.library_local_playlists
import simpmusic.composeapp.generated.resources.library_youtube_playlists
import simpmusic.composeapp.generated.resources.library
import simpmusic.composeapp.generated.resources.mix_for_you
import simpmusic.composeapp.generated.resources.no_YouTube_playlists
import simpmusic.composeapp.generated.resources.no_charts_found
import simpmusic.composeapp.generated.resources.no_favorite_playlists
import simpmusic.composeapp.generated.resources.no_favorite_podcasts
import simpmusic.composeapp.generated.resources.podcasts
import simpmusic.composeapp.generated.resources.no_playlists_added
import simpmusic.composeapp.generated.resources.no_playlists_downloaded
import simpmusic.composeapp.generated.resources.library_history
import simpmusic.composeapp.generated.resources.simpmusic_charts
import simpmusic.composeapp.generated.resources.wrapped
import simpmusic.composeapp.generated.resources.your_library

private val visibleLibraryCategories =
    listOf(
        LibraryChipType.YOUR_LIBRARY,
        LibraryChipType.FAVORITE_PODCAST,
        LibraryChipType.ARTISTS,
    )

private val libraryCategoryRowHeight = 56.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    innerPadding: PaddingValues,
    viewModel: LibraryViewModel = koinViewModel(),
    navController: NavController,
    onScrolling: (onTop: Boolean) -> Unit = {},
    sharedHeaderHeight: Dp,
    onAccountInfoChanged: (Pair<String?, String?>?) -> Unit = {},
    useFadeForCategoryTransitions: Boolean,
    useSlideV2ForCategoryTransitions: Boolean = false,
) {
    // The shelf switches between local and YouTube playlists based on this value. Seed it from
    // the persisted setting so returning to the Library doesn't briefly show local playlists
    // before the DataStore flow reports that the user is signed in.
    val initialLoggedIn = remember(viewModel) { viewModel.getYouTubeLoggedIn() }
    val loggedIn by viewModel.youtubeLoggedIn.collectAsStateWithLifecycle(initialValue = initialLoggedIn)
    // Wrapped and its recaps are built entirely from playback_event, so the chip follows the same
    // setting the Analytics tab does.
    val monthlyRecaps by viewModel.monthlyRecaps.collectAsStateWithLifecycle()
    val nowPlaying by viewModel.nowPlayingVideoId.collectAsStateWithLifecycle()
    val youTubePlaylist by viewModel.youTubePlaylist.collectAsStateWithLifecycle(minActiveState = Lifecycle.State.RESUMED)
    val listCanvasSong by viewModel.listCanvasSong.collectAsStateWithLifecycle()
    val yourLocalPlaylist by viewModel.yourLocalPlaylist.collectAsStateWithLifecycle(minActiveState = Lifecycle.State.RESUMED)
    val favoritePlaylist by viewModel.favoritePlaylist.collectAsStateWithLifecycle(minActiveState = Lifecycle.State.RESUMED)
    val favoriteSongsViewModel: LibraryDynamicPlaylistViewModel = koinViewModel()
    val favoriteSongs by favoriteSongsViewModel.listFavoriteSong.collectAsStateWithLifecycle(minActiveState = Lifecycle.State.RESUMED)
    val downloadedPlaylist by viewModel.downloadedPlaylist.collectAsStateWithLifecycle()
    val favoritePodcasts by viewModel.favoritePodcasts.collectAsStateWithLifecycle(minActiveState = Lifecycle.State.RESUMED)
    val chartPlaylists by viewModel.chartPlaylists.collectAsStateWithLifecycle()
    val recentlyAdded by viewModel.recentlyAdded.collectAsStateWithLifecycle(minActiveState = Lifecycle.State.RESUMED)

    val selectionState = rememberSongSelectionState()
    val selectionViewModel: SongSelectionViewModel = koinViewModel()
    val sharedViewModel: SharedViewModel = koinInject()
    var showSelectionSheet by rememberSaveable { mutableStateOf(false) }
    var showSelectionAddToPlaylist by rememberSaveable { mutableStateOf(false) }
    val accountThumbnail by viewModel.accountThumbnail.collectAsStateWithLifecycle()
    var showAddSheet by remember { mutableStateOf(false) }
    var creatingYouTubePlaylist by rememberSaveable { mutableStateOf(false) }
    var showingUnselectedPlaylistCategories by remember { mutableStateOf(false) }
    var showRecentSongSheet by remember { mutableStateOf(false) }
    var recentSongForSheet by remember { mutableStateOf<SongEntity?>(null) }
    var playlistForMenu by remember { mutableStateOf<PlaylistType?>(null) }
    val playlistMenuLoading by viewModel.showLoadingDialog.collectAsStateWithLifecycle()

    val chipRowState = rememberScrollState()
    val yourLibraryListState = rememberLazyListState()
    val libraryGridStates = LibraryChipType.entries.associateWith { filter ->
        key(filter) { rememberLazyGridState() }
    }
    val density = LocalDensity.current
    val reloadDestination by sharedViewModel.reloadDestination.collectAsStateWithLifecycle()
    val localPlaylistSyncRevision by sharedViewModel.localPlaylistSyncRevision.collectAsStateWithLifecycle()
    val currentFilter by viewModel.currentScreen.collectAsStateWithLifecycle()
    val currentFilterRestored by viewModel.currentScreenRestored.collectAsStateWithLifecycle()
    val libraryFilterResetRequests by sharedViewModel.libraryFilterResetRequests.collectAsStateWithLifecycle()
    val selectLibraryCategory: (LibraryChipType) -> Unit = { category ->
        showingUnselectedPlaylistCategories = false
        viewModel.setCurrentScreen(category)
    }
    val libraryCategoryRow: @Composable () -> Unit = {
        LibraryCategoryRow(
            chipRowState = chipRowState,
            currentFilter = currentFilter,
            onSelected = selectLibraryCategory,
        )
    }
    val libraryCategoryRowSpacer: @Composable () -> Unit = {
        Spacer(Modifier.fillMaxWidth().height(libraryCategoryRowHeight))
    }
    LaunchedEffect(libraryFilterResetRequests) {
        if (sharedViewModel.consumeLibraryFilterResetRequest(libraryFilterResetRequests)) {
            showingUnselectedPlaylistCategories = false
            if (currentFilter != LibraryChipType.YOUR_LIBRARY) {
                viewModel.setCurrentScreen(LibraryChipType.YOUR_LIBRARY)
                yourLibraryListState.scrollToItem(0)
            }
        }
    }
    BackHandler {
        showingUnselectedPlaylistCategories = false
        if (currentFilter != LibraryChipType.YOUR_LIBRARY) {
            viewModel.setCurrentScreen(LibraryChipType.YOUR_LIBRARY)
        } else {
            navController.navigateToTabRoot(
                target = BottomNavScreen.Home,
                activeTab = BottomNavScreen.Library,
            )
        }
    }
    val sharedHeaderState = LocalSharedHeaderState.current
    val backToLibrary = remember(viewModel) {
        {
            showingUnselectedPlaylistCategories = false
            viewModel.setCurrentScreen(LibraryChipType.YOUR_LIBRARY)
        }
    }
    val showingYourPlaylistsPage =
        currentFilter == LibraryChipType.YOUTUBE_MUSIC_PLAYLIST || showingUnselectedPlaylistCategories
    SideEffect {
        sharedHeaderState?.libraryPlaylistBackAction = if (showingYourPlaylistsPage) backToLibrary else null
    }
    DisposableEffect(sharedHeaderState) {
        onDispose { sharedHeaderState?.libraryPlaylistBackAction = null }
    }
    val libraryCategories =
        remember(loggedIn) {
            buildList {
                add(LibraryChipType.YOUR_LIBRARY)
                if (loggedIn) add(LibraryChipType.YOUTUBE_MUSIC_PLAYLIST)
                add(LibraryChipType.LOCAL_PLAYLIST)
                add(LibraryChipType.FAVORITE_PODCAST)
                add(LibraryChipType.ARTISTS)
            }
        }

    LaunchedEffect(reloadDestination, currentFilter) {
        if (reloadDestination == LibraryDestination::class) {
            if (currentFilter == LibraryChipType.YOUR_LIBRARY) {
                yourLibraryListState.animateScrollToTopSmoothly()
            } else {
                libraryGridStates.getValue(currentFilter).animateScrollToItem(0)
            }
            sharedViewModel.reloadDestinationDone()
        }
    }

    val sharedHeaderHeightPx = with(density) { sharedHeaderHeight.toPx() }
    val offsetTracker = remember(currentFilter) { HeaderScrollOffsetTracker() }

    LaunchedEffect(currentFilter, sharedHeaderState, selectionState.isActive) {
        if (selectionState.isActive) {
            sharedHeaderState?.resetHeaderOffset()
            return@LaunchedEffect
        }
        val updateScrollVisibility: (Int, Int) -> Unit = { index, offset ->
            sharedHeaderState?.updateLibrarySurfaceVisibility(index > 0 || offset > 0)
            val newOffset = offsetTracker.update(
                index = index,
                offset = offset,
                currentHeaderOffsetPx = sharedHeaderState?.headerOffsetPx ?: 0f,
                maxHeightPx = sharedHeaderHeightPx,
            )
            sharedHeaderState?.updateHeaderOffset(newOffset)
        }
        if (currentFilter == LibraryChipType.YOUR_LIBRARY) {
            snapshotFlow {
                yourLibraryListState.firstVisibleItemIndex to
                    yourLibraryListState.firstVisibleItemScrollOffset
            }.collect { (index, offset) ->
                updateScrollVisibility(index, offset)
            }
        } else {
            val gridState = libraryGridStates.getValue(currentFilter)
            snapshotFlow { gridState.firstVisibleItemIndex to gridState.firstVisibleItemScrollOffset }
                .collect { (index, offset) ->
                    updateScrollVisibility(index, offset)
                }
        }
    }

    LaunchedEffect(loggedIn, accountThumbnail) {
        onAccountInfoChanged(if (loggedIn) Pair("signed-in", accountThumbnail) else null)
    }

    LaunchedEffect(localPlaylistSyncRevision, loggedIn) {
        if (localPlaylistSyncRevision > 0 && loggedIn) {
            viewModel.getLocalPlaylist(forceRefresh = true)
            viewModel.getYouTubePlaylist(forceRefresh = true)
        }
    }

    val libraryLifecycle = LocalLifecycleOwner.current.lifecycle
    val libraryLifecycleState by libraryLifecycle.currentStateFlow.collectAsStateWithLifecycle()
    val libraryIsResumed = libraryLifecycleState == Lifecycle.State.RESUMED
    LaunchedEffect(libraryIsResumed, favoriteSongsViewModel) {
        if (libraryIsResumed) favoriteSongsViewModel.observe(LibraryDynamicPlaylistType.Favorite)
    }
    LaunchedEffect(currentFilter, currentFilterRestored, libraryCategories, libraryIsResumed) {
        // First-visit database observers and remote library fetches start after the incoming
        // slide settles. Returning visits can still draw their existing StateFlow values.
        if (!libraryIsResumed) return@LaunchedEffect
        if (!currentFilterRestored) return@LaunchedEffect
        if (currentFilter == LibraryChipType.LOCAL_PLAYLIST && !showingUnselectedPlaylistCategories) {
            // Don't restore the old standalone category as the active Library screen.
            viewModel.setCurrentScreen(LibraryChipType.YOUR_LIBRARY)
            return@LaunchedEffect
        }
        if (currentFilter !in libraryCategories) {
            viewModel.setCurrentScreen(LibraryChipType.YOUR_LIBRARY)
            return@LaunchedEffect
        }
        if (currentFilter == LibraryChipType.CHART ||
            currentFilter == LibraryChipType.DOWNLOADED_PLAYLIST
        ) {
            // These categories remain unavailable. Reset persisted selections from older installs.
            viewModel.setCurrentScreen(LibraryChipType.YOUR_LIBRARY)
            return@LaunchedEffect
        }
        when (currentFilter) {
            LibraryChipType.YOUTUBE_MUSIC_PLAYLIST -> {
                if (!loggedIn) {
                    viewModel.setCurrentScreen(LibraryChipType.YOUR_LIBRARY)
                    return@LaunchedEffect
                }
                if (youTubePlaylist.data.isNullOrEmpty()) {
                    viewModel.getYouTubePlaylist()
                }
            }

            // Mix for you has its own nav tab now. The filter is persisted, so a build upgraded
            // while it was selected would land here with no chip to match — send it back to the
            // default. The enum value itself stays so older persisted values still parse.
            LibraryChipType.YOUTUBE_MIX_FOR_YOU -> {
                viewModel.setCurrentScreen(LibraryChipType.YOUR_LIBRARY)
            }

            LibraryChipType.YOUR_LIBRARY -> {
                viewModel.getCanvasSong()
                viewModel.getRecentlyAdded()
                viewModel.getLocalPlaylist()
                if (loggedIn && youTubePlaylist.data.isNullOrEmpty()) {
                    viewModel.getYouTubePlaylist()
                }
            }

            LibraryChipType.ARTISTS -> Unit // Observed and prefetched on Library entry for every category.

            LibraryChipType.LOCAL_PLAYLIST -> {
                viewModel.getLocalPlaylist()
            }

            LibraryChipType.FAVORITE_PLAYLIST -> {
                viewModel.getPlaylistFavorite()
            }

            LibraryChipType.DOWNLOADED_PLAYLIST -> {
                viewModel.getDownloadedPlaylist()
            }

            LibraryChipType.FAVORITE_PODCAST -> {
                viewModel.getFavoritePodcasts()
            }

            LibraryChipType.CHART -> {
                if (chartPlaylists.data.isNullOrEmpty()) {
                    viewModel.getChartPlaylists()
                }
            }

            LibraryChipType.WRAPPED -> {
                viewModel.getMonthlyRecaps()
            }
        }
    }

    val selectionBarHeight = if (selectionState.isActive) 64.dp else 0.dp
    val libraryContentPadding = innerPadding.copy(top = sharedHeaderHeight + selectionBarHeight)
    var libraryHeightPx by remember { mutableIntStateOf(0) }
    val artistTypography = typo()
    val artistRowHeightPx = with(density) {
        val title = artistTypography.titleSmall
        val subtitle = artistTypography.bodySmall
        val titleHeight = if (title.lineHeight.isUnspecified) title.fontSize.toPx() * 1.2f else title.lineHeight.toPx()
        val subtitleHeight = if (subtitle.lineHeight.isUnspecified) subtitle.fontSize.toPx() * 1.2f else subtitle.lineHeight.toPx()
        maxOf(48.dp.toPx(), titleHeight + subtitleHeight) + 20.dp.toPx()
    }
    val initialArtistCount = with(density) {
        initialArtistThumbnailCount(
            libraryHeightPx,
            libraryContentPadding.calculateTopPadding().toPx() +
                libraryContentPadding.calculateBottomPadding().toPx() + libraryCategoryRowHeight.toPx(),
            artistRowHeightPx,
        )
    }
    val artistThumbnailLoader = PreloadLibraryArtistThumbnails(
        songs = favoriteSongs,
        thumbnailCache = sharedViewModel.libraryArtistThumbnailCache,
        enabled = libraryIsResumed,
        preloadCount = initialArtistCount,
        retryKey = currentFilter,
    )
    val categorySwipeThresholdPx = with(density) { 72.dp.toPx() }
    val currentSwipeCategoryIndex = visibleLibraryCategories.indexOf(currentFilter)
    Box(
        Modifier
            .fillMaxSize()
            .onSizeChanged { libraryHeightPx = it.height }
            .clipToBounds()
            .pointerInput(currentFilter, categorySwipeThresholdPx, selectionState.isActive) {
                if (selectionState.isActive || currentSwipeCategoryIndex < 0) return@pointerInput
                var horizontalDragPx = 0f
                detectHorizontalDragGestures(
                    onDragStart = { horizontalDragPx = 0f },
                    onDragCancel = { horizontalDragPx = 0f },
                    onHorizontalDrag = { change, dragAmount ->
                        horizontalDragPx += dragAmount
                        change.consume()
                    },
                    onDragEnd = {
                        val nextIndex = when {
                            horizontalDragPx <= -categorySwipeThresholdPx -> currentSwipeCategoryIndex + 1
                            horizontalDragPx >= categorySwipeThresholdPx -> currentSwipeCategoryIndex - 1
                            else -> currentSwipeCategoryIndex
                        }
                        if (nextIndex in visibleLibraryCategories.indices) {
                            selectLibraryCategory(visibleLibraryCategories[nextIndex])
                        }
                    },
                )
            },
    ) {
        AnimatedContent(
            targetState = currentFilter,
            modifier = Modifier.fillMaxSize(),
            transitionSpec = {
                if (useFadeForCategoryTransitions) {
                    fadeIn(tween(180)).togetherWith(fadeOut(tween(180)))
                } else if (useSlideV2ForCategoryTransitions) {
                    val movingForward = libraryCategories.indexOf(targetState) >= libraryCategories.indexOf(initialState)
                    com.maxrave.simpmusic.ui.component.slideV2EnterTransition(movingForward)
                        .togetherWith(com.maxrave.simpmusic.ui.component.slideV2ExitTransition(movingForward))
                        .using(null)
                } else {
                    val direction =
                        if (libraryCategories.indexOf(targetState) >= libraryCategories.indexOf(initialState)) {
                            AnimatedContentTransitionScope.SlideDirection.Left
                        } else {
                            AnimatedContentTransitionScope.SlideDirection.Right
                        }
                    (slideIntoContainer(direction, tween(260)) + fadeIn(tween(180))).togetherWith(
                        slideOutOfContainer(direction, tween(260)) + fadeOut(tween(180)),
                    )
                }
            },
            label = if (useFadeForCategoryTransitions) "library_filter_content_fade" else "library_filter_content_slide",
        ) { filter ->
            when (filter) {
                LibraryChipType.YOUR_LIBRARY -> {
                    // The scroll direction is used only by this collector; reading it as a delegated
                    // value in the screen scope needlessly recomposes the library as it scrolls.
                    val isScrollingUpState = yourLibraryListState.isScrollingUp()
                    LaunchedEffect(yourLibraryListState) {
                        snapshotFlow {
                            yourLibraryListState.firstVisibleItemIndex to isScrollingUpState.value
                        }.collect { (index, scrollingUp) ->
                            if (index <= 1) {
                                onScrolling.invoke(true)
                            } else {
                                onScrolling.invoke(scrollingUp)
                            }
                        }
                    }
                    LazyColumn(
                        contentPadding = libraryContentPadding,
                        state = yourLibraryListState,
                    ) {
                        item(key = "library-category-row-spacer", contentType = "library-category-row-spacer") {
                            libraryCategoryRowSpacer()
                        }
                        item(key = "library-tiles", contentType = "library-tiles") {
                            LibraryTilingBox(navController)
                        }

                        if (!listCanvasSong.data.isNullOrEmpty()) {
                            item(key = "library-canvas", contentType = "library-shelf") {
                                LibraryItem(
                                    state =
                                        LibraryItemState(
                                            type = LibraryItemType.CanvasSong,
                                            data = listCanvasSong.data ?: emptyList(),
                                            isLoading = listCanvasSong is LocalResource.Loading,
                                        ),
                                    navController = navController,
                                )
                            }
                        }

                        item(key = "library-your-playlists", contentType = "playlist-shelf") {
                            YourPlaylistsShelf(
                                playlists = if (loggedIn) youTubePlaylist.data.orEmpty().take(2) else emptyList(),
                                localPlaylists = yourLocalPlaylist.data.orEmpty(),
                                showLocalPlaylists = !loggedIn,
                                onCreatePlaylist = {
                                    creatingYouTubePlaylist = loggedIn
                                    showAddSheet = true
                                },
                                onSeeAll = {
                                    if (loggedIn) {
                                        showingUnselectedPlaylistCategories = false
                                        viewModel.setCurrentScreen(LibraryChipType.YOUTUBE_MUSIC_PLAYLIST)
                                    } else {
                                        // Show the local playlist grid without making any category
                                        // chip look selected. Keep the user's saved filter intact.
                                        showingUnselectedPlaylistCategories = true
                                        viewModel.setCurrentScreen(LibraryChipType.LOCAL_PLAYLIST, persist = false)
                                    }
                                },
                                onPlaylistClick = { playlist ->
                                    navController.navigate(
                                        PlaylistDestination(
                                            playlistId = playlist.browseId,
                                            isYourYouTubePlaylist = true,
                                        ),
                                    )
                                },
                                onLocalPlaylistClick = { playlist ->
                                    navController.navigate(LocalPlaylistDestination(playlist.id))
                                },
                                onPlaylistLongClick = { playlistForMenu = it },
                                onLocalPlaylistLongClick = { playlistForMenu = it },
                                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                            )
                        }

                        item(key = "library-recently-added-title", contentType = "shelf-title") {
                            Text(
                                text = stringResource(Res.string.library_history),
                                style = typo().headlineMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            top = 15.dp,
                                            start = AppContentHorizontalInset,
                                            end = AppContentHorizontalInset,
                                        )
                                        .height(35.dp)
                                        .wrapContentHeight(align = Alignment.CenterVertically),
                            )
                        }
                        val recentItems = (recentlyAdded as? LocalResource.Success)?.data.orEmpty()
                        if (recentItems.isNotEmpty()) {
                            items(
                                items = recentItems,
                                key = { item -> "recent:${item.libraryItemKey()}" },
                                contentType = { item -> item.libraryItemContentType() },
                            ) { item ->
                                LibraryRecentlyAddedRow(
                                    item = item,
                                    playingVideoId = nowPlaying,
                                    navController = navController,
                                    selectionState = selectionState,
                                    viewModel = viewModel,
                                    sharedViewModel = sharedViewModel,
                                    onMoreClick = { song ->
                                        recentSongForSheet = song
                                        showRecentSongSheet = true
                                    },
                                )
                            }
                        } else {
                            item(
                                key = if (recentlyAdded is LocalResource.Loading) "library-recent-loading" else "library-recent-empty",
                                contentType = "library-recent-placeholder",
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().height(130.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (recentlyAdded is LocalResource.Loading) {
                                        CenterLoadingBox(Modifier.wrapContentHeight())
                                    } else {
                                        Text(
                                            text = stringResource(Res.string.library_history),
                                            style = typo().bodyMedium,
                                        )
                                    }
                                }
                            }
                        }
                        item(key = "library-end-of-page", contentType = "end-of-page") {
                            EndOfPage(bottomPaddingAlreadyApplied = innerPadding.calculateBottomPadding())
                        }
                    }
                }

                LibraryChipType.YOUTUBE_MUSIC_PLAYLIST -> {
                    GridLibraryPlaylist(
                        navController,
                        libraryContentPadding,
                        youTubePlaylist,
                        emptyText = Res.string.no_YouTube_playlists,
                        onPlaylistLongClick = { playlistForMenu = it },
                        onScrolling = onScrolling,
                        state = libraryGridStates.getValue(filter),
                        header = libraryCategoryRowSpacer,
                        showSubtitles = false,
                        createNewPlaylist = {
                            creatingYouTubePlaylist = true
                            showAddSheet = true
                        },
                    ) {
                        viewModel.getYouTubePlaylist(forceRefresh = true)
                    }
                }

                // Nothing to draw: MixForYouScreen owns this content now, and the effect above bounces
                // the filter back to YOUR_LIBRARY the moment it lands here.
                LibraryChipType.YOUTUBE_MIX_FOR_YOU -> Unit

                LibraryChipType.ARTISTS -> {
                    val gridState = libraryGridStates.getValue(filter)
                    val isScrollingUpState = gridState.isScrollingUp()
                    LaunchedEffect(gridState) {
                        snapshotFlow {
                            gridState.firstVisibleItemIndex to isScrollingUpState.value
                        }.collect { (index, scrollingUp) ->
                            onScrolling.invoke(index <= 1 || scrollingUp)
                        }
                    }
                    FavoriteArtistsGrid(
                        songs = favoriteSongs,
                        state = gridState,
                        thumbnailCache = sharedViewModel.libraryArtistThumbnailCache,
                        thumbnailLoader = artistThumbnailLoader,
                        contentPadding = libraryContentPadding,
                        categorySpacerHeight = libraryCategoryRowHeight,
                        onArtistClick = { channelId ->
                            navController.navigate(
                                LibraryDynamicPlaylistDestination(
                                    type = LibraryDynamicPlaylistType.ArtistLiked(channelId).toStringParams(),
                                ),
                            )
                        },
                    )
                }

                LibraryChipType.LOCAL_PLAYLIST -> {
                    GridLibraryPlaylist(
                        navController,
                        libraryContentPadding,
                        yourLocalPlaylist,
                        onPlaylistLongClick = { playlistForMenu = it },
                        onScrolling = onScrolling,
                        state = libraryGridStates.getValue(filter),
                        header = libraryCategoryRowSpacer,
                        emptyText = Res.string.no_playlists_added,
                        showSubtitles = false,
                        createNewPlaylist = {
                            creatingYouTubePlaylist = loggedIn
                            showAddSheet = true
                        },
                    ) {
                        viewModel.getLocalPlaylist(forceRefresh = true)
                    }
                }

                LibraryChipType.FAVORITE_PLAYLIST -> {
                    GridLibraryPlaylist(
                        navController,
                        libraryContentPadding,
                        favoritePlaylist,
                        emptyText = Res.string.no_favorite_playlists,
                        onScrolling = onScrolling,
                        state = libraryGridStates.getValue(filter),
                        header = libraryCategoryRowSpacer,
                    ) {
                        viewModel.getPlaylistFavorite(forceRefresh = true)
                    }
                }

                LibraryChipType.DOWNLOADED_PLAYLIST -> {
                    GridLibraryPlaylist(
                        navController,
                        libraryContentPadding,
                        downloadedPlaylist,
                        emptyText = Res.string.no_playlists_downloaded,
                        onScrolling = onScrolling,
                        state = libraryGridStates.getValue(filter),
                        header = libraryCategoryRowSpacer,
                    ) {
                        viewModel.getDownloadedPlaylist(forceRefresh = true)
                    }
                }

                LibraryChipType.FAVORITE_PODCAST -> {
                    GridLibraryPlaylist(
                        navController,
                        libraryContentPadding,
                        favoritePodcasts,
                        emptyText = Res.string.no_favorite_podcasts,
                        onScrolling = onScrolling,
                        state = libraryGridStates.getValue(filter),
                        header = {
                            Column {
                                libraryCategoryRowSpacer()
                                YourEpisodesLibraryTile(navController)
                                ContinueListeningLibraryTile(navController)
                            }
                        },
                    ) {
                        viewModel.getFavoritePodcasts(forceRefresh = true)
                    }
                }

                LibraryChipType.CHART -> {
                    GridLibraryPlaylist(
                        navController,
                        libraryContentPadding,
                        chartPlaylists,
                        emptyText = Res.string.no_charts_found,
                        onScrolling = onScrolling,
                        state = libraryGridStates.getValue(filter),
                        header = libraryCategoryRowSpacer,
                    ) {
                        viewModel.getChartPlaylists(forceRefresh = true)
                    }
                }

                LibraryChipType.WRAPPED -> {
                    LibraryWrappedTab(
                        navController = navController,
                        contentPadding = libraryContentPadding,
                        recaps = monthlyRecaps,
                        onScrolling = onScrolling,
                        state = libraryGridStates.getValue(filter),
                        leadingHeader = libraryCategoryRowSpacer,
                    ) {
                        viewModel.getMonthlyRecaps(forceRefresh = true)
                    }
                }
            }
        }
        // Keep one row outside AnimatedContent so page changes only update its selected chip.
        // Each page reserves the row's height as its first lazy item; this row follows that page's
        // scroll offset and leaves the viewport naturally when the user scrolls down.
        LibraryCategoryRowOverlay(
            modifier = Modifier.align(Alignment.TopCenter),
            listState = yourLibraryListState.takeIf { currentFilter == LibraryChipType.YOUR_LIBRARY },
            gridState = libraryGridStates[currentFilter].takeIf { currentFilter != LibraryChipType.YOUR_LIBRARY },
            startOffset = sharedHeaderHeight + selectionBarHeight,
            rowHeight = libraryCategoryRowHeight,
            rowSharesHeaderItem =
                currentFilter == LibraryChipType.FAVORITE_PODCAST ||
                    currentFilter == LibraryChipType.WRAPPED,
            content = libraryCategoryRow,
        )
        AnimatedVisibility(
            visible = selectionState.isActive,
            modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth().padding(top = sharedHeaderHeight),
        ) {
            SongSelectionTopAppBar(
                state = selectionState,
                windowInsets = WindowInsets(0),
                onSelectAll = {
                    selectionState.toggleSelectAll(
                        (recentlyAdded.data ?: emptyList())
                            .filterIsInstance<SongEntity>()
                            .map { it.videoId },
                    )
                },
                onOpenActions = { showSelectionSheet = true },
            )
        }
    }
    playlistForMenu?.let { selected ->
        // Keep the selected metadata current while database observers deliver edits.
        val playlist = when (selected) {
            is LocalPlaylistEntity -> yourLocalPlaylist.data?.firstOrNull { it.id == selected.id } ?: selected
            is PlaylistsResult -> youTubePlaylist.data?.firstOrNull { it.browseId == selected.browseId } ?: selected
            else -> selected
        }
        val playlistKey = when (selected) {
            is LocalPlaylistEntity -> "local:${selected.id}"
            is PlaylistsResult -> "youtube:${selected.browseId}"
            else -> selected
        }
        key(playlistKey) {
            LibraryPlaylistMenu(
                playlist = playlist,
                viewModel = viewModel,
                onAddToQueue = { sharedViewModel.addListToQueue(ArrayList(it)) },
                onDismiss = { playlistForMenu = null },
            )
        }
    }
    if (playlistMenuLoading.first) {
        LoadingDialog(true, playlistMenuLoading.second)
    }
    if (showRecentSongSheet) {
        recentSongForSheet?.let { song ->
            NowPlayingBottomSheet(
                onDismiss = {
                    showRecentSongSheet = false
                    recentSongForSheet = null
                },
                navController = navController,
                song = song,
                onLibraryDelete = { viewModel.deleteSong(song.videoId) },
            )
        }
    }
    if (showAddSheet) {
        CreatePlaylistDialog(
            onDismissRequest = {
                showAddSheet = false
                creatingYouTubePlaylist = false
            },
            onCreate = { name ->
                if (creatingYouTubePlaylist) {
                    viewModel.createYouTubeMusicPlaylist(name)
                } else {
                    viewModel.createPlaylist(name)
                }
            },
        )
    }
    if (showSelectionSheet) {
        val selectedIds = selectionState.selected.toList()
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
        )
    }
    if (showSelectionAddToPlaylist) {
        val selectedIds = selectionState.selected.toList()
        val localPlaylists by selectionViewModel.listLocalPlaylist.collectAsStateWithLifecycle()
        val availableSongs = (recentlyAdded.data.orEmpty().filterIsInstance<SongEntity>()).filter { it.videoId in selectedIds }
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
}

@Composable
private fun LibraryCategoryRowOverlay(
    modifier: Modifier,
    listState: LazyListState?,
    gridState: LazyGridState?,
    startOffset: Dp,
    rowHeight: Dp,
    rowSharesHeaderItem: Boolean,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val startOffsetPx = with(density) { startOffset.roundToPx() }
    val rowHeightPx = with(density) { rowHeight.roundToPx() }
    val rowOffsetPx by remember(listState, gridState, startOffsetPx, rowHeightPx, rowSharesHeaderItem) {
        derivedStateOf {
            val (firstVisibleIndex, firstVisibleOffset) = when {
                listState != null -> listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset
                gridState != null -> gridState.firstVisibleItemIndex to gridState.firstVisibleItemScrollOffset
                else -> 0 to 0
            }
            when {
                firstVisibleIndex == 0 -> startOffsetPx - firstVisibleOffset
                firstVisibleIndex == 1 && !rowSharesHeaderItem ->
                    startOffsetPx - rowHeightPx - firstVisibleOffset
                else -> -rowHeightPx
            }
        }
    }
    val rowOffset = with(density) { rowOffsetPx.toDp() }

    Box(
        modifier = modifier.offset(y = rowOffset).fillMaxWidth().height(rowHeight),
        contentAlignment = Alignment.CenterStart,
    ) {
        content()
    }
}

@Composable
internal fun LibraryCategoryRow(
    chipRowState: ScrollState,
    currentFilter: LibraryChipType,
    onSelected: (LibraryChipType) -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = AppContentHorizontalInset)
                .horizontalScroll(chipRowState)
                .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        visibleLibraryCategories.forEach { type ->
            Chip(
                isAnimated = false,
                isSelected = currentFilter == type,
                animateLabel = false,
                text =
                    when (type) {
                        LibraryChipType.YOUR_LIBRARY -> stringResource(Res.string.your_library)
                        LibraryChipType.FAVORITE_PODCAST -> stringResource(Res.string.podcasts)
                        LibraryChipType.ARTISTS -> stringResource(Res.string.artists)
                        else -> error("Unexpected Library category tab: $type")
                    },
            ) {
                if (currentFilter != type) onSelected(type)
            }
        }
    }
}
