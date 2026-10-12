package com.petedev.wavemusic.ui.screen.home

import com.petedev.wavemusic.ui.component.LiquidGlassHeader
import com.petedev.wavemusic.ui.component.frostedHeaderSource
import com.petedev.wavemusic.expect.ui.rememberBackdrop
import androidx.compose.foundation.layout.fillMaxWidth

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.petedev.wavemusic.ui.component.PageTopAppBar
import com.petedev.wavemusic.ui.component.pageHeaderContentHeight
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.petedev.wavemusic.common.Config
import com.petedev.wavemusic.domain.data.entities.AlbumEntity
import com.petedev.wavemusic.domain.data.entities.ArtistEntity
import com.petedev.wavemusic.domain.data.entities.PlaylistEntity
import com.petedev.wavemusic.domain.data.entities.SongEntity
import com.petedev.wavemusic.domain.mediaservice.handler.PlaylistType
import com.petedev.wavemusic.domain.mediaservice.handler.QueueData
import com.petedev.wavemusic.domain.utils.toTrack
import com.petedev.wavemusic.ui.component.AddToPlaylistModalBottomSheet
import com.petedev.wavemusic.ui.component.ArtistFullWidthItems
import com.petedev.wavemusic.ui.component.CenterLoadingBox
import com.petedev.wavemusic.ui.component.EndOfPage
import com.petedev.wavemusic.ui.component.PlaylistFullWidthItems
import com.petedev.wavemusic.ui.component.RippleIconButton
import com.petedev.wavemusic.ui.component.SongFullWidthItems
import com.petedev.wavemusic.ui.component.selection.SelectedSongsBottomSheet
import com.petedev.wavemusic.ui.component.selection.SongSelectionTopAppBar
import com.petedev.wavemusic.ui.component.selection.rememberSongSelectionState
import com.petedev.wavemusic.ui.icon.ArrowBackIosNew
import com.petedev.wavemusic.ui.icon.WaveIcons
import com.petedev.wavemusic.ui.navigation.destination.list.AlbumDestination
import com.petedev.wavemusic.ui.navigation.destination.list.ArtistDestination
import com.petedev.wavemusic.ui.navigation.destination.list.PlaylistDestination
import com.petedev.wavemusic.ui.theme.typo
import com.petedev.wavemusic.viewModel.RecentlySongsViewModel
import com.petedev.wavemusic.viewModel.SharedViewModel
import com.petedev.wavemusic.viewModel.SongSelectionViewModel
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.flow.map
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import com.petedev.wavemusic.resources.Res
import com.petedev.wavemusic.resources.error
import com.petedev.wavemusic.resources.recently_added

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentlySongsScreen(
    innerPadding: PaddingValues,
    navController: NavController,
    viewModel: RecentlySongsViewModel = koinViewModel(),
    sharedViewModel: SharedViewModel = koinInject(),
) {
    val hazeState = rememberHazeState()
    val frostedBackdrop = rememberBackdrop(MaterialTheme.colorScheme.background)

    val selectionState = rememberSongSelectionState()
    val selectionViewModel: SongSelectionViewModel = koinViewModel()
    var showSelectionSheet by rememberSaveable { mutableStateOf(false) }
    var showSelectionAddToPlaylist by rememberSaveable { mutableStateOf(false) }

    val recentlyItems = viewModel.recentlySongs.collectAsLazyPagingItems()
    val playingTrack by sharedViewModel.nowPlayingState.map { it?.songEntity }.collectAsState(initial = null)
    val isPlaying by sharedViewModel.controllerState.map { it.isPlaying }.collectAsState(initial = false)

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .frostedHeaderSource(frostedBackdrop, auroraBackground = MaterialTheme.colorScheme.background)
                    .hazeSource(state = hazeState),
        ) {
            item {
                Spacer(
                    Modifier.size(
                        innerPadding.calculateTopPadding() + pageHeaderContentHeight(),
                    ),
                )
            }

            items(
                count = recentlyItems.itemCount,
                key = { index ->
                    val item = recentlyItems[index]
                    when (item) {
                        is SongEntity -> "song_${item.videoId}_$index"
                        is AlbumEntity -> "album_${item.browseId}_$index"
                        is PlaylistEntity -> "playlist_${item.id}_$index"
                        is ArtistEntity -> "artist_${item.channelId}_$index"
                        else -> index.toString()
                    }
                },
            ) { index ->
                val item = recentlyItems[index]
                when (item) {
                    is SongEntity -> {
                        val recentlyAddedString = stringResource(Res.string.recently_added)
                        SongFullWidthItems(
                            songEntity = item,
                            isPlaying = playingTrack?.videoId == item.videoId && isPlaying,
                            onClickListener = { videoId ->
                                val firstQueue = item.toTrack()
                                viewModel.setQueueData(
                                    QueueData.Data(
                                        listTracks = arrayListOf(firstQueue),
                                        firstPlayedTrack = firstQueue,
                                        playlistId = "RDAMVM$videoId",
                                        playlistName = recentlyAddedString,
                                        playlistType = PlaylistType.RADIO,
                                        continuation = null,
                                    ),
                                )
                                viewModel.loadMediaItem(
                                    firstQueue,
                                    Config.SONG_CLICK,
                                    0,
                                )
                            },
                            onAddToQueue = {
                                sharedViewModel.addListToQueue(
                                    arrayListOf(item.toTrack()),
                                )
                            },
                            selectionMode = selectionState.isActive,
                            isSelected = selectionState.isSelected(item.videoId),
                            onLongClick = { selectionState.start(it) },
                            onSelectToggle = { selectionState.toggle(it) },
                            modifier = Modifier.animateItem(),
                        )
                    }

                    is AlbumEntity -> {
                        PlaylistFullWidthItems(
                            data = item,
                            onClickListener = {
                                navController.navigate(
                                    AlbumDestination(
                                        item.browseId,
                                    ),
                                )
                            },
                            modifier = Modifier.animateItem(),
                        )
                    }

                    is PlaylistEntity -> {
                        PlaylistFullWidthItems(
                            data = item,
                            onClickListener = {
                                navController.navigate(
                                    PlaylistDestination(
                                        item.id,
                                    ),
                                )
                            },
                            modifier = Modifier.animateItem(),
                        )
                    }

                    is ArtistEntity -> {
                        ArtistFullWidthItems(
                            data = item,
                            onClickListener = {
                                navController.navigate(
                                    ArtistDestination(
                                        item.channelId,
                                    ),
                                )
                            },
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
            }

            // Loading state
            recentlyItems.apply {
                when {
                    loadState.refresh is LoadState.Loading || loadState.append is LoadState.Loading -> {
                        item {
                            CenterLoadingBox(
                                modifier =
                                    Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                            )
                        }
                    }

                    loadState.refresh is LoadState.Error || loadState.append is LoadState.Error -> {
                        item {
                            val error =
                                (loadState.refresh as? LoadState.Error)?.error?.message
                                    ?: (loadState.append as? LoadState.Error)?.error?.message
                                    ?: stringResource(Res.string.error)
                            LaunchedEffect(error) {
                                viewModel.makeToast(error)
                            }
                        }
                    }
                }
            }

            item {
                EndOfPage()
            }
        }

        LiquidGlassHeader(
            hazeState = hazeState,
            backdrop = frostedBackdrop,
            shareHeader = true,
            modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth(),
        ) {
        PageTopAppBar(
            modifier =
                Modifier
                    .align(Alignment.TopCenter),
            title = {
                Text(
                    text = stringResource(Res.string.recently_added),
                    style = typo().titleMedium,
                )
            },
            navigationIcon = {
                Box(Modifier.padding(horizontal = 5.dp)) {
                    RippleIconButton(
                        WaveIcons.ArrowBackIosNew,
                        Modifier.size(com.petedev.wavemusic.ui.component.landscapeChromeSize(32.dp)),
                        true,
                        tint = MaterialTheme.colorScheme.onSurface,
                    ) {
                        navController.navigateUp()
                    }
                }
            },
            actions = {},
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                ),
        )
        if (selectionState.isActive) {
            SongSelectionTopAppBar(
                state = selectionState,
                onSelectAll = {
                    selectionState.toggleSelectAll(
                        (0 until recentlyItems.itemCount).mapNotNull {
                            (recentlyItems.peek(it) as? SongEntity)?.videoId
                        },
                    )
                },
                onOpenActions = { showSelectionSheet = true },
                modifier = Modifier.align(Alignment.TopCenter),
            )
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
        val availableSongs = (recentlyItems.itemSnapshotList.items.filterIsInstance<SongEntity>()).filter { it.videoId in selectedIds }
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
