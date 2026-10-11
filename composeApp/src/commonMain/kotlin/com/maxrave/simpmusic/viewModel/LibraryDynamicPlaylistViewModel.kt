package com.maxrave.simpmusic.viewModel

import androidx.lifecycle.viewModelScope
import com.maxrave.common.Config
import com.maxrave.common.Config.REMOVED_SONG_DATE_TIME
import com.maxrave.domain.data.entities.ArtistEntity
import com.maxrave.domain.data.entities.DownloadState
import com.maxrave.domain.data.entities.SongEntity
import com.maxrave.domain.extension.now
import com.maxrave.domain.mediaservice.handler.PlaylistType
import com.maxrave.domain.mediaservice.handler.DownloadHandler
import com.maxrave.domain.mediaservice.handler.QueueData
import com.maxrave.domain.repository.AnalyticsRepository
import com.maxrave.domain.repository.ArtistRepository
import com.maxrave.domain.repository.SongRepository
import com.maxrave.domain.utils.Resource
import com.maxrave.domain.utils.toArrayListTrack
import com.maxrave.domain.utils.LiveStreamType
import com.maxrave.domain.utils.toTrack
import com.maxrave.logger.LogLevel
import com.maxrave.simpmusic.ui.screen.home.analytics.monthFullNameResource
import com.maxrave.simpmusic.ui.screen.library.LibraryDynamicPlaylistType
import com.maxrave.simpmusic.viewModel.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.lastOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlinx.datetime.atTime
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import org.koin.core.component.inject
import simpmusic.composeapp.generated.resources.Res
import simpmusic.composeapp.generated.resources.playlist
import simpmusic.composeapp.generated.resources.downloaded
import simpmusic.composeapp.generated.resources.downloading
import simpmusic.composeapp.generated.resources.error
import simpmusic.composeapp.generated.resources.removed_all_downloads
import simpmusic.composeapp.generated.resources.wrapped_recap_month
import simpmusic.composeapp.generated.resources.wrapped_recap_month_year

enum class MostPlayedPeriod {
    TODAY,
    THIS_WEEK,
    THIS_MONTH,
    THIS_YEAR,
    ALL_TIME,
}

class LibraryDynamicPlaylistViewModel(
    private val songRepository: SongRepository,
    private val artistRepository: ArtistRepository,
    private val analyticsRepository: AnalyticsRepository,
    private val sharedViewModel: SharedViewModel,
) : BaseViewModel() {
    private val _listFavoriteSong: MutableStateFlow<List<SongEntity>> = MutableStateFlow(emptyList())
    val listFavoriteSong: StateFlow<List<SongEntity>> get() = _listFavoriteSong
    private val _listFavoritePodcastEpisodes: MutableStateFlow<List<SongEntity>> = MutableStateFlow(emptyList())
    val listFavoritePodcastEpisodes: StateFlow<List<SongEntity>> get() = _listFavoritePodcastEpisodes
    private val downloadUtils: DownloadHandler by inject()
    private var favoriteDownloadJob: Job? = null
    val favoriteDownloadState =
        listFavoriteSong.map { songs ->
            when {
                songs.isNotEmpty() && songs.all { it.downloadState == DownloadState.STATE_DOWNLOADED } ->
                    DownloadState.STATE_DOWNLOADED
                songs.any {
                    it.downloadState == DownloadState.STATE_PREPARING || it.downloadState == DownloadState.STATE_DOWNLOADING
                } -> DownloadState.STATE_DOWNLOADING
                else -> DownloadState.STATE_NOT_DOWNLOADED
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DownloadState.STATE_NOT_DOWNLOADED)

    private val _listFollowedArtist: MutableStateFlow<List<ArtistEntity>> = MutableStateFlow(emptyList())
    val listFollowedArtist: StateFlow<List<ArtistEntity>> get() = _listFollowedArtist

    private val _listMostPlayedSong: MutableStateFlow<List<SongEntity>> = MutableStateFlow(emptyList())
    val listMostPlayedSong: StateFlow<List<SongEntity>> get() = _listMostPlayedSong
    private val _mostPlayedPlayCounts: MutableStateFlow<Map<String, Int>> = MutableStateFlow(emptyMap())
    val mostPlayedPlayCounts: StateFlow<Map<String, Int>> get() = _mostPlayedPlayCounts
    private val _mostPlayedPeriod = MutableStateFlow(MostPlayedPeriod.ALL_TIME)
    val mostPlayedPeriod: StateFlow<MostPlayedPeriod> get() = _mostPlayedPeriod

    private val _listDownloadedSong: MutableStateFlow<List<SongEntity>> = MutableStateFlow(emptyList())
    val listDownloadedSong: StateFlow<List<SongEntity>> get() = _listDownloadedSong

    /**
     * One month's top songs, filled in only once a route names the month.
     *
     * Unlike the four lists above it is not started in [init] and not observed: there is no
     * "current" month here — the screen may be showing any of the last twelve — so loading is
     * driven by [getMonthlyRecapSong].
     */
    private val _listMonthlyRecapSong: MutableStateFlow<List<SongEntity>> = MutableStateFlow(emptyList())
    val listMonthlyRecapSong: StateFlow<List<SongEntity>> get() = _listMonthlyRecapSong

    /**
     * The name of the month [_listMonthlyRecapSong] currently holds, resolved when it was loaded.
     *
     * Resolved there rather than where the queue is built because "Recap January" needs a format
     * argument, and the only `getString` that takes one suspends — [playAll] and [shuffle] do not.
     * Loading already runs in a coroutine, so the name comes free at the one moment it is knowable.
     */
    private var loadedRecapName: String? = null

    /**
     * Liked songs crediting one artist, filled in once a route names the artist — the same shape as
     * [_listMonthlyRecapSong], since there is no artist to observe until then.
     */
    private val _listArtistLikedSong: MutableStateFlow<List<SongEntity>> = MutableStateFlow(emptyList())
    val listArtistLikedSong: StateFlow<List<SongEntity>> get() = _listArtistLikedSong

    /** That artist's name for the header, or null when the artist was never stored. */
    private val _artistLikedName: MutableStateFlow<String?> = MutableStateFlow(null)
    val artistLikedName: StateFlow<String?> get() = _artistLikedName
    private val _artistLikedThumbnail: MutableStateFlow<String?> = MutableStateFlow(null)
    val artistLikedThumbnail: StateFlow<String?> get() = _artistLikedThumbnail

    private val startedCollections = mutableSetOf<String>()
    private var localFavoriteSongs: List<SongEntity> = emptyList()
    private var youtubeFavoriteSongs: List<SongEntity> = emptyList()
    private var youtubeLiveFavoriteIds: Set<String> = emptySet()

    /** Start only the database observer needed by the playlist currently being opened. */
    fun observe(type: LibraryDynamicPlaylistType) {
        when (type) {
            LibraryDynamicPlaylistType.Favorite ->
                if (startedCollections.add("favorite")) getFavoriteSong()

            LibraryDynamicPlaylistType.Followed ->
                if (startedCollections.add("followed")) getFollowedArtist()

            LibraryDynamicPlaylistType.MostPlayed ->
                if (startedCollections.add("mostPlayed")) getMostPlayedSong()

            LibraryDynamicPlaylistType.Downloaded ->
                if (startedCollections.add("downloaded")) getDownloadedSong()

            // These lists are loaded by their explicit route arguments, or by AnalyticsViewModel.
            is LibraryDynamicPlaylistType.TopTracks,
            is LibraryDynamicPlaylistType.TopArtists,
            is LibraryDynamicPlaylistType.TopAlbums,
            is LibraryDynamicPlaylistType.MonthlyRecap,
            is LibraryDynamicPlaylistType.ArtistLiked -> Unit
        }
    }

    private fun getFavoriteSong() {
        viewModelScope.launch {
            songRepository.observeLikedSongs().collectLatest { likedSong ->
                localFavoriteSongs =
                    withContext(Dispatchers.Default) {
                        likedSong.sortedByDescending {
                            it.favoriteAt ?: REMOVED_SONG_DATE_TIME
                        }
                    }
                publishFavoriteSongs()
            }
        }
        viewModelScope.launch {
            sharedViewModel.youTubeLikedSongs.collectLatest { likedSongs ->
                // Keep local favorites available immediately, then overlay YouTube Music likes
                // when the signed-in account's Liked Music playlist has loaded.
                youtubeFavoriteSongs = likedSongs
                publishFavoriteSongs()
            }
        }
        viewModelScope.launch {
            sharedViewModel.youTubeLiveLikedVideoIds.collectLatest { liveIds ->
                youtubeLiveFavoriteIds = liveIds
                publishFavoriteSongs()
            }
        }
    }

    /** Keep YouTube's entry for overlaps, then append only local favorites not already liked there. */
    private fun publishFavoriteSongs() {
        val uniqueYouTubeSongs =
            youtubeFavoriteSongs
                .filterNot { LiveStreamType.isLiveStream(it) }
                .distinctBy { it.videoId }
        val youtubeIds = uniqueYouTubeSongs.mapTo(mutableSetOf()) { it.videoId }
        val localById = localFavoriteSongs.associateBy { it.videoId }
        val mergedFavorites =
            uniqueYouTubeSongs.map { song ->
                // Keep live offline state even when the remote entry supplies the title/artwork.
                localById[song.videoId]?.let { local ->
                    song.copy(downloadState = local.downloadState, downloadedAt = local.downloadedAt)
                } ?: song
            } + localFavoriteSongs.filterNot {
                it.videoId in youtubeIds ||
                    it.videoId in youtubeLiveFavoriteIds ||
                    LiveStreamType.isLiveStream(it)
            }
        // YouTube likes are cached into the local song table too. Podcast favorites must follow
        // its live liked flag: the remote snapshot can still contain an episode after the player
        // or its row has unfavorited it, and merging it back keeps the page stale until a reload.
        _listFavoritePodcastEpisodes.value = localFavoriteSongs.filter { it.isPodcastEpisode() }
        _listFavoriteSong.value = mergedFavorites.filterNot { it.isPodcastEpisode() }
    }

    private fun SongEntity.isPodcastEpisode(): Boolean =
        resultType.equals("Podcast", ignoreCase = true) || category.equals("Podcast", ignoreCase = true)

    fun downloadFavorites() {
        if (favoriteDownloadJob?.isActive == true || listFavoriteSong.value.isEmpty()) return
        when (favoriteDownloadState.value) {
            DownloadState.STATE_DOWNLOADED -> {
                makeToast(getString(Res.string.downloaded))
                return
            }
            DownloadState.STATE_PREPARING, DownloadState.STATE_DOWNLOADING -> {
                makeToast(getString(Res.string.downloading))
                return
            }
        }
        val favorites = listFavoriteSong.value
        favoriteDownloadJob = viewModelScope.launch {
            val storedById =
                songRepository.getSongsByListVideoId(favorites.map { it.videoId })
                    .firstOrNull().orEmpty().associateBy { it.videoId }
            // Finish older favorites first so Downloads' newest-first order matches Favorite.
            val pending = favorites.asReversed().filter { song ->
                val state = storedById[song.videoId]?.downloadState ?: song.downloadState
                state != DownloadState.STATE_DOWNLOADED &&
                    state != DownloadState.STATE_PREPARING &&
                    state != DownloadState.STATE_DOWNLOADING &&
                    !LiveStreamType.isLiveStream(song)
            }
            if (pending.isEmpty()) return@launch
            makeToast(getString(Res.string.downloading))
            pending.forEach { song ->
                if (song.videoId !in storedById) songRepository.insertSong(song).firstOrNull()
                songRepository.updateDownloadState(song.videoId, DownloadState.STATE_PREPARING)
                downloadUtils.downloadTrack(song.videoId, song.title, song.thumbnails.orEmpty())
            }
        }
    }

    private fun getFollowedArtist() {
        viewModelScope.launch {
            artistRepository.getFollowedArtists().collectLatest { followedArtist ->
                _listFollowedArtist.value =
                    withContext(Dispatchers.Default) {
                        followedArtist.sortedByDescending {
                            it.followedAt ?: REMOVED_SONG_DATE_TIME
                        }
                    }
            }
        }
    }

    private fun getMostPlayedSong() {
        viewModelScope.launch {
            mostPlayedPeriod.collectLatest { period ->
                val end = now()
                val start = period.startOf(end)
                _listMostPlayedSong.value = emptyList()
                _mostPlayedPlayCounts.value = emptyMap()
                analyticsRepository
                    .queryTopPlayedSongsInRange(startTimestamp = start, endTimestamp = end)
                    .collectLatest { playedTracks ->
                        val rankedSongs =
                            withContext(Dispatchers.IO) {
                                val cappedTracks = playedTracks.take(MOST_PLAYED_LIMIT)
                                val songsById =
                                    songRepository
                                        .getSongsByListVideoId(cappedTracks.map { it.videoId })
                                        .lastOrNull()
                                        .orEmpty()
                                        .associateBy { it.videoId }
                                cappedTracks.mapNotNull { track ->
                                    val song = songsById[track.videoId] ?: return@mapNotNull null
                                    if (LiveStreamType.isLiveStream(song)) null else track to song
                                }
                            }
                        _mostPlayedPlayCounts.value = rankedSongs.associate { (track, song) -> song.videoId to track.playCount }
                        _listMostPlayedSong.value = rankedSongs.map { it.second }
                    }
            }
        }
    }

    fun setMostPlayedPeriod(period: MostPlayedPeriod) {
        _mostPlayedPeriod.value = period
    }

    private fun MostPlayedPeriod.startOf(end: LocalDateTime): LocalDateTime {
        val date = end.date
        val firstDate =
            when (this) {
                MostPlayedPeriod.TODAY -> date
                MostPlayedPeriod.THIS_WEEK -> date.minus(date.dayOfWeek.isoDayNumber - 1, DateTimeUnit.DAY)
                MostPlayedPeriod.THIS_MONTH -> LocalDate(date.year, date.month, 1)
                MostPlayedPeriod.THIS_YEAR -> LocalDate(date.year, Month.JANUARY, 1)
                MostPlayedPeriod.ALL_TIME -> LocalDate(1, Month.JANUARY, 1)
            }
        return firstDate.atTime(0, 0)
    }

    private fun getDownloadedSong() {
        viewModelScope.launch {
            songRepository.observeDownloadedSongs().collectLatest { downloadedSong ->
                _listDownloadedSong.value =
                    withContext(Dispatchers.Default) {
                        downloadedSong.filterNot { LiveStreamType.isLiveStream(it) }
                    }
            }
        }
    }

    /** Remove every completed download from storage and let the database observer refresh the list. */
    fun removeAllDownloads() {
        val downloadedSongs = listDownloadedSong.value
        if (downloadedSongs.isEmpty()) return
        viewModelScope.launch {
            var failed = false
            withContext(Dispatchers.IO) {
                downloadedSongs.forEach { song ->
                    try {
                        downloadUtils.removeDownload(song.videoId)
                        songRepository.updateDownloadState(song.videoId, DownloadState.STATE_NOT_DOWNLOADED)
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (error: Exception) {
                        failed = true
                        log("Failed to remove download for ${song.videoId}: ${error.message}", LogLevel.ERROR)
                    }
                }
            }
            makeToast(
                getString(
                    if (failed) Res.string.error else Res.string.removed_all_downloads,
                ),
            )
        }
    }

    /**
     * One calendar month's top songs, newest ranking first.
     *
     * Same two steps the Analytics screen's top-tracks list takes — the ranking from
     * [com.maxrave.domain.repository.AnalyticsRepository], then each row's `videoId` paired with
     * its stored [SongEntity] — so a recap and the Analytics list can never disagree about what
     * was played.
     *
     * The month's bounds are local wall-clock and inclusive at both ends, matching
     * `WrappedViewModel.yearRange`: the query underneath matches with `BETWEEN`, so ending at
     * midnight on the 1st of the next month would drop the last day of this one.
     */
    fun getMonthlyRecapSong(recap: LibraryDynamicPlaylistType.MonthlyRecap) {
        viewModelScope.launch {
            loadedRecapName = recapName(recap)
            val firstDay = LocalDate(recap.year, recap.month, 1)
            val lastDay = firstDay.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY)
            analyticsRepository
                .queryTopPlayedSongsInRange(
                    startTimestamp = firstDay.atTime(0, 0),
                    endTimestamp = lastDay.atTime(23, 59, 59),
                ).collectLatest { rows ->
                    _listMonthlyRecapSong.value =
                        rows
                            // Capped after resolving, not before: a row whose song row is gone
                            // would otherwise leave a "top 50" 49 long. The query already stops at
                            // 100, so this reaches at most that far — the same shape as
                            // `WrappedViewModel.resolveTop`.
                            .mapNotNull { songRepository.getSongById(it.videoId).lastOrNull() }
                            .filterNot { LiveStreamType.isLiveStream(it) }
                            .take(MONTHLY_RECAP_LIMIT)
                }
        }
    }

    /**
     * Liked songs crediting [artistLiked]'s artist, derived from the same merged local and YouTube
     * Music Favorites list so the detail page matches Favorites. Observed so unliking a song drops
     * it from the list immediately.
     */
    fun getArtistLikedSong(artistLiked: LibraryDynamicPlaylistType.ArtistLiked) {
        observe(LibraryDynamicPlaylistType.Favorite)
        _artistLikedName.value = null
        _artistLikedThumbnail.value = null
        viewModelScope.launch {
            val artist = artistRepository.getArtistById(artistLiked.channelId).firstOrNull()
            _artistLikedName.value = artist?.name
            _artistLikedThumbnail.value = artist?.thumbnails?.takeIf(String::isNotBlank)

            // The page hero represents the artist, while the song rows keep their track artwork.
            // Resolve the official channel thumbnail when this artist has not been browsed yet.
            if (_artistLikedThumbnail.value.isNullOrBlank()) {
                val browse = artistRepository.getArtistData(artistLiked.channelId).lastOrNull()
                if (browse is Resource.Success) {
                    browse.data?.let { data ->
                        _artistLikedName.value = data.name.takeIf(String::isNotBlank) ?: _artistLikedName.value
                        _artistLikedThumbnail.value = data.thumbnails?.lastOrNull()?.url?.takeIf(String::isNotBlank)
                    }
                }
            }
        }
        viewModelScope.launch {
            listFavoriteSong.collectLatest { likedSongs ->
                val artistSongs = likedSongs.filter { artistLiked.channelId in it.artistId.orEmpty() }
                _listArtistLikedSong.value = artistSongs.sortedByDescending {
                    it.favoriteAt ?: REMOVED_SONG_DATE_TIME
                }
                val firstArtistSong = artistSongs.firstOrNull()
                val artistIndex = firstArtistSong?.artistId.orEmpty().indexOf(artistLiked.channelId)
                if (_artistLikedName.value.isNullOrBlank()) {
                    _artistLikedName.value = firstArtistSong?.artistName?.getOrNull(artistIndex)
                }
            }
        }
    }

    /** "Recap January", or "Recap January 2025" once the year stops being obvious. */
    private suspend fun recapName(recap: LibraryDynamicPlaylistType.MonthlyRecap): String {
        val month =
            org.jetbrains.compose.resources
                .getString(monthFullNameResource(Month(recap.month)))
        return if (recap.year == now().date.year) {
            org.jetbrains.compose.resources
                .getString(Res.string.wrapped_recap_month, month)
        } else {
            org.jetbrains.compose.resources
                .getString(Res.string.wrapped_recap_month_year, month, recap.year.toString())
        }
    }

    /**
     * What the Now Playing screen calls the queue this screen started.
     *
     * A recap cannot answer [LibraryDynamicPlaylistType.name] with its own name, so it is the one
     * case read from the name resolved at load time instead.
     */
    private fun playlistName(type: LibraryDynamicPlaylistType): String {
        val name =
            when (type) {
                is LibraryDynamicPlaylistType.MonthlyRecap -> loadedRecapName ?: getString(type.name())
                is LibraryDynamicPlaylistType.ArtistLiked ->
                    listOfNotNull(getString(type.name()), _artistLikedName.value).joinToString(" · ")
                else -> getString(type.name())
            }
        return "${getString(Res.string.playlist)} $name"
    }

    fun playSong(
        videoId: String,
        type: LibraryDynamicPlaylistType,
    ) {
        val (targetList, playTrack) =
            when (type) {
                LibraryDynamicPlaylistType.Favorite -> listFavoriteSong.value to listFavoriteSong.value.find { it.videoId == videoId }
                LibraryDynamicPlaylistType.Downloaded -> listDownloadedSong.value to listDownloadedSong.value.find { it.videoId == videoId }
                LibraryDynamicPlaylistType.Followed -> return
                LibraryDynamicPlaylistType.MostPlayed -> listMostPlayedSong.value to listMostPlayedSong.value.find { it.videoId == videoId }
                is LibraryDynamicPlaylistType.MonthlyRecap ->
                    listMonthlyRecapSong.value to listMonthlyRecapSong.value.find { it.videoId == videoId }
                is LibraryDynamicPlaylistType.ArtistLiked ->
                    listArtistLikedSong.value to listArtistLikedSong.value.find { it.videoId == videoId }
                else -> return
            }
        if (playTrack == null) return
        setQueueData(
            QueueData.Data(
                listTracks = targetList.toArrayListTrack(),
                firstPlayedTrack = playTrack.toTrack(),
                playlistId = null,
                playlistName = playlistName(type),
                playlistType = PlaylistType.RADIO,
                continuation = null,
            ),
        )
        loadMediaItem(
            playTrack.toTrack(),
            Config.PLAYLIST_CLICK,
            targetList.indexOf(playTrack).coerceAtLeast(0),
        )
    }

    private fun getSongList(type: LibraryDynamicPlaylistType): List<SongEntity> =
        when (type) {
            LibraryDynamicPlaylistType.Favorite -> listFavoriteSong.value
            LibraryDynamicPlaylistType.Downloaded -> listDownloadedSong.value
            LibraryDynamicPlaylistType.MostPlayed -> listMostPlayedSong.value
            is LibraryDynamicPlaylistType.MonthlyRecap -> listMonthlyRecapSong.value
            is LibraryDynamicPlaylistType.ArtistLiked -> listArtistLikedSong.value
            else -> emptyList()
        }

    fun playAll(type: LibraryDynamicPlaylistType) {
        val targetList = getSongList(type)
        val firstTrack = targetList.firstOrNull() ?: return
        setQueueData(
            QueueData.Data(
                listTracks = targetList.toArrayListTrack(),
                firstPlayedTrack = firstTrack.toTrack(),
                playlistId = null,
                playlistName = playlistName(type),
                playlistType = PlaylistType.RADIO,
                continuation = null,
            ),
        )
        loadMediaItem(
            firstTrack.toTrack(),
            Config.PLAYLIST_CLICK,
            0,
        )
    }

    fun shuffle(type: LibraryDynamicPlaylistType) {
        val targetList = getSongList(type)
        if (targetList.isEmpty()) return
        val shuffledList = targetList.shuffled()
        val firstTrack = shuffledList.first()
        setQueueData(
            QueueData.Data(
                listTracks = shuffledList.toArrayListTrack(),
                firstPlayedTrack = firstTrack.toTrack(),
                playlistId = null,
                playlistName = playlistName(type),
                playlistType = PlaylistType.RADIO,
                continuation = null,
            ),
        )
        loadMediaItem(
            firstTrack.toTrack(),
            Config.PLAYLIST_CLICK,
            0,
        )
    }

    companion object {
        /** Songs a monthly recap holds. The ranking underneath already stops at 100. */
        private const val MONTHLY_RECAP_LIMIT = 50
        private const val MOST_PLAYED_LIMIT = 100
    }
}
