package com.petedev.wavemusic.ui.screen.other

import com.petedev.wavemusic.ui.component.rememberPageListState

import com.petedev.wavemusic.resources.back_to_search
import com.petedev.wavemusic.resources.clear_search
import com.petedev.wavemusic.resources.remove_search_history_item
import com.petedev.wavemusic.resources.search
import com.petedev.wavemusic.resources.search_by_voice
import com.petedev.wavemusic.resources.search_history
import com.petedev.wavemusic.resources.search_suggestion

import com.petedev.wavemusic.ui.component.searchFieldGlass
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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
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
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
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
import androidx.compose.ui.platform.LocalDensity
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
import com.petedev.wavemusic.common.Config
import com.petedev.wavemusic.domain.data.entities.SongEntity
import com.petedev.wavemusic.domain.data.model.browse.album.Track
import com.petedev.wavemusic.domain.data.model.intent.GenericIntent
import com.petedev.wavemusic.domain.data.model.searchResult.albums.AlbumsResult
import com.petedev.wavemusic.domain.data.model.searchResult.artists.ArtistsResult
import com.petedev.wavemusic.domain.data.model.searchResult.playlists.PlaylistsResult
import com.petedev.wavemusic.domain.data.model.searchResult.songs.SongsResult
import com.petedev.wavemusic.domain.data.model.searchResult.videos.VideosResult
import com.petedev.wavemusic.domain.data.type.SearchResultType
import com.petedev.wavemusic.domain.mediaservice.handler.PlaylistType
import com.petedev.wavemusic.domain.mediaservice.handler.QueueData
import com.petedev.wavemusic.domain.utils.connectArtists
import com.petedev.wavemusic.domain.utils.toSongEntity
import com.petedev.wavemusic.domain.utils.toTrack
import com.petedev.wavemusic.Platform
import com.petedev.wavemusic.getPlatform
import com.petedev.wavemusic.ui.component.AddToPlaylistModalBottomSheet
import com.petedev.wavemusic.ui.component.rememberHolderPainter
import com.petedev.wavemusic.extension.getStringBlocking
import com.petedev.wavemusic.extension.toAppDeepLinkOrNull
import com.petedev.wavemusic.ui.component.ArtistFullWidthItems
import com.petedev.wavemusic.ui.component.AppContentHorizontalInset
import com.petedev.wavemusic.ui.component.AppSearchBarTheme
import com.petedev.wavemusic.ui.component.BOTTOM_NAV_TRANSITION_DURATION_MILLIS
import com.petedev.wavemusic.ui.component.Chip
import com.petedev.wavemusic.ui.component.InPageSearchInputField
import com.petedev.wavemusic.ui.component.liquidGlass
import com.petedev.wavemusic.ui.component.isCompactLandscapeChrome
import com.petedev.wavemusic.ui.component.headerGlassControlSize
import com.petedev.wavemusic.ui.component.headerGlassIconSize
import com.petedev.wavemusic.ui.component.appSearchBarColors
import com.petedev.wavemusic.ui.component.appSearchBarTextStyle
import com.petedev.wavemusic.ui.component.inPageSearchBarTextStyle
import com.petedev.wavemusic.ui.theme.LocalLiquidGlassEnabled
import com.petedev.wavemusic.ui.component.EndOfPage
import com.petedev.wavemusic.ui.component.LocalPageBottomInset
import com.petedev.wavemusic.ui.component.NowPlayingBottomSheet
import com.petedev.wavemusic.ui.component.PlaylistFullWidthItems
import com.petedev.wavemusic.ui.component.ShimmerSearchItem
import com.petedev.wavemusic.ui.component.SongFullWidthItems
import com.petedev.wavemusic.ui.component.selection.SelectedSongsBottomSheet
import com.petedev.wavemusic.ui.component.selection.SongSelectionState
import com.petedev.wavemusic.ui.component.selection.SongSelectionTopAppBar
import com.petedev.wavemusic.ui.component.selection.rememberSongSelectionState
import com.petedev.wavemusic.ui.icon.ArrowOutward
import com.petedev.wavemusic.ui.icon.ArrowBackIosNew
import com.petedev.wavemusic.ui.icon.Close
import com.petedev.wavemusic.ui.icon.History
import com.petedev.wavemusic.ui.icon.Mic
import com.petedev.wavemusic.ui.component.rememberDialogMotionAnchor
import com.petedev.wavemusic.ui.icon.Search
import com.petedev.wavemusic.ui.icon.WaveIcons
import com.petedev.wavemusic.ui.navigation.destination.list.AlbumDestination
import com.petedev.wavemusic.ui.navigation.destination.list.ArtistDestination
import com.petedev.wavemusic.ui.navigation.destination.list.PlaylistDestination
import com.petedev.wavemusic.ui.navigation.destination.list.PodcastDestination
import com.petedev.wavemusic.ui.navigation.destination.search.SearchDestination
import com.petedev.wavemusic.ui.theme.typo
import com.petedev.wavemusic.viewModel.SearchScreenUIState
import com.petedev.wavemusic.domain.utils.isPodcastEpisode
import com.petedev.wavemusic.viewModel.SearchType
import com.petedev.wavemusic.viewModel.SearchViewModel
import com.petedev.wavemusic.viewModel.SharedViewModel
import com.petedev.wavemusic.viewModel.SongSelectionViewModel
import com.petedev.wavemusic.viewModel.toStringRes
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import com.petedev.wavemusic.resources.Res
import com.petedev.wavemusic.resources.albums
import com.petedev.wavemusic.resources.artist
import com.petedev.wavemusic.resources.artists
import com.petedev.wavemusic.resources.error_occurred
import com.petedev.wavemusic.resources.in_search
import com.petedev.wavemusic.resources.no_results_found
import com.petedev.wavemusic.resources.playlists
import com.petedev.wavemusic.resources.podcasts
import com.petedev.wavemusic.resources.retry
import com.petedev.wavemusic.resources.search_for
import com.petedev.wavemusic.resources.search_music
import com.petedev.wavemusic.resources.search_music_description
import com.petedev.wavemusic.resources.song
import com.petedev.wavemusic.resources.what_do_you_want_to_listen_to

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
    val resultsState = rememberPageListState()
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
                SearchType.EPISODES -> searchViewModel.searchEpisodes(query)
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
                    SearchType.EPISODES -> searchViewModel.searchEpisodes(query)
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
                    SearchType.EPISODES -> searchScreenState.searchEpisodesResult.map { it.videoId }
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
                                                    playlistId = if (firstTrack.isPodcastEpisode()) null else "RDAMVM${firstTrack.videoId}",
                                                    playlistName = "\"${searchText}\" ${getStringBlocking(Res.string.in_search)}",
                                                    playlistType = if (firstTrack.isPodcastEpisode()) PlaylistType.PLAYLIST else PlaylistType.RADIO,
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
                                        imageVector = WaveIcons.ArrowOutward,
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
                                    imageVector = WaveIcons.History,
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
                                        imageVector = WaveIcons.Close,
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
                                        imageVector = WaveIcons.ArrowOutward,
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
                                                    SearchType.EPISODES -> searchScreenState.searchEpisodesResult
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
                                                                                    playlistId = if (firstTrack.isPodcastEpisode()) null else "RDAMVM${result.videoId}",
                                                                                    playlistName =
                                                                                        "\"${searchText}\" ${
                                                                                            getStringBlocking(
                                                                                                Res.string.in_search,
                                                                                            )
                                                                                        }",
                                                                                    playlistType = if (firstTrack.isPodcastEpisode()) PlaylistType.PLAYLIST else PlaylistType.RADIO,
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
    backdrop: com.petedev.wavemusic.expect.ui.PlatformBackdrop,
    contentHeight: Dp,
) {
    DisposableEffect(state) {
        onDispose { state.focused.value = false }
    }
    BoxWithConstraints(
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
            // Physical-pixel adjustment follows the device density in both glass modes.
            // Start from the actual available height: flat portrait headers cap the old 52dp field.
            val portraitAdjustment = if (isAndroid && !compactLandscape) {
                with(LocalDensity.current) { 2.toDp() }
            } else 0.dp
            val horizontalInset = if (compactLandscape) AppContentHorizontalInset else 16.dp
            val fieldHeight = if (compactLandscape) {
                minOf(headerGlassControlSize(), contentHeight)
            } else {
                minOf(52.dp, contentHeight) - portraitAdjustment * 2
            }
            val expandedFieldWidth =
                (maxWidth - horizontalInset * 2 - if (isAndroid) fieldHeight + 8.dp else 0.dp).coerceAtLeast(0.dp)
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
                        centeredContent = true,
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
                                modifier = Modifier.fillMaxWidth(),
                                transitionSpec = {
                                    (
                                        fadeIn(animationSpec = tween(500)) +
                                            slideInVertically { height -> height }
                                    ).togetherWith(
                                        fadeOut(animationSpec = tween(500)) +
                                            slideOutVertically { height -> -height },
                                    ).using(null)
                                },
                                label = "placeholder_animation",
                            ) { index ->
                                Text(
                                    text = placeholderTexts[index],
                                    style = fieldTextStyle,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
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
                                        imageVector = WaveIcons.ArrowBackIosNew,
                                        contentDescription = stringResource(Res.string.back_to_search),
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = WaveIcons.Search,
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
                                        imageVector = WaveIcons.Close,
                                        contentDescription = stringResource(Res.string.clear_search),
                                    )
                                }
                            }
                        },
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().height(contentHeight)
                    .offset(y = -portraitAdjustment)
                    .padding(horizontal = horizontalInset),
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
                            fieldModifier.clip(CircleShape).then(
                                if (LocalLiquidGlassEnabled.current) {
                                    Modifier
                                } else {
                                    Modifier.clip(CircleShape).background(searchBarColors.containerColor, CircleShape)
                                },
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        // Keep the input text measured at the search field's full width.
                        Box(
                            Modifier.fillMaxSize()
                                .wrapContentWidth(Alignment.Start, unbounded = true)
                                .width(expandedFieldWidth),
                        ) {
                            inputField()
                        }
                    }
                }

                if (isAndroid) {
                    // Keep the drawn microphone at the pill height in either theme. Material's
                    // minimum layout and default fill otherwise add a larger, faint outer circle.
                    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
                        IconButton(
                            modifier =
                                Modifier
                                    .size(fieldHeight)
                                    .then(voiceDialogAnchor.modifier)
                                    .clip(CircleShape)
                                    .liquidGlass(backdrop),
                            shape = CircleShape,
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                            ),
                            onClick = {
                                voiceDialogAnchor.record()
                                isVoiceSearchOpen = true
                            },
                        ) {
                            Icon(
                                imageVector = WaveIcons.Mic,
                                modifier = Modifier.size(if (compactLandscape) headerGlassIconSize() else 28.dp),
                                tint = MaterialTheme.colorScheme.onBackground,
                                contentDescription = stringResource(Res.string.search_by_voice),
                            )
                        }
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
