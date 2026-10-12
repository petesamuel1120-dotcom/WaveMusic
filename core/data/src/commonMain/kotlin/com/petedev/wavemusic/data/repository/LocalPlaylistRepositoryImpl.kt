@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.petedev.wavemusic.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.petedev.wavemusic.data.db.Converters
import com.petedev.wavemusic.data.db.datasource.LocalDataSource
import com.petedev.wavemusic.data.io.readLocalImageBytes
import com.petedev.wavemusic.data.extension.getFullDataFromDB
import com.petedev.wavemusic.data.mapping.toListTrack
import com.petedev.wavemusic.data.mapping.toTrack
import com.petedev.wavemusic.data.paging.LocalPlaylistPagingSource
import com.petedev.wavemusic.data.paging.LocalPlaylistTimeBasedPagingSource
import com.petedev.wavemusic.data.parser.parseSetVideoId
import com.petedev.wavemusic.domain.data.entities.DownloadState
import com.petedev.wavemusic.domain.data.entities.LocalPlaylistEntity
import com.petedev.wavemusic.domain.data.entities.LocalPlaylistEntity.YouTubeSyncState.Synced
import com.petedev.wavemusic.domain.data.entities.LocalPlaylistEntity.YouTubeSyncState.Syncing
import com.petedev.wavemusic.domain.data.entities.PairSongLocalPlaylist
import com.petedev.wavemusic.domain.data.entities.SetVideoIdEntity
import com.petedev.wavemusic.domain.data.entities.SongEntity
import com.petedev.wavemusic.domain.data.model.browse.album.Track
import com.petedev.wavemusic.domain.data.model.browse.playlist.PlaylistState
import com.petedev.wavemusic.domain.extension.now
import com.petedev.wavemusic.domain.repository.LocalPlaylistRepository
import com.petedev.wavemusic.domain.utils.FilterState
import com.petedev.wavemusic.domain.utils.LocalResource
import com.petedev.wavemusic.domain.utils.PLAYLIST_ITEM_ALREADY_PRESENT
import com.petedev.wavemusic.domain.utils.PlaylistMembershipChanges
import com.petedev.wavemusic.domain.utils.toListVideoId
import com.petedev.wavemusic.domain.utils.toSongEntity
import com.petedev.wavemusic.domain.utils.wrapDataResource
import com.petedev.wavemusic.domain.utils.wrapMessageResource
import com.petedev.wavemusic.kotlinytmusicscraper.YouTube
import com.petedev.wavemusic.kotlinytmusicscraper.extension.verifyYouTubePlaylistId
import com.petedev.wavemusic.kotlinytmusicscraper.models.MusicShelfRenderer
import com.petedev.wavemusic.kotlinytmusicscraper.models.SongItem
import com.petedev.wavemusic.kotlinytmusicscraper.models.response.SearchResponse
import com.petedev.wavemusic.kotlinytmusicscraper.pages.NextPage
import com.petedev.wavemusic.kotlinytmusicscraper.pages.SearchPage
import com.petedev.wavemusic.kotlinytmusicscraper.parser.getPlaylistContinuation
import com.petedev.wavemusic.logger.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDateTime

private const val TAG = "LocalPlaylistRepositoryImpl"

internal class LocalPlaylistRepositoryImpl(
    private val localDataSource: LocalDataSource,
    private val youTube: YouTube,
) : LocalPlaylistRepository {
    override fun getLocalPlaylist(id: Long) =
        wrapDataResource {
            localDataSource.getLocalPlaylist(id)
        }

    override fun getAllLocalPlaylists(): Flow<List<LocalPlaylistEntity>> =
        flow {
            val list =
                getFullDataFromDB { limit, offset ->
                    localDataSource.getAllLocalPlaylists(limit, offset)
                }
            emit(list)
        }.flowOn(Dispatchers.IO)

    override suspend fun updateLocalPlaylistTracks(
        tracks: List<String>,
        id: Long,
    ) = withContext(Dispatchers.IO) { localDataSource.updateLocalPlaylistTracks(tracks, id) }

    override suspend fun updateLocalPlaylistDownloadState(
        downloadState: Int,
        id: Long,
    ) = withContext(Dispatchers.IO) {
        localDataSource.updateLocalPlaylistDownloadState(
            downloadState,
            id,
        )
    }

    override suspend fun updateLocalPlaylistYouTubePlaylistSyncState(
        id: Long,
        syncState: Int,
    ) = withContext(Dispatchers.IO) {
        localDataSource.updateLocalPlaylistYouTubePlaylistSyncState(id, syncState)
    }

    override fun downloadStateFlow(id: Long): Flow<Int> = localDataSource.getDownloadStateFlowOfLocalPlaylist(id)

    override fun getAllDownloadingLocalPlaylists(): Flow<List<LocalPlaylistEntity>> =
        flow {
            emit(
                getFullDataFromDB { limit, offset ->
                    localDataSource.getAllDownloadingLocalPlaylists(limit, offset)
                },
            )
        }.flowOn(Dispatchers.IO)

    override fun listTrackFlow(id: Long): Flow<List<String>> =
        localDataSource
            .getListTracksFlowOfLocalPlaylist(id)
            .map { Converters().fromString(it.firstOrNull()) ?: emptyList() }

    override fun searchTracks(
        id: Long,
        query: String,
        limit: Int,
    ): Flow<List<Pair<SongEntity, PairSongLocalPlaylist>>> =
        flow {
            val pairs = localDataSource.searchPlaylistPairSong(id, query, limit)
            if (pairs.isEmpty()) {
                emit(emptyList())
                return@flow
            }
            // Two steps, the same shape LocalPlaylistPagingSource uses: the join narrows the rows,
            // then the songs are fetched by id. Re-sorted by the pair order afterwards because the
            // id lookup returns them in whatever order the database finds them.
            val byId = localDataSource.getSongByListVideoIdFull(pairs.map { it.songId }).associateBy { it.videoId }
            emit(pairs.mapNotNull { pair -> byId[pair.songId]?.let { it to pair } })
        }.flowOn(Dispatchers.IO)

    override fun getTracksPaging(
        id: Long,
        filter: FilterState,
    ): Flow<PagingData<Pair<SongEntity, PairSongLocalPlaylist>>> {
        if (filter == FilterState.CustomOrder || filter == FilterState.Title) {
            return Pager(
                config = PagingConfig(pageSize = 100, prefetchDistance = 5),
                pagingSourceFactory = {
                    LocalPlaylistPagingSource(
                        playlistId = id,
                        filter = filter,
                        localDataSource = localDataSource,
                    )
                },
            ).flow
        } else {
            return Pager(
                config = PagingConfig(pageSize = 100, prefetchDistance = 5),
                pagingSourceFactory = {
                    LocalPlaylistTimeBasedPagingSource(
                        playlistId = id,
                        filter = filter,
                        localDataSource = localDataSource,
                    )
                },
            ).flow
        }
    }

    override suspend fun getFullPlaylistTracks(id: Long): List<SongEntity> {
        val playlist = localDataSource.getLocalPlaylist(id) ?: return emptyList()
        Logger.d(TAG, "getFullPlaylistTracks: $playlist")
        val tracks = mutableListOf<SongEntity>()
        var currentPage = 0
        while (true) {
            val pairs =
                localDataSource.getPlaylistPairSongByOffset(
                    playlistId = id,
                    filterState = FilterState.CustomOrder,
                    offset = currentPage,
                )
            if (pairs.isNullOrEmpty()) {
                break
            }
            val songs =
                localDataSource
                    .getSongByListVideoIdFull(
                        pairs.map { it.songId },
                    )
            val idValue = songs.associateBy { it.videoId }
            val sorted =
                pairs.mapNotNull {
                    idValue[it.songId]
                }
            tracks.addAll(sorted)
            currentPage++
        }
        return tracks
    }

    override suspend fun getListTrackVideoId(id: Long): List<String> {
        val playlist = localDataSource.getLocalPlaylist(id)
        return playlist?.tracks ?: emptyList()
    }

    override fun insertLocalPlaylist(
        localPlaylist: LocalPlaylistEntity,
        successMessage: String,
    ): Flow<LocalResource<String>> =
        wrapMessageResource(
            successMessage = successMessage,
        ) {
            localDataSource.insertLocalPlaylist(localPlaylist)
        }

    override suspend fun insertLocalPlaylistWithTracks(
        localPlaylist: LocalPlaylistEntity,
        videoIds: List<String>,
    ): Long = withContext(Dispatchers.IO) {
        localDataSource.insertLocalPlaylistWithTracks(localPlaylist, videoIds)
    }

    override fun deleteLocalPlaylist(
        id: Long,
        successMessage: String,
    ) = wrapMessageResource(
        successMessage = successMessage,
    ) {
        localDataSource.deleteLocalPlaylist(id)
    }

    override fun updateTitleLocalPlaylist(
        id: Long,
        newTitle: String,
        updatedMessage: String,
        updatedYtMessage: String,
        errorMessage: String,
    ): Flow<LocalResource<String>> =
        flow {
            emit(LocalResource.Loading<String>())
            runCatching {
                localDataSource.updateLocalPlaylistTitle(id = id, title = newTitle)
            }.onSuccess {
                emit(LocalResource.Success(updatedMessage))
                val localPlaylist = localDataSource.getLocalPlaylist(id)
                val ytId = localPlaylist?.youtubePlaylistId
                if (ytId != null) {
                    youTube
                        .editPlaylist(ytId, newTitle)
                        .onSuccess {
                            emit(LocalResource.Success(updatedYtMessage))
                        }.onFailure {
                            emit(LocalResource.Error<String>(it.message ?: errorMessage))
                        }
                }
            }.onFailure {
                emit(LocalResource.Error<String>(it.message ?: errorMessage))
            }
        }.flowOn(Dispatchers.IO)

    override fun updateThumbnailLocalPlaylist(
        id: Long,
        newThumbnail: String,
        successMessage: String,
    ) = flow<LocalResource<String>> {
        emit(LocalResource.Loading())
        runCatching {
            localDataSource.updateLocalPlaylistThumbnail(id = id, thumbnail = newThumbnail)
            pushCustomThumbnailToYouTube(id, newThumbnail)
        }.onSuccess { youTubeError ->
            // The image IS saved locally either way. Reporting the YouTube half as an error anyway
            // is deliberate: the user pressed this to change the cover of a synced playlist, and
            // silently keeping the old one on YouTube is the failure they need to know about.
            if (youTubeError == null) {
                emit(LocalResource.Success(successMessage))
            } else {
                emit(LocalResource.Error<String>(youTubeError))
            }
        }.onFailure {
            emit(LocalResource.Error<String>(it.message ?: "Error updating playlist thumbnail"))
        }
    }.flowOn(Dispatchers.IO)

    override fun updateDownloadState(
        id: Long,
        downloadState: Int,
        successMessage: String,
    ) = wrapMessageResource(
        successMessage = successMessage,
    ) {
        localDataSource.updateLocalPlaylistDownloadState(id = id, downloadState = downloadState)
    }

    override fun syncYouTubePlaylistToLocalPlaylist(
        playlist: PlaylistState,
        tracks: List<Track>,
        successMessage: String,
        errorMessage: String,
    ): Flow<LocalResource<String>> =
        flow<LocalResource<String>> {
            emit(LocalResource.Loading())
            val localPlaylistEntity =
                LocalPlaylistEntity(
                    title = playlist.title,
                    thumbnail = playlist.thumbnail,
                    youtubePlaylistId = playlist.id,
                    tracks = tracks.toListVideoId(),
                    downloadState = DownloadState.STATE_NOT_DOWNLOADED,
                    syncState = Syncing,
                )
            runBlocking { localDataSource.insertLocalPlaylist(localPlaylistEntity) }
            val localPlaylistId =
                localDataSource.getLocalPlaylistByYoutubePlaylistId(playlist.id)?.id
                    ?: throw Exception(errorMessage)
            tracks.forEachIndexed { i, track ->
                runBlocking {
                    localDataSource.insertSong(
                        track.toSongEntity(),
                    )
                    localDataSource.insertPairSongLocalPlaylist(
                        PairSongLocalPlaylist(
                            playlistId = localPlaylistId,
                            songId = track.videoId,
                            position = i,
                            inPlaylist = now(),
                        ),
                    )
                }
            }
            val ytPlaylistId = playlist.id
            val id = ytPlaylistId.verifyYouTubePlaylistId()
            youTube
                .customQuery(browseId = id, setLogin = true)
                .onSuccess { res ->
                    val listContent: ArrayList<MusicShelfRenderer.Content> = arrayListOf()
                    val data =
                        res.contents
                            ?.twoColumnBrowseResultsRenderer
                            ?.secondaryContents
                            ?.sectionListRenderer
                            ?.contents
                            ?.firstOrNull()
                            ?.musicPlaylistShelfRenderer
                            ?.contents
                    data?.let { listContent.addAll(it) }
                    var continueParam =
                        res.contents
                            ?.twoColumnBrowseResultsRenderer
                            ?.secondaryContents
                            ?.sectionListRenderer
                            ?.continuations
                            ?.firstOrNull()
                            ?.nextContinuationData
                            ?.continuation
                    while (continueParam != null) {
                        youTube
                            .customQuery(
                                "",
                                continuation = continueParam,
                                setLogin = true,
                            ).onSuccess { values ->
                                val dataMore: List<MusicShelfRenderer.Content>? =
                                    values.continuationContents
                                        ?.sectionListContinuation
                                        ?.contents
                                        ?.firstOrNull()
                                        ?.musicShelfRenderer
                                        ?.contents
                                if (dataMore != null) {
                                    listContent.addAll(dataMore)
                                }
                                continueParam =
                                    values.continuationContents
                                        ?.sectionListContinuation
                                        ?.continuations
                                        ?.firstOrNull()
                                        ?.nextContinuationData
                                        ?.continuation
                            }.onFailure { continueParam = null }
                    }
                    if (listContent.isEmpty()) {
                        emit(LocalResource.Error("Can't get setVideoId"))
                    }
                    val parsed = parseSetVideoId(ytPlaylistId, listContent)
                    if (parsed.isEmpty()) {
                        emit(LocalResource.Error("Can't get setVideoId"))
                    }
                    parsed.forEach { setVideoId ->
                        localDataSource.insertSetVideoId(setVideoId)
                    }
                    localDataSource.updateLocalPlaylistYouTubePlaylistSyncState(
                        localPlaylistId,
                        Synced,
                    )
                    emit(LocalResource.Success(successMessage))
                }.onFailure {
                    emit(LocalResource.Error("Can't get setVideoId"))
                }
        }.flowOn(
            Dispatchers.IO,
        )

    /**
     * Sync local playlist to YouTube playlist
     * return youtubePlaylistId
     * @param playlistId
     * @return Flow<LocalResource<String>>
     */
    override fun syncLocalPlaylistToYouTubePlaylist(
        playlistId: Long,
        successMessage: String,
        errorMessage: String,
    ) = flow<LocalResource<String>> {
        emit(LocalResource.Loading())
        val playlist = localDataSource.getLocalPlaylist(playlistId) ?: return@flow
        val res =
            youTube.createPlaylist(
                playlist.title,
                playlist.tracks,
            )
        val value = res.getOrNull()
        if (res.isSuccess && value != null) {
            val ytId = value.playlistId
            Logger.d(TAG, "syncLocalPlaylistToYouTubePlaylist: $ytId")
            // The YouTube playlist exists as soon as createPlaylist succeeds. Persist its id now
            // so a later failure while fetching set-video ids cannot leave an unlinked local copy
            // or cause a retry to create a duplicate remote playlist.
            localDataSource.updateLocalPlaylistYouTubePlaylistId(playlistId, ytId)
            localDataSource.updateLocalPlaylistYouTubePlaylistSyncState(playlistId, Synced)
            youTube
                .getYouTubePlaylistFullTracksWithSetVideoId(ytId)
                .onSuccess { list ->
                    Logger.d(TAG, "syncLocalPlaylistToYouTubePlaylist: onSuccess song ${list.map { it.first.title }}")
                    Logger.d(TAG, "syncLocalPlaylistToYouTubePlaylist: onSuccess setVideoId ${list.map { it.second }}")
                    list.forEach { new ->
                        localDataSource.insertSong(new.first.toTrack().toSongEntity())
                        localDataSource.insertSetVideoId(
                            SetVideoIdEntity(
                                videoId = new.first.id,
                                setVideoId = new.second,
                                youtubePlaylistId = ytId,
                            ),
                        )
                    }
                    if (list.isEmpty()) Logger.w(TAG, "syncLocalPlaylistToYouTubePlaylist: SetVideoIds Empty list")
                    // Carry a cover the user picked over to the new YouTube playlist. Without this
                    // the playlist arrives wearing YouTube's own four-song collage, which is what
                    // the user was trying to replace in the first place.
                    //
                    // The returned reason is deliberately dropped HERE and only logged: the sync
                    // itself has already succeeded, and reporting a cover problem as a sync error
                    // would tell the user the playlist did not sync when it did. The other caller,
                    // where changing the cover IS the whole operation, does surface it.
                    pushCustomThumbnailToYouTube(playlistId, playlist.thumbnail)
                    Logger.d(TAG, "syncLocalPlaylistToYouTubePlaylist: $ytId")
                    emit(LocalResource.Success(ytId))
                }.onFailure {
                    // The playlist was created remotely with its tracks. Set-video ids are only
                    // needed for later YouTube-side edits, so keep the successful sync and linked
                    // id even when this optional readback fails.
                    Logger.w(TAG, "syncLocalPlaylistToYouTubePlaylist: set-video readback failed: ${it.message}")
                    emit(LocalResource.Success(ytId))
                }
        } else {
            val e = res.exceptionOrNull()
            e?.printStackTrace()
            emit(LocalResource.Error(e?.message ?: errorMessage))
        }
    }

    /**
     * Mirrors a locally picked cover onto the YouTube playlist backing this local one.
     *
     * Silent on every failure. It is a best-effort extra on top of an operation the user already
     * considers done — the cover is saved locally either way, and a signed-out account or a revoked
     * file permission must not turn "cover changed" into an error.
     */
    private suspend fun pushCustomThumbnailToYouTube(
        playlistId: Long,
        thumbnail: String?,
    ): String? {
        // Returns null when there was nothing to do or the upload worked, and a reason when the
        // user should be told. Two of the three "nothing to do" cases are NOT failures: a playlist
        // that is not synced has no YouTube cover to set, and a http(s) thumbnail is one this app
        // derived from a track rather than something the user picked — pushing that back would
        // overwrite the cover with art nobody chose.
        val ytId =
            runCatching {
                localDataSource
                    .getLocalPlaylist(playlistId)
                    ?.youtubePlaylistId
                    ?.takeIf { it.isNotBlank() }
            }.getOrNull()
        if (ytId == null) {
            Logger.d(TAG, "Cover push skipped for $playlistId: playlist is not synced to YouTube")
            return null
        }
        val uri = thumbnail?.takeIf { it.isNotBlank() && !it.startsWith("http") }
        if (uri == null) {
            Logger.d(TAG, "Cover push skipped for $playlistId: not a locally picked image ($thumbnail)")
            return null
        }
        val bytes = readLocalImageBytes(uri)
        if (bytes == null) {
            Logger.w(TAG, "Cover push failed for $playlistId: could not read $uri")
            return "Could not read the selected image"
        }
        Logger.d(TAG, "Uploading cover for $playlistId -> $ytId (${bytes.size} bytes)")
        return youTube
            .setPlaylistCustomThumbnail(ytId, bytes)
            .fold(
                onSuccess = { status ->
                    Logger.d(TAG, "Cover uploaded for $ytId, status $status")
                    if (status in 200..299) null else "YouTube rejected the cover (HTTP $status)"
                },
                onFailure = {
                    Logger.e(TAG, "Cover upload failed for $ytId: ${it.message}")
                    it.message ?: "Could not update the cover on YouTube"
                },
            )
    }

    override fun unsyncLocalPlaylist(
        id: Long,
        successMessage: String,
    ) = wrapMessageResource(
        successMessage = successMessage,
    ) {
        localDataSource.unsyncLocalPlaylist(id)
    }

    override fun updateSyncState(
        id: Long,
        syncState: Int,
        successMessage: String,
    ) = wrapMessageResource(
        successMessage = successMessage,
    ) {
        localDataSource.updateLocalPlaylistYouTubePlaylistSyncState(id, syncState)
    }

    override fun updateYouTubePlaylistId(
        id: Long,
        youtubePlaylistId: String,
        successMessage: String,
    ) = wrapMessageResource(
        successMessage = successMessage,
    ) {
        localDataSource.updateLocalPlaylistYouTubePlaylistId(id, youtubePlaylistId)
    }

    override fun updateListTrackSynced(id: Long) =
        flow<Boolean> {
            val localPlaylist = localDataSource.getLocalPlaylist(id) ?: return@flow
            val tracks = localPlaylist.tracks ?: emptyList()
            val currentTracks = tracks.toMutableList()
            localPlaylist.youtubePlaylistId?.let { ytId ->
                Logger.d(TAG, "updateListTrackSynced: $ytId")
                youTube
                    .getYouTubePlaylistFullTracksWithSetVideoId(ytId)
                    .onSuccess { list ->
                        Logger.d(TAG, "updateListTrackSynced: onSuccess ${list.map { it.first.title }}")
                        val newTrack =
                            list
                                .map { it.first }
                                .toListTrack()
                                .map { it.videoId }
                                .toMutableSet()
                                .subtract(tracks.toMutableSet())
                        val newTrackList = list.filter { newTrack.contains(it.first.id) }
                        Logger.w(TAG, "updateListTrackSynced: newTrackList ${newTrackList.map { it.first.title }}")
                        newTrackList.forEach { new ->
                            localDataSource.insertSong(new.first.toTrack().toSongEntity())
                            Logger.i(TAG, "insertSong: ${new.first.toTrack().toSongEntity()}")
                            localDataSource.insertPairSongLocalPlaylist(
                                PairSongLocalPlaylist(
                                    playlistId = id,
                                    songId = new.first.id,
                                    position = currentTracks.size,
                                    inPlaylist = now(),
                                ),
                            )
                            localDataSource.insertSetVideoId(
                                SetVideoIdEntity(
                                    videoId = new.first.id,
                                    setVideoId = new.second,
                                    youtubePlaylistId = ytId,
                                ),
                            )
                            currentTracks.add(new.first.id)
                        }
                        localDataSource.updateLocalPlaylistTracks(currentTracks, id).let {
                            emit(true)
                        }
                    }.onFailure { e ->
                        Logger.e(TAG, "updateListTrackSynced: onFailure ${e.message}")
                        e.printStackTrace()
                        emit(false)
                    }
            }
            emit(false)
        }

    // Update
    override fun addTrackToLocalPlaylist(
        id: Long,
        song: SongEntity,
        successMessage: String,
        updatedYtMessage: String,
        errorMessage: String,
    ): Flow<LocalResource<String>> =
        flow {
            emit(LocalResource.Loading())
            val localPlaylist = localDataSource.getLocalPlaylist(id) ?: return@flow
            if (song.videoId in localPlaylist.tracks.orEmpty()) {
                emit(LocalResource.Error(PLAYLIST_ITEM_ALREADY_PRESENT))
                return@flow
            }
            val checkSong = localDataSource.getSong(song.videoId)
            if (checkSong == null) {
                localDataSource.insertSong(song)
            }
            val nextPosition = localPlaylist.tracks?.size ?: 0
            val nextPair =
                PairSongLocalPlaylist(
                    playlistId = id,
                    songId = song.videoId,
                    position = nextPosition,
                    inPlaylist = now(),
                )
            runBlocking {
                localDataSource.insertPairSongLocalPlaylist(nextPair)
                localDataSource.updateLocalPlaylistTracks(
                    localPlaylist.tracks?.plus(song.videoId) ?: mutableListOf(song.videoId),
                    id,
                )
            }
            // Emit success message
            emit(LocalResource.Success(successMessage))

            // Add to YouTube playlist
            val ytId = localPlaylist.youtubePlaylistId
            if (ytId != null) {
                youTube
                    .addPlaylistItem(ytId, song.videoId)
                    .onSuccess {
                        val data = it.playlistEditResults
                        if (data.isNotEmpty()) {
                            for (d in data) {
                                localDataSource.insertSetVideoId(
                                    SetVideoIdEntity(
                                        d.playlistEditVideoAddedResultData.videoId,
                                        d.playlistEditVideoAddedResultData.setVideoId,
                                    ),
                                )
                            }
                            PlaylistMembershipChanges.record(ytId)
                            emit(LocalResource.Success(updatedYtMessage))
                        } else {
                            emit(LocalResource.Error<String>("$errorMessage: Empty playlistEditResults"))
                        }
                    }.onFailure {
                        emit(LocalResource.Error<String>("$errorMessage: ${it.message}"))
                    }
            }
        }.flowOn(Dispatchers.IO)

    override fun removeTrackFromLocalPlaylist(
        id: Long,
        song: SongEntity,
        successMessage: String,
        updatedYtMessage: String,
        errorMessage: String,
    ): Flow<LocalResource<String>> =
        flow {
            emit(LocalResource.Loading())
            val localPlaylist = localDataSource.getLocalPlaylist(id) ?: return@flow
            val nextTracks = localPlaylist.tracks?.toMutableList() ?: mutableListOf()
            nextTracks.remove(song.videoId)
            localDataSource.updateLocalPlaylistTracks(nextTracks, id)
            localDataSource.deletePairSongLocalPlaylist(id, song.videoId)
            emit(LocalResource.Success(successMessage))
            val ytPlaylistId = localPlaylist.youtubePlaylistId
            if (ytPlaylistId != null) {
                val normalizedPlaylistId = ytPlaylistId.removePrefix("VL")
                var setVideoId = localDataSource.getSetVideoId(song.videoId, normalizedPlaylistId)?.setVideoId
                if (setVideoId.isNullOrBlank()) {
                    getYouTubeSetVideoId(normalizedPlaylistId).collect {}
                    setVideoId = localDataSource.getSetVideoId(song.videoId, normalizedPlaylistId)?.setVideoId
                }
                if (!setVideoId.isNullOrBlank()) {
                    youTube
                        .removeItemYouTubePlaylist(normalizedPlaylistId, song.videoId, setVideoId)
                        .onSuccess {
                            localDataSource.deleteSetVideoId(song.videoId, normalizedPlaylistId)
                            PlaylistMembershipChanges.record(normalizedPlaylistId)
                            emit(LocalResource.Success(successMessage))
                        }.onFailure {
                            emit(LocalResource.Error<String>("$errorMessage: ${it.message}"))
                        }
                } else {
                    emit(LocalResource.Error<String>("$errorMessage: SetVideoId is null"))
                }
            }
        }.flowOn(Dispatchers.IO)

    override fun getSuggestionsTrackForPlaylist(id: Long): Flow<LocalResource<Pair<String?, List<Track>>>> =
        flow {
            val youtubePlaylistId = localDataSource.getLocalPlaylist(id)?.youtubePlaylistId ?: return@flow
            getSuggestionsTrackForYouTubePlaylist(youtubePlaylistId).collect { emit(it) }
        }.flowOn(Dispatchers.IO)

    override fun getSuggestionsTrackForYouTubePlaylist(playlistId: String): Flow<LocalResource<Pair<String?, List<Track>>>> =
        flow {
            youTube
                .getSuggestionsTrackForPlaylist(playlistId.removePrefix("VL"))
                .onSuccess { data ->
                    val tracks = data?.second?.map { it.toTrack() }
                    if (data != null && !tracks.isNullOrEmpty()) {
                        emit(LocalResource.Success(data.first to tracks))
                    } else {
                        emit(LocalResource.Error("List suggestions is null"))
                    }
                }.onFailure { e ->
                    e.printStackTrace()
                    emit(LocalResource.Error(e.message ?: "Error"))
                }
        }.flowOn(Dispatchers.IO)

    override fun reloadSuggestionPlaylist(reloadParams: String): Flow<LocalResource<Pair<String?, List<Track>>>> =
        flow {
            runCatching {
                emit(LocalResource.Loading())
                youTube
                    .customQuery(browseId = "", continuation = reloadParams, setLogin = true)
                    .onSuccess { values ->
                        val data = values.continuationContents?.musicShelfContinuation?.contents
                        val dataResult:
                            ArrayList<SearchResponse.ContinuationContents.MusicShelfContinuation.Content> =
                            arrayListOf()
                        if (!data.isNullOrEmpty()) {
                            dataResult.addAll(data)
                        }
                        val reloadParamsNew =
                            values.continuationContents
                                ?.musicShelfContinuation
                                ?.continuations
                                ?.get(
                                    0,
                                )?.reloadContinuationData
                                ?.continuation
                        if (dataResult.isNotEmpty()) {
                            val listTrack: ArrayList<Track> = arrayListOf()
                            dataResult.forEach {
                                listTrack.add(
                                    (
                                        SearchPage.toYTItem(
                                            it.musicResponsiveListItemRenderer,
                                        ) as SongItem
                                    ).toTrack(),
                                )
                            }
                            emit(LocalResource.Success(Pair(reloadParamsNew, listTrack.toList())))
                        } else {
                            emit(LocalResource.Error("Empty data"))
                        }
                    }.onFailure { exception ->
                        exception.printStackTrace()
                        emit(LocalResource.Error(exception.message ?: "Error"))
                    }
            }
        }.flowOn(Dispatchers.IO)

    override fun getYouTubeSetVideoId(youtubePlaylistId: String): Flow<List<SetVideoIdEntity>> =
        flow {
            runCatching {
                var id = ""
                if (!youtubePlaylistId.startsWith("VL")) {
                    id += "VL$youtubePlaylistId"
                } else {
                    id += youtubePlaylistId
                }
                Logger.d("Repository", "playlist id: $id")
                youTube
                    .customQuery(browseId = id, setLogin = true)
                    .onSuccess { result ->
                        val listContent: ArrayList<SongItem> = arrayListOf()
                        val data: List<MusicShelfRenderer.Content>? =
                            result.contents
                                ?.singleColumnBrowseResultsRenderer
                                ?.tabs
                                ?.get(
                                    0,
                                )?.tabRenderer
                                ?.content
                                ?.sectionListRenderer
                                ?.contents
                                ?.get(
                                    0,
                                )?.musicPlaylistShelfRenderer
                                ?.contents
                        var continueParam =
                            result.getPlaylistContinuation()
                        var count = 0
                        var previousContinuation: String? = null
                        Logger.d("Repository", "playlist data: ${listContent.size}")
                        Logger.d("Repository", "continueParam: $continueParam")
                        while (continueParam != null && continueParam != previousContinuation && count < 100) {
                            val currentContinuation = continueParam
                            previousContinuation = currentContinuation
                            youTube
                                .customQuery(
                                    browseId = "",
                                    continuation = currentContinuation,
                                    setLogin = true,
                                ).onSuccess { values ->
                                    Logger.d("getPlaylistData", "continue: $currentContinuation")
                                    Logger.d(
                                        "getPlaylistData",
                                        "values: ${values.onResponseReceivedActions}",
                                    )
                                    val dataMore: List<SongItem> =
                                        values.onResponseReceivedActions
                                            ?.firstOrNull()
                                            ?.appendContinuationItemsAction
                                            ?.continuationItems
                                            ?.apply {
                                                Logger.w("getPlaylistData", "dataMore: ${this.size}")
                                            }?.mapNotNull {
                                                NextPage.fromMusicResponsiveListItemRenderer(
                                                    it.musicResponsiveListItemRenderer ?: return@mapNotNull null,
                                                )
                                            } ?: emptyList()
                                    listContent.addAll(dataMore)
                                    continueParam =
                                        values.getPlaylistContinuation()
                                    count++
                                }.onFailure {
                                    Logger.e("Continue", "Error: ${it.message}")
                                    continueParam = null
                                    count++
                                }
                        }
                        Logger.d("Repository", "playlist final data: ${listContent.size}")
                        parseSetVideoId(youtubePlaylistId, data ?: emptyList()).let { playlist ->
                            playlist.forEach { item ->
                                localDataSource.insertSetVideoId(item)
                            }
                            listContent.forEach { item ->
                                localDataSource.insertSetVideoId(
                                    SetVideoIdEntity(
                                        videoId = item.id,
                                        setVideoId = item.setVideoId,
                                        youtubePlaylistId = youtubePlaylistId,
                                    ),
                                )
                            }
                            emit(playlist)
                        }
                    }.onFailure { e ->
                        e.printStackTrace()
                        emit(emptyList())
                    }
            }
        }.flowOn(Dispatchers.IO)

    override fun addYouTubePlaylistItem(
        youtubePlaylistId: String,
        videoId: String,
    ): Flow<LocalResource<String>> =
        flow {
            emit(LocalResource.Loading())
            val normalizedPlaylistId = youtubePlaylistId.removePrefix("VL")
            val result = youTube.addPlaylistItem(normalizedPlaylistId.verifyYouTubePlaylistId(), videoId)
            val failure = result.exceptionOrNull()
            if (failure is CancellationException) throw failure
            val response = result.getOrNull()
            if (response != null && response.playlistEditResults.isNotEmpty()) {
                for (playlistEditResult in response.playlistEditResults) {
                    localDataSource.insertSetVideoId(
                        SetVideoIdEntity(
                            playlistEditResult.playlistEditVideoAddedResultData.videoId,
                            playlistEditResult.playlistEditVideoAddedResultData.setVideoId,
                            normalizedPlaylistId,
                        ),
                    )
                }
                PlaylistMembershipChanges.record(normalizedPlaylistId)
                emit(LocalResource.Success(response.status))
            } else {
                val message = rejectedPlaylistAddMessage(videoId) { continuation ->
                    youTube.customQuery(
                        browseId = if (continuation == null) normalizedPlaylistId.verifyYouTubePlaylistId() else "",
                        continuation = continuation,
                        setLogin = true,
                    ).map(::playlistMembershipPage)
                }
                emit(LocalResource.Error(message))
            }
        }.flowOn(Dispatchers.IO)

    override fun removeYouTubePlaylistItem(
        youtubePlaylistId: String,
        videoId: String,
        successMessage: String,
        errorMessage: String,
    ): Flow<LocalResource<String>> =
        flow {
            emit(LocalResource.Loading())
            val normalizedPlaylistId = youtubePlaylistId.removePrefix("VL")
            var setVideoId = localDataSource.getSetVideoId(videoId, normalizedPlaylistId)?.setVideoId
            if (setVideoId.isNullOrBlank()) {
                // The playlist parser doesn't expose YouTube's per-item edit token. Fetch the
                // playlist once to cache its setVideoIds, then resolve this item for this playlist.
                getYouTubeSetVideoId(normalizedPlaylistId).collect {}
                setVideoId = localDataSource.getSetVideoId(videoId, normalizedPlaylistId)?.setVideoId
            }
            if (setVideoId.isNullOrBlank()) {
                emit(LocalResource.Error(errorMessage))
                return@flow
            }
            youTube
                .removeItemYouTubePlaylist(normalizedPlaylistId, videoId, setVideoId)
                .onSuccess {
                        localDataSource.deleteSetVideoId(videoId, normalizedPlaylistId)
                        PlaylistMembershipChanges.record(normalizedPlaylistId)
                        emit(LocalResource.Success(successMessage))
                }.onFailure {
                    emit(LocalResource.Error(errorMessage))
                }
        }.flowOn(Dispatchers.IO)

    override suspend fun insertPairSongLocalPlaylist(pairSongLocalPlaylist: PairSongLocalPlaylist) =
        withContext(Dispatchers.IO) {
            localDataSource.insertPairSongLocalPlaylist(pairSongLocalPlaylist)
        }

    override fun getPlaylistPairSongByListPosition(
        playlistId: Long,
        listPosition: List<Int>,
    ): Flow<List<PairSongLocalPlaylist>?> =
        flow {
            emit(localDataSource.getPlaylistPairSongByListPosition(playlistId, listPosition))
        }.flowOn(Dispatchers.IO)

    override fun getPlaylistPairSongByOffset(
        playlistId: Long,
        offset: Int,
        filterState: FilterState,
    ): Flow<List<PairSongLocalPlaylist>?> =
        flow {
            emit(localDataSource.getPlaylistPairSongByOffset(playlistId, offset, filterState))
        }.flowOn(Dispatchers.IO)

    override fun getPlaylistPairSongByTime(
        playlistId: Long,
        filterState: FilterState,
        localDateTime: LocalDateTime,
    ): Flow<List<PairSongLocalPlaylist>?> =
        flow {
            emit(
                localDataSource.getPlaylistPairSongByTime(
                    playlistId,
                    filterState,
                    localDateTime,
                ),
            )
        }.flowOn(Dispatchers.IO)

    override fun getPlaylistPairOfSong(
        playlistId: Long,
        videoId: String,
    ): Flow<PairSongLocalPlaylist?> =
        flow {
            emit(
                localDataSource.getPlaylistPairOfSong(
                    videoId,
                    playlistId,
                ),
            )
        }.flowOn(Dispatchers.IO)

    override fun changePositionOfSongInPlaylist(
        playlistId: Long,
        videoId: String,
        newPosition: Int,
    ): Flow<String> =
        flow {
            localDataSource.editPositionOfSongInPlaylist(
                playlistId,
                videoId,
                newPosition,
            )
            delay(100)
            emit("Position updated")
        }.flowOn(Dispatchers.IO)

    /**
     * Move a song within a synced playlist.
     * 1. Resolve setVideoIds for the moved item and its new successor from the local DB
     * 2. Call YouTube API (ACTION_MOVE_VIDEO_BEFORE) to reorder on YouTube
     * 3. Update local DB positions (shift items between from/to, then place the moved item)
     */
    override fun moveItemInSyncedPlaylist(
        playlistId: Long,
        fromIndex: Int,
        toIndex: Int,
    ): Flow<LocalResource<String>> =
        flow<LocalResource<String>> {
            if (fromIndex == toIndex) {
                emit(LocalResource.Success("No change"))
                return@flow
            }
            emit(LocalResource.Loading())

            val localPlaylist = localDataSource.getLocalPlaylist(playlistId) ?: run {
                emit(LocalResource.Error("Playlist not found"))
                return@flow
            }
            val ytPlaylistId = localPlaylist.youtubePlaylistId ?: run {
                emit(LocalResource.Error("Playlist is not synced with YouTube"))
                return@flow
            }

            // Get all pairs ordered by position to resolve indexes
            val allPairs = localDataSource.getAllPlaylistPairSongByPosition(playlistId)
            if (fromIndex >= allPairs.size || toIndex >= allPairs.size) {
                emit(LocalResource.Error("Index out of bounds"))
                return@flow
            }

            val movedPair = allPairs[fromIndex]
            val movedVideoId = movedPair.songId

            // Resolve setVideoId of the moved item
            val movedSetVideoIdEntity = localDataSource.getSetVideoId(movedVideoId)
            val movedSetVideoId = movedSetVideoIdEntity?.setVideoId ?: run {
                emit(LocalResource.Error("SetVideoId not found for moved item: $movedVideoId"))
                return@flow
            }

            // Resolve the successor's setVideoId (the item that should come AFTER the moved item)
            // If moving down (fromIndex < toIndex): successor is the item at toIndex + 1 (if exists)
            // If moving up (fromIndex > toIndex): successor is the item at toIndex
            val successorSetVideoId: String? = if (fromIndex < toIndex) {
                // Moving down: after removal, the successor is at toIndex + 1 in original list
                val successorIndex = toIndex + 1
                if (successorIndex < allPairs.size) {
                    val successorVideoId = allPairs[successorIndex].songId
                    localDataSource.getSetVideoId(successorVideoId)?.setVideoId
                } else {
                    null // Move to end
                }
            } else {
                // Moving up: successor is the item currently at toIndex
                val successorVideoId = allPairs[toIndex].songId
                localDataSource.getSetVideoId(successorVideoId)?.setVideoId
            }

            // Step 1: Call YouTube API
            youTube
                .movePlaylistItem(
                    playlistId = ytPlaylistId,
                    setVideoId = movedSetVideoId,
                    movedSetVideoIdSuccessor = successorSetVideoId,
                ).onSuccess {
                    Logger.d(TAG, "moveItemInSyncedPlaylist: YouTube API success, status=$it")
                }.onFailure { e ->
                    Logger.e(TAG, "moveItemInSyncedPlaylist: YouTube API failed: ${e.message}")
                    emit(LocalResource.Error("Failed to reorder on YouTube: ${e.message}"))
                    return@flow
                }

            // Step 2: Update local DB positions — read, shift and place as one transaction. The API
            // call above stays outside it: a DB lock must never be held while waiting on the network.
            if (!localDataSource.moveSongInPlaylist(playlistId, fromIndex, toIndex)) {
                emit(LocalResource.Error("Index out of bounds"))
                return@flow
            }

            emit(LocalResource.Success("Position updated"))
        }.flowOn(Dispatchers.IO)

    /**
     * Move a song within an unsynced local playlist.
     * Same position bookkeeping as [moveItemInSyncedPlaylist], without the YouTube API call:
     * shift the items between from/to by one, then place the moved item at the target position.
     */
    override fun moveItemInLocalPlaylist(
        playlistId: Long,
        fromIndex: Int,
        toIndex: Int,
    ): Flow<LocalResource<String>> =
        flow<LocalResource<String>> {
            if (fromIndex == toIndex) {
                emit(LocalResource.Success("No change"))
                return@flow
            }
            emit(LocalResource.Loading())

            if (!localDataSource.moveSongInPlaylist(playlistId, fromIndex, toIndex)) {
                emit(LocalResource.Error("Index out of bounds"))
                return@flow
            }

            emit(LocalResource.Success("Position updated"))
        }.flowOn(Dispatchers.IO)
}
