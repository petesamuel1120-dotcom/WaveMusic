package com.maxrave.simpmusic.ui.screen.other

import simpmusic.composeapp.generated.resources.back_to_search
import simpmusic.composeapp.generated.resources.clear_search
import simpmusic.composeapp.generated.resources.remove_search_history_item
import simpmusic.composeapp.generated.resources.search
import simpmusic.composeapp.generated.resources.search_by_voice
import simpmusic.composeapp.generated.resources.search_history
import simpmusic.composeapp.generated.resources.search_suggestion

import com.maxrave.simpmusic.ui.component.searchFieldGlass
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.maxrave.common.Config
import com.maxrave.domain.data.entities.SongEntity
import com.maxrave.domain.data.model.browse.album.Track
import com.maxrave.domain.data.model.intent.GenericIntent
import com.maxrave.domain.data.model.searchResult.albums.AlbumsResult
import com.maxrave.domain.data.model.searchResult.artists.ArtistsResult
import com.maxrave.domain.data.model.searchResult.playlists.PlaylistsResult
import com.maxrave.domain.data.model.searchResult.songs.SongsResult
import com.maxrave.domain.data.model.searchResult.videos.VideosResult
import com.maxrave.domain.data.type.SearchResultType
import com.maxrave.domain.mediaservice.handler.PlaylistType
import com.maxrave.domain.mediaservice.handler.QueueData
import com.maxrave.domain.utils.connectArtists
import com.maxrave.domain.utils.toSongEntity
import com.maxrave.domain.utils.toTrack
import com.maxrave.simpmusic.Platform
import com.maxrave.simpmusic.getPlatform
import com.maxrave.simpmusic.ui.component.AddToPlaylistModalBottomSheet
import com.maxrave.simpmusic.ui.component.rememberHolderPainter
import com.maxrave.simpmusic.extension.getStringBlocking
import com.maxrave.simpmusic.extension.toAppDeepLinkOrNull
import com.maxrave.simpmusic.ui.component.ArtistFullWidthItems
import com.maxrave.simpmusic.ui.component.AppContentHorizontalInset
import com.maxrave.simpmusic.ui.component.AppSearchBarTheme
import com.maxrave.simpmusic.ui.component.BOTTOM_NAV_TRANSITION_DURATION_MILLIS
import com.maxrave.simpmusic.ui.component.Chip
import com.maxrave.simpmusic.ui.component.InPageSearchInputField
import com.maxrave.simpmusic.ui.component.liquidGlass
import com.maxrave.simpmusic.ui.component.isCompactLandscapeChrome
import com.maxrave.simpmusic.ui.component.appSearchBarColors
import com.maxrave.simpmusic.ui.component.appSearchBarTextStyle
import com.maxrave.simpmusic.ui.component.inPageSearchBarTextStyle
import com.maxrave.simpmusic.ui.theme.LocalLiquidGlassEnabled
import com.maxrave.simpmusic.ui.component.EndOfPage
import com.maxrave.simpmusic.ui.component.LocalPageBottomInset
import com.maxrave.simpmusic.ui.component.NowPlayingBottomSheet
import com.maxrave.simpmusic.ui.component.PlaylistFullWidthItems
import com.maxrave.simpmusic.ui.component.ShimmerSearchItem
import com.maxrave.simpmusic.ui.component.SongFullWidthItems
import com.maxrave.simpmusic.ui.component.selection.SelectedSongsBottomSheet
import com.maxrave.simpmusic.ui.component.selection.SongSelectionState
import com.maxrave.simpmusic.ui.component.selection.SongSelectionTopAppBar
import com.maxrave.simpmusic.ui.component.selection.rememberSongSelectionState
import com.maxrave.simpmusic.ui.icon.ArrowOutward
import com.maxrave.simpmusic.ui.icon.ArrowBackIosNew
import com.maxrave.simpmusic.ui.icon.Close
import com.maxrave.simpmusic.ui.icon.History
import com.maxrave.simpmusic.ui.icon.Mic
import com.maxrave.simpmusic.ui.component.rememberDialogMotionAnchor
import com.maxrave.simpmusic.ui.icon.Search
import com.maxrave.simpmusic.ui.icon.SimpIcons
import com.maxrave.simpmusic.ui.navigation.destination.list.AlbumDestination
import com.maxrave.simpmusic.ui.navigation.destination.list.ArtistDestination
import com.maxrave.simpmusic.ui.navigation.destination.list.PlaylistDestination
import com.maxrave.simpmusic.ui.navigation.destination.list.PodcastDestination
import com.maxrave.simpmusic.ui.navigation.destination.search.SearchDestination
import com.maxrave.simpmusic.ui.theme.typo
import com.maxrave.simpmusic.viewModel.SearchScreenUIState
import com.maxrave.simpmusic.viewModel.SearchType
import com.maxrave.simpmusic.viewModel.SearchViewModel
import com.maxrave.simpmusic.viewModel.SharedViewModel
import com.maxrave.simpmusic.viewModel.SongSelectionViewModel
import com.maxrave.simpmusic.viewModel.toStringRes
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import simpmusic.composeapp.generated.resources.Res
import simpmusic.composeapp.generated.resources.albums
import simpmusic.composeapp.generated.resources.artist
import simpmusic.composeapp.generated.resources.artists
import simpmusic.composeapp.generated.resources.error_occurred
import simpmusic.composeapp.generated.resources.in_search
import simpmusic.composeapp.generated.resources.no_results_found
import simpmusic.composeapp.generated.resources.playlists
import simpmusic.composeapp.generated.resources.podcasts
import simpmusic.composeapp.generated.resources.retry
import simpmusic.composeapp.generated.resources.search_for
import simpmusic.composeapp.generated.resources.search_music
import simpmusic.composeapp.generated.resources.search_music_description
import simpmusic.composeapp.generated.resources.song
import simpmusic.composeapp.generated.resources.what_do_you_want_to_listen_to

private const val SEARCH_PLACEHOLDER_COUNT = 5
private const val SEARCH_RESELECT_KEYBOARD_DELAY_MILLIS =
    BOTTOM_NAV_TRANSITION_DURATION_MILLIS * 70L / 100L

@Stable
class SearchHeaderState(
    val query: MutableState<String>,
    val focused: MutableState<Boolean>,
    val submitted: MutableState<Boolean>,
    val placeholderIndex: MutableIntState,
    val focusRequester: FocusRequester,
) {
    var selectionState by mutableStateOf<SongSelectionState?>(null)
    var showingResults by mutableStateOf(false)
    var onSubmit by mutableStateOf<(String) -> Unit>({})
    var onClear by mutableStateOf<() -> Unit>({})
    var onBackToSearch by mutableStateOf<() -> Unit>({})
    var onSelectAll by mutableStateOf<() -> Unit>({})
    var onOpenSelectionActions by mutableStateOf<() -> Unit>({})
}

@Composable
fun rememberSearchHeaderState(): SearchHeaderState {
    val query = rememberSaveable { mutableStateOf("") }
    val focused = remember { mutableStateOf(false) }
    val submitted = rememberSaveable { mutableStateOf(false) }
    val placeholderIndex = remember { mutableIntStateOf(0) }
    val focusRequester = remember { FocusRequester() }
    return remember { SearchHeaderState(query, focused, submitted, placeholderIndex, focusRequester) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavController,
    sharedHeaderHeight: Dp,
    headerState: SearchHeaderState,
    searchViewModel: SearchViewModel = koinInject(),
    sharedViewModel: SharedViewModel = koinInject(),
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val searchScreenState by searchViewModel.searchScreenState.collectAsStateWithLifecycle()
    val uiState by searchViewModel.searchScreenUIState.collectAsStateWithLifecycle()
    val searchHistory by searchViewModel.searchHistory.collectAsStateWithLifecycle()

    var searchText by headerState.query
    var isSearchSubmitted by headerState.submitted

    val focusRequester = headerState.focusRequester
    var isFocused by headerState.focused
    // Keep the visible mode in sync with the shared header, including restored tabs and
    // repeated submissions of the same query. Submission must not depend on a state effect.
    val searchUIType =
        when {
            searchText.isNotEmpty() && isSearchSubmitted -> SearchUIType.SEARCH_RESULTS
            searchText.isNotEmpty() -> SearchUIType.SEARCH_SUGGESTIONS
            searchHistory.isEmpty() -> SearchUIType.EMPTY
            isFocused -> SearchUIType.SEARCH_HISTORY
            else -> SearchUIType.EMPTY
        }

    // The bar floats over an empty initial state, history, suggestions, or results.
    val suggestionsState = rememberLazyListState()
    val historyState = rememberLazyListState()
    val resultsState = rememberLazyListState()
    val searchBarHeight = sharedHeaderHeight
    var currentPlaceholderIndex by headerState.placeholderIndex

    // Animate placeholder - pause when focused
    LaunchedEffect(isFocused) {
        while (!isFocused) {
            delay(3000) // Change every 3 seconds
            currentPlaceholderIndex = (currentPlaceholderIndex + 1) % SEARCH_PLACEHOLDER_COUNT
        }
    }

    var sheetSong by remember { mutableStateOf<SongEntity?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) }

    val selectionState = rememberSongSelectionState()
    val selectionViewModel: SongSelectionViewModel = koinViewModel()
    var showSelectionSheet by rememberSaveable { mutableStateOf(false) }
    var showSelectionAddToPlaylist by rememberSaveable { mutableStateOf(false) }
    val currentVideoId by searchViewModel.nowPlayingVideoId.collectAsStateWithLifecycle()
    val chipRowState = rememberScrollState()
    val pullToRefreshState = rememberPullToRefreshState()

    val onMoreClick: (SongEntity) -> Unit = { song ->
        sheetSong = song
        showBottomSheet = true
    }
    val onSearchTypeSelected: (SearchType) -> Unit = { type ->
        searchViewModel.setSearchType(type)
        val query = searchText.trim()
        if (searchUIType == SearchUIType.SEARCH_RESULTS && query.isNotEmpty()) {
            when (type) {
                SearchType.ALL -> searchViewModel.searchAll(query)
                SearchType.SONGS -> searchViewModel.searchSongs(query)
                SearchType.VIDEOS -> searchViewModel.searchVideos(query)
                SearchType.ALBUMS -> searchViewModel.searchAlbums(query)
                SearchType.ARTISTS -> searchViewModel.searchArtists(query)
                SearchType.PLAYLISTS -> searchViewModel.searchPlaylists(query)
                SearchType.FEATURED_PLAYLISTS -> searchViewModel.searchFeaturedPlaylist(query)
                SearchType.PODCASTS -> searchViewModel.searchPodcast(query)
            }
        }
    }

    val latestHeaderSubmit =
        rememberUpdatedState<(String) -> Unit> { text ->
            val query = text.trim()
            // A pasted YouTube link is a destination, not a query. Translating it into the app's
            // deep link hands it to the same intent flow that handles shared links.
            val deepLink = query.toAppDeepLinkOrNull()
            if (deepLink != null) {
                focusManager.clearFocus(force = true)
                isFocused = false
                keyboardController?.hide()
                sharedViewModel.setIntent(GenericIntent(data = deepLink))
            } else if (query.isNotEmpty()) {
                searchText = query
                isSearchSubmitted = true
                focusManager.clearFocus(force = true)
                isFocused = false
                keyboardController?.hide()
                searchViewModel.insertSearchHistory(query)
                when (searchScreenState.searchType) {
                    SearchType.ALL -> searchViewModel.searchAll(query)
                    SearchType.SONGS -> searchViewModel.searchSongs(query)
                    SearchType.VIDEOS -> searchViewModel.searchVideos(query)
                    SearchType.ALBUMS -> searchViewModel.searchAlbums(query)
                    SearchType.ARTISTS -> searchViewModel.searchArtists(query)
                    SearchType.PLAYLISTS -> searchViewModel.searchPlaylists(query)
                    SearchType.FEATURED_PLAYLISTS -> searchViewModel.searchFeaturedPlaylist(query)
                    SearchType.PODCASTS -> searchViewModel.searchPodcast(query)
                }
            }
        }
    val latestHeaderClear = rememberUpdatedState<() -> Unit> {
        searchText = ""
        isSearchSubmitted = false
    }
    val latestHeaderBackToSearch = rememberUpdatedState<() -> Unit> {
        selectionState.exit()
        focusManager.clearFocus(force = true)
        isFocused = false
        keyboardController?.hide()
        searchText = ""
        isSearchSubmitted = false
    }
    val latestHeaderSelectAll =
        rememberUpdatedState<() -> Unit> {
            val visible =
                when (searchScreenState.searchType) {
                    SearchType.SONGS -> searchScreenState.searchSongsResult.map { it.videoId }
                    SearchType.ALL ->
                        searchScreenState.searchAllResult.mapNotNull {
                            (it as? SongsResult)?.videoId
                        }
                    else -> emptyList()
                }
            selectionState.toggleSelectAll(visible)
        }
    val latestHeaderOpenActions = rememberUpdatedState<() -> Unit> { showSelectionSheet = true }
    val headerSubmit: (String) -> Unit = remember { { query -> latestHeaderSubmit.value(query) } }
    val headerClear: () -> Unit = remember { { latestHeaderClear.value() } }
    val headerBackToSearch: () -> Unit = remember { { latestHeaderBackToSearch.value() } }
    val headerSelectAll: () -> Unit = remember { { latestHeaderSelectAll.value() } }
    val headerOpenActions: () -> Unit = remember { { latestHeaderOpenActions.value() } }
    SideEffect {
        headerState.selectionState = selectionState
        headerState.showingResults = searchUIType == SearchUIType.SEARCH_RESULTS
        headerState.onSubmit = headerSubmit
        headerState.onClear = headerClear
        headerState.onBackToSearch = headerBackToSearch
        headerState.onSelectAll = headerSelectAll
        headerState.onOpenSelectionActions = headerOpenActions
    }

    LaunchedEffect(searchText, isFocused, isSearchSubmitted) {
        if (searchText.isNotEmpty() && isFocused && !isSearchSubmitted) {
            searchViewModel.suggestQuery(searchText)
        }
    }

    //On search icon click while on search screen, open keyboard. Android only feature
    if (getPlatform() == Platform.Android) {
        val reloadDestination by sharedViewModel.reloadDestination.collectAsStateWithLifecycle()
        LaunchedEffect(reloadDestination) {
            if (reloadDestination == SearchDestination::class) {
                if (!selectionState.isActive) {
                    // Let the page and navbar pill finish their shared transition before the
                    // keyboard changes the viewport. If the user has already left Search, don't
                    // reopen the keyboard over the destination they chose.
                    delay(SEARCH_RESELECT_KEYBOARD_DELAY_MILLIS)
                    if (navController.currentDestination?.hasRoute(SearchDestination::class) == true) {
                        isSearchSubmitted = false
                        focusRequester.requestFocus()
                        keyboardController?.show()
                    }
                }
                sharedViewModel.reloadDestinationDone()
            }
        }
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
        val availableSongs = ((searchScreenState.searchSongsResult + searchScreenState.searchAllResult.filterIsInstance<SongsResult>()).map { it.toTrack().toSongEntity() }).filter { it.videoId in selectedIds }
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
                sheetSong = null
            },
            navController = navController,
            song = sheetSong,
        )
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.Transparent),
    ) {
        // Content scrolls under the shared header, so leave enough top space to keep
        // the first item clear of it.
        Crossfade(
            targetState = searchUIType,
            modifier =
                Modifier
                    .fillMaxSize(),
        ) {
            when (it) {
                SearchUIType.SEARCH_SUGGESTIONS -> {
                    LazyColumn(
                        Modifier.padding(horizontal = AppContentHorizontalInset),
                        state = suggestionsState,
                        contentPadding = PaddingValues(top = searchBarHeight, bottom = 10.dp),
                    ) {
                        items(searchScreenState.suggestYTItems) { item ->
                            SuggestItemRow(
                                searchResult = item,
                                onItemClick = { item ->
                                    when (item) {
                                        is VideosResult -> {
                                            keyboardController?.hide()
                                            focusManager.clearFocus(force = true)
                                            searchViewModel.playVideo(item.toTrack())
                                        }

                                        is SongsResult -> {
                                            val firstTrack: Track = item.toTrack()
                                            searchViewModel.setQueueData(
                                                QueueData.Data(
                                                    listTracks = arrayListOf(firstTrack),
                                                    firstPlayedTrack = firstTrack,
                                                    playlistId = "RDAMVM${firstTrack.videoId}",
                                                    playlistName = "\"${searchText}\" ${getStringBlocking(Res.string.in_search)}",
                                                    playlistType = PlaylistType.RADIO,
                                                    continuation = null,
                                                ),
                                            )
                                            keyboardController?.hide()
                                            focusManager.clearFocus(force = true)
                                            searchViewModel.loadMediaItem(firstTrack, type = Config.SONG_CLICK)
                                        }

                                        is ArtistsResult -> {
                                            navController.navigate(
                                                ArtistDestination(item.browseId),
                                            )
                                        }

                                        is AlbumsResult -> {
                                            navController.navigate(
                                                AlbumDestination(item.browseId),
                                            )
                                        }

                                        is PlaylistsResult -> {
                                            if (item.resultType == "Podcast") {
                                                navController.navigate(PodcastDestination(item.browseId))
                                            } else {
                                                navController.navigate(PlaylistDestination(item.browseId))
                                            }
                                        }
                                    }
                                },
                            )
                        }
                        items(searchScreenState.suggestQueries) { suggestion ->
                            Row(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = ripple(),
                                            onClick = { headerSubmit(suggestion) },
                                        ).padding(horizontal = 12.dp, vertical = 2.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = suggestion,
                                    style = typo().bodyMedium,
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                IconButton(
                                    onClick = {
                                        isSearchSubmitted = false
                                        searchText = suggestion
                                        focusRequester.requestFocus()
                                    },
                                ) {
                                    Icon(
                                        imageVector = SimpIcons.ArrowOutward,
                                        contentDescription = stringResource(Res.string.search_suggestion),
                                        modifier = Modifier.size(24.dp).graphicsLayer { scaleX = -1f },
                                    )
                                }
                            }
                        }
                        item {
                            EndOfPage()
                        }
                    }
                }

                SearchUIType.SEARCH_HISTORY -> {
                    // Search history state
                    Column(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(horizontal = AppContentHorizontalInset),
                    ) {
                        LazyColumn(
                            state = historyState,
                            contentPadding = PaddingValues(top = searchBarHeight, bottom = 10.dp),
                        ) {
                            items(searchHistory) { historyItem ->
                                Row(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .clickable { headerSubmit(historyItem) }
                                            .padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = SimpIcons.History,
                                    contentDescription = stringResource(Res.string.search_history),
                                    modifier = Modifier.size(24.dp),
                                )
                                Spacer(modifier = Modifier.padding(horizontal = 12.dp))
                                Text(
                                    text = historyItem,
                                    style = typo().bodyMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f),
                                )
                                IconButton(
                                    onClick = { searchViewModel.deleteSearchHistoryItem(historyItem) },
                                ) {
                                    Icon(
                                        imageVector = SimpIcons.Close,
                                        contentDescription = stringResource(Res.string.remove_search_history_item),
                                        modifier = Modifier.size(24.dp),
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        isSearchSubmitted = false
                                        searchText = historyItem
                                        focusRequester.requestFocus()
                                    },
                                ) {
                                    Icon(
                                        imageVector = SimpIcons.ArrowOutward,
                                        contentDescription = stringResource(Res.string.search_suggestion),
                                        modifier = Modifier.size(24.dp).graphicsLayer { scaleX = -1f },
                                    )
                                }
                                }
                            }
                            item {
                                EndOfPage()
                            }
                        }
                    }
                }

                SearchUIType.EMPTY -> {
                    if (searchHistory.isEmpty()) {
                        SearchEmptyState(
                            title = stringResource(Res.string.search_music),
                            description = stringResource(Res.string.search_music_description),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = searchBarHeight, bottom = LocalPageBottomInset.current),
                        )
                    }
                }

                SearchUIType.SEARCH_RESULTS -> {
                    // Content area — chips now live in the blurred bar block above.
                    Column(modifier = Modifier.fillMaxSize()) {
                        PullToRefreshBox(
                            modifier = Modifier.fillMaxSize(),
                            state = pullToRefreshState,
                            onRefresh = { headerSubmit(searchText) },
                            isRefreshing = uiState is SearchScreenUIState.Loading,
                            indicator = {
                                PullToRefreshDefaults.Indicator(
                                    state = pullToRefreshState,
                                    isRefreshing = uiState is SearchScreenUIState.Loading,
                                    // Anchored to the top of the box, which now starts under the
                                    // bar — without this offset the spinner sits behind the bar
                                    // and only its top sliver shows.
                                    modifier =
                                        Modifier
                                            .align(Alignment.TopCenter)
                                            .padding(top = searchBarHeight),
                                    containerColor = PullToRefreshDefaults.indicatorContainerColor,
                                    color = PullToRefreshDefaults.indicatorColor,
                                    maxDistance = PullToRefreshDefaults.PositionalThreshold - 5.dp,
                                )
                            },
                        ) {
                            Crossfade(targetState = uiState) { uiState ->
                                when (uiState) {
                                    is SearchScreenUIState.Loading -> {
                                        // Loading state — same top inset as the results list, or
                                        // the first shimmer row hides behind the bar and chips.
                                        LazyColumn(
                                            contentPadding =
                                                PaddingValues(
                                                    top = searchBarHeight,
                                                    bottom = 10.dp,
                                                ),
                                        ) {
                                            item(key = "search-categories") {
                                                SearchCategoryRow(
                                                    chipRowState = chipRowState,
                                                    selectedType = searchScreenState.searchType,
                                                    isAnimated = true,
                                                    onSelected = onSearchTypeSelected,
                                                )
                                            }
                                            items(10) {
                                                ShimmerSearchItem()
                                            }
                                        }
                                    }

                                    is SearchScreenUIState.Success -> {
                                        // Success state with results
                                        Column(modifier = Modifier.fillMaxSize()) {
                                            // Search Results List
                                            val currentResults =
                                                when (searchScreenState.searchType) {
                                                    SearchType.ALL -> searchScreenState.searchAllResult
                                                    SearchType.SONGS -> searchScreenState.searchSongsResult
                                                    SearchType.VIDEOS -> searchScreenState.searchVideosResult
                                                    SearchType.ALBUMS -> searchScreenState.searchAlbumsResult
                                                    SearchType.ARTISTS -> searchScreenState.searchArtistsResult
                                                    SearchType.PLAYLISTS -> searchScreenState.searchPlaylistsResult
                                                    SearchType.FEATURED_PLAYLISTS -> searchScreenState.searchFeaturedPlaylistsResult
                                                    SearchType.PODCASTS -> searchScreenState.searchPodcastsResult
                                                }

                                            Crossfade(targetState = currentResults.isNotEmpty()) {
                                                if (it) {
                                                    LazyColumn(
                                                        contentPadding =
                                                            PaddingValues(
                                                                top = searchBarHeight,
                                                                bottom = 10.dp,
                                                            ),
                                                        state = resultsState,
                                                    ) {
                                                        item(key = "search-categories") {
                                                            SearchCategoryRow(
                                                                chipRowState = chipRowState,
                                                                selectedType = searchScreenState.searchType,
                                                                isAnimated = false,
                                                                onSelected = onSearchTypeSelected,
                                                            )
                                                        }
                                                        items(currentResults) { result ->
                                                            when (result) {
                                                                is SongsResult -> {
                                                                    SongFullWidthItems(
                                                                        track = result.toTrack(),
                                                                        isPlaying = result.videoId == currentVideoId,
                                                                        modifier = Modifier,
                                                                        onMoreClickListener = {
                                                                            onMoreClick(result.toTrack().toSongEntity())
                                                                        },
                                                                        onClickListener = {
                                                                            val firstTrack = result.toTrack()
                                                                            searchViewModel.setQueueData(
                                                                                QueueData.Data(
                                                                                    listTracks = arrayListOf(firstTrack),
                                                                                    firstPlayedTrack = firstTrack,
                                                                                    playlistId = "RDAMVM${result.videoId}",
                                                                                    playlistName =
                                                                                        "\"${searchText}\" ${
                                                                                            getStringBlocking(
                                                                                                Res.string.in_search,
                                                                                            )
                                                                                        }",
                                                                                    playlistType = PlaylistType.RADIO,
                                                                                    continuation = null,
                                                                                ),
                                                                            )
                                                                            keyboardController?.hide()
                                                                            focusManager.clearFocus(force = true)
                                                                            searchViewModel.loadMediaItem(firstTrack, Config.SONG_CLICK)
                                                                        },
                                                                        onAddToQueue = {
                                                                            sharedViewModel.addListToQueue(
                                                                                arrayListOf(result.toTrack()),
                                                                            )
                                                                        },
                                                                        selectionMode = selectionState.isActive,
                                                                        isSelected = selectionState.isSelected(result.videoId),
                                                                        onLongClick = { selectionState.start(it) },
                                                                        onSelectToggle = { selectionState.toggle(it) },
                                                                        startContentPadding = AppContentHorizontalInset,
                                                                        leadingContentSpacer = 0.dp,
                                                                        endContentPadding = AppContentHorizontalInset,
                                                                    )
                                                                }

                                                                is VideosResult -> {
                                                                    SongFullWidthItems(
                                                                        track = result.toTrack(),
                                                                        isPlaying = result.videoId == currentVideoId,
                                                                        modifier = Modifier,
                                                                        onClickListener = {
                                                                            keyboardController?.hide()
                                                                            focusManager.clearFocus(force = true)
                                                                            searchViewModel.playVideo(result.toTrack())
                                                                        },
                                                                        startContentPadding = AppContentHorizontalInset,
                                                                        leadingContentSpacer = 0.dp,
                                                                        endContentPadding = AppContentHorizontalInset,
                                                                    )
                                                                }

                                                                is AlbumsResult -> {
                                                                    PlaylistFullWidthItems(
                                                                        data = result,
                                                                        onClickListener = {
                                                                            navController.navigate(
                                                                                AlbumDestination(
                                                                                    result.browseId,
                                                                                ),
                                                                            )
                                                                        },
                                                                        startContentPadding = AppContentHorizontalInset,
                                                                        leadingContentSpacer = 0.dp,
                                                                        endContentPadding = AppContentHorizontalInset,
                                                                    )
                                                                }

                                                                is ArtistsResult -> {
                                                                    ArtistFullWidthItems(
                                                                        data = result,
                                                                        onClickListener = {
                                                                            navController.navigate(
                                                                                ArtistDestination(
                                                                                    result.browseId,
                                                                                ),
                                                                            )
                                                                        },
                                                                        startContentPadding = AppContentHorizontalInset,
                                                                        leadingContentSpacer = 0.dp,
                                                                        endContentPadding = AppContentHorizontalInset,
                                                                    )
                                                                }

                                                                is PlaylistsResult -> {
                                                                    PlaylistFullWidthItems(
                                                                        data = result,
                                                                        onClickListener = {
                                                                            if (result.resultType == "Podcast") {
                                                                                navController.navigate(
                                                                                    PodcastDestination(
                                                                                        result.browseId,
                                                                                    ),
                                                                                )
                                                                            } else {
                                                                                navController.navigate(
                                                                                    PlaylistDestination(
                                                                                        result.browseId,
                                                                                    ),
                                                                                )
                                                                            }
                                                                        },
                                                                        startContentPadding = AppContentHorizontalInset,
                                                                        leadingContentSpacer = 0.dp,
                                                                        endContentPadding = AppContentHorizontalInset,
                                                                    )
                                                                }
                                                            }
                                                        }
                                                        // Space at bottom to account for bottom navigation and mini player
                                                        item { Spacer(modifier = Modifier.height(150.dp)) }
                                                    }
                                                } else {
                                                    Column(
                                                        modifier = Modifier.fillMaxSize().padding(top = searchBarHeight),
                                                    ) {
                                                        SearchCategoryRow(
                                                            chipRowState = chipRowState,
                                                            selectedType = searchScreenState.searchType,
                                                            isAnimated = false,
                                                            onSelected = onSearchTypeSelected,
                                                        )
                                                        Box(
                                                            modifier = Modifier.fillMaxSize(),
                                                            contentAlignment = Alignment.Center,
                                                        ) {
                                                            Text(
                                                                text = stringResource(Res.string.no_results_found),
                                                                style = typo().titleMedium,
                                                                textAlign = TextAlign.Center,
                                                                modifier = Modifier.fillMaxWidth(),
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    is SearchScreenUIState.Error -> {
                                        Column(
                                            modifier = Modifier.fillMaxSize().padding(top = searchBarHeight),
                                        ) {
                                            SearchCategoryRow(
                                                chipRowState = chipRowState,
                                                selectedType = searchScreenState.searchType,
                                                isAnimated = false,
                                                onSelected = onSearchTypeSelected,
                                            )
                                            // Error state
                                            Column(
                                                modifier = Modifier.fillMaxSize(),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center,
                                            ) {
                                                Text(
                                                    text = stringResource(Res.string.error_occurred),
                                                    style = typo().titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    textAlign = TextAlign.Center,
                                                    modifier = Modifier.fillMaxWidth(),
                                                )
                                                Spacer(modifier = Modifier.height(10.dp))
                                                Button(onClick = {
                                                    if (searchText.isNotEmpty()) {
                                                        searchViewModel.searchAll(searchText)
                                                    }
                                                }) {
                                                    Text(text = stringResource(Res.string.retry))
                                                }
                                            }
                                        }
                                    }

                                    SearchScreenUIState.Empty -> {
                                        // Empty state
                                        Column(
                                            modifier = Modifier.fillMaxSize().padding(top = searchBarHeight),
                                        ) {
                                            SearchCategoryRow(
                                                chipRowState = chipRowState,
                                                selectedType = searchScreenState.searchType,
                                                isAnimated = false,
                                                onSelected = onSearchTypeSelected,
                                            )
                                            Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                Text(
                                                    text = stringResource(Res.string.no_results_found),
                                                    style = typo().titleMedium,
                                                    textAlign = TextAlign.Center,
                                                    modifier = Modifier.fillMaxWidth(),
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreenHeader(
    state: SearchHeaderState,
    backdrop: com.maxrave.simpmusic.expect.ui.PlatformBackdrop,
    contentHeight: Dp,
) {
    DisposableEffect(state) {
        onDispose { state.focused.value = false }
    }
    Box(
        modifier = Modifier.fillMaxWidth().height(contentHeight),
        contentAlignment = Alignment.Center,
    ) {
        val selectionState = state.selectionState
        if (selectionState?.isActive == true) {
            SongSelectionTopAppBar(
                state = selectionState,
                onSelectAll = state.onSelectAll,
                onOpenActions = state.onOpenSelectionActions,
                modifier = Modifier.fillMaxWidth().height(contentHeight),
                windowInsets = WindowInsets(0),
            )
        } else {
            val isAndroid = getPlatform() == Platform.Android
            val compactLandscape = isCompactLandscapeChrome()
            var isVoiceSearchOpen by remember { mutableStateOf(false) }
            val searchForString = stringResource(Res.string.search_for)
            val songString = stringResource(Res.string.song).lowercase()
            val artistString = stringResource(Res.string.artists).lowercase()
            val albumString = stringResource(Res.string.albums).lowercase()
            val playlistString = stringResource(Res.string.playlists).lowercase()
            val podcastString = stringResource(Res.string.podcasts).lowercase()
            val placeholderTexts =
                listOf(
                    "$searchForString $songString...",
                    "$searchForString $artistString...",
                    "$searchForString $albumString...",
                    "$searchForString $playlistString...",
                    "$searchForString $podcastString...",
                )

            val searchBarColors = appSearchBarColors(transparentContainer = LocalLiquidGlassEnabled.current)
            val fieldHeight = if (compactLandscape) 36.dp else 52.dp
            val voiceDialogAnchor = rememberDialogMotionAnchor(fieldHeight / 2)
            val fieldTextStyle =
                if (compactLandscape) {
                    inPageSearchBarTextStyle(typo().bodyLarge)
                } else {
                    appSearchBarTextStyle(typo().labelMedium)
                }
            val inputField: @Composable () -> Unit = {
                CompositionLocalProvider(LocalTextStyle provides fieldTextStyle) {
                    InPageSearchInputField(
                        query = state.query.value,
                        onQueryChange = {
                            state.submitted.value = false
                            state.query.value = it
                        },
                        onSearch = state.onSubmit,
                        expanded = false,
                        onExpandedChange = {},
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .focusRequester(state.focusRequester)
                                .onFocusChanged {
                                    if (it.isFocused && !state.focused.value) {
                                        state.submitted.value = false
                                    }
                                    state.focused.value = it.isFocused
                                },
                        placeholder = {
                            AnimatedContent(
                                targetState = state.placeholderIndex.intValue,
                                transitionSpec = {
                                    (
                                        fadeIn(animationSpec = tween(500)) +
                                            slideInVertically { height -> height }
                                    ).togetherWith(
                                        fadeOut(animationSpec = tween(500)) +
                                            slideOutVertically { height -> -height },
                                    )
                                },
                                label = "placeholder_animation",
                            ) { index ->
                                Text(
                                    text = placeholderTexts[index],
                                    style = fieldTextStyle,
                                )
                            }
                        },
                        leadingIcon = {
                            if (state.showingResults && state.query.value.isNotEmpty()) {
                                IconButton(
                                    modifier = Modifier.clip(CircleShape),
                                    onClick = state.onBackToSearch,
                                ) {
                                    Icon(
                                        imageVector = SimpIcons.ArrowBackIosNew,
                                        contentDescription = stringResource(Res.string.back_to_search),
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = SimpIcons.Search,
                                    contentDescription = stringResource(Res.string.search),
                                )
                            }
                        },
                        trailingIcon = {
                            if (state.query.value.isNotEmpty()) {
                                IconButton(
                                    modifier = Modifier.clip(CircleShape),
                                    onClick = state.onClear,
                                ) {
                                    Icon(
                                        imageVector = SimpIcons.Close,
                                        contentDescription = stringResource(Res.string.clear_search),
                                    )
                                }
                            }
                        },
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().height(contentHeight).padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val fieldModifier =
                    Modifier
                        .weight(1f)
                        .height(fieldHeight)
                        .then(
                            if (LocalLiquidGlassEnabled.current) {
                                // Keep the input stationary while the IME positions its cursor handles.
                                Modifier.searchFieldGlass(backdrop)
                            } else {
                                Modifier
                            },
                        )
                AppSearchBarTheme(compact = compactLandscape) {
                    // The collapsed Material SearchBar adds a top inset beneath the glass.
                    // Host the input directly so its icons and text share the pill's center.
                    Box(
                        modifier =
                            fieldModifier.then(
                                if (LocalLiquidGlassEnabled.current) {
                                    Modifier
                                } else {
                                    Modifier.clip(CircleShape).background(searchBarColors.containerColor, CircleShape)
                                },
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        inputField()
                    }
                }

                if (isAndroid) {
                    IconButton(
                        modifier =
                            Modifier
                                .size(fieldHeight)
                                .then(voiceDialogAnchor.modifier)
                                .liquidGlass(backdrop),
                        shape = CircleShape,
                        onClick = {
                            voiceDialogAnchor.record()
                            isVoiceSearchOpen = true
                        },
                    ) {
                        Icon(
                            imageVector = SimpIcons.Mic,
                            modifier = Modifier.size(if (compactLandscape) 22.dp else 28.dp),
                            tint = MaterialTheme.colorScheme.onBackground,
                            contentDescription = stringResource(Res.string.search_by_voice),
                        )
                    }
                }
            }
            if (isAndroid && isVoiceSearchOpen) {
                VoiceSearchDialog(
                    onDismissRequest = { isVoiceSearchOpen = false },
                    onResult = state.onSubmit,
                )
            }
        }
    }
}

@Composable
private fun SearchCategoryRow(
    chipRowState: androidx.compose.foundation.ScrollState,
    selectedType: SearchType,
    isAnimated: Boolean,
    onSelected: (SearchType) -> Unit,
) {
    Row(
        modifier =
            Modifier
                .padding(horizontal = AppContentHorizontalInset)
                .horizontalScroll(chipRowState)
                .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        SearchType.entries.forEach { type ->
            Chip(
                isAnimated = isAnimated,
                isSelected = type == selectedType,
                text = stringResource(type.toStringRes()),
            ) {
                onSelected(type)
            }
        }
    }
}

@Composable
fun SuggestItemRow(
    searchResult: SearchResultType,
    onItemClick: (SearchResultType) -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable { onItemClick(searchResult) }
                .padding(vertical = 8.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val url =
            when (searchResult) {
                is SongsResult -> {
                    searchResult.thumbnails?.lastOrNull()?.url
                }

                is AlbumsResult -> {
                    searchResult.thumbnails.lastOrNull()?.url
                }

                is ArtistsResult -> {
                    searchResult.thumbnails.lastOrNull()?.url
                }

                is PlaylistsResult -> {
                    searchResult.thumbnails.lastOrNull()?.url
                }

                is VideosResult -> {
                    searchResult.thumbnails?.lastOrNull()?.url
                }

                else -> {
                    null
                }
            }

        Box(
            modifier =
                Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(4.dp)),
        ) {
            AsyncImage(
                model =
                    ImageRequest
                        .Builder(LocalPlatformContext.current)
                        .data(url)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .diskCacheKey(url)
                        .crossfade(true)
                        .build(),
                placeholder = rememberHolderPainter(),
                error = rememberHolderPainter(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier =
                    Modifier
                        .size(40.dp)
                        .clip(
                            if (searchResult is ArtistsResult) {
                                CircleShape
                            } else {
                                RoundedCornerShape(4.dp)
                            },
                        ),
            )
        }

        Spacer(modifier = Modifier.padding(horizontal = 12.dp))

        Column(modifier = Modifier.weight(1f)) {
            val title =
                when (searchResult) {
                    is SongsResult -> {
                        searchResult.title
                    }

                    is AlbumsResult -> {
                        searchResult.title
                    }

                    is ArtistsResult -> {
                        searchResult.artist
                    }

                    is PlaylistsResult -> {
                        searchResult.title
                    }

                    is VideosResult -> {
                        searchResult.title
                    }

                    else -> {
                        null
                    }
                } ?: "Unknown"

            Text(
                text = title,
                style = typo().labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(2.dp))

            val subtitle =
                when (searchResult) {
                    is SongsResult -> searchResult.artists?.map { it.name }?.connectArtists()
                    is AlbumsResult -> searchResult.artists.map { it.name }.connectArtists()
                    is PlaylistsResult -> searchResult.author.ifEmpty { "YouTube Music" }
                    is ArtistsResult -> stringResource(Res.string.artist)
                    is VideosResult -> searchResult.artists?.map { it.name }?.connectArtists()
                    else -> null
                } ?: "Unknown"

            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    style = typo().bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

enum class SearchUIType {
    EMPTY,
    SEARCH_HISTORY,
    SEARCH_SUGGESTIONS,
    SEARCH_RESULTS,
}
