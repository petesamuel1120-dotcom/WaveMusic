package com.petedev.wavemusic.ui.screen.other

import com.petedev.wavemusic.ui.component.localizedDescription
import com.petedev.wavemusic.resources.play_action
import com.petedev.wavemusic.resources.share

import com.petedev.wavemusic.ui.component.pageContentHorizontalInset
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.kmpalette.rememberPaletteState
import com.petedev.wavemusic.domain.data.model.browse.album.Track
import com.petedev.wavemusic.domain.utils.toSongEntity
import com.petedev.wavemusic.domain.utils.toTrack
import com.petedev.wavemusic.expect.ui.rememberBackdrop
import com.petedev.wavemusic.expect.ui.PlatformBackdrop
import com.petedev.wavemusic.expect.ui.layerBackdrop
import com.petedev.wavemusic.expect.ui.toImageBitmap
import com.petedev.wavemusic.extension.getScreenSizeInfo
import com.petedev.wavemusic.extension.toImmersiveBackground
import com.petedev.wavemusic.ui.component.CenterLoadingBox
import com.petedev.wavemusic.ui.component.DescriptionView
import com.petedev.wavemusic.ui.component.EndOfPage
import com.petedev.wavemusic.ui.component.GlassBackdropBox
import com.petedev.wavemusic.ui.component.HeartCheckBox
import com.petedev.wavemusic.ui.component.LiquidGlassContainer
import com.petedev.wavemusic.ui.component.LocalPageContentInsets
import com.petedev.wavemusic.ui.component.LocalPageArtworkBackground
import com.petedev.wavemusic.ui.component.LiquidGlassIconButton
import com.petedev.wavemusic.ui.component.NowPlayingBottomSheet
import com.petedev.wavemusic.ui.component.PageActionHeader
import com.petedev.wavemusic.ui.component.onlineLandscapeHeroLayout
import com.petedev.wavemusic.ui.component.PlaylistSearchBar
import com.petedev.wavemusic.ui.component.PodcastEpisodeFullWidthItem
import com.petedev.wavemusic.ui.component.SearchBarBackHandler
import com.petedev.wavemusic.ui.component.SearchBarEnter
import com.petedev.wavemusic.ui.component.SearchBarExit
import com.petedev.wavemusic.ui.component.SearchBarScrollDismissEffect
import com.petedev.wavemusic.ui.component.SearchResultsScrollEffect
import com.petedev.wavemusic.ui.component.SharedHeaderContentHeight
import com.petedev.wavemusic.ui.component.StickyPlaylistTitleState
import com.petedev.wavemusic.ui.component.frostedHeaderSource
import com.petedev.wavemusic.ui.component.headerGlassControlSize
import com.petedev.wavemusic.ui.component.headerGlassIconSize
import com.petedev.wavemusic.ui.component.landscapeChromeSize
import com.petedev.wavemusic.ui.component.landscapeChromeTextStyle
import com.petedev.wavemusic.ui.component.rememberHolderPainter
import com.petedev.wavemusic.ui.icon.Close
import com.petedev.wavemusic.ui.icon.PlayArrow
import com.petedev.wavemusic.ui.icon.Search
import com.petedev.wavemusic.ui.icon.Share
import com.petedev.wavemusic.ui.icon.WaveIcons
import com.petedev.wavemusic.ui.navigation.destination.list.ArtistDestination
import com.petedev.wavemusic.ui.theme.LocalAppColorScheme
import com.petedev.wavemusic.ui.theme.LocalAppIsDarkTheme
import com.petedev.wavemusic.ui.theme.LocalLiquidGlassEnabled
import com.petedev.wavemusic.ui.theme.typo
import com.petedev.wavemusic.viewModel.PodcastUIEvent
import com.petedev.wavemusic.viewModel.PodcastUIState
import com.petedev.wavemusic.viewModel.PodcastViewModel
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import com.petedev.wavemusic.resources.Res
import com.petedev.wavemusic.resources.album_length
import com.petedev.wavemusic.resources.no_description
import com.petedev.wavemusic.resources.no_results_found
import com.petedev.wavemusic.resources.podcasts

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun PodcastScreen(
    viewModel: PodcastViewModel = koinViewModel(),
    podcastId: String,
    navController: NavController,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isFavorite by viewModel.isFavorite.collectAsStateWithLifecycle()
    val lazyState = rememberLazyListState()
    val stickyTitle = remember(lazyState, podcastId) { StickyPlaylistTitleState(lazyState, "podcast-hero") }
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
    val success = (uiState as? PodcastUIState.Success)?.takeIf { it.id == podcastId }
    val podcastContentTransition = updateTransition(uiState, label = "podcastPage")
    val isPodcastContentReady = success != null &&
        podcastContentTransition.currentState == uiState && !podcastContentTransition.isRunning
    val hasPodcastTitleScroll by remember(lazyState) {
        derivedStateOf { lazyState.firstVisibleItemIndex > 0 || lazyState.firstVisibleItemScrollOffset > 0 }
    }
    val artworkUrl = success?.data?.thumbnail?.lastOrNull()?.url
    // A new podcast must not reuse the previous cover's palette while its image loads.
    val paletteState = key(podcastId, artworkUrl) { rememberPaletteState() }
    var bitmap by remember(podcastId, artworkUrl) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(bitmap, paletteState) {
        bitmap?.let { paletteState.generate(it) }
    }
    val pageBackground = paletteState.palette?.toImmersiveBackground(isDark)
        ?: if (isDark) Color.Black else appColorScheme.background
    val pageArtworkBackgroundSink = LocalPageArtworkBackground.current
    SideEffect {
        pageArtworkBackgroundSink?.value = pageBackground.takeIf { success != null }
    }
    val hazeState = rememberHazeState()
    val frostedBackdrop = rememberBackdrop(pageBackground)
    val podcastActionsBackdrop = rememberBackdrop(pageBackground)
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
    // Show the compact header title only after the original title has fully left the page.
    val showHeaderTitle = isPodcastContentReady && hasPodcastTitleScroll && stickyTitle.isSourceOutOfViewport
    val screenInfo = getScreenSizeInfo()
    val landscape = screenInfo.wDP >= screenInfo.hDP
    val landscapeHero = onlineLandscapeHeroLayout(maxCoverSize = 250.dp)
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
        podcastContentTransition.Crossfade { state ->
            when (state) {
                is PodcastUIState.Success -> {
                    val data = state.data
                    val id = state.id
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                            .windowInsetsPadding(LocalPageContentInsets.current)
                            .then(stickyTitle.viewportModifier)
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
                                    onSuccess = {
                                        if (id == podcastId && data.thumbnail.lastOrNull()?.url == artworkUrl) {
                                            bitmap = it.result.image.toImageBitmap()
                                        }
                                    },
                                    modifier = modifier.clip(RoundedCornerShape(8.dp)),
                                )
                            }
                            val details: @Composable () -> Unit = {
                                val tightMetadata = landscape && landscapeHero.coverSize < landscapeChromeSize(110.dp)
                                val titleStyle = when {
                                    tightMetadata -> landscapeChromeTextStyle(typo().titleMedium.copy(fontSize = 14.sp, lineHeight = 16.sp))
                                    landscape -> landscapeHero.titleStyle
                                    else -> typo().titleLarge
                                }
                                Text(
                                    data.title,
                                    style = titleStyle,
                                    color = pageContentColor,
                                    maxLines = if (landscape) {
                                        if (landscapeHero.coverSize < landscapeChromeSize(152.dp)) 1 else landscapeHero.titleMaxLines
                                    } else 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.fillMaxWidth().then(
                                        stickyTitle.sourceModifier(
                                            style = titleStyle,
                                            color = pageContentColor,
                                            hideSource = false,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Start,
                                        ),
                                    ),
                                )
                                Row(
                                    Modifier.fillMaxWidth().padding(top = if (tightMetadata) 0.dp else if (landscape) 4.dp else 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalPlatformContext.current)
                                            .data(data.authorThumbnail).diskCachePolicy(CachePolicy.ENABLED)
                                            .diskCacheKey(data.authorThumbnail).crossfade(true).build(),
                                        placeholder = rememberHolderPainter(),
                                        error = rememberHolderPainter(),
                                        contentDescription = null,
                                        modifier = Modifier.size(landscapeChromeSize(if (tightMetadata) 12.dp else 25.dp)).clip(CircleShape),
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        data.author.name,
                                        style = landscapeChromeTextStyle(
                                            if (tightMetadata) typo().labelSmall.copy(fontSize = 10.sp, lineHeight = 12.sp) else typo().labelSmall,
                                        ),
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
                                // The mini-player can leave a very short hero. Keep the title and
                                // author readable instead of squeezing the redundant category label.
                                if (!tightMetadata) Text(
                                    stringResource(Res.string.podcasts),
                                    style = landscapeChromeTextStyle(if (landscape) typo().labelMedium else typo().bodyMedium),
                                    color = pageSecondaryContentColor, maxLines = 1,
                                    modifier = Modifier.padding(top = if (landscape) 4.dp else 8.dp),
                                )
                            }
                            Box(Modifier.fillMaxWidth()) {
                                Box(Modifier.fillMaxWidth().height(
                                    if (landscape) landscapeHero.topPadding + landscapeHero.coverSize else 260.dp,
                                ).then(
                                    if (landscape && LocalLiquidGlassEnabled.current) {
                                        Modifier.layerBackdrop(podcastActionsBackdrop)
                                    } else {
                                        Modifier
                                    },
                                )) {
                                    // Match the full-window artwork ground, including the cutout
                                    // margins and dock. A dark accent gradient made light pages grey.
                                    Box(Modifier.matchParentSize().background(pageBackground))
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
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        cover(Modifier.size(landscapeHero.coverSize))
                                        // Reserve the dock's measured height through coverSize,
                                        // and fit metadata plus controls into that same safe frame.
                                        Column(Modifier.weight(1f).height(landscapeHero.coverSize)) {
                                            Column(
                                                Modifier.weight(1f).fillMaxWidth(),
                                                verticalArrangement = Arrangement.Center,
                                            ) { details() }
                                            Spacer(Modifier.height(landscapeHero.actionSpacing))
                                            PodcastActions(
                                                isFavorite = isFavorite,
                                                onLike = { viewModel.onUIEvent(PodcastUIEvent.ToggleFavorite(id, !isFavorite)) },
                                                onPlay = { viewModel.onUIEvent(PodcastUIEvent.PlayAll(id)) },
                                                onShare = { viewModel.onUIEvent(PodcastUIEvent.Share(id)) },
                                                compact = landscapeHero.compact,
                                                contentAlignment = Alignment.CenterStart,
                                                backgroundColor = pageBackground,
                                                contentColor = pageContentColor,
                                                primaryActionBackground = primaryActionBackground,
                                                primaryActionContent = primaryActionContent,
                                                modifier = Modifier.fillMaxWidth(),
                                                backdrop = podcastActionsBackdrop,
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
                                    textColor = pageSecondaryContentColor,
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
            stickyTitle = stickyTitle,
            scrollState = lazyState,
            animateStickyTitle = false,
        ) {
            IconButton(
                onClick = { if (showSearchBar || filter.isNotEmpty()) dismissSearch() else showSearchBar = true },
                enabled = success != null,
                modifier = Modifier.size(headerGlassControlSize()),
            ) {
                Icon(
                    if (showSearchBar || filter.isNotEmpty()) WaveIcons.Close else WaveIcons.Search,
                    contentDescription = if (showSearchBar || filter.isNotEmpty()) "Close podcast search" else "Search podcast episodes",
                    tint = pageContentColor,
                    modifier = Modifier.size(headerGlassIconSize()),
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
    onShare: () -> Unit,
    backgroundColor: Color,
    contentColor: Color,
    primaryActionBackground: Color,
    primaryActionContent: Color,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    backdrop: PlatformBackdrop? = null,
    contentAlignment: Alignment = Alignment.Center,
) {
    if (backdrop != null) {
        PodcastActionButtons(
            isFavorite, onLike, onPlay, onShare,
            contentColor, primaryActionBackground, primaryActionContent,
            compact, modifier.fillMaxWidth(), backdrop, contentAlignment,
        )
    } else {
        GlassBackdropBox(
            backgroundColor = backgroundColor,
            modifier = modifier.fillMaxWidth().padding(
                horizontal = if (compact) 0.dp else 16.dp,
                vertical = if (compact) 0.dp else 12.dp,
            ),
        ) { actionBackdrop ->
            PodcastActionButtons(
                isFavorite, onLike, onPlay, onShare,
                contentColor, primaryActionBackground, primaryActionContent,
                compact, Modifier.fillMaxWidth(), actionBackdrop, contentAlignment,
            )
        }
    }
}

@Composable
private fun PodcastActionButtons(
    isFavorite: Boolean,
    onLike: () -> Unit,
    onPlay: () -> Unit,
    onShare: () -> Unit,
    contentColor: Color,
    primaryActionBackground: Color,
    primaryActionContent: Color,
    compact: Boolean,
    modifier: Modifier,
    backdrop: PlatformBackdrop,
    contentAlignment: Alignment = Alignment.Center,
) {
    BoxWithConstraints(modifier.fillMaxWidth(), contentAlignment = contentAlignment) {
        val normalButtonSize = if (compact) landscapeChromeSize(40.dp) else 48.dp
        val normalFavoriteSize = if (compact) landscapeChromeSize(32.dp) else 40.dp
        val normalPlayWidth = if (compact) landscapeChromeSize(104.dp) else 112.dp
        val normalSpacing = if (compact) landscapeChromeSize(12.dp) else 16.dp
        // Equal circular controls and equal gaps center Play in both orientations. Keep the
        // heart inset from the larger glass rim, and scale the group only if space is tight.
        val normalRowWidth = normalPlayWidth + (normalSpacing + normalButtonSize) * 2
        val rowWidth = minOf(maxWidth, normalRowWidth)
        val actionScale = minOf(1f, rowWidth / normalRowWidth)
        val buttonSize = normalButtonSize * actionScale
        Row(
            modifier = Modifier.width(rowWidth),
            horizontalArrangement = Arrangement.spacedBy(normalSpacing * actionScale, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LiquidGlassContainer(backdrop, Modifier.size(buttonSize)) {
                HeartCheckBox(
                    size = (normalFavoriteSize * actionScale).value.toInt(),
                    checked = isFavorite,
                    tint = contentColor,
                    onStateChange = onLike,
                )
            }
            Row(
                modifier = Modifier.width(normalPlayWidth * actionScale)
                    .height((if (compact) landscapeChromeSize(40.dp) else 56.dp) * actionScale)
                    .clip(CircleShape).background(primaryActionBackground)
                    .clickable(role = Role.Button, onClick = onPlay),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(WaveIcons.PlayArrow, contentDescription = null, tint = primaryActionContent, modifier = Modifier.size(landscapeChromeSize(24.dp) * actionScale))
                Spacer(Modifier.width(landscapeChromeSize(6.dp) * actionScale))
                val playTextStyle = landscapeChromeTextStyle(typo().labelLarge)
                Text(
                    stringResource(Res.string.play_action), color = primaryActionContent,
                    style = playTextStyle.copy(fontSize = playTextStyle.fontSize * actionScale),
                    maxLines = 1,
                )
            }
            LiquidGlassIconButton(
                backdrop, WaveIcons.Share,
                Modifier.size(buttonSize).localizedDescription(Res.string.share),
                tint = contentColor,
                onClick = onShare,
            )
        }
    }
}
