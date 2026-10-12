package com.petedev.wavemusic.viewModel

import androidx.lifecycle.viewModelScope
import com.petedev.wavemusic.domain.data.entities.DownloadState
import com.petedev.wavemusic.domain.data.entities.LocalPlaylistEntity
import com.petedev.wavemusic.domain.data.entities.SongEntity
import com.petedev.wavemusic.domain.manager.DataStoreManager
import com.petedev.wavemusic.domain.data.model.searchResult.playlists.PlaylistsResult
import com.petedev.wavemusic.domain.mediaservice.handler.DownloadHandler
import com.petedev.wavemusic.domain.repository.LocalPlaylistRepository
import com.petedev.wavemusic.domain.repository.PlaylistRepository
import com.petedev.wavemusic.domain.repository.SongRepository
import com.petedev.wavemusic.domain.utils.collectResource
import com.petedev.wavemusic.domain.utils.PLAYLIST_ITEM_ALREADY_PRESENT
import com.petedev.wavemusic.domain.utils.toTrack
import com.petedev.wavemusic.viewModel.base.BaseViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.Dispatchers
import org.koin.core.component.inject
import com.petedev.wavemusic.resources.Res
import com.petedev.wavemusic.resources.playlist
import com.petedev.wavemusic.resources.already_in_playlist
import com.petedev.wavemusic.resources.added_to_queue
import com.petedev.wavemusic.resources.delete_song_from_playlist
import com.petedev.wavemusic.resources.downloading
import com.petedev.wavemusic.resources.error
import com.petedev.wavemusic.resources.error_occurred
import com.petedev.wavemusic.resources.play_next
import com.petedev.wavemusic.resources.removed_from_YouTube_playlist
import com.petedev.wavemusic.resources.synced

/** Playlist writes must finish even when navigation clears the originating screen's ViewModel. */
private object SelectedPlaylistOperations {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
}

/**
 * Runs the bulk actions offered by
 * [com.petedev.wavemusic.ui.component.selection.SelectedSongsBottomSheet].
 *
 * Shared by every screen that offers multi-selection: the actions only ever need videoIds, so
 * they do not depend on which screen the selection was made on. The one exception is removing
 * from a local playlist, which takes the playlist id from the screen.
 */
class SongSelectionViewModel(
    private val songRepository: SongRepository,
    private val localPlaylistRepository: LocalPlaylistRepository,
) : BaseViewModel() {
    private val downloadUtils: DownloadHandler by inject()
    private val dataStoreManager: DataStoreManager by inject()
    private val playlistRepository: PlaylistRepository by inject()
    private val playlistListRefresh = MutableStateFlow(0L)
    private val _playlistListLoading = MutableStateFlow(true)
    val playlistListLoading: StateFlow<Boolean> = _playlistListLoading

    val listLocalPlaylist: StateFlow<List<LocalPlaylistEntity>> =
        localPlaylistRepository
            .getAllLocalPlaylists()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Same authenticated library used by the player picker, loaded only while this picker is open. */
    @OptIn(ExperimentalCoroutinesApi::class)
    val listYouTubePlaylist: StateFlow<List<PlaylistsResult>> =
        combine(dataStoreManager.loggedIn, dataStoreManager.cookie, playlistListRefresh) { loggedIn, cookie, _ ->
            loggedIn == DataStoreManager.TRUE && cookie.isNotBlank()
        }.flatMapLatest { authenticated ->
            if (!authenticated) {
                _playlistListLoading.value = false
                flowOf(emptyList())
            }
            else playlistRepository.getLibraryPlaylist()
                .map { playlists -> playlists.orEmpty().filterNot { it.browseId.removePrefix("VL") == "LM" } }
                .onStart { _playlistListLoading.value = true }
                .onEach { _playlistListLoading.value = false }
                .catch { error ->
                    if (error is CancellationException) throw error
                    log("Could not load YouTube playlists: ${error.message}")
                    emit(emptyList())
                }
                .onCompletion { _playlistListLoading.value = false }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000, replayExpirationMillis = 0), emptyList())

    /** Refresh on each opening so playlists created in another menu appear immediately. */
    fun refreshYouTubePlaylists() {
        _playlistListLoading.value = true
        playlistListRefresh.update { it + 1L }
    }

    /**
     * Each call inserts right after the current track, so playing them in order would leave the
     * queue reversed — the last one inserted ends up first. Walking the list backwards puts them
     * in the order the user saw them.
     */
    fun playNext(videoIds: List<String>) {
        viewModelScope.launch {
            val songs = songsOf(videoIds)
            if (songs.isEmpty()) {
                makeToast(getString(Res.string.error_occurred))
                return@launch
            }
            songs.asReversed().forEach { mediaPlayerHandler.playNext(it.toTrack()) }
            makeToast(getString(Res.string.play_next))
        }
    }

    fun addToQueue(videoIds: List<String>) {
        viewModelScope.launch {
            val songs = songsOf(videoIds)
            if (songs.isEmpty()) {
                makeToast(getString(Res.string.error_occurred))
                return@launch
            }
            mediaPlayerHandler.loadMoreCatalog(
                ArrayList(songs.map { it.toTrack() }),
                isAddToQueue = true,
            )
            makeToast(getString(Res.string.added_to_queue))
        }
    }

    /**
     * Only starts songs that are not already downloaded or in flight — unlike the single-song
     * menu, this is not a toggle: a selection of 25 will usually mix downloaded and not, and
     * toggling would silently delete the ones already on disk.
     */
    fun download(videoIds: List<String>) {
        viewModelScope.launch {
            val pending =
                songsOf(videoIds).filter {
                    it.downloadState == DownloadState.STATE_NOT_DOWNLOADED
                }
            if (pending.isEmpty()) return@launch
            pending.forEach { song ->
                songRepository.updateDownloadState(
                    videoId = song.videoId,
                    downloadState = DownloadState.STATE_PREPARING,
                )
                downloadUtils.downloadTrack(
                    videoId = song.videoId,
                    title = song.title,
                    thumbnail = song.thumbnails ?: "",
                )
            }
            makeToast(getString(Res.string.downloading))
        }
    }

    fun addToFavorite(videoIds: List<String>) {
        viewModelScope.launch {
            songsOf(videoIds)
                .filterNot { it.liked }
                .forEach { songRepository.updateLikeStatus(it.videoId, 1) }
        }
    }

    /** Remove every selected track from Favorites, including remote YouTube Music likes. */
    fun removeFromFavorite(videoIds: List<String>) {
        viewModelScope.launch {
            videoIds.distinct().forEach { songRepository.updateLikeStatus(it, 0) }
        }
    }

    fun addToPlaylist(
        playlistId: Long,
        videoIds: List<String>,
        availableSongs: List<SongEntity> = emptyList(),
    ) {
        val ids = videoIds.distinct()
        val snapshots = availableSongs.toList()
        launchPlaylistOperation {
            performLocalPlaylistAdd(playlistId, ids, snapshots)
        }
    }

    private suspend fun performLocalPlaylistAdd(playlistId: Long, ids: List<String>, availableSongs: List<SongEntity>) {
        val playlist = localPlaylistRepository.getAllLocalPlaylists().firstOrNull()?.find { it.id == playlistId }
            ?: error("Playlist no longer exists")
        val pendingIds = ids.filterNot { it in playlist.tracks.orEmpty() }
        if (pendingIds.isEmpty()) {
            makeToast(getString(Res.string.already_in_playlist))
            return
        }
        // Resolve the whole selection before any additions; never silently add only cached rows.
        val songs = resolvePlaylistSongs(pendingIds, availableSongs)
        val addedMessage = playlistAdditionSuccessMessage(playlist.title)
        val originalCookie = dataStoreManager.cookie.first()
        var added = 0
        var failures = 0
        var firstFailure: String? = null
        for (song in songs) {
            if (playlist.youtubePlaylistId != null && dataStoreManager.cookie.first() != originalCookie) {
                error("Account changed during playlist addition")
            }
            var saved = false
            var failed = false
            try {
                localPlaylistRepository.addTrackToLocalPlaylist(
                    id = playlistId, song = song, successMessage = addedMessage,
                    updatedYtMessage = addedMessage, errorMessage = getString(Res.string.error),
                ).collectResource(
                    onSuccess = { saved = true },
                    onError = { message -> failed = true; if (firstFailure == null) firstFailure = message },
                )
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                failed = true
            }
            if (saved) added++
            if (failed || !saved) failures++
        }
        log("Bulk playlist add: selected=${ids.size}, pending=${songs.size}, saved=$added, failures=$failures")
        makeToast(if (failures == 0 && added == songs.size) addedMessage
            else firstFailure?.let(::playlistAdditionErrorMessage) ?: getString(Res.string.error_occurred))
    }

    fun addToYouTubePlaylist(playlist: PlaylistsResult, videoIds: List<String>, availableSongs: List<SongEntity> = emptyList()) {
        val selectedIds = videoIds.distinct()
        val snapshots = availableSongs.toList()
        if (selectedIds.isEmpty()) return
        launchPlaylistOperation {
            val linkedPlaylist = localPlaylistRepository.getAllLocalPlaylists().firstOrNull()?.find {
                it.youtubePlaylistId?.removePrefix("VL") == playlist.browseId.removePrefix("VL")
            }
            if (linkedPlaylist != null) {
                // Keep the local mirror and YouTube in sync through the existing linked-playlist path.
                performLocalPlaylistAdd(linkedPlaylist.id, selectedIds, snapshots)
                return@launchPlaylistOperation
            }
            var added = false
            var alreadyPresent = false
            var failure: String? = null
            val originalCookie = dataStoreManager.cookie.first()
            for (videoId in selectedIds) {
                if (dataStoreManager.cookie.first() != originalCookie) error("Account changed during playlist addition")
                try {
                    localPlaylistRepository.addYouTubePlaylistItem(
                        youtubePlaylistId = playlist.browseId,
                        videoId = videoId,
                    ).collectResource(
                        onSuccess = { added = true },
                        onError = { message ->
                            if (message == PLAYLIST_ITEM_ALREADY_PRESENT) alreadyPresent = true
                            else if (failure == null) failure = message
                        },
                    )
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    if (failure == null) failure = getString(Res.string.error_occurred)
                }
            }
            // One batch confirmation; backend status tokens never reach the UI.
            makeToast(when {
                failure != null -> playlistAdditionErrorMessage(failure!!)
                added -> playlistAdditionSuccessMessage(playlist.title)
                alreadyPresent -> getString(Res.string.already_in_playlist)
                else -> getString(Res.string.error)
            })
        }
    }

    /** Create with the whole selection, using the single-track menu's local-first sync flow. */
    fun createPlaylistWithSelectedSongs(title: String, videoIds: List<String>, availableSongs: List<SongEntity> = emptyList()) {
        val trimmedTitle = title.trim()
        if (trimmedTitle.isEmpty() || videoIds.isEmpty()) return
        val selectedIds = videoIds.distinct()
        val snapshots = availableSongs.toList()
        launchPlaylistOperation {
            var localPlaylistId: Long? = null
            try {
                resolvePlaylistSongs(selectedIds, snapshots)
                val isYouTubeAuthenticated =
                    dataStoreManager.loggedIn.first() == DataStoreManager.TRUE &&
                        dataStoreManager.cookie.first().isNotBlank()
                val createdId = localPlaylistRepository.insertLocalPlaylistWithTracks(
                    localPlaylist = LocalPlaylistEntity(
                        title = trimmedTitle,
                        tracks = selectedIds,
                        syncState = if (isYouTubeAuthenticated) {
                            LocalPlaylistEntity.YouTubeSyncState.Syncing
                        } else {
                            LocalPlaylistEntity.YouTubeSyncState.PendingSync
                        },
                    ),
                    videoIds = selectedIds,
                )
                localPlaylistId = createdId
                if (createdId < 0L) {
                    makeToast(getString(Res.string.error_occurred))
                    return@launchPlaylistOperation
                }
                makeToast(playlistAdditionSuccessMessage(trimmedTitle))
                if (isYouTubeAuthenticated) {
                    var syncError: String? = null
                    localPlaylistRepository.syncLocalPlaylistToYouTubePlaylist(
                        playlistId = createdId,
                        successMessage = getString(Res.string.synced),
                        errorMessage = getString(Res.string.error),
                    ).collectResource(
                        onSuccess = {},
                        onError = { message -> syncError = message },
                    )
                    syncError?.let { message ->
                        localPlaylistRepository.updateLocalPlaylistYouTubePlaylistSyncState(
                            createdId,
                            LocalPlaylistEntity.YouTubeSyncState.PendingSync,
                        )
                        makeToast(message)
                    }
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

    private fun launchPlaylistOperation(action: suspend () -> Unit) {
        SelectedPlaylistOperations.scope.launch {
            try {
                action()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                log("Bulk playlist operation failed: ${failure.message}")
                makeToast(getString(Res.string.error_occurred))
            }
        }
    }

    private suspend fun resolvePlaylistSongs(ids: List<String>, availableSongs: List<SongEntity>): List<SongEntity> {
        val cached = songsOf(ids).associateBy { it.videoId }
        val visible = availableSongs.associateBy { it.videoId }
        val resolved = ids.map { cached[it] ?: visible[it] ?: error("Missing selected track metadata") }
        // Preserve cached like/download/history state; only seed rows that were not stored yet.
        for (song in resolved.filterNot { it.videoId in cached }) {
            check(songRepository.insertSong(song).firstOrNull() != null) { "Could not cache selected track" }
        }
        return resolved
    }

    fun removeFromLocalPlaylist(
        playlistId: Long,
        videoIds: List<String>,
    ) {
        viewModelScope.launch {
            val songs = songsOf(videoIds)
            if (songs.isEmpty()) {
                makeToast(getString(Res.string.error_occurred))
                return@launch
            }
            songs.forEach { song ->
                localPlaylistRepository
                    .removeTrackFromLocalPlaylist(
                        id = playlistId,
                        song = song,
                        successMessage = getString(Res.string.delete_song_from_playlist),
                        updatedYtMessage = getString(Res.string.removed_from_YouTube_playlist),
                        errorMessage = getString(Res.string.error_occurred),
                    ).collectResource(
                        onSuccess = { },
                        onError = { },
                    )
            }
            makeToast(getString(Res.string.delete_song_from_playlist))
        }
    }

    /**
     * The database returns rows in its own order, so the result is re-sorted back into the order
     * the user picked them in — which is what makes "play next" land in the expected sequence.
     */
    private suspend fun songsOf(videoIds: List<String>): List<SongEntity> =
        songRepository
            .getSongsByListVideoId(videoIds)
            .firstOrNull()
            .orEmpty()
            .sortedBy { videoIds.indexOf(it.videoId) }
}
