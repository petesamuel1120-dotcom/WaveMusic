package com.petedev.wavemusic.viewModel

import androidx.lifecycle.viewModelScope
import com.petedev.wavemusic.common.Config
import com.petedev.wavemusic.domain.data.entities.ArtistEntity
import com.petedev.wavemusic.domain.data.entities.SongEntity
import com.petedev.wavemusic.domain.data.model.browse.album.Track
import com.petedev.wavemusic.domain.data.model.browse.artist.Albums
import com.petedev.wavemusic.domain.data.model.browse.artist.ArtistBrowse
import com.petedev.wavemusic.domain.data.model.browse.artist.ArtistLogo
import com.petedev.wavemusic.domain.data.model.browse.artist.Related
import com.petedev.wavemusic.domain.data.model.browse.artist.ResultPlaylist
import com.petedev.wavemusic.domain.data.model.browse.artist.Singles
import com.petedev.wavemusic.domain.data.model.streams.YouTubeWatchEndpoint
import com.petedev.wavemusic.domain.extension.now
import com.petedev.wavemusic.domain.mediaservice.handler.PlaylistType
import com.petedev.wavemusic.domain.mediaservice.handler.QueueData
import com.petedev.wavemusic.domain.repository.ArtistRepository
import com.petedev.wavemusic.domain.repository.LyricsCanvasRepository
import com.petedev.wavemusic.domain.repository.SongRepository
import com.petedev.wavemusic.domain.utils.LocalResource
import com.petedev.wavemusic.domain.utils.Resource
import com.petedev.wavemusic.extension.toArtistScreenData
import com.petedev.wavemusic.viewModel.ArtistScreenState.Error
import com.petedev.wavemusic.viewModel.ArtistScreenState.Loading
import com.petedev.wavemusic.viewModel.ArtistScreenState.Success
import com.petedev.wavemusic.viewModel.base.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import com.petedev.wavemusic.resources.Res
import com.petedev.wavemusic.resources.radio
import com.petedev.wavemusic.resources.shuffle
import com.petedev.wavemusic.resources.sync_follow_failed
import com.petedev.wavemusic.resources.subscribed_on_youtube
import com.petedev.wavemusic.resources.unsubscribed_on_youtube
import org.jetbrains.compose.resources.getString

class ArtistViewModel(
    private val artistRepository: ArtistRepository,
    private val songRepository: SongRepository,
    private val lyricsCanvasRepository: LyricsCanvasRepository,
) : BaseViewModel() {
    // It is dynamic and can be changed by the user, so separate it from the ArtistScreenData
    private var _canvasUrl: MutableStateFlow<Pair<String, SongEntity>?> = MutableStateFlow(null)
    var canvasUrl: StateFlow<Pair<String, SongEntity>?> = _canvasUrl

    // Artist name-logo image + accent color from the hidden catalog (immersive header).
    // A pending lookup must not be mistaken for an artist without a logo.
    private val _artistLogo: MutableStateFlow<LocalResource<ArtistLogo>> = MutableStateFlow(LocalResource.Loading())
    val artistLogo: StateFlow<LocalResource<ArtistLogo>> = _artistLogo
    private var artistBrowseJob: Job? = null
    private var artistDetailsJob: Job? = null

    // How many liked songs credit this artist — the "Liked songs" row above Popular (issue #2524)
    // shows only while this is above zero. Observed, so liking a song from the page updates it.
    private val _likedSongCount: MutableStateFlow<Int> = MutableStateFlow(0)
    val likedSongCount: StateFlow<Int> = _likedSongCount
    private var likedSongsJob: Job? = null

    private var _followed: MutableStateFlow<Boolean> = MutableStateFlow(false)
    var followed: StateFlow<Boolean> = _followed

    private val _artistScreenState: MutableStateFlow<ArtistScreenState> = MutableStateFlow(Loading)
    val artistScreenState: StateFlow<ArtistScreenState> = _artistScreenState

    fun browseArtist(channelId: String) {
        artistBrowseJob?.cancel()
        artistDetailsJob?.cancel()
        _artistScreenState.value = Loading
        _canvasUrl.value = null
        _artistLogo.value = LocalResource.Loading()
        _followed.value = false
        _likedSongCount.value = 0
        likedSongsJob?.cancel()
        likedSongsJob =
            viewModelScope.launch {
                songRepository.getLikedSongsByArtist(channelId).collect { _likedSongCount.value = it.size }
            }
        artistBrowseJob = viewModelScope.launch {
            artistRepository.getArtistData(channelId).collect { browse ->
                val data = browse.data
                when (browse) {
                    is Resource.Success if (data != null) -> {
                        insertArtist(
                            ArtistEntity(
                                data.channelId ?: channelId,
                                data.name,
                                data.thumbnails
                                    ?.lastOrNull()
                                    ?.url,
                            ),
                        )
                        _artistScreenState.value =
                            Success(data.toArtistScreenData())
                        // Canvas comes ONLY from the single most-popular song: take the first
                        // popular result and use its canvas if it has one. If it doesn't,
                        // leave canvas null (already reset above) — no fallback to other songs.
                        data.songs?.results?.firstOrNull()?.let { topSong ->
                            val entity = songRepository.getSongById(topSong.videoId).firstOrNull()
                            val canvasUrl = entity?.canvasUrl
                            if (entity != null && canvasUrl != null) {
                                _canvasUrl.value = Pair(canvasUrl, entity)
                                log("CanvasUrl: $canvasUrl")
                            }
                        }
                    }

                    is Resource.Error ->
                        _artistScreenState.value = Error(browse.message ?: "Error")

                    else -> {
                        _artistScreenState.value = Error("Error")
                    }
                }
            }
        }
    }

    private suspend fun fetchAndCacheArtistLogo(
        channelId: String,
        artistName: String,
    ) {
        val result = lyricsCanvasRepository.getArtistLogo(artistName).firstOrNull()
        val logo = if (result is Resource.Success) result.data else null
        if (logo != null) {
            _artistLogo.value = LocalResource.Success(logo)
            artistRepository.updateArtistNameLogo(channelId, logo.logoUrl, logo.bgColorHex)
        } else {
            _artistLogo.value = LocalResource.Error(result?.message ?: "No artist logo")
        }
    }

    private fun insertArtist(artist: ArtistEntity) {
        artistDetailsJob?.cancel()
        artistDetailsJob = viewModelScope.launch {
            try {
                artistRepository.insertArtist(artist)
                artistRepository.updateArtistInLibrary(now(), artist.channelId)
                val artistEntity = artistRepository.getArtistById(artist.channelId).firstOrNull()
                if (artistEntity != null) {
                    artist.thumbnails?.let {
                        artistRepository.updateArtistImage(artistEntity.channelId, it)
                    }
                    _followed.value = artistEntity.followed
                    log("insertArtist: ${artistEntity.followed}")
                }
                // Resolve the title representation once, after checking the local cache.
                val cachedLogoUrl = artistEntity?.nameLogoUrl?.takeIf { it.isNotBlank() }
                if (cachedLogoUrl != null) {
                    _artistLogo.value =
                        LocalResource.Success(
                            ArtistLogo(
                                logoUrl = cachedLogoUrl,
                                bgColorHex = artistEntity.nameLogoColor,
                                width = 0,
                                height = 0,
                            ),
                        )
                } else {
                    fetchAndCacheArtistLogo(artist.channelId, artist.name)
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                // A cache write failure must not replace an already resolved logo with text.
                if (_artistLogo.value is LocalResource.Loading) {
                    _artistLogo.value = LocalResource.Error(error.message ?: "Failed to load artist logo")
                }
            }
        }
    }

    fun updateFollowed(
        followed: Int,
        channelId: String,
    ) {
        viewModelScope.launch {
            _followed.value = (followed == 1)
            // Both outcomes are reported; only null stays quiet, because that means mirroring
            // is switched off and nothing was attempted. The local follow above stands either
            // way — these toasts speak for the account, not for the follow itself.
            val synced = artistRepository.updateFollowedStatus(channelId, followed)
            when (synced) {
                true ->
                    makeToast(
                        getString(
                            if (followed == 1) {
                                Res.string.subscribed_on_youtube
                            } else {
                                Res.string.unsubscribed_on_youtube
                            },
                        ),
                    )

                false -> makeToast(getString(Res.string.sync_follow_failed))
                null -> Unit
            }
            log("updateFollowed: ${_followed.value}, synced: $synced")
        }
    }

    fun onRadioClick(endpoint: YouTubeWatchEndpoint) {
        viewModelScope.launch {
            songRepository.getRadioFromEndpoint(endpoint).collectLatest { res ->
                val data = res.data
                when (res) {
                    is Resource.Success if data != null && data.first.isNotEmpty() -> {
                        setQueueData(
                            QueueData.Data(
                                listTracks = data.first,
                                firstPlayedTrack = data.first.first(),
                                playlistId = endpoint.playlistId,
                                playlistName = "\"${artistScreenState.value.data.title}\" ${getString(Res.string.radio)}",
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
                        makeToast(res.message)
                    }
                }
            }
        }
    }

    fun onShuffleClick(endpoint: YouTubeWatchEndpoint) {
        viewModelScope.launch {
            songRepository.getRadioFromEndpoint(endpoint).collectLatest { res ->
                val data = res.data
                when (res) {
                    is Resource.Success if data != null && data.first.isNotEmpty() -> {
                        setQueueData(
                            QueueData.Data(
                                listTracks = data.first,
                                firstPlayedTrack = data.first.first(),
                                playlistId = endpoint.playlistId,
                                playlistName = "\"${artistScreenState.value.data.title}\" ${getString(Res.string.shuffle)}",
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
                        makeToast(res.message)
                    }
                }
            }
        }
    }
}

data class ArtistScreenData(
    val title: String? = null,
    val imageUrl: String? = null,
    val subscribers: String? = null,
    val playCount: String? = null,
    val isChannel: Boolean = false,
    val channelId: String? = null,
    val radioParam: YouTubeWatchEndpoint? = null,
    val shuffleParam: YouTubeWatchEndpoint? = null,
    val description: String? = null,
    val listSongParam: String? = null,
    val popularSongs: List<Track> = emptyList(),
    val singles: Singles? = null,
    val albums: Albums? = null,
    val video: ArtistBrowse.Videos? = null,
    val videoListParam: String? = null,
    val videoListBrowseParams: String? = null,
    val related: Related? = null,
    val featuredOn: List<ResultPlaylist> = emptyList(),
)

sealed class ArtistScreenState(
    val data: ArtistScreenData = ArtistScreenData(),
    val message: String? = null,
) {
    data object Loading : ArtistScreenState()

    class Success(
        data: ArtistScreenData,
    ) : ArtistScreenState(data)

    class Error(
        message: String,
    ) : ArtistScreenState(message = message)
}
