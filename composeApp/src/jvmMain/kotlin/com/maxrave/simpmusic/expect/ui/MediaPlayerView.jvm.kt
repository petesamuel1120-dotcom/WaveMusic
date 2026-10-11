package com.maxrave.simpmusic.expect.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import com.maxrave.domain.data.model.metadata.Lyrics
import com.maxrave.domain.data.model.streams.TimeLine
import com.maxrave.media_jvm_ui.ui.MediaPlayerViewWithSubtitleJvm
import com.maxrave.media_jvm_ui.ui.MediaPlayerViewWithUrl
import com.maxrave.media_jvm_ui.ui.rememberVideoAspectRatioJvm
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
    MediaPlayerViewWithUrl(
        url = url,
        modifier = modifier,
        cropToBounds = cropToBounds,
        coverSurfaceUntilReady = coverSurfaceUntilReady,
        onFirstFrameRendered = onFirstFrameRendered,
    )
}

@Composable
actual fun rememberVideoAspectRatio(playerName: String): Float? = rememberVideoAspectRatioJvm()

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
    // Desktop frames are drawn by Compose, so hidden views can release their UI subscriptions.
    if (!isUiVisible || !LocalPlayerVisualsActive.current) return
    // mpv already fits and letterboxes its frames to the supplied viewport.
    MediaPlayerViewWithSubtitleJvm(
        playerName = playerName,
        modifier = modifier,
        shouldShowSubtitle = shouldShowSubtitle,
        shouldScaleDownSubtitle = shouldScaleDownSubtitle,
        timelineState = timelineState,
        lyricsData = lyricsData,
        translatedLyricsData = translatedLyricsData,
        mainTextStyle = typo().bodyLarge,
        translatedTextStyle = typo().bodyMedium,
        showAudioArtwork = showAudioArtwork,
    )
}
