package com.petedev.wavemusic.viewModel

import com.petedev.wavemusic.ui.navigation.destination.library.LibraryDestination

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageBitmapConfig
import androidx.compose.runtime.mutableStateMapOf
import com.petedev.wavemusic.utils.ByteBudgetCache
import androidx.lifecycle.viewModelScope
import com.petedev.wavemusic.common.Config
import com.petedev.wavemusic.common.Config.ALBUM_CLICK
import com.petedev.wavemusic.common.Config.DOWNLOAD_CACHE
import com.petedev.wavemusic.common.Config.PLAYLIST_CLICK
import com.petedev.wavemusic.common.Config.RECOVER_TRACK_QUEUE
import com.petedev.wavemusic.common.Config.SHARE
import com.petedev.wavemusic.common.Config.SONG_CLICK
import com.petedev.wavemusic.common.Config.VIDEO_CLICK
import com.petedev.wavemusic.common.SELECTED_LANGUAGE
import com.petedev.wavemusic.common.STATUS_DONE
import com.petedev.wavemusic.domain.data.entities.AlbumEntity
import com.petedev.wavemusic.domain.data.entities.DownloadState
import com.petedev.wavemusic.domain.data.entities.LocalPlaylistEntity
import com.petedev.wavemusic.domain.data.entities.LyricsEntity
import com.petedev.wavemusic.domain.data.entities.NewFormatEntity
import com.petedev.wavemusic.domain.data.entities.PlaylistEntity
import com.petedev.wavemusic.domain.data.entities.SongEntity
import com.petedev.wavemusic.domain.data.entities.SongInfoEntity
import com.petedev.wavemusic.domain.data.entities.TranslatedLyricsEntity
import com.petedev.wavemusic.domain.data.model.browse.album.Track
import com.petedev.wavemusic.domain.data.model.browse.playlist.PlaylistBrowse
import com.petedev.wavemusic.domain.data.model.download.DownloadProgress
import com.petedev.wavemusic.domain.data.model.intent.GenericIntent
import com.petedev.wavemusic.domain.data.model.metadata.Lyrics
import com.petedev.wavemusic.domain.data.model.streams.TimeLine
import com.petedev.wavemusic.domain.data.model.update.UpdateData
import com.petedev.wavemusic.domain.data.player.GenericCastState
import com.petedev.wavemusic.domain.extension.activeVideoId
import com.petedev.wavemusic.domain.extension.decodeHtmlEntities
import com.petedev.wavemusic.domain.extension.isSong
import com.petedev.wavemusic.domain.extension.isVideo
import com.petedev.wavemusic.domain.extension.toGenericMediaItem
import com.petedev.wavemusic.domain.manager.DataStoreManager
import com.petedev.wavemusic.domain.manager.LiquidGlassIntensity
import com.petedev.wavemusic.domain.data.model.lyrics.RomanizationLanguage
import com.petedev.wavemusic.domain.manager.DataStoreManager.Values.FALSE
import com.petedev.wavemusic.domain.manager.DataStoreManager.Values.TRUE
import com.petedev.wavemusic.domain.mediaservice.handler.ControlState
import com.petedev.wavemusic.domain.mediaservice.handler.DownloadHandler
import com.petedev.wavemusic.domain.mediaservice.handler.NowPlayingTrackState
import com.petedev.wavemusic.domain.mediaservice.handler.PlayerEvent
import com.petedev.wavemusic.domain.mediaservice.handler.PlaylistType
import com.petedev.wavemusic.domain.mediaservice.handler.QueueData
import com.petedev.wavemusic.domain.mediaservice.handler.RepeatState
import com.petedev.wavemusic.domain.mediaservice.handler.SimpleMediaState
import com.petedev.wavemusic.domain.mediaservice.handler.SleepTimerState
import com.petedev.wavemusic.domain.repository.AlbumRepository
import com.petedev.wavemusic.domain.repository.CacheRepository
import com.petedev.wavemusic.domain.repository.LocalPlaylistRepository
import com.petedev.wavemusic.domain.repository.LyricsCanvasRepository
import com.petedev.wavemusic.domain.repository.PlaylistRepository
import com.petedev.wavemusic.domain.repository.SongRepository
import com.petedev.wavemusic.domain.repository.StreamRepository
import com.petedev.wavemusic.domain.repository.UpdateRepository
import com.petedev.wavemusic.domain.utils.Resource
import com.petedev.wavemusic.domain.utils.LocalResource
import com.petedev.wavemusic.domain.utils.LiveStreamType
import com.petedev.wavemusic.domain.utils.toListName
import com.petedev.wavemusic.domain.utils.toLyrics
import com.petedev.wavemusic.domain.utils.toLyricsEntity
import com.petedev.wavemusic.domain.utils.toPlaylistEntity
import com.petedev.wavemusic.domain.utils.toSongEntity
import com.petedev.wavemusic.domain.utils.toSyncedLyrics
import com.petedev.wavemusic.domain.utils.toTrack
import com.petedev.wavemusic.logger.LogLevel
import com.petedev.wavemusic.logger.Logger
import com.petedev.wavemusic.Platform
import com.petedev.wavemusic.expect.getDownloadFolderPath
import com.petedev.wavemusic.expect.ui.toByteArray
import com.petedev.wavemusic.getPlatform
import com.petedev.wavemusic.utils.VersionManager
import com.petedev.wavemusic.viewModel.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.lastOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.yield
import org.koin.core.component.inject
import org.jetbrains.compose.resources.getString as getComposeString
import com.petedev.wavemusic.lastfm.completeLogin
import com.petedev.wavemusic.resources.Res
import com.petedev.wavemusic.resources.added_to_queue
import com.petedev.wavemusic.resources.error
import com.petedev.wavemusic.resources.lastfm_login_failed
import com.petedev.wavemusic.resources.login_success
import com.petedev.wavemusic.resources.play_next
import com.petedev.wavemusic.resources.shared
import com.petedev.wavemusic.resources.updated
import com.petedev.wavemusic.resources.view_count
import com.petedev.wavemusic.resources.vote_submitted
import java.io.FileOutputStream
import kotlin.math.abs
import kotlin.reflect.KClass

// DataStore key for the kotlin-footguns star prompt. Cleared on every version bump,
// so the ask comes back after an update - same policy as OPEN_APP_TIME.
const val FOOTGUNS_STAR_KEY = "footguns_starred"
const val SONG_LYRICS_PROVIDER_KEY_PREFIX = "lyrics_provider_song:"
private const val SUPPRESSED_YOUTUBE_LIKES_KEY = "suppressed_youtube_likes"
const val PODCAST_RESUME_POINTS_KEY = "podcast_resume_points_v1"
const val DISMISSED_CONTINUE_LISTENING_EPISODES_KEY = "dismissed_continue_listening_episodes_v1"
private const val YOUTUBE_LIKED_PLAYLIST_REFRESH_INTERVAL_MILLIS = 5 * 60 * 1_000L

data class YouTubeLikedPlaylistRefresh(
    val revision: Int,
    val videoId: String,
    val isLiked: Boolean,
)

@OptIn(ExperimentalCoroutinesApi::class)
class SharedViewModel(
    private val dataStoreManager: DataStoreManager,
    private val streamRepository: StreamRepository,
    private val updateRepository: UpdateRepository,
    private val songRepository: SongRepository,
    private val albumRepository: AlbumRepository,
    private val localPlaylistRepository: LocalPlaylistRepository,
    private val playlistRepository: PlaylistRepository,
) : BaseViewModel() {
    private val continueListeningDismissMutex = Mutex()

    /** Hide a suggestion independently of its saved position, download, or favorite status. */
    fun dismissContinueListeningEpisode(videoId: String) {
        if (videoId.isBlank()) return
        viewModelScope.launch {
            continueListeningDismissMutex.withLock {
                val dismissed = dataStoreManager.getString(DISMISSED_CONTINUE_LISTENING_EPISODES_KEY)
                    .firstOrNull().orEmpty().split(';').filter { it.isNotBlank() }.toMutableSet()
                if (dismissed.add(videoId)) {
                    dataStoreManager.putString(DISMISSED_CONTINUE_LISTENING_EPISODES_KEY, dismissed.joinToString(";"))
                }
            }
        }
    }

    /** Keep artist thumbnails loaded by Library across bottom-tab screen disposal. */
    val libraryArtistThumbnailCache = mutableStateMapOf<String, String>()

    private val cachedArtworkBitmaps = ByteBudgetCache<String, ImageBitmap>(
        maxEntries = 6,
        maxBytes = (if (com.petedev.wavemusic.data.dataStore.prefersReducedVisualEffects()) 8L else 24L) * 1024L * 1024L,
    ) { bitmap ->
        bitmap.width.toLong() * bitmap.height * if (bitmap.config == ImageBitmapConfig.F16) 8L else 4L
    }

    var showedUpdateDialog: Boolean = false

    // CacheRepository eagerly opens Media3's on-disk caches. Resolve it only when the deferred
    // restore check runs, so constructing the root ViewModel doesn't do player/cache I/O before
    // the first screen is drawn.
    private val cacheRepository: CacheRepository by inject()
    // Lyrics services bring several HTTP clients and parsing helpers. Resolve them only when a
    // track actually needs lyrics instead of building that feature's object graph before Home.
    private val lyricsCanvasRepository: LyricsCanvasRepository by inject()

    private val _isCheckingUpdate = MutableStateFlow(false)
    val isCheckingUpdate: StateFlow<Boolean> = _isCheckingUpdate

    private var _liked: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val liked: SharedFlow<Boolean> = _liked.asSharedFlow()

    var isServiceRunning: Boolean = false

    private var _sleepTimerState = MutableStateFlow(SleepTimerState(false, 0))
    val sleepTimerState: StateFlow<SleepTimerState> = _sleepTimerState

    private var _format: MutableStateFlow<NewFormatEntity?> = MutableStateFlow(null)
    val format: SharedFlow<NewFormatEntity?> = _format.asSharedFlow()

    /**
     * Which extractor and cipher decoder produced the current track's URLs, shown in the info sheet.
     *
     * Read alongside the format rather than stored with it: the format row is cached and reused,
     * while this describes the extraction that happened in this run of the app.
     */
    private val _extractSource: MutableStateFlow<String?> = MutableStateFlow(null)
    val extractSource: StateFlow<String?> = _extractSource.asStateFlow()

    private var lyricsJob: Job? = null
    private var lyricsJobVideoId: String? = null
    private var manualLyricsTranslationJob: Job? = null
    private val completedLyricsCache = MutableStateFlow<Map<CompletedLyricsCacheKey, NowPlayingScreenData.LyricsData>>(emptyMap())

    private fun cacheLyrics(videoId: String, data: NowPlayingScreenData.LyricsData) {
        if (data.lyrics.error || data.lyrics.lines.isNullOrEmpty()) return
        viewModelScope.launch {
            val key = completedLyricsCacheKey(videoId)
            completedLyricsCache.update { cached ->
                val existing = cached[key]
                val result = if (data.translatedLyrics == null && existing?.lyrics == data.lyrics) {
                    data.copy(translatedLyrics = existing.translatedLyrics)
                } else data
                LinkedHashMap(cached).apply {
                    remove(key)
                    put(key, result)
                    while (size > 32) remove(keys.first())
                }
            }
        }
    }

    private suspend fun completedLyricsCacheKey(videoId: String): CompletedLyricsCacheKey =
        CompletedLyricsCacheKey(
            videoId = videoId,
            mainProvider = lyricsProviderFor(videoId),
            translationLanguage = dataStoreManager.translationLanguage.first(),
        )

    private suspend fun lyricsProviderFor(videoId: String): String =
        dataStoreManager
            .getString(SONG_LYRICS_PROVIDER_KEY_PREFIX + videoId)
            .firstOrNull()
            ?.takeIf {
                it in setOf(
                    DataStoreManager.SIMPMUSIC,
                    DataStoreManager.YOUTUBE,
                    DataStoreManager.LRCLIB,
                    DataStoreManager.BETTER_LYRICS,
                    DataStoreManager.BETTER_LYRICS_PORTATO,
                    DataStoreManager.YOULYPLUS,
                    DataStoreManager.KUGOU,
                    DataStoreManager.UNISON,
                )
            } ?: if (dataStoreManager.prioritizeWordSyncedLyrics.first() == TRUE) {
                DataStoreManager.AUTO_WORD_SYNCED_LYRICS
            } else {
                DataStoreManager.SIMPMUSIC
            }

    private val _intent: MutableStateFlow<GenericIntent?> = MutableStateFlow(null)
    val intent: StateFlow<GenericIntent?> = _intent

    private val _showNotificationPermissionDialog = MutableStateFlow(false)
    val showNotificationPermissionDialog: StateFlow<Boolean> = _showNotificationPermissionDialog


    // One-shot: the Desktop capsule asks the Now Playing panel, which hosts the page, to open
    // full-screen lyrics. The panel consumes it once shown.
    private val _fullscreenLyricsRequest = MutableStateFlow(false)
    val fullscreenLyricsRequest: StateFlow<Boolean> = _fullscreenLyricsRequest

    fun requestFullscreenLyrics() {
        _fullscreenLyricsRequest.value = true
    }

    fun consumeFullscreenLyricsRequest() {
        _fullscreenLyricsRequest.value = false
    }

    private var getFormatFlowJob: Job? = null

    var playlistId: MutableStateFlow<String?> = MutableStateFlow(null)

    var isFullScreen: Boolean = false

    private var _nowPlayingState = MutableStateFlow<NowPlayingTrackState?>(null)
    val nowPlayingState: StateFlow<NowPlayingTrackState?> = _nowPlayingState

    fun getQueueDataState() = mediaPlayerHandler.queueData

    val castState: StateFlow<GenericCastState> get() = mediaPlayerHandler.castState

    private var _controllerState =
        MutableStateFlow<ControlState>(
            ControlState(
                isPlaying = false,
                isShuffle = false,
                repeatState = RepeatState.None,
                isLiked = false,
                isNextAvailable = false,
                isPreviousAvailable = false,
                isCrossfading = false,
                volume = 1f,
            ),
        )
    val controllerState: StateFlow<ControlState> = _controllerState
    private var _timeline =
        MutableStateFlow<TimeLine>(
            TimeLine(
                current = -1L,
                total = -1L,
                bufferedPercent = 0,
                loading = true,
            ),
        )
    val timeline: StateFlow<TimeLine> = _timeline

    private var _nowPlayingScreenData =
        MutableStateFlow<NowPlayingScreenData>(
            NowPlayingScreenData.initial(),
        )
    val nowPlayingScreenData: StateFlow<NowPlayingScreenData> = _nowPlayingScreenData

    private val _lyricsTranslationRequestState = MutableStateFlow(LyricsTranslationRequestState())
    val lyricsTranslationRequestState: StateFlow<LyricsTranslationRequestState> = _lyricsTranslationRequestState.asStateFlow()

    private var _likeStatus = MutableStateFlow<Boolean>(false)
    val likeStatus: StateFlow<Boolean> = _likeStatus
    // Lets a visible YouTube Liked Music playlist insert the changed track immediately, then
    // reconcile with YouTube's playlist response.
    private val _youTubeLikedPlaylistRefresh =
        MutableStateFlow(YouTubeLikedPlaylistRefresh(0, "", false))
    val youTubeLikedPlaylistRefresh: StateFlow<YouTubeLikedPlaylistRefresh> =
        _youTubeLikedPlaylistRefresh
    private val _youTubeLikedSongs = MutableStateFlow<List<SongEntity>>(emptyList())
    val youTubeLikedSongs: StateFlow<List<SongEntity>> = _youTubeLikedSongs
    private val _youTubeLiveLikedVideoIds = MutableStateFlow<Set<String>>(emptySet())
    val youTubeLiveLikedVideoIds: StateFlow<Set<String>> = _youTubeLiveLikedVideoIds
    private val _suppressedYouTubeLikeIds = MutableStateFlow<Set<String>>(emptySet())
    private val _localPlaylistSyncRevision = MutableStateFlow(0)
    val localPlaylistSyncRevision: StateFlow<Int> = _localPlaylistSyncRevision.asStateFlow()

    /**
     * Which body of the Apple Music player was open last — held by ENUM NAME so this class stays
     * ignorant of the UI enum, which is internal to the player package.
     *
     * It cannot live in the composable: that player is inside a ModalBottomSheet, so dismissing
     * the sheet disposes the whole tree and takes any rememberSaveable with it — the tab snapped
     * back to the artwork every single time it was reopened. This class is a Koin `single`, so it
     * outlives the sheet while still resetting on app restart, which is the right lifetime for
     * "where I was a moment ago".
     */
    private val _lastPlayerViewTab = MutableStateFlow<String?>(null)
    val lastPlayerViewTab: StateFlow<String?> = _lastPlayerViewTab

    fun setLastPlayerViewTab(tabName: String) {
        _lastPlayerViewTab.value = tabName
    }

    val openAppTime: StateFlow<Int> = dataStoreManager.openAppTime.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), 0)
    private val _shareSavedLyrics: MutableStateFlow<Boolean> = MutableStateFlow(true)
    val shareSavedLyrics: StateFlow<Boolean> get() = _shareSavedLyrics

    private suspend fun fetchYouTubeLikedSongsFirstPage(): Pair<PlaylistBrowse, String?>? {
        val firstPage =
            playlistRepository
                .getPlaylistData("LM", getComposeString(Res.string.view_count))
                .firstOrNull { it is Resource.Success }
                ?.data ?: return null

        return firstPage
    }

    /** Restore YouTube's last saved ordering before local favorite timestamps can reorder those rows. */
    private suspend fun restoreCachedYouTubeLikedSongs() {
        val orderedVideoIds = playlistRepository.getPlaylist("LM").firstOrNull()?.tracks.orEmpty()
        if (orderedVideoIds.isEmpty()) return

        val cachedSongs = songRepository.getSongsByListVideoId(orderedVideoIds).firstOrNull().orEmpty()
        val suppressedIds = _suppressedYouTubeLikeIds.value
        _youTubeLiveLikedVideoIds.value =
            cachedSongs.filter { LiveStreamType.isLiveStream(it) }.mapTo(mutableSetOf()) { it.videoId }
        _youTubeLikedSongs.value = cachedSongs.filter {
            it.liked && it.videoId !in suppressedIds && !LiveStreamType.isLiveStream(it)
        }
    }

    /** Save the stable visible order so a later launch never has to infer it from favoriteAt. */
    private suspend fun persistYouTubeLikedPlaylistOrder(
        videoIds: List<String>,
        playlistBrowse: PlaylistBrowse? = null,
    ) {
        val existing = playlistRepository.getPlaylist("LM").firstOrNull()
        val updated =
            when {
                existing != null ->
                    existing.copy(
                        tracks = videoIds.distinct(),
                        trackCount = maxOf(existing.trackCount, playlistBrowse?.trackCount ?: 0, videoIds.size),
                    )
                playlistBrowse != null ->
                    playlistBrowse.copy(
                        tracks = playlistBrowse.tracks.filterNot { LiveStreamType.isLiveStream(it) },
                        trackCount = maxOf(playlistBrowse.trackCount, videoIds.size),
                    ).toPlaylistEntity().copy(tracks = videoIds.distinct())
                else -> return
            }
        playlistRepository.insertAndReplacePlaylist(updated)
    }

    private suspend fun loadSuppressedYouTubeLikeIds() {
        _suppressedYouTubeLikeIds.value =
            dataStoreManager
                .getString(SUPPRESSED_YOUTUBE_LIKES_KEY)
                .firstOrNull()
                .orEmpty()
                .split(',')
                .filter { it.isNotBlank() }
                .toSet()
    }

    private suspend fun setYouTubeLikeSuppressed(
        videoId: String,
        suppressed: Boolean,
    ) {
        val updated =
            if (suppressed) {
                _suppressedYouTubeLikeIds.value + videoId
            } else {
                _suppressedYouTubeLikeIds.value - videoId
            }
        _suppressedYouTubeLikeIds.value = updated
        dataStoreManager.putString(SUPPRESSED_YOUTUBE_LIKES_KEY, updated.joinToString(","))
    }

    /** Load YouTube likes progressively so Favorites can show the first page without waiting for
     * every continuation in a potentially large playlist to finish. */
    private suspend fun loadYouTubeLikedSongs(): Pair<List<SongEntity>, Boolean>? {
        val (firstPageBrowse, firstContinuation) = fetchYouTubeLikedSongsFirstPage() ?: return null
        val firstPageSongs = firstPageBrowse.tracks.map { it.toSongEntity().copy(liked = true) }.distinctBy { it.videoId }
        val suppressedIds = _suppressedYouTubeLikeIds.value
        var remoteSongs = firstPageSongs.distinctBy { it.videoId }.toMutableList()
        val seenVideoIds = remoteSongs.mapTo(mutableSetOf()) { it.videoId }
        val liveVideoIds = remoteSongs.filter { LiveStreamType.isLiveStream(it) }.mapTo(mutableSetOf()) { it.videoId }
        _youTubeLiveLikedVideoIds.value = liveVideoIds + _youTubeLiveLikedVideoIds.value
        val currentLikedIds = _youTubeLikedSongs.value.mapTo(mutableSetOf()) { it.videoId }
        val newlySeenFirstPageSongs = firstPageSongs.filter {
            it.videoId !in suppressedIds && !LiveStreamType.isLiveStream(it) && currentLikedIds.add(it.videoId)
        }
        val likedSongs = (newlySeenFirstPageSongs + _youTubeLikedSongs.value).toMutableList()
        // Keep known rows in their saved order. Only IDs never seen before are promoted to the
        // front, even when the service repeats an older song on its first page.
        _youTubeLikedSongs.value = likedSongs
        persistYouTubeLikedPlaylistOrder(likedSongs.map { it.videoId }, firstPageBrowse)
        // Cache even hidden broadcasts with their live flag. That marks an older local row too,
        // so the local/remote Favorites merge cannot resurrect it after filtering the remote list.
        songRepository.cacheYouTubeLikedSongs(remoteSongs)

        var continuation = firstContinuation
        var fullyLoaded = continuation.isNullOrBlank()
        val seenContinuations = mutableSetOf<String>()
        while (!continuation.isNullOrBlank() && seenContinuations.add(continuation)) {
            var page: Pair<ArrayList<Track>?, String?>? = null
            for (attempt in 0..2) {
                page = songRepository.getContinueTrack("LM", continuation, fromPlaylist = true).firstOrNull()
                if (page?.first != null) break
                if (attempt < 2) delay(500L * (attempt + 1))
            }
            val loadedPage = page ?: break
            val tracks = loadedPage.first ?: break
            val nextSongs = tracks.map { it.toSongEntity().copy(liked = true) }
            val newSongs = nextSongs.filter { seenVideoIds.add(it.videoId) }
            if (newSongs.isNotEmpty()) {
                remoteSongs.addAll(newSongs)
                val newLiveIds = newSongs.filter { LiveStreamType.isLiveStream(it) }.mapTo(mutableSetOf()) { it.videoId }
                if (newLiveIds.isNotEmpty()) {
                    liveVideoIds.addAll(newLiveIds)
                    _youTubeLiveLikedVideoIds.value = liveVideoIds.toSet()
                }
                val displayedIds = likedSongs.mapTo(mutableSetOf()) { it.videoId }
                likedSongs.addAll(newSongs.filterNot {
                    it.videoId in suppressedIds || LiveStreamType.isLiveStream(it) || it.videoId in displayedIds
                })
                _youTubeLikedSongs.value = likedSongs.toList()
                songRepository.cacheYouTubeLikedSongs(newSongs)
                persistYouTubeLikedPlaylistOrder(likedSongs.map { it.videoId }, firstPageBrowse)
            }

            val nextContinuation = loadedPage.second
            if (nextContinuation.isNullOrBlank()) {
                fullyLoaded = true
                break
            }
            if (nextContinuation == continuation && nextSongs.isEmpty()) {
                break
            }
            continuation = nextContinuation
            // Let foreground composition and input run between page fetches.
            yield()
        }
        _youTubeLikedSongs.value = likedSongs.toList()
        if (fullyLoaded) {
            // A complete playlist read confirms which removals reached YouTube. Keep suppressing
            // IDs still returned by the server (it may briefly serve a stale snapshot), and let
            // a later external re-like appear once the old like is confirmed gone.
            val remoteIds = remoteSongs.mapTo(mutableSetOf()) { it.videoId }
            val stillSuppressed = suppressedIds intersect remoteIds
            if (stillSuppressed != suppressedIds) {
                _suppressedYouTubeLikeIds.value = stillSuppressed
                dataStoreManager.putString(SUPPRESSED_YOUTUBE_LIKES_KEY, stillSuppressed.joinToString(","))
            }
        }
        return likedSongs.toList() to fullyLoaded
    }

    /** Refresh the newest YouTube likes without walking the entire playlist on every poll. */
    private suspend fun refreshRecentYouTubeLikedSongs() {
        val (newestBrowse, _) = fetchYouTubeLikedSongsFirstPage() ?: return
        val newestSongs = newestBrowse.tracks.map { it.toSongEntity().copy(liked = true) }.distinctBy { it.videoId }
        val currentSongs = _youTubeLikedSongs.value
        val suppressedIds = _suppressedYouTubeLikeIds.value
        val newestIds = newestSongs.mapTo(mutableSetOf()) { it.videoId }
        val newestLiveIds = newestSongs.filter { LiveStreamType.isLiveStream(it) }.mapTo(mutableSetOf()) { it.videoId }
        _youTubeLiveLikedVideoIds.value =
            newestLiveIds + _youTubeLiveLikedVideoIds.value.filterNot { it in newestIds }
        val visibleNewestSongs = newestSongs.filterNot {
            it.videoId in suppressedIds || LiveStreamType.isLiveStream(it)
        }
        val currentIds = currentSongs.mapTo(mutableSetOf()) { it.videoId }
        val newlyLikedSongs = visibleNewestSongs.filterNot { it.videoId in currentIds }.distinctBy { it.videoId }
        // Persist the Favorite flags before publishing new remote entries. Otherwise a fast tap on
        // a newly refreshed row can open its menu while the local SongEntity still says unliked.
        songRepository.cacheYouTubeLikedSongs(newestSongs)
        // A refreshed first page may contain songs that are already deeper in the current list.
        // Only new IDs belong at the front; refreshing must never promote known songs.
        _youTubeLikedSongs.value = newlyLikedSongs + currentSongs
        if (newlyLikedSongs.isNotEmpty()) {
            persistYouTubeLikedPlaylistOrder(_youTubeLikedSongs.value.map { it.videoId }, newestBrowse)
        }
        newlyLikedSongs.firstOrNull()?.let { newlyLikedSong ->
            _youTubeLikedPlaylistRefresh.update {
                YouTubeLikedPlaylistRefresh(it.revision + 1, newlyLikedSong.videoId, true)
            }
        }
    }

    private suspend fun updateCachedYouTubeLike(videoId: String, isLiked: Boolean) {
        setYouTubeLikeSuppressed(videoId, suppressed = !isLiked)
        val song =
            songRepository.getSongAsFlow(videoId).firstOrNull()
                ?: mediaPlayerHandler.nowPlayingState.value.songEntity?.takeIf { it.videoId == videoId }
                ?: _youTubeLikedSongs.value.firstOrNull { it.videoId == videoId }
        if (isLiked) {
            song?.let { songRepository.cacheYouTubeLikedSongs(listOf(it.copy(liked = true))) }
        } else {
            songRepository.setLikedLocally(videoId, liked = false)
        }
        val currentSongs = _youTubeLikedSongs.value
        val alreadyLiked = currentSongs.any { it.videoId == videoId }
        if (alreadyLiked != isLiked) {
            _youTubeLikedSongs.value =
                if (isLiked) {
                    listOfNotNull(song?.copy(liked = true)) + currentSongs
                } else {
                    currentSongs.filterNot { it.videoId == videoId }
                }
            _youTubeLikedPlaylistRefresh.update {
                YouTubeLikedPlaylistRefresh(it.revision + 1, videoId, isLiked)
            }
            persistYouTubeLikedPlaylistOrder(_youTubeLikedSongs.value.map { it.videoId })
        }
        if (mediaPlayerHandler.nowPlaying.value?.mediaId == videoId) {
            _likeStatus.value = isLiked
        }
    }

    init {
        viewModelScope.launch {
            log("SharedViewModel init")
            if (dataStoreManager.appVersion.first() != VersionManager.getVersionName()) {
                dataStoreManager.resetOpenAppTime()
                dataStoreManager.putString(FOOTGUNS_STAR_KEY, "false")
                dataStoreManager.setAppVersion(
                    VersionManager.getVersionName(),
                )
            }
            dataStoreManager.openApp()
            launch {
                combine(dataStoreManager.loggedIn, dataStoreManager.cookie) { loggedIn, cookie ->
                    (loggedIn == TRUE) to cookie
                }.distinctUntilChanged().collect { (isLoggedIn, cookie) ->
                    if (!isLoggedIn || cookie.isBlank()) return@collect
                    delay(5_000L)
                    if (dataStoreManager.loggedIn.first() != TRUE || dataStoreManager.cookie.first().isBlank()) return@collect

                    val pendingPlaylists =
                        localPlaylistRepository
                            .getAllLocalPlaylists()
                            .first()
                            .filter {
                                (it.syncState == LocalPlaylistEntity.YouTubeSyncState.PendingSync ||
                                    it.syncState == LocalPlaylistEntity.YouTubeSyncState.NotSynced) &&
                                    it.youtubePlaylistId.isNullOrBlank()
                            }
                    pendingPlaylists.forEach { playlist ->
                        try {
                            localPlaylistRepository.updateLocalPlaylistYouTubePlaylistSyncState(
                                playlist.id,
                                LocalPlaylistEntity.YouTubeSyncState.Syncing,
                            )
                            val result =
                                localPlaylistRepository
                                    .syncLocalPlaylistToYouTubePlaylist(
                                        playlistId = playlist.id,
                                        successMessage = "",
                                        errorMessage = "",
                                    ).lastOrNull()
                            if (result is LocalResource.Success) {
                                _localPlaylistSyncRevision.update { it + 1 }
                            } else {
                                localPlaylistRepository.updateLocalPlaylistYouTubePlaylistSyncState(
                                    playlist.id,
                                    LocalPlaylistEntity.YouTubeSyncState.PendingSync,
                                )
                                val error = (result as? LocalResource.Error)?.message ?: "Unknown error"
                                Logger.w(tag, "Deferred YouTube playlist sync failed for ${playlist.id}: $error")
                            }
                        } catch (cancelled: kotlinx.coroutines.CancellationException) {
                            throw cancelled
                        } catch (error: Exception) {
                            localPlaylistRepository.updateLocalPlaylistYouTubePlaylistSyncState(
                                playlist.id,
                                LocalPlaylistEntity.YouTubeSyncState.PendingSync,
                            )
                            Logger.w(tag, "Deferred YouTube playlist sync failed for ${playlist.id}: ${error.message}")
                        }
                    }
                }
            }
            // Podcast episode resume points are stored independently from song history so the
            // library can collect every unfinished episode across app launches.
            launch {
                while (true) {
                    delay(5_000)
                    val state = _nowPlayingState.value ?: continue
                    val track = state.track ?: continue
                    if (!track.category.equals("Podcast", ignoreCase = true) &&
                        !track.resultType.equals("Podcast", ignoreCase = true)
                    ) continue
                    val position = _timeline.value.current.coerceAtLeast(0L)
                    val duration = _timeline.value.total.takeIf { it > 0L }
                        ?: (track.durationSeconds?.toLong()?.times(1_000L) ?: 0L)
                    if (duration <= 0L || position <= 0L) continue
                    val entries = dataStoreManager.getString(PODCAST_RESUME_POINTS_KEY).firstOrNull()
                        .orEmpty()
                        .split(';')
                        .mapNotNull { value ->
                            val fields = value.split('|')
                            if (fields.size == 4) fields[0] to fields.drop(1) else null
                        }.toMap().toMutableMap()
                    if (position >= duration * 9 / 10) {
                        entries.remove(track.videoId)
                    } else {
                        entries[track.videoId] = listOf(position.toString(), duration.toString(), System.currentTimeMillis().toString())
                    }
                    dataStoreManager.putString(
                        PODCAST_RESUME_POINTS_KEY,
                        entries.entries.joinToString(";") { (id, values) -> "$id|${values.joinToString("|")}" },
                    )
                }
            }
            launch {
                combine(dataStoreManager.loggedIn, dataStoreManager.cookie) { loggedIn, cookie ->
                    (loggedIn == TRUE && cookie.isNotBlank()) to cookie
                }.distinctUntilChanged().collectLatest { (isLoggedIn, _) ->
                    _youTubeLikedSongs.value = emptyList()
                    _youTubeLiveLikedVideoIds.value = emptySet()
                    if (!isLoggedIn) return@collectLatest
                    loadSuppressedYouTubeLikeIds()
                    restoreCachedYouTubeLikedSongs()
                    delay(5_000L)

                    coroutineScope {
                        val localFavorites = songRepository.getLikedSongs().first()
                        suspend fun syncAllLikedSongs(): Boolean {
                            try {
                                val (remoteTracks, fullyLoaded) = loadYouTubeLikedSongs() ?: return false
                                val remoteIds = remoteTracks.mapTo(mutableSetOf()) { it.videoId }

                                // Mirror local favorites only after the complete remote list is known.
                                // This also runs after a retry recovers an incomplete initial sync.
                                if (fullyLoaded) {
                                    val suppressedIds = _suppressedYouTubeLikeIds.value
                                    for (song in localFavorites.filterNot {
                                        it.videoId in remoteIds || it.videoId in suppressedIds
                                    }) {
                                        songRepository.addToYouTubeLiked(song.videoId).collect { result ->
                                            Logger.d(tag, "Sync local favorite to YouTube Music: ${song.videoId} ($result)")
                                            if (result in 200..299) updateCachedYouTubeLike(song.videoId, true)
                                        }
                                    }
                                }
                                return fullyLoaded
                            } catch (cancelled: kotlinx.coroutines.CancellationException) {
                                throw cancelled
                            } catch (error: Exception) {
                                Logger.w(tag, "Full YouTube Music likes sync failed: ${error.message}")
                                return false
                            }
                        }
                        var likedPlaylistFullyLoaded = syncAllLikedSongs()

                        // Do not overlap a second YouTube request with the initial paginated read.
                        // YouTube has no push stream for Liked Music, so keep the periodic refresh
                        // after the first sync has settled.
                        launch {
                            while (true) {
                                delay(if (likedPlaylistFullyLoaded) YOUTUBE_LIKED_PLAYLIST_REFRESH_INTERVAL_MILLIS else 15_000L)
                                try {
                                    if (likedPlaylistFullyLoaded) {
                                        refreshRecentYouTubeLikedSongs()
                                    } else {
                                        // A failed page must not strand login sync at its first 100 songs.
                                        likedPlaylistFullyLoaded = syncAllLikedSongs()
                                    }
                                } catch (cancelled: kotlinx.coroutines.CancellationException) {
                                    throw cancelled
                                } catch (error: Exception) {
                                    Logger.w(tag, "Refresh YouTube Music likes failed: ${error.message}")
                                }
                            }
                        }
                    }
                }
            }
            launch {
                songRepository.observeYouTubeLikeChanges().collectLatest { (videoId, isLiked) ->
                    if (dataStoreManager.loggedIn.first() != TRUE) return@collectLatest
                    updateCachedYouTubeLike(videoId, isLiked)
                }
            }
            val timeLineJob =
                launch {
                    nowPlayingState
                        .filterNotNull()
                        .flatMapLatest { nowPlayingState ->
                            timeline.map { timeLine ->
                                Pair(timeLine, nowPlayingState)
                            }
                        }.distinctUntilChanged { old, new ->
                            old.first.total == new.first.total &&
                                old.second.songEntity?.videoId == new.second.songEntity?.videoId
                        }.collectLatest {
                            log("Timeline job ${(it.first.total.toString() + it.second.songEntity?.videoId).hashCode()}")
                            val nowPlaying = it.second
                            val timeline = it.first
                            if (timeline.total > 0 && nowPlaying.songEntity != null) {

                                nowPlaying.songEntity?.let { song ->
                                    val screenLyrics = nowPlayingScreenData.value
                                    val hasLyricsForCurrentTrack =
                                        screenLyrics.lyricsVideoId == song.videoId && screenLyrics.lyricsData != null
                                    if (!hasLyricsForCurrentTrack &&
                                        (lyricsJobVideoId != song.videoId || lyricsJob?.isActive != true)
                                    ) {
                                        Logger.w(tag, "Get lyrics from format for ${song.videoId}")
                                        // Timeline and player-state collectors can observe a track
                                        // change in either order. An old song's active lookup must
                                        // never block the new song's automatic lyrics request.
                                        lyricsJob?.cancel()
                                        lyricsJobVideoId = song.videoId
                                        lyricsJob = getLyricsFromFormat(
                                            nowPlaying.mediaItem.isVideo(),
                                            song,
                                            (timeline.total / 1000).toInt(),
                                        )
                                    }
                                }
                            }
                        }
                }
            val shareSavedLyricsJob =
                launch {
                    dataStoreManager.helpBuildLyricsDatabase.distinctUntilChanged().collectLatest {
                        _shareSavedLyrics.value = it == TRUE
                    }
                }
//            val controllerStateJob =
//                launch {
//                    controllerState.map { it.isLiked }.distinctUntilChanged().collectLatest {
//                        if (dataStoreManager.combineLocalAndYouTubeLiked.first() == TRUE) {
//                            nowPlayingState.value?.mediaItem?.mediaId?.let {
//                                getLikeStatus(it)
//                            }
//                        }
//                    }
//                }
            timeLineJob.join()
            shareSavedLyricsJob.join()
//            controllerStateJob.join()
        }

        viewModelScope.launch {
            // Match BaseViewModel's post-first-frame player subscription. This collector used to
            // resolve MediaPlayerHandler during SharedViewModel construction, which built ExoPlayer
            // on the UI thread before MainActivity's existing delayed service connection.
            delay(300)
            mediaPlayerHandler.nowPlayingState
                .distinctUntilChangedBy {
                    Triple(
                        it.activeVideoId(),
                        it.mediaItem,
                        it.track to it.songEntity?.let { song ->
                            Triple(song.videoId, song.title, song.thumbnails)
                        },
                    )
                }.collectLatest { state ->
                    Logger.w(tag, "NowPlayingState is $state")
                    lyricsJob?.cancel()
                    lyricsJob = null
                    lyricsJobVideoId = null
                    manualLyricsTranslationJob?.cancel()
                    manualLyricsTranslationJob = null
                    _lyricsTranslationRequestState.value = LyricsTranslationRequestState()
                    val activeVideoId = state.activeVideoId()
                    val memoryLyrics =
                        activeVideoId?.let { videoId ->
                            completedLyricsCache.value[completedLyricsCacheKey(videoId)]
                        }
                    // Resolve a saved source before publishing the new screen. Clearing first
                    // exposed a loading page even for fully downloaded, cached songs.
                    val restoredLyrics = memoryLyrics ?: activeVideoId?.takeIf { state.mediaItem.isSong() }?.let { id ->
                        lyricsCanvasRepository.getSavedLyrics(id).firstOrNull()
                            ?.takeUnless { it.error || it.lines.isNullOrEmpty() }
                            ?.toLyrics()?.let { saved ->
                                NowPlayingScreenData.LyricsData(
                                    lyrics = saved.copy(lines = saved.lines?.map { it.copy(words = decodeHtmlEntities(it.words)) }),
                                    lyricsProvider = LyricsProvider.OFFLINE,
                                )
                            }
                    }

                    // Seed the timeline from METADATA as soon as the track is known, instead of
                    // waiting for the player. SimpleMediaState.Ready carries a duration and only
                    // fires once the container is parsed; after a queue restore nothing plays, the
                    // position poll never starts (it runs only while isPlaying), and so nothing
                    // ever reports a length — which is why a restored queue showed no times at all.
                    // SongEntity has known the length since the song was first seen.
                    //
                    // Written on EVERY track change, not just when missing: leaving the old value
                    // in place would show the previous track's length over the new one.
                    // Order matters: metadata first because it is available immediately, then the
                    // player's own duration, and only -1 ("not known yet") when neither has one.
                    //
                    // Writing -1 whenever metadata is missing — which this did at first — makes
                    // every radio track and every first-time track flash --:-- on the clock until
                    // the container is parsed, because those rows have no durationSeconds stored.
                    // Asking the player before giving up covers exactly that case: on a normal
                    // track change it usually already knows.
                    val metadataDurationMs = (state.songEntity?.durationSeconds ?: 0).toLong() * 1000L
                    val seededTotal =
                        metadataDurationMs.takeIf { it > 0L }
                            ?: mediaPlayerHandler.getPlayerDuration().takeIf { it > 0L }
                            ?: -1L
                    _timeline.update {
                        it.copy(
                            current = 0L,
                            total = seededTotal,
                            bufferedPercent = 0,
                        )
                    }
                    val song = state.songEntity
                    val metadata = state.mediaItem.metadata
                    val title = song?.title?.takeIf { it.isNotBlank() } ?: metadata.title.orEmpty()
                    val artistName =
                        song?.artistName
                            ?.joinToString(", ")
                            ?.takeIf { it.isNotBlank() }
                            ?: metadata.artist.orEmpty()
                    val savedSongArtwork =
                        song
                            ?.takeIf { it.videoId == activeVideoId }
                            ?.thumbnails
                            ?.takeIf { it.isNotBlank() }
                    val queueTrackArtwork =
                        state.track
                            ?.takeIf { it.videoId == activeVideoId }
                            ?.thumbnails
                            ?.maxByOrNull { it.width * it.height }
                            ?.url
                    // Reset on every media identity, even when a queue/download source has not
                    // resolved a SongEntity yet. Otherwise its old ID rejects the valid artwork
                    // URL below, and the previous track's video flag can leave a black surface.
                    _nowPlayingScreenData.value =
                        NowPlayingScreenData(
                            nowPlayingTitle = title,
                            currentVideoId = activeVideoId,
                            artistName = artistName,
                            // Publish the video kind before the player state so the correct video
                            // surface is mounted on the first frame of playlist playback.
                            isVideo = state.mediaItem.isVideo(),
                            isExplicit = song?.isExplicit == true,
                            // Video metadata often carries the video thumbnail. Keep the
                            // matching song or queue track's artwork ahead of that fallback.
                            thumbnailURL =
                                savedSongArtwork
                                    ?: queueTrackArtwork
                                    ?: metadata.artworkUri?.takeIf { it.isNotBlank() },
                            canvasData = null,
                            canvasResolved = true,
                            lyricsData = restoredLyrics,
                            lyricsLoading = song != null && restoredLyrics == null && state.mediaItem.isSong(),
                            lyricsVideoId = activeVideoId.takeIf { restoredLyrics != null },
                            songInfoData = null,
                            playlistName =
                                mediaPlayerHandler.queueData.value
                                    ?.data
                                    ?.playlistName ?: "",
                        )
                    // Start looking up lyrics as soon as a new song is published, rather than
                    // relying only on the timeline collector. On a cold launch the timeline
                    // can still be unknown for a while; waiting for it made uncached songs
                    // appear to need a manual refetch. The timeline collector below remains
                    // the fallback when duration metadata is not available yet.
                    // A restored source can still need its saved translation or text migration.
                    // Enrich it in place; do not clear the already usable lyrics.
                    if (song != null && (restoredLyrics == null || restoredLyrics.translatedLyrics == null) &&
                        state.mediaItem.isSong() &&
                        seededTotal > 0L
                    ) {
                        lyricsJobVideoId = song.videoId
                        lyricsJob =
                            getLyricsFromFormat(
                                state.mediaItem.isVideo(),
                                song,
                                (seededTotal / 1000L).toInt(),
                            )
                    }
                    // Publish the new track only after its screen data has been reset/restored.
                    // The timeline collector can start lyric loading as soon as this StateFlow
                    // changes; publishing earlier let its result land on the old screen data and
                    // then get overwritten here, so a cached translation appeared only on retry.
                    _nowPlayingState.value = state

                    state.mediaItem.let { now ->
                        getLikeStatus(now.mediaId)
                        getSongInfo(now.mediaId)
                        getFormat(now.mediaId)
                        _nowPlayingScreenData.update {
                            it.copy(
                                isVideo = now.isVideo(),
                            )
                        }
                    }
                    state.songEntity?.let { song ->
                        _liked.value = song.liked == true
                        _nowPlayingScreenData.update {
                            it.copy(
                                isExplicit = song.isExplicit,
                            )
                        }
                    }
                }
        }
        viewModelScope.launch {
            val job1 =
                launch {
                    mediaPlayerHandler.simpleMediaState.collect { mediaState ->
                        when (mediaState) {
                            is SimpleMediaState.Buffering -> {
                                _timeline.update {
                                    it.copy(
                                        loading = true,
                                    )
                                }
                            }

                            SimpleMediaState.Initial -> {
                                _timeline.update { it.copy(loading = true) }
                            }

                            SimpleMediaState.Ended -> {
                                // Park at the end of the track rather than at -1. The only formatter
                                // for these fields renders any negative as "--:--", and nothing here
                                // is guaranteed to follow: at the end of the queue the player simply
                                // stays ended, so a -1 written here stays on screen. Worse, the
                                // Progress branch below ignores negative values and the Loading
                                // branch restores `total` without touching `current`, which is how
                                // the player ends up showing a correct duration next to "--:--".
                                _timeline.update {
                                    it.copy(
                                        current = it.total.coerceAtLeast(0L),
                                        bufferedPercent = 0,
                                        loading = false,
                                    )
                                }
                            }

                            is SimpleMediaState.Progress -> {
                                if (mediaState.progress >= 0L && mediaState.progress != _timeline.value.current) {
                                    if (_timeline.value.total > 0L) {
                                        _timeline.update {
                                            it.copy(
                                                total = mediaPlayerHandler.getPlayerDuration().takeIf { d -> d > 0L } ?: it.total,
                                                current = mediaState.progress,
                                                loading = false,
                                            )
                                        }
                                    } else {
                                        _timeline.update {
                                            it.copy(
                                                current = mediaState.progress,
                                                loading = true,
                                                total = mediaPlayerHandler.getPlayerDuration().takeIf { d -> d > 0L } ?: it.total,
                                            )
                                        }
                                    }
                                }
                                // When progress hasn't changed (same value polled again) or is negative,
                                // don't modify loading state. The loading flag is already managed by
                                // Buffering/Ready/Loading state events. Setting loading=true here would
                                // cause rapid flickering whenever the same value arrives twice,
                                // which it can: the handler's ticker and the adapter's position
                                // poll both run at 50ms and are not in step, so a poll is sometimes
                                // read twice. A repeat is not evidence of a stall, and the loading
                                // flag belongs to the Buffering/Ready events rather than to a
                                // guess made here.
                            }

                            is SimpleMediaState.Loading -> {
                                _timeline.update {
                                    it.copy(
                                        // Keep the metadata duration when a loading event reports
                                        // the player's unknown-duration sentinel. This keeps the
                                        // timeline stable and allows a valid seek during loading.
                                        bufferedPercent = mediaState.bufferedPercentage.coerceIn(0, 100),
                                        total = mediaState.duration.takeIf { duration -> duration > 0L } ?: it.total,
                                        loading = true,
                                    )
                                }
                            }

                            is SimpleMediaState.Ready -> {
                                _timeline.update {
                                    it.copy(
                                        current = mediaPlayerHandler.getProgress(),
                                        loading = false,
                                        // The player's own duration wins, but ONLY when it has one.
                                        // ExoPlayer answers C.TIME_UNSET (a large negative, not
                                        // null) until it has parsed the container, and Ready is
                                        // also published from onIsLoadingChanged — which fires
                                        // before STATE_READY. Writing that would throw away the
                                        // metadata duration seeded on the track change above.
                                        total = mediaState.duration.takeIf { d -> d > 0L } ?: it.total,
                                    )
                                }
                            }
                        }
                    }
                }
            val controllerJob =
                launch {
                    Logger.w(tag, "ControllerJob is running")
                    mediaPlayerHandler.controlState.collectLatest {
                        Logger.w(tag, "ControlState is $it")
                        _controllerState.value = it
                        // Propagate crossfade state to timeline so UI can react
                        _timeline.update { timeline ->
                            timeline.copy(isCrossfading = it.isCrossfading)
                        }
                    }
                }
            val sleepTimerJob =
                launch {
                    mediaPlayerHandler.sleepTimerState.collectLatest {
                        _sleepTimerState.value = it
                    }
                }
            val playlistNameJob =
                launch {
                    mediaPlayerHandler.queueData.collectLatest {
                        _nowPlayingScreenData.update {
                            it.copy(playlistName = it.playlistName)
                        }
                    }
                }
            job1.join()
            controllerJob.join()
            sleepTimerJob.join()
            playlistNameJob.join()
        }
        // These recovery scans touch three repositories; delay them until the first screen settles.
        viewModelScope.launch {
            delay(20_000L)
            checkAllDownloadingSongs()
            checkAllDownloadingPlaylists()
            checkAllDownloadingLocalPlaylists()
        }
    }

    fun setIntent(intent: GenericIntent?) {
        _intent.value = intent
    }

    /**
     * Finishes a Last.fm login from the `wordbyword://lastfm-auth` callback.
     *
     * It lands here, and not on the login screen, because the callback arrives at the app rather
     * than at any one screen: the user left for their browser, and the screen they left from may
     * not even exist any more if the process was killed. Routing the token onwards through
     * navigation would push a second copy of the login screen on top of the one already open.
     *
     * The screen learns it succeeded by watching the stored session key, not by being told.
     */
    fun completeLastfmLogin(token: String) {
        if (token.isEmpty()) return
        viewModelScope.launch {
            val session = completeLogin(token)
            if (session != null) {
                dataStoreManager.setLastfmSession(
                    sessionKey = session.sessionKey,
                    username = session.username,
                )
                makeToast(getString(Res.string.login_success))
            } else {
                makeToast(getString(Res.string.lastfm_login_failed))
            }
        }
    }

    fun showNotificationPermissionDialog() {
        _showNotificationPermissionDialog.value = true
    }

    fun dismissNotificationPermissionDialog(doNotShowAgain: Boolean) {
        _showNotificationPermissionDialog.value = false
        if (doNotShowAgain) {
            putString("notification_permission_do_not_ask", "true")
        }
    }

    private fun getLikeStatus(videoId: String?) {
        viewModelScope.launch {
            if (videoId != null) {
                if (videoId in _suppressedYouTubeLikeIds.value) {
                    _likeStatus.value = false
                    return@launch
                }
                _likeStatus.value = false
                songRepository.getLikeStatus(videoId).collectLatest { status ->
                    _likeStatus.value = status
                }
            }
        }
    }

    fun getString(key: String): String? = runBlocking { dataStoreManager.getString(key).first() }

    fun putString(
        key: String,
        value: String,
    ) {
        runBlocking { dataStoreManager.putString(key, value) }
    }

    fun setSleepTimer(minutes: Int) {
        mediaPlayerHandler.sleepStart(minutes)
    }

    fun stopSleepTimer() {
        mediaPlayerHandler.sleepStop()
    }

    private var _downloadState: MutableStateFlow<DownloadHandler.Download?> = MutableStateFlow(null)
    var downloadState: StateFlow<DownloadHandler.Download?> = _downloadState.asStateFlow()
    private var restoringDownloadsJob: Job? = null

    fun checkIsRestoring() {
        if (restoringDownloadsJob?.isActive == true) return
        // This startup reconciliation can walk every downloaded track and playlist. Keep the
        // cache scan, database reads, and per-item repair writes off Main and out of the initial
        // startup window. Query Room first so users with no downloads never open cache indexes.
        restoringDownloadsJob = viewModelScope.launch(Dispatchers.IO) {
            delay(15_000L)
            val downloadedSongs = songRepository.getDownloadedSongs().first().orEmpty()
            val downloadedPlaylists = playlistRepository.getAllDownloadedPlaylist().first()
            if (downloadedSongs.isEmpty() && downloadedPlaylists.isEmpty()) return@launch

            val downloadedCacheKeys = cacheRepository.getAllCacheKeys(DOWNLOAD_CACHE).toHashSet()
            downloadedSongs.forEach { song ->
                if (!downloadedCacheKeys.contains(song.videoId)) {
                    songRepository.updateDownloadState(
                        song.videoId,
                        DownloadState.STATE_NOT_DOWNLOADED,
                    )
                }
            }
            for (data in downloadedPlaylists) {
                when (data) {
                    is AlbumEntity -> {
                        val tracks = data.tracks ?: emptyList()
                        if (tracks.isEmpty() || !downloadedCacheKeys.containsAll(tracks)) {
                            albumRepository.updateAlbumDownloadState(
                                data.browseId,
                                DownloadState.STATE_NOT_DOWNLOADED,
                            )
                        }
                    }

                    is PlaylistEntity -> {
                        val tracks = data.tracks ?: emptyList()
                        if (tracks.isEmpty() || !downloadedCacheKeys.containsAll(tracks)) {
                            playlistRepository.updatePlaylistDownloadState(
                                data.id,
                                DownloadState.STATE_NOT_DOWNLOADED,
                            )
                        }
                    }

                    is LocalPlaylistEntity -> {
                        val tracks = data.tracks ?: emptyList()
                        if (tracks.isEmpty() || !downloadedCacheKeys.containsAll(tracks)) {
                            localPlaylistRepository.updateLocalPlaylistDownloadState(
                                DownloadState.STATE_NOT_DOWNLOADED,
                                data.id,
                            )
                        }
                    }
                }
            }
        }
    }

    fun insertLyrics(lyrics: LyricsEntity) {
        viewModelScope.launch {
            lyricsCanvasRepository.insertLyrics(lyrics)
        }
    }

    private suspend fun cacheBoundaryAwareLyrics(videoId: String, lyrics: Lyrics) {
        lyricsCanvasRepository.insertLyrics(lyrics.toLyricsEntity(videoId))
        if (!lyrics.error && !lyrics.lines.isNullOrEmpty()) {
            dataStoreManager.putString("lyrics_text_boundaries_v1_$videoId", "1")
        }
    }

    private suspend fun getSavedLyrics(track: Track): Boolean {
        val savedLyrics = lyricsCanvasRepository.getSavedLyrics(track.videoId).firstOrNull() ?: return false
        // Empty/error rows are failed lookup results, not a usable cache hit. Treating them as
        // cached suppresses the provider request and leaves the player blank until manual retry.
        if (savedLyrics.error || savedLyrics.lines.isNullOrEmpty()) return false
        val lyricsData = savedLyrics.toLyrics()
        Logger.d(tag, "Saved Lyrics $lyricsData")
        updateLyrics(
            track.videoId,
            track.durationSeconds ?: 0,
            lyricsData,
            false,
            LyricsProvider.OFFLINE,
        )
        val selectedProvider = lyricsProviderFor(track.videoId)
        // Old TTML caches contain spaces invented between timed syllables. Keep the saved
        // copy visible, but let the normal fetch replace it once with boundary-aware text.
        if (lyricsData.syncType == "RICH_SYNCED" &&
            selectedProvider in setOf(DataStoreManager.BETTER_LYRICS, DataStoreManager.AUTO_WORD_SYNCED_LYRICS) &&
            dataStoreManager.getString("lyrics_text_boundaries_v1_${track.videoId}").firstOrNull() != "1"
        ) {
            return false
        }
        val cachedTranslation =
            when (selectedProvider) {
                DataStoreManager.SIMPMUSIC -> {
                    lyricsCanvasRepository
                        .getSavedTranslatedLyrics(
                            track.videoId,
                            dataStoreManager.translationLanguage.first(),
                            LyricsProvider.SIMPMUSIC.name,
                        ).firstOrNull()
                }
                DataStoreManager.YOUTUBE -> {
                    lyricsCanvasRepository
                        .getSavedTranslatedLyrics(
                            track.videoId,
                            dataStoreManager.translationLanguage.first(),
                            LyricsProvider.YOUTUBE.name,
                        ).firstOrNull()
                }
                else -> null
            }?.takeUnless { it.error || it.lines.isNullOrEmpty() }
        if (cachedTranslation != null) {
            val provider =
                if (selectedProvider == DataStoreManager.SIMPMUSIC) LyricsProvider.SIMPMUSIC else LyricsProvider.YOUTUBE
            updateLyrics(
                track.videoId,
                0,
                cachedTranslation.toLyrics(),
                true,
                provider,
            )
        } else {
            // This checks the AI translation cache before it attempts a fresh translation.
            getAITranslationLyrics(track.videoId, lyricsData)
        }
        return true
    }

    fun loadSharedMediaItem(videoId: String) {
        viewModelScope.launch {
            val localSong = songRepository.getSongById(videoId).firstOrNull()
            if (localSong != null) {
                val track = localSong.toTrack()
                if (track.toGenericMediaItem().isVideo()) {
                    playVideo(track)
                } else {
                    mediaPlayerHandler.setQueueData(
                        QueueData.Data(
                            listTracks = arrayListOf(track),
                            firstPlayedTrack = track,
                            playlistId = "RDAMVM$videoId",
                            playlistName = getString(Res.string.shared),
                            playlistType = PlaylistType.RADIO,
                            continuation = null,
                        ),
                    )
                    loadMediaItemFromTrack(track, SONG_CLICK)
                }
            } else {
                streamRepository.getFullMetadata(videoId).collectLatest { response ->
                    val track = response.data
                    when (response) {
                        is Resource.Success if (track != null) -> {
                            if (track.toGenericMediaItem().isVideo()) {
                                playVideo(track)
                            } else {
                                mediaPlayerHandler.setQueueData(
                                    QueueData.Data(
                                        listTracks = arrayListOf(track),
                                        firstPlayedTrack = track,
                                        playlistId = "RDAMVM$videoId",
                                        playlistName = getString(Res.string.shared),
                                        playlistType = PlaylistType.RADIO,
                                        continuation = null,
                                    ),
                                )
                                loadMediaItemFromTrack(track, SONG_CLICK)
                            }
                        }

                        else -> {
                            log("Load shared media item error: ${response.message}", LogLevel.WARN)
                            makeToast("${getString(Res.string.error)}: ${response.message}")
                        }
                    }
                }
            }
        }
    }

    fun loadMediaItemFromTrack(
        track: Track,
        type: String,
        index: Int? = null,
    ) {
        viewModelScope.launch {
            // Resolve missing duration/live metadata before handing the item to the player. A
            // short timeout keeps playback usable if metadata lookup is unavailable or slow.
            val playbackTrack =
                if ((track.durationSeconds ?: 0) <= 0) {
                    withTimeoutOrNull(5_000L) {
                        streamRepository
                            .getFullMetadata(track.videoId)
                            .firstOrNull { it is Resource.Success }
                            ?.data
                    }?.let { metadata ->
                        track.copy(
                            duration = metadata.duration ?: track.duration,
                            durationSeconds = metadata.durationSeconds ?: track.durationSeconds,
                            isLiveVideo = metadata.isLiveVideo,
                        )
                    } ?: track
                } else {
                    track
                }
            mediaPlayerHandler.clearMediaItems()
            songRepository.insertSong(playbackTrack.toSongEntity()).lastOrNull()?.let {
                println("insertSong: $it")
                songRepository
                    .getSongById(playbackTrack.videoId)
                    .collect { songEntity ->
                        if (songEntity != null) {
                            Logger.w("Check like", "loadMediaItemFromTrack ${songEntity.liked}")
                            _liked.value = songEntity.liked
                        }
                    }
            }
            playbackTrack.durationSeconds?.let {
                songRepository.updateDurationSeconds(
                    it,
                    playbackTrack.videoId,
                )
            }
            withContext(Dispatchers.Main) {
                mediaPlayerHandler.addMediaItem(playbackTrack.toGenericMediaItem(), playWhenReady = type != RECOVER_TRACK_QUEUE)
            }

            when (type) {
                SONG_CLICK -> {
                    mediaPlayerHandler.getRelated(playbackTrack.videoId)
                }

                VIDEO_CLICK -> mediaPlayerHandler.getRelated(playbackTrack.videoId)

                SHARE -> {
                    mediaPlayerHandler.getRelated(playbackTrack.videoId)
                }

                PLAYLIST_CLICK -> {
                    if (index == null) {
//                                        fetchSourceFromQueue(downloaded = downloaded ?: 0)
                        loadPlaylistOrAlbum(index = 0)
                    } else {
//                                        fetchSourceFromQueue(index!!, downloaded = downloaded ?: 0)
                        loadPlaylistOrAlbum(index = index)
                    }
                }

                ALBUM_CLICK -> {
                    if (index == null) {
//                                        fetchSourceFromQueue(downloaded = downloaded ?: 0)
                        loadPlaylistOrAlbum(index = 0)
                    } else {
//                                        fetchSourceFromQueue(index!!, downloaded = downloaded ?: 0)
                        loadPlaylistOrAlbum(index = index)
                    }
                }
            }
        }
    }

    fun onUIEvent(uiEvent: UIEvent) =
        viewModelScope.launch {
            when (uiEvent) {
                UIEvent.Backward -> {
                    mediaPlayerHandler.onPlayerEvent(
                        PlayerEvent.Backward,
                    )
                }

                UIEvent.Forward -> {
                    mediaPlayerHandler.onPlayerEvent(PlayerEvent.Forward)
                }

                UIEvent.PlayPause -> {
                    mediaPlayerHandler.onPlayerEvent(
                        PlayerEvent.PlayPause,
                    )
                }

                UIEvent.Next -> {
                    mediaPlayerHandler.onPlayerEvent(PlayerEvent.Next)
                }

                UIEvent.Previous -> {
                    mediaPlayerHandler.onPlayerEvent(
                        PlayerEvent.Previous,
                    )
                }

                UIEvent.SkipToPrevious -> {
                    mediaPlayerHandler.onPlayerEvent(
                        PlayerEvent.SkipToPrevious,
                    )
                }

                UIEvent.Stop -> {
                    mediaPlayerHandler.onPlayerEvent(PlayerEvent.Stop)
                }

                is UIEvent.UpdateProgress -> {
                    mediaPlayerHandler.onPlayerEvent(
                        PlayerEvent.UpdateProgress(
                            newProgress = uiEvent.newProgress,
                            fallbackDurationMs = _timeline.value.total.takeIf { it > 0L },
                        ),
                    )
                }

                UIEvent.Repeat -> {
                    mediaPlayerHandler.onPlayerEvent(PlayerEvent.Repeat)
                }

                UIEvent.Shuffle -> {
                    mediaPlayerHandler.onPlayerEvent(PlayerEvent.Shuffle)
                }

                UIEvent.ToggleLike -> {
                    Logger.w(tag, "ToggleLike")
                    mediaPlayerHandler.onPlayerEvent(PlayerEvent.ToggleLike)
                }

                is UIEvent.UpdateVolume -> {
                    val newVolume = uiEvent.newVolume
                    // Apply to the player first: persisting to DataStore is a suspending disk write
                    // and must not sit between the user's gesture and the audible change.
                    mediaPlayerHandler.onPlayerEvent(PlayerEvent.UpdateVolume(newVolume))
                    dataStoreManager.setPlayerVolume(newVolume)
                }
            }
        }

    override fun onCleared() {
        Logger.w("Check onCleared", "onCleared")
    }

    private fun checkAllDownloadingLocalPlaylists() {
        viewModelScope.launch(Dispatchers.IO) {
            val updatedMessage = getComposeString(Res.string.updated)
            localPlaylistRepository.getAllDownloadingLocalPlaylists().collectLatest { playlists ->
                playlists.forEach { playlist ->
                    localPlaylistRepository.updateDownloadState(playlist.id, 0, successMessage = updatedMessage).lastOrNull()
                }
            }
        }
    }

    private fun checkAllDownloadingPlaylists() {
        viewModelScope.launch(Dispatchers.IO) {
            playlistRepository.getAllDownloadingPlaylist().collectLatest { list ->
                list.forEach { data ->
                    when (data) {
                        is AlbumEntity -> {
                            albumRepository.updateAlbumDownloadState(data.browseId, 0)
                        }

                        is PlaylistEntity -> {
                            playlistRepository.updatePlaylistDownloadState(data.id, 0)
                        }

                        else -> {
                            // Skip
                        }
                    }
                }
            }
        }
    }

    private fun checkAllDownloadingSongs() {
        viewModelScope.launch(Dispatchers.IO) {
            songRepository.getDownloadingSongs().collect { songs ->
                songs?.forEach { song ->
                    songRepository.updateDownloadState(
                        song.videoId,
                        DownloadState.STATE_NOT_DOWNLOADED,
                    )
                }
            }
            songRepository.getPreparingSongs().collect { songs ->
                songs.forEach { song ->
                    songRepository.updateDownloadState(
                        song.videoId,
                        DownloadState.STATE_NOT_DOWNLOADED,
                    )
                }
            }
        }
    }

    private fun getFormat(mediaId: String?) {
        if (mediaId != _format.value?.videoId && !mediaId.isNullOrEmpty()) {
            _format.value = null
            _extractSource.value = streamRepository.getExtractSource(mediaId)
            getFormatFlowJob?.cancel()
            getFormatFlowJob =
                viewModelScope.launch {
                    streamRepository.getFormatFlow(mediaId).cancellable().collectLatest { f ->
                        Logger.w(tag, "Get format for $mediaId: $f")
                        if (f != null) {
                            _format.emit(f)
                        } else {
                            _format.emit(null)
                        }
                        // Re-read on every emission: the first one usually lands before the
                        // extractor has finished, so the source is only known on a later pass.
                        _extractSource.value = streamRepository.getExtractSource(mediaId)
                    }
                }
        }
    }

    private var songInfoJob: Job? = null

    fun getSongInfo(mediaId: String?) {
        songInfoJob?.cancel()
        songInfoJob =
            viewModelScope.launch {
                if (mediaId != null) {
                    songRepository.getSongInfo(mediaId).collect { song ->
                        _nowPlayingScreenData.update {
                            it.copy(
                                songInfoData = song,
                            )
                        }
                    }
                }
            }
    }

    private var _updateResponse = MutableStateFlow<UpdateData?>(null)
    val updateResponse: StateFlow<UpdateData?> = _updateResponse
    private val _updateCheckRevision = MutableStateFlow(0)
    val updateCheckRevision: StateFlow<Int> = _updateCheckRevision

    fun checkForUpdate() {
        if (_isCheckingUpdate.value) return
        _isCheckingUpdate.value = true
        viewModelScope.launch {
            try {
                dataStoreManager.putString(
                    "CheckForUpdateAt",
                    System.currentTimeMillis().toString(),
                )
                updateRepository.checkForGithubReleaseUpdate().collectLatest { response ->
                    val data = response.data
                    when (response) {
                        is Resource.Success if (data != null) -> {
                            showedUpdateDialog = true
                            _updateResponse.value = data
                            // StateFlow suppresses equal releases; a manual recheck still needs a popup.
                            _updateCheckRevision.update { it + 1 }
                        }

                        else -> {
                            log("Check for update error: ${response.message}", LogLevel.WARN)
                        }
                    }
                }
            } finally {
                _isCheckingUpdate.value = false
            }
        }
    }

    fun stopPlayer() {
        _nowPlayingScreenData.value = NowPlayingScreenData.initial()
        _nowPlayingState.value = null
        mediaPlayerHandler.resetSongAndQueue()
        onUIEvent(UIEvent.Stop)
    }

    private fun loadPlaylistOrAlbum(index: Int? = null) {
        mediaPlayerHandler.loadPlaylistOrAlbum(index)
    }

    private var translateAfterLyricsRefetchVideoId: String? = null

    private fun updateLyrics(
        videoId: String,
        duration: Int, // 0 if translated lyrics
        inputLyrics: Lyrics?,
        isTranslatedLyrics: Boolean,
        lyricsProvider: LyricsProvider = LyricsProvider.SIMPMUSIC,
    ) {
        // A cancelled lookup can finish after a fast skip, including an empty result.
        if (_nowPlayingScreenData.value.currentVideoId != videoId) return
        if (inputLyrics == null) {
            _nowPlayingScreenData.update {
                it.copy(
                    lyricsData = null,
                    lyricsVideoId = null,
                    lyricsLoading = false,
                )
            }
            return
        }

        val lyrics =
            inputLyrics.copy(
                lines =
                    inputLyrics.lines?.map { line ->
                        line.copy(
                            words = decodeHtmlEntities(line.words),
                        )
                    },
            )

        val shouldSendLyricsToSimpMusic =
            runBlocking {
                dataStoreManager.helpBuildLyricsDatabase.first() == TRUE
            } &&
                lyricsProvider != LyricsProvider.SIMPMUSIC
        if (_nowPlayingState.value?.songEntity?.videoId == videoId) {
            val track = _nowPlayingState.value?.track
            when (isTranslatedLyrics) {
                true -> {
                    if (lyricsProvider == LyricsProvider.SIMPMUSIC) {
                        _translatedVoteState.value =
                            VoteData(
                                id = lyrics.simpMusicLyrics?.id ?: "",
                                vote = lyrics.simpMusicLyrics?.vote ?: 0,
                                state = VoteState.Idle,
                            )
                    }
                    if (!lyrics.error && !lyrics.lines.isNullOrEmpty()) {
                        viewModelScope.launch {
                            val language =
                                if (lyricsProvider == LyricsProvider.YOUTUBE) {
                                    dataStoreManager.translationLanguage.first()
                                } else {
                                    dataStoreManager.translationLanguage.first()
                                }
                            lyricsCanvasRepository.insertTranslatedLyrics(
                                TranslatedLyricsEntity(
                                    videoId = videoId,
                                    language = language,
                                    error = false,
                                    lines = lyrics.lines,
                                    syncType = lyrics.syncType,
                                    provider = lyricsProvider.name,
                                    simpMusicLyricsId = lyrics.simpMusicLyrics?.id,
                                    simpMusicLyricsVote = lyrics.simpMusicLyrics?.vote,
                                ),
                            )
                        }
                    }
                    var completedLyricsData: NowPlayingScreenData.LyricsData? = null
                    _nowPlayingScreenData.update { screenData ->
                        val updatedLyricsData =
                            screenData.lyricsData?.copy(
                                translatedLyrics = lyrics to lyricsProvider,
                            )
                        completedLyricsData = updatedLyricsData
                        screenData.copy(
                            lyricsData = updatedLyricsData,
                            lyricsVideoId = videoId,
                        )
                    }
                    completedLyricsData?.let { dataToCache ->
                        if (!lyrics.error && !lyrics.lines.isNullOrEmpty()) {
                            cacheLyrics(videoId, dataToCache)
                        }
                    }
                    if (shouldSendLyricsToSimpMusic && track != null) {
                        viewModelScope.launch {
                            lyricsCanvasRepository
                                .insertSimpMusicTranslatedLyrics(
                                    dataStoreManager,
                                    track,
                                    lyrics,
                                    dataStoreManager.translationLanguage.first(),
                                ).collect {
                                    when (it) {
                                        is Resource.Error -> {
                                            log("Insert SimpMusic Translated Lyrics Error ${it.message}")
                                        }

                                        is Resource.Success -> {
                                            log("Insert SimpMusic Translated Lyrics Success")
                                        }
                                    }
                                }
                        }
                    }
                }

                false -> {
                    if (lyricsProvider == LyricsProvider.SIMPMUSIC) {
                        _lyricsVoteState.value =
                            VoteData(
                                id = lyrics.simpMusicLyrics?.id ?: "",
                                vote = lyrics.simpMusicLyrics?.vote ?: 0,
                                state = VoteState.Idle,
                            )
                    }
                    _nowPlayingScreenData.update {
                        it.copy(
                            lyricsData =
                                NowPlayingScreenData.LyricsData(
                                    lyrics = lyrics,
                                    lyricsProvider = lyricsProvider,
                                ),
                            lyricsVideoId = videoId,
                            lyricsLoading = false,
                        )
                    }
                    _nowPlayingScreenData.value.lyricsData?.let { cacheLyrics(videoId, it) }
                    if (translateAfterLyricsRefetchVideoId == videoId) {
                        translateAfterLyricsRefetchVideoId = null
                        // The newly fetched source lyrics are now in screen state; translate that
                        // exact text rather than the stale copy that was visible before refetch.
                        translateCurrentLyrics()
                    }
                    // Save lyrics to database
                    viewModelScope.launch {
                        lyricsCanvasRepository.insertLyrics(
                            LyricsEntity(
                                videoId = videoId,
                                error = false,
                                lines = lyrics.lines,
                                syncType = lyrics.syncType,
                            ),
                        )
                    }
                    if (shouldSendLyricsToSimpMusic && track != null) {
                        viewModelScope.launch {
                            lyricsCanvasRepository
                                .insertSimpMusicLyrics(
                                    dataStoreManager,
                                    track,
                                    duration,
                                    lyrics,
                                ).collect {
                                    when (it) {
                                        is Resource.Error -> {
                                            Logger.w(tag, "Insert SimpMusic Lyrics Error ${it.message}")
                                        }

                                        is Resource.Success -> {
                                            Logger.d(tag, "Insert SimpMusic Lyrics Success")
                                        }
                                    }
                                }
                        }
                    }
                }
            }
        }
    }

    private fun getLyricsFromFormat(
        isVideo: Boolean,
        song: SongEntity,
        duration: Int,
        forceRefetch: Boolean = false,
    ): Job {
        val videoId = song.videoId
        val job = viewModelScope.launch {
            log("Get Lyrics From Format for $videoId", LogLevel.WARN)
            // Lyrics can be requested from the timeline flow a moment before the player-state
            // mirror catches up. Wait for this track so updateLyrics doesn't silently reject the
            // saved result as belonging to the previous song.
            if (_nowPlayingState.value?.songEntity?.videoId != videoId) {
                nowPlayingState.filterNotNull().first { it.songEntity?.videoId == videoId }
            }
            _nowPlayingScreenData.update { screenData ->
                if (screenData.currentVideoId == videoId && screenData.lyricsData == null) {
                    screenData.copy(lyricsLoading = true)
                } else {
                    screenData
                }
            }
            // Restore previously saved lyrics/translations before making another provider request.
            // This makes translations available immediately when a saved song is opened from a playlist.
            if (!forceRefetch && getSavedLyrics(song.toTrack().copy(durationSeconds = duration))) return@launch
            val artistName = song.artistName
            val artist =
                if (artistName?.firstOrNull() != null &&
                    artistName
                        .firstOrNull()
                        ?.contains("Various Artists") == false
                ) {
                    artistName.firstOrNull()
                } else {
                    mediaPlayerHandler.nowPlaying
                        .first()
                        ?.metadata
                        ?.artist
                        ?: ""
                }
            resetLyricsVoteState()
            val lyricsProvider = lyricsProviderFor(videoId)
            when (lyricsProvider) {
                DataStoreManager.SIMPMUSIC -> {
                    getSimpMusicLyrics(
                        videoId,
                        song,
                        (artist ?: ""),
                        duration,
                    )
                }

                DataStoreManager.LRCLIB -> {
                    getLrclibLyrics(
                        song,
                        (artist ?: ""),
                        duration,
                    )
                }

                DataStoreManager.YOUTUBE -> {
                    getYouTubeCaption(
                        videoId,
                        song,
                        (artist ?: ""),
                        duration,
                    )
                }

                DataStoreManager.BETTER_LYRICS -> {
                    getBetterLyrics(
                        song,
                        (artist ?: "").toString(),
                        duration,
                    )
                }

                DataStoreManager.AUTO_WORD_SYNCED_LYRICS -> {
                    val selected =
                        findPrioritizedLyrics(
                            videoId = videoId,
                            title = song.title,
                            artist = artist.orEmpty(),
                            duration = duration,
                        )
                    if (selected == null) {
                        dataStoreManager.putString(SONG_LYRICS_PROVIDER_KEY_PREFIX + videoId, DataStoreManager.SIMPMUSIC)
                        getSimpMusicLyrics(videoId, song, artist.orEmpty(), duration)
                    } else {
                        // Remember the winner for this track so subsequent opens are immediate and
                        // do not issue the full provider comparison again.
                        dataStoreManager.putString(SONG_LYRICS_PROVIDER_KEY_PREFIX + videoId, selected.first)
                        storeFetchedLyrics(videoId, duration, selected.first, selected.second)
                    }
                }

                DataStoreManager.BETTER_LYRICS_PORTATO,
                DataStoreManager.YOULYPLUS,
                DataStoreManager.KUGOU,
                DataStoreManager.UNISON -> {
                    getLyricsFromSelectedProvider(
                        provider = lyricsProvider,
                        videoId = videoId,
                        song = song,
                        artist = artist.orEmpty(),
                        duration = duration,
                    )
                }
            }
        }
        // A provider chain can finish without returning lyrics, and a provider can fail before it
        // calls updateLyrics. Clear the loading state then, but ignore cancellation because a newer
        // lookup (or a different track) may already own the screen.
        job.invokeOnCompletion { cause ->
            if (cause !is kotlinx.coroutines.CancellationException) {
                _nowPlayingScreenData.update { screenData ->
                    if (screenData.currentVideoId == videoId && screenData.lyricsData == null) {
                        screenData.copy(lyricsLoading = false)
                    } else {
                        screenData
                    }
                }
            }
        }
        return job
    }

    private suspend fun getSimpMusicLyrics(
        videoId: String,
        song: SongEntity,
        artist: String?,
        duration: Int,
    ) {
        lyricsCanvasRepository.getSimpMusicLyrics(videoId).collectLatest {
            Logger.w(tag, "Get SimpMusic Lyrics for $videoId: $it")
            val data = it.data
            if (it is Resource.Success && data != null) {
                Logger.d(tag, "Get SimpMusic Lyrics Success")
                updateLyrics(
                    videoId,
                    duration,
                    data,
                    false,
                    LyricsProvider.SIMPMUSIC,
                )
                insertLyrics(
                    data.toLyricsEntity(videoId),
                )
                getSimpMusicTranslatedLyrics(
                    videoId,
                    data,
                )
            } else if (dataStoreManager.spotifyLyrics.first() == TRUE) {
                getSpotifyLyrics(
                    song.toTrack().copy(durationSeconds = duration),
                    "${song.title} $artist",
                    duration,
                )
            } else {
                getLrclibLyrics(
                    song,
                    (artist ?: ""),
                    duration,
                )
            }
        }
    }

    private suspend fun getYouTubeCaption(
        videoId: String,
        song: SongEntity,
        artist: String?,
        duration: Int,
    ) {
        lyricsCanvasRepository
            .getYouTubeCaption(dataStoreManager.translationLanguage.first(), videoId)
            .cancellable()
            .collect { response ->
                val data = response.data
                when (response) {
                    is Resource.Success if (data != null) -> {
                        val lyrics = data.first
                        val translatedLyrics = data.second
                        insertLyrics(lyrics.toLyricsEntity(videoId))
                        updateLyrics(
                            videoId,
                            duration,
                            lyrics,
                            false,
                            LyricsProvider.YOUTUBE,
                        )
                        if (translatedLyrics != null) {
                            updateLyrics(
                                videoId,
                                duration,
                                translatedLyrics,
                                true,
                                LyricsProvider.YOUTUBE,
                            )
                        } else {
                            getAITranslationLyrics(
                                videoId,
                                lyrics,
                            )
                        }
                    }

                    else -> {
                        getSimpMusicLyrics(
                            videoId,
                            song,
                            (artist ?: ""),
                            duration,
                        )
                    }
                }
            }
    }

    private suspend fun getLrclibLyrics(
        song: SongEntity,
        artist: String,
        duration: Int,
    ) {
        lyricsCanvasRepository
            .getLrclibLyricsData(
                artist,
                song.title,
                duration,
            ).collectLatest { res ->
                val data = res.data
                when (res) {
                    is Resource.Success if (data != null) -> {
                        Logger.d(tag, "Get Lyrics Data Success")
                        updateLyrics(
                            song.videoId,
                            duration,
                            res.data,
                            false,
                            LyricsProvider.LRCLIB,
                        )
                        insertLyrics(
                            res.data?.toLyricsEntity(
                                song.videoId,
                            ) ?: return@collectLatest,
                        )
                        getAITranslationLyrics(
                            song.videoId,
                            data,
                        )
                    }

                    else -> {
                        getSavedLyrics(
                            song.toTrack().copy(
                                durationSeconds = duration,
                            ),
                        )
                    }
                }
            }
    }

    private suspend fun getBetterLyrics(
        song: SongEntity,
        artist: String,
        duration: Int,
    ) {
        lyricsCanvasRepository
            .getBetterLyrics(
                artist,
                song.title,
                duration,
            ).collectLatest { res ->
                val data = res.data
                when (res) {
                    is Resource.Success if (data != null) -> {
                        Logger.d(tag, "Get BetterLyrics Success")
                        cacheBoundaryAwareLyrics(song.videoId, data)
                        updateLyrics(
                            song.videoId,
                            duration,
                            data,
                            false,
                            LyricsProvider.BETTER_LYRICS,
                        )
                        getAITranslationLyrics(
                            song.videoId,
                            data,
                        )
                    }

                    else -> {
                        log("Get BetterLyrics Error: ${res.message}")
                        getSimpMusicLyrics(
                            song.videoId,
                            song,
                            artist,
                            duration,
                        )
                    }
                }
            }
    }

    private suspend fun getSimpMusicTranslatedLyrics(
        videoId: String,
        lyrics: Lyrics,
    ) {
        val translationLanguage =
            dataStoreManager.translationLanguage.first()
        val cachedTranslation =
            lyricsCanvasRepository
                .getSavedTranslatedLyrics(videoId, translationLanguage, LyricsProvider.SIMPMUSIC.name)
                .firstOrNull()
                ?.takeUnless { it.error || it.lines.isNullOrEmpty() }
        if (cachedTranslation != null) {
            updateLyrics(
                videoId,
                0,
                cachedTranslation.toLyrics(),
                true,
                LyricsProvider.SIMPMUSIC,
            )
            return
        }
        lyricsCanvasRepository.getSimpMusicTranslatedLyrics(videoId, translationLanguage).collectLatest { response ->
            val data = response.data
            when (response) {
                is Resource.Success if (data != null) -> {
                    // If SimpMusic translated lyrics are RICH_SYNCED (word-by-word),
                    // convert to LINE_SYNCED, downvote, and fallback to AI translation
                    if (data.syncType == "RICH_SYNCED") {
                        Logger.w(tag, "SimpMusic translated lyrics are RICH_SYNCED, downvoting and falling back to AI")
                        val simpMusicLyricsId = data.simpMusicLyrics?.id
                        if (!simpMusicLyricsId.isNullOrEmpty()) {
                            viewModelScope.launch {
                                lyricsCanvasRepository
                                    .voteSimpMusicTranslatedLyrics(simpMusicLyricsId, false)
                                    .collectLatest { voteResult ->
                                        when (voteResult) {
                                            is Resource.Error -> Logger.w(tag, "Downvote RICH_SYNCED translated lyrics error: ${voteResult.message}")
                                            is Resource.Success -> Logger.d(tag, "Downvote RICH_SYNCED translated lyrics success")
                                        }
                                    }
                            }
                        }
                        // Fallback to AI translation
                        getAITranslationLyrics(videoId, lyrics)
                    } else {
                        Logger.d(tag, "Get SimpMusic Translated Lyrics Success")
                        updateLyrics(
                            videoId,
                            0,
                            data,
                            true,
                            LyricsProvider.SIMPMUSIC,
                        )
                    }
                }

                else -> {
                    Logger.w(tag, "Get SimpMusic Translated Lyrics Error: ${response.message}")
                    getAITranslationLyrics(
                        videoId,
                        lyrics,
                    )
                }
            }
        }
    }

    private suspend fun findPrioritizedLyrics(
        videoId: String,
        title: String,
        artist: String,
        duration: Int,
    ): Pair<String, Lyrics>? =
        coroutineScope {
            val providers =
                listOf(
                    DataStoreManager.BETTER_LYRICS,
                    DataStoreManager.BETTER_LYRICS_PORTATO,
                    DataStoreManager.YOULYPLUS,
                    DataStoreManager.UNISON,
                    DataStoreManager.SIMPMUSIC,
                    DataStoreManager.KUGOU,
                    DataStoreManager.LRCLIB,
                )
            providers
                .map { provider ->
                    async {
                        val response =
                            lyricsCanvasRepository
                                .getLyricsFromProvider(
                                    provider = provider,
                                    videoId = videoId,
                                    artist = artist,
                                    track = title,
                                    album = null,
                                    duration = duration,
                                ).firstOrNull()
                        if (response is Resource.Success) response.data?.let { provider to it } else null
                    }
                }.map { it.await() }
                .filterNotNull()
                .maxWithOrNull(
                    compareBy<Pair<String, Lyrics>> { lyricsSyncQuality(it.second) }
                        .thenBy { lyricsProviderPreference(it.first) },
                )
        }

    private fun lyricsSyncQuality(lyrics: Lyrics): Int =
        when (lyrics.syncType?.uppercase()) {
            "RICH_SYNCED", "WORD_SYNCED", "SYLLABLE_SYNCED" -> 3
            "LINE_SYNCED" -> 2
            else -> 1
        }

    private fun lyricsProviderPreference(provider: String): Int =
        when (provider) {
            // BetterLyrics TTML has syllable-level timing; QQ/Portato and YouLyPlus carry
            // word-level timing. All rank above line-only sources.
            DataStoreManager.BETTER_LYRICS -> 7
            DataStoreManager.BETTER_LYRICS_PORTATO -> 6
            DataStoreManager.YOULYPLUS -> 5
            DataStoreManager.UNISON -> 4
            DataStoreManager.SIMPMUSIC -> 3
            DataStoreManager.KUGOU -> 2
            DataStoreManager.LRCLIB -> 1
            else -> 0
        }

    private suspend fun getLyricsFromSelectedProvider(
        provider: String,
        videoId: String,
        song: SongEntity,
        artist: String,
        duration: Int,
    ) {
        lyricsCanvasRepository
            .getLyricsFromProvider(
                provider = provider,
                videoId = videoId,
                artist = artist,
                track = song.title,
                album = null,
                duration = duration,
            ).collectLatest { response ->
                val data = response.data
                if (response is Resource.Success && data != null) {
                    storeFetchedLyrics(videoId, duration, provider, data)
                } else {
                    Logger.w(tag, "Lyrics provider '$provider' failed (${response.message}); falling back to SimpMusic")
                    getSimpMusicLyrics(videoId, song, artist, duration)
                }
            }
    }

    private suspend fun storeFetchedLyrics(
        videoId: String,
        duration: Int,
        provider: String,
        lyrics: Lyrics,
    ) {
        val source =
            when (provider) {
                DataStoreManager.SIMPMUSIC -> LyricsProvider.SIMPMUSIC
                DataStoreManager.LRCLIB -> LyricsProvider.LRCLIB
                DataStoreManager.BETTER_LYRICS -> LyricsProvider.BETTER_LYRICS
                DataStoreManager.BETTER_LYRICS_PORTATO -> LyricsProvider.BETTER_LYRICS_PORTATO
                DataStoreManager.YOULYPLUS -> LyricsProvider.YOULYPLUS
                DataStoreManager.KUGOU -> LyricsProvider.KUGOU
                DataStoreManager.UNISON -> LyricsProvider.UNISON
                else -> LyricsProvider.SIMPMUSIC
            }
        cacheBoundaryAwareLyrics(videoId, lyrics)
        updateLyrics(videoId, duration, lyrics, false, source)
        if (source == LyricsProvider.SIMPMUSIC) {
            getSimpMusicTranslatedLyrics(videoId, lyrics)
        } else {
            getAITranslationLyrics(videoId, lyrics)
        }
    }

    private suspend fun getAITranslationLyrics(
        videoId: String,
        lyrics: Lyrics,
    ) {
        Logger.d(tag, "Get AI Translation Lyrics for $videoId")
        val targetLanguage = dataStoreManager.translationLanguage.first()
        val sourceText = lyrics.lines.orEmpty().joinToString(" ") { it.words }
        if (!shouldOfferLyricsTranslation(sourceText, targetLanguage)) return
        val savedTranslatedLyrics =
            lyricsCanvasRepository
                .getSavedTranslatedLyrics(videoId, targetLanguage, LyricsProvider.AI.name)
                .firstOrNull()
                ?.takeUnless { it.error }
        if (savedTranslatedLyrics != null) {
            Logger.d(tag, "Get Saved Translated Lyrics")
            updateLyrics(
                videoId,
                0,
                savedTranslatedLyrics.toLyrics(),
                true,
                LyricsProvider.AI,
            )
            return
        }
        val aiProvider = dataStoreManager.aiProvider.first()
        val aiApiKey =
            if (aiProvider == DataStoreManager.AI_PROVIDER_OPENROUTER) {
                dataStoreManager.openRouterApiKey.first()
            } else {
                dataStoreManager.aiApiKey.first()
            }
        if (dataStoreManager.getSongLyricsTranslationEnabled(videoId).first() && aiApiKey.isNotBlank() &&
            dataStoreManager.enableTranslateLyric.first() == FALSE
        ) {
            // Convert RICH_SYNCED to LINE_SYNCED before sending to AI
            // AI should only work with line-level or plain lyrics
            val lyricsForAi =
                if (lyrics.syncType == "RICH_SYNCED") {
                    lyrics.toSyncedLyrics()
                } else {
                    lyrics
                }
            lyricsCanvasRepository
                .getAITranslationLyrics(
                    lyricsForAi,
                    targetLanguage,
                ).cancellable()
                .collectLatest {
                    val data = it.data
                    when (it) {
                        is Resource.Success if (data != null) -> {
                            Logger.d(tag, "Get AI Translate Lyrics Success")
                            lyricsCanvasRepository.insertTranslatedLyrics(
                                TranslatedLyricsEntity(
                                    videoId = videoId,
                                    language = targetLanguage,
                                    error = false,
                                    lines = data.lines,
                                    syncType = data.syncType,
                                ),
                            )
                            updateLyrics(
                                videoId,
                                0,
                                data,
                                true,
                                LyricsProvider.AI,
                            )
                        }

                        else -> {
                            Logger.w(tag, "Get AI Translate Lyrics Error: ${it.message}")
                        }
                    }
                }
        }
    }

    fun getSongLyricsTranslationEnabled(videoId: String) = dataStoreManager.getSongLyricsTranslationEnabled(videoId)

    fun setSongLyricsTranslationEnabled(videoId: String, enabled: Boolean) {
        viewModelScope.launch { dataStoreManager.setSongLyricsTranslationEnabled(videoId, enabled) }
    }

    /** Explicitly translate the lyrics currently shown in Now Playing, independent of auto-translate. */
    fun translateCurrentLyrics() {
        val lyrics = _nowPlayingScreenData.value.lyricsData?.lyrics
        val videoId = _nowPlayingState.value?.track?.videoId
        if (lyrics == null || lyrics.lines.isNullOrEmpty() || videoId.isNullOrBlank()) {
            _lyricsTranslationRequestState.value =
                LyricsTranslationRequestState(videoId = videoId, error = "Lyrics are not available to translate.")
            return
        }

        // Hiding a translation only changes its visibility. Reuse the completed result
        // and let an in-flight request finish instead of spending credits on another call.
        val existingTranslation = _nowPlayingScreenData.value.lyricsData?.translatedLyrics?.first
        if (existingTranslation != null && !existingTranslation.error && !existingTranslation.lines.isNullOrEmpty()) {
            _lyricsTranslationRequestState.value = LyricsTranslationRequestState(videoId = videoId)
            return
        }
        if (manualLyricsTranslationJob?.isActive == true) return

        manualLyricsTranslationJob = viewModelScope.launch {
            try {
                val targetLanguage = dataStoreManager.translationLanguage.first()
                val sourceText = lyrics.lines.orEmpty().joinToString(" ") { it.words }
                if (!shouldOfferLyricsTranslation(sourceText, targetLanguage)) {
                    _lyricsTranslationRequestState.value = LyricsTranslationRequestState(videoId = videoId)
                    return@launch
                }
                _lyricsTranslationRequestState.value = LyricsTranslationRequestState(videoId = videoId, isLoading = true)
                val provider = dataStoreManager.aiProvider.first()
                val apiKey =
                    if (provider == DataStoreManager.AI_PROVIDER_OPENROUTER) {
                        dataStoreManager.openRouterApiKey.first()
                    } else {
                        dataStoreManager.aiApiKey.first()
                    }
                if (apiKey.isBlank()) {
                    _lyricsTranslationRequestState.value =
                        LyricsTranslationRequestState(
                            videoId = videoId,
                            error = "API key required",
                        )
                    return@launch
                }
                if (provider == DataStoreManager.AI_PROVIDER_OPENROUTER && dataStoreManager.openRouterModelId.first().isBlank()) {
                    _lyricsTranslationRequestState.value =
                        LyricsTranslationRequestState(videoId = videoId, error = "Set an OpenRouter model ID in AI settings.")
                    return@launch
                }

                val lyricsForAi = if (lyrics.syncType == "RICH_SYNCED") lyrics.toSyncedLyrics() else lyrics
                lyricsCanvasRepository
                    .getAITranslationLyrics(lyricsForAi, targetLanguage)
                    .cancellable()
                    .collectLatest { response ->
                        val translatedLyrics = response.data
                        when {
                            response is Resource.Success && translatedLyrics != null -> {
                                lyricsCanvasRepository.insertTranslatedLyrics(
                                    TranslatedLyricsEntity(
                                        videoId = videoId,
                                        language = targetLanguage,
                                        error = false,
                                        lines = translatedLyrics.lines,
                                        syncType = translatedLyrics.syncType,
                                    ),
                                )
                                updateLyrics(videoId, 0, translatedLyrics, true, LyricsProvider.AI)
                                _lyricsTranslationRequestState.value = LyricsTranslationRequestState(videoId = videoId)
                            }

                            response is Resource.Error -> {
                                _lyricsTranslationRequestState.value =
                                    LyricsTranslationRequestState(
                                        videoId = videoId,
                                        error = translationFailureMessage(response.message),
                                    )
                            }
                        }
                    }
            } catch (exception: kotlinx.coroutines.CancellationException) {
                throw exception
            } catch (exception: Exception) {
                Logger.w(tag, "Manual AI lyrics translation failed: ${exception.message}")
                _lyricsTranslationRequestState.value =
                    LyricsTranslationRequestState(
                        videoId = videoId,
                        error = translationFailureMessage(exception.message),
                    )
            }
        }
    }

    private suspend fun getSpotifyLyrics(
        track: Track,
        query: String,
        duration: Int? = null,
    ) {
        Logger.d("Check SpotifyLyrics", "SpotifyLyrics $query")
        lyricsCanvasRepository.getSpotifyLyrics(dataStoreManager, query, duration).cancellable().collect { response ->
            Logger.d("Check SpotifyLyrics", response.toString())
            val data = response.data
            when (response) {
                is Resource.Success -> {
                    if (data != null) {
                        insertLyrics(
                            data.toLyricsEntity(
                                track.videoId,
                            ),
                        )
                        updateLyrics(
                            track.videoId,
                            duration ?: 0,
                            data,
                            false,
                            LyricsProvider.SPOTIFY,
                        )
                        getAITranslationLyrics(
                            track.videoId,
                            data,
                        )
                    }
                }

                else -> {
                    getLrclibLyrics(
                        track.toSongEntity(),
                        track.artists.toListName().firstOrNull() ?: "",
                        duration ?: 0,
                    )
                }
            }
        }
    }

    private fun setLyricsProvider(forceRefetch: Boolean = false) {
        viewModelScope.launch {
            val songEntity = nowPlayingState.value?.songEntity ?: return@launch
            val isVideo = nowPlayingState.value?.mediaItem?.isVideo() ?: false
            if (forceRefetch) {
                manualLyricsTranslationJob?.cancel()
                manualLyricsTranslationJob = null
                _lyricsTranslationRequestState.value = LyricsTranslationRequestState()
                _nowPlayingScreenData.update { screenData ->
                    screenData.copy(lyricsData = screenData.lyricsData?.copy(translatedLyrics = null))
                }
                val language = dataStoreManager.translationLanguage.first()
                LyricsProvider.values().forEach { provider ->
                    lyricsCanvasRepository.removeTranslatedLyrics(songEntity.videoId, language, provider.name)
                }
            }
            lyricsJob?.cancel()
            lyricsJobVideoId = songEntity.videoId
            lyricsJob =
                getLyricsFromFormat(
                    isVideo = isVideo,
                    song = songEntity,
                    duration = timeline.value.total.toInt() / 1000,
                    forceRefetch = forceRefetch,
                )
        }
    }

    /** Force a fresh fetch from the currently selected original-lyrics provider. */
    fun refetchCurrentLyrics(translationEnabled: Boolean = false) {
        viewModelScope.launch {
            val videoId = _nowPlayingState.value?.track?.videoId
            if (translationEnabled && !videoId.isNullOrBlank()) {
                translateAfterLyricsRefetchVideoId = videoId
            } else {
                translateAfterLyricsRefetchVideoId = null
            }
            setLyricsProvider(forceRefetch = true)
        }
    }

    fun changeLyricsProviderAndRefetch(provider: String) {
        viewModelScope.launch {
            val videoId = _nowPlayingState.value?.songEntity?.videoId ?: return@launch
            if (provider !in setOf(
                    DataStoreManager.SIMPMUSIC,
                    DataStoreManager.YOUTUBE,
                    DataStoreManager.LRCLIB,
                    DataStoreManager.BETTER_LYRICS,
                    DataStoreManager.BETTER_LYRICS_PORTATO,
                    DataStoreManager.YOULYPLUS,
                    DataStoreManager.KUGOU,
                    DataStoreManager.UNISON,
                )
            ) return@launch
            dataStoreManager.putString(SONG_LYRICS_PROVIDER_KEY_PREFIX + videoId, provider)
            completedLyricsCache.update { cache -> cache.filterKeys { it.videoId != videoId } }
            setLyricsProvider(forceRefetch = true)
        }
    }

    private var _recreateActivity: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val recreateActivity: StateFlow<Boolean> = _recreateActivity

    fun activityRecreate() {
        _recreateActivity.value = true
    }

    fun activityRecreateDone() {
        _recreateActivity.value = false
    }

    fun addListToQueue(listTrack: ArrayList<Track>) {
        viewModelScope.launch {
            if (listTrack.size == 1 && dataStoreManager.endlessQueue.first() == TRUE) {
                mediaPlayerHandler.playNext(listTrack.first())
                makeToast(getString(Res.string.play_next))
            } else {
                mediaPlayerHandler.loadMoreCatalog(listTrack, isAddToQueue = true)
                makeToast(getString(Res.string.added_to_queue))
            }
        }
    }

    fun addToYouTubeLiked() {
        viewModelScope.launch {
            val videoId = mediaPlayerHandler.nowPlaying.first()?.mediaId
            if (videoId != null) {
                val like = likeStatus.value
                if (!like) {
                    songRepository
                        .addToYouTubeLiked(
                            mediaPlayerHandler.nowPlaying.first()?.mediaId,
                        ).collect { response ->
                            if (response in 200..299) {
                                updateCachedYouTubeLike(videoId, true)
                                getLikeStatus(videoId)
                            } else {
                                makeToast(getString(Res.string.error))
                            }
                        }
                } else {
                    songRepository
                        .removeFromYouTubeLiked(
                            mediaPlayerHandler.nowPlaying.first()?.mediaId,
                        ).collect {
                            if (it in 200..299) {
                                updateCachedYouTubeLike(videoId, false)
                                getLikeStatus(videoId)
                            } else {
                                makeToast(getString(Res.string.error))
                            }
                        }
                }
            }
        }
    }

    fun getEnableLiquidGlass() = dataStoreManager.enableLiquidGlass

    val frostedGlassHeaderStyle = dataStoreManager.frostedGlassHeaderStyle
        .stateIn(viewModelScope, SharingStarted.Eagerly, com.petedev.wavemusic.domain.manager.FrostedGlassHeaderStyle.Default)

    fun setFrostedGlassHeaderStyle(style: com.petedev.wavemusic.domain.manager.FrostedGlassHeaderStyle) {
        viewModelScope.launch { dataStoreManager.setFrostedGlassHeaderStyle(style) }
    }

    val frostedGlassHorizonStyle = dataStoreManager.frostedGlassHorizonStyle
        .stateIn(viewModelScope, SharingStarted.Eagerly, com.petedev.wavemusic.domain.manager.FrostedGlassHeaderStyle.Off)

    fun setFrostedGlassHorizonStyle(style: com.petedev.wavemusic.domain.manager.FrostedGlassHeaderStyle) {
        viewModelScope.launch { dataStoreManager.setFrostedGlassHorizonStyle(style) }
    }

    val fixedHeader = dataStoreManager.persistentHeaderButtons
        .map { it == DataStoreManager.TRUE }
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    fun setFixedHeader(enabled: Boolean) {
        viewModelScope.launch { dataStoreManager.setPersistentHeaderButtons(enabled) }
    }

    private val savedGlassIntensity = dataStoreManager.liquidGlassIntensity.stateIn(
        viewModelScope, SharingStarted.Eagerly, LiquidGlassIntensity.Default,
    )
    private val glassIntensityPreview = MutableStateFlow<Float?>(null)
    private val glassIntensitySaveMutex = Mutex()
    private var glassIntensityPreviewRevision = 0L

    val liquidGlassIntensity = combine(savedGlassIntensity, glassIntensityPreview) { saved, preview ->
        preview ?: saved
    }.stateIn(viewModelScope, SharingStarted.Eagerly, LiquidGlassIntensity.Default)

    fun previewLiquidGlassIntensity(intensity: Float) {
        glassIntensityPreviewRevision++
        glassIntensityPreview.value = LiquidGlassIntensity.normalize(intensity)
    }

    /** Preview every drag frame; write once on release or when leaving the page. */
    fun saveLiquidGlassIntensity() {
        val intensity = glassIntensityPreview.value ?: return
        val revision = glassIntensityPreviewRevision
        viewModelScope.launch {
            glassIntensitySaveMutex.withLock {
                dataStoreManager.setLiquidGlassIntensity(intensity)
                // Keep the preview until storage catches up, avoiding a flash of the old value.
                savedGlassIntensity.first { it == intensity }
                if (glassIntensityPreviewRevision == revision) glassIntensityPreview.value = null
            }
        }
    }

    fun getLocalTrackingEnabled() = dataStoreManager.localTrackingEnabled

    // Drives the Mix for you tab: YouTube hands an anonymous session no mixes at all.
    fun getYouTubeLoggedIn() = dataStoreManager.loggedIn

    fun getThemeMode() = dataStoreManager.themeMode

    fun getPageTransitionStyle() = dataStoreManager.pageTransitionStyle

    fun getAdvancedDialogAnimations() = dataStoreManager.advancedDialogAnimations

    fun getReduceVisualEffects() = dataStoreManager.reduceVisualEffects

    private val hideStatusBarState by lazy {
        dataStoreManager.hideStatusBar.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DataStoreManager.FALSE)
    }

    fun getHideStatusBar() = hideStatusBarState

    fun getSlimNavigationBar() = dataStoreManager.slimNavigationBar

    fun getAppBackgroundPreset() = dataStoreManager.appBackgroundPreset

    fun setAppBackgroundPreset(preset: String) {
        viewModelScope.launch { dataStoreManager.setAppBackgroundPreset(preset) }
    }

    fun getThemeColorSource() = dataStoreManager.themeColorSource

    fun getCustomThemeColor() = dataStoreManager.customThemeColor

    fun getNowPlayingStyle() = dataStoreManager.nowPlayingStyle

    fun getLyricsStyle() = dataStoreManager.lyricsStyle

    fun getLyricsFontSizeTenthsPt() = dataStoreManager.lyricsFontSizeTenthsPt

    fun getTranslatedLyricsFontSizeTenthsPt() = dataStoreManager.translatedLyricsFontSizeTenthsPt

    private val effectivePlayerLyricsOffsetMs by lazy {
        mediaPlayerHandler.nowPlayingState
            .map { it.activeVideoId() }
            .distinctUntilChanged()
            .flatMapLatest { dataStoreManager.getEffectiveLyricsOffsetMs(it) }
    }

    fun getLyricsOffsetMs() = effectivePlayerLyricsOffsetMs

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            dataStoreManager.setThemeMode(mode)
        }
    }

    fun setPageTransitionStyle(style: String) {
        viewModelScope.launch {
            dataStoreManager.setPageTransitionStyle(style)
        }
    }

    fun setAdvancedDialogAnimations(enabled: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setAdvancedDialogAnimations(enabled)
        }
    }

    fun setReduceVisualEffects(enabled: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setReduceVisualEffects(enabled)
        }
    }

    fun setHideStatusBar(enabled: Boolean) {
        viewModelScope.launch { dataStoreManager.setHideStatusBar(enabled) }
    }

    fun setSlimNavigationBar(enabled: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setSlimNavigationBar(enabled)
        }
    }

    private val accentColorWriteMutex = Mutex()

    fun setAccentColor(argbHex: String?) {
        viewModelScope.launch {
            accentColorWriteMutex.withLock {
                if (argbHex == null) {
                    dataStoreManager.setThemeColorSource(DataStoreManager.THEME_COLOR_DEFAULT)
                } else {
                    // Save the seed before enabling it so the previous custom color never flashes.
                    dataStoreManager.setCustomThemeColor(argbHex)
                    dataStoreManager.setThemeColorSource(DataStoreManager.THEME_COLOR_CUSTOM)
                }
            }
        }
    }

    fun setThemeColorSource(source: String) {
        viewModelScope.launch {
            dataStoreManager.setThemeColorSource(source)
        }
    }

    fun setCustomThemeColor(argbHex: String) {
        viewModelScope.launch {
            dataStoreManager.setCustomThemeColor(argbHex)
        }
    }

    fun setNowPlayingStyle(style: String) {
        viewModelScope.launch {
            dataStoreManager.setNowPlayingStyle(style)
        }
    }

    fun setLyricsStyle(style: String) {
        viewModelScope.launch {
            dataStoreManager.setLyricsStyle(style)
        }
    }

    fun setLyricsFontSizeTenthsPt(sizeTenthsPt: Int) {
        viewModelScope.launch {
            dataStoreManager.setLyricsFontSizeTenthsPt(sizeTenthsPt)
        }
    }

    fun setTranslatedLyricsFontSizeTenthsPt(sizeTenthsPt: Int) {
        viewModelScope.launch {
            dataStoreManager.setTranslatedLyricsFontSizeTenthsPt(sizeTenthsPt)
        }
    }

    fun getRomanizationLanguages() = dataStoreManager.romanizationLanguages

    fun setRomanizationLanguages(languages: Set<RomanizationLanguage>) {
        viewModelScope.launch {
            // Sorted by name so the stored string is stable: an unsorted Set writes a different
            // value for the same selection depending on iteration order, which makes the DataStore
            // flow emit on a change that did not happen.
            dataStoreManager.setRomanizationLanguages(languages.map { it.name }.sorted().joinToString(","))
        }
    }

    private val _reloadDestination: MutableStateFlow<KClass<*>?> = MutableStateFlow(null)
    val reloadDestination: StateFlow<KClass<*>?> = _reloadDestination.asStateFlow()
    private val _libraryFilterResetRequests = MutableStateFlow(0L)
    val libraryFilterResetRequests: StateFlow<Long> = _libraryFilterResetRequests.asStateFlow()
    private var handledLibraryFilterResetRequest = 0L

    fun consumeLibraryFilterResetRequest(request: Long): Boolean {
        if (request <= handledLibraryFilterResetRequest) return false
        handledLibraryFilterResetRequest = request
        return true
    }

    fun reloadDestination(destination: KClass<*>) {
        // A single reselect should return from an in-library category/"See all" view to the root.
        if (destination == LibraryDestination::class) {
            _libraryFilterResetRequests.value += 1L
        }
        _reloadDestination.value = destination
    }

    fun reloadDestinationDone() {
        _reloadDestination.value = null
    }

    fun checkForUpdateIfEnabled() {
        viewModelScope.launch {
            if (dataStoreManager.autoCheckForUpdates.first() == TRUE) {
                checkForUpdate()
            }
        }
    }

    // Desktop startup still uses a synchronous gate before creating its window. Android calls
    // [checkForUpdateIfEnabled] so DataStore cannot block its first frame.
    fun shouldCheckForUpdate(): Boolean = runBlocking { dataStoreManager.autoCheckForUpdates.first() == TRUE }

    private var _downloadFileProgress = MutableStateFlow<DownloadProgress>(DownloadProgress.INIT)
    val downloadFileProgress: StateFlow<DownloadProgress> get() = _downloadFileProgress

    fun downloadFile(bitmap: ImageBitmap) {
        val fileName =
            "${nowPlayingScreenData.value.nowPlayingTitle} - ${nowPlayingScreenData.value.artistName}"
                .replace(Regex("""[|\\?*<":>]"""), "")
                .replace(" ", "_")
        val path =
            "${getDownloadFolderPath()}/$fileName"
        viewModelScope.launch {
            nowPlayingState.value?.track?.let { track ->
                val bytesArray = bitmap.toByteArray()
                try {
                    val fileOutputStream = FileOutputStream("$path.jpg")
                    fileOutputStream.write(bytesArray)
                    fileOutputStream.close()
                    Logger.d(tag, "Thumbnail saved to $path.jpg")
                } catch (e: Exception) {
                    throw RuntimeException(e)
                }
                songRepository
                    .downloadToFile(
                        track = track,
                        videoId = track.videoId,
                        path = path,
                        isVideo = nowPlayingScreenData.value.isVideo,
                    ).collectLatest {
                        _downloadFileProgress.value = it
                    }
            }
        }
    }

    fun downloadFileDone() {
        _downloadFileProgress.value = DownloadProgress.INIT
    }

    fun onDoneReview(isDismissOnly: Boolean = true) {
        viewModelScope.launch {
            if (!isDismissOnly) {
                dataStoreManager.doneOpenAppTime()
            } else {
                dataStoreManager.openApp()
            }
        }
    }

    fun onDoneRequestingShareLyrics(contributor: Pair<String, String>? = null) {
        viewModelScope.launch {
            dataStoreManager.setHelpBuildLyricsDatabase(true)
            dataStoreManager.setContributorLyricsDatabase(
                contributor,
            )
        }
    }

    fun setBitmap(bitmap: ImageBitmap?) {
        _nowPlayingScreenData.update {
            it.copy(bitmap = bitmap)
        }
    }

    fun cachedArtworkBitmap(key: String?): ImageBitmap? =
        key?.let { cachedArtworkBitmaps[it] }

    fun cacheArtworkBitmap(key: String?, bitmap: ImageBitmap) {
        if (key.isNullOrBlank()) return
        cachedArtworkBitmaps[key] = bitmap
    }

    // Vote state for translated lyrics
    private val _translatedVoteState = MutableStateFlow<VoteData?>(null)
    val translatedVoteState: StateFlow<VoteData?> = _translatedVoteState.asStateFlow()

    // Vote state for original lyrics
    private val _lyricsVoteState = MutableStateFlow<VoteData?>(null)
    val lyricsVoteState: StateFlow<VoteData?> = _lyricsVoteState.asStateFlow()

    /**
     * Vote for WaveMusic original lyrics (upvote or downvote)
     * @param upvote true for upvote, false for downvote
     */
    fun voteLyrics(upvote: Boolean) {
        val lyricsData = _nowPlayingScreenData.value.lyricsData
        val lyricsProvider = lyricsData?.lyricsProvider
        val simpMusicLyricsId = lyricsData?.lyrics?.simpMusicLyrics?.id ?: return

        if (lyricsProvider != LyricsProvider.SIMPMUSIC || simpMusicLyricsId.isEmpty()) {
            Logger.w(tag, "Cannot vote: not a SimpMusic lyrics or missing ID")
            return
        }

        viewModelScope.launch {
            _lyricsVoteState.update {
                it?.copy(
                    state = VoteState.Loading,
                )
            }
            lyricsCanvasRepository
                .voteSimpMusicLyrics(
                    lyricsId = simpMusicLyricsId,
                    upvote = upvote,
                ).collectLatest { result ->
                    when (result) {
                        is Resource.Error -> {
                            Logger.w(tag, "Vote SimpMusic Lyrics Error ${result.message}")
                            _lyricsVoteState.update {
                                it?.copy(
                                    state = VoteState.Error(result.message ?: "Unknown error"),
                                )
                            }
                        }

                        is Resource.Success -> {
                            Logger.d(tag, "Vote SimpMusic Lyrics Success")
                            _lyricsVoteState.update {
                                it?.copy(
                                    state = VoteState.Success(upvote),
                                    vote = it.vote + if (upvote) 1 else -1,
                                )
                            }
                            makeToast(getString(Res.string.vote_submitted))
                        }
                    }
                }
        }
    }

    private fun resetLyricsVoteState() {
        _lyricsVoteState.value = null
        _translatedVoteState.value = null
    }

    /**
     * Vote for SimpMusic translated lyrics (upvote or downvote)
     * @param upvote true for upvote, false for downvote
     */
    fun voteTranslatedLyrics(upvote: Boolean) {
        val translatedLyrics = _nowPlayingScreenData.value.lyricsData?.translatedLyrics
        val lyricsProvider = translatedLyrics?.second
        val simpMusicLyricsId = translatedLyrics?.first?.simpMusicLyrics?.id ?: return

        if (lyricsProvider != LyricsProvider.SIMPMUSIC || simpMusicLyricsId.isEmpty()) {
            Logger.w(tag, "Cannot vote: not a SimpMusic translated lyrics or missing ID")
            return
        }

        viewModelScope.launch {
            _translatedVoteState.update {
                it?.copy(
                    state = VoteState.Loading,
                )
            }
            lyricsCanvasRepository
                .voteSimpMusicTranslatedLyrics(
                    translatedLyricsId = simpMusicLyricsId,
                    upvote = upvote,
                ).collectLatest { result ->
                    when (result) {
                        is Resource.Error -> {
                            Logger.w(tag, "Vote SimpMusic Translated Lyrics Error ${result.message}")
                            _translatedVoteState.update {
                                it?.copy(
                                    state = VoteState.Error(result.message ?: "Unknown error"),
                                )
                            }
                        }

                        is Resource.Success -> {
                            Logger.d(tag, "Vote SimpMusic Translated Lyrics Success")
                            _translatedVoteState.update {
                                it?.copy(
                                    state = VoteState.Success(upvote),
                                    vote = it.vote + if (upvote) 1 else -1,
                                )
                            }
                            makeToast(getString(Res.string.vote_submitted))
                        }
                    }
                }
        }
    }

    fun shouldStopMusicService(): Boolean = runBlocking { dataStoreManager.killServiceOnExit.first() == TRUE }

    fun isUserLoggedIn(): Boolean = runBlocking { dataStoreManager.cookie.first().isNotEmpty() }

    // Flow-based variant of [isUserLoggedIn] so composables can collect login state once
    // instead of calling runBlocking inside composition (used by NowPlayingScreenContent).
    fun isUserLoggedInFlow(): Flow<Boolean> = dataStoreManager.cookie.map { it.isNotEmpty() }

    fun isCombineFavoriteAndYTLiked(): Boolean = runBlocking { dataStoreManager.combineLocalAndYouTubeLiked.first() == TRUE }
}

sealed class UIEvent {
    data object PlayPause : UIEvent()

    data object Backward : UIEvent()

    data object Forward : UIEvent()

    data object Next : UIEvent()

    data object Previous : UIEvent()

    /**
     * Always advances to the previous track — bypasses the 3-second
     * "seek to start of current track" rule used by [Previous]. Used by the
     * NowPlaying artwork pager swipe.
     */
    data object SkipToPrevious : UIEvent()

    data object Stop : UIEvent()

    data object Shuffle : UIEvent()

    data object Repeat : UIEvent()

    data class UpdateProgress(
        val newProgress: Float,
    ) : UIEvent()

    data class UpdateVolume(
        val newVolume: Float,
    ) : UIEvent()

    data object ToggleLike : UIEvent()
}

enum class LyricsProvider {
    SIMPMUSIC,
    YOUTUBE,
    SPOTIFY,
    LRCLIB,
    BETTER_LYRICS,
    BETTER_LYRICS_PORTATO,
    YOULYPLUS,
    KUGOU,
    UNISON,
    AI,
    OFFLINE,
}

data class LyricsTranslationRequestState(
    val videoId: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)

private fun translationFailureMessage(message: String?): String =
    if (message.isNullOrBlank()) "Lyrics translation failed." else "Translation failed, try again"

private data class CompletedLyricsCacheKey(
    val videoId: String,
    val mainProvider: String,
    val translationLanguage: String,
)

data class NowPlayingScreenData(
    val playlistName: String,
    val nowPlayingTitle: String,
    val currentVideoId: String? = null,
    val artistName: String,
    val isVideo: Boolean,
    val isExplicit: Boolean = false,
    val thumbnailURL: String?,
    val canvasData: CanvasData? = null,
    /** True after animated artwork / canvas lookup has settled for the current track. */
    val canvasResolved: Boolean = false,
    val lyricsData: LyricsData? = null,
    /** True while the current track's original lyrics are being looked up. */
    val lyricsLoading: Boolean = false,
    /** Track that owns [lyricsData], used to avoid rendering the previous song's lyrics during a switch. */
    val lyricsVideoId: String? = null,
    val songInfoData: SongInfoEntity? = null,
    val bitmap: ImageBitmap? = null,
) {
    data class CanvasData(
        val isVideo: Boolean,
        val url: String,
        val videoId: String? = null,
        /** Still frame from the animated artwork, used while its video decoder warms up. */
        val thumbnailUrl: String? = null,
    )

    data class LyricsData(
        val lyrics: Lyrics,
        val translatedLyrics: Pair<Lyrics, LyricsProvider>? = null,
        val lyricsProvider: LyricsProvider,
    )

    companion object {
        fun initial(): NowPlayingScreenData =
            NowPlayingScreenData(
                nowPlayingTitle = "",
                artistName = "",
                isVideo = false,
                thumbnailURL = null,
                canvasData = null,
                lyricsData = null,
                songInfoData = null,
                playlistName = "",
            )
    }
}

data class VoteData(
    val id: String,
    val vote: Int,
    val state: VoteState,
)

sealed class VoteState {
    data object Idle : VoteState()

    data object Loading : VoteState()

    data class Success(
        val upvote: Boolean,
    ) : VoteState()

    data class Error(
        val message: String,
    ) : VoteState()
}

/**
 * Whether a stored canvas url points at something a player should open rather than an image.
 *
 * The column holds whatever the active source wrote: a Spotify canvas is an `.mp4`, while AM
 * animated artwork is an HLS `.m3u8` master playlist. Testing only for `.mp4` — as this did before
 * AM existed — sends every AM artwork down the still-image branch, and because the branch that
 * reads this is the one that restores a *cached* url, the failure only appears from the second play
 * of a track onwards.
 */
private fun String.isCanvasVideoUrl(): Boolean = contains(".mp4", ignoreCase = true) || contains(".m3u8", ignoreCase = true)
