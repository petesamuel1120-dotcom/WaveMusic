package com.maxrave.simpmusic.ui.screen.library

import simpmusic.composeapp.generated.resources.download_track_named
import simpmusic.composeapp.generated.resources.pause_action
import simpmusic.composeapp.generated.resources.pinned
import simpmusic.composeapp.generated.resources.play_action
import simpmusic.composeapp.generated.resources.remove_favorite_named
import simpmusic.composeapp.generated.resources.shuffle

import com.maxrave.simpmusic.ui.component.adaptiveCardContainerColor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.maxrave.common.Config
import com.maxrave.domain.data.entities.DownloadState
import com.maxrave.domain.data.entities.SongEntity
import com.maxrave.domain.data.model.podcast.PodcastBrowse
import com.maxrave.domain.data.model.searchResult.songs.Artist
import com.maxrave.domain.data.model.searchResult.songs.Thumbnail
import com.maxrave.domain.mediaservice.handler.DownloadHandler
import com.maxrave.domain.mediaservice.handler.PlaylistType
import com.maxrave.domain.mediaservice.handler.QueueData
import com.maxrave.domain.repository.SongRepository
import com.maxrave.domain.utils.toTrack
import com.maxrave.simpmusic.expect.ui.PlatformBackdrop
import com.maxrave.simpmusic.expect.ui.rememberBackdrop
import com.maxrave.simpmusic.extension.getScreenSizeInfo
import com.maxrave.simpmusic.ui.component.EndOfPage
import com.maxrave.simpmusic.ui.component.FrostedHeader
import com.maxrave.simpmusic.ui.component.LandscapeHeroActionMaxWidth
import com.maxrave.simpmusic.ui.component.PageTopAppBar
import com.maxrave.simpmusic.ui.component.PodcastEpisodeFullWidthItem
import com.maxrave.simpmusic.ui.component.RippleIconButton
import com.maxrave.simpmusic.ui.component.StickyPlaylistTitleState
import com.maxrave.simpmusic.ui.component.frostedHeaderSource
import com.maxrave.simpmusic.ui.component.liquidGlass
import com.maxrave.simpmusic.ui.component.onlineLandscapeHeroLayout
import com.maxrave.simpmusic.ui.component.pageHeaderContentHeight
import com.maxrave.simpmusic.ui.icon.ArrowBackIosNew
import com.maxrave.simpmusic.ui.icon.DownloadForOffline
import com.maxrave.simpmusic.ui.icon.Download
import com.maxrave.simpmusic.ui.icon.Downloading
import com.maxrave.simpmusic.ui.icon.Favorite
import com.maxrave.simpmusic.ui.icon.Mic
import com.maxrave.simpmusic.ui.icon.Pause
import com.maxrave.simpmusic.ui.icon.PushPin
import com.maxrave.simpmusic.ui.icon.PlayArrow
import com.maxrave.simpmusic.ui.icon.Shuffle
import com.maxrave.simpmusic.ui.icon.SimpIcons
import com.maxrave.simpmusic.ui.theme.typo
import com.maxrave.simpmusic.ui.theme.LocalLiquidGlassEnabled
import com.maxrave.simpmusic.viewModel.LibraryDynamicPlaylistViewModel
import com.maxrave.simpmusic.viewModel.SharedViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.firstOrNull
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import simpmusic.composeapp.generated.resources.Res
import simpmusic.composeapp.generated.resources.favorite_podcast_episode
import simpmusic.composeapp.generated.resources.favorite_podcast_episode_count
import simpmusic.composeapp.generated.resources.favorite_podcast_episode_duration
import simpmusic.composeapp.generated.resources.favorite_podcast_episode_empty
import simpmusic.composeapp.generated.resources.favorite_podcast_episode_page_description
import simpmusic.composeapp.generated.resources.favorite_podcast_episode_page_title
import simpmusic.composeapp.generated.resources.favorite_podcast_episode_byline
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

const val YOUR_EPISODES_ROUTE_TYPE = "your_podcast_episodes"

@Composable
fun YourEpisodesLibraryTile(navController: NavController) {
    val tileShape = RoundedCornerShape(18.dp)
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)
            .clip(tileShape)
            .background(adaptiveCardContainerColor(MaterialTheme.colorScheme.surfaceContainerLow), tileShape)
            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f)), tileShape)
            .clickable {
                navController.navigate(
                    com.maxrave.simpmusic.ui.navigation.destination.library.LibraryDynamicPlaylistDestination(
                        type = YOUR_EPISODES_ROUTE_TYPE,
                    ),
                )
            }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(48.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(
                    Brush.linearGradient(listOf(Color(0xFFFF5BAE), Color(0xFFC45BEB), Color(0xFF755BFF))),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(SimpIcons.Mic, contentDescription = null, tint = Color.White, modifier = Modifier.size(27.dp))
            Icon(
                SimpIcons.Favorite,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.align(Alignment.BottomEnd).padding(3.dp).size(15.dp),
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(stringResource(Res.string.favorite_podcast_episode_page_title), style = typo().titleSmall, color = MaterialTheme.colorScheme.onSurface)
            Text(stringResource(Res.string.favorite_podcast_episode_page_description), style = typo().bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(
            imageVector = SimpIcons.PushPin,
            contentDescription = stringResource(Res.string.pinned),
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
fun YourEpisodesScreen(
    innerPadding: PaddingValues,
    navController: NavController,
    headerTint: Color,
    playlistViewModel: LibraryDynamicPlaylistViewModel,
    sharedViewModel: SharedViewModel = koinInject(),
    downloadHandler: DownloadHandler = koinInject(),
    songRepository: SongRepository = koinInject(),
) {
    LaunchedEffect(playlistViewModel) {
        playlistViewModel.observe(LibraryDynamicPlaylistType.Favorite)
    }
    val favorites by playlistViewModel.listFavoritePodcastEpisodes.collectAsStateWithLifecycle()
    val entries = remember(favorites) { favorites.sortedByDescending { it.favoriteAt } }
    // SongEntity.toTrack() predates podcasts and drops resultType; restore it so the player
    // continues to treat these queued items as podcast episodes.
    val listTracks = remember(entries) { entries.map { it.toTrack().copy(resultType = it.resultType) } }
    val listState = rememberLazyListState()
    val heroKey = "your-episodes-hero"
    val stickyTitle = remember(listState) { StickyPlaylistTitleState(listState, heroKey) }
    val backdrop = rememberBackdrop(headerTint)
    val hazeState = rememberHazeState()
    val controller by sharedViewModel.controllerState.collectAsStateWithLifecycle()
    val queue by sharedViewModel.getQueueDataState().collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    val isCurrentQueue = queue?.data?.playlistName == "Your Episodes"
    val totalDurationSeconds = remember(entries) { entries.sumOf { it.durationInSeconds() } }

    fun startEpisodes(shuffle: Boolean = false) {
        if (listTracks.isEmpty()) return
        val tracks = if (shuffle) listTracks.shuffled() else listTracks
        val selected = tracks.first()
        sharedViewModel.setQueueData(
            QueueData.Data(
                listTracks = tracks,
                firstPlayedTrack = selected,
                playlistId = "your_podcast_episodes",
                playlistName = "Your Episodes",
                playlistType = PlaylistType.PLAYLIST,
                continuation = null,
            ),
        )
        sharedViewModel.loadMediaItem(selected, Config.PLAYLIST_CLICK, 0)
    }

    Box(Modifier.fillMaxSize()) {
        // Keep the backdrop source separate from the scrolling content and its glass controls.
        // Capturing the LazyColumn here feeds the hero's glass controls back into their own source.
        Box(
            Modifier.matchParentSize()
                .frostedHeaderSource(backdrop, auroraBackground = headerTint),
        )
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().then(stickyTitle.viewportModifier)
                .hazeSource(hazeState),
            contentPadding = innerPadding,
        ) {
            item { Spacer(Modifier.height(pageHeaderContentHeight() + 8.dp)) }
            item(key = heroKey) {
                YourEpisodesHero(
                    entries = entries,
                    totalDurationSeconds = totalDurationSeconds,
                    backdrop = backdrop,
                    stickyTitle = stickyTitle,
                    isPlaying = isCurrentQueue && controller.isPlaying,
                    onPlayPause = {
                        if (isCurrentQueue) sharedViewModel.onUIEvent(com.maxrave.simpmusic.viewModel.UIEvent.PlayPause)
                        else startEpisodes()
                    },
                    onShuffle = { startEpisodes(shuffle = true) },
                )
            }
            if (entries.isEmpty()) {
                item {
                    Text(
                        stringResource(Res.string.favorite_podcast_episode_empty),
                        style = typo().bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.68f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 28.dp),
                    )
                }
            } else {
                items(entries, key = { it.videoId }) { song ->
                    PodcastEpisodeFullWidthItem(
                        episode = song.toPodcastEpisodeItem(),
                        onClick = { videoId ->
                            val index = entries.indexOfFirst { it.videoId == videoId }
                            if (index >= 0 && listTracks.isNotEmpty()) {
                                sharedViewModel.setQueueData(
                                    QueueData.Data(
                                        listTracks = listTracks,
                                        firstPlayedTrack = listTracks[index],
                                        playlistId = "your_podcast_episodes",
                                        playlistName = "Your Episodes",
                                        playlistType = PlaylistType.PLAYLIST,
                                        continuation = null,
                                    ),
                                )
                                sharedViewModel.loadMediaItem(listTracks[index], Config.PLAYLIST_CLICK, index)
                            }
                        },
                        trailingContent = {
                            val downloadIcon = when (song.downloadState) {
                                DownloadState.STATE_DOWNLOADED -> SimpIcons.DownloadForOffline
                                DownloadState.STATE_PREPARING, DownloadState.STATE_DOWNLOADING -> SimpIcons.Downloading
                                else -> SimpIcons.Download
                            }
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RippleIconButton(
                                    imageVector = downloadIcon,
                                    modifier = Modifier.size(40.dp),
                                    tint = if (song.downloadState == DownloadState.STATE_DOWNLOADED) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    iconSize = 20.dp,
                                    contentDescription = stringResource(Res.string.download_track_named, song.title),
                                ) {
                                    if (song.downloadState != DownloadState.STATE_DOWNLOADED &&
                                        song.downloadState != DownloadState.STATE_PREPARING &&
                                        song.downloadState != DownloadState.STATE_DOWNLOADING
                                    ) {
                                        coroutineScope.launch {
                                            downloadHandler.downloadTrack(song.videoId, song.title, song.thumbnails.orEmpty())
                                        }
                                    }
                                }
                                RippleIconButton(
                                    imageVector = SimpIcons.Favorite,
                                    modifier = Modifier.size(40.dp),
                                    tint = MaterialTheme.colorScheme.primary,
                                    iconSize = 20.dp,
                                    contentDescription = stringResource(Res.string.remove_favorite_named, song.title),
                                ) {
                                    coroutineScope.launch {
                                        songRepository.insertSong(song).firstOrNull()
                                        songRepository.setLikedLocally(song.videoId, false)
                                    }
                                }
                            }
                        },
                    )
                }
            }
            item { EndOfPage(bottomPaddingAlreadyApplied = innerPadding.calculateBottomPadding()) }
        }
        FrostedHeader(
            hazeState = hazeState,
            modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth(),
            backdrop = backdrop,
            tint = headerTint,
            shareHeader = true,
            showSurface = stickyTitle.isDocked,
        ) {
            Box {
                PageTopAppBar(
                    backdrop = backdrop,
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    title = {
                        val title = stringResource(Res.string.favorite_podcast_episode_page_title)
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            stickyTitle.Target(title)
                            if (!LocalLiquidGlassEnabled.current && stickyTitle.isDocked) {
                                Text(
                                    text = title,
                                    style = typo().titleMedium,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        RippleIconButton(SimpIcons.ArrowBackIosNew, Modifier.size(32.dp), tint = MaterialTheme.colorScheme.onBackground) {
                            navController.navigateUp()
                        }
                    },
                )
                if (LocalLiquidGlassEnabled.current) {
                    with(stickyTitle) {
                        Title(stringResource(Res.string.favorite_podcast_episode_page_title), MaterialTheme.colorScheme.onBackground)
                    }
                }
            }
        }
    }
}

@Composable
private fun YourEpisodesHero(
    entries: List<SongEntity>,
    totalDurationSeconds: Long,
    backdrop: PlatformBackdrop,
    stickyTitle: StickyPlaylistTitleState,
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onShuffle: () -> Unit,
) {
    val isLandscape = getScreenSizeInfo().let { it.wDP >= it.hDP }
    val landscapeLayout = if (isLandscape) onlineLandscapeHeroLayout(maxCoverSize = 154.dp) else null
    val contentColor = MaterialTheme.colorScheme.onBackground
    val title = stringResource(Res.string.favorite_podcast_episode_page_title)
    val creator = entries.firstOrNull()?.artistName?.firstOrNull()
    val count = pluralStringResource(Res.plurals.favorite_podcast_episode_count, entries.size, entries.size)
    val totalTime = stringResource(Res.string.favorite_podcast_episode_duration, formatEpisodeDuration(totalDurationSeconds))
    val summary = "$count • $totalTime"
    val titleAlignment = if (isLandscape) TextAlign.Start else TextAlign.Center
    val titleModifier = stickyTitle.sourceModifier(typo().titleMedium, textAlign = titleAlignment)

    @Composable
    fun Metadata(
        modifier: Modifier,
        alignment: Alignment.Horizontal,
        showActions: Boolean,
    ) {
        Column(modifier = modifier, horizontalAlignment = alignment) {
            Text(
                title,
                modifier = Modifier.then(titleModifier).fillMaxWidth(),
                style = typo().titleMedium,
                color = contentColor,
                textAlign = titleAlignment,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                creator?.let { stringResource(Res.string.favorite_podcast_episode_byline, it) }
                    ?: stringResource(Res.string.favorite_podcast_episode_page_description),
                style = typo().bodySmall,
                color = contentColor.copy(alpha = 0.68f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = titleAlignment,
            )
            Spacer(Modifier.height(6.dp))
            Text(summary, style = typo().bodyMedium, color = contentColor.copy(alpha = 0.68f), maxLines = 1, textAlign = titleAlignment)
            if (showActions) {
                Spacer(Modifier.height(14.dp))
                YourEpisodesActions(
                    backdrop = backdrop,
                    isPlaying = isPlaying,
                    contentColor = contentColor,
                    onPlayPause = onPlayPause,
                    onShuffle = onShuffle,
                    enabled = entries.isNotEmpty(),
                    isLandscape = isLandscape,
                )
            }
        }
    }

    val icon: @Composable () -> Unit = {
        Box(
            Modifier.size(landscapeLayout?.coverSize?.coerceAtLeast(120.dp) ?: 132.dp)
                .clip(RoundedCornerShape(26.dp))
                .clickable(enabled = entries.isNotEmpty(), onClick = onPlayPause),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier.fillMaxSize().background(
                    Brush.linearGradient(listOf(Color(0xFFFF5BAE), Color(0xFFC45BEB), Color(0xFF755BFF))),
                ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(SimpIcons.Mic, contentDescription = title, tint = Color.White, modifier = Modifier.size(62.dp))
                Icon(SimpIcons.Favorite, contentDescription = null, tint = Color.White,
                    modifier = Modifier.align(Alignment.BottomEnd).padding(22.dp).size(24.dp))
            }
        }
    }

    if (isLandscape) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                icon()
                Spacer(Modifier.width(24.dp))
                Metadata(Modifier.weight(1f), Alignment.Start, showActions = true)
            }
        }
    } else {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            icon()
            Spacer(Modifier.height(18.dp))
            Metadata(Modifier.fillMaxWidth(), Alignment.CenterHorizontally, showActions = true)
        }
    }
}

@Composable
private fun YourEpisodesActions(
    modifier: Modifier = Modifier,
    backdrop: PlatformBackdrop,
    isPlaying: Boolean,
    contentColor: Color,
    onPlayPause: () -> Unit,
    onShuffle: () -> Unit,
    enabled: Boolean,
    isLandscape: Boolean,
) {
    Row(
        modifier.widthIn(max = if (isLandscape) LandscapeHeroActionMaxWidth else 380.dp).fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EpisodeSideAction(backdrop, onClick = onShuffle, enabled = enabled) {
            Icon(SimpIcons.Shuffle, contentDescription = stringResource(Res.string.shuffle), tint = contentColor, modifier = Modifier.size(30.dp))
        }
        Row(
            Modifier.weight(1f).height(54.dp).clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .clickable(enabled = enabled, role = Role.Button, onClick = onPlayPause)
                .padding(horizontal = 18.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                if (isPlaying) SimpIcons.Pause else SimpIcons.PlayArrow,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(22.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(if (isPlaying) stringResource(Res.string.pause_action) else stringResource(Res.string.play_action), style = typo().labelLarge, color = MaterialTheme.colorScheme.onPrimary, maxLines = 1)
        }
    }
}

@Composable
private fun EpisodeSideAction(
    backdrop: PlatformBackdrop,
    onClick: () -> Unit,
    enabled: Boolean,
    content: @Composable () -> Unit,
) {
    Box(
        Modifier.size(56.dp).liquidGlass(backdrop, CircleShape).clip(CircleShape)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
private fun SongEntity.toPodcastEpisodeItem(): PodcastBrowse.EpisodeItem {
    val authorName = artistName?.firstOrNull().orEmpty()
    return PodcastBrowse.EpisodeItem(
        title = title,
        author = Artist(id = artistId?.firstOrNull().orEmpty(), name = authorName),
        description = authorName.takeIf { it.isNotBlank() },
        thumbnail = listOfNotNull(thumbnails?.takeIf { it.isNotBlank() }?.let { Thumbnail(url = it, width = 544, height = 544) }),
        createdDay = stringResource(Res.string.favorite_podcast_episode),
        durationString = duration.takeIf { it.isNotBlank() },
        videoId = videoId,
    )
}

private fun formatEpisodeDuration(durationSeconds: Long): String {
    val hours = durationSeconds / 3600L
    val minutes = (durationSeconds % 3600L) / 60L
    return if (hours > 0L) "${hours}h ${minutes}m" else "${minutes} min"
}

private fun SongEntity.durationInSeconds(): Long {
    if (durationSeconds > 0) return durationSeconds.toLong()
    val value = duration.trim().lowercase()
    if (value.isEmpty()) return 0L

    if (':' in value) {
        val parts = value.split(':').map { it.trim().toLongOrNull() }
        if (parts.all { it != null }) {
            return when (parts.size) {
                2 -> parts[0]!! * 60L + parts[1]!!
                3 -> parts[0]!! * 3600L + parts[1]!! * 60L + parts[2]!!
                else -> 0L
            }
        }
    }

    fun amount(pattern: String): Long? = Regex(pattern).find(value)?.groupValues?.get(1)?.toLongOrNull()

    val hours = amount("(\\d+)\\s*(?:h|hr|hrs|hour|hours)\\b")
    val minutes = amount("(\\d+)\\s*(?:m|min|mins|minute|minutes)\\b")
    val seconds = amount("(\\d+)\\s*(?:s|sec|secs|second|seconds)\\b")
    if (hours != null || minutes != null || seconds != null) {
        return (hours ?: 0L) * 3600L + (minutes ?: 0L) * 60L + (seconds ?: 0L)
    }

    // Podcast metadata sometimes stores a plain minute count such as "47".
    return value.toLongOrNull()?.times(60L) ?: 0L
}
