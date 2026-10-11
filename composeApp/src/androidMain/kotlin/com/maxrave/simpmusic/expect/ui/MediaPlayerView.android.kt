package com.maxrave.simpmusic.expect.ui

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import com.maxrave.domain.data.model.metadata.Lyrics
import com.maxrave.domain.data.model.streams.TimeLine
import com.maxrave.media3.ui.MediaPlayerView
import com.maxrave.media3.ui.MediaPlayerViewWithSubtitle
import com.maxrave.simpmusic.extension.findActivity
import com.maxrave.simpmusic.extension.getScreenSizeInfo
import com.maxrave.simpmusic.ui.theme.typo
import com.maxrave.simpmusic.ui.component.LocalPlayerVisualsActive

@Composable
actual fun MediaPlayerView(
    url: String,
    modifier: Modifier,
    cropToBounds: Boolean,
    useTextureView: Boolean,
    coverSurfaceUntilReady: Boolean,
    onFirstFrameRendered: (() -> Unit)?,
) {
    MediaPlayerView(
        modifier = modifier,
        context = LocalContext.current,
        density = LocalDensity.current,
        url = url,
        screenSize = getScreenSizeInfo(),
        cropToBounds = cropToBounds,
        useTextureView = useTextureView,
        coverSurfaceUntilReady = coverSurfaceUntilReady,
        onFirstFrameRendered = onFirstFrameRendered,
        isUiVisible = LocalPlayerVisualsActive.current,
    )
}

// Fully qualified: the media3-ui helper shares this function's name and signature.
@Composable
actual fun rememberVideoAspectRatio(playerName: String): Float? = com.maxrave.media3.ui.rememberVideoAspectRatio(playerName)

@Composable
actual fun MediaPlayerViewWithSubtitle(
    modifier: Modifier,
    playerName: String,
    shouldPip: Boolean,
    shouldShowSubtitle: Boolean,
    shouldScaleDownSubtitle: Boolean,
    isInPipMode: Boolean,
    timelineState: TimeLine,
    lyricsData: Lyrics?,
    translatedLyricsData: Lyrics?,
    mainTextStyle: TextStyle,
    translatedTextStyle: TextStyle,
    onVideoFrameColor: ((Color) -> Unit)?,
    freezeVideoFrameOnExit: Boolean,
    videoExitProgress: Float,
    videoCornerRadius: Dp,
    showAudioArtwork: Boolean,
    isUiVisible: Boolean,
    fitVideoToBounds: Boolean,
) {
    MediaPlayerViewWithSubtitle(
        playerName = playerName,
        modifier = modifier,
        shouldShowSubtitle = shouldShowSubtitle,
        shouldPip = shouldPip,
        shouldScaleDownSubtitle = shouldScaleDownSubtitle,
        timelineState = timelineState,
        lyricsData = lyricsData,
        translatedLyricsData = translatedLyricsData,
        context = LocalContext.current,
        activity = LocalActivity.current as? ComponentActivity ?: LocalContext.current.findActivity(),
        isInPipMode = isInPipMode,
        mainTextStyle = typo().bodyLarge,
        translatedTextStyle = typo().bodyMedium,
        onVideoFrameColor = onVideoFrameColor,
        freezeVideoFrameOnExit = freezeVideoFrameOnExit,
        videoExitProgress = videoExitProgress,
        videoCornerRadius = videoCornerRadius,
        showAudioArtwork = showAudioArtwork,
        isUiVisible = isUiVisible && LocalPlayerVisualsActive.current,
        fitVideoToBounds = fitVideoToBounds,
    )
}
