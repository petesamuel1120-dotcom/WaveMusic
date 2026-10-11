package com.maxrave.simpmusic.ui.screen.other

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.kmpalette.rememberPaletteState
import com.maxrave.domain.data.model.browse.album.Track
import com.maxrave.domain.utils.toSongEntity
import com.maxrave.domain.utils.toTrack
import com.maxrave.simpmusic.expect.ui.layerBackdrop
import com.maxrave.simpmusic.expect.ui.rememberBackdrop
import com.maxrave.simpmusic.expect.ui.toImageBitmap
import com.maxrave.simpmusic.extension.angledGradientBackground
import com.maxrave.simpmusic.extension.artworkScrimBrush
import com.maxrave.simpmusic.extension.getColorFromPalette
import com.maxrave.simpmusic.ui.component.CenterLoadingBox
import com.maxrave.simpmusic.ui.component.DescriptionView
import com.maxrave.simpmusic.ui.component.EndOfPage
import com.maxrave.simpmusic.ui.component.HeartCheckBox
import com.maxrave.simpmusic.ui.component.LiquidGlassContainer
import com.maxrave.simpmusic.ui.component.LiquidGlassIconButton
import com.maxrave.simpmusic.ui.component.NowPlayingBottomSheet
import com.maxrave.simpmusic.ui.component.PageActionHeader
import com.maxrave.simpmusic.ui.component.PlaylistSearchBar
import com.maxrave.simpmusic.ui.component.PodcastEpisodeFullWidthItem
import com.maxrave.simpmusic.ui.component.SearchBarBackHandler
import com.maxrave.simpmusic.ui.component.SearchBarEnter
import com.maxrave.simpmusic.ui.component.SearchBarExit
import com.maxrave.simpmusic.ui.component.SearchBarScrollDismissEffect
import com.maxrave.simpmusic.ui.component.SearchResultsScrollEffect
import com.maxrave.simpmusic.ui.component.SharedHeaderContentHeight
import com.maxrave.simpmusic.ui.component.frostedHeaderSource
import com.maxrave.simpmusic.ui.component.headerGlassControlSize
import com.maxrave.simpmusic.ui.component.isCompactLandscapeChrome
import com.maxrave.simpmusic.ui.component.rememberHolderPainter
import com.maxrave.simpmusic.ui.icon.Close
import com.maxrave.simpmusic.ui.icon.PlayArrow
import com.maxrave.simpmusic.ui.icon.Search
import com.maxrave.simpmusic.ui.icon.Share
import com.maxrave.simpmusic.ui.icon.Shuffle
import com.maxrave.simpmusic.ui.icon.SimpIcons
import com.maxrave.simpmusic.ui.navigation.destination.list.ArtistDestination
import com.maxrave.simpmusic.ui.theme.typo
import com.maxrave.simpmusic.viewModel.PodcastUIEvent
import com.maxrave.simpmusic.viewModel.PodcastUIState
import com.maxrave.simpmusic.viewModel.PodcastViewModel
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import simpmusic.composeapp.generated.resources.Res
import simpmusic.composeapp.generated.resources.album_length
import simpmusic.composeapp.generated.resources.no_description
import simpmusic.composeapp.generated.resources.no_results_found
import simpmusic.composeapp.generated.resources.podcasts

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PodcastScreen(
    viewModel: PodcastViewModel = koinViewModel(),
    podcastId: String,
    navController: NavController,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isFavorite by viewModel.isFavorite.collectAsStateWithLifecycle()
    val lazyState = rememberLazyListState()
    var showSearchBar by rememberSaveable(podcastId) { mutableStateOf(false) }
    var query by rememberSaveable(podcastId) { mutableStateOf("") }
    var searchSubmissionCount by remember { mutableStateOf(0) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    var currentTrack by remember { mutableStateOf<Track?>(null) }
    var shouldShowMoreBottomSheet by rememberSaveable { mutableStateOf(false) }

    var gradientColors by remember { mutableStateOf(listOf(Color.Black, Color.Black)) }
    val paletteState = rememberPaletteState()
    val hazeState = rememberHazeState()
    val frostedBackdrop = rememberBackdrop(Color.Black)
    var bitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(bitmap) {
        bitmap?.let { paletteState.generate(it) }
    }
    LaunchedEffect(paletteState) {
        snapshotFlow { paletteState.palette }.distinctUntilChanged().collectLatest {
            gradientColors = listOf(it.getColorFromPalette(), Color.Black)
        }
    }
    LaunchedEffect(podcastId) {
        if ((uiState as? PodcastUIState.Success)?.id != podcastId) viewModel.getPodcastBrowse(podcastId)
    }
    LaunchedEffect(uiState) {
        (uiState as? PodcastUIState.Error)?.let {
            viewModel.makeToast("Error: ${it.message}")
            navController.navigateUp()
        }
    }

    val searchHeaderHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + SharedHeaderContentHeight
    val headerHeightPx = with(LocalDensity.current) { searchHeaderHeight.roundToPx() }
    val showHeaderTitle by remember(lazyState, headerHeightPx) {
        derivedStateOf {
            lazyState.firstVisibleItemIndex > 0 ||
                lazyState.layoutInfo.visibleItemsInfo.firstOrNull { it.key == "podcast-hero" }
                    ?.let { it.offset + it.size <= headerHeightPx } == true
        }
    }
    val landscape = isCompactLandscapeChrome()
    val success = uiState as? PodcastUIState.Success
    val filter = query.trim()
    val episodes = remember(success?.data?.listEpisode, filter) {
        success?.data?.listEpisode.orEmpty().filter { episode ->
            filter.isEmpty() || episode.title.contains(filter, ignoreCase = true) ||
                episode.author.name.contains(filter, ignoreCase = true) ||
                episode.description?.contains(filter, ignoreCase = true) == true
        }
    }
    val dismissSearch = {
        showSearchBar = false
        query = ""
        focusManager.clearFocus(force = true)
        keyboardController?.hide()
    }
    SearchBarScrollDismissEffect(lazyState, showSearchBar) { showSearchBar = false }
    SearchBarBackHandler(showSearchBar, onDismiss = { showSearchBar = false })
    SearchResultsScrollEffect(
        state = lazyState,
        searchVisible = showSearchBar,
        hasQuery = filter.isNotEmpty(),
        firstResultIndex = 3,
        headerHeight = searchHeaderHeight,
        submissionCount = searchSubmissionCount,
        resultsReady = success != null,
    )
    val searchField: @Composable () -> Unit = {
        PlaylistSearchBar(
            backdrop = frostedBackdrop,
            query = query,
            onQueryChange = { query = it },
            onSearch = {
                query = it.trim()
                searchSubmissionCount++
                keyboardController?.hide()
                showSearchBar = false
            },
            expanded = showSearchBar,
            onExpandedChange = { showSearchBar = it },
            focusRequester = focusRequester,
        )
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        Crossfade(targetState = uiState, label = "podcastPage") { state ->
            when (state) {
                is PodcastUIState.Success -> {
                    val data = state.data
                    val id = state.id
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                            .frostedHeaderSource(frostedBackdrop)
                            .background(Color.Black)
                            .hazeSource(hazeState),
                        state = lazyState,
                    ) {
                        item(key = "podcast-hero", contentType = "header") {
                            Box(Modifier.fillMaxWidth()) {
                                Box(Modifier.fillMaxWidth().height(260.dp)) {
                                    Box(Modifier.matchParentSize().angledGradientBackground(gradientColors, 25f))
                                    Box(
                                        Modifier.fillMaxWidth().height(180.dp).align(Alignment.BottomCenter)
                                            .background(artworkScrimBrush(Color.Black)),
                                    )
                                }
                                Column(Modifier.fillMaxWidth()) {
                                    Spacer(Modifier.height(searchHeaderHeight + 12.dp))
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalPlatformContext.current)
                                            .data(data.thumbnail.lastOrNull()?.url)
                                            .diskCachePolicy(CachePolicy.ENABLED)
                                            .diskCacheKey(data.thumbnail.lastOrNull()?.url)
                                            .crossfade(true).build(),
                                        placeholder = rememberHolderPainter(),
                                        error = rememberHolderPainter(),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        onSuccess = { bitmap = it.result.image.toImageBitmap() },
                                        modifier = Modifier.size(if (landscape) 180.dp else 250.dp)
                                            .align(Alignment.CenterHorizontally).clip(RoundedCornerShape(8.dp)),
                                    )
                                    Column(Modifier.padding(horizontal = 32.dp)) {
                                        Spacer(Modifier.height(25.dp))
                                        Text(data.title, style = typo().titleLarge, color = Color.White, maxLines = 2)
                                        Row(
                                            Modifier.padding(top = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            AsyncImage(
                                                model = ImageRequest.Builder(LocalPlatformContext.current)
                                                    .data(data.authorThumbnail).diskCachePolicy(CachePolicy.ENABLED)
                                                    .diskCacheKey(data.authorThumbnail).crossfade(true).build(),
                                                placeholder = rememberHolderPainter(),
                                                error = rememberHolderPainter(),
                                                contentDescription = null,
                                                modifier = Modifier.size(25.dp).clip(CircleShape),
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
                                                TextButton(
                                                    modifier = Modifier.wrapContentHeight().defaultMinSize(1.dp, 1.dp),
                                                    contentPadding = PaddingValues(vertical = 1.dp),
                                                    onClick = {
                                                        data.author.id?.takeIf { it.isNotEmpty() }?.let {
                                                            navController.navigate(ArtistDestination(it))
                                                        }
                                                    },
                                                ) {
                                                    Text(data.author.name, style = typo().labelSmall, color = Color.White)
                                                }
                                            }
                                        }
                                        Text(
                                            stringResource(Res.string.podcasts), style = typo().bodyMedium,
                                            color = Color.White.copy(alpha = 0.7f),
                                            modifier = Modifier.padding(top = 8.dp),
                                        )
                                    }
                                }
                            }
                        }
                        item(key = "podcast-actions", contentType = "actions") {
                            PodcastActions(
                                isFavorite = isFavorite,
                                onLike = { viewModel.onUIEvent(PodcastUIEvent.ToggleFavorite(id, !isFavorite)) },
                                onPlay = { viewModel.onUIEvent(PodcastUIEvent.PlayAll(id)) },
                                onShuffle = { viewModel.onUIEvent(PodcastUIEvent.Shuffle(id)) },
                                onShare = { viewModel.onUIEvent(PodcastUIEvent.Share(id)) },
                            )
                        }
                        item(key = "podcast-description", contentType = "description") {
                            val uriHandler = LocalUriHandler.current
                            Column(Modifier.padding(horizontal = 32.dp)) {
                                DescriptionView(
                                    text = data.description ?: stringResource(Res.string.no_description),
                                    limitLine = 3,
                                    onTimeClicked = {},
                                    onURLClicked = { uriHandler.openUri(it) },
                                )
                                Text(
                                    stringResource(Res.string.album_length, episodes.size.toString(), ""),
                                    color = Color.White, style = typo().bodyMedium,
                                    modifier = Modifier.padding(vertical = 8.dp),
                                )
                            }
                        }
                        itemsIndexed(episodes, key = { index, episode -> "episode:${episode.videoId}:$index" }) { _, episode ->
                            PodcastEpisodeFullWidthItem(
                                episode = episode,
                                onClick = { viewModel.onUIEvent(PodcastUIEvent.EpisodeClick(episode.videoId, id)) },
                                onMoreClickListener = {
                                    currentTrack = episode.toTrack()
                                    shouldShowMoreBottomSheet = true
                                },
                            )
                        }
                        if (filter.isNotEmpty() && episodes.isEmpty()) {
                            item(key = "podcast-no-results") {
                                Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                                    Text(stringResource(Res.string.no_results_found), color = Color.White)
                                }
                            }
                        }
                        item(key = "podcast-end") { EndOfPage() }
                    }
                }
                is PodcastUIState.Loading -> CenterLoadingBox(Modifier.fillMaxSize())
                is PodcastUIState.Error -> Unit
            }
        }
        AnimatedVisibility(
            visible = showSearchBar && !landscape,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = searchHeaderHeight),
            enter = SearchBarEnter,
            exit = SearchBarExit,
        ) { searchField() }
        PageActionHeader(
            title = success?.data?.title.orEmpty(),
            showTitle = showHeaderTitle || showSearchBar || filter.isNotEmpty(),
            hazeState = hazeState,
            backdrop = frostedBackdrop,
            tint = gradientColors.first(),
            contentColor = Color.White,
            onBack = { navController.navigateUp() },
            showSurface = showHeaderTitle || showSearchBar || filter.isNotEmpty(),
            centerTitle = true,
            centerContent = searchField.takeIf { landscape },
            centerContentVisible = showSearchBar && landscape,
        ) {
            IconButton(
                onClick = { if (showSearchBar || filter.isNotEmpty()) dismissSearch() else showSearchBar = true },
                enabled = success != null,
                modifier = Modifier.size(headerGlassControlSize()),
            ) {
                Icon(
                    if (showSearchBar || filter.isNotEmpty()) SimpIcons.Close else SimpIcons.Search,
                    contentDescription = if (showSearchBar || filter.isNotEmpty()) "Close podcast search" else "Search podcast episodes",
                    tint = Color.White,
                )
            }
        }
        if (shouldShowMoreBottomSheet) {
            currentTrack?.toSongEntity()?.let { song ->
                NowPlayingBottomSheet(
                    onDismiss = { shouldShowMoreBottomSheet = false },
                    navController = navController,
                    song = song,
                )
            }
        }
    }
}

/** Separate capture and controls: the glass never samples a layer containing itself. */
@Composable
private fun PodcastActions(
    isFavorite: Boolean,
    onLike: () -> Unit,
    onPlay: () -> Unit,
    onShuffle: () -> Unit,
    onShare: () -> Unit,
) {
    val backdrop = rememberBackdrop(Color.Black)
    val compact = isCompactLandscapeChrome()
    val buttonSize = if (compact) 36.dp else 40.dp
    Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
        Box(Modifier.matchParentSize().layerBackdrop(backdrop).background(Color.Black))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                LiquidGlassContainer(backdrop, Modifier.size(buttonSize)) {
                    HeartCheckBox(size = buttonSize.value.toInt(), checked = isFavorite, tint = Color.White, onStateChange = onLike)
                }
            }
            Row(
                modifier = Modifier.width(if (compact) 104.dp else 112.dp)
                    .height(if (compact) 44.dp else 56.dp).clip(CircleShape).background(Color.White)
                    .clickable(role = Role.Button, onClick = onPlay),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(SimpIcons.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(6.dp))
                Text("Play", color = Color.Black, style = typo().labelLarge)
            }
            Row(
                Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LiquidGlassIconButton(
                    backdrop, SimpIcons.Shuffle,
                    Modifier.size(buttonSize).semantics { contentDescription = "Shuffle podcast episodes" },
                    onClick = onShuffle,
                )
                LiquidGlassIconButton(
                    backdrop, SimpIcons.Share,
                    Modifier.size(buttonSize).semantics { contentDescription = "Share podcast" },
                    onClick = onShare,
                )
            }
        }
    }
}
