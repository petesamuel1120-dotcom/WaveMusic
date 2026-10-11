package com.maxrave.simpmusic.viewModel

import androidx.lifecycle.viewModelScope
import com.maxrave.common.Config
import com.maxrave.common.LibraryChipType
import com.maxrave.domain.data.entities.AlbumEntity
import com.maxrave.domain.data.entities.LocalPlaylistEntity
import com.maxrave.domain.data.entities.PlaylistEntity
import com.maxrave.domain.data.entities.SongEntity
import com.maxrave.domain.data.model.browse.album.Track
import com.maxrave.domain.data.model.browse.playlist.PlaylistBrowse
import com.maxrave.domain.data.model.browse.playlist.PlaylistState
import com.maxrave.domain.data.model.searchResult.playlists.PlaylistsResult
import com.maxrave.domain.data.type.ChartItem
import com.maxrave.domain.data.type.MonthlyRecapItem
import com.maxrave.domain.data.type.PlaylistType
import com.maxrave.domain.data.type.RecentlyType
import com.maxrave.domain.extension.now
import com.maxrave.domain.manager.DataStoreManager
import com.maxrave.domain.repository.AlbumRepository
import com.maxrave.domain.repository.AnalyticsRepository
import com.maxrave.domain.repository.CommonRepository
import com.maxrave.domain.repository.LocalPlaylistRepository
import com.maxrave.domain.repository.PlaylistRepository
import com.maxrave.domain.repository.PodcastRepository
import com.maxrave.domain.repository.SongRepository
import com.maxrave.domain.utils.LocalResource
import com.maxrave.domain.utils.Resource
import com.maxrave.domain.utils.LiveStreamType
import com.maxrave.domain.utils.toArrayListTrack
import com.maxrave.domain.utils.isRadioPlaylistId
import com.maxrave.simpmusic.ui.screen.home.analytics.monthFullNameResource
import com.maxrave.simpmusic.ui.screen.library.LibraryDynamicPlaylistType
import com.maxrave.simpmusic.viewModel.base.BaseViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.lastOrNull
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlinx.datetime.atTime
import kotlinx.datetime.minus
import kotlinx.datetime.number
import kotlinx.datetime.plus
import simpmusic.composeapp.generated.resources.Res
import simpmusic.composeapp.generated.resources.added_local_playlist
import simpmusic.composeapp.generated.resources.error
import simpmusic.composeapp.generated.resources.error_occurred
import simpmusic.composeapp.generated.resources.synced
import simpmusic.composeapp.generated.resources.unsynced
import simpmusic.composeapp.generated.resources.updated
import simpmusic.composeapp.generated.resources.updated_to_youtube_playlist
import simpmusic.composeapp.generated.resources.delete
import simpmusic.composeapp.generated.resources.playlist_deleted
import simpmusic.composeapp.generated.resources.added_to_queue
import simpmusic.composeapp.generated.resources.playlist_is_empty
import simpmusic.composeapp.generated.resources.view_count
import simpmusic.composeapp.generated.resources.wrapped_recap_month
import simpmusic.composeapp.generated.resources.wrapped_recap_month_year

class LibraryViewModel(
    private val dataStoreManager: DataStoreManager,
    private val analyticsRepository: AnalyticsRepository,
    private val songRepository: SongRepository,
    private val commonRepository: CommonRepository,
    private val playlistRepository: PlaylistRepository,
    private val localPlaylistRepository: LocalPlaylistRepository,
    private val albumRepository: AlbumRepository,
    private val podcastRepository: PodcastRepository,
) : BaseViewModel() {
    // Keep one request or database observer per section so restoring a tab does not stack jobs.
    private var recentlyAddedJob: Job? = null
    private var youTubePlaylistJob: Job? = null
    private var canvasSongJob: Job? = null
    private var youTubeMixForYouJob: Job? = null
    private var favoritePlaylistJob: Job? = null
    private var favoritePodcastsJob: Job? = null
    private var localPlaylistJob: Job? = null
    private var downloadedPlaylistJob: Job? = null
    private var monthlyRecapsJob: Job? = null
    private var chartPlaylistsJob: Job? = null

    private fun <T> LocalResource<T>.hasSuccessfulResult(): Boolean =
        this is LocalResource.Success

    private val _currentScreen: MutableStateFlow<LibraryChipType> = MutableStateFlow(LibraryChipType.YOUR_LIBRARY)
    val currentScreen: StateFlow<LibraryChipType> get() = _currentScreen.asStateFlow()
    private val _currentScreenRestored = MutableStateFlow(false)
    val currentScreenRestored: StateFlow<Boolean> get() = _currentScreenRestored.asStateFlow()
    private val _recentlyAdded: MutableStateFlow<LocalResource<List<RecentlyType>>> =
        MutableStateFlow(LocalResource.Loading())
    val recentlyAdded: StateFlow<LocalResource<List<RecentlyType>>> get() = _recentlyAdded.asStateFlow()

    private val _yourLocalPlaylist: MutableStateFlow<LocalResource<List<LocalPlaylistEntity>>> =
        MutableStateFlow(LocalResource.Loading())
    val yourLocalPlaylist: StateFlow<LocalResource<List<LocalPlaylistEntity>>> get() = _yourLocalPlaylist.asStateFlow()

    private val _youTubePlaylist: MutableStateFlow<LocalResource<List<PlaylistsResult>>> =
        MutableStateFlow(LocalResource.Loading())
    val youTubePlaylist: StateFlow<LocalResource<List<PlaylistsResult>>> get() = _youTubePlaylist.asStateFlow()

    private val _youTubeMixForYou: MutableStateFlow<LocalResource<List<PlaylistsResult>>> =
        MutableStateFlow(LocalResource.Loading())
    val youTubeMixForYou: StateFlow<LocalResource<List<PlaylistsResult>>> get() = _youTubeMixForYou.asStateFlow()

    private val _favoritePlaylist: MutableStateFlow<LocalResource<List<PlaylistType>>> =
        MutableStateFlow(LocalResource.Loading())
    val favoritePlaylist: StateFlow<LocalResource<List<PlaylistType>>> get() = _favoritePlaylist.asStateFlow()

    private val _favoritePodcasts: MutableStateFlow<LocalResource<List<PlaylistType>>> =
        MutableStateFlow(LocalResource.Loading())
    val favoritePodcasts: StateFlow<LocalResource<List<PlaylistType>>> get() = _favoritePodcasts.asStateFlow()

    private val _downloadedPlaylist: MutableStateFlow<LocalResource<List<PlaylistType>>> =
        MutableStateFlow(LocalResource.Loading())
    val downloadedPlaylist: StateFlow<LocalResource<List<PlaylistType>>> get() = _downloadedPlaylist.asStateFlow()

    private val _chartPlaylists: MutableStateFlow<LocalResource<List<ChartItem>>> =
        MutableStateFlow(LocalResource.Loading())
    val chartPlaylists: StateFlow<LocalResource<List<ChartItem>>> get() = _chartPlaylists.asStateFlow()

    private val _listCanvasSong: MutableStateFlow<LocalResource<List<SongEntity>>> =
        MutableStateFlow(LocalResource.Loading())
    val listCanvasSong: StateFlow<LocalResource<List<SongEntity>>> get() = _listCanvasSong.asStateFlow()

    /**
     * The months the Wrapped tab offers a recap for, newest first.
     *
     * A [MonthlyRecapItem] rather than the destination's own
     * [LibraryDynamicPlaylistType.MonthlyRecap]: the tab draws these through the shared
     * `GridLibraryPlaylist`, which renders only [PlaylistType]s, and a tile needs a title and a
     * cover on top of the year and month the destination carries. The destination is rebuilt from
     * the year and month when a tile is tapped.
     */
    private val _monthlyRecaps: MutableStateFlow<LocalResource<List<MonthlyRecapItem>>> =
        MutableStateFlow(LocalResource.Loading())
    val monthlyRecaps: StateFlow<LocalResource<List<MonthlyRecapItem>>> get() = _monthlyRecaps.asStateFlow()

    private val _accountThumbnail: MutableStateFlow<String?> = MutableStateFlow(null)
    val accountThumbnail: StateFlow<String?> get() = _accountThumbnail.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val youtubeLoggedIn = dataStoreManager.loggedIn.mapLatest { it == DataStoreManager.TRUE }

    /**
     * Whether the Wrapped chip has anything behind it.
     *
     * The same setting the Analytics tab follows, read the same way — Wrapped and the recaps are
     * built entirely from `playback_event`, which local tracking is what fills.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val localTrackingEnabled = dataStoreManager.localTrackingEnabled.mapLatest { it == DataStoreManager.TRUE }

    init {
        viewModelScope.launch {
            val currentScreenJob =
                launch {
                    dataStoreManager.getString("library_current_screen").first()?.let { chipType ->
                        LibraryChipType.fromStringValue(chipType)?.let {
                            _currentScreen.value = it
                        }
                    }
                    _currentScreenRestored.value = true
                }
            val accountThumbnailJob =
                launch {
                    dataStoreManager.getString("AccountThumbUrl").distinctUntilChanged().collect { thumbnail ->
                        _accountThumbnail.value = thumbnail.takeIf { !it.isNullOrEmpty() }
                    }
                }
            currentScreenJob.join()
            accountThumbnailJob.join()
        }
    }

    fun setCurrentScreen(chipType: LibraryChipType, persist: Boolean = true) {
        _currentScreen.value = chipType
        if (persist) {
            viewModelScope.launch {
                dataStoreManager.putString("library_current_screen", chipType.toStringValue())
            }
        }
    }

    fun getRecentlyAdded(forceRefresh: Boolean = false) {
        if (recentlyAddedJob?.isActive == true) {
            if (!forceRefresh) return
            recentlyAddedJob?.cancel()
        }
        // Keep observing after the first result so visits and playback update History in place.
        if (_recentlyAdded.value !is LocalResource.Success) {
            _recentlyAdded.value = LocalResource.Loading()
        }
        recentlyAddedJob = viewModelScope.launch {
            commonRepository.getAllRecentData().collectLatest { data ->
                val recentItems = withContext(Dispatchers.Default) {
                    val temp: MutableList<RecentlyType> = data.toMutableList()
                    temp.removeAll { it is PlaylistEntity && (it.id == "LM" || it.id.isRadioPlaylistId()) }
                    temp.removeAll { it is SongEntity && it.inLibrary == Config.REMOVED_SONG_DATE_TIME }
                    temp.reverse()
                    temp.toImmutableList()
                }
                _recentlyAdded.value = LocalResource.Success(recentItems)
            }
        }
    }

    fun getYouTubePlaylist(forceRefresh: Boolean = false) {
        if (youTubePlaylistJob?.isActive == true) {
            if (!forceRefresh) return
            youTubePlaylistJob?.cancel()
        }
        if (!forceRefresh && _youTubePlaylist.value.hasSuccessfulResult()) return
        if (forceRefresh || _youTubePlaylist.value !is LocalResource.Success) {
            _youTubePlaylist.value = LocalResource.Loading()
        }
        youTubePlaylistJob = viewModelScope.launch {
            fun visiblePlaylists(playlists: List<PlaylistsResult>): List<PlaylistsResult> {
                val appProvidedAlternatives = setOf("liked music", "new episodes", "episodes for later")
                return playlists.filterNot { playlist ->
                    playlist.title.trim().lowercase() in appProvidedAlternatives
                }
            }

            val accountCacheKey = "youtube_library_playlists_${dataStoreManager.cookie.first().hashCode()}"
            if (!forceRefresh) {
                dataStoreManager.getString(accountCacheKey).first()?.let { json ->
                    runCatching {
                        Json.decodeFromString(ListSerializer(PlaylistsResult.serializer()), json)
                    }.getOrNull()?.let { cached ->
                        _youTubePlaylist.value = LocalResource.Success(visiblePlaylists(cached))
                    }
                }
            }
            playlistRepository.getLibraryPlaylist().collect { data ->
                if (data != null) {
                    val visiblePlaylists = visiblePlaylists(data)
                    _youTubePlaylist.value = LocalResource.Success(visiblePlaylists)
                    runCatching {
                        dataStoreManager.putString(
                            accountCacheKey,
                            Json.encodeToString(ListSerializer(PlaylistsResult.serializer()), visiblePlaylists),
                        )
                    }
                } else if (_youTubePlaylist.value !is LocalResource.Success) {
                    _youTubePlaylist.value = LocalResource.Success(emptyList())
                }
            }
        }
    }

    fun removeYouTubePlaylistFromCache(playlistId: String) {
        val normalizedId = playlistId.removePrefix("VL")
        val remaining =
            _youTubePlaylist.value.data
                ?.filterNot { it.browseId.removePrefix("VL") == normalizedId }
                ?: return
        _youTubePlaylist.value = LocalResource.Success(remaining)
        viewModelScope.launch {
            val accountCacheKey = "youtube_library_playlists_${dataStoreManager.cookie.first().hashCode()}"
            runCatching {
                dataStoreManager.putString(
                    accountCacheKey,
                    Json.encodeToString(ListSerializer(PlaylistsResult.serializer()), remaining),
                )
            }
        }
    }

    fun getYouTubeMixedForYou() {
        if (youTubeMixForYouJob?.isActive == true) return
        if (_youTubeMixForYou.value.hasSuccessfulResult()) return
        _youTubeMixForYou.value = LocalResource.Loading()
        youTubeMixForYouJob = viewModelScope.launch {
            playlistRepository.getMixedForYou().collect { data ->
                _youTubeMixForYou.value = LocalResource.Success(data ?: emptyList())
            }
        }
    }

    fun getYouTubeLoggedIn(): Boolean = runBlocking { dataStoreManager.loggedIn.first() } == DataStoreManager.TRUE

    fun getPlaylistFavorite(forceRefresh: Boolean = false) {
        if (favoritePlaylistJob?.isActive == true) {
            if (!forceRefresh) return
            favoritePlaylistJob?.cancel()
        }
        if (!forceRefresh && _favoritePlaylist.value.hasSuccessfulResult()) return
        favoritePlaylistJob = viewModelScope.launch {
            combine(
                albumRepository.getLikedAlbums(),
                playlistRepository.getLikedPlaylists(),
            ) { albums, playlists ->
                val allFavorites: MutableList<PlaylistType> = mutableListOf()
                allFavorites.addAll(albums)
                allFavorites.addAll(playlists)
                allFavorites.sortedWith(
                    Comparator { first, second ->
                        fun favoriteTime(item: PlaylistType): LocalDateTime? =
                            when (item) {
                                is AlbumEntity -> item.favoriteAt ?: item.inLibrary
                                is PlaylistEntity -> item.favoriteAt ?: item.inLibrary
                                else -> null
                            }
                        val firstTime = favoriteTime(first)
                        val secondTime = favoriteTime(second)
                        when {
                            firstTime == null && secondTime == null -> 0
                            firstTime == null -> -1
                            secondTime == null -> 1
                            else -> firstTime.compareTo(secondTime)
                        }
                    },
                )
            }.flowOn(Dispatchers.Default).collect { sortedFavorites ->
                _favoritePlaylist.value = LocalResource.Success(sortedFavorites)
            }
        }
    }
    fun getFavoritePodcasts(forceRefresh: Boolean = false) {
        if (favoritePodcastsJob?.isActive == true) {
            if (!forceRefresh) return
            favoritePodcastsJob?.cancel()
        }
        if (!forceRefresh && _favoritePodcasts.value.hasSuccessfulResult()) return
        favoritePodcastsJob = viewModelScope.launch {
            podcastRepository.getFavoritePodcasts()
                .map { podcasts -> podcasts.sortedByDescending { it.favoriteTime } }
                .flowOn(Dispatchers.Default)
                .collectLatest { sortedList ->
                    _favoritePodcasts.value = LocalResource.Success(sortedList)
                }
        }
    }

    fun getCanvasSong(forceRefresh: Boolean = false) {
        if (canvasSongJob?.isActive == true) {
            if (!forceRefresh) return
            canvasSongJob?.cancel()
        }
        if (!forceRefresh && _listCanvasSong.value.hasSuccessfulResult()) return
        _listCanvasSong.value = LocalResource.Loading()
        canvasSongJob = viewModelScope.launch {
            songRepository.getCanvasSong(max = 5).collect { data ->
                _listCanvasSong.value = LocalResource.Success(data)
            }
        }
    }

    fun getLocalPlaylist(forceRefresh: Boolean = false) {
        if (localPlaylistJob?.isActive == true) {
            if (!forceRefresh) return
            localPlaylistJob?.cancel()
        }
        if (!forceRefresh && _yourLocalPlaylist.value.hasSuccessfulResult()) return
        _yourLocalPlaylist.value = LocalResource.Loading()
        localPlaylistJob = viewModelScope.launch {
            localPlaylistRepository.getAllLocalPlaylists()
                // Synced playlists are represented by their real YouTube Music entries in the
                // remote playlist list. Keep only unsynced local playlists in this collection.
                .map { values ->
                    values
                        .filter {
                            it.youtubePlaylistId.isNullOrBlank() &&
                                it.syncState != LocalPlaylistEntity.YouTubeSyncState.Synced
                        }.reversed()
                }
                .flowOn(Dispatchers.Default)
                .collect { values ->
                    _yourLocalPlaylist.value = LocalResource.Success(values)
                }
        }
    }

    fun getDownloadedPlaylist(forceRefresh: Boolean = false) {
        if (downloadedPlaylistJob?.isActive == true) {
            if (!forceRefresh) return
            downloadedPlaylistJob?.cancel()
        }
        if (!forceRefresh && _downloadedPlaylist.value.hasSuccessfulResult()) return
        downloadedPlaylistJob = viewModelScope.launch {
            playlistRepository.getAllDownloadedPlaylist().collect { values ->
                _downloadedPlaylist.value = LocalResource.Success(values)
            }
        }
    }

    /**
     * Which of the last twelve months the user actually listened in, and what each tile shows.
     *
     * A month with no plays is left out rather than shown empty: a "Recap March" that opens onto
     * nothing is worse than no row at all. Twelve is a cap, not a quota — a new install shows one
     * row, or none.
     *
     * The count comes first and gates everything after it: twelve `COUNT`s over an indexed
     * timestamp range are cheap, so the months with nothing in them are dropped before anything
     * asks them for a ranking. Only the survivors pay for a cover.
     *
     * Title and cover are resolved here rather than in the tile, which cannot suspend: the title
     * needs a month name out of a string resource with a format argument, and the cover needs a
     * ranking query followed by a song lookup.
     */
    fun getMonthlyRecaps(forceRefresh: Boolean = false) {
        if (monthlyRecapsJob?.isActive == true) {
            if (!forceRefresh) return
            monthlyRecapsJob?.cancel()
        }
        if (!forceRefresh && _monthlyRecaps.value.hasSuccessfulResult()) return
        _monthlyRecaps.value = LocalResource.Loading()
        monthlyRecapsJob = viewModelScope.launch {
            val today = now().date
            val thisMonth = LocalDate(today.year, today.month, 1)
            val months =
                (0 until MONTHS_OF_RECAP)
                    .map { thisMonth.minus(it, DateTimeUnit.MONTH) }
                    .mapNotNull { firstDay ->
                        val lastDay = firstDay.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY)
                        val start = firstDay.atTime(0, 0)
                        val end = lastDay.atTime(23, 59, 59)
                        val plays =
                            analyticsRepository
                                .getPlaybackEventCountInRange(
                                    startTimestamp = start,
                                    endTimestamp = end,
                                ).firstOrNull() ?: 0L
                        if (plays <= 0L) return@mapNotNull null
                        MonthlyRecapItem(
                            year = firstDay.year,
                            month = firstDay.month.number,
                            title = recapTitle(firstDay.year, firstDay.month, today.year),
                        )
                    }
            _monthlyRecaps.value = LocalResource.Success(months)
        }
    }

    /**
     * "Recap January", or "Recap January 2025" once the year stops being obvious.
     *
     * The same rule and the same two format strings as the header the tile opens — see
     * [LibraryDynamicPlaylistType.title]. Fully qualified because [BaseViewModel] has a `getString`
     * of its own that takes no format argument and wraps `runBlocking`, which has no business
     * running inside a coroutine that is already suspended here.
     */
    private suspend fun recapTitle(
        year: Int,
        month: Month,
        currentYear: Int,
    ): String {
        val monthName =
            org.jetbrains.compose.resources
                .getString(monthFullNameResource(month))
        return if (year == currentYear) {
            org.jetbrains.compose.resources
                .getString(Res.string.wrapped_recap_month, monthName)
        } else {
            org.jetbrains.compose.resources
                .getString(Res.string.wrapped_recap_month_year, monthName, year.toString())
        }
    }

    fun getChartPlaylists(forceRefresh: Boolean = false) {
        if (chartPlaylistsJob?.isActive == true) {
            if (!forceRefresh) return
            chartPlaylistsJob?.cancel()
        }
        if (!forceRefresh && _chartPlaylists.value.hasSuccessfulResult()) return
        _chartPlaylists.value = LocalResource.Loading()
        chartPlaylistsJob = viewModelScope.launch {
            playlistRepository.getChartPlaylist().collectLatest {
                when (it) {
                    is Resource.Success -> _chartPlaylists.value = LocalResource.Success(it.data ?: emptyList())
                    is Resource.Error -> _chartPlaylists.value = LocalResource.Error(it.message ?: "Unknown error")
                }
            }
        }
    }

    fun createPlaylist(title: String) {
        val trimmedTitle = title.trim()
        if (trimmedTitle.isEmpty()) return
        viewModelScope.launch {
            val hasYouTubeAccount =
                dataStoreManager.loggedIn.first() == DataStoreManager.TRUE &&
                    dataStoreManager.cookie.first().isNotBlank()
            if (hasYouTubeAccount) {
                createYouTubeMusicPlaylistNow(trimmedTitle)
                return@launch
            }
            val localPlaylistEntity =
                LocalPlaylistEntity(
                    title = trimmedTitle,
                    // A playlist created while signed out is uploaded after the next sign-in.
                    syncState = LocalPlaylistEntity.YouTubeSyncState.PendingSync,
                )
            localPlaylistRepository
                .insertLocalPlaylist(
                    localPlaylistEntity,
                    getString(Res.string.added_local_playlist),
                ).lastOrNull()
                ?.let {
                    log("Created playlist with id: $it")
                }
            getLocalPlaylist(forceRefresh = true)
        }
    }

    /** Creates a local mirror and syncs it immediately so this playlist also exists in YouTube Music. */
    fun createYouTubeMusicPlaylist(title: String) {
        val trimmedTitle = title.trim()
        if (trimmedTitle.isEmpty()) return

        viewModelScope.launch { createYouTubeMusicPlaylistNow(trimmedTitle) }
    }

    private suspend fun createYouTubeMusicPlaylistNow(title: String) {
        var localPlaylistId: Long? = null
        try {
            val isYouTubeAuthenticated =
                dataStoreManager.loggedIn.first() == DataStoreManager.TRUE &&
                    dataStoreManager.cookie.first().isNotBlank()
            val createdPlaylistId =
                localPlaylistRepository.insertLocalPlaylistWithTracks(
                    localPlaylist =
                        LocalPlaylistEntity(
                            title = title,
                            tracks = emptyList(),
                            syncState = if (isYouTubeAuthenticated) {
                                LocalPlaylistEntity.YouTubeSyncState.Syncing
                            } else {
                                LocalPlaylistEntity.YouTubeSyncState.PendingSync
                            },
                        ),
                    videoIds = emptyList(),
                )
            localPlaylistId = createdPlaylistId
            if (createdPlaylistId < 0L) {
                makeToast(getString(Res.string.error_occurred))
                return
            }

            if (isYouTubeAuthenticated) {
                val result =
                    localPlaylistRepository
                        .syncLocalPlaylistToYouTubePlaylist(
                            playlistId = createdPlaylistId,
                            successMessage = getString(Res.string.synced),
                            errorMessage = getString(Res.string.error),
                        ).lastOrNull()

                when (result) {
                    is LocalResource.Success -> getYouTubePlaylist(forceRefresh = true)
                    is LocalResource.Error -> {
                        localPlaylistRepository.updateLocalPlaylistYouTubePlaylistSyncState(
                            createdPlaylistId,
                            LocalPlaylistEntity.YouTubeSyncState.PendingSync,
                        )
                        makeToast(result.message ?: getString(Res.string.error))
                    }
                    else -> {
                        localPlaylistRepository.updateLocalPlaylistYouTubePlaylistSyncState(
                            createdPlaylistId,
                            LocalPlaylistEntity.YouTubeSyncState.PendingSync,
                        )
                        makeToast(getString(Res.string.error))
                    }
                }
            }
            getLocalPlaylist(forceRefresh = true)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Exception) {
            localPlaylistId?.takeIf { it >= 0L }?.let {
                localPlaylistRepository.updateLocalPlaylistYouTubePlaylistSyncState(
                    it,
                    LocalPlaylistEntity.YouTubeSyncState.PendingSync,
                )
            }
            getLocalPlaylist(forceRefresh = true)
            makeToast(getString(Res.string.error_occurred))
        }
    }

    // These menu actions use the same repositories as the playlist screens. Keep them in the
    // Library owner so closing a dialog cannot cancel a rename/delete already in progress.
    private fun runPlaylistMenuAction(action: suspend () -> Unit) {
        viewModelScope.launch {
            showLoadingDialog()
            try {
                action()
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                makeToast(getString(Res.string.error))
            } finally {
                hideLoadingDialog()
            }
        }
    }

    private fun reportLocalPlaylistAction(result: LocalResource<String>?) {
        when (result) {
            is LocalResource.Success -> makeToast(result.data ?: getString(Res.string.updated))
            is LocalResource.Error -> makeToast(result.message ?: getString(Res.string.error))
            else -> makeToast(getString(Res.string.error))
        }
    }

    fun renameLocalPlaylist(id: Long, title: String) = runPlaylistMenuAction {
        reportLocalPlaylistAction(
            localPlaylistRepository.updateTitleLocalPlaylist(
                id,
                title,
                getString(Res.string.updated),
                getString(Res.string.updated_to_youtube_playlist),
                getString(Res.string.error),
            ).lastOrNull(),
        )
    }

    fun deleteLocalPlaylist(id: Long) = runPlaylistMenuAction {
        reportLocalPlaylistAction(
            localPlaylistRepository.deleteLocalPlaylist(id, getString(Res.string.delete)).lastOrNull(),
        )
    }

    fun updateLocalPlaylistCover(id: Long, uri: String) = runPlaylistMenuAction {
        reportLocalPlaylistAction(
            localPlaylistRepository.updateThumbnailLocalPlaylist(id, uri, getString(Res.string.updated)).lastOrNull(),
        )
    }

    fun setLocalPlaylistSynced(id: Long, synced: Boolean) = runPlaylistMenuAction {
        val result = if (synced) {
            localPlaylistRepository.syncLocalPlaylistToYouTubePlaylist(
                id,
                getString(Res.string.synced),
                getString(Res.string.error),
            )
        } else {
            localPlaylistRepository.unsyncLocalPlaylist(id, getString(Res.string.unsynced))
        }
        reportLocalPlaylistAction(result.lastOrNull())
    }

    fun refreshSyncedLocalPlaylist(id: Long) = runPlaylistMenuAction {
        val done = localPlaylistRepository.updateListTrackSynced(id).lastOrNull() == true
        makeToast(getString(if (done) Res.string.updated else Res.string.error))
    }

    fun addLocalPlaylistToQueue(id: Long) = runPlaylistMenuAction {
        val tracks = localPlaylistRepository.getFullPlaylistTracks(id)
        if (tracks.isEmpty()) {
            makeToast(getString(Res.string.playlist_is_empty))
        } else {
            mediaPlayerHandler.loadMoreCatalog(tracks.toArrayListTrack(), true)
            makeToast(getString(Res.string.added_to_queue))
        }
    }

    fun renameYouTubePlaylist(id: String, title: String) = runPlaylistMenuAction {
        val normalizedId = id.removePrefix("VL")
        when (val result = playlistRepository.updateYourYouTubePlaylistTitle(normalizedId, title).lastOrNull()) {
            is Resource.Success -> {
                PlaylistViewModel.invalidateCachedPlaylist(normalizedId)
                val playlists = _youTubePlaylist.value.data?.map {
                    if (it.browseId.removePrefix("VL") == normalizedId) it.copy(title = title) else it
                }
                if (playlists != null) {
                    _youTubePlaylist.value = LocalResource.Success(playlists)
                    val accountCacheKey = "youtube_library_playlists_${dataStoreManager.cookie.first().hashCode()}"
                    dataStoreManager.putString(
                        accountCacheKey,
                        Json.encodeToString(ListSerializer(PlaylistsResult.serializer()), playlists),
                    )
                }
                playlistRepository.getPlaylist(normalizedId).firstOrNull()?.let { playlist ->
                    playlistRepository.insertAndReplacePlaylist(playlist.copy(title = title))
                }
                makeToast(getString(Res.string.updated))
            }
            is Resource.Error -> makeToast(result.message ?: getString(Res.string.error))
            else -> makeToast(getString(Res.string.error))
        }
    }

    fun deleteYouTubePlaylist(id: String) = runPlaylistMenuAction {
        when (val result = playlistRepository.deleteYourYouTubePlaylist(id.removePrefix("VL")).lastOrNull()) {
            is Resource.Success -> {
                removeYouTubePlaylistFromCache(id)
                PlaylistViewModel.invalidateCachedPlaylist(id)
                makeToast(getString(Res.string.playlist_deleted))
            }
            is Resource.Error -> makeToast(result.message ?: getString(Res.string.error))
            else -> makeToast(getString(Res.string.error))
        }
    }

    private suspend fun fullYouTubePlaylistForMenu(id: String): PlaylistBrowse? {
        return when (val result = playlistRepository.getFullPlaylistData(id, getString(Res.string.view_count)).lastOrNull()) {
            is Resource.Success -> result.data
            is Resource.Error -> {
                makeToast(result.message ?: getString(Res.string.error))
                null
            }
            else -> {
                makeToast(getString(Res.string.error))
                null
            }
        }
    }

    fun addYouTubePlaylistToQueue(id: String, onTracksReady: (List<Track>) -> Unit) = runPlaylistMenuAction {
        val playlist = fullYouTubePlaylistForMenu(id) ?: return@runPlaylistMenuAction
        val tracks = playlist.tracks.filterNot { LiveStreamType.isLiveStream(it) }
        if (tracks.isEmpty()) {
            makeToast(getString(Res.string.playlist_is_empty))
        } else {
            onTracksReady(tracks)
        }
    }

    fun saveYouTubePlaylistToLocal(id: String) = runPlaylistMenuAction {
        val playlist = fullYouTubePlaylistForMenu(id) ?: return@runPlaylistMenuAction
        val state = PlaylistState(
            id = playlist.id,
            title = playlist.title,
            isRadio = false,
            author = playlist.author,
            thumbnail = playlist.thumbnails.lastOrNull()?.url,
            description = playlist.description,
            year = playlist.year,
            trackCount = playlist.trackCount,
            radioEndpoint = playlist.radioEndpoint,
            shuffleEndpoint = playlist.shuffleEndpoint,
            isEditable = playlist.isEditable,
        )
        reportLocalPlaylistAction(
            localPlaylistRepository.syncYouTubePlaylistToLocalPlaylist(
                state,
                playlist.tracks.filterNot { LiveStreamType.isLiveStream(it) },
                getString(Res.string.synced),
                getString(Res.string.error),
            ).lastOrNull(),
        )
    }

    fun deleteSong(videoId: String) {
        viewModelScope.launch {
            songRepository.setInLibrary(videoId, Config.REMOVED_SONG_DATE_TIME)
            songRepository.resetTotalPlayTime(videoId)
        }
    }

    companion object {
        /** How far back the Wrapped tab offers recaps, counting the current month as the first. */
        private const val MONTHS_OF_RECAP = 12
    }
}
