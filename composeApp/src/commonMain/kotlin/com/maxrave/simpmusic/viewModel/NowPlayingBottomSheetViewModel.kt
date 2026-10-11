package com.maxrave.simpmusic.viewModel

import com.maxrave.domain.mediaservice.handler.QueueMediaType
import androidx.lifecycle.viewModelScope
import com.maxrave.common.Config
import com.maxrave.domain.data.entities.DownloadState
import com.maxrave.domain.data.entities.LocalPlaylistEntity
import com.maxrave.domain.data.entities.SongEntity
import com.maxrave.domain.data.model.searchResult.playlists.PlaylistsResult
import com.maxrave.domain.data.model.searchResult.songs.Album
import com.maxrave.domain.data.model.searchResult.songs.Artist
import com.maxrave.domain.data.model.streams.YouTubeWatchEndpoint
import com.maxrave.domain.manager.DataStoreManager
import com.maxrave.domain.manager.DataStoreManager.Values.BETTER_LYRICS
import com.maxrave.domain.manager.DataStoreManager.Values.BETTER_LYRICS_PORTATO
import com.maxrave.domain.manager.DataStoreManager.Values.KUGOU
import com.maxrave.domain.manager.DataStoreManager.Values.LRCLIB
import com.maxrave.domain.manager.DataStoreManager.Values.SIMPMUSIC
import com.maxrave.domain.manager.DataStoreManager.Values.UNISON
import com.maxrave.domain.manager.DataStoreManager.Values.YOULYPLUS
import com.maxrave.domain.manager.DataStoreManager.Values.YOUTUBE
import com.maxrave.domain.mediaservice.handler.DownloadHandler
import com.maxrave.domain.mediaservice.handler.PlaylistType
import com.maxrave.domain.mediaservice.handler.QueueData
import com.maxrave.domain.mediaservice.handler.SleepTimerState
import com.maxrave.domain.extension.isVideo
import com.maxrave.domain.extension.toGenericMediaItem
import com.maxrave.domain.repository.LocalPlaylistRepository
import com.maxrave.domain.repository.PlaylistRepository
import com.maxrave.domain.repository.SongRepository
import com.maxrave.domain.utils.Resource
import com.maxrave.domain.utils.collectLatestResource
import com.maxrave.domain.utils.collectResource
import com.maxrave.domain.utils.toTrack
import com.maxrave.logger.LogLevel
import com.maxrave.simpmusic.expect.shareUrl
import com.maxrave.simpmusic.viewModel.base.BaseViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.lastOrNull
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.component.inject
import simpmusic.composeapp.generated.resources.Res
import simpmusic.composeapp.generated.resources.already_in_playlist
import simpmusic.composeapp.generated.resources.added_to_queue
import simpmusic.composeapp.generated.resources.delete_song_from_playlist
import simpmusic.composeapp.generated.resources.downloading
import simpmusic.composeapp.generated.resources.error
import simpmusic.composeapp.generated.resources.error_occurred
import simpmusic.composeapp.generated.resources.play_next
import simpmusic.composeapp.generated.resources.removed_download
import simpmusic.composeapp.generated.resources.removed_from_YouTube_playlist
import simpmusic.composeapp.generated.resources.share_url
import simpmusic.composeapp.generated.resources.sleep_timer_off_done
import simpmusic.composeapp.generated.resources.synced

class NowPlayingBottomSheetViewModel(
    private val dataStoreManager: DataStoreManager,
    private val localPlaylistRepository: LocalPlaylistRepository,
    private val playlistRepository: PlaylistRepository,
    private val songRepository: SongRepository,
) : BaseViewModel() {
    private val downloadUtils: DownloadHandler by inject()
    private val _uiState: MutableStateFlow<NowPlayingBottomSheetUIState> =
        MutableStateFlow(
            NowPlayingBottomSheetUIState(
                listLocalPlaylist = emptyList(),
                listYouTubePlaylist = emptyList(),
                mainLyricsProvider = SIMPMUSIC,
                sleepTimer =
                    SleepTimerState(
                        false,
                        0,
                    ),
            ),
        )
    val uiState: StateFlow<NowPlayingBottomSheetUIState> get() = _uiState.asStateFlow()

    private var getSongAsFlow: Job? = null
    private var songLyricsProviderJob: Job? = null
    private val lyricsOffsetEditMutex = Mutex()

    init {
        viewModelScope.launch {
            val sleepTimerJob =
                launch {
                    mediaPlayerHandler.sleepTimerState.collectLatest { sl ->
                        _uiState.update { it.copy(sleepTimer = sl) }
                    }
                }
            val listLocalPlaylistJob =
                launch {
                    localPlaylistRepository.getAllLocalPlaylists().collectLatest { list ->
                        _uiState.update {
                            it.copy(
                                listLocalPlaylist =
                                    list.filter {
                                        it.youtubePlaylistId.isNullOrBlank() &&
                                            it.syncState != LocalPlaylistEntity.YouTubeSyncState.Synced
                                    },
                            )
                        }
                    }
                }
            val listYouTubePlaylistJob =
                launch { refreshYouTubePlaylistList() }
            sleepTimerJob.join()
            listLocalPlaylistJob.join()
            listYouTubePlaylistJob.join()
        }
    }

    fun resetPlaylists() {
        viewModelScope.launch {
            localPlaylistRepository.getAllLocalPlaylists().collectLatest { list ->
                _uiState.update {
                    it.copy(
                        listLocalPlaylist =
                            list.filter {
                                it.youtubePlaylistId.isNullOrBlank() &&
                                    it.syncState != LocalPlaylistEntity.YouTubeSyncState.Synced
                            },
                    )
                }
            }
        }
        viewModelScope.launch { refreshYouTubePlaylistList() }
    }

    private suspend fun refreshYouTubePlaylistList() {
        playlistRepository.getLibraryPlaylist().collect { data ->
            _uiState.update { state ->
                state.copy(
                    listYouTubePlaylist =
                        data?.filter {
                            it.browseId != "VLLM"
                        } ?: emptyList(),
                )
            }
        }
    }

    fun setSongEntity(songEntity: SongEntity?) {
        val songOrNowPlaying = songEntity ?: (mediaPlayerHandler.nowPlayingState.value.songEntity ?: return)
        viewModelScope.launch {
            songOrNowPlaying.videoId.let {
                _uiState.update { state ->
                    state.copy(
                        songUIState =
                            state.songUIState.copy(
                                isAddedToYouTubeLiked = false,
                            ),
                    )
                }
                songRepository.getSongById(it).lastOrNull().let { song ->
                    if (song != null) {
                        getSongEntityFlow(videoId = song.videoId)
                    } else {
                        songRepository.insertSong(songOrNowPlaying).singleOrNull()?.let {
                            getSongEntityFlow(videoId = songOrNowPlaying.videoId)
                        }
                    }
                }
            }
        }
    }

    private fun getSongEntityFlow(videoId: String) {
        getSongAsFlow?.cancel()
        songLyricsProviderJob?.cancel()
        if (videoId.isEmpty()) return
        _uiState.update { it.copy(mainLyricsProvider = SIMPMUSIC) }
        songLyricsProviderJob =
            viewModelScope.launch {
                dataStoreManager.getString(SONG_LYRICS_PROVIDER_KEY_PREFIX + videoId).collectLatest { provider ->
                    val selectedProvider = provider ?: SIMPMUSIC
                    if (selectedProvider in listOf(SIMPMUSIC, YOUTUBE, LRCLIB, BETTER_LYRICS, BETTER_LYRICS_PORTATO, YOULYPLUS, KUGOU, UNISON)) {
                        _uiState.update { it.copy(mainLyricsProvider = selectedProvider) }
                    }
                }
            }
        getSongAsFlow =
            viewModelScope.launch {
                songRepository.getSongAsFlow(videoId).collectLatest { song ->
                    log("getSongEntityFlow: $song", LogLevel.WARN)
                    if (song != null) {
                        val activePlaybackVideo =
                            mediaPlayerHandler.nowPlayingState.value
                                .takeIf { it.songEntity?.videoId == song.videoId }
                                ?.mediaItem
                                ?.isVideo()
                        val isVideo = activePlaybackVideo ?: song.toTrack().toGenericMediaItem().isVideo()
                        _uiState.update { state ->
                            state.copy(
                                songUIState =
                                    NowPlayingBottomSheetUIState.SongUIState(
                                        videoId = song.videoId,
                                        title = song.title,
                                        isVideo = isVideo,
                                        listArtists =
                                            song.artistName?.mapIndexed { i, name ->
                                                Artist(name = name, id = song.artistId?.getOrNull(i) ?: "")
                                            } ?: emptyList(),
                                        thumbnails = song.thumbnails,
                                        liked = song.liked,
                                        downloadState = song.downloadState,
                                        album =
                                            song.albumName?.takeIf { it.isNotEmpty() }?.let { name ->
                                                Album(name = name, id = song.albumId ?: "")
                                            },
                                    ),
                            )
                        }
                    }
                }
            }
    }

    /** Both player entry points offer creation for any loaded audio or video item. */
    fun playlistCreationAction(): ((String) -> Unit)? =
        if (uiState.value.songUIState.videoId.isBlank()) null else ::createYouTubeMusicPlaylistWithCurrentSong

    fun createYouTubeMusicPlaylistWithCurrentSong(title: String) {
        val trimmedTitle = title.trim()
        val songUIState = uiState.value.songUIState
        if (trimmedTitle.isEmpty() || songUIState.videoId.isBlank()) return

        viewModelScope.launch {
            var localPlaylistId: Long? = null
            try {
                val song = songRepository.getSongById(songUIState.videoId).singleOrNull()
                if (song == null) {
                    makeToast(getString(Res.string.error_occurred))
                    return@launch
                }
                val isYouTubeAuthenticated =
                    dataStoreManager.loggedIn.first() == DataStoreManager.TRUE &&
                        dataStoreManager.cookie.first().isNotBlank()
                val createdPlaylistId =
                    localPlaylistRepository.insertLocalPlaylistWithTracks(
                        localPlaylist =
                            LocalPlaylistEntity(
                                title = trimmedTitle,
                                tracks = listOf(song.videoId),
                                syncState = if (isYouTubeAuthenticated) {
                                    LocalPlaylistEntity.YouTubeSyncState.Syncing
                                } else {
                                    LocalPlaylistEntity.YouTubeSyncState.PendingSync
                                },
                            ),
                        videoIds = listOf(song.videoId),
                    )
                localPlaylistId = createdPlaylistId
                if (createdPlaylistId >= 0L) makeToast(playlistAdditionSuccessMessage(trimmedTitle))
                if (createdPlaylistId < 0L) {
                    makeToast(getString(Res.string.error_occurred))
                } else if (isYouTubeAuthenticated) {
                    var syncError: String? = null
                    var syncedToYouTube = false
                    localPlaylistRepository
                        .syncLocalPlaylistToYouTubePlaylist(
                            playlistId = createdPlaylistId,
                            successMessage = getString(Res.string.synced),
                            errorMessage = getString(Res.string.error),
                        ).collectLatestResource(
                            onSuccess = { syncedToYouTube = true },
                            onError = { message -> syncError = message },
                        )
                    syncError?.let { message ->
                        localPlaylistRepository.updateLocalPlaylistYouTubePlaylistSyncState(
                            createdPlaylistId,
                            LocalPlaylistEntity.YouTubeSyncState.PendingSync,
                        )
                        makeToast(message)
                    }
                    if (syncedToYouTube) refreshYouTubePlaylistList()
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                localPlaylistId?.takeIf { it >= 0L }?.let {
                    localPlaylistRepository.updateLocalPlaylistYouTubePlaylistSyncState(
                        it,
                        LocalPlaylistEntity.YouTubeSyncState.PendingSync,
                    )
                }
                makeToast(getString(Res.string.error_occurred))
            }
        }
    }

    fun onUIEvent(ev: NowPlayingBottomSheetUIEvent) {
        val songUIState = uiState.value.songUIState
        if (songUIState.videoId.isEmpty()) return
        if (songUIState.isVideo &&
            (ev is NowPlayingBottomSheetUIEvent.PlayNext ||
                ev is NowPlayingBottomSheetUIEvent.AddToQueue)
        ) {
            return
        }
        viewModelScope.launch {
            when (ev) {
                is NowPlayingBottomSheetUIEvent.DeleteFromPlaylist -> {
                    localPlaylistRepository
                        .removeTrackFromLocalPlaylist(
                            id = ev.playlistId,
                            song =
                                songRepository.getSongById(ev.videoId).lastOrNull() ?: run {
                                    makeToast(getString(Res.string.error_occurred))
                                    return@launch
                                },
                            successMessage = getString(Res.string.delete_song_from_playlist),
                            updatedYtMessage = getString(Res.string.removed_from_YouTube_playlist),
                            errorMessage = getString(Res.string.error_occurred),
                        ).collectResource(
                            onSuccess = {
                                makeToast(it ?: getString(Res.string.delete_song_from_playlist))
                            },
                            onError = {
                                makeToast(it)
                            },
                        )
                }

                is NowPlayingBottomSheetUIEvent.AddToYouTubePlaylist -> {
                    localPlaylistRepository
                        .addYouTubePlaylistItem(
                            youtubePlaylistId = ev.browseId,
                            videoId = songUIState.videoId,
                        ).collectLatestResource(
                            // Keep backend status tokens out of user-facing confirmations.
                            onSuccess = { makeToast(playlistAdditionSuccessMessage(ev.playlistName)) },
                            onError = {
                                makeToast(playlistAdditionErrorMessage(it))
                            },
                        )
                }

                is NowPlayingBottomSheetUIEvent.SetLiked -> {
                    songRepository.updateLikeStatus(
                        songUIState.videoId,
                        if (ev.liked) 1 else 0,
                    )
                }

                is NowPlayingBottomSheetUIEvent.Download -> {
                    when (songUIState.downloadState) {
                        DownloadState.STATE_NOT_DOWNLOADED -> {
                            songRepository.updateDownloadState(
                                videoId = songUIState.videoId,
                                downloadState = DownloadState.STATE_PREPARING,
                            )
                            downloadUtils.downloadTrack(
                                videoId = songUIState.videoId,
                                title = songUIState.title,
                                thumbnail = songUIState.thumbnails ?: "",
                            )
                            makeToast(getString(Res.string.downloading))
                        }

                        DownloadState.STATE_PREPARING, DownloadState.STATE_DOWNLOADING -> {
                            downloadUtils.removeDownload(songUIState.videoId)
                            songRepository.updateDownloadState(
                                songUIState.videoId,
                                DownloadState.STATE_NOT_DOWNLOADED,
                            )
                            makeToast(getString(Res.string.removed_download))
                        }

                        DownloadState.STATE_DOWNLOADED -> {
                            downloadUtils.removeDownload(songUIState.videoId)
                            songRepository.updateDownloadState(
                                songUIState.videoId,
                                DownloadState.STATE_NOT_DOWNLOADED,
                            )
                            makeToast(getString(Res.string.removed_download))
                        }
                    }
                }

                is NowPlayingBottomSheetUIEvent.AddToPlaylist -> {
                    val targetPlaylist = uiState.value.listLocalPlaylist.find { it.id == ev.playlistId } ?: return@launch
                    val newList = (targetPlaylist.tracks ?: emptyList<String>()).toMutableList()
                    if (newList.contains(songUIState.videoId)) {
                        makeToast(getString(Res.string.already_in_playlist))
                        return@launch
                    } else {
                        val songEntity = songRepository.getSongById(songUIState.videoId).singleOrNull() ?: return@launch
                        val addedMessage = playlistAdditionSuccessMessage(targetPlaylist.title)
                        var successShown = false
                        localPlaylistRepository
                            .addTrackToLocalPlaylist(
                                id = ev.playlistId,
                                song = songEntity,
                                successMessage = addedMessage,
                                updatedYtMessage = addedMessage,
                                errorMessage = getString(Res.string.error),
                            ).collectLatestResource(
                                onSuccess = {
                                    // Linked playlists can emit success again when syncing.
                                    if (!successShown) {
                                        successShown = true
                                        makeToast(addedMessage)
                                    }
                                },
                                onError = {
                                    makeToast(playlistAdditionErrorMessage(it))
                                },
                            )
                    }
                }

                is NowPlayingBottomSheetUIEvent.PlayNext -> {
                    val songEntity = songRepository.getSongById(songUIState.videoId).singleOrNull() ?: return@launch
                    mediaPlayerHandler.playNext(songEntity.toTrack())
                    makeToast(getString(Res.string.play_next))
                }

                is NowPlayingBottomSheetUIEvent.AddToQueue -> {
                    val songEntity = songRepository.getSongById(songUIState.videoId).singleOrNull() ?: return@launch
                    mediaPlayerHandler.loadMoreCatalog(arrayListOf(songEntity.toTrack()), isAddToQueue = true)
                    makeToast(getString(Res.string.added_to_queue))
                }

                is NowPlayingBottomSheetUIEvent.ChangeLyricsProvider -> {
                    if (listOf(SIMPMUSIC, YOUTUBE, LRCLIB, BETTER_LYRICS, BETTER_LYRICS_PORTATO, YOULYPLUS, KUGOU, UNISON).contains(ev.lyricsProvider)) {
                        dataStoreManager.putString(
                            SONG_LYRICS_PROVIDER_KEY_PREFIX + songUIState.videoId,
                            ev.lyricsProvider,
                        )
                        _uiState.update { it.copy(mainLyricsProvider = ev.lyricsProvider) }
                    } else {
                        return@launch
                    }
                }

                is NowPlayingBottomSheetUIEvent.AdjustLyricsOffset -> {
                    lyricsOffsetEditMutex.withLock { dataStoreManager.adjustSongLyricsOffsetMs(songUIState.videoId, ev.deltaMs) }
                }

                is NowPlayingBottomSheetUIEvent.ResetLyricsOffset -> {
                    lyricsOffsetEditMutex.withLock { dataStoreManager.clearSongLyricsOffsetMs(songUIState.videoId) }
                }

                is NowPlayingBottomSheetUIEvent.SetSleepTimer -> {
                    if (ev.cancel) {
                        mediaPlayerHandler.sleepStop()
                        makeToast(getString(Res.string.sleep_timer_off_done))
                    } else if (ev.minutes > 0) {
                        mediaPlayerHandler.sleepStart(ev.minutes)
                    }
                }

                is NowPlayingBottomSheetUIEvent.ChangePlaybackSpeedPitch -> {
                    dataStoreManager.setPlaybackSpeed(ev.speed)
                    dataStoreManager.setPitch(ev.pitch)
                }

                is NowPlayingBottomSheetUIEvent.Share -> {
                    val url = "https://music.youtube.com/watch?v=${songUIState.videoId}"
                    shareUrl(
                        title = getString(Res.string.share_url),
                        url,
                    )
                }

                is NowPlayingBottomSheetUIEvent.StartRadio -> {
                    val seed = songRepository.getSongById(ev.videoId).singleOrNull()?.toTrack()
                    val mediaType = if (ev.videoId == songUIState.videoId) {
                        if (songUIState.isVideo) QueueMediaType.VIDEO else QueueMediaType.SONG
                    } else {
                        seed?.let(QueueMediaType::fromTrack) ?: QueueMediaType.SONG
                    }
                    songRepository
                        .getRadioFromEndpoint(
                            YouTubeWatchEndpoint(
                                videoId = ev.videoId,
                                playlistId = "RDAMVM${ev.videoId}",
                            ),
                            mediaType = mediaType,
                        ).collectLatest { res ->
                            val data = res.data
                            when (res) {
                                is Resource.Success if (data != null && data.first.isNotEmpty()) -> {
                                    setQueueData(
                                        QueueData.Data(
                                            listTracks = data.first,
                                            firstPlayedTrack = data.first.first(),
                                            playlistId = "RDAMVM${ev.videoId}",
                                            playlistName = ev.name,
                                            playlistType = PlaylistType.RADIO,
                                            continuation = data.second,
                                        ),
                                    )
                                    loadMediaItem(
                                        data.first.first(),
                                        Config.PLAYLIST_CLICK,
                                        0,
                                    )
                                }

                                else -> {
                                    makeToast(res.message ?: getString(Res.string.error))
                                }
                            }
                        }
                }
            }
        }
    }
}

data class NowPlayingBottomSheetUIState(
    val songUIState: SongUIState = SongUIState(),
    val listLocalPlaylist: List<LocalPlaylistEntity>,
    val listYouTubePlaylist: List<PlaylistsResult>,
    val mainLyricsProvider: String,
    val sleepTimer: SleepTimerState,
) {
    data class SongUIState(
        val videoId: String = "",
        val title: String = "",
        val isVideo: Boolean = false,
        val listArtists: List<Artist> = emptyList(),
        val thumbnails: String? = null,
        val liked: Boolean = false,
        val isAddedToYouTubeLiked: Boolean = false,
        val downloadState: Int = DownloadState.STATE_NOT_DOWNLOADED,
        val album: Album? = null,
    )
}

sealed class NowPlayingBottomSheetUIEvent {
    data class DeleteFromPlaylist(
        val videoId: String,
        val playlistId: Long,
    ) : NowPlayingBottomSheetUIEvent()

    data class SetLiked(
        val liked: Boolean,
    ) : NowPlayingBottomSheetUIEvent()

    data object Download : NowPlayingBottomSheetUIEvent()

    data class AddToPlaylist(
        val playlistId: Long,
    ) : NowPlayingBottomSheetUIEvent()

    data class AddToYouTubePlaylist(
        val browseId: String,
        val playlistName: String,
    ) : NowPlayingBottomSheetUIEvent()

    data object PlayNext : NowPlayingBottomSheetUIEvent()

    data object AddToQueue : NowPlayingBottomSheetUIEvent()

    data class ChangeLyricsProvider(
        val lyricsProvider: String,
    ) : NowPlayingBottomSheetUIEvent()

    data class AdjustLyricsOffset(
        val deltaMs: Int,
    ) : NowPlayingBottomSheetUIEvent()

    data object ResetLyricsOffset : NowPlayingBottomSheetUIEvent()

    data class SetSleepTimer(
        val cancel: Boolean = false,
        val minutes: Int = 0,
    ) : NowPlayingBottomSheetUIEvent()

    data class ChangePlaybackSpeedPitch(
        val speed: Float,
        val pitch: Int,
    ) : NowPlayingBottomSheetUIEvent()

    data class StartRadio(
        val videoId: String,
        val name: String,
    ) : NowPlayingBottomSheetUIEvent()

    data object Share : NowPlayingBottomSheetUIEvent()
}
