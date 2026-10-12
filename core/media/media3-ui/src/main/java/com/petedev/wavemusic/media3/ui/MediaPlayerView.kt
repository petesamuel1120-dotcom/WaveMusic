package com.petedev.wavemusic.media3.ui

import android.app.PictureInPictureParams
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Outline
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.PixelCopy
import android.view.SurfaceView
import android.view.SurfaceControl
import android.view.View
import android.view.ViewOutlineProvider
import android.window.SurfaceSyncGroup
import androidx.activity.ComponentActivity
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.FileDataSource
import androidx.media3.datasource.cache.CacheDataSink
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.compose.PlayerSurface
import androidx.media3.ui.compose.SURFACE_TYPE_SURFACE_VIEW
import androidx.media3.ui.compose.SURFACE_TYPE_TEXTURE_VIEW
import androidx.media3.ui.compose.modifiers.resizeWithContentScale
import androidx.media3.ui.compose.state.rememberPresentationState
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.petedev.wavemusic.common.Config
import com.petedev.wavemusic.domain.data.model.metadata.Lyrics
import com.petedev.wavemusic.domain.data.model.streams.TimeLine
import com.petedev.wavemusic.domain.data.model.ui.ScreenSizeInfo
import com.petedev.wavemusic.domain.manager.DataStoreManager
import com.petedev.wavemusic.logger.Logger
import com.petedev.wavemusic.media3.extension.isVideo
import com.petedev.wavemusic.common.MERGING_DATA_TYPE
import com.petedev.wavemusic.media3.ui.extension.KeepScreenOn
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.first
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.core.qualifier.named
import kotlin.math.roundToInt

private val RICH_SYNC_TIMESTAMP_REGEX = Regex("""<\d{2}:\d{2}\.\d{2,3}>\s*""")
private const val VIDEO_TRANSITION_CROSSFADE_MILLIS = 300
private const val VIDEO_ITEM_HANDOFF_MILLIS = 260

@Composable
private fun videoSurfaceBackgroundColor(): Color {
    val appBackground = MaterialTheme.colorScheme.background
    return if (appBackground.luminance() > 0.5f) appBackground else Color.Black
}

/** Calculates a small, brightness-weighted average from a PixelCopy frame. */
private fun sampleRenderedVideoColor(bitmap: Bitmap): Color? {
    val pixels = IntArray(bitmap.width * bitmap.height)
    bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

    var red = 0f
    var green = 0f
    var blue = 0f
    var totalWeight = 0f
    pixels.forEach { pixel ->
        val alpha = (pixel ushr 24 and 0xFF) / 255f
        val r = (pixel ushr 16 and 0xFF).toFloat()
        val g = (pixel ushr 8 and 0xFF).toFloat()
        val b = (pixel and 0xFF).toFloat()
        val brightest = maxOf(r, g, b)
        val darkest = minOf(r, g, b)
        if (alpha < 0.5f || brightest < 24f) return@forEach

        val saturation = if (brightest == 0f) 0f else (brightest - darkest) / brightest
        val brightness = brightest / 255f
        val weight = alpha * (0.28f + saturation * 0.72f) * (0.35f + brightness * 0.65f)
        red += r * weight
        green += g * weight
        blue += b * weight
        totalWeight += weight
    }

    if (totalWeight < 1f) return null
    return Color(
        red = (red / totalWeight / 255f).coerceIn(0f, 1f),
        green = (green / totalWeight / 255f).coerceIn(0f, 1f),
        blue = (blue / totalWeight / 255f).coerceIn(0f, 1f),
    )
}

/** A native surface starts hidden, before attaching a decoder that may already have a frame. */
@androidx.annotation.OptIn(UnstableApi::class)
@Composable
private fun HandoffPlayerSurface(
    player: Player,
    modifier: Modifier,
    surfaceAlpha: Float,
    canRevealSurface: () -> Boolean,
    onSurfaceAvailable: (SurfaceView) -> Unit,
) {
    var surface by remember { mutableStateOf<SurfaceView?>(null) }
    var surfaceSyncGroup: SurfaceSyncGroup? by remember { mutableStateOf(null) }
    val scope = rememberCoroutineScope()
    AndroidView(
        modifier = modifier,
        factory = { context ->
            object : SurfaceView(context) {
                override fun setAlpha(alpha: Float) {
                    super.setAlpha(alpha)
                    if (alpha > 0f && isAttachedToWindow &&
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                    ) {
                        val videoLayer = surfaceControl
                        if (videoLayer?.isValid != true) return
                        // The decoder handoff hides the native layer directly. This surface
                        // sits below the window, so View alpha animates its hole punch and
                        // never restores that layer's alpha. Restore the native layer with
                        // the revealing window frame; otherwise every subsequent video is black.
                        rootSurfaceControl?.applyTransactionOnDraw(
                            SurfaceControl.Transaction().setAlpha(videoLayer, 1f),
                        )
                        invalidate()
                    }
                }

                override fun dispatchDraw(canvas: Canvas) {
                    super.dispatchDraw(canvas)
                    if (Build.VERSION.SDK_INT == 34) {
                        surfaceSyncGroup?.markSyncReady()
                        surfaceSyncGroup = null
                    }
                }
            }.apply { alpha = 0f }
        },
        onReset = { it.alpha = 0f },
        update = { view ->
            // Apply the handoff gate in the AndroidView update, before its first draw.
            // Polling the view tree left the first decoded frame visible for one frame.
            view.alpha = if (canRevealSurface()) surfaceAlpha else 0f
            surface = view
            onSurfaceAvailable(view)
        },
    )
    surface?.let { view ->
        DisposableEffect(view, player) {
            // Preserve Media3 PlayerSurface's Android 14 surface-size synchronization.
            val listener = object : Player.Listener {
                override fun onSurfaceSizeChanged(width: Int, height: Int) {
                    if (Build.VERSION.SDK_INT == 34 && !Build.FINGERPRINT.equals("robolectric", ignoreCase = true)) {
                        scope.launch(Dispatchers.Main) {
                            view.rootSurfaceControl?.let { root ->
                                surfaceSyncGroup = SurfaceSyncGroup("wavemusic-video-handoff").apply {
                                    check(add(root) {})
                                }
                                view.invalidate()
                                root.applyTransactionOnDraw(SurfaceControl.Transaction())
                            }
                        }
                    }
                }
            }
            player.addListener(listener)
            if (player.isCommandAvailable(Player.COMMAND_SET_VIDEO_SURFACE)) {
                player.setVideoSurfaceView(view)
                view.tag = player
            }
            onDispose {
                player.removeListener(listener)
                if (player.isCommandAvailable(Player.COMMAND_SET_VIDEO_SURFACE)) {
                    player.clearVideoSurfaceView(view)
                }
                if (view.tag === player) view.tag = null
            }
        }
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun MediaPlayerView(
    modifier: Modifier = Modifier,
    context: Context,
    density: Density,
    url: String,
    screenSize: ScreenSizeInfo,
    cropToBounds: Boolean = false,
    useTextureView: Boolean = false,
    coverSurfaceUntilReady: Boolean = true,
    onFirstFrameRendered: (() -> Unit)? = null,
    isUiVisible: Boolean = true,
) {
    val canvasCache: SimpleCache = koinInject<SimpleCache>(named(Config.CANVAS_CACHE))

    var widthPx by rememberSaveable {
        mutableIntStateOf(screenSize.wPX)
    }

    var keepScreenOn by rememberSaveable {
        mutableStateOf(false)
    }
    val currentOnFirstFrameRendered by rememberUpdatedState(onFirstFrameRendered)
    val currentScreenHeightPx by rememberUpdatedState(screenSize.hPX)

    val playerListener =
        remember {
            object : Player.Listener {
                override fun onVideoSizeChanged(videoSize: VideoSize) {
                    super.onVideoSizeChanged(videoSize)
                    Logger.w("MediaPlayerView", "Video size changed: ${videoSize.width} / ${videoSize.height}")
                    if (videoSize.width != 0 && videoSize.height != 0) {
                        val h = (videoSize.width.toFloat() / videoSize.height) * currentScreenHeightPx
                        Logger.w("MediaPlayerView", "Calculated width: $h")
                        widthPx = h.roundToInt()
                    }
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    super.onIsPlayingChanged(isPlaying)
                    keepScreenOn = isPlaying
                }

                override fun onRenderedFirstFrame() {
                    currentOnFirstFrameRendered?.invoke()
                }
            }
        }

    // Initialize ExoPlayer
    val exoPlayer =
        remember {
            val cacheSink =
                CacheDataSink
                    .Factory()
                    .setCache(canvasCache)
            val upstreamFactory = DefaultDataSource.Factory(context, DefaultHttpDataSource.Factory())
            val downStreamFactory = FileDataSource.Factory()
            val cacheDataSourceFactory =
                CacheDataSource
                    .Factory()
                    .setCache(canvasCache)
                    .setCacheWriteDataSinkFactory(cacheSink)
                    .setCacheReadDataSourceFactory(downStreamFactory)
                    .setUpstreamDataSourceFactory(upstreamFactory)
                    .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
            ExoPlayer
                .Builder(context)
                .setLoadControl(
                    DefaultLoadControl
                        .Builder()
                        .setPrioritizeTimeOverSizeThresholds(false)
                        .build(),
                ).setMediaSourceFactory(
                    DefaultMediaSourceFactory(cacheDataSourceFactory),
                ).build()
                .apply {
                    addListener(playerListener)
                    videoScalingMode = C.VIDEO_SCALING_MODE_DEFAULT
                }
        }

    // Create a MediaSource
    val mediaSource =
        remember(url) {
            MediaItem.fromUri(url)
        }

    // Set MediaSource to ExoPlayer
    LaunchedEffect(mediaSource) {
        exoPlayer.setMediaItem(mediaSource)
        exoPlayer.repeatMode = Player.REPEAT_MODE_ONE
        exoPlayer.prepare()
        exoPlayer.playWhenReady = isUiVisible
    }

    // Retain the canvas decoder when collapsed, but stop decoding frames nobody can see.
    LaunchedEffect(exoPlayer, isUiVisible) {
        exoPlayer.playWhenReady = isUiVisible
    }

    // Manage lifecycle events
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.removeListener(playerListener)
            exoPlayer.release()
        }
    }

    if (keepScreenOn && isUiVisible) {
        KeepScreenOn()
    }

    val presentationState = rememberPresentationState(exoPlayer)
    if (cropToBounds) {
        // Center scale-to-cover (ContentScale.Crop) into whatever frame the caller gives us.
        // resizeWithContentScale keeps the true video aspect ratio (no stretch), scales it
        // to fully cover the frame using the real videoSizeDp, then clips the overflow.
        Box(modifier = modifier.graphicsLayer { clip = true }) {
            PlayerSurface(
                player = exoPlayer,
                surfaceType = if (useTextureView) SURFACE_TYPE_TEXTURE_VIEW else SURFACE_TYPE_SURFACE_VIEW,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .resizeWithContentScale(
                            contentScale = ContentScale.Crop,
                            sourceSizeDp = presentationState.videoSizeDp,
                        ),
            )

            if (coverSurfaceUntilReady && presentationState.coverSurface) {
                // Cover the surface that is being prepared with a shutter
                Box(Modifier.matchParentSize().background(videoSurfaceBackgroundColor()))
            }
        }
    } else {
        Box(modifier = modifier.graphicsLayer { clip = true }) {
            PlayerSurface(
                player = exoPlayer,
                surfaceType = if (useTextureView) SURFACE_TYPE_TEXTURE_VIEW else SURFACE_TYPE_SURFACE_VIEW,
                modifier =
                    Modifier
                        .fillMaxHeight()
                        .width(with(density) { widthPx.toDp() })
                        .align(Alignment.Center),
            )

            if (presentationState.coverSurface) {
                // Cover the surface that is being prepared with a shutter
                Box(Modifier.background(videoSurfaceBackgroundColor()))
            }
        }
    }
}

/**
 * Aspect ratio (width / height) of the video [playerName] is showing, or null while it has no
 * video size. From [rememberPresentationState], which reads the player's current size before it
 * listens, so a frame composed mid-video gets the right shape at once — the same reason the
 * surface below takes its own ratio from there.
 */
@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun rememberVideoAspectRatio(playerName: String): Float? {
    val player: Player = koinInject(named(playerName))
    val size = rememberPresentationState(player).videoSizeDp ?: return null
    return size.width / size.height
}

@Composable
@androidx.annotation.OptIn(UnstableApi::class)
fun MediaPlayerViewWithSubtitle(
    modifier: Modifier = Modifier,
    context: Context,
    activity: ComponentActivity,
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
    showAudioArtwork: Boolean = true,
    isUiVisible: Boolean = true,
    fitVideoToBounds: Boolean = false,
    hasVideoHandoffCover: Boolean = false,
    onVideoFrameReady: ((String) -> Unit)? = null,
    onVideoFrameSnapshot: ((String, ImageBitmap) -> Unit)? = null,
) {
    val player: Player = koinInject(named(playerName))
    val videoBackgroundColor = videoSurfaceBackgroundColor()

    // Subtitles over the video are lyrics like any other, so they take the same audio-delay
    // correction the lyrics sheet does. timelineState is used for nothing else in this composable,
    // but it is subtracted at each read rather than up front so the parameter keeps meaning "where
    // the player is".
    val lyricsOffsetDataStore = koinInject<DataStoreManager>()

    var shouldEnterPipMode by rememberSaveable {
        mutableStateOf(false)
    }

    var showArtwork by remember(player) { mutableStateOf(!hasVideoTrack(player.currentTracks)) }
    var isVideoItem by remember(player) { mutableStateOf(player.currentMediaItem?.isVideo() == true) }
    val latestVideoFrameColorCallback by rememberUpdatedState(onVideoFrameColor)
    val latestFreezeVideoFrameOnExit by rememberUpdatedState(freezeVideoFrameOnExit)
    val latestVideoExitProgress by rememberUpdatedState(videoExitProgress)
    val latestVideoFrameReadyCallback by rememberUpdatedState(onVideoFrameReady)
    val latestVideoFrameSnapshotCallback by rememberUpdatedState(onVideoFrameSnapshot)
    var videoFrameGeneration by remember(player) { mutableIntStateOf(0) }
    var nativeVideoSurface by remember(player) { mutableStateOf<SurfaceView?>(null) }
    var latestVideoFrame by remember(player) { mutableStateOf<ImageBitmap?>(null) }
    var latestVideoFrameMediaItem by remember(player) { mutableStateOf<MediaItem?>(null) }
    // A delegate swap can make the new item's first-frame callback arrive before the forwarding
    // player delivers onMediaItemTransition. Keep that item identity so the transition callback
    // can consume the already-rendered frame instead of holding the outgoing frame until timeout.
    var latestRenderedFirstFrameMediaItem by remember(player) { mutableStateOf<MediaItem?>(null) }
    var observedMediaItem by remember(player) { mutableStateOf(player.currentMediaItem) }
    val lyricsSongId = observedMediaItem?.mediaId?.removePrefix(MERGING_DATA_TYPE.VIDEO)
    val lyricsOffsetFlow = remember(lyricsOffsetDataStore, lyricsSongId) { lyricsOffsetDataStore.getEffectiveLyricsOffsetMs(lyricsSongId) }
    val lyricsOffsetMs by lyricsOffsetFlow.collectAsState(0)
    var frozenVideoFrame by remember(player) { mutableStateOf<ImageBitmap?>(null) }
    // PixelCopy returns the decoded buffer without the SurfaceView's display alpha.
    // Preserve that alpha when a skip interrupts an incoming video's fade.
    var presentedVideoFrameAlpha by remember(player) { mutableStateOf(0f) }
    var presentedFrozenFrameAlpha by remember(player) { mutableStateOf(1f) }
    var frozenVideoFrameBaseAlpha by remember(player) { mutableStateOf(1f) }
    var videoItemHandoff by remember(player) { mutableStateOf(false) }
    var frozenHandoffBaseProgress by remember(player) { mutableStateOf(0f) }
    var presentedFrozenHandoffProgress by remember(player) { mutableStateOf(0f) }
    var frozenVideoArtworkUri by remember(player) { mutableStateOf<String?>(null) }
    var fadeFrozenVideoFrame by remember(player) { mutableStateOf(false) }
    var awaitingVideoFirstFrame by remember(player) { mutableStateOf(player.currentMediaItem?.isVideo() == true) }
    var hideSurfaceUntilVideoFrame by remember(player) { mutableStateOf(player.currentMediaItem?.isVideo() == true) }

    var artworkUri by remember(player) {
        mutableStateOf(player.currentMediaItem?.mediaMetadata?.artworkUri?.toString())
    }

    var currentLineIndex by rememberSaveable {
        mutableIntStateOf(-1)
    }
    var currentTranslatedLineIndex by rememberSaveable {
        mutableIntStateOf(-1)
    }

    LaunchedEffect(key1 = timelineState, key2 = lyricsOffsetMs) {
        val lines = lyricsData?.lines ?: return@LaunchedEffect
        val translatedLines = translatedLyricsData?.lines
        // What the ear is hearing right now, rather than where the player is.
        val nowMs = timelineState.current - lyricsOffsetMs
        if (nowMs > 0L) {
            lines.indices.forEach { i ->
                val sentence = lines[i]
                val startTimeMs = sentence.startTimeMs.toLong()

                // estimate the end time of the current sentence based on the start time of the next sentence
                val endTimeMs =
                    if (i < lines.size - 1) {
                        lines[i + 1].startTimeMs.toLong()
                    } else {
                        // if this is the last sentence, set the end time to be some default value (e.g., 1 minute after the start time)
                        startTimeMs + 60000
                    }
                if (nowMs in startTimeMs..endTimeMs) {
                    currentLineIndex = i
                }
            }
            translatedLines?.indices?.forEach { i ->
                val sentence = translatedLines[i]
                val startTimeMs = sentence.startTimeMs.toLong()

                // estimate the end time of the current sentence based on the start time of the next sentence
                val endTimeMs =
                    if (i < translatedLines.size - 1) {
                        translatedLines[i + 1].startTimeMs.toLong()
                    } else {
                        // if this is the last sentence, set the end time to be some default value (e.g., 1 minute after the start time)
                        startTimeMs + 60000
                    }
                if (nowMs in startTimeMs..endTimeMs) {
                    currentTranslatedLineIndex = i
                }
            }
            if (lines.isNotEmpty() &&
                (
                    nowMs in (
                        0..(
                            lines.getOrNull(0)?.startTimeMs
                                ?: "0"
                        ).toLong()
                    )
                )
            ) {
                currentLineIndex = -1
                currentTranslatedLineIndex = -1
            }
        } else {
            currentLineIndex = -1
            currentTranslatedLineIndex = -1
        }
    }

    val playerListener =
        remember(player) {
            object : Player.Listener {
                override fun onMediaItemTransition(
                    mediaItem: MediaItem?,
                    reason: Int,
                ) {
                    super.onMediaItemTransition(mediaItem, reason)
                    val nextItemIsVideo = mediaItem?.isVideo() == true
                    val incomingFrameAlreadyRendered =
                        nextItemIsVideo && latestRenderedFirstFrameMediaItem == mediaItem
                    // A SurfaceView can retain the outgoing buffer while the new item buffers.
                    // Hide it synchronously here: waiting for Compose's next SideEffect can leave
                    // one compositor frame showing the outgoing video after the item changed.
                    // Audio has no incoming video frame. Hide its retained surface too, so it
                    // cannot brighten the fading frozen image by drawing the same frame twice.
                    hideSurfaceUntilVideoFrame = !incomingFrameAlreadyRendered
                    // Readiness never preserves the previous generation's visible alpha.
                    // The incoming surface must start its own fade after the outgoing still.
                    nativeVideoSurface?.alpha = 0f
                    val outgoingFrame = latestVideoFrame.takeIf {
                        latestVideoFrameMediaItem == observedMediaItem && presentedVideoFrameAlpha > 0f
                    }
                    videoItemHandoff = isVideoItem && nextItemIsVideo &&
                        latestFreezeVideoFrameOnExit && fitVideoToBounds && !showAudioArtwork
                    frozenHandoffBaseProgress =
                        if (outgoingFrame != null) 0f else presentedFrozenHandoffProgress
                    if (isVideoItem && latestFreezeVideoFrameOnExit) {
                        // Hold only a decoded frame during video-to-video switches. An artwork
                        // fallback would briefly flash a thumbnail over the incoming video.
                        frozenVideoFrame = outgoingFrame ?: frozenVideoFrame.takeIf { nextItemIsVideo }
                        frozenVideoFrameBaseAlpha =
                            if (outgoingFrame != null) presentedVideoFrameAlpha else presentedFrozenFrameAlpha
                        frozenVideoArtworkUri =
                            if (outgoingFrame == null && !nextItemIsVideo) artworkUri else null
                    } else {
                        frozenVideoFrame = null
                        frozenVideoArtworkUri = null
                    }
                    fadeFrozenVideoFrame = false
                    observedMediaItem = mediaItem
                    latestVideoFrame = null
                    latestVideoFrameMediaItem = null
                    videoFrameGeneration += 1
                    artworkUri = mediaItem?.mediaMetadata?.artworkUri?.toString()
                    isVideoItem = nextItemIsVideo
                    awaitingVideoFirstFrame = nextItemIsVideo && !incomingFrameAlreadyRendered
                    fadeFrozenVideoFrame =
                        incomingFrameAlreadyRendered &&
                            (frozenVideoFrame != null || frozenVideoArtworkUri != null)
                    latestRenderedFirstFrameMediaItem = null
                    // onMediaItemTransition can arrive before the new item's tracks. Use the
                    // item's declared type here instead of the outgoing tracks; onTracksChanged
                    // will refine the state once Media3 publishes them.
                    showArtwork = !nextItemIsVideo
                }

                override fun onTracksChanged(tracks: Tracks) {
                    super.onTracksChanged(tracks)
                    showArtwork = !hasVideoTrack(tracks)
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    super.onIsPlayingChanged(isPlaying)
                    shouldEnterPipMode = isPlaying && shouldPip
                }

                override fun onRenderedFirstFrame() {
                    val renderedItem = player.currentMediaItem
                    if (renderedItem?.isVideo() == true) {
                        if (observedMediaItem == renderedItem) {
                            latestRenderedFirstFrameMediaItem = null
                            fadeFrozenVideoFrame = frozenVideoFrame != null || frozenVideoArtworkUri != null
                            awaitingVideoFirstFrame = false
                            hideSurfaceUntilVideoFrame = false
                        } else {
                            latestRenderedFirstFrameMediaItem = renderedItem
                            hideSurfaceUntilVideoFrame = true
                            nativeVideoSurface?.alpha = 0f
                        }
                    }
                }
            }
        }

    DisposableEffect(player) {
        shouldEnterPipMode = shouldPip
        val currentItem = player.currentMediaItem
        if (observedMediaItem != currentItem) {
            observedMediaItem = currentItem
            isVideoItem = currentItem?.isVideo() == true
            awaitingVideoFirstFrame = isVideoItem
            hideSurfaceUntilVideoFrame = isVideoItem
            artworkUri = currentItem?.mediaMetadata?.artworkUri?.toString()
            frozenVideoFrame = null
            frozenVideoArtworkUri = null
            fadeFrozenVideoFrame = false
            videoItemHandoff = false
        }
        player.addListener(playerListener)
        showArtwork = !hasVideoTrack(player.currentTracks)
        playerListener.onTracksChanged(player.currentTracks)
        onDispose {
            shouldEnterPipMode = false
            player.removeListener(playerListener)
            Logger.w("MediaPlayerView", "Disposing ExoPlayer")
            if (shouldPip && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val builder = PictureInPictureParams.Builder()

                // Add autoEnterEnabled for versions S and up
                builder.setAutoEnterEnabled(false)
                activity.setPictureInPictureParams(builder.build())
            }
        }
    }
    LaunchedEffect(player) {
        (player as? ExoPlayer)?.videoScalingMode = C.VIDEO_SCALING_MODE_DEFAULT
    }

    // Reveal the incoming surface only when its decoded frame and presentation are ready.
    // Video item handoffs dismiss their still promptly; other callers retain the first-frame cover.
    val frozenFrameAlpha = remember(videoFrameGeneration) { Animatable(1f) }
    val outgoingVideoDismissed = !videoItemHandoff || frozenFrameAlpha.value == 0f
    val frozenHandoffProgress = if (videoItemHandoff) {
        frozenHandoffBaseProgress + (1f - frozenHandoffBaseProgress) * (1f - frozenFrameAlpha.value)
    } else 0f
    LaunchedEffect(videoFrameGeneration, videoItemHandoff) {
        if (!videoItemHandoff) return@LaunchedEffect
        val generation = videoFrameGeneration
        // Start moving at the skip, even if Previous needs a new decoder. The outgoing
        // scene leaves promptly; an empty black viewport owns any remaining buffering.
        if (frozenVideoFrame != null) {
            frozenFrameAlpha.animateTo(0f, tween(VIDEO_ITEM_HANDOFF_MILLIS, easing = FastOutSlowInEasing))
        } else {
            frozenFrameAlpha.snapTo(0f)
        }
        if (generation == videoFrameGeneration) {
            frozenVideoFrame = null
            frozenVideoArtworkUri = null
            fadeFrozenVideoFrame = false
        }
    }
    val presentationState = rememberPresentationState(player)
    val incomingVideoReady =
        isVideoItem && !awaitingVideoFirstFrame && !showArtwork && !presentationState.coverSurface
    val videoAspectRatio = presentationState.videoSizeDp
        ?.let { it.width / it.height }
        ?.takeIf { it.isFinite() && it > 0f }
    var lastReadyVideoAspectRatio by remember(player) { mutableStateOf(videoAspectRatio ?: 16f / 9) }
    val displayedVideoAspectRatio =
        if (incomingVideoReady) videoAspectRatio ?: lastReadyVideoAspectRatio else lastReadyVideoAspectRatio
    val incomingVideoSurfaceAlpha = remember(player, videoFrameGeneration) {
        // A cached decoder may report its first frame before this generation's
        // first composition. Readiness permits the fade; it must never bypass it.
        Animatable(0f)
    }
    LaunchedEffect(videoFrameGeneration, incomingVideoReady, outgoingVideoDismissed) {
        if (incomingVideoReady && outgoingVideoDismissed) {
            if (videoItemHandoff) {
                // Native SurfaceView and the copied scene never overlap during this handoff.
                // Their different compositor paths otherwise produce a bright/ghosted blend.
                incomingVideoSurfaceAlpha.animateTo(
                    1f, tween(VIDEO_ITEM_HANDOFF_MILLIS, easing = FastOutSlowInEasing),
                )
            } else if (hasVideoHandoffCover || frozenVideoFrame != null || frozenVideoArtworkUri != null) {
                // Put an opaque live frame UNDER the outgoing still. Fading both layers loses
                // brightness at the midpoint and looks like a flash, even at a steady frame rate.
                incomingVideoSurfaceAlpha.snapTo(1f)
            } else {
                incomingVideoSurfaceAlpha.animateTo(
                    1f,
                    tween(VIDEO_TRANSITION_CROSSFADE_MILLIS, easing = FastOutSlowInEasing),
                )
            }
        } else {
            // Rapid skips must not inherit the previous video's partially completed fade.
            incomingVideoSurfaceAlpha.snapTo(0f)
        }
    }
    LaunchedEffect(videoFrameGeneration, fadeFrozenVideoFrame, incomingVideoReady, isVideoItem) {
        if (videoItemHandoff) return@LaunchedEffect
        val transitionGeneration = videoFrameGeneration
        if (frozenVideoFrame == null && frozenVideoArtworkUri == null) return@LaunchedEffect
        if (isVideoItem) {
            // Do not discard the cover on a timer while the next video is still buffering.
            if (!fadeFrozenVideoFrame || !incomingVideoReady) return@LaunchedEffect
            frozenFrameAlpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(
                    durationMillis = VIDEO_TRANSITION_CROSSFADE_MILLIS,
                    easing = FastOutSlowInEasing,
                ),
            )
        } else {
            if (showAudioArtwork) {
                delay(420L)
            } else {
                // The surrounding song image may still be loading. Retain the decoded frame
                // until that caller has actually finished its exit, however long loading took.
                snapshotFlow { latestVideoExitProgress }.first { it >= 1f }
            }
        }
        if (transitionGeneration == videoFrameGeneration) {
            frozenVideoFrame = null
            frozenVideoArtworkUri = null
            fadeFrozenVideoFrame = false
        }
    }
    val videoExitAlpha = (1f - videoExitProgress).coerceIn(0f, 1f)

    val videoSurfaceAlpha = incomingVideoSurfaceAlpha.value * videoExitAlpha
    val composedVideoGeneration = videoFrameGeneration
    val composedVideoItem = observedMediaItem
    // A decoder swap can occur before Compose applies this update. Check the live
    // identity and generation at the write, including rapid A -> B -> A skips.
    val canRevealVideoSurface = {
        composedVideoGeneration == videoFrameGeneration &&
            composedVideoItem == player.currentMediaItem &&
            !hideSurfaceUntilVideoFrame && isVideoItem
    }
    SideEffect {
        presentedVideoFrameAlpha =
            if (canRevealVideoSurface()) incomingVideoSurfaceAlpha.value else 0f
        presentedFrozenFrameAlpha = frozenVideoFrameBaseAlpha * frozenFrameAlpha.value
        presentedFrozenHandoffProgress = frozenHandoffProgress
        if (incomingVideoReady && videoAspectRatio != null) {
            lastReadyVideoAspectRatio = videoAspectRatio
        }
        nativeVideoSurface?.let { surface ->
            surface.alpha = if (canRevealVideoSurface()) videoSurfaceAlpha else 0f
        }
        // Repeat only an already-ready identity when the caller's track snapshot catches up.
        // A one-shot effect can fire before that snapshot and lose the readiness handoff.
        if (incomingVideoReady && (videoItemHandoff || incomingVideoSurfaceAlpha.value == 1f)) {
            observedMediaItem?.mediaId?.removePrefix(MERGING_DATA_TYPE.VIDEO)?.let {
                latestVideoFrameReadyCallback?.invoke(it)
            }
        }
    }
    DisposableEffect(player, nativeVideoSurface) {
        val attachedSurface = nativeVideoSurface
        onDispose {
            // Restore the view this effect owned, not a replacement found meanwhile.
            attachedSurface?.apply {
                alpha = 1f
                scaleX = 1f
                scaleY = 1f
            }
        }
    }

    // SurfaceView renders in its own compositor layer; give that layer the same rounded
    // outline as the Compose frame without changing the playback surface or scaling.
    val videoCornerRadiusPx = with(LocalDensity.current) { videoCornerRadius.toPx() }
    DisposableEffect(nativeVideoSurface, videoCornerRadiusPx) {
        val surface = nativeVideoSurface
        if (surface != null && videoCornerRadiusPx > 0f) {
            val previousProvider = surface.outlineProvider
            val previousClip = surface.clipToOutline
            val roundedOutline = object : ViewOutlineProvider() {
                override fun getOutline(view: View, outline: Outline) {
                    outline.setRoundRect(0, 0, view.width, view.height, videoCornerRadiusPx)
                }
            }
            surface.outlineProvider = roundedOutline
            surface.clipToOutline = true
            surface.invalidateOutline()
            onDispose {
                if (surface.outlineProvider === roundedOutline) {
                    surface.outlineProvider = previousProvider
                    surface.clipToOutline = previousClip
                    surface.invalidateOutline()
                }
            }
        } else {
            onDispose { }
        }
    }

    // Sample a tiny copy of the already-rendered SurfaceView through PixelCopy. This leaves the
    // decoder and SurfaceView path untouched; only the ambient surround gets the sampled color.
    // Sampling is deliberately sparse and is enabled only by a visible caller that requests it.
    LaunchedEffect(
        player,
        nativeVideoSurface,
        onVideoFrameColor != null,
        videoFrameGeneration,
        isUiVisible,
    ) {
        if (!isUiVisible || onVideoFrameColor == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return@LaunchedEffect
        }
        val samplingJob = currentCoroutineContext()[Job]
        val mainHandler = Handler(Looper.getMainLooper())
        var requestInFlight = false
        var capturedFirstFrame = false
        while (isActive) {
            val surface = nativeVideoSurface
            if (player.currentMediaItem?.isVideo() == true &&
                player.isPlaying &&
                !awaitingVideoFirstFrame &&
                !showArtwork &&
                !presentationState.coverSurface &&
                surface?.isAttachedToWindow == true &&
                surface.isShown &&
                surface.holder.surface.isValid &&
                !requestInFlight
            ) {
                requestInFlight = true
                val sampledMediaItem = player.currentMediaItem
                val sampledGeneration = videoFrameGeneration
                val sampleBitmap = Bitmap.createBitmap(24, 14, Bitmap.Config.ARGB_8888)
                try {
                    PixelCopy.request(
                        surface,
                        sampleBitmap,
                        { result ->
                            try {
                                // A copy can finish after a skip. Ignore the outgoing request and
                                // wait for the new item's decoded surface before changing the glow.
                                if (samplingJob?.isActive == true && result == PixelCopy.SUCCESS &&
                                    sampledGeneration == videoFrameGeneration &&
                                    sampledMediaItem == player.currentMediaItem &&
                                    !awaitingVideoFirstFrame &&
                                    !showArtwork && !presentationState.coverSurface
                                ) {
                                    sampleRenderedVideoColor(sampleBitmap)?.let { color ->
                                        capturedFirstFrame = true
                                        latestVideoFrameColorCallback?.invoke(color)
                                    }
                                }
                            } finally {
                                sampleBitmap.recycle()
                                requestInFlight = false
                            }
                        },
                        mainHandler,
                    )
                } catch (_: IllegalArgumentException) {
                    sampleBitmap.recycle()
                    requestInFlight = false
                } catch (_: IllegalStateException) {
                    sampleBitmap.recycle()
                    requestInFlight = false
                }
            }
            delay(
                when {
                    surface == null || !surface.isAttachedToWindow -> 100L
                    capturedFirstFrame -> 1_000L
                    else -> 250L
                },
            )
        }
    }

    // Keep a modest-resolution copy of the most recent decoded frame so a still-mounted feed can
    // bridge the short period between a video item transition and the parent page update. Do not
    // sample until this item's first frame callback arrives: SurfaceView can still contain the
    // outgoing item's buffer while the shutter is covering it.

    LaunchedEffect(player, nativeVideoSurface, freezeVideoFrameOnExit, videoFrameGeneration, isUiVisible) {
        if (!isUiVisible || !freezeVideoFrameOnExit || Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return@LaunchedEffect
        }
        val samplingJob = currentCoroutineContext()[Job]
        val mainHandler = Handler(Looper.getMainLooper())
        var requestInFlight = false
        while (isActive) {
            val surface = nativeVideoSurface
            if (player.currentMediaItem?.isVideo() == true &&
                !awaitingVideoFirstFrame &&
                !showArtwork &&
                !presentationState.coverSurface &&
                surface?.isAttachedToWindow == true &&
                surface.isShown &&
                surface.holder.surface.isValid &&
                !requestInFlight
            ) {
                val sourceWidth = surface.width
                val sourceHeight = surface.height
                if (sourceWidth > 0 && sourceHeight > 0) {
                    requestInFlight = true
                    val sampledMediaItem = player.currentMediaItem
                    val sampledGeneration = videoFrameGeneration
                    val scale = minOf(1f, 720f / maxOf(sourceWidth, sourceHeight))
                    val frameBitmap = Bitmap.createBitmap(
                        (sourceWidth * scale).roundToInt().coerceAtLeast(1),
                        (sourceHeight * scale).roundToInt().coerceAtLeast(1),
                        Bitmap.Config.ARGB_8888,
                    )
                    try {
                        PixelCopy.request(
                            surface,
                            frameBitmap,
                            { result ->
                                try {
                                    if (samplingJob?.isActive == true && result == PixelCopy.SUCCESS &&
                                        sampledGeneration == videoFrameGeneration &&
                                        sampledMediaItem == player.currentMediaItem &&
                                        player.currentMediaItem?.isVideo() == true &&
                                        !awaitingVideoFirstFrame &&
                                        !showArtwork && !presentationState.coverSurface
                                    ) {
                                        latestVideoFrame = frameBitmap.asImageBitmap()
                                        latestVideoFrameMediaItem = sampledMediaItem
                                        sampledMediaItem?.mediaId?.removePrefix(MERGING_DATA_TYPE.VIDEO)?.let { id ->
                                            latestVideoFrame?.let { latestVideoFrameSnapshotCallback?.invoke(id, it) }
                                        }
                                    } else {
                                        frameBitmap.recycle()
                                    }
                                } finally {
                                    requestInFlight = false
                                }
                            },
                            mainHandler,
                        )
                    } catch (_: IllegalArgumentException) {
                        frameBitmap.recycle()
                        requestInFlight = false
                    } catch (_: IllegalStateException) {
                        frameBitmap.recycle()
                        requestInFlight = false
                    }
                }
            }
            // A moving video's handoff image should not jump back a third of a second.
            delay(if (player.isPlaying && fitVideoToBounds && !showAudioArtwork) 100L else 350L)
        }
    }

    LaunchedEffect(shouldEnterPipMode) {
        Logger.w("MediaPlayerView", "shouldEnterPipMode: $shouldEnterPipMode")
    }

    if (shouldPip && Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
        val currentShouldEnterPipMode by rememberUpdatedState(newValue = shouldEnterPipMode)
        DisposableEffect(context) {
            val onUserLeaveBehavior =
                Runnable {
                    if (currentShouldEnterPipMode) {
                        activity.enterPictureInPictureMode(PictureInPictureParams.Builder().build())
                    }
                }
            activity.addOnUserLeaveHintListener(
                onUserLeaveBehavior,
            )
            onDispose {
                activity.removeOnUserLeaveHintListener(
                    onUserLeaveBehavior,
                )
            }
        }
    }

    Box(
        modifier =
            modifier
                .background(
                    if (isVideoItem) videoBackgroundColor.copy(alpha = videoExitAlpha) else Color.Transparent,
                )
                .then(
                    if (shouldPip && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        Modifier.onGloballyPositioned { layoutCoordinates ->
                            val builder = PictureInPictureParams.Builder()

                            // Add autoEnterEnabled for versions S and up
                            builder.setAutoEnterEnabled(shouldEnterPipMode)
                            activity?.setPictureInPictureParams(builder.build())
                        }
                    } else {
                        Modifier
                    },
                ),
        contentAlignment = Alignment.Center,
    ) {
        if (isUiVisible) KeepScreenOn()
        // Keep one native surface attached while Media3 changes/clears tracks during a skip.
        // Crossfading this branch recreated SurfaceView and briefly promoted the outgoing poster.
        val videoSurfaceModifier =
            if (fitVideoToBounds) {
                // Size the incoming surface under the first-frame cover, before its fade starts.
                // The viewport and frozen outgoing frame keep their bounds across aspect changes.
                Modifier
                    .resizeWithContentScale(ContentScale.Fit, presentationState.videoSizeDp)
                    .align(Alignment.Center)
            } else {
                Modifier
                    .wrapContentSize()
                    .aspectRatio(displayedVideoAspectRatio)
                    .align(Alignment.Center)
            }
        HandoffPlayerSurface(
            player = player,
            modifier = videoSurfaceModifier,
            surfaceAlpha = if (hideSurfaceUntilVideoFrame || !isVideoItem) 0f else videoSurfaceAlpha,
            canRevealSurface = canRevealVideoSurface,
            onSurfaceAvailable = { nativeVideoSurface = it },
        )
        if (showArtwork || presentationState.coverSurface || (isVideoItem && awaitingVideoFirstFrame)) {
            Box(
                Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = videoExitAlpha }
                    .background(videoBackgroundColor),
            )
        }
        if (showAudioArtwork && showArtwork && !isVideoItem) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(artworkUri)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .diskCacheKey(artworkUri)
                    .crossfade(250)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.FillHeight,
                modifier = Modifier.fillMaxHeight().align(Alignment.Center),
            )
        }
        frozenVideoFrame?.let { frame ->
            // Round the fitted image itself, including corners inside the viewport's black
            // bars. Clipping only the outer viewport leaves PixelCopy's square buffer visible.
            BoxWithConstraints(Modifier.fillMaxSize()) {
                val frameRatio = frame.width.toFloat() / frame.height
                val frameWidth = minOf(maxWidth, maxHeight * frameRatio)
                Image(
                    bitmap = frame,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(frameWidth, frameWidth / frameRatio)
                        .align(Alignment.Center)
                        .graphicsLayer {
                            alpha = videoExitAlpha * frozenVideoFrameBaseAlpha * frozenFrameAlpha.value
                            scaleX = 1f - 0.045f * frozenHandoffProgress
                            scaleY = 1f - 0.045f * frozenHandoffProgress
                            shape = RoundedCornerShape(videoCornerRadius)
                            clip = videoCornerRadius > 0.dp
                        }
                        .then(
                            if (videoItemHandoff) {
                                Modifier.blur((10f * frozenHandoffProgress).dp, BlurredEdgeTreatment.Rectangle)
                            } else Modifier,
                        ),
                )
            }
        } ?: frozenVideoArtworkUri?.let { outgoingArtwork ->
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(outgoingArtwork)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .diskCacheKey(outgoingArtwork)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().graphicsLayer { alpha = videoExitAlpha * frozenFrameAlpha.value },
            )
        }
        if (lyricsData != null && shouldShowSubtitle) {
            Crossfade(
                currentLineIndex != -1,
                modifier =
                    Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxSize(),
            ) {
                val lines = lyricsData.lines ?: return@Crossfade
                if (it) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .padding(bottom = if (isInPipMode || shouldScaleDownSubtitle) 10.dp else 40.dp)
                            .align(Alignment.BottomCenter),
                        contentAlignment = Alignment.BottomCenter,
                    ) {
                        Box(Modifier.fillMaxWidth(0.7f)) {
                            Column(
                                Modifier.align(Alignment.BottomCenter),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(
                                    text =
                                        lines
                                            .getOrNull(currentLineIndex)
                                            ?.words
                                            ?.replace(RICH_SYNC_TIMESTAMP_REGEX, "")
                                            ?.trim()
                                            ?: return@Crossfade,
                                    style =
                                        mainTextStyle
                                            .let {
                                                if (isInPipMode || shouldScaleDownSubtitle) {
                                                    it.copy(fontSize = it.fontSize * 0.8f)
                                                } else {
                                                    it
                                                }
                                            },
                                    color = Color.White,
                                    textAlign = TextAlign.Center,
                                    modifier =
                                        Modifier
                                            .padding(4.dp)
                                            .background(Color.Black.copy(alpha = 0.5f))
                                            .wrapContentWidth(),
                                )
                                Crossfade(translatedLyricsData?.lines != null, label = "") { translate ->
                                    val translateLines = translatedLyricsData?.lines ?: return@Crossfade
                                    if (translate) {
                                        Text(
                                            text =
                                                translateLines
                                                    .getOrNull(
                                                        currentTranslatedLineIndex,
                                                    )?.words
                                                    ?.replace(RICH_SYNC_TIMESTAMP_REGEX, "")
                                                    ?.trim()
                                                    ?: return@Crossfade,
                                            style =
                                                translatedTextStyle.let {
                                                    if (isInPipMode || shouldScaleDownSubtitle) {
                                                        it.copy(fontSize = it.fontSize * 0.8f)
                                                    } else {
                                                        it
                                                    }
                                                },
                                            color = Color.Yellow,
                                            textAlign = TextAlign.Center,
                                            modifier =
                                                Modifier
                                                    .background(Color.Black.copy(alpha = 0.5f))
                                                    .wrapContentWidth(),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun hasVideoTrack(tracks: Tracks): Boolean =
    tracks.groups.any { group ->
        (0 until group.length).any { trackIndex ->
            group.getTrackFormat(trackIndex).sampleMimeType?.startsWith("video/") == true
        }
    }
