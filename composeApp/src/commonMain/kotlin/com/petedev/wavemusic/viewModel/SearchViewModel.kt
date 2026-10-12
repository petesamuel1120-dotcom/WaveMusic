package com.petedev.wavemusic.viewModel

import androidx.lifecycle.viewModelScope
import com.petedev.wavemusic.common.SELECTED_LANGUAGE
import com.petedev.wavemusic.domain.data.entities.SearchHistory
import com.petedev.wavemusic.domain.data.model.searchResult.albums.AlbumsResult
import com.petedev.wavemusic.domain.data.model.searchResult.artists.ArtistsResult
import com.petedev.wavemusic.domain.data.model.searchResult.playlists.PlaylistsResult
import com.petedev.wavemusic.domain.data.model.searchResult.songs.SongsResult
import com.petedev.wavemusic.domain.data.model.searchResult.videos.VideosResult
import com.petedev.wavemusic.domain.data.type.SearchResultType
import com.petedev.wavemusic.domain.manager.DataStoreManager
import com.petedev.wavemusic.domain.repository.SearchRepository
import com.petedev.wavemusic.domain.utils.LiveStreamType
import com.petedev.wavemusic.domain.utils.MusicVideoType
import com.petedev.wavemusic.domain.utils.rankSearchResults
import com.petedev.wavemusic.domain.utils.Resource
import com.petedev.wavemusic.domain.utils.toQueryList
import com.petedev.wavemusic.logger.LogLevel
import com.petedev.wavemusic.logger.Logger
import com.petedev.wavemusic.viewModel.base.BaseViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.StringResource
import com.petedev.wavemusic.resources.Res
import com.petedev.wavemusic.resources.albums
import com.petedev.wavemusic.resources.all
import com.petedev.wavemusic.resources.artists
import com.petedev.wavemusic.resources.featured_playlists
import com.petedev.wavemusic.resources.playlists
import com.petedev.wavemusic.resources.podcasts
import com.petedev.wavemusic.resources.episodes
import com.petedev.wavemusic.resources.songs
import com.petedev.wavemusic.resources.videos

// State cho tìm kiếm
data class SearchScreenState(
    val searchType: SearchType = SearchType.ALL,
    val searchAllResult: List<SearchResultType> = emptyList(),
    val searchSongsResult: List<SongsResult> = emptyList(),
    val searchVideosResult: List<VideosResult> = emptyList(),
    val searchAlbumsResult: List<AlbumsResult> = emptyList(),
    val searchArtistsResult: List<ArtistsResult> = emptyList(),
    val searchPlaylistsResult: List<PlaylistsResult> = emptyList(),
    val searchFeaturedPlaylistsResult: List<PlaylistsResult> = emptyList(),
    val searchPodcastsResult: List<PlaylistsResult> = emptyList(),
    val searchEpisodesResult: List<SongsResult> = emptyList(),
    val suggestQueries: List<String> = emptyList(),
    val suggestYTItems: List<SearchResultType> = emptyList(),
)

// Loại tìm kiếm
enum class SearchType {
    ALL,
    SONGS,
    VIDEOS,
    ALBUMS,
    ARTISTS,
    PLAYLISTS,
    FEATURED_PLAYLISTS,
    PODCASTS,
    EPISODES,
}

fun SearchType.toStringRes(): StringResource =
    when (this) {
        SearchType.ALL -> Res.string.all
        SearchType.SONGS -> Res.string.songs
        SearchType.VIDEOS -> Res.string.videos
        SearchType.ALBUMS -> Res.string.albums
        SearchType.ARTISTS -> Res.string.artists
        SearchType.PLAYLISTS -> Res.string.playlists
        SearchType.FEATURED_PLAYLISTS -> Res.string.featured_playlists
        SearchType.PODCASTS -> Res.string.podcasts
        SearchType.EPISODES -> Res.string.episodes
    }

// UI state cho tìm kiếm
sealed class SearchScreenUIState {
    object Empty : SearchScreenUIState()

    object Loading : SearchScreenUIState()

    object Success : SearchScreenUIState()

    object Error : SearchScreenUIState()
}

class SearchViewModel(
    private val dataStoreManager: DataStoreManager,
    private val searchRepository: SearchRepository,
) : BaseViewModel() {
    private val _searchScreenUIState = MutableStateFlow<SearchScreenUIState>(SearchScreenUIState.Empty)
    val searchScreenUIState: StateFlow<SearchScreenUIState> get() = _searchScreenUIState.asStateFlow()

    private val _searchScreenState = MutableStateFlow(SearchScreenState())
    val searchScreenState: StateFlow<SearchScreenState> get() = _searchScreenState.asStateFlow()

    private val _searchHistory: MutableStateFlow<List<String>> = MutableStateFlow(emptyList())
    val searchHistory: StateFlow<List<String>> get() = _searchHistory.asStateFlow()

    var regionCode: String? = null
    var language: String? = null
    private var suggestRequestId = 0
    private var suggestJob: Job? = null
    private var podcastSuggestJob: Job? = null
    private var baseSuggestItems: List<SearchResultType> = emptyList()
    private var podcastSuggestItems: List<PlaylistsResult> = emptyList()

    init {
        regionCode = runBlocking { dataStoreManager.location.first() }
        language = runBlocking { dataStoreManager.getString(SELECTED_LANGUAGE).first() }
        getSearchHistory()
    }

    private fun getSearchHistory() {
        viewModelScope.launch {
            searchRepository.getSearchHistory().collect { values ->
                if (values.isNotEmpty()) {
                    values.toQueryList().reversed().let { list ->
                        _searchHistory.value = list
                        log("Search history updated: $list")
                    }
                } else {
                    _searchHistory.value = emptyList()
                    log("Search history is empty")
                }
            }
        }
    }

    fun insertSearchHistory(query: String) {
        viewModelScope.launch {
            searchRepository.insertSearchHistory(SearchHistory(query = query)).collectLatest {
                Logger.d(tag, "Inserted search history: $query, $it")
                getSearchHistory()
            }
        }
    }

    fun deleteSearchHistory() {
        viewModelScope.launch {
            searchRepository.deleteSearchHistory()
            delay(1000)
            getSearchHistory()
        }
    }

    fun deleteSearchHistoryItem(query: String) {
        viewModelScope.launch {
            searchRepository.deleteSearchHistoryItem(query)
            _searchHistory.update { history -> history.filterNot { it == query } }
        }
    }

    fun searchSongs(query: String) {
        _searchScreenUIState.value = SearchScreenUIState.Loading
        viewModelScope.launch {
            searchRepository.getSearchDataSong(query).collect { values ->
                when (values) {
                    is Resource.Success -> {
                        values.data?.let { songsList ->
                            _searchScreenState.update { state ->
                                state.copy(
                                    searchSongsResult = songsList.filterNot { result ->
                                        MusicVideoType.isVideoSong(result.videoType) ||
                                            LiveStreamType.isLiveStream(result.isLiveVideo, result.title)
                                    },
                                )
                            }
                        }
                        _searchScreenUIState.value = SearchScreenUIState.Success
                    }

                    is Resource.Error -> {
                        _searchScreenUIState.value = SearchScreenUIState.Error
                    }
                }
            }
        }
    }

    fun searchVideos(query: String) {
        _searchScreenUIState.value = SearchScreenUIState.Loading
        viewModelScope.launch {
            searchRepository.getSearchDataVideo(query).collect { values ->
                when (values) {
                    is Resource.Success -> {
                        _searchScreenState.update { state ->
                            state.copy(
                                searchType = SearchType.VIDEOS,
                                searchVideosResult = values.data.orEmpty().filterNot { result ->
                                    LiveStreamType.isLiveStream(result.isLiveVideo, result.title)
                                },
                            )
                        }
                        _searchScreenUIState.value = SearchScreenUIState.Success
                    }

                    is Resource.Error -> {
                        _searchScreenUIState.value = SearchScreenUIState.Error
                    }
                }
            }
        }
    }

    fun searchAll(query: String) {
        _searchScreenUIState.value = SearchScreenUIState.Loading
        viewModelScope.launch {
            var song = ArrayList<SongsResult>()
            var video = ArrayList<VideosResult>()
            var album = ArrayList<AlbumsResult>()
            var artist = ArrayList<ArtistsResult>()
            var playlist = ArrayList<PlaylistsResult>()
            var featuredPlaylist = ArrayList<PlaylistsResult>()
            var podcast = ArrayList<PlaylistsResult>()
            var episode = ArrayList<SongsResult>()
            val temp: ArrayList<SearchResultType> = ArrayList()

            val job1 =
                launch {
                    searchRepository.getSearchDataSong(query).collect { values ->
                        when (values) {
                            is Resource.Success -> {
                                values.data?.let { songs ->
                                    song = ArrayList(
                                        songs.filterNot { result ->
                                            MusicVideoType.isVideoSong(result.videoType) ||
                                                LiveStreamType.isLiveStream(result.isLiveVideo, result.title)
                                        },
                                    )
                                }
                            }
                            is Resource.Error -> {}
                        }
                    }
                }
            val job2 =
                launch {
                    searchRepository.getSearchDataArtist(query).collect { values ->
                        when (values) {
                            is Resource.Success -> values.data?.let { artist = it }
                            is Resource.Error -> {}
                        }
                    }
                }
            val job3 =
                launch {
                    searchRepository
                        .getSearchDataAlbum(query)
                        .collect { values ->
                            when (values) {
                                is Resource.Success -> values.data?.let { album = it }
                                is Resource.Error -> {}
                            }
                        }
                }
            val job4 =
                launch {
                    searchRepository.getSearchDataPlaylist(query).collect { values ->
                        when (values) {
                            is Resource.Success -> values.data?.let { playlist = it }
                            is Resource.Error -> {}
                        }
                    }
                }
            val job6 =
                launch {
                    searchRepository.getSearchDataFeaturedPlaylist(query).collect { values ->
                        when (values) {
                            is Resource.Success -> values.data?.let { featuredPlaylist = it }
                            is Resource.Error -> {}
                        }
                    }
                }
            val job7 =
                launch {
                    searchRepository.getSearchDataPodcast(query).collect { values ->
                        when (values) {
                            is Resource.Success -> values.data?.let { podcast = it }
                            is Resource.Error -> {}
                        }
                    }
                }
            val job8 =
                launch {
                    searchRepository.getSearchDataVideo(query).collect { values ->
                        when (values) {
                            is Resource.Success -> values.data?.let { results ->
                                video = ArrayList(
                                    results.filterNot { result ->
                                        LiveStreamType.isLiveStream(result.isLiveVideo, result.title)
                                    },
                                )
                            }
                            is Resource.Error -> {}
                        }
                    }
                }
            val job9 = launch {
                searchRepository.getSearchDataEpisode(query).collect { values ->
                    if (values is Resource.Success) values.data?.let { episode = it }
                }
            }
            job1.join()
            job2.join()
            job3.join()
            job4.join()
            job6.join()
            job7.join()
            job8.join()
            job9.join()

            try {
                val episodeIds = episode.mapTo(mutableSetOf()) { it.videoId }
                temp.addAll(song.filterNot { it.videoId in episodeIds })
                temp.addAll(video.filterNot { it.videoId in episodeIds })
                temp.addAll(episode)
                temp.addAll(artist)
                temp.addAll(podcast)
                temp.addAll(album)
                temp.addAll(playlist)
                temp.addAll(featuredPlaylist)

                _searchScreenState.update { state ->
                    state.copy(
                        searchType = SearchType.ALL,
                        searchAllResult = rankSearchResults(query, temp),
                        searchSongsResult = song,
                        searchVideosResult = video,
                        searchArtistsResult = artist,
                        searchAlbumsResult = album,
                        searchPlaylistsResult = playlist,
                        searchFeaturedPlaylistsResult = featuredPlaylist,
                        searchPodcastsResult = podcast,
                        searchEpisodesResult = episode,
                    )
                }
                _searchScreenUIState.value = SearchScreenUIState.Success
            } catch (e: Exception) {
                e.printStackTrace()
                _searchScreenUIState.value = SearchScreenUIState.Error
            }
        }
    }

    fun suggestQuery(query: String) {
        val requestId = ++suggestRequestId
        suggestJob?.cancel()
        podcastSuggestJob?.cancel()
        baseSuggestItems = emptyList()
        podcastSuggestItems = emptyList()
        _searchScreenState.update { it.copy(suggestYTItems = emptyList(), suggestQueries = emptyList()) }

        suggestJob = viewModelScope.launch {
            searchRepository.getSuggestQuery(query).collectLatest { values ->
                when (values) {
                    is Resource.Success -> {
                        values.data?.let { suggestData ->
                            if (requestId == suggestRequestId) {
                                baseSuggestItems = suggestData.recommendedItems.filterNot { item ->
                                        when (item) {
                                            is SongsResult ->
                                                MusicVideoType.isVideoSong(item.videoType) ||
                                                    LiveStreamType.isLiveStream(item.isLiveVideo, item.title)
                                            is VideosResult ->
                                                LiveStreamType.isLiveStream(item.isLiveVideo, item.title)
                                            else -> false
                                        }
                                    }
                                _searchScreenState.update { state -> state.copy(suggestQueries = suggestData.queries) }
                                publishSuggestItems(requestId)
                            }
                        }
                    }

                    is Resource.Error -> {
                        // Không cần xử lý lỗi đặc biệt cho gợi ý
                        log("Error fetching suggest queries: ${values.message}", LogLevel.ERROR)
                    }
                }
            }
        }
        if (query.trim().length >= 3) {
            podcastSuggestJob = viewModelScope.launch {
                // Wait for typing to pause so we do not issue a multi-page podcast search per key.
                delay(350)
                searchRepository.getSearchDataPodcast(query.trim()).collectLatest { values ->
                    if (requestId != suggestRequestId) return@collectLatest
                    if (values is Resource.Success) {
                        podcastSuggestItems = values.data.orEmpty().take(2)
                        publishSuggestItems(requestId)
                    }
                }
            }
        }
    }

    private fun publishSuggestItems(requestId: Int) {
        if (requestId != suggestRequestId) return
        val podcastResults = podcastSuggestItems.map { it as SearchResultType }
        val existingPodcasts = baseSuggestItems.filterIsInstance<PlaylistsResult>().filter { it.resultType == "Podcast" }
        val nonPodcastItems = baseSuggestItems.filterNot { it is PlaylistsResult && it.resultType == "Podcast" }
        val merged = (podcastResults + existingPodcasts + nonPodcastItems).distinctBy { item ->
            when (item) {
                is PlaylistsResult -> "podcast:${item.browseId}"
                else -> "${item.objectType()}:${item.hashCode()}"
            }
        }
        _searchScreenState.update { it.copy(suggestYTItems = merged) }
    }

    fun searchAlbums(query: String) {
        _searchScreenUIState.value = SearchScreenUIState.Loading
        viewModelScope.launch {
            searchRepository.getSearchDataAlbum(query).collect { values ->
                when (values) {
                    is Resource.Success -> {
                        values.data?.let { albumsList ->
                            _searchScreenState.update { state ->
                                state.copy(
                                    searchType = SearchType.ALBUMS,
                                    searchAlbumsResult = albumsList,
                                )
                            }
                        }
                        _searchScreenUIState.value = SearchScreenUIState.Success
                    }

                    is Resource.Error -> {
                        _searchScreenUIState.value = SearchScreenUIState.Error
                    }
                }
            }
        }
    }

    fun searchFeaturedPlaylist(query: String) {
        _searchScreenUIState.value = SearchScreenUIState.Loading
        viewModelScope.launch {
            searchRepository.getSearchDataFeaturedPlaylist(query).collect { values ->
                when (values) {
                    is Resource.Success -> {
                        values.data?.let { featuredPlaylistList ->
                            _searchScreenState.update { state ->
                                state.copy(
                                    searchType = SearchType.FEATURED_PLAYLISTS,
                                    searchFeaturedPlaylistsResult = featuredPlaylistList,
                                )
                            }
                        }
                        _searchScreenUIState.value = SearchScreenUIState.Success
                    }

                    is Resource.Error -> {
                        _searchScreenUIState.value = SearchScreenUIState.Error
                    }
                }
            }
        }
    }

    fun searchPodcast(query: String) {
        _searchScreenUIState.value = SearchScreenUIState.Loading
        viewModelScope.launch {
            searchRepository.getSearchDataPodcast(query).collect { values ->
                when (values) {
                    is Resource.Success -> {
                        values.data?.let { podcastList ->
                            _searchScreenState.update { state ->
                                state.copy(
                                    searchType = SearchType.PODCASTS,
                                    searchPodcastsResult = podcastList,
                                )
                            }
                        }
                        _searchScreenUIState.value = SearchScreenUIState.Success
                    }

                    is Resource.Error -> {
                        _searchScreenUIState.value = SearchScreenUIState.Error
                    }
                }
            }
        }
    }

    fun searchEpisodes(query: String) {
        _searchScreenUIState.value = SearchScreenUIState.Loading
        viewModelScope.launch {
            searchRepository.getSearchDataEpisode(query).collect { values ->
                when (values) {
                    is Resource.Success -> {
                        values.data?.let { podcastList ->
                            _searchScreenState.update { state ->
                                state.copy(
                                    searchType = SearchType.EPISODES,
                                    searchEpisodesResult = podcastList,
                                )
                            }
                        }
                        _searchScreenUIState.value = SearchScreenUIState.Success
                    }

                    is Resource.Error -> {
                        _searchScreenUIState.value = SearchScreenUIState.Error
                    }
                }
            }
        }
    }

    fun searchArtists(query: String) {
        _searchScreenUIState.value = SearchScreenUIState.Loading
        viewModelScope.launch {
            searchRepository.getSearchDataArtist(query).collect { values ->
                when (values) {
                    is Resource.Success -> {
                        values.data?.let { artistsList ->
                            _searchScreenState.update { state ->
                                state.copy(
                                    searchType = SearchType.ARTISTS,
                                    searchArtistsResult = artistsList,
                                )
                            }
                        }
                        _searchScreenUIState.value = SearchScreenUIState.Success
                    }

                    is Resource.Error -> {
                        _searchScreenUIState.value = SearchScreenUIState.Error
                    }
                }
            }
        }
    }

    fun searchPlaylists(query: String) {
        _searchScreenUIState.value = SearchScreenUIState.Loading
        viewModelScope.launch {
            searchRepository.getSearchDataPlaylist(query).collect { values ->
                when (values) {
                    is Resource.Success -> {
                        values.data?.let { playlistsList ->
                            _searchScreenState.update { state ->
                                state.copy(
                                    searchType = SearchType.PLAYLISTS,
                                    searchPlaylistsResult = playlistsList,
                                )
                            }
                        }
                        _searchScreenUIState.value = SearchScreenUIState.Success
                    }

                    is Resource.Error -> {
                        _searchScreenUIState.value = SearchScreenUIState.Error
                    }
                }
            }
        }
    }

    fun setSearchType(searchType: SearchType) {
        _searchScreenState.update { state ->
            state.copy(searchType = searchType)
        }
    }
}
