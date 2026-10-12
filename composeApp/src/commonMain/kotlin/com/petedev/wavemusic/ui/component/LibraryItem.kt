package com.petedev.wavemusic.ui.component

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.petedev.wavemusic.common.Config
import com.petedev.wavemusic.domain.data.entities.AlbumEntity
import com.petedev.wavemusic.domain.data.entities.ArtistEntity
import com.petedev.wavemusic.domain.data.entities.LocalPlaylistEntity
import com.petedev.wavemusic.domain.data.entities.PlaylistEntity
import com.petedev.wavemusic.domain.data.entities.PodcastsEntity
import com.petedev.wavemusic.domain.data.entities.SongEntity
import com.petedev.wavemusic.domain.data.model.browse.album.Track
import com.petedev.wavemusic.domain.data.model.searchResult.playlists.PlaylistsResult
import com.petedev.wavemusic.domain.data.type.LibraryType
import com.petedev.wavemusic.domain.data.type.PlaylistType
import com.petedev.wavemusic.domain.data.type.RecentlyType
import com.petedev.wavemusic.domain.mediaservice.handler.QueueData
import com.petedev.wavemusic.domain.utils.connectArtists
import com.petedev.wavemusic.domain.utils.toTrack
import com.petedev.wavemusic.ui.component.selection.SongSelectionState
import com.petedev.wavemusic.ui.navigation.destination.list.AlbumDestination
import com.petedev.wavemusic.ui.navigation.destination.list.ArtistDestination
import com.petedev.wavemusic.ui.navigation.destination.list.LocalPlaylistDestination
import com.petedev.wavemusic.ui.navigation.destination.list.PlaylistDestination
import com.petedev.wavemusic.ui.navigation.destination.list.PodcastDestination
import com.petedev.wavemusic.ui.theme.typo
import com.petedev.wavemusic.viewModel.LibraryViewModel
import com.petedev.wavemusic.viewModel.SharedViewModel
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import com.petedev.wavemusic.resources.Res
import com.petedev.wavemusic.resources.most_played
import com.petedev.wavemusic.resources.no_favorite_playlists
import com.petedev.wavemusic.resources.no_playlists_downloaded
import com.petedev.wavemusic.resources.radio
import com.petedev.wavemusic.resources.library_history
import com.petedev.wavemusic.domain.mediaservice.handler.PlaylistType as DomainPlaylistType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryItem(
    state: LibraryItemState,
    viewModel: LibraryViewModel = koinViewModel(),
    sharedViewModel: SharedViewModel = koinInject(),
    navController: NavController,
    // Null means this row does not take part in multi-selection; the selection bar lives on the
    // hosting screen, so only a screen that shows one passes a state down.
    selectionState: SongSelectionState? = null,
) {
    var showBottomSheet by remember { mutableStateOf(false) }
    var songEntity by remember { mutableStateOf<SongEntity?>(null) }
    val title =
        when (state.type) {
            is LibraryItemType.RecentlyAdded -> stringResource(Res.string.library_history)
            is LibraryItemType.CanvasSong -> stringResource(Res.string.most_played)
            else -> return
        }
    val noPlaylistTitle =
        when (state.type) {
            LibraryItemType.DownloadedPlaylist -> stringResource(Res.string.no_playlists_downloaded)
            LibraryItemType.FavoritePlaylist -> stringResource(Res.string.no_favorite_playlists)
            is LibraryItemType.RecentlyAdded -> stringResource(Res.string.library_history)
            is LibraryItemType.CanvasSong -> stringResource(Res.string.most_played)
            else -> return
        }
    Box {
        if (showBottomSheet) {
            NowPlayingBottomSheet(
                onDismiss = {
                    showBottomSheet = false
                    songEntity = null
                },
                navController = navController,
                song = songEntity ?: return,
                onLibraryDelete = {
                    songEntity?.videoId?.let { viewModel.deleteSong(it) }
                },
            )
        }
        Column {
            Row(
                modifier =
                    Modifier.padding(
                        top = 15.dp,
                        start = AppContentHorizontalInset,
                        end = AppContentHorizontalInset,
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    style = typo().headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(35.dp)
                            .wrapContentHeight(align = Alignment.CenterVertically)
                            .weight(1f)
                            .basicMarquee(
                                iterations = Int.MAX_VALUE,
                                animationMode = MarqueeAnimationMode.Immediately,
                            ).focusable(),
                )
            }
            Crossfade(targetState = state.isLoading, label = "Loading") { isLoading ->
                if (!isLoading) {
                    if (state.type is LibraryItemType.RecentlyAdded) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            state.data.filterIsInstance<RecentlyType>().forEach { item ->
                                when (item.objectType()) {
                                    RecentlyType.Type.SONG -> {
                                        SongFullWidthItems(
                                            songEntity = item as SongEntity,
                                            isPlaying = item.videoId == state.type.playingVideoId,
                                            modifier = Modifier,
                                            onMoreClickListener = {
                                                songEntity = item
                                                showBottomSheet = true
                                            },
                                            onClickListener = {
                                                viewModel.setQueueData(
                                                    QueueData.Data(
                                                        listTracks = arrayListOf(item.toTrack()),
                                                        firstPlayedTrack = item.toTrack(),
                                                        playlistId = "RDAMVM${item.videoId}",
                                                        playlistName = item.title,
                                                        playlistType = DomainPlaylistType.RADIO,
                                                        continuation = null,
                                                    ),
                                                )
                                                viewModel.loadMediaItem(
                                                    item,
                                                    type = Config.SONG_CLICK,
                                                    index = 0,
                                                )
                                            },
                                            selectionMode = selectionState?.isActive == true,
                                            isSelected = selectionState?.isSelected(item.videoId) == true,
                                            onLongClick =
                                                selectionState?.let { sel -> { id: String -> sel.start(id) } },
                                            onSelectToggle =
                                                selectionState?.let { sel -> { id: String -> sel.toggle(id) } },
                                            onAddToQueue = { videoId ->
                                                sharedViewModel.addListToQueue(
                                                    arrayListOf(item.toTrack()),
                                                )
                                            },
                                        )
                                    }

                                    RecentlyType.Type.ARTIST -> {
                                        ArtistFullWidthItems(
                                            data = item as? ArtistEntity ?: return@forEach,
                                            onClickListener = {
                                                navController.navigate(
                                                    ArtistDestination(
                                                        channelId = item.channelId,
                                                    ),
                                                )
                                            },
                                        )
                                    }

                                    else -> {
                                        if (item is PlaylistType) {
                                            PlaylistFullWidthItems(
                                                data = item,
                                                showPodcastDescription = false,
                                                onClickListener = {
                                                    when (item) {
                                                        is AlbumEntity -> {
                                                            navController.navigate(
                                                                AlbumDestination(
                                                                    item.browseId,
                                                                ),
                                                            )
                                                        }

                                                        is PlaylistEntity -> {
                                                            navController.navigate(
                                                                PlaylistDestination(
                                                                    item.id,
                                                                ),
                                                            )
                                                        }

                                                        is PodcastsEntity -> {
                                                            navController.navigate(
                                                                PodcastDestination(
                                                                    podcastId = item.podcastId,
                                                                ),
                                                            )
                                                        }
                                                    }
                                                },
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else if (state.type is LibraryItemType.CanvasSong) {
                        LazyRow(
                            Modifier
                                .padding(top = 10.dp)
                                .clipToBounds(),
                        ) {
                            items(
                                items = state.data,
                                key = { item -> item.libraryItemKey() },
                                contentType = { "canvas-song" },
                            ) { item ->
                                val song = item as? SongEntity ?: return@items
                                Box(
                                    Modifier
                                        .padding(horizontal = 12.dp)
                                        .height(300.dp)
                                        .width(170.dp)
                                        .clickable {
                                            val firstQueue: Track = song.toTrack()
                                            viewModel.setQueueData(
                                                QueueData.Data(
                                                    listTracks = arrayListOf(firstQueue),
                                                    firstPlayedTrack = firstQueue,
                                                    playlistId = "RDAMVM${firstQueue.videoId}",
                                                    playlistName = "\"${song.title}\" ${runBlocking { getString(Res.string.radio) }}",
                                                    playlistType = DomainPlaylistType.RADIO,
                                                    continuation = null,
                                                ),
                                            )
                                            viewModel.loadMediaItem(
                                                firstQueue,
                                                type = Config.SONG_CLICK,
                                            )
                                        },
                                ) {
                                    AsyncImage(
                                        model =
                                            ImageRequest
                                                .Builder(LocalPlatformContext.current)
                                                .data(item.canvasThumbUrl)
                                                .diskCachePolicy(CachePolicy.ENABLED)
                                                .diskCacheKey(item.canvasThumbUrl)
                                                .crossfade(true)
                                                .build(),
                                        placeholder = rememberHolderPainter(),
                                        error = rememberHolderPainter(),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier =
                                            Modifier
                                                .fillMaxSize()
                                                .clip(
                                                    RoundedCornerShape(8.dp),
                                                ),
                                    )
                                    Column(
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp)
                                            .align(Alignment.BottomStart),
                                    ) {
                                        Text(
                                            text = song.title,
                                            style = typo().labelSmall,
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier =
                                                Modifier
                                                    .fillMaxWidth()
                                                    .wrapContentHeight(
                                                        align = Alignment.CenterVertically,
                                                    ),
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            androidx.compose.animation.AnimatedVisibility(visible = song.isExplicit) {
                                                ExplicitBadge(
                                                    modifier =
                                                        Modifier
                                                            .size(20.dp)
                                                            .padding(end = 4.dp)
                                                            .weight(1f),
                                                )
                                            }
                                            Text(
                                                text = (song.artistName?.connectArtists() ?: ""),
                                                style = typo().bodySmall,
                                                maxLines = 1,
                                                modifier =
                                                    Modifier
                                                        .weight(1f)
                                                        .wrapContentHeight(
                                                            align = Alignment.CenterVertically,
                                                        ).basicMarquee(
                                                            iterations = Int.MAX_VALUE,
                                                            animationMode = MarqueeAnimationMode.Immediately,
                                                        ).focusable(),
                                            )
                                        }
                                        Spacer(Modifier.height(8.dp))
                                    }
                                }
                            }
                        }
                    } else {
                        if (state.data.isNotEmpty()) {
                            LazyRow {
                                items(
                                    items = state.data,
                                    key = { item -> item.libraryItemKey() },
                                    contentType = { item -> item.libraryItemContentType() },
                                ) { item ->
                                    Box(modifier = Modifier.animateItem()) {
                                        HomeItemContentPlaylist(
                                            onClick = {
                                                when (item) {
                                                    is LocalPlaylistEntity -> {
                                                        navController.navigate(
                                                            LocalPlaylistDestination(
                                                                item.id,
                                                            ),
                                                        )
                                                    }

                                                    is PlaylistsResult -> {
                                                        navController.navigate(
                                                            PlaylistDestination(
                                                                item.browseId,
                                                                isYourYouTubePlaylist = true,
                                                            ),
                                                        )
                                                    }

                                                    is AlbumEntity -> {
                                                        navController.navigate(
                                                            AlbumDestination(
                                                                item.browseId,
                                                            ),
                                                        )
                                                    }

                                                    is PlaylistEntity -> {
                                                        navController.navigate(
                                                            PlaylistDestination(
                                                                item.id,
                                                            ),
                                                        )
                                                    }

                                                    is PodcastsEntity -> {
                                                        navController.navigate(
                                                            PodcastDestination(
                                                                podcastId = item.podcastId,
                                                            ),
                                                        )
                                                    }
                                                }
                                            },
                                            data = item as? PlaylistType ?: return@items,
                                            thumbSize = 125.dp,
                                        )
                                    }
                                }
                            }
                        } else {
                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .height(130.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(noPlaylistTitle, style = typo().bodyMedium)
                            }
                        }
                    }
                } else {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(130.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CenterLoadingBox(Modifier.wrapContentSize())
                    }
                }
            }
        }
    }
}

/**
 * A single Recently Added row. Keeping these as direct children of the screen's LazyColumn avoids
 * eagerly composing every row (and starting every artwork request) inside one large list item.
 */
@Composable
fun LibraryRecentlyAddedRow(
    item: RecentlyType,
    playingVideoId: String?,
    navController: NavController,
    selectionState: SongSelectionState,
    onMoreClick: (SongEntity) -> Unit,
    viewModel: LibraryViewModel,
    sharedViewModel: SharedViewModel,
) {
    when (item.objectType()) {
        RecentlyType.Type.SONG -> {
            val song = item as SongEntity
            SongFullWidthItems(
                songEntity = song,
                isPlaying = song.videoId == playingVideoId,
                modifier = Modifier,
                onMoreClickListener = { onMoreClick(song) },
                onClickListener = {
                    val track = song.toTrack()
                    viewModel.setQueueData(
                        QueueData.Data(
                            listTracks = arrayListOf(track),
                            firstPlayedTrack = track,
                            playlistId = "RDAMVM${song.videoId}",
                            playlistName = song.title,
                            playlistType = DomainPlaylistType.RADIO,
                            continuation = null,
                        ),
                    )
                    viewModel.loadMediaItem(song, type = Config.SONG_CLICK, index = 0)
                },
                selectionMode = selectionState.isActive,
                isSelected = selectionState.isSelected(song.videoId),
                onLongClick = { id -> selectionState.start(id) },
                onSelectToggle = { id -> selectionState.toggle(id) },
                onAddToQueue = { sharedViewModel.addListToQueue(arrayListOf(song.toTrack())) },
                startContentPadding = AppContentHorizontalInset,
                leadingContentSpacer = 0.dp,
                endContentPadding = AppContentHorizontalInset,
            )
        }

        RecentlyType.Type.ARTIST -> {
            val artist = item as? ArtistEntity ?: return
            ArtistFullWidthItems(
                data = artist,
                onClickListener = {
                    navController.navigate(ArtistDestination(channelId = artist.channelId))
                },
                startContentPadding = AppContentHorizontalInset,
                leadingContentSpacer = 0.dp,
                endContentPadding = AppContentHorizontalInset,
            )
        }

        else -> {
            val playlist = item as? PlaylistType ?: return
            PlaylistFullWidthItems(
                data = playlist,
                showPodcastDescription = false,
                onClickListener = {
                    when (playlist) {
                        is AlbumEntity -> navController.navigate(AlbumDestination(playlist.browseId))
                        is PlaylistEntity -> navController.navigate(PlaylistDestination(playlist.id))
                        is PodcastsEntity -> navController.navigate(PodcastDestination(podcastId = playlist.podcastId))
                    }
                },
                startContentPadding = AppContentHorizontalInset,
                leadingContentSpacer = 0.dp,
                endContentPadding = AppContentHorizontalInset,
            )
        }
    }
}

sealed class LibraryItemType {
    data object CanvasSong : LibraryItemType()

    data class YouTubePlaylist(
        val isLoggedIn: Boolean,
        val onReload: () -> Unit = {},
    ) : LibraryItemType()

    data class LocalPlaylist(
        // Create new local playlist
        val onAddClick: (String) -> Unit,
    ) : LibraryItemType()

    data object FavoritePlaylist : LibraryItemType()

    data object DownloadedPlaylist : LibraryItemType()

    data object FavoritePodcasts : LibraryItemType()

    data class RecentlyAdded(
        val playingVideoId: String,
    ) : LibraryItemType()
}

data class LibraryItemState(
    val type: LibraryItemType,
    val data: List<LibraryType>,
    val isLoading: Boolean = true,
)

internal fun LibraryType.libraryItemKey(): String =
    when (this) {
        is SongEntity -> "song:$videoId"
        is ArtistEntity -> "artist:$channelId"
        is AlbumEntity -> "album:$browseId"
        is PlaylistEntity -> "youtube-playlist-entity:$id"
        is PodcastsEntity -> "podcast:$podcastId"
        is LocalPlaylistEntity -> "local-playlist:$id"
        is PlaylistsResult -> "youtube-playlist-result:$browseId"
        else -> "library-item:${hashCode()}"
    }

internal fun LibraryType.libraryItemContentType(): String =
    when (this) {
        is SongEntity -> "song"
        is ArtistEntity -> "artist"
        is AlbumEntity -> "album"
        is PlaylistEntity -> "youtube-playlist-entity"
        is PodcastsEntity -> "podcast"
        is LocalPlaylistEntity -> "local-playlist"
        is PlaylistsResult -> "youtube-playlist"
        else -> "library-item"
    }
