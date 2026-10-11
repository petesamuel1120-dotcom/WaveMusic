package com.maxrave.simpmusic.ui.screen.library

import org.jetbrains.compose.resources.stringResource
import simpmusic.composeapp.generated.resources.continue_listening
import simpmusic.composeapp.generated.resources.continue_listening_description
import simpmusic.composeapp.generated.resources.download_track_named
import simpmusic.composeapp.generated.resources.pause_action
import simpmusic.composeapp.generated.resources.pinned
import simpmusic.composeapp.generated.resources.play_action
import simpmusic.composeapp.generated.resources.Res
import simpmusic.composeapp.generated.resources.shuffle

import com.maxrave.simpmusic.ui.component.adaptiveCardContainerColor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.maxrave.domain.data.entities.EpisodeEntity
import com.maxrave.domain.data.model.podcast.PodcastBrowse
import com.maxrave.domain.manager.DataStoreManager
import com.maxrave.domain.mediaservice.handler.DownloadHandler
import com.maxrave.domain.mediaservice.handler.PlaylistType
import com.maxrave.domain.mediaservice.handler.QueueData
import com.maxrave.domain.repository.PodcastRepository
import com.maxrave.domain.repository.SongRepository
import com.maxrave.domain.utils.toSongEntity
import com.maxrave.domain.utils.toTrack
import com.maxrave.simpmusic.ui.component.EndOfPage
import com.maxrave.simpmusic.ui.component.FrostedHeader
import com.maxrave.simpmusic.ui.component.LandscapeHeroActionMaxWidth
import com.maxrave.simpmusic.ui.component.PageTopAppBar
import com.maxrave.simpmusic.ui.component.PodcastEpisodeFullWidthItem
import com.maxrave.simpmusic.ui.component.RippleIconButton
import com.maxrave.simpmusic.ui.component.StickyPlaylistTitleState
import com.maxrave.simpmusic.viewModel.SharedViewModel
import com.maxrave.simpmusic.viewModel.PODCAST_RESUME_POINTS_KEY
import com.maxrave.simpmusic.ui.component.frostedHeaderSource
import com.maxrave.simpmusic.ui.component.onlineLandscapeHeroLayout
import com.maxrave.simpmusic.ui.component.pageHeaderContentHeight
import com.maxrave.simpmusic.expect.ui.rememberBackdrop
import com.maxrave.simpmusic.expect.ui.PlatformBackdrop
import com.maxrave.simpmusic.ui.component.liquidGlass
import com.maxrave.simpmusic.ui.icon.ArrowBackIosNew
import com.maxrave.simpmusic.ui.icon.Download
import com.maxrave.simpmusic.ui.icon.Favorite
import com.maxrave.simpmusic.ui.icon.FavoriteBorder
import com.maxrave.simpmusic.ui.icon.GraphicEq
import com.maxrave.simpmusic.ui.icon.Pause
import com.maxrave.simpmusic.ui.icon.PushPin
import com.maxrave.simpmusic.ui.icon.PlayArrow
import com.maxrave.simpmusic.ui.icon.SimpIcons
import com.maxrave.simpmusic.ui.icon.Shuffle
import com.maxrave.simpmusic.ui.theme.typo
import com.maxrave.simpmusic.ui.theme.LocalLiquidGlassEnabled
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

const val CONTINUE_LISTENING_ROUTE_TYPE = "continue_listening"

@Composable
fun ContinueListeningLibraryTile(navController: NavController) {
    val tileShape = RoundedCornerShape(18.dp)
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)
            .clip(tileShape)
            .background(adaptiveCardContainerColor(MaterialTheme.colorScheme.surfaceContainerLow), tileShape)
            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f)), tileShape)
            .clickable {
                navController.navigate(
                    com.maxrave.simpmusic.ui.navigation.destination.library.LibraryDynamicPlaylistDestination(
                        type = CONTINUE_LISTENING_ROUTE_TYPE,
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
                    Brush.linearGradient(listOf(Color(0xFF765BFF), Color(0xFFB24FE3), Color(0xFFFF5BAE))),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                SimpIcons.GraphicEq,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(28.dp),
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(stringResource(Res.string.continue_listening), style = typo().titleSmall, color = MaterialTheme.colorScheme.onSurface)
            Text(stringResource(Res.string.continue_listening_description), style = typo().bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(
            imageVector = SimpIcons.PushPin,
            contentDescription = stringResource(Res.string.pinned),
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
        )
    }
}

private data class EpisodeResumePoint(val position: Long, val duration: Long, val updatedAt: Long)
private data class ContinueListeningEntry(
    val episode: EpisodeEntity,
    val podcastTitle: String,
    val podcastAuthor: String,
    val podcastThumbnail: String?,
    val position: Long,
    val duration: Long,
    val updatedAt: Long,
)

@Composable
fun ContinueListeningScreen(
    innerPadding: PaddingValues,
    navController: NavController,
    headerTint: Color,
    sharedViewModel: SharedViewModel = koinInject(),
    podcastRepository: PodcastRepository = koinInject(),
    songRepository: SongRepository = koinInject(),
    dataStoreManager: DataStoreManager = koinInject(),
    downloadHandler: DownloadHandler = koinInject(),
) {
    var entries by remember { mutableStateOf(emptyList<ContinueListeningEntry>()) }
    LaunchedEffect(podcastRepository, dataStoreManager) {
        // Inventory changes rebuild the index; frequent resume-point writes only copy the
        // episodes that have a saved position, rather than rebuilding every downloaded episode.
        val indexedEpisodes = podcastRepository.getAllPodcastWithEpisodes().map { podcasts ->
            buildMap {
                podcasts.forEach { podcast ->
                    podcast.episodes.forEach { episode ->
                        put(
                            episode.videoId,
                            ContinueListeningEntry(
                                episode = episode,
                                podcastTitle = podcast.podcast.title,
                                podcastAuthor = podcast.podcast.authorName,
                                podcastThumbnail = podcast.podcast.thumbnail,
                                position = 0L,
                                duration = 0L,
                                updatedAt = 0L,
                            ),
                        )
                    }
                }
            }
        }
        combine(
            indexedEpisodes,
            dataStoreManager.getString(PODCAST_RESUME_POINTS_KEY),
        ) { episodeLookup, storedPoints ->
            val resumePoints = storedPoints.parseResumePoints()
            resumePoints.mapNotNull { (videoId, point) ->
                episodeLookup[videoId]?.copy(
                    position = point.position,
                    duration = point.duration,
                    updatedAt = point.updatedAt,
                )
            }.sortedByDescending { it.updatedAt }
        }.flowOn(Dispatchers.Default).collect { entries = it }
    }

    val listState = rememberLazyListState()
    val stickyTitle = remember(listState) { StickyPlaylistTitleState(listState, "continue-listening-hero") }
    val backdrop = rememberBackdrop(headerTint)
    val hazeState = rememberHazeState()
    val controller by sharedViewModel.controllerState.collectAsStateWithLifecycle()
    val queue by sharedViewModel.getQueueDataState().collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    val continueListeningTitle = stringResource(Res.string.continue_listening)
    val isCurrentQueue = queue?.data?.playlistId == "continue_listening"
    val remainingMillis = remember(entries) {
        entries.sumOf { (it.duration - it.position).coerceAtLeast(0L) }
    }
    val listTracks = remember(entries) { entries.map { it.episode.toPodcastBrowseItem().toTrack() } }
    val favoriteEpisodes by songRepository.observeLikedSongs().collectAsStateWithLifecycle(initialValue = emptyList())
    val favoriteEpisodeIds = remember(favoriteEpisodes) { favoriteEpisodes.mapTo(mutableSetOf()) { it.videoId } }

    fun startEpisode(index: Int, shuffled: Boolean = false) {
        if (listTracks.isEmpty()) return
        val tracks = if (shuffled) listTracks.shuffled() else listTracks
        val selected = if (shuffled) tracks.first() else tracks[index.coerceIn(0, tracks.lastIndex)]
        val resume = entries.firstOrNull { it.episode.videoId == selected.videoId }
        sharedViewModel.setQueueData(
            QueueData.Data(
                listTracks = tracks,
                firstPlayedTrack = selected,
                playlistId = "continue_listening",
                playlistName = continueListeningTitle,
                playlistType = PlaylistType.PLAYLIST,
                continuation = null,
            ),
        )
        sharedViewModel.loadMediaItem(selected, Config.PLAYLIST_CLICK, tracks.indexOf(selected))
        if (resume != null && resume.position > 0L && resume.duration > 0L) {
            coroutineScope.launch {
                delay(500)
                sharedViewModel.onUIEvent(
                    com.maxrave.simpmusic.viewModel.UIEvent.UpdateProgress(
                        (resume.position.toFloat() / resume.duration.toFloat()).coerceIn(0f, 1f),
                    ),
                )
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        // Keep the captured backdrop separate from the list and its glass controls. Capturing
        // the LazyColumn here feeds those controls back into their own source and crashes glass.
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
            item(key = "continue-listening-hero") {
                ContinueListeningHero(
                    entries = entries,
                    totalRemaining = remainingMillis,
                    backdrop = backdrop,
                    stickyTitle = stickyTitle,
                    onPlay = {
                        if (isCurrentQueue) sharedViewModel.onUIEvent(com.maxrave.simpmusic.viewModel.UIEvent.PlayPause)
                        else startEpisode(0)
                    },
                    onShuffle = { startEpisode(0, shuffled = true) },
                    isPlaying = isCurrentQueue && controller.isPlaying,
                )
            }
            if (entries.isEmpty()) {
                item {
                    Text(
                        "Your unfinished podcast episodes will appear here.",
                        style = typo().bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 28.dp),
                    )
                }
            } else {
                items(entries, key = { it.episode.videoId }) { entry ->
                    val item = entry.episode.toPodcastBrowseItem().copy(
                        description = "${entry.podcastTitle} • ${formatRemaining(entry.duration - entry.position)} left",
                    )
                    PodcastEpisodeFullWidthItem(
                        episode = item,
                        onClick = { startEpisode(entries.indexOfFirst { it.episode.videoId == entry.episode.videoId }) },
                        modifier = Modifier.fillMaxWidth(),
                        playbackProgress = if (entry.duration > 0L) {
                            (entry.position.toFloat() / entry.duration.toFloat()).coerceIn(0f, 1f)
                        } else {
                            null
                        },
                        trailingContent = {
                            val isFavorite = entry.episode.videoId in favoriteEpisodeIds
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RippleIconButton(
                                    imageVector = SimpIcons.Download,
                                    modifier = Modifier.size(40.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    iconSize = 20.dp,
                                    contentDescription = stringResource(Res.string.download_track_named, entry.episode.title),
                                ) {
                                    coroutineScope.launch {
                                        downloadHandler.downloadTrack(
                                            entry.episode.videoId,
                                            entry.episode.title,
                                            entry.episode.thumbnail ?: entry.podcastThumbnail.orEmpty(),
                                        )
                                    }
                                }
                                RippleIconButton(
                                    imageVector = if (isFavorite) SimpIcons.Favorite else SimpIcons.FavoriteBorder,
                                    modifier = Modifier.size(40.dp),
                                    tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    iconSize = 20.dp,
                                    contentDescription = if (isFavorite) {
                                        "Remove ${entry.episode.title} from favorites"
                                    } else {
                                        "Favorite ${entry.episode.title}"
                                    },
                                ) {
                                    coroutineScope.launch {
                                        songRepository.insertSong(entry.episode.toPodcastBrowseItem().toTrack().toSongEntity()).firstOrNull()
                                        songRepository.setLikedLocally(entry.episode.videoId, !isFavorite)
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
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            stickyTitle.Target(stringResource(Res.string.continue_listening))
                            if (!LocalLiquidGlassEnabled.current && stickyTitle.isDocked) {
                                Text(
                                    text = stringResource(Res.string.continue_listening),
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
                    with(stickyTitle) { Title(stringResource(Res.string.continue_listening), MaterialTheme.colorScheme.onBackground) }
                }
            }
        }
    }
}

@Composable
private fun ContinueListeningHero(
    entries: List<ContinueListeningEntry>,
    totalRemaining: Long,
    backdrop: PlatformBackdrop,
    stickyTitle: StickyPlaylistTitleState,
    onPlay: () -> Unit,
    onShuffle: () -> Unit,
    isPlaying: Boolean,
) {
    val isLandscape = com.maxrave.simpmusic.extension.getScreenSizeInfo().let { it.wDP >= it.hDP }
    val landscapeLayout =
        if (isLandscape) onlineLandscapeHeroLayout(maxCoverSize = 154.dp, extraTopPadding = 8.dp) else null
    val contentColor = MaterialTheme.colorScheme.onBackground
    val titleAlignment = if (isLandscape) TextAlign.Start else TextAlign.Center
    val titleModifier = stickyTitle.sourceModifier(typo().titleMedium, textAlign = titleAlignment)
    val totalLabel = "${entries.size} ${if (entries.size == 1) "episode" else "episodes"} • ${formatRemaining(totalRemaining)} left"
    val creator = entries.firstOrNull()?.podcastAuthor?.let { "by $it" }
    @Composable
    fun Metadata(modifier: Modifier, alignment: Alignment.Horizontal) {
        Column(modifier = modifier, horizontalAlignment = alignment) {
            Text(
                stringResource(Res.string.continue_listening),
                modifier = Modifier.then(titleModifier).fillMaxWidth(),
                style = typo().titleMedium,
                textAlign = titleAlignment,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (creator != null) {
                Spacer(Modifier.height(4.dp))
                Text(creator, style = typo().bodySmall, color = contentColor.copy(alpha = 0.65f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Spacer(Modifier.height(6.dp))
            Text(totalLabel, style = typo().bodyMedium, color = contentColor.copy(alpha = 0.65f), maxLines = 1)
            Spacer(Modifier.height(14.dp))
            ContinueListeningActions(
                backdrop = backdrop,
                isPlaying = isPlaying,
                contentColor = contentColor,
                onPlayPause = onPlay,
                onShuffle = onShuffle,
                enabled = entries.isNotEmpty(),
                isLandscape = isLandscape,
            )
        }
    }
    val icon: @Composable () -> Unit = {
        Box(
            Modifier.size(landscapeLayout?.coverSize?.coerceAtLeast(120.dp) ?: 132.dp)
                .clip(RoundedCornerShape(26.dp))
                .clickable(enabled = entries.isNotEmpty(), onClick = onPlay),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier.fillMaxSize().background(
                    Brush.linearGradient(listOf(Color(0xFF765BFF), Color(0xFFB24FE3), Color(0xFFFF5BAE))),
                ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(SimpIcons.GraphicEq, contentDescription = stringResource(Res.string.continue_listening), tint = Color.White, modifier = Modifier.size(62.dp))
            }
        }
    }
    if (isLandscape) {
        val layout = landscapeLayout!!
        Row(
            Modifier.fillMaxWidth()
                .padding(horizontal = layout.horizontalPadding)
                .padding(top = 4.dp, bottom = 8.dp),
            verticalAlignment = Alignment.Top,
        ) {
            icon()
            Spacer(Modifier.width(layout.columnSpacing))
            Metadata(Modifier.weight(1f), Alignment.Start)
        }
    } else {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            icon()
            Spacer(Modifier.height(18.dp))
            Metadata(Modifier.fillMaxWidth(), Alignment.CenterHorizontally)
        }
    }
}

@Composable
private fun ContinueListeningActions(
    backdrop: PlatformBackdrop,
    isPlaying: Boolean,
    contentColor: Color,
    onPlayPause: () -> Unit,
    onShuffle: () -> Unit,
    enabled: Boolean,
    isLandscape: Boolean,
) {
    Row(
        Modifier.widthIn(max = if (isLandscape) LandscapeHeroActionMaxWidth else 380.dp).fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ContinueListeningSideAction(backdrop, onClick = onShuffle, enabled = enabled) {
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
private fun ContinueListeningSideAction(
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

private fun String?.parseResumePoints(): Map<String, EpisodeResumePoint> =
    this.orEmpty().split(';').mapNotNull { row ->
        val parts = row.split('|')
        if (parts.size != 4) return@mapNotNull null
        val position = parts[1].toLongOrNull() ?: return@mapNotNull null
        val duration = parts[2].toLongOrNull() ?: return@mapNotNull null
        val updated = parts[3].toLongOrNull() ?: return@mapNotNull null
        parts[0] to EpisodeResumePoint(position, duration, updated)
    }.toMap()

private fun EpisodeEntity.toPodcastBrowseItem() =
    PodcastBrowse.EpisodeItem(
        title = title,
        author = com.maxrave.domain.data.model.searchResult.songs.Artist(id = authorId, name = authorName),
        description = description,
        thumbnail = listOfNotNull(thumbnail?.let { com.maxrave.domain.data.model.searchResult.songs.Thumbnail(url = it, width = 0, height = 0) }),
        createdDay = createdDay,
        durationString = durationString,
        videoId = videoId,
    )

private fun formatRemaining(milliseconds: Long): String {
    val minutes = milliseconds.coerceAtLeast(0L) / 60_000L
    val hours = minutes / 60L
    return if (hours > 0) "${hours}h ${minutes % 60}m" else "${minutes} min"
}
