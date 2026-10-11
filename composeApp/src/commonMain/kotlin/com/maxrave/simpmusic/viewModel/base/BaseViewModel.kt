package com.maxrave.simpmusic.viewModel.base

import com.maxrave.domain.mediaservice.handler.PlaylistType
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maxrave.common.Config.VIDEO_CLICK
import com.maxrave.domain.data.model.browse.album.Track
import com.maxrave.domain.mediaservice.handler.MediaPlayerHandler
import com.maxrave.domain.mediaservice.handler.QueueData
import com.maxrave.logger.LogLevel
import com.maxrave.logger.Logger
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import multiplatform.network.cmptoast.ToastDuration
import multiplatform.network.cmptoast.ToastGravity
import multiplatform.network.cmptoast.showToast
import org.jetbrains.compose.resources.StringResource
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import simpmusic.composeapp.generated.resources.Res
import simpmusic.composeapp.generated.resources.loading
import simpmusic.composeapp.generated.resources.already_in_playlist
import simpmusic.composeapp.generated.resources.added_to_named_playlist
import com.maxrave.domain.utils.PLAYLIST_ITEM_ALREADY_PRESENT

abstract class BaseViewModel :
    ViewModel(),
    KoinComponent {
    protected val mediaPlayerHandler: MediaPlayerHandler by inject<MediaPlayerHandler>()
    private val _nowPlayingVideoId: MutableStateFlow<String> = MutableStateFlow("")

    /**
     * Get now playing video id
     * If empty, no video is playing
     */
    val nowPlayingVideoId: StateFlow<String> get() = _nowPlayingVideoId

    /**
     * Tag for logging
     */
    protected val tag: String = this::class.simpleName ?: "BaseViewModel"

    /**
     * Log with viewModel tag
     */
    protected fun log(
        message: String,
        logType: LogLevel = LogLevel.WARN,
    ) {
        when (logType) {
            LogLevel.DEBUG -> Logger.d(tag, message)
            LogLevel.INFO -> Logger.i(tag, message)
            LogLevel.WARN -> Logger.w(tag, message)
            LogLevel.ERROR -> Logger.e(tag, message)
        }
    }

    /**
     * Cancel all jobs
     */
    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
        log("ViewModel cleared", LogLevel.WARN)
    }

    init {
        getNowPlayingVideoId()
    }

    fun makeToast(message: String?) {
        showToast(
            message = message ?: "NO MESSAGE",
            duration = ToastDuration.Short,
            gravity = ToastGravity.Bottom,
        )
    }

    protected fun playlistAdditionSuccessMessage(playlistName: String): String =
        runBlocking { org.jetbrains.compose.resources.getString(Res.string.added_to_named_playlist, playlistName) }

    protected fun playlistAdditionErrorMessage(message: String): String =
        if (message == PLAYLIST_ITEM_ALREADY_PRESENT) getString(Res.string.already_in_playlist) else message

    protected fun getString(resId: StringResource): String =
        runBlocking {
            org.jetbrains.compose.resources
                .getString(resId)
        }

    // Loading dialog
    // This is hidden initially. Avoid resolving a Compose resource synchronously for every
    // screen ViewModel created on the first frame; showLoadingDialog() resolves the localized
    // string when a loading dialog is actually requested.
    private val _showLoadingDialog: MutableStateFlow<Pair<Boolean, String>> = MutableStateFlow(false to "Loading…")
    val showLoadingDialog: StateFlow<Pair<Boolean, String>> get() = _showLoadingDialog

    fun showLoadingDialog(message: String? = null) {
        _showLoadingDialog.value = true to (message ?: getString(Res.string.loading))
    }

    fun hideLoadingDialog() {
        _showLoadingDialog.value = false to "Loading…"
    }

    private fun getNowPlayingVideoId() {
        viewModelScope.launch {
            // MainActivity connects the media service 300 ms after onStart so the first Home
            // frame can settle. Resolving MediaPlayerHandler here used to construct an idle
            // ExoPlayer synchronously during ViewModel construction, before setContent could draw.
            // Match the existing service startup window instead of pulling the player graph into
            // every screen ViewModel's first composition.
            delay(300)
            combine(mediaPlayerHandler.nowPlayingState, mediaPlayerHandler.controlState) { nowPlayingState, controlState ->
                Pair(nowPlayingState, controlState)
            }.collect { (nowPlayingState, controlState) ->
                if (controlState.isPlaying) {
                    _nowPlayingVideoId.value = nowPlayingState.songEntity?.videoId ?: ""
                } else {
                    _nowPlayingVideoId.value = ""
                }
            }
        }
    }

    /**
     * Communicate with SimpleMediaServiceHandler to load media item
     */
    fun setQueueData(queueData: QueueData.Data) {
        mediaPlayerHandler.reset()
        mediaPlayerHandler.setQueueData(queueData)
    }

    /** Start a video radio queue, separate from song playback. */
    fun playVideo(track: Track) {
        val videoTrack = track.copy(resultType = "Video")
        // The clicked video is the seed for both the player and its video-only recommendations.
        setQueueData(
            QueueData.Data(
                listTracks = arrayListOf(videoTrack),
                firstPlayedTrack = videoTrack,
                playlistId = "RDAMVM${videoTrack.videoId}",
                playlistName = videoTrack.title,
                playlistType = PlaylistType.RADIO,
            ),
        )
        loadMediaItem(videoTrack, VIDEO_CLICK)
    }

    fun <T> loadMediaItem(
        anyTrack: T,
        type: String,
        index: Int? = null,
    ) {
        viewModelScope.launch {
            mediaPlayerHandler.loadMediaItem(
                anyTrack = anyTrack,
                type = type,
                index = index,
            )
        }
    }

    fun shufflePlaylist(firstPlayIndex: Int = 0) {
        mediaPlayerHandler.shufflePlaylist(firstPlayIndex)
    }
}
