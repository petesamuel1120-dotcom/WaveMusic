package com.maxrave.simpmusic.ui.screen.other

import com.maxrave.simpmusic.ui.component.localizedDescription
import simpmusic.composeapp.generated.resources.play_action
import simpmusic.composeapp.generated.resources.share
import simpmusic.composeapp.generated.resources.shuffle

import com.maxrave.simpmusic.ui.component.pageContentHorizontalInset
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextOverflow
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
import com.maxrave.simpmusic.expect.ui.rememberBackdrop
import com.maxrave.simpmusic.expect.ui.toImageBitmap
import com.maxrave.simpmusic.extension.angledGradientBackground
import com.maxrave.simpmusic.extension.artworkScrimBrush
import com.maxrave.simpmusic.extension.getColorFromPalette
import com.maxrave.simpmusic.extension.getScreenSizeInfo
import com.maxrave.simpmusic.extension.toImmersiveBackground
import com.maxrave.simpmusic.ui.component.CenterLoadingBox
import com.maxrave.simpmusic.ui.component.DescriptionView
import com.maxrave.simpmusic.ui.component.EndOfPage
import com.maxrave.simpmusic.ui.component.GlassBackdropBox
import com.maxrave.simpmusic.ui.component.HeartCheckBox
import com.maxrave.simpmusic.ui.component.LiquidGlassContainer
import com.maxrave.simpmusic.ui.component.LocalPageContentInsets
import com.maxrave.simpmusic.ui.component.LiquidGlassIconButton
import com.maxrave.simpmusic.ui.component.NowPlayingBottomSheet
import com.maxrave.simpmusic.ui.component.PageActionHeader
import com.maxrave.simpmusic.ui.component.onlineLandscapeHeroLayout
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
import com.maxrave.simpmusic.ui.component.rememberHolderPainter
import com.maxrave.simpmusic.ui.icon.Close
import com.maxrave.simpmusic.ui.icon.PlayArrow
import com.maxrave.simpmusic.ui.icon.Search
import com.maxrave.simpmusic.ui.icon.Share
import com.maxrave.simpmusic.ui.icon.Shuffle
import com.maxrave.simpmusic.ui.icon.SimpIcons
import com.maxrave.simpmusic.ui.navigation.destination.list.ArtistDestination
import com.maxrave.simpmusic.ui.theme.LocalAppColorScheme
import com.maxrave.simpmusic.ui.theme.LocalAppIsDarkTheme
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

    val isDark = LocalAppIsDarkTheme.current
    val appColorScheme = LocalAppColorScheme.current ?: MaterialTheme.colorScheme
    val pageContentColor = if (isDark) Color.White else appColorScheme.onSurface
    val pageSecondaryContentColor = if (isDark) Color.White.copy(alpha = 0.7f) else appColorScheme.onSurfaceVariant
    val primaryActionBackground = if (isDark) Color.White else appColorScheme.primary
    val primaryActionContent = if (isDark) Color.Black else appColorScheme.onPrimary
    var pageBackground by remember(isDark, appColorScheme.background) {
        mutableStateOf(if (isDark) Color.Black else appColorScheme.background)
    }
    var heroAccent by remember(isDark, appColorScheme.primaryContainer) {
        mutableStateOf(if (isDark) Color.Black else appColorScheme.primaryContainer)
    }
    val gradientColors = listOf(heroAccent, pageBackground)
    val paletteState = rememberPaletteState()
    val hazeState = rememberHazeState()
    val frostedBackdrop = rememberBackdrop(pageBackground)
    var bitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(bitmap) {
        bitmap?.let { paletteState.generate(it) }
    }
    LaunchedEffect(paletteState, isDark) {
        snapshotFlow { paletteState.palette }.distinctUntilChanged().collectLatest { palette ->
            pageBackground = palette?.toImmersiveBackground(isDark)
                ?: if (isDark) Color.Black else appColorScheme.background
            val accent = palette.getColorFromPalette()
            heroAccent = if (!isDark && accent == Color.Black) pageBackground else accent
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
    val screenInfo = getScreenSizeInfo()
    val landscape = screenInfo.wDP >= screenInfo.hDP
    val landscapeHero = onlineLandscapeHeroLayout(maxCoverSize = 250.dp)
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

    Box(Modifier.fillMaxSize().background(pageBackground)) {
        Crossfade(targetState = uiState, label = "podcastPage") { state ->
            when (state) {
                is PodcastUIState.Success -> {
                    val data = state.data
                    val id = state.id
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                            .windowInsetsPadding(LocalPageContentInsets.current)
                            .frostedHeaderSource(frostedBackdrop)
                            .background(pageBackground)
                            .hazeSource(hazeState),
                        state = lazyState,
                    ) {
                        item(key = "podcast-hero", contentType = "header") {
                            val cover: @Composable (Modifier) -> Unit = { modifier ->
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
                                    modifier = modifier.clip(RoundedCornerShape(8.dp)),
                                )
                            }
                            val details: @Composable () -> Unit = {
                                Text(
                                    data.title,
                                    style = if (landscape) landscapeHero.titleStyle else typo().titleLarge,
                                    color = pageContentColor,
                                    maxLines = if (landscape) landscapeHero.titleMaxLines else 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                                Row(
                                    Modifier.fillMaxWidth().padding(top = if (landscape) 4.dp else 8.dp),
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
                                    Text(
                                        data.author.name,
                                        style = typo().labelSmall,
                                        color = pageContentColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                            .clickable(
                                                enabled = !data.author.id.isNullOrEmpty(),
                                                role = Role.Button,
                                            ) {
                                                data.author.id?.takeIf { it.isNotEmpty() }?.let {
                                                    navController.navigate(ArtistDestination(it))
                                                }
                                            },
                                    )
                                }
                                Text(
                                    stringResource(Res.string.podcasts), style = typo().bodyMedium,
                                    color = pageSecondaryContentColor, maxLines = 1,
                                    modifier = Modifier.padding(top = if (landscape) 4.dp else 8.dp),
                                )
                            }
                            Box(Modifier.fillMaxWidth()) {
                                Box(Modifier.fillMaxWidth().height(
                                    if (landscape) landscapeHero.topPadding + landscapeHero.coverSize else 260.dp,
                                )) {
                                    Box(Modifier.matchParentSize().angledGradientBackground(gradientColors, 25f))
                                    Box(
                                        Modifier.fillMaxWidth().height(180.dp).align(Alignment.BottomCenter)
                                            .background(artworkScrimBrush(pageBackground)),
                                    )
                                }
                                if (landscape) {
                                    Row(
                                        Modifier.fillMaxWidth().padding(
                                            start = landscapeHero.horizontalPadding,
                                            end = landscapeHero.horizontalPadding,
                                            top = landscapeHero.topPadding,
                                            bottom = landscapeHero.bottomPadding,
                                        ),
                                        horizontalArrangement = Arrangement.spacedBy(landscapeHero.columnSpacing),
                                        verticalAlignment = Alignment.Top,
                                    ) {
                                        cover(Modifier.size(landscapeHero.coverSize))
                                        Column(Modifier.weight(1f)) {
                                            details()
                                            Spacer(Modifier.height(landscapeHero.actionSpacing))
                                            PodcastActions(
                                                isFavorite = isFavorite,
                                                onLike = { viewModel.onUIEvent(PodcastUIEvent.ToggleFavorite(id, !isFavorite)) },
                                                onPlay = { viewModel.onUIEvent(PodcastUIEvent.PlayAll(id)) },
                                                onShuffle = { viewModel.onUIEvent(PodcastUIEvent.Shuffle(id)) },
                                                onShare = { viewModel.onUIEvent(PodcastUIEvent.Share(id)) },
                                                compact = landscapeHero.compact,
                                                backgroundColor = pageBackground,
                                                contentColor = pageContentColor,
                                                primaryActionBackground = primaryActionBackground,
                                                primaryActionContent = primaryActionContent,
                                                modifier = Modifier.fillMaxWidth(),
                                                extendBackground = landscape,
                                            )
                                        }
                                    }
                                } else {
                                    Column(Modifier.fillMaxWidth()) {
                                        Spacer(Modifier.height(searchHeaderHeight + 12.dp))
                                        cover(Modifier.size(250.dp).align(Alignment.CenterHorizontally))
                                        Column(Modifier.padding(horizontal = pageContentHorizontalInset(32.dp))) {
                                            Spacer(Modifier.height(25.dp))
                                            details()
                                        }
                                    }
                                }
                            }
                        }
                        item(key = "podcast-actions", contentType = "actions") {
                            if (!landscape) PodcastActions(
                                isFavorite = isFavorite,
                                onLike = { viewModel.onUIEvent(PodcastUIEvent.ToggleFavorite(id, !isFavorite)) },
                                onPlay = { viewModel.onUIEvent(PodcastUIEvent.PlayAll(id)) },
                                onShuffle = { viewModel.onUIEvent(PodcastUIEvent.Shuffle(id)) },
                                onShare = { viewModel.onUIEvent(PodcastUIEvent.Share(id)) },
                                backgroundColor = pageBackground,
                                contentColor = pageContentColor,
                                primaryActionBackground = primaryActionBackground,
                                primaryActionContent = primaryActionContent,
                            )
                        }
                        item(key = "podcast-description", contentType = "description") {
                            val uriHandler = LocalUriHandler.current
                            Column(Modifier.padding(horizontal = pageContentHorizontalInset(32.dp))) {
                                DescriptionView(
                                    text = data.description ?: stringResource(Res.string.no_description),
                                    limitLine = 3,
                                    onTimeClicked = {},
                                    onURLClicked = { uriHandler.openUri(it) },
                                )
                                Text(
                                    stringResource(Res.string.album_length, episodes.size.toString(), ""),
                                    color = pageContentColor, style = typo().bodyMedium,
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
                                    Text(stringResource(Res.string.no_results_found), color = pageContentColor)
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
            tint = pageBackground,
            contentColor = pageContentColor,
            onBack = { navController.navigateUp() },
            showSurface = showHeaderTitle || showSearchBar || filter.isNotEmpty(),
            modifier = Modifier.windowInsetsPadding(LocalPageContentInsets.current),
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
                    tint = pageContentColor,
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
    backgroundColor: Color,
    contentColor: Color,
    primaryActionBackground: Color,
    primaryActionContent: Color,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    extendBackground: Boolean = false,
) {
    val buttonSize = if (compact) 36.dp else 40.dp
    GlassBackdropBox(
        backgroundColor = backgroundColor,
        modifier = modifier.fillMaxWidth().padding(
            horizontal = if (compact) 0.dp else 16.dp,
            vertical = if (compact) 0.dp else 12.dp,
        ),
        auroraBackground = backgroundColor.takeIf { extendBackground },
    ) { backdrop ->
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(
                if (compact) 12.dp else 16.dp,
                Alignment.CenterHorizontally,
            ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LiquidGlassContainer(backdrop, Modifier.size(buttonSize)) {
                HeartCheckBox(
                    size = buttonSize.value.toInt(),
                    checked = isFavorite,
                    tint = contentColor,
                    onStateChange = onLike,
                )
            }
            Row(
                modifier = Modifier.width(if (compact) 104.dp else 112.dp)
                    .height(if (compact) 40.dp else 56.dp).clip(CircleShape).background(primaryActionBackground)
                    .clickable(role = Role.Button, onClick = onPlay),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(SimpIcons.PlayArrow, contentDescription = null, tint = primaryActionContent, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(6.dp))
                Text(stringResource(Res.string.play_action), color = primaryActionContent, style = typo().labelLarge)
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LiquidGlassIconButton(
                    backdrop, SimpIcons.Shuffle,
                    Modifier.size(buttonSize).localizedDescription(Res.string.shuffle),
                    tint = contentColor,
                    onClick = onShuffle,
                )
                LiquidGlassIconButton(
                    backdrop, SimpIcons.Share,
                    Modifier.size(buttonSize).localizedDescription(Res.string.share),
                    tint = contentColor,
                    onClick = onShare,
                )
            }
        }
    }
}
