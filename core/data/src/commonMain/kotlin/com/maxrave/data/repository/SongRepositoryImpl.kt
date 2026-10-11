package com.maxrave.data.repository

import com.maxrave.kotlinytmusicscraper.parser.playlistMemberPage

import com.maxrave.domain.mediaservice.handler.PlaylistType
import com.maxrave.domain.mediaservice.handler.RadioQueueTrim
import com.maxrave.data.mapping.toTrack
import com.maxrave.domain.mediaservice.handler.selectQueueMedia
import com.maxrave.domain.mediaservice.handler.QueueMediaType
import com.maxrave.common.MERGING_DATA_TYPE
import com.maxrave.common.Config
import com.maxrave.data.db.datasource.LocalDataSource
import com.maxrave.data.extension.getFullDataFromDB
import com.maxrave.data.mapping.toListTrack
import com.maxrave.data.mapping.toSongItemForDownload
import com.maxrave.data.mapping.toWatchEndpoint
import com.maxrave.domain.data.entities.QueueEntity
import com.maxrave.domain.data.entities.DownloadState
import com.maxrave.domain.data.entities.SongEntity
import com.maxrave.domain.data.entities.SongInfoEntity
import com.maxrave.domain.data.model.browse.album.Track
import com.maxrave.domain.data.model.download.DownloadProgress
import com.maxrave.domain.data.model.streams.YouTubeWatchEndpoint
import com.maxrave.domain.manager.DataStoreManager
import com.maxrave.domain.manager.DataStoreManager.Values.TRUE
import com.maxrave.domain.mediaservice.handler.DownloadHandler
import com.maxrave.domain.mediaservice.handler.MediaPlayerHandler
import com.maxrave.domain.repository.SongRepository
import com.maxrave.domain.utils.Resource
import com.maxrave.kotlinytmusicscraper.YouTube
import com.maxrave.kotlinytmusicscraper.models.SongItem
import com.maxrave.kotlinytmusicscraper.models.WatchEndpoint
import com.maxrave.kotlinytmusicscraper.models.response.LikeStatus
import com.maxrave.kotlinytmusicscraper.pages.NextPage
import com.maxrave.kotlinytmusicscraper.parser.fromPlaylistContinuationToTracks
import com.maxrave.kotlinytmusicscraper.parser.getContinuePlaylistContinuation
import com.maxrave.kotlinytmusicscraper.parser.getPlaylistContinuation
import com.maxrave.logger.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.lastOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDateTime
import org.koin.mp.KoinPlatform.getKoin

private const val TAG = "SongRepositoryImpl"

internal class SongRepositoryImpl(
    private val dataStoreManager: DataStoreManager,
    private val localDataSource: LocalDataSource,
    private val youTube: YouTube,
) : SongRepository {
    private val _youTubeLikeChanges = MutableSharedFlow<Pair<String, Boolean>>(extraBufferCapacity = 32)

    override fun observeYouTubeLikeChanges(): Flow<Pair<String, Boolean>> = _youTubeLikeChanges.asSharedFlow()

    override fun getAllSongs(limit: Int): Flow<List<SongEntity>> =
        flow {
            emit(localDataSource.getAllSongs(limit))
        }.flowOn(Dispatchers.IO)

    override suspend fun setInLibrary(
        videoId: String,
        inLibrary: LocalDateTime,
    ) = withContext(Dispatchers.IO) { localDataSource.setInLibrary(videoId, inLibrary) }

    override fun getSongsByListVideoId(listVideoId: List<String>): Flow<List<SongEntity>> =
        flow {
            // SQLite's `WHERE videoId IN (:list)` does NOT preserve the input list order
            // (it returns rows in table/rowid order). Album tracks, local playlist items,
            // and queue snapshots all rely on caller-defined order, so we restore it here.
            val songs = localDataSource.getSongByListVideoIdFull(listVideoId)
            val byId = songs.associateBy { it.videoId }
            emit(listVideoId.mapNotNull { byId[it] })
        }.flowOn(Dispatchers.IO)

    override fun getDownloadedSongs(): Flow<List<SongEntity>?> =
        flow {
            emit(
                getFullDataFromDB { limit, offset ->
                    localDataSource.getDownloadedSongs(limit, offset)
                },
            )
        }.flowOn(Dispatchers.IO)

    override fun observeDownloadedSongs(): Flow<List<SongEntity>> =
        localDataSource.observeDownloadedSongs().distinctUntilChanged()

    override fun getDownloadingSongs(): Flow<List<SongEntity>?> =
        flow {
            emit(
                getFullDataFromDB { limit, offset ->
                    localDataSource.getDownloadingSongs(limit, offset)
                },
            )
        }.flowOn(Dispatchers.IO)

    override fun getPreparingSongs(): Flow<List<SongEntity>> =
        flow {
            emit(
                getFullDataFromDB { limit, offset ->
                    localDataSource.getPreparingSongs(limit, offset)
                },
            )
        }.flowOn(Dispatchers.IO)

    override fun getDownloadedVideoIdListFromListVideoIdAsFlow(listVideoId: List<String>) =
        localDataSource.getDownloadedVideoIdListFromListVideoIdAsFlow(listVideoId)

    override fun getLikedSongs(): Flow<List<SongEntity>> =
        flow {
            emit(
                getFullDataFromDB { limit, offset ->
                    localDataSource.getLikedSongs(limit, offset)
                },
            )
        }.flowOn(Dispatchers.IO)

    override fun observeLikedSongs(): Flow<List<SongEntity>> = localDataSource.observeLikedSongs()

    override suspend fun downloadAllLikedSongs(): Int =
        withContext(Dispatchers.IO) {
            val pending =
                getFullDataFromDB { limit, offset ->
                    localDataSource.getLikedSongs(limit, offset)
                }.filter { it.downloadState != DownloadState.STATE_DOWNLOADED }
            Logger.d(TAG, "Auto-download: queueing ${pending.size} liked songs")
            // Favorites and Downloads both show newest first. Complete older favorites
            // first so enabling auto-download preserves that order for the existing batch.
            pending.asReversed().forEach { song ->
                downloadHandler.downloadTrack(song.videoId, song.title, song.thumbnails.orEmpty())
            }
            pending.size
        }

    /**
     * The order below is the whole feature.
     *
     * A song is orphaned only once nothing points at it, so every cached container has to go first —
     * sweeping songs on their own found zero to delete, because eleven thousand of them were held
     * alive by playlists nobody prunes. Artists lead so the two tables keyed on `channelId` can be
     * measured against what is still followed; podcasts, albums and playlists follow; songs last,
     * then the satellite rows that were already orphaned before any of this ran.
     */
    override suspend fun clearHistoryAndOrphanedSongs(): Int =
        withContext(Dispatchers.IO) {
            persistLiveQueueBeforeSweep()
            localDataSource.deleteAllPlaybackEvents()
            val artists = localDataSource.deleteUnfollowedArtists()
            val notifications = localDataSource.deleteNotificationsOfUnfollowedArtists()
            val artistReleases = localDataSource.deleteFollowedArtistReleasesOfUnfollowedArtists()
            val podcasts = localDataSource.deleteUnfavoritedPodcasts()
            val albums = localDataSource.deleteUnreferencedAlbums()
            val playlists = localDataSource.deleteUnreferencedPlaylists()
            val orphans = localDataSource.getOrphanedSongIds()
            val removed = localDataSource.deleteSongsAndRelatedData(orphans)
            val satellites = localDataSource.deleteStaleSongSatellites()
            Logger.d(
                TAG,
                "Clear history: removed $removed of ${orphans.size} candidate songs, " +
                    "$artists artists, $notifications notifications, $artistReleases artist releases, " +
                    "$podcasts podcasts, $albums albums, $playlists playlists, $satellites stale rows",
            )
            // Last, and outside every statement above: SQLite refuses VACUUM inside a transaction.
            // Without it the file stays fragmented and less space comes back than the user expects.
            // The checkpoint first folds the WAL — which every delete above just filled — back into
            // the main file, so VACUUM actually has those pages to reclaim.
            //
            // Failure here is not failure of the operation: every delete has already committed, so
            // letting it throw would show an error toast for work that actually succeeded. It only
            // costs some disk space, and the next run reclaims it.
            runCatching {
                localDataSource.checkpoint()
                localDataSource.vacuum()
            }.onFailure { Logger.e(TAG, "VACUUM after clearing history failed: ${it.message}") }
            removed
        }

    /**
     * Write whatever is playing right now into the `queue` table, so the sweep cannot delete it.
     *
     * The sweep spares songs found in `queue.listTrack`, but that table is only written on pause,
     * on track change, and on exit — and only when the user has "save recent song and queue" on.
     * Two ways that loses the current session: with the setting off the table is empty, and even
     * with it on, the endless queue keeps appending tracks that no save has caught up with yet.
     * Either way the song playing through the speakers looks orphaned and goes.
     *
     * Deliberately ignores that setting: this is not "remember my queue for next launch", it is
     * "do not delete what is on screen". Writing nothing when the queue is empty matters too —
     * overwriting a previously saved queue with an empty one would strip the protection instead of
     * adding it.
     */
    private suspend fun persistLiveQueueBeforeSweep() {
        val liveQueue = mediaPlayerHandler.queueData.value?.data?.listTracks.orEmpty()
        if (liveQueue.isEmpty()) return
        Logger.d(TAG, "Clear history: pinning ${liveQueue.size} queued tracks before the sweep")
        localDataSource.recoverQueue(QueueEntity(listTrack = liveQueue))
    }

    override fun getCanvasSong(max: Int): Flow<List<SongEntity>> =
        flow {
            emit(localDataSource.getCanvasSong(max))
        }.flowOn(Dispatchers.IO)

    override fun getSongById(id: String): Flow<SongEntity?> =
        flow {
            emit(localDataSource.getSong(id))
        }.flowOn(Dispatchers.IO)

    override fun getSongAsFlow(id: String) = localDataSource.getSongAsFlow(id)

    override fun getLikedSongsByArtist(channelId: String): Flow<List<SongEntity>> = localDataSource.getLikedSongsByArtist(channelId)

    override fun insertSong(songEntity: SongEntity): Flow<Long> = flow<Long> { emit(localDataSource.insertSong(songEntity)) }.flowOn(Dispatchers.IO)

    override fun updateThumbnailsSongEntity(
        thumbnail: String,
        videoId: String,
    ): Flow<Int> = flow { emit(localDataSource.updateThumbnailsSongEntity(thumbnail, videoId)) }.flowOn(Dispatchers.IO)

    override fun updateVideoTypeSongEntity(
        videoType: String,
        videoId: String,
    ): Flow<Int> = flow { emit(localDataSource.updateVideoTypeSongEntity(videoType, videoId)) }.flowOn(Dispatchers.IO)

    override suspend fun updateListenCount(videoId: String) =
        withContext(Dispatchers.IO) {
            localDataSource.updateListenCount(videoId)
        }

    override suspend fun resetTotalPlayTime(videoId: String) =
        withContext(Dispatchers.IO) {
            localDataSource.resetTotalPlayTime(videoId)
        }

    /**
     * Resolved lazily rather than taken in the constructor: the download handler is implemented in
     * the platform media modules, and pulling it in at construction time would make this repository
     * depend on them being built first.
     */
    private val downloadHandler: DownloadHandler by lazy { getKoin().get() }

    /**
     * Lazy for the same reason as [downloadHandler], and here it also breaks a cycle: the handler
     * already depends on this repository (it calls [recoverQueue] whenever it saves the queue).
     */
    private val mediaPlayerHandler: MediaPlayerHandler by lazy { getKoin().get() }

    /**
     * Note the side effect: liking a song can start a download, when the user has asked for that.
     *
     * Deliberately one-way. It fires only on the way *in* (`likeStatus == 1`) and only for songs
     * not already offline, so re-liking something you already have does not queue it twice, and
     * unliking never removes a file — the user downloaded it, only they should undo that.
     */
    override suspend fun updateLikeStatus(
        videoId: String,
        likeStatus: Int,
    ) = withContext(Dispatchers.Main) {
        localDataSource.updateLiked(likeStatus, videoId)
        if (likeStatus == 1 && dataStoreManager.autoDownloadLikedSongs.first() == TRUE) {
            val song = localDataSource.getSong(videoId)
            if (song != null && song.downloadState != DownloadState.STATE_DOWNLOADED) {
                Logger.d(TAG, "Auto-downloading liked song: $videoId")
                downloadHandler.downloadTrack(videoId, song.title, song.thumbnails.orEmpty())
            }
        }
        if (dataStoreManager.loggedIn.first() == TRUE) {
            if (likeStatus == 1) {
                addToYouTubeLiked(videoId).collect { result ->
                    Logger.d(TAG, "updateLikeStatus -> addToYouTubeLiked: $result")
                    if (result in 200..299) _youTubeLikeChanges.tryEmit(videoId to true)
                }
            } else {
                removeFromYouTubeLiked(videoId).collect { result ->
                    Logger.d(TAG, "updateLikeStatus -> removeFromYouTubeLiked: $result")
                    if (result in 200..299) _youTubeLikeChanges.tryEmit(videoId to false)
                }
            }
        }
    }

    override suspend fun setLikedLocally(
        videoId: String,
        liked: Boolean,
    ) = withContext(Dispatchers.IO) {
        localDataSource.updateLiked(if (liked) 1 else 0, videoId)
    }

    override suspend fun cacheYouTubeLikedSongs(songs: List<SongEntity>) = withContext(Dispatchers.IO) {
        val uniqueSongs = songs.distinctBy { it.videoId }
        if (uniqueSongs.isEmpty()) return@withContext

        // Keep synced tracks out of the user's manually added library, while still giving them
        // local rows so the player and every Favorite star read the same liked state.
        localDataSource.insertSongs(
            uniqueSongs.map { song ->
                song.copy(liked = true, inLibrary = Config.REMOVED_SONG_DATE_TIME)
            },
        )
        uniqueSongs.filter { it.isLiveVideo }.map { it.videoId }.takeIf { it.isNotEmpty() }?.let {
            localDataSource.markSongsLive(it)
        }
        localDataSource.markSongsLiked(uniqueSongs.map { it.videoId })
    }

    override fun updateSongInLibrary(
        inLibrary: LocalDateTime,
        videoId: String,
    ): Flow<Int> = flow { emit(localDataSource.updateSongInLibrary(inLibrary, videoId)) }

    override suspend fun updateDurationSeconds(
        durationSeconds: Int,
        videoId: String,
    ) = withContext(Dispatchers.Main) {
        localDataSource.updateDurationSeconds(
            durationSeconds,
            videoId,
        )
    }

    override fun getMostPlayedSongs(): Flow<List<SongEntity>> = localDataSource.getMostPlayedSongs()

    override suspend fun updateDownloadState(
        videoId: String,
        downloadState: Int,
    ) = withContext(Dispatchers.Main) {
        localDataSource.updateDownloadState(
            downloadState,
            videoId,
        )
    }

    override suspend fun getRecentSong(
        limit: Int,
        offset: Int,
    ) = localDataSource.getRecentSongs(limit, offset)

    override suspend fun insertSongInfo(songInfo: SongInfoEntity) =
        withContext(Dispatchers.IO) {
            localDataSource.insertSongInfo(songInfo)
        }

    override suspend fun getSongInfoEntity(videoId: String): Flow<SongInfoEntity?> =
        flow { emit(localDataSource.getSongInfo(videoId)) }.flowOn(Dispatchers.Main)

    override suspend fun recoverQueue(temp: List<Track>) {
        val queueEntity = QueueEntity(listTrack = temp)
        withContext(Dispatchers.IO) { localDataSource.recoverQueue(queueEntity) }
    }

    override suspend fun removeQueue() {
        withContext(Dispatchers.IO) { localDataSource.deleteQueue() }
    }

    override suspend fun getSavedQueue(): Flow<List<QueueEntity>?> =
        flow {
            emit(localDataSource.getQueue())
        }.flowOn(Dispatchers.IO)

    /** Keep radio pages homogeneous, swapping to YouTube's matching version where possible. */
    private suspend fun List<SongItem>.forQueueMediaType(
        isRadio: Boolean,
        mediaType: QueueMediaType?,
    ): List<SongItem> {
        if (!isRadio && mediaType == null) return this
        val kind = mediaType ?: if (dataStoreManager.radioAudioOnly.first() == TRUE) {
            QueueMediaType.SONG
        } else {
            firstOrNull()?.toTrack()?.let(QueueMediaType::fromTrack) ?: return emptyList()
        }
        return selectQueueMedia(this, kind, { QueueMediaType.fromTrack(it.toTrack()) }, { it.counterpart })
            .distinctBy { it.id }
    }

    override fun getContinueTrack(
        playlistId: String,
        continuation: String,
        fromPlaylist: Boolean,
        mediaType: QueueMediaType?,
        requirePlaylistMembership: Boolean,
    ): Flow<Pair<ArrayList<Track>?, String?>> =
        flow {
            runCatching {
                var newContinuation: String? = null
                Logger.d(TAG, "getContinueTrack -> playlistId: $playlistId")
                Logger.d(TAG, "getContinueTrack -> continuation: $continuation")
                if (!fromPlaylist) {
                    youTube
                        .next(
                            if (playlistId.startsWith("RRDAMVM")) {
                                WatchEndpoint(videoId = playlistId.removePrefix("RRDAMVM"))
                            } else {
                                WatchEndpoint(playlistId = playlistId)
                            },
                            continuation = continuation,
                        ).onSuccess { next ->
                            val data: ArrayList<SongItem> = arrayListOf()
                            // Only this branch can be a radio — the `else` below continues a real
                            // playlist. `RRDAMVM…` counts as one too: it is YouTube's other
                            // spelling for the radio of a single video, which `isRadioQueueId`
                            // deliberately does not match because it never appears as a queue's
                            // own playlistId.
                            val isRadio =
                                playlistId.startsWith("RRDAMVM") || RadioQueueTrim.appliesTo(PlaylistType.RADIO, playlistId)
                            var page = next
                            var selected = page.items.forQueueMediaType(isRadio, mediaType)
                            val visited = mutableSetOf<String>()
                            // Opposite-kind pages can be empty after filtering. Advance their
                            // continuation rather than making the UI wait with an empty queue.
                            while (selected.isEmpty() && page.continuation?.let(visited::add) == true) {
                                page = youTube.next(
                                    if (playlistId.startsWith("RRDAMVM")) WatchEndpoint(videoId = playlistId.removePrefix("RRDAMVM"))
                                    else WatchEndpoint(playlistId = playlistId),
                                    continuation = page.continuation,
                                ).getOrNull() ?: break
                                selected = page.items.forQueueMediaType(isRadio, mediaType)
                            }
                            data.addAll(selected)
                            newContinuation = page.continuation
                            emit(Pair(data.toListTrack(), newContinuation))
                        }.onFailure { exception ->
                            exception.printStackTrace()
                            emit(Pair(null, null))
                        }
                } else {
                    youTube
                        .customQuery(
                            browseId = null,
                            continuation = continuation,
                            setLogin = true,
                        ).onSuccess { values ->
                            Logger.d(TAG, "getPlaylistData -> continue: $continuation")
                            Logger.d(TAG, "getPlaylistData -> values: ${values.onResponseReceivedActions}")
                            val page = values.playlistMemberPage(
                                requireMembership = requirePlaylistMembership,
                                allowLikedMusicShelves = playlistId.removePrefix("VL") == "LM",
                            )
                            if (playlistId.removePrefix("VL") == "LM") {
                                val appendCount = values.onResponseReceivedActions.orEmpty().count { it.appendContinuationItemsAction != null }
                                Logger.i("LikedMusicSync", "Continuation: rows=${page.rows.size}, recognized=${page.hasPlaylistSource}, appendActions=$appendCount, hasNext=${!page.continuation.isNullOrBlank()}")
                            }
                            if (playlistId.removePrefix("VL") == "LM" && !page.hasPlaylistSource) {
                                Logger.w(TAG, "Unrecognized playlist continuation response for $playlistId")
                                emit(Pair(null, continuation))
                                return@onSuccess
                            }
                            val dataMore = page.rows.mapNotNull(NextPage::fromMusicResponsiveListItemRenderer)
                            newContinuation = page.continuation
                            emit(
                                Pair<ArrayList<Track>?, String?>(
                                    dataMore.toListTrack(),
                                    newContinuation,
                                ),
                            )
                        }.onFailure {
                            Logger.e(TAG, "getContinueTrack -> Error: ${it.message}")
                            emit(Pair(null, null))
                        }
                }
            }
        }.flowOn(Dispatchers.IO)

    override fun getSongInfo(videoId: String): Flow<SongInfoEntity?> =
        flow {
            runCatching {
                val id =
                    if (videoId.contains(MERGING_DATA_TYPE.VIDEO)) {
                        videoId.removePrefix(MERGING_DATA_TYPE.VIDEO)
                    } else {
                        videoId
                    }
                youTube
                    .getSongInfo(id)
                    .onSuccess { songInfo ->
                        val song =
                            SongInfoEntity(
                                videoId = songInfo.videoId,
                                author = songInfo.author,
                                authorId = songInfo.authorId,
                                authorThumbnail = songInfo.authorThumbnail,
                                description = songInfo.description,
                                uploadDate = songInfo.uploadDate,
                                subscribers = songInfo.subscribers,
                                viewCount = songInfo.viewCount,
                                like = songInfo.like,
                                dislike = songInfo.dislike,
                            )
                        emit(song)
                        insertSongInfo(
                            song,
                        )
                    }.onFailure {
                        it.printStackTrace()
                        emit(getSongInfoEntity(videoId).lastOrNull())
                    }
            }
        }.flowOn(Dispatchers.IO)

    override suspend fun getLikeStatus(videoId: String): Flow<Boolean> =
        flow {
            runCatching {
                youTube
                    .getLikedInfo(videoId)
                    .onSuccess {
                        if (it == LikeStatus.LIKE) emit(true) else emit(false)
                    }.onFailure {
                        it.printStackTrace()
                        emit(false)
                    }
            }
        }

    override suspend fun addToYouTubeLiked(mediaId: String?): Flow<Int> =
        flow {
            if (mediaId != null) {
                runCatching {
                    youTube
                        .addToLiked(mediaId)
                        .onSuccess {
                            Logger.d(TAG, "Liked -> Success: $it")
                            emit(it)
                        }.onFailure {
                            it.printStackTrace()
                            emit(0)
                        }
                }
            }
        }.flowOn(Dispatchers.IO)

    override suspend fun removeFromYouTubeLiked(mediaId: String?): Flow<Int> =
        flow {
            if (mediaId != null) {
                runCatching {
                    youTube
                        .removeFromLiked(mediaId)
                        .onSuccess {
                            Logger.d(TAG, "Liked -> Success: $it")
                            emit(it)
                        }.onFailure {
                            it.printStackTrace()
                            emit(0)
                        }
                }
            }
        }.flowOn(Dispatchers.IO)

    override fun downloadToFile(
        track: Track,
        path: String,
        videoId: String,
        isVideo: Boolean,
    ): Flow<DownloadProgress> =
        youTube
            .download(
                track.toSongItemForDownload(),
                path,
                videoId,
                isVideo,
            ).map {
                DownloadProgress(
                    audioDownloadProgress = it.audioDownloadProgress,
                    videoDownloadProgress = it.videoDownloadProgress,
                    downloadSpeed = it.downloadSpeed,
                    errorMessage = it.errorMessage,
                    isMerging = it.isMerging,
                    isError = it.isError,
                    isDone = it.isDone,
                )
            }

    override fun getRelatedData(videoId: String, mediaType: QueueMediaType?): Flow<Resource<Pair<List<Track>, String?>>> =
        flow {
            runCatching {
                youTube
                    .next(WatchEndpoint(videoId = videoId))
                    .onSuccess { next ->
                        var page = next
                        var selected = page.items.forQueueMediaType(isRadio = true, mediaType = mediaType)
                            .filter { it.id != videoId }
                        val visited = mutableSetOf<String>()
                        while (selected.isEmpty() && page.continuation?.let(visited::add) == true) {
                            page = youTube.next(WatchEndpoint(videoId = videoId), continuation = page.continuation)
                                .getOrNull() ?: break
                            selected = page.items.forQueueMediaType(isRadio = true, mediaType = mediaType)
                                .filter { it.id != videoId }
                        }
                        val data = selected.toListTrack()
                        val nextContinuation = page.continuation
                        emit(Resource.Success<Pair<List<Track>, String?>>(Pair(data.toList(), nextContinuation)))
                    }.onFailure { exception ->
                        exception.printStackTrace()
                        emit(Resource.Error<Pair<List<Track>, String?>>(exception.message.toString()))
                    }
            }
        }.flowOn(Dispatchers.IO)

    override fun getRadioFromEndpoint(endpoint: YouTubeWatchEndpoint, mediaType: QueueMediaType?): Flow<Resource<Pair<List<Track>, String?>>> =
        flow {
            runCatching {
                youTube
                    .next(endpoint.toWatchEndpoint())
                    .onSuccess { next ->
                        val isRadio = RadioQueueTrim.appliesTo(PlaylistType.RADIO, endpoint.playlistId)
                        var page = next
                        var items = page.items.forQueueMediaType(isRadio, mediaType)
                        val visited = mutableSetOf<String>()
                        while (items.isEmpty() && page.continuation?.let(visited::add) == true) {
                            page = youTube.next(endpoint.toWatchEndpoint(), continuation = page.continuation)
                                .getOrNull() ?: break
                            items = page.items.forQueueMediaType(isRadio, mediaType)
                        }
                        emit(Resource.Success(Pair(items.toListTrack(), page.continuation)))
                    }.onFailure {
                        it.printStackTrace()
                        emit(Resource.Error(it.message ?: "Error"))
                    }
            }
        }
}
