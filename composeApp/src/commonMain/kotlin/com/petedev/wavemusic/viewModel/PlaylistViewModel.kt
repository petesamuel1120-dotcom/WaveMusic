@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.petedev.wavemusic.viewModel

import androidx.lifecycle.viewModelScope
import com.petedev.wavemusic.common.Config
import com.petedev.wavemusic.domain.data.entities.DownloadState.STATE_DOWNLOADED
import com.petedev.wavemusic.domain.data.entities.DownloadState.STATE_DOWNLOADING
import com.petedev.wavemusic.domain.data.entities.PlaylistEntity
import com.petedev.wavemusic.domain.data.entities.SongEntity
import com.petedev.wavemusic.domain.data.model.browse.album.Track
import com.petedev.wavemusic.domain.data.model.browse.playlist.Author
import com.petedev.wavemusic.domain.data.model.browse.playlist.PlaylistBrowse
import com.petedev.wavemusic.domain.data.model.browse.playlist.PlaylistState
import com.petedev.wavemusic.domain.extension.now
import com.petedev.wavemusic.domain.extension.isSong
import com.petedev.wavemusic.domain.extension.toGenericMediaItem
import com.petedev.wavemusic.domain.mediaservice.handler.DownloadHandler
import com.petedev.wavemusic.domain.mediaservice.handler.PlaylistType
import com.petedev.wavemusic.domain.mediaservice.handler.QueueData
import com.petedev.wavemusic.domain.repository.LocalPlaylistRepository
import com.petedev.wavemusic.domain.repository.PlaylistRepository
import com.petedev.wavemusic.domain.repository.SongRepository
import com.petedev.wavemusic.domain.utils.Resource
import com.petedev.wavemusic.domain.utils.PlaylistMembershipChanges
import com.petedev.wavemusic.domain.utils.LiveStreamType
import com.petedev.wavemusic.domain.utils.MusicVideoType
import com.petedev.wavemusic.domain.utils.isAudioChartPlaylist
import com.petedev.wavemusic.domain.utils.isConvertedGenreVideoChart
import com.petedev.wavemusic.domain.utils.audioChartTitle
import com.petedev.wavemusic.domain.utils.collectLatestResource
import com.petedev.wavemusic.domain.utils.isRadioPlaylistId
import com.petedev.wavemusic.domain.utils.toListVideoId
import com.petedev.wavemusic.domain.utils.toPlaylistEntity
import com.petedev.wavemusic.domain.utils.toSongEntity
import com.petedev.wavemusic.domain.utils.toTrack
import com.petedev.wavemusic.logger.Logger
import com.petedev.wavemusic.viewModel.PlaylistUIState.Error
import com.petedev.wavemusic.viewModel.PlaylistUIState.Loading
import com.petedev.wavemusic.viewModel.PlaylistUIState.Success
import com.petedev.wavemusic.viewModel.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import org.koin.core.component.inject
import com.petedev.wavemusic.resources.Res
import com.petedev.wavemusic.resources.auto_created_by_youtube_music
import com.petedev.wavemusic.resources.can_t_add_to_youtube_playlist
import com.petedev.wavemusic.resources.can_t_delete_from_youtube_playlist
import com.petedev.wavemusic.resources.downloading
import com.petedev.wavemusic.resources.error
import com.petedev.wavemusic.resources.playlist
import com.petedev.wavemusic.resources.playlist_deleted
import com.petedev.wavemusic.resources.playlist_is_empty
import com.petedev.wavemusic.resources.radio
import com.petedev.wavemusic.resources.radio_not_available
import com.petedev.wavemusic.resources.removed_from_YouTube_playlist
import com.petedev.wavemusic.resources.suggestions
import com.petedev.wavemusic.resources.shuffle
import com.petedev.wavemusic.resources.shuffle_not_available
import com.petedev.wavemusic.resources.synced
import com.petedev.wavemusic.resources.syncing
import com.petedev.wavemusic.resources.view_count

private fun withoutLiveStreams(tracks: List<Track>): List<Track> =
    tracks.filterNot { LiveStreamType.isLiveStream(it) }

/** Keep the established Liked Music order and put only previously unseen songs at the front. */
private fun mergeNewLikedTracks(
    current: List<Track>,
    incoming: List<Track>,
): List<Track> {
    val currentIds = current.mapTo(mutableSetOf()) { it.videoId }
    val newTracks = withoutLiveStreams(incoming).filterNot { it.videoId in currentIds }
    return withoutLiveStreams((newTracks + current).distinctBy { it.videoId })
}

private fun Track.isSuggestedPlaylistSong(): Boolean =
    !MusicVideoType.isVideoSong(videoType) && toGenericMediaItem().isSong()

class PlaylistViewModel(
    private val songRepository: SongRepository,
    private val localPlaylistRepository: LocalPlaylistRepository,
    private val playlistRepository: PlaylistRepository,
) : BaseViewModel() {
    val downloadUtils: DownloadHandler by inject<DownloadHandler>()
    private var _uiState = MutableStateFlow<PlaylistUIState>(Loading)
    val uiState: StateFlow<PlaylistUIState> = _uiState

    private var _continuation = MutableStateFlow<String?>(null)
    val continuation: StateFlow<String?> = _continuation

    private var _playlistEntity: MutableStateFlow<PlaylistEntity?> = MutableStateFlow(null)
    var playlistEntity: StateFlow<PlaylistEntity?> = _playlistEntity

    val downloadState = _playlistEntity.map { it?.downloadState ?: 0 }.stateIn(viewModelScope, WhileSubscribed(1000), 0)
    val liked = _playlistEntity.map { it?.liked == true }.stateIn(viewModelScope, WhileSubscribed(1000), false)

    private var _tracks = MutableStateFlow<List<Track>>(emptyList())
    val tracks: StateFlow<List<Track>> = _tracks

    private val _suggestions = MutableStateFlow(PlaylistSuggestionState())
    val suggestions: StateFlow<PlaylistSuggestionState> = _suggestions
    private var suggestionsJob: Job? = null

    private var _tracksListState = MutableStateFlow<ListState>(ListState.IDLE)
    val tracksListState: StateFlow<ListState> = _tracksListState

    private var collectDownloadedJob: Job? = null
    private var _downloadedList = MutableStateFlow<List<String>>(emptyList())
    val downloadedList: StateFlow<List<String>> = _downloadedList

    private var playlistEntityJob: Job? = null
    private var getDataJob: Job? = null
    private var activePlaylistId: String? = null
    private val playlistRemovalMutex = Mutex()
    private var activeMembershipRevision = 0L
    private var newUpdateJob: Job? = null
    private var checkDownloadedPlaylist: Job? = null

    init {
        viewModelScope.launch {
            PlaylistMembershipChanges.changes.collect {
                val id = activePlaylistId ?: return@collect
                if (activeMembershipRevision != PlaylistMembershipChanges.revision(id)) getData(id)
            }
        }
        viewModelScope.launch {
            val listTrackStringJob =
                launch(Dispatchers.IO) {
                    downloadState
                        .collectLatest { state ->
                            newUpdateJob?.cancel()
                            val id = playlistEntity.value?.id ?: return@collectLatest
                            if (state == STATE_DOWNLOADING || state == STATE_DOWNLOADED) {
                                getFullTracks { tracks ->
                                    newUpdateJob =
                                        launch {
                                            val listSongs = songRepository.getSongsByListVideoId(tracks.toListVideoId()).firstOrNull() ?: emptyList()
                                            if (state == STATE_DOWNLOADED && listSongs.isNotEmpty()) {
                                                listSongs.filter { it.downloadState != STATE_DOWNLOADED }.let { notDownloaded ->
                                                    if (notDownloaded.isNotEmpty()) {
                                                        downloadTracks(notDownloaded.map { it.videoId })
                                                        updatePlaylistDownloadState(id, STATE_DOWNLOADING)
                                                    } else {
                                                        updatePlaylistDownloadState(id, STATE_DOWNLOADED)
                                                    }
                                                }
                                            }
                                            downloadUtils.downloads.collectLatest { downloads ->
                                                var count = 0
                                                tracks.forEachIndexed { index, track ->
                                                    val trackDownloadState = downloads[track.videoId]?.first?.state
                                                    val videoDownloadState =
                                                        downloads[track.videoId]?.second?.state ?: DownloadHandler.State.STATE_COMPLETED
                                                    if (trackDownloadState == DownloadHandler.State.STATE_DOWNLOADING ||
                                                        videoDownloadState == DownloadHandler.State.STATE_DOWNLOADING
                                                    ) {
                                                        updatePlaylistDownloadState(id, STATE_DOWNLOADING)
                                                    } else if (trackDownloadState == DownloadHandler.State.STATE_COMPLETED &&
                                                        videoDownloadState == DownloadHandler.State.STATE_COMPLETED
                                                    ) {
                                                        count++
                                                    }
                                                    if (count == tracks.size) {
                                                        updatePlaylistDownloadState(id, STATE_DOWNLOADED)
                                                    }
                                                }
                                            }
                                        }
                                }
                            }
                        }
                }
            listTrackStringJob.join()
        }
    }

    private fun updatePlaylistDownloadState(
        id: String,
        state: Int,
    ) {
        viewModelScope.launch {
            playlistRepository.updatePlaylistDownloadState(id, state)
            delay(500)
            _playlistEntity.update {
                it?.copy(downloadState = state)
            }
        }
    }

    private fun downloadTracks(listJob: List<String>) {
        viewModelScope.launch {
            listJob.forEach { videoId ->
                songRepository.getSongById(videoId).singleOrNull()?.let { song ->
                    if (song.downloadState != STATE_DOWNLOADED) {
                        downloadUtils.downloadTrack(videoId, song.title, song.thumbnails ?: "")
                    }
                }
            }
        }
    }

    private fun resetData() {
        playlistEntityJob?.cancel()
        playlistEntityJob = null
        _uiState.value = Loading
        _playlistEntity.value = null
        _tracks.value = emptyList()
        _continuation.value = null
        _tracksListState.value = ListState.IDLE
        _downloadedList.value = emptyList()
        suggestionsJob?.cancel()
        suggestionsJob = null
        _suggestions.value = PlaylistSuggestionState()
        checkDownloadedPlaylist?.cancel()
        checkDownloadedPlaylist = null
    }

    @OptIn(ExperimentalTime::class)
    fun getData(id: String) {
        val samePlaylist = _uiState.value.data?.id == id
        val currentData = _uiState.value.data
        val staleAudioChart = currentData != null &&
            isAudioChartPlaylist(currentData.title, currentData.author.name) &&
            _tracks.value.any { !MusicVideoType.isAudio(it.videoType) }
        val staleVideoChart = currentData != null &&
            isConvertedGenreVideoChart(currentData.title, currentData.author.name)
        val revision = PlaylistMembershipChanges.revision(id)
        val previousSnapshot = getCachedPlaylistSnapshot(id)
        val membershipChanged = (activePlaylistId == id && activeMembershipRevision != revision) ||
            (previousSnapshot != null && previousSnapshot.membershipRevision != revision) ||
            (previousSnapshot == null && revision > 0L)
        activePlaylistId = id
        // Re-entering a playlist that is already alive should not blank it and trigger a second
        // network load. The route can be restored while its ViewModel remains in the back stack.
        if (samePlaylist && !membershipChanged && !staleAudioChart && !staleVideoChart) return
        activeMembershipRevision = revision

        val cachedSnapshot = previousSnapshot?.takeIf {
            it.membershipRevision == revision &&
                !isConvertedGenreVideoChart(it.playlistState.title, it.playlistState.author.name) &&
                (!isAudioChartPlaylist(it.playlistState.title, it.playlistState.author.name) ||
                    (it.continuation.isNullOrEmpty() && it.tracks.all { track -> MusicVideoType.isAudio(track.videoType) }))
        }
        val shouldRefreshFromNetwork =
            membershipChanged || cachedSnapshot == null ||
                Clock.System.now().toEpochMilliseconds() - cachedSnapshot.cachedAtMillis >= PLAYLIST_CACHE_TTL_MS
        getDataJob?.cancel()
        if (!samePlaylist || staleVideoChart) resetData()
        else if (staleAudioChart) _tracks.value = _tracks.value.filter { MusicVideoType.isAudio(it.videoType) }
        // Restore the last in-process snapshot synchronously. Playlist destinations are removed
        // from the back stack when closed, so their ViewModels are recreated on the next open.
        cachedSnapshot?.let { snapshot ->
            _uiState.value = Success(snapshot.playlistState)
            _tracks.value = withoutLiveStreams(snapshot.tracks)
            _continuation.value = snapshot.continuation
            _tracksListState.value =
                if (snapshot.continuation.isNullOrEmpty()) ListState.PAGINATION_EXHAUST else ListState.IDLE
        }
        getDataJob = viewModelScope.launch {
            // Playlist browse data and its songs are persisted locally. Hydrate the page from that
            // snapshot first so Liked Music and other previously opened playlists are immediately
            // usable on re-entry, including offline. The request below refreshes the snapshot in
            // the background without putting the page back into its loading state.
            val cachedPlaylist =
                if (id.isRadioPlaylistId()) {
                    null
                } else {
                    playlistRepository.getPlaylist(id).firstOrNull()
                }
            if (cachedPlaylist != null && !samePlaylist && !(membershipChanged && cachedSnapshot == null) &&
                !isConvertedGenreVideoChart(cachedPlaylist.title, cachedPlaylist.author)) {
                val audioChart = isAudioChartPlaylist(cachedPlaylist.title, cachedPlaylist.author)
                _playlistEntity.value = cachedPlaylist
                if (cachedSnapshot == null) {
                    _uiState.value =
                        Success(
                            PlaylistState(
                                id = cachedPlaylist.id,
                                title = if (audioChart) audioChartTitle(cachedPlaylist.title) else cachedPlaylist.title,
                                isRadio = false,
                                author = Author(id = "", name = cachedPlaylist.author.orEmpty()),
                                thumbnail = cachedPlaylist.thumbnails,
                                description = cachedPlaylist.description,
                                trackCount = cachedPlaylist.trackCount,
                                year = cachedPlaylist.year ?: now().year.toString(),
                            ),
                        )
                    _tracksListState.value = ListState.LOADING
                    val cachedSongIds = cachedPlaylist.tracks.orEmpty()
                    if (cachedSongIds.isNotEmpty()) {
                        songRepository.getSongsByListVideoId(cachedSongIds).firstOrNull()?.let { songs ->
                            _tracks.value = withContext(Dispatchers.Default) {
                                withoutLiveStreams(songs.map { it.toTrack() }).let { tracks ->
                                    if (audioChart) tracks.filter { MusicVideoType.isAudio(it.videoType) } else tracks
                                }
                            }
                        }
                    }
                    _tracksListState.value = if (audioChart) ListState.LOADING else ListState.PAGINATION_EXHAUST
                }
            }

            // A recent in-memory snapshot is already the page the user left. Keep it as-is and
            // avoid another browse request; older snapshots continue through the refresh path.
            if (!shouldRefreshFromNetwork) return@launch

            // Check radio
            if (id.isRadioPlaylistId()) {
                playlistRepository
                    .getRadio(
                        id,
                        radioString = getString(Res.string.radio),
                        defaultDescription = getString(Res.string.auto_created_by_youtube_music),
                        viewString = getString(Res.string.view_count),
                    ).collect { res ->
                        val data = res.data
                        when (res) {
                            is Resource.Success if (data != null) -> {
                                Logger.d(tag, "Radio data: $data")
                                _uiState.value =
                                    Success(
                                        data =
                                            PlaylistState(
                                                id = data.first.id,
                                                title = data.first.title,
                                                isRadio = true,
                                                author = data.first.author,
                                                thumbnail =
                                                    data.first.thumbnails
                                                        .lastOrNull()
                                                        ?.url,
                                                description = data.first.description,
                                                trackCount = data.first.trackCount,
                                                year = data.first.year,
                                            ),
                                    )
                                // Keep the cached playlist visible and in order, while adding only
                                // genuinely new songs from YouTube's latest page at the front.
                                // This avoids rebuilding the whole Liked Music list on every fresh
                                // route entry and still surfaces newly liked songs immediately.
                                _tracks.value = withoutLiveStreams(
                                    (data.first.tracks + _tracks.value).distinctBy { it.videoId },
                                )
                                _continuation.value = data.second
                                // A cached snapshot is displayed before this request and is marked
                                // exhausted so it can render immediately. A successful refresh can
                                // replace it with only the first page, so restore pagination from
                                // the fresh continuation token instead of keeping that stale state.
                                _tracksListState.value =
                                    if (data.second.isNullOrEmpty()) {
                                        ListState.PAGINATION_EXHAUST
                                    } else {
                                        ListState.IDLE
                                    }
                                updatePlaylistSnapshot(data.second)
                                playlistRepository.insertRadioPlaylist(data.first.toPlaylistEntity())
                            }

                            else -> {
                                if (cachedSnapshot == null) {
                                    _uiState.value = Error(res.message ?: "Empty response")
                                }
                            }
                        }
                    }
            } else {
                // This is an online playlist
                playlistRepository
                    .getPlaylistData(id, getString(Res.string.view_count))
                    .collect { res ->
                        val data = res.data
                        when (res) {
                            is Resource.Success if (data != null) -> {
                                Logger.d(tag, "Playlist data: $data")
                                log("Playlist endpoint: ${data.first.shuffleEndpoint}")
                                val playlistState =
                                    PlaylistState(
                                                id = data.first.id,
                                                title = data.first.title,
                                                isRadio = false,
                                                author = data.first.author,
                                                thumbnail =
                                                    data.first.thumbnails
                                                        .lastOrNull()
                                                        ?.url,
                                                description = data.first.description,
                                                trackCount = data.first.trackCount,
                                                year = data.first.year,
                                                shuffleEndpoint = data.first.shuffleEndpoint,
                                                radioEndpoint = data.first.radioEndpoint,
                                                isEditable = data.first.isEditable,
                                            )
                                _uiState.value = Success(playlistState)
                                // Liked Music should retain its cached rows and only add tracks
                                // that appeared since the last visit. Other playlists continue to
                                // use their normal first-page replacement behavior.
                                _tracks.value = withoutLiveStreams(
                                    if (id == LIKED_MUSIC_PLAYLIST_ID) {
                                        mergeNewLikedTracks(_tracks.value, data.first.tracks)
                                    } else {
                                        data.first.tracks
                                    },
                                )
                                _continuation.value = data.second
                                // A cached playlist snapshot can include pages beyond this
                                // response. Restore pagination from the current continuation
                                // token after updating the first page.
                                _tracksListState.value =
                                    if (data.second.isNullOrEmpty()) {
                                        ListState.PAGINATION_EXHAUST
                                    } else {
                                        ListState.IDLE
                                    }
                                updatePlaylistSnapshot(data.second)
                                getPlaylistEntity(id = data.first.id, playlistBrowse = data.first)
                            }

                            else -> {
                                if (cachedPlaylist == null) getPlaylistEntity(id)
                            }
                        }
                    }
            }
        }
    }

    fun getContinuationTrack(
        playlistId: String,
        continuation: String?,
    ) {
        viewModelScope.launch {
            if (activePlaylistId != playlistId) return@launch
            if (_tracksListState.value == ListState.PAGINATING) return@launch
            if (continuation.isNullOrEmpty()) {
                _tracksListState.value = ListState.PAGINATION_EXHAUST
                return@launch
            } else {
                _tracksListState.value = ListState.PAGINATING
                songRepository
                    .getContinueTrack(
                        playlistId,
                        continuation,
                        fromPlaylist = true,
                        requirePlaylistMembership = _uiState.value.data?.isEditable == true,
                    ).collectLatest { res ->
                        if (activePlaylistId != playlistId) return@collectLatest
                        val responseTracks = res.first
                        if (responseTracks == null) {
                            // A failed YouTube continuation request is not the end of the list.
                            // Preserve its token so the user can retry instead of losing every
                            // song after this page.
                            _continuation.value = continuation
                            _tracksListState.value = ListState.ERROR
                            return@collectLatest
                        }
                        val pageTracks = withoutLiveStreams(responseTracks)
                        pageTracks.forEach { track ->
                            songRepository
                                .insertSong(
                                    track
                                        .toSongEntity()
                                        .copy(
                                            inLibrary = Config.REMOVED_SONG_DATE_TIME,
                                        ),
                                ).singleOrNull()
                                ?.let {
                                    log("Insert song: $it")
                                }
                        }
                        val existingIds = _tracks.value.mapTo(mutableSetOf()) { it.videoId }
                        val addedTracks = pageTracks.filterNot { it.videoId in existingIds }
                        _tracks.update { current ->
                            (current + pageTracks).distinctBy { it.videoId }
                        }
                        _uiState.value.data?.let { state ->
                            _uiState.value = Success(state.copy(trackCount = maxOf(state.trackCount, _tracks.value.size)))
                        }
                        val nextContinuation = res.second
                        if (playlistId == LIKED_MUSIC_PLAYLIST_ID) {
                            persistLikedMusicTrackOrder(playlistId)
                        }
                        if (nextContinuation.isNullOrEmpty()) {
                            _continuation.value = null
                            _tracksListState.value = ListState.PAGINATION_EXHAUST
                        } else if (nextContinuation == continuation && addedTracks.isEmpty()) {
                            // Avoid repeatedly requesting a token that returned no new songs.
                            // Keep it retryable instead of presenting the playlist as complete.
                            _continuation.value = continuation
                            _tracksListState.value = ListState.ERROR
                        } else {
                            _continuation.value = nextContinuation
                            _tracksListState.value = ListState.IDLE
                        }
                        updatePlaylistSnapshot(_continuation.value)
                    }
            }
        }
    }

    /** Persist the accumulated remote order instead of letting refreshes shrink it back to page one. */
    private suspend fun persistLikedMusicTrackOrder(playlistId: String) {
        if (playlistId != LIKED_MUSIC_PLAYLIST_ID || activePlaylistId != playlistId) return
        val currentEntity =
            _playlistEntity.value
                ?: playlistRepository.getPlaylist(playlistId).firstOrNull()
                ?: return
        val updatedEntity =
            currentEntity.copy(
                tracks = _tracks.value.map { it.videoId },
                trackCount = maxOf(currentEntity.trackCount, _tracks.value.size),
            )
        playlistRepository.insertAndReplacePlaylist(updatedEntity)
        _playlistEntity.value = updatedEntity
    }

    /** Apply a YouTube like change to the open Liked Music list, then reconcile with YouTube. */
    fun refreshYouTubeLikedPlaylist(
        videoId: String,
        isLiked: Boolean,
        currentlyPlayingSong: SongEntity?,
    ) {
        getDataJob?.cancel()
        getDataJob =
            viewModelScope.launch {
                // Update the visible list immediately from the player entity. The local song
                // lookup is only a fallback; newly played songs are not always persisted yet.
                val changedTrack =
                    currentlyPlayingSong
                        ?.takeIf { it.videoId == videoId && !LiveStreamType.isLiveStream(it) }
                        ?.toTrack()
                        ?: if (isLiked) songRepository.getSongById(videoId).firstOrNull()?.toTrack() else null
                _tracks.update { current ->
                    if (isLiked) {
                        withoutLiveStreams((listOfNotNull(changedTrack) + current).distinctBy { it.videoId })
                    } else {
                        current.filterNot { it.videoId == videoId }
                    }
                }
                updatePlaylistSnapshot()

                playlistRepository
                    .getPlaylistData(LIKED_MUSIC_PLAYLIST_ID, getString(Res.string.view_count))
                    .collectLatest { res ->
                        val data = res.data
                        when (res) {
                            is Resource.Success if (data != null) -> {
                                val (browse, nextContinuation) = data
                                val playlistState =
                                    PlaylistState(
                                        id = browse.id,
                                        title = browse.title,
                                        isRadio = false,
                                        author = browse.author,
                                        thumbnail = browse.thumbnails.lastOrNull()?.url,
                                        description = browse.description,
                                        trackCount = browse.trackCount,
                                        year = browse.year,
                                        shuffleEndpoint = browse.shuffleEndpoint,
                                        radioEndpoint = browse.radioEndpoint,
                                    )
                                _uiState.value = Success(playlistState)
                                // Merge the fresh page with the loaded pages so refresh preserves
                                // every known song's position. Only genuinely unseen songs from
                                // YouTube's first page are inserted ahead of the existing order.
                                _tracks.update { current ->
                                    val merged = mergeNewLikedTracks(current, browse.tracks)
                                    if (isLiked && changedTrack != null) {
                                        listOfNotNull(merged.firstOrNull { it.videoId == changedTrack.videoId }) +
                                            merged.filterNot { it.videoId == changedTrack.videoId }
                                    } else {
                                        merged
                                            .filterNot { it.videoId == videoId }
                                            .distinctBy { it.videoId }
                                    }
                                }
                                _continuation.value = nextContinuation
                                _tracksListState.value =
                                    if (nextContinuation.isNullOrEmpty()) {
                                        ListState.PAGINATION_EXHAUST
                                    } else {
                                        ListState.IDLE
                                    }
                                updatePlaylistSnapshot(nextContinuation)
                                getPlaylistEntity(id = browse.id, playlistBrowse = browse)
                            }

                            else -> {
                                // Preserve the visible list if the refresh fails; re-entering the
                                // playlist can retry through the regular load path.
                            }
                        }
                    }
            }
    }

    @OptIn(ExperimentalTime::class)
    private fun updatePlaylistSnapshot(continuation: String? = _continuation.value) {
        val id = activePlaylistId ?: return
        _uiState.value.data?.let { playlistState ->
            savePlaylistSnapshot(
                id,
                CachedPlaylistSnapshot(
                    playlistState = playlistState,
                    tracks = _tracks.value,
                    continuation = continuation,
                    cachedAtMillis = Clock.System.now().toEpochMilliseconds(),
                    membershipRevision = activeMembershipRevision,
                ),
            )
        }
    }

    private fun getPlaylistEntity(
        id: String,
        playlistBrowse: PlaylistBrowse? = null,
    ) {
        playlistEntityJob?.cancel()
        playlistEntityJob =
            viewModelScope.launch {
                val playlistEntity = playlistRepository.getPlaylist(id).firstOrNull()
                if (playlistBrowse != null) {
                    val playlistToStore =
                        if (id == LIKED_MUSIC_PLAYLIST_ID) {
                            playlistBrowse.copy(
                                tracks = _tracks.value,
                                trackCount = maxOf(playlistBrowse.trackCount, _tracks.value.size),
                            ).toPlaylistEntity()
                        } else {
                            playlistBrowse.toPlaylistEntity()
                        }
                    if (playlistEntity == null) {
                        playlistRepository.insertAndReplacePlaylist(
                            playlistToStore,
                        )
                        _playlistEntity.value = playlistRepository.getPlaylist(id).firstOrNull()
                        playlistRepository.updatePlaylistInLibrary(
                            playlistId = id,
                            inLibrary = now(),
                        )
                    } else {
                        // Keep the local snapshot current as well as the in-memory page. Without
                        // replacing the browse metadata here, later visits keep hydrating the old
                        // title/artwork/track list even though the live request succeeded.
                        playlistRepository.insertAndReplacePlaylist(
                            playlistToStore,
                        )
                        _playlistEntity.value =
                            playlistRepository.getPlaylist(id).firstOrNull() ?: playlistEntity
                        playlistRepository.updatePlaylistInLibrary(
                            playlistId = id,
                            inLibrary = now(),
                        )
                    }
                    playlistBrowse.tracks.forEach { tracks ->
                        songRepository
                            .insertSong(
                                tracks.toSongEntity().copy(
                                    inLibrary = Config.REMOVED_SONG_DATE_TIME,
                                ),
                            ).firstOrNull()
                            ?.let {
                                log("Insert song: $it")
                            }
                    }
                } else if (playlistEntity != null) {
                    _playlistEntity.value = playlistEntity
                    playlistRepository.updatePlaylistInLibrary(
                        playlistId = id,
                        inLibrary = now(),
                    )
                    _uiState.value =
                        Success(
                            data =
                                PlaylistState(
                                    id = playlistEntity.id,
                                    title = playlistEntity.title,
                                    isRadio = false,
                                    author =
                                        Author(
                                            id = "",
                                            name = playlistEntity.author ?: "",
                                        ),
                                    thumbnail = playlistEntity.thumbnails,
                                    description = playlistEntity.description,
                                    trackCount = playlistEntity.trackCount,
                                    year = playlistEntity.year ?: now().year.toString(),
                                ),
                        )
                    _tracksListState.value = ListState.LOADING
                    playlistEntity.tracks?.let {
                        songRepository
                            .getSongsByListVideoId(it)
                            .singleOrNull()
                            ?.let { song ->
                                _tracks.value = withoutLiveStreams(song.map { it.toTrack() })
                            }
                    }
                    _tracksListState.value = ListState.PAGINATION_EXHAUST
                    if (playlistEntity.downloadState != STATE_DOWNLOADED) {
                        checkDownloadedPlaylist =
                            launch {
                                val listSong =
                                    songRepository
                                        .getSongsByListVideoId(
                                            playlistEntity.tracks ?: emptyList(),
                                        ).firstOrNull()
                                Logger.d(tag, "List song: $listSong")
                                if (!listSong.isNullOrEmpty() && listSong.size == playlistEntity.tracks?.size &&
                                    listSong.all {
                                        it.downloadState == STATE_DOWNLOADED
                                    }
                                ) {
                                    updatePlaylistDownloadState(playlistEntity.id, STATE_DOWNLOADED)
                                }
                            }
                    }
                } else {
                    _uiState.value = Error("Empty response")
                }
            }
    }

    fun updatePlaylistLiked(
        liked: Boolean,
        id: String,
    ) {
        viewModelScope.launch {
            val tempLiked = if (liked) 1 else 0
            playlistRepository.updatePlaylistLiked(id, tempLiked)
            _playlistEntity.update {
                it?.copy(
                    liked = tempLiked == 1,
                )
            }
            getFullTracks { }
        }
    }

    fun removeYourYouTubePlaylistItem(
        playlistId: String,
        videoId: String,
    ) {
        removeYourYouTubePlaylistItems(playlistId, listOf(videoId))
    }

    /** Remove the selection in order and finish each cache write before removing the next item. */
    fun removeYourYouTubePlaylistItems(
        playlistId: String,
        videoIds: List<String>,
    ) {
        val normalizedPlaylistId = playlistId.removePrefix("VL")
        val requestedIds = videoIds.filter { it.isNotBlank() }.distinct()
        if (requestedIds.isEmpty()) return
        viewModelScope.launch {
            playlistRemovalMutex.withLock {
                val successMessage = getString(Res.string.removed_from_YouTube_playlist)
                val errorMessage = getString(Res.string.can_t_delete_from_youtube_playlist)
                var removed = 0
                var lastError: String? = null
                for (videoId in requestedIds) {
                    var playlistToSave: PlaylistEntity? = null
                    localPlaylistRepository
                        .removeYouTubePlaylistItem(
                            youtubePlaylistId = normalizedPlaylistId,
                            videoId = videoId,
                            successMessage = successMessage,
                            errorMessage = errorMessage,
                        ).collectLatestResource(
                            onSuccess = {
                                removed++
                                // A batch can finish after navigation. Keep the new page's state intact.
                                if (activePlaylistId == normalizedPlaylistId) {
                                    val previousTracks = _tracks.value
                                    val updatedTracks = previousTracks.filterNot { it.videoId == videoId }
                                    if (updatedTracks.size != previousTracks.size) {
                                        _tracks.value = updatedTracks
                                        _uiState.value.data?.let { state ->
                                            _uiState.value = Success(state.copy(trackCount = (state.trackCount - 1).coerceAtLeast(0)))
                                        }
                                        _playlistEntity.value?.let { playlist ->
                                            val updatedPlaylist = playlist.copy(
                                                tracks = playlist.tracks?.filterNot { it == videoId },
                                                trackCount = (playlist.trackCount - 1).coerceAtLeast(0),
                                            )
                                            _playlistEntity.value = updatedPlaylist
                                            playlistToSave = updatedPlaylist
                                        }
                                        updatePlaylistSnapshot()
                                    }
                                }
                            },
                            onError = { lastError = it },
                        )
                    playlistToSave?.let { playlistRepository.insertAndReplacePlaylist(it) }
                }
                // Keep the established feedback style without stacking one toast per selected song.
                if (lastError != null) {
                    makeToast(lastError!!)
                } else if (removed > 0) {
                    makeToast(successMessage)
                }
            }
        }
    }

    fun getSuggestions(playlistId: String) {
        val current = _suggestions.value
        val fallbackTracks = fallbackSuggestionsFor(playlistId)
        if (current.playlistId == playlistId && (current.isLoading || current.hasLoaded)) {
            if (fallbackTracks.isNotEmpty()) {
                _suggestions.update { state ->
                    if (state.playlistId == playlistId) {
                        state.copy(tracks = (state.tracks + fallbackTracks).distinctBy { it.videoId })
                    } else {
                        state
                    }
                }
            }
            clearEmptyPlaylistBrowseRows(playlistId)
            return
        }

        suggestionsJob?.cancel()
        // Empty YouTube playlists can already include recommendation rows in the first browse
        // response. Surface those immediately as suggestions while fetching the editable
        // suggestions continuation; otherwise they look like owned tracks and get a remove action.
        _suggestions.value =
            PlaylistSuggestionState(
                playlistId = playlistId,
                tracks = fallbackTracks,
                isLoading = true,
            )
        clearEmptyPlaylistBrowseRows(playlistId)
        suggestionsJob =
            viewModelScope.launch {
                localPlaylistRepository
                    .getSuggestionsTrackForYouTubePlaylist(playlistId)
                    .collectLatestResource(
                        onSuccess = { result ->
                            _suggestions.value =
                                PlaylistSuggestionState(
                                    playlistId = playlistId,
                                    reloadParams = result?.first,
                                    tracks =
                                        result?.second.orEmpty()
                                            .filter(Track::isSuggestedPlaylistSong)
                                            .filterNot { it.videoId in ownedYouTubePlaylistTrackIds(playlistId) },
                                    hasLoaded = true,
                                )
                        },
                        onError = { message ->
                            Logger.w(tag, "Could not load playlist suggestions: $message")
                            _suggestions.update {
                                it.copy(
                                    tracks = it.tracks.ifEmpty { fallbackTracks },
                                    isLoading = false,
                                    hasLoaded = true,
                                )
                            }
                        },
                        onLoading = {
                            _suggestions.update { it.copy(playlistId = playlistId, isLoading = true) }
                        },
                    )
            }
    }

    private fun fallbackSuggestionsFor(playlistId: String): List<Track> {
        val playlist = _uiState.value.data ?: return emptyList()
        if (playlist.id.removePrefix("VL") != playlistId || playlist.isRadio || playlist.trackCount != 0) return emptyList()
        return _tracks.value
            .asSequence()
            .filter(Track::isSuggestedPlaylistSong)
            .distinctBy { it.videoId }
            .toList()
    }

    private fun clearEmptyPlaylistBrowseRows(playlistId: String) {
        val playlist = _uiState.value.data ?: return
        if (playlist.id.removePrefix("VL") == playlistId && !playlist.isRadio && playlist.trackCount == 0) {
            _tracks.value = emptyList()
        }
    }

    private fun ownedYouTubePlaylistTrackIds(playlistId: String): Set<String> {
        val playlist = _uiState.value.data ?: return emptySet()
        if (playlist.id.removePrefix("VL") != playlistId || playlist.isRadio || playlist.trackCount == 0) return emptySet()
        return _playlistEntity.value?.tracks?.toSet()
            ?: _tracks.value.mapTo(mutableSetOf()) { it.videoId }
    }

    fun reloadSuggestions() {
        val current = _suggestions.value
        val reloadParams = current.reloadParams ?: return
        viewModelScope.launch {
            _suggestions.update { it.copy(isLoading = true) }
            localPlaylistRepository
                .reloadSuggestionPlaylist(reloadParams)
                .collectLatestResource(
                    onSuccess = { result ->
                        _suggestions.value =
                            current.copy(
                                reloadParams = result?.first,
                                tracks = result?.second.orEmpty().filter(Track::isSuggestedPlaylistSong),
                                isLoading = false,
                                hasLoaded = true,
                            )
                    },
                    onError = { message ->
                        Logger.w(tag, "Could not reload playlist suggestions: $message")
                        _suggestions.update { it.copy(isLoading = false, hasLoaded = true) }
                    },
                )
        }
    }

    fun playSuggestedTrack(
        track: Track,
        suggestionTracks: List<Track>,
    ) {
        if (suggestionTracks.none { it.videoId == track.videoId }) return
        val index = suggestionTracks.indexOfFirst { it.videoId == track.videoId }
        setQueueData(
            QueueData.Data(
                listTracks = suggestionTracks.toCollection(arrayListOf()),
                firstPlayedTrack = track,
                playlistId = "RDAMVM${track.videoId}",
                playlistName = "${uiState.value.data?.title.orEmpty()} ${getString(Res.string.suggestions)}",
                playlistType = PlaylistType.RADIO,
                continuation = null,
            ),
        )
        loadMediaItem(track, Config.PLAYLIST_CLICK, index)
    }

    fun addSuggestedTrackToPlaylist(
        playlistId: String,
        track: Track,
    ) {
        if (_suggestions.value.playlistId != playlistId || !_suggestions.value.tracks.any { it.videoId == track.videoId }) return
        val playlistName = _uiState.value.data?.title ?: _playlistEntity.value?.title ?: getString(Res.string.playlist)
        viewModelScope.launch {
            localPlaylistRepository
                .addYouTubePlaylistItem(playlistId, track.videoId)
                .collectLatestResource(
                    onSuccess = {
                        val alreadyLoaded = _tracks.value.any { it.videoId == track.videoId }
                        _suggestions.update { state ->
                            state.copy(tracks = state.tracks.filterNot { it.videoId == track.videoId })
                        }
                        _tracks.update { tracks -> (tracks + track).distinctBy { it.videoId } }
                        val updatedEntity =
                            _playlistEntity.value?.let { entity ->
                                entity.copy(
                                    // The first browse page for an empty YouTube playlist may
                                    // contain suggestion rows which were cached beside the empty
                                    // playlist. Once the user adds a suggestion, replace that
                                    // stale zero-track cache instead of marking every suggestion
                                    // as a playlist member.
                                    tracks =
                                        (if (entity.trackCount == 0) emptyList() else entity.tracks.orEmpty())
                                            .plus(track.videoId)
                                            .distinct(),
                                    trackCount = (entity.trackCount + if (alreadyLoaded) 0 else 1).coerceAtLeast(1),
                                )
                            }
                        if (updatedEntity != null) {
                            _playlistEntity.value = updatedEntity
                            viewModelScope.launch { playlistRepository.insertAndReplacePlaylist(updatedEntity) }
                        }
                        _uiState.value.data?.let { state ->
                            _uiState.value = Success(state.copy(trackCount = state.trackCount + if (alreadyLoaded) 0 else 1))
                        }
                        makeToast(playlistAdditionSuccessMessage(playlistName))
                    },
                    onError = { message ->
                        makeToast(if (message == com.petedev.wavemusic.domain.utils.PLAYLIST_ITEM_ALREADY_PRESENT)
                            playlistAdditionErrorMessage(message) else getString(Res.string.can_t_add_to_youtube_playlist))
                    },
                )
        }
    }

    fun onUIEvent(event: PlaylistUIEvent) {
        val data = uiState.value.data ?: return
        when (event) {
            is PlaylistUIEvent.ItemClick -> {
                val videoId = event.videoId
                val loadedList = tracks.value
                val clickedSong = loadedList.first { it.videoId == videoId }
                val index = loadedList.indexOf(clickedSong)
                setQueueData(
                    QueueData.Data(
                        listTracks = loadedList.toCollection(arrayListOf<Track>()),
                        firstPlayedTrack = clickedSong,
                        playlistId = data.id,
                        playlistName = "${
                            getString(
                                Res.string.playlist,
                            )
                        } \"${data.title}\"",
                        playlistType = PlaylistType.PLAYLIST,
                        continuation = continuation.value,
                    ),
                )
                loadMediaItem(
                    clickedSong,
                    Config.PLAYLIST_CLICK,
                    index,
                )
            }

            PlaylistUIEvent.PlayAll -> {
                val loadedList = tracks.value
                if (loadedList.isEmpty()) {
                    makeToast(
                        getString(Res.string.playlist_is_empty),
                    )
                    return
                }
                val clickedSong = loadedList.first()
                setQueueData(
                    QueueData.Data(
                        listTracks = loadedList.toCollection(arrayListOf<Track>()),
                        firstPlayedTrack = clickedSong,
                        playlistId = data.id,
                        playlistName = "${
                            getString(
                                Res.string.playlist,
                            )
                        } \"${data.title}\"",
                        playlistType = PlaylistType.PLAYLIST,
                        continuation = continuation.value,
                    ),
                )
                loadMediaItem(
                    clickedSong,
                    Config.PLAYLIST_CLICK,
                    0,
                )
            }

            PlaylistUIEvent.Shuffle -> {
                val shuffleEndpoint = data.shuffleEndpoint
                if (shuffleEndpoint == null) {
                    // Cached Liked Music has no remote shuffle endpoint, but its loaded songs
                    // can still start a shuffled playlist queue with the current continuation.
                    if (data.id == LIKED_MUSIC_PLAYLIST_ID) {
                        val playableTracks = withoutLiveStreams(tracks.value).shuffled()
                        if (playableTracks.isEmpty()) {
                            makeToast(getString(Res.string.playlist_is_empty))
                            return
                        }
                        val firstTrack = playableTracks.first()
                        setQueueData(
                            QueueData.Data(
                                listTracks = playableTracks.toCollection(arrayListOf<Track>()),
                                firstPlayedTrack = firstTrack,
                                playlistId = data.id,
                                playlistName = "\"${data.title}\" ${getString(Res.string.shuffle)}",
                                playlistType = PlaylistType.PLAYLIST,
                                continuation = continuation.value,
                            ),
                        )
                        loadMediaItem(firstTrack, Config.PLAYLIST_CLICK, 0)
                        return
                    }
                    makeToast(
                        getString(Res.string.shuffle_not_available),
                    )
                    return
                } else {
                    viewModelScope.launch {
                        songRepository.getRadioFromEndpoint(shuffleEndpoint).collectLatest { res ->
                            val result = res.data
                            when (res) {
                                is Resource.Success if (result != null) -> {
                                    Logger.d(tag, "Shuffle data: ${result.first.size}")
                                    val playableTracks = withoutLiveStreams(result.first)
                                    if (playableTracks.isEmpty()) return@collectLatest
                                    setQueueData(
                                        QueueData.Data(
                                            listTracks = playableTracks.toCollection(arrayListOf<Track>()),
                                            firstPlayedTrack = playableTracks.first(),
                                            playlistId = shuffleEndpoint.playlistId,
                                            playlistName = "\"${data.title}\" ${getString(Res.string.shuffle)}",
                                            playlistType = PlaylistType.RADIO,
                                            continuation = result.second,
                                        ),
                                    )
                                    loadMediaItem(
                                        playableTracks.first(),
                                        Config.RADIO_CLICK,
                                        0,
                                    )
                                }

                                else -> {
                                    makeToast(
                                        res.message ?: getString(Res.string.error),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            PlaylistUIEvent.StartRadio -> {
                val radioEndpoint = data.radioEndpoint
                if (radioEndpoint == null) {
                    makeToast(
                        getString(Res.string.radio_not_available),
                    )
                    return
                } else {
                    viewModelScope.launch {
                        songRepository.getRadioFromEndpoint(radioEndpoint).collectLatest { res ->
                            val result = res.data
                            when (res) {
                                is Resource.Success if (result != null) -> {
                                    Logger.d(tag, "Radio data: ${result.first.size}")
                                    val playableTracks = withoutLiveStreams(result.first)
                                    if (playableTracks.isEmpty()) return@collectLatest
                                    setQueueData(
                                        QueueData.Data(
                                            listTracks = playableTracks.toCollection(arrayListOf<Track>()),
                                            firstPlayedTrack = playableTracks.first(),
                                            playlistId = radioEndpoint.playlistId,
                                            playlistName = "\"${data.title}\" ${getString(Res.string.radio)}",
                                            playlistType = PlaylistType.RADIO,
                                            continuation = result.second,
                                        ),
                                    )
                                    loadMediaItem(
                                        playableTracks.first(),
                                        Config.RADIO_CLICK,
                                        0,
                                    )
                                }

                                else -> {
                                    makeToast(
                                        res.message ?: getString(Res.string.error),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            PlaylistUIEvent.Download -> {
                downloadFullPlaylist()
            }

            PlaylistUIEvent.Favorite -> {
                updatePlaylistLiked(!liked.value, data.id)
            }
        }
    }

    fun getFullTracks(callback: (List<Track>) -> Unit) {
        viewModelScope.launch {
            if (tracksListState.value == ListState.PAGINATION_EXHAUST) {
                _playlistEntity.value
                    ?.copy(
                        tracks = tracks.value.toListVideoId(),
                        trackCount = tracks.value.size,
                    )?.let {
                        playlistRepository.insertAndReplacePlaylist(it)
                    }
                callback(tracks.value)
            } else {
                val id = uiState.value.data?.id ?: return@launch
                tracksListState.collectLatest { state ->
                    if (state == ListState.PAGINATION_EXHAUST) {
                        _playlistEntity.value
                            ?.copy(
                                tracks = tracks.value.toListVideoId(),
                                trackCount = tracks.value.size,
                            )?.let {
                                playlistRepository.insertAndReplacePlaylist(it)
                            }
                        callback(tracks.value)
                    } else if (state == ListState.IDLE) {
                        getContinuationTrack(id, continuation.value)
                    }
                }
            }
        }
    }

    fun saveToLocal(tracks: List<Track>) {
        viewModelScope.launch {
            val data = uiState.value.data ?: return@launch
            localPlaylistRepository
                .syncYouTubePlaylistToLocalPlaylist(
                    data,
                    tracks,
                    getString(Res.string.synced),
                    getString(Res.string.error),
                ).collectLatestResource(
                    onSuccess = {
                        makeToast(it)
                    },
                    onLoading = {
                        makeToast(getString(Res.string.syncing))
                    },
                    onError = {
                        makeToast(it)
                    },
                )
        }
    }

    fun downloadFullPlaylist() {
        viewModelScope.launch {
            val id = playlistEntity.value?.id ?: return@launch
            makeToast(getString(Res.string.downloading))
            updatePlaylistDownloadState(id, STATE_DOWNLOADING)
            getFullTracks { tracks ->
                viewModelScope.launch {
                    tracks.forEach {
                        downloadUtils.downloadTrack(it.videoId, it.title, it.thumbnails?.lastOrNull()?.url ?: "")
                    }
                }
            }
        }
    }

    fun updatePlaylistTitle(
        title: String,
        id: String,
    ) {
        viewModelScope.launch {
            playlistRepository
                .updateYourYouTubePlaylistTitle(
                    id,
                    title,
                ).collect {
                    when (it) {
                        is Resource.Success -> {
                            getData(id)
                            makeToast(it.data ?: getString(Res.string.synced))
                        }

                        is Resource.Error -> {
                            makeToast(it.message ?: getString(Res.string.error))
                        }
                    }
                }
        }
    }

    fun deleteYourYouTubePlaylist(
        id: String,
        onDeleted: () -> Unit,
    ) {
        viewModelScope.launch {
            playlistRepository
                .deleteYourYouTubePlaylist(id)
                .collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            makeToast(getString(Res.string.playlist_deleted))
                            onDeleted()
                        }

                        is Resource.Error -> makeToast(result.message ?: getString(Res.string.error))
                    }
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        collectDownloadedJob?.cancel()
        playlistEntityJob?.cancel()
    }

    private data class CachedPlaylistSnapshot(
        val playlistState: PlaylistState,
        val tracks: List<Track>,
        val continuation: String?,
        val cachedAtMillis: Long,
        val membershipRevision: Long = 0L,
    )

    companion object {
        private const val LIKED_MUSIC_PLAYLIST_ID = "LM"
        private const val PLAYLIST_CACHE_TTL_MS = 5 * 60 * 1000L
        private const val MAX_CACHED_PLAYLISTS = 8
        private val playlistSnapshots = linkedMapOf<String, CachedPlaylistSnapshot>()

        internal fun invalidateCachedPlaylist(id: String) {
            val normalizedId = id.removePrefix("VL")
            playlistSnapshots.keys.removeAll { it.removePrefix("VL") == normalizedId }
        }

        private fun getCachedPlaylistSnapshot(id: String): CachedPlaylistSnapshot? =
            playlistSnapshots.remove(id)?.also { snapshot ->
                playlistSnapshots[id] = snapshot
            }

        private fun savePlaylistSnapshot(
            id: String,
            snapshot: CachedPlaylistSnapshot,
        ) {
            playlistSnapshots.remove(id)
            playlistSnapshots[id] = snapshot
            while (playlistSnapshots.size > MAX_CACHED_PLAYLISTS) {
                playlistSnapshots.remove(playlistSnapshots.keys.first())
            }
        }
    }
}

sealed class PlaylistUIState(
    val data: PlaylistState? = null,
    val message: String? = null,
) {
    data object Loading : PlaylistUIState()

    class Success(
        data: PlaylistState,
    ) : PlaylistUIState(
            data = data,
        )

    class Error(
        message: String? = null,
    ) : PlaylistUIState(
            message = message,
        )
}

data class PlaylistSuggestionState(
    val playlistId: String? = null,
    val reloadParams: String? = null,
    val tracks: List<Track> = emptyList(),
    val isLoading: Boolean = false,
    val hasLoaded: Boolean = false,
)

sealed class PlaylistUIEvent {
    data object PlayAll : PlaylistUIEvent()

    data object Shuffle : PlaylistUIEvent()

    data object StartRadio : PlaylistUIEvent()

    data class ItemClick(
        val videoId: String,
    ) : PlaylistUIEvent()

    data object Favorite : PlaylistUIEvent()

    data object Download : PlaylistUIEvent()
}

enum class ListState {
    IDLE,
    LOADING,
    PAGINATING,
    ERROR,
    PAGINATION_EXHAUST,
}
