package com.petedev.wavemusic.expect.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.petedev.wavemusic.domain.data.model.metadata.Lyrics
import com.petedev.wavemusic.domain.data.model.streams.TimeLine

@Composable
expect fun MediaPlayerView(
    url: String,
    modifier: Modifier,
    // When true, the video center scales-to-cover the given frame (ContentScale.Crop)
    // and clips the overflow. Default false keeps the legacy fit-height-by-screen
    // behavior used by NowPlaying / Fullscreen.
    cropToBounds: Boolean = false,
    // TextureView participates in Compose alpha/blur effects; normal video keeps SurfaceView.
    useTextureView: Boolean = false,
    coverSurfaceUntilReady: Boolean = true,
    // Called when ExoPlayer has submitted its first decoded video frame to the view.
    onFirstFrameRendered: (() -> Unit)? = null,
)

@Composable
expect fun MediaPlayerViewWithSubtitle(
    modifier: Modifier,
    playerName: String,
    shouldPip: Boolean = false,
    shouldShowSubtitle: Boolean,
    shouldScaleDownSubtitle: Boolean = false,
    isInPipMode: Boolean,
    timelineState: TimeLine,
    lyricsData: Lyrics? = null,
    translatedLyricsData: Lyrics? = null,
    mainTextStyle: TextStyle,
    translatedTextStyle: TextStyle,
    onVideoFrameColor: ((Color) -> Unit)? = null,
    freezeVideoFrameOnExit: Boolean = false,
    videoExitProgress: Float = 0f,
    videoCornerRadius: Dp = 0.dp,
    // Disable when the surrounding player owns the song artwork during video-to-audio changes.
    showAudioArtwork: Boolean = true,
    // Keep the playback surface attached while suspending hidden visual work on Android.
    isUiVisible: Boolean = true,
    // Fit the video inside a fixed viewport, including while the next item buffers.
    fitVideoToBounds: Boolean = false,
    // A caller-owned still covers decoder startup and performs the single incoming dissolve.
    hasVideoHandoffCover: Boolean = false,
    // Reports the identity whose decoded frame is ready, never a timer-based approximation.
    onVideoFrameReady: ((String) -> Unit)? = null,
    // A decoded still lets the caller animate geometry without moving the native surface.
    onVideoFrameSnapshot: ((String, ImageBitmap) -> Unit)? = null,
)

/**
 * Aspect ratio (width / height) of the video [playerName] is showing, or null until the player
 * knows it. For laying out the frame around the video, so the frame takes the video's own shape
 * instead of assuming 16:9.
 */
@Composable
expect fun rememberVideoAspectRatio(playerName: String): Float?
