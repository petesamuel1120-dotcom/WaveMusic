package com.petedev.wavemusic.data.repository

import com.petedev.wavemusic.data.db.datasource.LocalDataSource
import com.petedev.wavemusic.data.mapping.toDomainSearchSuggestions
import com.petedev.wavemusic.data.parser.parsePodcast
import com.petedev.wavemusic.data.parser.search.parseSearchAlbum
import com.petedev.wavemusic.data.parser.search.parseSearchArtist
import com.petedev.wavemusic.data.parser.search.parseSearchPlaylist
import com.petedev.wavemusic.data.parser.search.parseSearchSong
import com.petedev.wavemusic.data.parser.search.parseSearchEpisode
import com.petedev.wavemusic.data.parser.search.parseSearchVideo
import com.petedev.wavemusic.domain.data.entities.SearchHistory
import com.petedev.wavemusic.domain.data.model.searchResult.SearchSuggestions
import com.petedev.wavemusic.domain.data.model.searchResult.albums.AlbumsResult
import com.petedev.wavemusic.domain.data.model.searchResult.artists.ArtistsResult
import com.petedev.wavemusic.domain.data.model.searchResult.playlists.PlaylistsResult
import com.petedev.wavemusic.domain.data.model.searchResult.songs.SongsResult
import com.petedev.wavemusic.domain.data.model.searchResult.videos.VideosResult
import com.petedev.wavemusic.domain.repository.SearchRepository
import com.petedev.wavemusic.domain.utils.Resource
import com.petedev.wavemusic.kotlinytmusicscraper.YouTube
import com.petedev.wavemusic.logger.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

internal class SearchRepositoryImpl(
    private val localDataSource: LocalDataSource,
    private val youTube: YouTube,
) : SearchRepository {
    override fun getSearchHistory(): Flow<List<SearchHistory>> =
        flow {
            emit(localDataSource.getSearchHistory())
        }.flowOn(Dispatchers.IO)

    override fun insertSearchHistory(searchHistory: SearchHistory): Flow<Long> =
        flow {
            emit(localDataSource.insertSearchHistory(searchHistory))
        }.flowOn(Dispatchers.IO)

    override suspend fun deleteSearchHistory() =
        withContext(Dispatchers.IO) {
            localDataSource.deleteSearchHistory()
        }

    override suspend fun deleteSearchHistoryItem(query: String) =
        withContext(Dispatchers.IO) {
            localDataSource.deleteSearchHistoryItem(query)
        }

    override fun getSearchDataSong(query: String): Flow<Resource<ArrayList<SongsResult>>> =
        flow {
            runCatching {
                youTube
                    .search(query, YouTube.SearchFilter.FILTER_SONG)
                    .onSuccess { result ->
                        val listSongs: ArrayList<SongsResult> = arrayListOf()
                        var countinueParam = result.continuation
                        parseSearchSong(result).let { list ->
                            listSongs.addAll(list)
                        }
                        var count = 0
                        while (count < 2 && countinueParam != null) {
                            youTube
                                .searchContinuation(countinueParam)
                                .onSuccess { values ->
                                    parseSearchSong(values).let { list ->
                                        listSongs.addAll(list)
                                    }
                                    count++
                                    countinueParam = values.continuation
                                }.onFailure {
                                    Logger.e("Continue", "Error: ${it.message}")
                                    countinueParam = null
                                    count++
                                }
                        }

                        emit(Resource.Success<ArrayList<SongsResult>>(listSongs))
                    }.onFailure { e ->
                        Logger.d("Search", "Error: ${e.message}")
                        emit(Resource.Error<ArrayList<SongsResult>>(e.message.toString()))
                    }
            }
        }.flowOn(Dispatchers.IO)

    override fun getSearchDataVideo(query: String): Flow<Resource<ArrayList<VideosResult>>> =
        flow {
            runCatching {
                youTube
                    .search(query, YouTube.SearchFilter.FILTER_VIDEO)
                    .onSuccess { result ->
                        val listSongs: ArrayList<VideosResult> = arrayListOf()
                        var countinueParam = result.continuation
                        parseSearchVideo(result).let { list ->
                            listSongs.addAll(list)
                        }
                        var count = 0
                        while (count < 2 && countinueParam != null) {
                            youTube
                                .searchContinuation(countinueParam)
                                .onSuccess { values ->
                                    parseSearchVideo(values).let { list ->
                                        listSongs.addAll(list)
                                    }
                                    count++
                                    countinueParam = values.continuation
                                }.onFailure {
                                    Logger.e("Continue", "Error: ${it.message}")
                                    countinueParam = null
                                    count++
                                }
                        }

                        emit(Resource.Success<ArrayList<VideosResult>>(listSongs))
                    }.onFailure { e ->
                        Logger.d("Search", "Error: ${e.message}")
                        emit(Resource.Error<ArrayList<VideosResult>>(e.message.toString()))
                    }
            }
        }.flowOn(Dispatchers.IO)

    override fun getSearchDataPodcast(query: String): Flow<Resource<ArrayList<PlaylistsResult>>> =
        flow {
            runCatching {
                youTube
                    .search(query, YouTube.SearchFilter.FILTER_PODCAST)
                    .onSuccess { result ->
                        println(query)
                        val listPlaylist: ArrayList<PlaylistsResult> = arrayListOf()
                        var countinueParam = result.continuation
                        Logger.w("Podcast", "result: $result")
                        parsePodcast(result.listPodcast).let { list ->
                            listPlaylist.addAll(list)
                        }
                        var count = 0
                        while (count < 2 && countinueParam != null) {
                            youTube
                                .searchContinuation(countinueParam)
                                .onSuccess { values ->
                                    parsePodcast(values.listPodcast).let { list ->
                                        listPlaylist.addAll(list)
                                    }
                                    count++
                                    countinueParam = values.continuation
                                }.onFailure {
                                    Logger.e("Continue", "Error: ${it.message}")
                                    countinueParam = null
                                    count++
                                }
                        }
                        emit(Resource.Success<ArrayList<PlaylistsResult>>(listPlaylist))
                    }.onFailure { e ->
                        Logger.d("Search", "Error: ${e.message}")
                        emit(Resource.Error<ArrayList<PlaylistsResult>>(e.message.toString()))
                    }
            }
        }.flowOn(Dispatchers.IO)

    override fun getSearchDataEpisode(query: String): Flow<Resource<ArrayList<SongsResult>>> =
        flow {
            val filter = YouTube.SearchFilter.FILTER_EPISODE
            val initial = youTube.search(query, filter).getOrElse {
                emit(Resource.Error(it.message.orEmpty()))
                return@flow
            }
            val episodes = parseSearchEpisode(initial)
            var continuation = initial.continuation
            repeat(2) {
                val next = continuation ?: return@repeat
                youTube.searchContinuation(next, filter)
                    .onSuccess { page ->
                        episodes.addAll(parseSearchEpisode(page))
                        continuation = page.continuation
                    }.onFailure { continuation = null }
            }
            emit(Resource.Success(ArrayList(episodes.distinctBy { it.videoId })))
        }.flowOn(Dispatchers.IO)

    override fun getSearchDataFeaturedPlaylist(query: String): Flow<Resource<ArrayList<PlaylistsResult>>> =
        flow {
            runCatching {
                youTube
                    .search(query, YouTube.SearchFilter.FILTER_FEATURED_PLAYLIST)
                    .onSuccess { result ->
                        val listPlaylist: ArrayList<PlaylistsResult> = arrayListOf()
                        var countinueParam = result.continuation
                        parseSearchPlaylist(result).let { list ->
                            listPlaylist.addAll(list)
                        }
                        var count = 0
                        while (count < 2 && countinueParam != null) {
                            youTube
                                .searchContinuation(countinueParam)
                                .onSuccess { values ->
                                    parseSearchPlaylist(values).let { list ->
                                        listPlaylist.addAll(list)
                                    }
                                    count++
                                    countinueParam = values.continuation
                                }.onFailure {
                                    Logger.e("Continue", "Error: ${it.message}")
                                    countinueParam = null
                                    count++
                                }
                        }
                        emit(Resource.Success<ArrayList<PlaylistsResult>>(listPlaylist))
                    }.onFailure { e ->
                        Logger.d("Search", "Error: ${e.message}")
                        emit(Resource.Error<ArrayList<PlaylistsResult>>(e.message.toString()))
                    }
            }
        }.flowOn(Dispatchers.IO)

    override fun getSearchDataArtist(query: String): Flow<Resource<ArrayList<ArtistsResult>>> =
        flow {
            runCatching {
                youTube
                    .search(query, YouTube.SearchFilter.FILTER_ARTIST)
                    .onSuccess { result ->
                        val listArtist: ArrayList<ArtistsResult> = arrayListOf()
                        var countinueParam = result.continuation
                        parseSearchArtist(result).let { list ->
                            listArtist.addAll(list)
                        }
                        var count = 0
                        while (count < 2 && countinueParam != null) {
                            youTube
                                .searchContinuation(countinueParam)
                                .onSuccess { values ->
                                    parseSearchArtist(values).let { list ->
                                        listArtist.addAll(list)
                                    }
                                    count++
                                    countinueParam = values.continuation
                                }.onFailure {
                                    Logger.e("Continue", "Error: ${it.message}")
                                    countinueParam = null
                                    count++
                                }
                        }
                        emit(Resource.Success<ArrayList<ArtistsResult>>(listArtist))
                    }.onFailure { e ->
                        Logger.d("Search", "Error: ${e.message}")
                        emit(Resource.Error<ArrayList<ArtistsResult>>(e.message.toString()))
                    }
            }
        }.flowOn(Dispatchers.IO)

    override fun getSearchDataAlbum(query: String): Flow<Resource<ArrayList<AlbumsResult>>> =
        flow {
            runCatching {
                youTube
                    .search(query, YouTube.SearchFilter.FILTER_ALBUM)
                    .onSuccess { result ->
                        val listAlbum: ArrayList<AlbumsResult> = arrayListOf()
                        var countinueParam = result.continuation
                        parseSearchAlbum(result).let { list ->
                            listAlbum.addAll(list)
                        }
                        var count = 0
                        while (count < 2 && countinueParam != null) {
                            youTube
                                .searchContinuation(countinueParam)
                                .onSuccess { values ->
                                    parseSearchAlbum(values).let { list ->
                                        listAlbum.addAll(list)
                                    }
                                    count++
                                    countinueParam = values.continuation
                                }.onFailure {
                                    Logger.e("Continue", "Error: ${it.message}")
                                    countinueParam = null
                                    count++
                                }
                        }
                        emit(Resource.Success<ArrayList<AlbumsResult>>(listAlbum))
                    }.onFailure { e ->
                        Logger.d("Search", "Error: ${e.message}")
                        emit(Resource.Error<ArrayList<AlbumsResult>>(e.message.toString()))
                    }
            }
        }.flowOn(Dispatchers.IO)

    override fun getSearchDataPlaylist(query: String): Flow<Resource<ArrayList<PlaylistsResult>>> =
        flow {
            runCatching {
                youTube
                    .search(query, YouTube.SearchFilter.FILTER_COMMUNITY_PLAYLIST)
                    .onSuccess { result ->
                        val listPlaylist: ArrayList<PlaylistsResult> = arrayListOf()
                        var countinueParam = result.continuation
                        parseSearchPlaylist(result).let { list ->
                            listPlaylist.addAll(list)
                        }
                        var count = 0
                        while (count < 2 && countinueParam != null) {
                            youTube
                                .searchContinuation(countinueParam)
                                .onSuccess { values ->
                                    parseSearchPlaylist(values).let { list ->
                                        listPlaylist.addAll(list)
                                    }
                                    count++
                                    countinueParam = values.continuation
                                }.onFailure {
                                    Logger.e("Continue", "Error: ${it.message}")
                                    countinueParam = null
                                    count++
                                }
                        }
                        emit(Resource.Success<ArrayList<PlaylistsResult>>(listPlaylist))
                    }.onFailure { e ->
                        Logger.d("Search", "Error: ${e.message}")
                        emit(Resource.Error<ArrayList<PlaylistsResult>>(e.message.toString()))
                    }
            }
        }.flowOn(Dispatchers.IO)

    override fun getSuggestQuery(query: String): Flow<Resource<SearchSuggestions>> =
        flow {
            runCatching {
                youTube
                    .getYTMusicSearchSuggestions(query)
                    .onSuccess {
                        emit(Resource.Success(it.toDomainSearchSuggestions()))
                    }.onFailure { e ->
                        Logger.d("Suggest", "Error: ${e.message}")
                        emit(Resource.Error<SearchSuggestions>(e.message.toString()))
                    }
            }
        }.flowOn(Dispatchers.IO)
}
