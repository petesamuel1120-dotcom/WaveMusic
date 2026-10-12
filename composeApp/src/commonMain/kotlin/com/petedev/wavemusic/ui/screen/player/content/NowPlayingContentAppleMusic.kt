package com.petedev.wavemusic.ui.screen.player.content

import org.jetbrains.compose.resources.stringResource
import com.petedev.wavemusic.resources.playback_toggle
import com.petedev.wavemusic.resources.Res

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.focusable

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.kyant.backdrop.highlight.Highlight
import com.petedev.wavemusic.common.Config.MAIN_PLAYER
import com.petedev.wavemusic.Platform
import com.petedev.wavemusic.expect.ui.DeviceVolumeController
import com.petedev.wavemusic.expect.ui.MediaPlayerView
import com.petedev.wavemusic.expect.ui.MediaPlayerViewWithSubtitle
import com.petedev.wavemusic.expect.ui.PlatformBackdrop
import com.petedev.wavemusic.expect.ui.layerBackdrop
import com.petedev.wavemusic.expect.ui.rememberBackdrop
import com.petedev.wavemusic.expect.ui.rememberDeviceVolumeController
import com.petedev.wavemusic.expect.ui.toImageBitmap
import com.petedev.wavemusic.extension.getScreenSizeInfo
import com.petedev.wavemusic.extension.smoothScrimBrush
import com.petedev.wavemusic.getPlatform
import com.petedev.wavemusic.ui.component.ExplicitBadge
import com.petedev.wavemusic.ui.component.LiquidGlassIconButton
import com.petedev.wavemusic.ui.component.rememberHolderPainter
import com.petedev.wavemusic.ui.component.videoPlaybackBackgroundColor
import com.petedev.wavemusic.ui.screen.player.content.applemusic.AppleMusicBottomCluster
import com.petedev.wavemusic.ui.screen.player.content.applemusic.AppleMusicCompactHeader
import com.petedev.wavemusic.ui.screen.player.content.applemusic.AppleMusicHeaderActions
import com.petedev.wavemusic.ui.screen.player.content.applemusic.AppleMusicLyricsView
import com.petedev.wavemusic.ui.screen.player.content.applemusic.AppleMusicLandscapePlayer
import com.petedev.wavemusic.ui.screen.player.content.applemusic.AppleMusicLandscapeVideoArea
import com.petedev.wavemusic.ui.screen.player.content.applemusic.APPLE_MUSIC_LYRICS_CONTENT_EXTENSION
import com.petedev.wavemusic.ui.screen.player.content.applemusic.AppleMusicQueueView
import com.petedev.wavemusic.ui.screen.player.content.applemusic.AppleMusicTypography
import com.petedev.wavemusic.ui.icon.Forward5
import com.petedev.wavemusic.ui.icon.Fullscreen
import com.petedev.wavemusic.ui.icon.Pause
import com.petedev.wavemusic.ui.icon.PlayArrow
import com.petedev.wavemusic.ui.icon.Replay5
import com.petedev.wavemusic.ui.icon.WaveIcons
import com.petedev.wavemusic.ui.icon.Subtitles
import com.petedev.wavemusic.ui.icon.SubtitlesOff
import com.petedev.wavemusic.ui.screen.player.content.applemusic.AppleMusicView
import com.petedev.wavemusic.ui.screen.player.content.applemusic.appleMusicGradientColorAt
import com.petedev.wavemusic.ui.screen.player.content.applemusic.appleMusicVerticalFadeEdges
import com.petedev.wavemusic.ui.screen.player.content.applemusic.appleMusicSongColorContinuation
import com.petedev.wavemusic.ui.screen.player.content.applemusic.rememberAppleMusicTypography
import com.petedev.wavemusic.ui.theme.seed
import com.petedev.wavemusic.ui.theme.LocalReduceVisualEffects
import com.petedev.wavemusic.ui.theme.typo
import com.petedev.wavemusic.viewModel.SharedViewModel
import com.petedev.wavemusic.viewModel.UIEvent
import com.petedev.wavemusic.viewModel.NowPlayingScreenData
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.koin.compose.koinInject

/** Returns metadata artwork only when the screen-data snapshot belongs to the active track. */
private fun NowPlayingContentState.currentTrackArtworkUrl(): String? {
    val trackId = currentTrackId
    return screenData.thumbnailURL?.takeIf {
        !trackId.isNullOrBlank() && screenData.currentVideoId == trackId && it.isNotBlank()
    }
}

/** Stable identity for what the artwork pane actually shows, independent of queue index changes. */
private fun NowPlayingContentState.currentArtworkVisualIdentity(): String {
    val trackId = currentTrackId.orEmpty()
    if (isCurrentVideoItem()) return "media-video:$trackId"

    val currentCanvas = screenData.canvasData?.takeIf {
        currentTrackId != null &&
            screenData.currentVideoId == currentTrackId &&
            (it.videoId == null || it.videoId == currentTrackId)
    }
    if (currentCanvas != null) return "canvas:${currentCanvas.url}"

    // Queue thumbnails are available as soon as the player confirms the new item, while screen
    // metadata can arrive later. Use that stable source so the same album art does not transition
    // once at the queue change and again when metadata catches up.
    val queuedArtworkUrl =
        artworkQueue
            .getOrNull(currentOrderIndex)
            ?.takeIf { it.videoId == currentTrackId }
            ?.thumbnails
            ?.maxByOrNull { it.width * it.height }
            ?.url
            ?.requestLargePlayerArtwork()
    val artworkUrl = queuedArtworkUrl ?: currentTrackArtworkUrl()
    return artworkUrl?.let { "artwork:$it" } ?: "track:$trackId"
}

/**
 * The Apple Music Now Playing style: a style-internal dock (Lyrics · Cast · Queue) swaps the
 * BODY between three full-screen views instead of scrolling a single column, à la
 * [NowPlayingContentSpotify]/[NowPlayingContentM3Expressive]. The page background is the
 * artwork's own tint (a vertical gradient), not a Material scheme — see the color-system note on
 * [AppleMusicMainView] for the canvas/video exception.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NowPlayingContentAppleMusic(
    state: NowPlayingContentState,
    actions: NowPlayingContentActions,
) {
    // Seeded from the view model, not from MAIN: this player lives in a ModalBottomSheet, so
    // dismissing it disposes the tree and rememberSaveable dies with it. rememberSaveable is still
    // the right holder WITHIN a session (it survives rotation without a round trip), the view model
    // just supplies where the user was last.
    val sharedViewModel: SharedViewModel = koinInject()
    var selectedViewState by rememberSaveable {
        mutableStateOf(
            sharedViewModel.lastPlayerViewTab.value
                ?.let { saved -> AppleMusicView.entries.firstOrNull { it.name == saved } }
                ?: AppleMusicView.MAIN,
        )
    }
    val viewState = if (!state.lyricsEnabled && selectedViewState == AppleMusicView.LYRICS) {
        AppleMusicView.MAIN
    } else selectedViewState
    LaunchedEffect(state.lyricsEnabled, selectedViewState) {
        if (!state.lyricsEnabled && selectedViewState == AppleMusicView.LYRICS) selectedViewState = AppleMusicView.MAIN
    }
    var showLyricsCluster by rememberSaveable { mutableStateOf(true) }
    LaunchedEffect(viewState) { sharedViewModel.setLastPlayerViewTab(viewState.name) }

    // This style has no scroll and no collapsed toolbar (unlike Classic/M3E); park the shared
    // toolbar-visibility flag at false so a style switch mid-session can't leave it stuck shown.
    LaunchedEffect(Unit) {
        actions.onToolbarVisibilityChange(false)
    }

    // Use the media player's track identity, not the pager index: the pager can still point at
    // the outgoing page for a frame after a queue skip. Never let that stale page supply artwork
    // for the newly active track.
    val currentArtworkTrackId = state.currentTrackId
    val currentTrackArtworkUrl = state.currentTrackArtworkUrl()
    val currentArtworkVisualIdentity = state.currentArtworkVisualIdentity()
    val displayCanvasData =
        state.screenData.canvasData?.takeIf {
            currentArtworkTrackId != null &&
                state.screenData.currentVideoId == currentArtworkTrackId &&
                (it.videoId == null || it.videoId == currentArtworkTrackId)
        }
    // Keep the outgoing animated cover alive while a new animated canvas prepares. Its surface
    // remains visible until the incoming surface renders its first frame, then both dissolve
    // together so the new song's static thumbnail never flashes between animated covers.
    var transitionCanvasVideo by remember {
        mutableStateOf(displayCanvasData?.takeIf { it.isVideo })
    }
    var outgoingCanvasVideo by remember { mutableStateOf<NowPlayingScreenData.CanvasData?>(null) }
    // The polarity tells the artwork layer whether the still-running video dissolve is moving
    // toward its current incoming layer or back toward the outgoing one. Toggling it alongside
    // swapping the layer identities makes the rendered opacity continuous on that exact frame.
    var canvasVideoProgressReversed by remember { mutableStateOf(false) }
    val renderedCanvasFrames = remember { mutableStateMapOf<String, Boolean>() }
    var lastObservedArtworkTrackId by remember { mutableStateOf(currentArtworkTrackId) }
    var lastArtworkTrackChangeNanos by remember { mutableLongStateOf(0L) }
    var isRapidSongChange by remember { mutableStateOf(false) }
    var canvasWasResolvedAtTrackStart by remember { mutableStateOf(state.screenData.canvasResolved) }
    var animatedToAnimatedTrackId by remember { mutableStateOf<String?>(null) }
    var previewOnlyCanvasVideoUrl by remember { mutableStateOf<String?>(null) }
    val canvasTrackChangeProgress = remember { Animatable(1f) }
    val songHandoffBlur = remember { Animatable(0f) }
    var incomingStillArtworkLoaded by remember(currentArtworkTrackId, currentTrackArtworkUrl) {
        mutableStateOf(false)
    }
    val animatedToAnimatedHandoffBlur = remember { Animatable(0f) }
    var animatedToAnimatedBlurActive by remember { mutableStateOf(false) }
    var lastBlurredArtworkTrackId by remember { mutableStateOf(currentArtworkTrackId) }
    var lastBlurredArtworkVisualIdentity by remember { mutableStateOf(currentArtworkVisualIdentity) }
    val animatedToAnimatedHandoffStarting =
        transitionCanvasVideo?.isVideo == true &&
            displayCanvasData?.isVideo == true &&
            transitionCanvasVideo?.url != displayCanvasData?.url
    val reduceVisualEffects = LocalReduceVisualEffects.current
    val paletteColor = state.startColor.value
    val seedColor = if (reduceVisualEffects || paletteColor == Color.Black) seed else paletteColor
    // startColor is already an Animatable driven by the artwork palette in NowPlayingScreen.
    // Wrapping its per-frame values in a second animateColorAsState continually restarted that
    // animation, so the page tint lagged behind the cover and the bottom colour looked detached.
    // Use the single shared palette ramp directly; this does not change any cover handoff timing.
    val displayedSeedColor = seedColor
    // Hold the outgoing glow while the next video buffers or the next song artwork loads.
    // Resetting this per track made the surround animate to black between valid frame samples.
    val videoFrameAmbientColor = remember {
        mutableStateOf<Color?>(null)
    }
    val reportVideoFrameColor: (Color) -> Unit = remember(videoFrameAmbientColor) {
        { color -> videoFrameAmbientColor.value = color }
    }
    val activePillContainer = remember(seedColor) { lerp(seedColor, Color.White, 0.75f) }
    val activePillContent = remember(seedColor) { lerp(seedColor, Color.Black, 0.6f) }
    val handoffBlurPeak = SONG_HANDOFF_BLUR_RADIUS_DP * if (animatedToAnimatedHandoffStarting) 1.5f else 1f
    val visibleSongHandoffBlurRadius =
        if (animatedToAnimatedBlurActive ||
            (animatedToAnimatedHandoffStarting && currentArtworkTrackId != lastBlurredArtworkTrackId)
        ) {
            maxOf(
                songHandoffBlur.value,
                animatedToAnimatedHandoffBlur.value,
                if (animatedToAnimatedHandoffStarting && currentArtworkTrackId != lastBlurredArtworkTrackId) {
                    ANIMATED_HANDOFF_PREROLL_BLUR_DP
                } else {
                    0f
                },
            )
        } else {
            songHandoffBlur.value
    }
    val canvasTrackChangeAlpha = remember {
        derivedStateOf { canvasTrackChangeProgress.value }
    }
    LaunchedEffect(currentArtworkTrackId, currentArtworkVisualIdentity) {
        if (currentArtworkTrackId == lastBlurredArtworkTrackId) {
            // Metadata may fill in after the player's track id. Keep the active visual identity
            // current without starting a second handoff for the same item.
            lastBlurredArtworkVisualIdentity = currentArtworkVisualIdentity
            if (currentArtworkVisualIdentity.startsWith("media-video:")) {
                animatedToAnimatedBlurActive = false
                animatedToAnimatedHandoffBlur.snapTo(0f)
                songHandoffBlur.snapTo(0f)
            }
        }
    }
    LaunchedEffect(currentArtworkTrackId) {
        if (currentArtworkTrackId != lastBlurredArtworkTrackId) {
            val previousVisualIdentity = lastBlurredArtworkVisualIdentity
            val artworkVisuallyChanged = currentArtworkVisualIdentity != previousVisualIdentity
            val mediaVideoOwnsHandoff =
                previousVisualIdentity.startsWith("media-video:") ||
                    currentArtworkVisualIdentity.startsWith("media-video:")
            val hasVideoHandoff = transitionCanvasVideo?.isVideo == true || displayCanvasData?.isVideo == true
            lastBlurredArtworkTrackId = currentArtworkTrackId
            lastBlurredArtworkVisualIdentity = currentArtworkVisualIdentity
            if (!artworkVisuallyChanged || mediaVideoOwnsHandoff) {
                // Same-cover track changes should only animate metadata. Real media-video swaps
                // have their own held-frame dissolve, so a second page blur makes them look muddy.
                animatedToAnimatedBlurActive = false
                animatedToAnimatedHandoffBlur.snapTo(0f)
                songHandoffBlur.snapTo(0f)
                return@LaunchedEffect
            }
            val handoffDuration =
                if (hasVideoHandoff) {
                    if (isRapidSongChange) 180 else 240
                } else if (isRapidSongChange) {
                    220
                } else {
                    420
                }
            // Keep the established blur animation for static and mixed handoffs unchanged.
            // A→A gets a pre-roll blur and keeps it active until the video dissolve completes.
            val isAnimatedToAnimatedHandoff =
                transitionCanvasVideo?.isVideo == true &&
                    displayCanvasData?.isVideo == true &&
                    transitionCanvasVideo?.url != displayCanvasData?.url
            if (isAnimatedToAnimatedHandoff) {
                animatedToAnimatedBlurActive = true
                if (animatedToAnimatedHandoffBlur.value < ANIMATED_HANDOFF_PREROLL_BLUR_DP) {
                    animatedToAnimatedHandoffBlur.snapTo(ANIMATED_HANDOFF_PREROLL_BLUR_DP)
                }
                if (songHandoffBlur.value > 0f) {
                    songHandoffBlur.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(durationMillis = 180, easing = LinearEasing),
                    )
                }
                animatedToAnimatedHandoffBlur.animateTo(
                    targetValue = SONG_HANDOFF_BLUR_RADIUS_DP * 1.5f,
                    animationSpec = tween(durationMillis = 360, easing = LinearEasing),
                )
            } else {
                // A→A blur belongs only to an A→A handoff. Clear it before any other transition
                // so static and mixed handoffs retain their existing appearance.
                animatedToAnimatedBlurActive = false
                animatedToAnimatedHandoffBlur.snapTo(0f)
                val blurInDuration = 70
                songHandoffBlur.animateTo(
                    targetValue = handoffBlurPeak,
                    animationSpec = tween(durationMillis = blurInDuration, easing = LinearEasing),
                )
                if (!hasVideoHandoff) {
                    // Keep the transition soft until Coil has decoded the incoming cover. A fixed
                    // timer exposed the unready artwork and made its final palette haze pop away.
                    withTimeoutOrNull(1_800) {
                        snapshotFlow { incomingStillArtworkLoaded }.first { it }
                    }
                    songHandoffBlur.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(durationMillis = 620, easing = FastOutSlowInEasing),
                    )
                } else {
                    songHandoffBlur.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(
                            durationMillis = (handoffDuration - blurInDuration).coerceAtLeast(80),
                            easing = LinearEasing,
                        ),
                    )
                }
            }
        }
    }
    LaunchedEffect(currentArtworkTrackId, displayCanvasData?.url, state.screenData.canvasResolved, outgoingCanvasVideo?.url) {
        var rapidTrackChange = isRapidSongChange
        val isAnimatedToAnimatedHandoff =
            transitionCanvasVideo?.isVideo == true &&
                displayCanvasData?.isVideo == true &&
                transitionCanvasVideo?.url != displayCanvasData?.url
        if (isAnimatedToAnimatedHandoff) {
            animatedToAnimatedTrackId = currentArtworkTrackId
        }
        if (currentArtworkTrackId != lastObservedArtworkTrackId) {
            val nowNanos = withFrameNanos { it }
            rapidTrackChange =
                lastArtworkTrackChangeNanos != 0L && nowNanos - lastArtworkTrackChangeNanos < 650_000_000L
            lastObservedArtworkTrackId = currentArtworkTrackId
            lastArtworkTrackChangeNanos = nowNanos
            isRapidSongChange = rapidTrackChange
            // canvasResolved is already true on a cache hit. Keep the initial per-track value
            // after the async lookup completes so only first-time covers get the delayed reveal.
            canvasWasResolvedAtTrackStart = state.screenData.canvasResolved
            if (!isAnimatedToAnimatedHandoff) animatedToAnimatedTrackId = null
        }
        val nextVideo = displayCanvasData?.takeIf { it.isVideo }
        if (previewOnlyCanvasVideoUrl != nextVideo?.url) previewOnlyCanvasVideoUrl = null
        // Start handing off as soon as the player confirms a new track, even if its canvas lookup
        // is still running. Holding the outgoing video until that lookup finished added hundreds
        // of milliseconds specifically to animated -> static switches. First-time animation can
        // still appear after its short uncached hold once lookup resolves.
        if (transitionCanvasVideo?.url == nextVideo?.url) return@LaunchedEffect

        // If the user quickly returns to the still-playing outgoing cover, reverse the same
        // two live surfaces. Recreating the incoming player/alpha here reset the animation and
        // briefly blended the cover with the wrong track's palette.
        if (outgoingCanvasVideo != null && nextVideo?.url == outgoingCanvasVideo?.url) {
            val displacedIncoming = transitionCanvasVideo
            transitionCanvasVideo = outgoingCanvasVideo
            outgoingCanvasVideo = displacedIncoming
            canvasVideoProgressReversed = !canvasVideoProgressReversed
            return@LaunchedEffect
        }

        // If another track arrives before the previous dissolve has fully retired, only promote
        // the incoming video when it has actually rendered. A not-yet-ready TextureView can be
        // fully transparent; promoting it and disposing the last rendered cover produces a flash
        // or leaves the handoff showing a stale/blank surface during rapid skips.
        var retainExistingOutgoing = false
        if (outgoingCanvasVideo != null && nextVideo != null) {
            val previousOutgoing = outgoingCanvasVideo
            val previousIncoming = transitionCanvasVideo
            val previousIncomingIsReady = previousIncoming?.url?.let { renderedCanvasFrames[it] == true } == true
            val retiredVideo = if (previousIncomingIsReady) previousOutgoing else previousIncoming
            val retainedVideo = if (previousIncomingIsReady) previousIncoming else previousOutgoing
            retiredVideo?.url?.let { renderedCanvasFrames.remove(it) }
            outgoingCanvasVideo = retainedVideo
            retainExistingOutgoing = !previousIncomingIsReady
            canvasVideoProgressReversed = false
            canvasTrackChangeProgress.snapTo(1f)
        } else if (outgoingCanvasVideo != null) {
            // Preserve the established animated -> static path; the rapid-retarget handling
            // above is intentionally limited to animated -> animated transitions.
            outgoingCanvasVideo?.url?.let { renderedCanvasFrames.remove(it) }
            outgoingCanvasVideo = null
            canvasVideoProgressReversed = false
            canvasTrackChangeProgress.snapTo(1f)
        }

        if (transitionCanvasVideo != null && nextVideo != null) {
            // Keep the outgoing canvas playing while the incoming one prepares. Once its first
            // frame arrives, the animated-to-animated renderer hands off through the incoming
            // cover's matching cached preview instead of alpha-compositing two live surfaces.
            if (!canvasWasResolvedAtTrackStart) delay(120)
            renderedCanvasFrames.remove(nextVideo.url)
            val currentlyVisibleVideo = transitionCanvasVideo
            if (!retainExistingOutgoing) outgoingCanvasVideo = currentlyVisibleVideo
            transitionCanvasVideo = nextVideo
            canvasVideoProgressReversed = false
            val rapidCanvasHandoff = rapidTrackChange
            isRapidSongChange = rapidCanvasHandoff
            canvasTrackChangeProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 120, easing = FastOutSlowInEasing),
            )
        } else if (nextVideo != null) {
            // Static/image -> animated artwork: begin the incoming video beneath the existing
            // thumbnail. First-time covers get a short hold before the decoder starts; cached
            // covers start immediately because they are already the canonical artwork.
            if (!canvasWasResolvedAtTrackStart) delay(120)
            renderedCanvasFrames.remove(nextVideo.url)
            transitionCanvasVideo = nextVideo
            outgoingCanvasVideo = null
            canvasVideoProgressReversed = false
            canvasTrackChangeProgress.snapTo(1f)
        } else {
            if (transitionCanvasVideo != null) {
                val finishedVideoUrl = transitionCanvasVideo?.url
                canvasTrackChangeProgress.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(
                        durationMillis = if (rapidTrackChange || outgoingCanvasVideo != null) 220 else 420,
                        easing = FastOutSlowInEasing,
                    ),
                )
                finishedVideoUrl?.let { renderedCanvasFrames.remove(it) }
            }
            outgoingCanvasVideo = null
            transitionCanvasVideo = nextVideo
            canvasVideoProgressReversed = false
            canvasTrackChangeProgress.snapTo(1f)
        }
    }

    val activeCanvasFrameReady = transitionCanvasVideo?.url?.let { renderedCanvasFrames[it] == true } == true

    // A real video track owns the player surface even if the previous song's canvas is still
    // resolving. Do not let that stale canvas or its cover leak behind the video feed.
    val isVideoFeed = state.isCurrentVideoItem()
    val isVideoBackdropTop = viewState == AppleMusicView.MAIN && isVideoFeed
    // Reset the ambient color when switching from audio to a regular video. The player surface
    // itself switches immediately; matching other players, this handoff has no entry animation.
    var lastObservedMediaTrackId by remember { mutableStateOf(state.currentTrackId) }
    var lastObservedMediaIsVideo by remember { mutableStateOf(state.shouldShowVideo) }
    LaunchedEffect(state.currentTrackId, state.shouldShowVideo) {
        if (state.shouldShowVideo && !lastObservedMediaIsVideo && lastObservedMediaTrackId != null) {
            videoFrameAmbientColor.value = seedColor
        }
        lastObservedMediaTrackId = state.currentTrackId
        lastObservedMediaIsVideo = state.shouldShowVideo
    }
    // A regular video feed takes priority over any canvas data still resolving from the previous
    // track. Keep its lyrics handoff isolated from the animated/static artwork choreography.
    val animatedArtworkLyricsFade = displayCanvasData != null && !isVideoFeed
    // Keep the established cover/canvas timing; regular video feeds get a separate lyrics handoff.
    val bodyFadeDuration = 440
    var videoLyricsTransitionActive by remember { mutableStateOf(false) }
    LaunchedEffect(viewState, videoLyricsTransitionActive) {
        if (videoLyricsTransitionActive) {
            delay(VIDEO_LYRICS_TRANSITION_DURATION_MILLIS + 48L)
            videoLyricsTransitionActive = false
        }
    }
    // Keep the existing page/tab exit behavior. Track-type changes themselves are immediate.
    val videoFeedExitProgress by animateFloatAsState(
        targetValue = if (isVideoFeed && viewState != AppleMusicView.MAIN) 1f else 0f,
        animationSpec = tween(
            durationMillis = when {
                videoLyricsTransitionActive -> VIDEO_LYRICS_TRANSITION_DURATION_MILLIS
                else -> bodyFadeDuration
            },
            easing = FastOutSlowInEasing,
        ),
        label = "videoLyricsFeedExit",
    )
    val videoFeedTransitionProgress = videoFeedExitProgress
    var previousCanvasUrl by remember { mutableStateOf(state.screenData.canvasData?.url) }
    LaunchedEffect(displayCanvasData?.url, state.screenData.canvasResolved, viewState, state.showControlLayout) {
        val currentCanvasUrl = displayCanvasData?.url
        // Animated artwork stays in the regular player presentation. Keep its controls visible
        // even if a previously saved hidden-controls state is restored.
        if (currentCanvasUrl != null && viewState == AppleMusicView.MAIN && !state.showControlLayout) {
            actions.onToggleControls()
        }
        // If the next track has no canvas, restore the regular player controls rather than
        // leaving the static cover blank.
        if (
            state.screenData.canvasResolved && previousCanvasUrl != null && currentCanvasUrl == null &&
            viewState == AppleMusicView.MAIN && !state.showControlLayout
        ) {
            actions.onToggleControls()
        }
        if (currentCanvasUrl != null || state.screenData.canvasResolved) {
            previousCanvasUrl = currentCanvasUrl
        }
    }
    val transitionBlur = remember { Animatable(0f) }
    val lyricsExitBlur = remember { Animatable(0f) }
    // A rapid track change can interrupt the lyric transition coroutine while its blur
    // envelope is in flight. Never carry that transient blur onto the next song's cover.
    LaunchedEffect(currentArtworkTrackId) {
        transitionBlur.snapTo(0f)
        lyricsExitBlur.snapTo(0f)
    }
    val lyricsTransitionBlurSpec = remember(bodyFadeDuration) {
        val blurPeakTime = (bodyFadeDuration * 0.34f).toInt()
        keyframes<Float> {
            durationMillis = bodyFadeDuration
            0f at 0
            4f at blurPeakTime using FastOutSlowInEasing
            0f at bodyFadeDuration using FastOutSlowInEasing
        }
    }
    val canvasEntryAlpha = remember { Animatable(1f) }
    LaunchedEffect(displayCanvasData?.url, displayCanvasData?.isVideo) {
        if (displayCanvasData?.isVideo == true || displayCanvasData == null) {
            // Video waits for its real first frame in the artwork page, beneath the static cover.
            // Returning from lyrics therefore never exposes an unprepared black video surface.
            canvasEntryAlpha.snapTo(1f)
        } else {
            // Static canvas images can fade in immediately once Coil has them available.
            canvasEntryAlpha.snapTo(0f)
            canvasEntryAlpha.animateTo(1f, tween(420, easing = FastOutSlowInEasing))
        }
    }
    val playerViewScope = rememberCoroutineScope()
    val selectPlayerView: (AppleMusicView) -> Unit = selectView@ { nextView ->
        if (nextView == AppleMusicView.LYRICS && !state.lyricsEnabled) return@selectView
        val previousView = viewState
        val isVideoLyricsHandoff =
            (isVideoBackdropTop && nextView == AppleMusicView.LYRICS) ||
                (isVideoFeed && previousView == AppleMusicView.LYRICS && nextView == AppleMusicView.MAIN)
        playerViewScope.launch {
            videoLyricsTransitionActive = isVideoLyricsHandoff
            when {
                isVideoLyricsHandoff -> {
                    // Keep regular video feeds on their dedicated lyrics transition, even if stale
                    // canvas metadata from the previous track has not cleared yet.
                    transitionBlur.snapTo(0f)
                    lyricsExitBlur.snapTo(0f)
                    selectedViewState = nextView
                }

                animatedArtworkLyricsFade &&
                    previousView == AppleMusicView.MAIN && nextView == AppleMusicView.LYRICS -> {
                    // Blur the outgoing canvas with the established opening envelope.
                    lyricsExitBlur.snapTo(0f)
                    transitionBlur.snapTo(0f)
                    selectedViewState = nextView
                    transitionBlur.animateTo(0f, lyricsTransitionBlurSpec)
                }

                animatedArtworkLyricsFade &&
                    previousView == AppleMusicView.LYRICS && nextView == AppleMusicView.MAIN -> {
                    // Reverse the opening: blur the lyrics as they leave while the regular-scale
                    // animated cover fades back in underneath.
                    transitionBlur.snapTo(0f)
                    lyricsExitBlur.snapTo(0f)
                    selectedViewState = nextView
                    lyricsExitBlur.animateTo(0f, lyricsTransitionBlurSpec)
                }

                else -> {
                    transitionBlur.snapTo(0f)
                    lyricsExitBlur.snapTo(0f)
                    selectedViewState = nextView
                }
            }
        }
    }

    val sharedPlaybackControlsAlpha by animateFloatAsState(
        targetValue =
            when (viewState) {
                AppleMusicView.MAIN -> if (state.showControlLayout) 1f else 0f
                AppleMusicView.LYRICS -> if (showLyricsCluster) 1f else 0f
                AppleMusicView.QUEUE -> 1f
            },
        animationSpec = tween(
            durationMillis =
                if (videoLyricsTransitionActive) VIDEO_LYRICS_TRANSITION_DURATION_MILLIS
                else if (viewState == AppleMusicView.LYRICS) 360 else if (state.showControlLayout) 180 else 250,
            easing = FastOutSlowInEasing,
        ),
        label = "appleMusicSharedPlaybackControls",
    )

    val playerClusterVisibility by animateFloatAsState(
        targetValue = when (viewState) {
            AppleMusicView.MAIN -> if (state.showControlLayout) 1f else 0f
            AppleMusicView.LYRICS -> if (showLyricsCluster) 1f else 0f
            AppleMusicView.QUEUE -> 1f
        },
        animationSpec = tween(
            if (videoLyricsTransitionActive) VIDEO_LYRICS_TRANSITION_DURATION_MILLIS else 360,
            easing = FastOutSlowInEasing,
        ),
        label = "appleMusicPlayerClusterVisibility",
    )

    val deviceVolumeController = rememberDeviceVolumeController()
    val typography = rememberAppleMusicTypography()
    val localDensity = LocalDensity.current
    val bottomInset = with(localDensity) { WindowInsets.systemBars.getBottom(localDensity).toDp() }
    val sharedClusterHeight =
        if (getPlatform() == Platform.Android) 324.dp + bottomInset else 76.dp + bottomInset
    val sharedDockAlpha by animateFloatAsState(
        targetValue =
            when (viewState) {
                AppleMusicView.MAIN -> if (state.showControlLayout) 1f else 0f
                AppleMusicView.LYRICS -> if (showLyricsCluster) 1f else 0f
                AppleMusicView.QUEUE -> 1f
            },
        animationSpec = tween(
            durationMillis =
                if (videoLyricsTransitionActive) VIDEO_LYRICS_TRANSITION_DURATION_MILLIS
                else if (viewState == AppleMusicView.LYRICS) 360 else 220,
            easing = FastOutSlowInEasing,
        ),
        label = "appleMusicSharedDock",
    )
    val isPlaying = state.controllerState.isPlaying
    var previousPlaying by remember(state.currentTrackArtworkUrl()) { mutableStateOf(isPlaying) }
    var showLyricsResumeFeedback by remember(state.currentTrackArtworkUrl()) { mutableStateOf(false) }
    LaunchedEffect(isPlaying, showLyricsCluster, viewState, state.showControlLayout, animatedArtworkLyricsFade) {
        val justResumed = isPlaying && !previousPlaying
        previousPlaying = isPlaying
        val compactPlaybackSurface =
            (viewState == AppleMusicView.LYRICS && !showLyricsCluster) ||
                (viewState == AppleMusicView.MAIN && animatedArtworkLyricsFade && !state.showControlLayout)
        if (compactPlaybackSurface && justResumed) {
            showLyricsResumeFeedback = true
            delay(1_500)
            showLyricsResumeFeedback = false
        } else if (!isPlaying || !compactPlaybackSurface) {
            showLyricsResumeFeedback = false
        }
    }
    val latestActions by rememberUpdatedState(actions)
    val latestCanPrevious by rememberUpdatedState(state.controllerState.isPreviousAvailable)
    val latestCanNext by rememberUpdatedState(state.controllerState.isNextAvailable)
    val sharedLyricsHeaderGestures = Modifier.pointerInput(Unit) {
        var horizontalDrag = 0f
        val swipeThreshold = 36.dp.toPx()
        detectHorizontalDragGestures(
            onDragStart = { horizontalDrag = 0f },
            onHorizontalDrag = { change, dragAmount ->
                horizontalDrag += dragAmount
                change.consume()
            },
            onDragEnd = {
                when {
                    horizontalDrag > swipeThreshold && latestCanPrevious ->
                        latestActions.onUIEvent(UIEvent.SkipToPrevious)
                    horizontalDrag < -swipeThreshold && latestCanNext ->
                        latestActions.onUIEvent(UIEvent.Next)
                }
                horizontalDrag = 0f
            },
            onDragCancel = { horizontalDrag = 0f },
        )
    }

    // Backdrop source for the player and queue glass controls. The glass layers MUST be a sibling of
    // the button, never its parent: nesting the button inside the source is the render-feedback
    // loop that crashes the RuntimeShader.
    val panelBackdrop = rememberBackdrop(Color.Black)

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        val useLandscapeLayout = getPlatform() == Platform.Android && maxWidth > maxHeight
        var songArtworkHeight by remember(maxWidth, maxHeight) {
            mutableStateOf(if (useLandscapeLayout) maxHeight else (maxHeight - 330.dp).coerceAtLeast(200.dp))
        }
        Box(modifier = Modifier.matchParentSize().layerBackdrop(panelBackdrop)) {
            AppleMusicArtworkBackdrop(
                state = state,
                actions = actions,
                seedColor = displayedSeedColor,
                videoFrameColorState = videoFrameAmbientColor,
                canvasThumbnailUrl = displayCanvasData?.thumbnailUrl,
                preferCanvasThumbnail =
                    animatedToAnimatedTrackId != null && animatedToAnimatedTrackId == currentArtworkTrackId,
                songArtworkHeight = songArtworkHeight,
                landscapeArtwork = useLandscapeLayout,
            )
        }
        val artworkPlayerAlpha by animateFloatAsState(
            targetValue = if (viewState == AppleMusicView.MAIN) 1f else 0f,
            animationSpec = tween(
                if (videoLyricsTransitionActive) VIDEO_LYRICS_TRANSITION_DURATION_MILLIS else bodyFadeDuration,
                easing = FastOutSlowInEasing,
            ),
            label = "appleMusicPersistentArtworkFade",
        )
        // Both layouts share the same artwork/media renderer, with one surface kept alive
        // across tab switches. Landscape supplies its actual pane height instead of window height.
        val artworkContent: @Composable (Dp?, AppleMusicLandscapeVideoArea?) -> Unit = { artworkHeight, landscapeVideoArea ->
            AppleMusicMainView(
                state = state,
                actions = actions,
                typography = typography,
                backdrop = panelBackdrop,
                viewState = AppleMusicView.MAIN,
                onSelectView = selectPlayerView,
                seedColor = displayedSeedColor,
                onVideoFrameColor =
                    if (
                        (useLandscapeLayout || viewState == AppleMusicView.MAIN) &&
                            isVideoFeed &&
                        !reduceVisualEffects &&
                            (useLandscapeLayout || videoFeedTransitionProgress <= 0.001f)
                    ) {
                        reportVideoFrameColor
                    } else {
                        null
                    },
                // Keep the existing landscape behavior while a video is the active track.
                activePillContainer = activePillContainer,
                activePillContent = activePillContent,
                deviceVolumeController = deviceVolumeController,
                sharedClusterHeight = sharedClusterHeight,
                sharedLyricsHeaderGestures = if (animatedArtworkLyricsFade) sharedLyricsHeaderGestures else Modifier,
                showLyricsResumeFeedback = showLyricsResumeFeedback,
                canvasTransitionBlur = if (useLandscapeLayout) 0f else transitionBlur.value,
                songHandoffBlurRadius = visibleSongHandoffBlurRadius,
                animatedToAnimatedHandoffBlurRadius = animatedToAnimatedHandoffBlur.value,
                animatedToAnimatedBlurActive = animatedToAnimatedBlurActive,
                animatedToAnimatedHandoffStarting = animatedToAnimatedHandoffStarting,
                animatedToAnimatedTrackId = animatedToAnimatedTrackId,
                canvasEntryAlpha = canvasEntryAlpha.value,
                canvasData = displayCanvasData,
                canvasTrackChangeAlpha = canvasTrackChangeAlpha,
                canvasVideoData = transitionCanvasVideo,
                outgoingCanvasVideoData = outgoingCanvasVideo,
                previewOnlyCanvasVideoUrl = previewOnlyCanvasVideoUrl,
                onCanvasPreviewFallback = { previewOnlyCanvasVideoUrl = it },
                canvasVideoProgressReversed = canvasVideoProgressReversed,
                hasRenderedCanvasFrame = activeCanvasFrameReady,
                onCanvasVideoFrameRendered = { url ->
                    // Ignore callbacks from a surface that was retired during a rapid skip.
                    if (transitionCanvasVideo?.url == url || outgoingCanvasVideo?.url == url) {
                        renderedCanvasFrames[url] = true
                    }
                },
                isRapidSongChange = isRapidSongChange,
                onCurrentArtworkReady = { incomingStillArtworkLoaded = true },
                onCanvasVideoHandoffComplete = { incomingUrl, outgoingUrl ->
                    if (transitionCanvasVideo?.url == incomingUrl && outgoingCanvasVideo?.url == outgoingUrl) {
                        if (animatedToAnimatedBlurActive && outgoingUrl != null) {
                            playerViewScope.launch {
                                animatedToAnimatedHandoffBlur.animateTo(
                                    targetValue = 0f,
                                    animationSpec =
                                        tween(
                                            durationMillis = if (previewOnlyCanvasVideoUrl == incomingUrl) 140 else 560,
                                            easing = FastOutSlowInEasing,
                                        ),
                                )
                                if (transitionCanvasVideo?.url == incomingUrl && outgoingCanvasVideo?.url == outgoingUrl) {
                                    animatedToAnimatedBlurActive = false
                                    outgoingCanvasVideo = null
                                    renderedCanvasFrames.remove(outgoingUrl)
                                }
                            }
                        } else {
                            outgoingCanvasVideo = null
                            outgoingUrl?.let { renderedCanvasFrames.remove(it) }
                        }
                    }
                },
                artworkHeight = artworkHeight,
                landscapeVideoArea = landscapeVideoArea,
                onArtworkHeightChanged = { height ->
                    if (!useLandscapeLayout && !isVideoFeed) songArtworkHeight = height
                },
                videoExitProgress = if (useLandscapeLayout) 0f else videoFeedTransitionProgress,
            )
        }
        if (useLandscapeLayout) {
            AppleMusicLandscapePlayer(
                state = state,
                actions = actions,
                typography = typography,
                viewState = viewState,
                onSelectView = selectPlayerView,
                activePillContainer = activePillContainer,
                activePillContent = activePillContent,
                backdrop = panelBackdrop,
                deviceVolumeController = deviceVolumeController,
                showLyricsControls = showLyricsCluster,
                onToggleLyricsControls = { showLyricsCluster = !showLyricsCluster },
                artworkContent = { height, videoArea -> artworkContent(height, videoArea) },
            )
        } else {
            Box(Modifier.fillMaxSize().alpha(artworkPlayerAlpha)) {
                artworkContent(null, null)
            }
            AnimatedContent(
                targetState = viewState,
                transitionSpec = {
                    val fadeInSpec = tween<Float>(bodyFadeDuration, easing = FastOutSlowInEasing)
                    val fadeOutSpec = tween<Float>(bodyFadeDuration, easing = FastOutSlowInEasing)
                    when {
                        videoLyricsTransitionActive &&
                            initialState == AppleMusicView.MAIN && targetState == AppleMusicView.LYRICS -> {
                            // Let the native surface begin fading before introducing readable text.
                            // Keep the lyric layout stationary throughout the handoff.
                            fadeIn(
                                tween<Float>(
                                    durationMillis = VIDEO_LYRICS_TRANSITION_DURATION_MILLIS - VIDEO_LYRICS_ENTRY_DELAY_MILLIS,
                                    delayMillis = VIDEO_LYRICS_ENTRY_DELAY_MILLIS,
                                    easing = FastOutSlowInEasing,
                                ),
                            ).togetherWith(
                                fadeOut(tween(VIDEO_LYRICS_TRANSITION_DURATION_MILLIS, easing = FastOutSlowInEasing)),
                            )
                        }

                        videoLyricsTransitionActive &&
                            initialState == AppleMusicView.LYRICS && targetState == AppleMusicView.MAIN -> {
                            val videoLyricsFade = tween<Float>(VIDEO_LYRICS_TRANSITION_DURATION_MILLIS, easing = FastOutSlowInEasing)
                            fadeIn(videoLyricsFade).togetherWith(
                                fadeOut(videoLyricsFade),
                            )
                        }

                        initialState == AppleMusicView.MAIN && targetState == AppleMusicView.LYRICS ->
                            (fadeIn(fadeInSpec) + slideInVertically(tween(bodyFadeDuration, easing = FastOutSlowInEasing)) { it / 18 }).togetherWith(
                                fadeOut(fadeOutSpec),
                            )

                        initialState == AppleMusicView.LYRICS && targetState == AppleMusicView.MAIN ->
                            fadeIn(fadeInSpec).togetherWith(
                                fadeOut(fadeOutSpec) + slideOutVertically(tween(bodyFadeDuration, easing = FastOutSlowInEasing)) { it / 18 },
                            )

                        else ->
                            fadeIn(fadeInSpec).togetherWith(fadeOut(fadeOutSpec))
                    }
                },
                label = "appleMusicView",
            ) { view ->
                Box(
                    Modifier
                        .fillMaxSize()
                ) {
                when (view) {
                    AppleMusicView.MAIN -> Spacer(Modifier.fillMaxSize())

                    AppleMusicView.LYRICS ->
                        AppleMusicLyricsView(
                            state = state,
                            actions = actions,
                            typography = typography,
                            viewState = view,
                            onSelectView = selectPlayerView,
                            activePillContainer = activePillContainer,
                            activePillContent = activePillContent,
                            backdrop = panelBackdrop,
                            deviceVolumeController = deviceVolumeController,
                            showCluster = showLyricsCluster,
                            onToggleCluster = { showLyricsCluster = !showLyricsCluster },
                            sharedClusterHeight =
                                (sharedClusterHeight - APPLE_MUSIC_LYRICS_CONTENT_EXTENSION).coerceAtLeast(0.dp),
                            transitionBlur = lyricsExitBlur.value,
                        )

                    AppleMusicView.QUEUE ->
                        AppleMusicQueueView(
                            state = state,
                            actions = actions,
                            typography = typography,
                            viewState = view,
                            onSelectView = selectPlayerView,
                            activePillContainer = activePillContainer,
                            activePillContent = activePillContent,
                            backdrop = panelBackdrop,
                            deviceVolumeController = deviceVolumeController,
                            // Give the queue the same 120dp of headroom as lyrics before the shared
                            // bottom cluster. Its edge fade then blends into the same backdrop margin
                            // instead of starting early and washing out the last visible queue row.
                            sharedClusterHeight =
                                (sharedClusterHeight - APPLE_MUSIC_LYRICS_CONTENT_EXTENSION).coerceAtLeast(0.dp),
                        )
                }
                }
            }

            // Lyrics and Queue use the same compact song/action row. Keep this row outside the body
            // AnimatedContent so tapping between those tabs doesn't reload its artwork or icons.
            AnimatedVisibility(
                visible = viewState != AppleMusicView.MAIN,
                modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth(),
                enter =
                    if (videoLyricsTransitionActive) {
                        fadeIn(tween(
                            durationMillis = VIDEO_LYRICS_TRANSITION_DURATION_MILLIS - VIDEO_LYRICS_ENTRY_DELAY_MILLIS,
                            delayMillis = VIDEO_LYRICS_ENTRY_DELAY_MILLIS,
                            easing = FastOutSlowInEasing,
                        )) +
                            slideInVertically(tween(VIDEO_LYRICS_TRANSITION_DURATION_MILLIS, easing = FastOutSlowInEasing)) { -it / 32 }
                    } else {
                        fadeIn(tween(bodyFadeDuration, easing = FastOutSlowInEasing)) +
                            slideInVertically(tween(bodyFadeDuration, easing = FastOutSlowInEasing)) { -it / 20 }
                    },
                exit =
                    if (videoLyricsTransitionActive) {
                        fadeOut(tween(VIDEO_LYRICS_TRANSITION_DURATION_MILLIS, easing = FastOutSlowInEasing)) +
                            slideOutVertically(tween(VIDEO_LYRICS_TRANSITION_DURATION_MILLIS, easing = FastOutSlowInEasing)) { -it / 32 }
                    } else {
                        fadeOut(tween(bodyFadeDuration, easing = FastOutSlowInEasing)) +
                            slideOutVertically(tween(bodyFadeDuration, easing = FastOutSlowInEasing)) { -it / 20 }
                    },
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Spacer(
                        modifier =
                            Modifier.height(
                                with(localDensity) { WindowInsets.statusBars.getTop(localDensity).toDp() } + 20.dp,
                            ),
                    )
                    AppleMusicCompactHeader(
                        state = state,
                        actions = actions,
                        typography = typography,
                        // Lyrics and Queue share the same compact header layout: prominent title and
                        // artist styling, with the action buttons centered on the track row.
                        prominentTrackInfo = true,
                        actionsVerticalOffset = 0.dp,
                        showPlaylistAction = viewState == AppleMusicView.LYRICS,
                        artworkInteraction =
                            if (viewState == AppleMusicView.LYRICS && !showLyricsCluster) {
                                Modifier
                                    .clickable { actions.onUIEvent(UIEvent.PlayPause) }
                                    .then(sharedLyricsHeaderGestures)
                            } else if (viewState == AppleMusicView.LYRICS) {
                                sharedLyricsHeaderGestures
                            } else {
                                Modifier
                            },
                        trackInfoInteraction =
                            if (viewState == AppleMusicView.LYRICS) sharedLyricsHeaderGestures else Modifier,
                        artworkOverlay = {
                            AnimatedVisibility(
                                visible =
                                    viewState == AppleMusicView.LYRICS &&
                                        !showLyricsCluster && (!isPlaying || showLyricsResumeFeedback),
                                enter = fadeIn(animationSpec = tween(120)),
                                exit = fadeOut(animationSpec = tween(180)),
                            ) {
                                Box(
                                    modifier = Modifier.size(36.dp).background(Color.Black.copy(alpha = 0.48f), CircleShape),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = if (isPlaying) WaveIcons.PlayArrow else WaveIcons.Pause,
                                        contentDescription = if (isPlaying) "Resumed playback" else "Playback paused",
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp),
                                    )
                                }
                            }
                        },
                    )
                }
            }

            // Keep the complete bottom cluster (seek/transport and lyrics/queue dock) mounted above
            // the animated body. Switching MAIN ↔ LYRICS updates the selected dock pill in place,
            // avoiding a second set of icons being disposed and recreated during the crossfade.
            AppleMusicBottomCluster(
                state = state,
                actions = actions,
                typography = typography,
                viewState = viewState,
                onSelectView = selectPlayerView,
                activePillContainer = activePillContainer,
                activePillContent = activePillContent,
                backdrop = panelBackdrop,
                deviceVolumeController = deviceVolumeController,
                playbackControlsAlpha = if (getPlatform() == Platform.Android) sharedPlaybackControlsAlpha else 0f,
                dockAlpha = sharedDockAlpha,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .graphicsLayer {
                        // Ease the transport/dock down as they fade, instead of making them
                        // vanish in place when fullscreen lyrics are toggled.
                        translationY = (1f - playerClusterVisibility) * 20.dp.toPx()
                    },
            )

            // Desktop uses a side panel and keeps its explicit dismiss button.
            // Android uses the player-wide pull-down gesture and the handle above both layouts.
            if (getPlatform() == Platform.Desktop && viewState == AppleMusicView.MAIN) {
                LiquidGlassIconButton(
                    backdrop = panelBackdrop,
                    imageVector = state.dismissIcon,
                    shape = RoundedCornerShape(24.dp),
                    // Same as AnalyticsScreen's back button: a 48dp circle catches only a short arc
                    // of the default directional sweep and reads as rimless. 1.dp is the smallest
                    // step that stays visible without looking like a plain border.
                    highlight = Highlight(width = 1.dp),
                    modifier =
                        Modifier
                            .align(Alignment.TopStart)
                            .padding(start = DISMISS_BUTTON_INSET, end = DISMISS_BUTTON_INSET, top = DISMISS_BUTTON_TOP_INSET, bottom = DISMISS_BUTTON_TOP_INSET)
                            .size(DISMISS_BUTTON_SIZE),
                    onClick = { actions.onDismiss() },
                )
            }
        }
        if (getPlatform() == Platform.Android && state.isExpanded && !state.isInPipMode) {
            // Keep one handle above Main, Lyrics and Queue in either orientation. It has no
            // pointer handler, so pulling here reaches the existing player dismissal gesture.
            Box(
                Modifier
                    .align(Alignment.TopCenter)
                    .padding(
                        top = with(localDensity) {
                            maxOf(
                                WindowInsets.statusBars.getTop(localDensity),
                                WindowInsets.displayCutout.getTop(localDensity),
                            ).toDp()
                        } + 8.dp,
                    )
                    .size(width = 36.dp, height = 4.dp)
                    .background(Color.White.copy(alpha = 0.48f), RoundedCornerShape(50)),
            )
        }
    }
}

/**
 * MAIN: full-bleed artwork pager (top ~60%, fading into the page gradient) with the shell's
 * artwork-pager sync, a title row, and the shared bottom cluster.
 *
 * Animated canvas artwork uses the same crop, area and bottom fade as static cover art, while the
 * title row and shared controls keep their regular placement. A tap on an already-shown VIDEO still
 * enters the existing fullscreen video route instead of rebuilding Classic's ±5s/subtitle overlay.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppleMusicMainView(
    state: NowPlayingContentState,
    actions: NowPlayingContentActions,
    typography: AppleMusicTypography,
    backdrop: PlatformBackdrop,
    viewState: AppleMusicView,
    onSelectView: (AppleMusicView) -> Unit,
    seedColor: Color,
    onVideoFrameColor: ((Color) -> Unit)?,
    activePillContainer: Color,
    activePillContent: Color,
    deviceVolumeController: DeviceVolumeController?,
    sharedClusterHeight: androidx.compose.ui.unit.Dp,
    sharedLyricsHeaderGestures: Modifier,
    showLyricsResumeFeedback: Boolean,
    canvasTransitionBlur: Float,
    songHandoffBlurRadius: Float,
    animatedToAnimatedHandoffBlurRadius: Float,
    animatedToAnimatedBlurActive: Boolean,
    animatedToAnimatedHandoffStarting: Boolean,
    animatedToAnimatedTrackId: String?,
    canvasEntryAlpha: Float,
    canvasData: NowPlayingScreenData.CanvasData?,
    canvasTrackChangeAlpha: State<Float>,
    canvasVideoData: NowPlayingScreenData.CanvasData?,
    outgoingCanvasVideoData: NowPlayingScreenData.CanvasData?,
    previewOnlyCanvasVideoUrl: String?,
    onCanvasPreviewFallback: (String?) -> Unit,
    canvasVideoProgressReversed: Boolean,
    hasRenderedCanvasFrame: Boolean,
    onCanvasVideoFrameRendered: (String) -> Unit,
    isRapidSongChange: Boolean,
    onCurrentArtworkReady: () -> Unit,
    onCanvasVideoHandoffComplete: (incomingUrl: String, outgoingUrl: String?) -> Unit,
    artworkHeight: Dp? = null,
    landscapeVideoArea: AppleMusicLandscapeVideoArea? = null,
    onArtworkHeightChanged: (Dp) -> Unit = {},
    videoExitProgress: Float = 0f,
) {
    val screenInfo = getScreenSizeInfo()
    val localDensity = LocalDensity.current
    val isVideoBackdrop = state.isCurrentVideoItem()
    val currentArtworkTrackId = state.currentTrackId
    val sharedViewModel: SharedViewModel = koinInject()
    var songArtworkReady by remember(currentArtworkTrackId) { mutableStateOf(false) }
    var hasComposedVideo by remember { mutableStateOf(false) }
    var readyVideoTrackId by remember(currentArtworkTrackId, isVideoBackdrop) { mutableStateOf<String?>(null) }
    var lastSongArtwork by remember { mutableStateOf<AppleMusicVideoEntryArtwork?>(null) }
    var lastVideoArtwork by remember { mutableStateOf<AppleMusicVideoEntryArtwork?>(null) }
    var lastSongArtworkTrackId by remember { mutableStateOf<String?>(null) }
    var lastVideoArtworkTrackId by remember { mutableStateOf<String?>(null) }
    var previousMediaWasVideo by remember { mutableStateOf(isVideoBackdrop) }
    var previousMediaTrackId by remember { mutableStateOf(currentArtworkTrackId) }
    val mediaHandoffArtwork = remember(isVideoBackdrop) {
        if (previousMediaWasVideo != isVideoBackdrop) {
            // A buffering item has no decoded still. Morph its black viewport rather
            // than reviving an image cached from an earlier track during a quick skip.
            (if (isVideoBackdrop) {
                lastSongArtwork.takeIf { lastSongArtworkTrackId == previousMediaTrackId }
            } else {
                lastVideoArtwork.takeIf { lastVideoArtworkTrackId == previousMediaTrackId }
            }) ?: AppleMusicVideoEntryArtwork(null, null)
        } else null
    }
    val mediaHandoffTrackId = remember(isVideoBackdrop) { currentArtworkTrackId }
    val morphMediaType = getPlatform() == Platform.Android && mediaHandoffArtwork != null &&
        (!isVideoBackdrop || mediaHandoffTrackId == currentArtworkTrackId)
    val mediaViewportProgress = remember { Animatable(if (isVideoBackdrop) 1f else 0f) }
    val mediaMorphActive = morphMediaType &&
        (if (isVideoBackdrop) mediaViewportProgress.value < 1f else mediaViewportProgress.value > 0f)
    val mediaVideoReveal = remember(currentArtworkTrackId, isVideoBackdrop) {
        Animatable(if (isVideoBackdrop && morphMediaType) 0f else 1f)
    }
    val currentVideoFrameReady = isVideoBackdrop && readyVideoTrackId == currentArtworkTrackId
    var retainedVideoRatio by remember { mutableFloatStateOf(state.videoAspectRatio) }
    val targetVideoRatio = if (currentVideoFrameReady) state.videoAspectRatio else retainedVideoRatio
    val videoFrameRatio by animateFloatAsState(
        targetValue = targetVideoRatio.takeIf { it.isFinite() && it > 0f } ?: (16f / 10f),
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "appleMusicSharedVideoFrameRatio",
    )
    val songArtworkUrl = state.currentTrackArtworkUrl()
    val songArtworkBitmap = if (!isVideoBackdrop) {
        sharedViewModel.cachedArtworkBitmap(currentArtworkTrackId)
            ?: sharedViewModel.cachedArtworkBitmap(songArtworkUrl)
    } else null
    SideEffect {
        previousMediaWasVideo = isVideoBackdrop
        previousMediaTrackId = currentArtworkTrackId
        if (currentVideoFrameReady) retainedVideoRatio = targetVideoRatio
        if (isVideoBackdrop) {
            hasComposedVideo = true
        } else if (songArtworkBitmap != null || !songArtworkUrl.isNullOrBlank()) {
            lastSongArtwork = AppleMusicVideoEntryArtwork(songArtworkUrl, songArtworkBitmap)
            lastSongArtworkTrackId = currentArtworkTrackId
        }
    }
    // Capture only at the song -> video boundary. Later video -> video skips must never
    // resurrect that song cover while the next decoder buffers.
    val videoEntryArtwork = remember(isVideoBackdrop) { lastSongArtwork.takeIf { isVideoBackdrop } }
    val videoEntryArtworkAlpha = remember(isVideoBackdrop) {
        Animatable(if (videoEntryArtwork != null) 1f else 0f)
    }
    LaunchedEffect(isVideoBackdrop) {
        if (isVideoBackdrop) {
            // Leave the song cover promptly, even when the video buffers for seconds.
            // The black video card is the loading visual; the renderer fades in its
            // decoded frame independently once this outgoing cover has disappeared.
            videoEntryArtworkAlpha.animateTo(0f, tween(350, easing = FastOutSlowInEasing))
        }
    }
    val mediaTypeExitProgress = remember { Animatable(if (isVideoBackdrop) 0f else 1f) }
    val songVisualReady by rememberUpdatedState(
        songArtworkReady || songArtworkBitmap != null ||
            (hasRenderedCanvasFrame && state.screenData.currentVideoId == currentArtworkTrackId &&
                state.screenData.canvasData?.isVideo == true),
    )
    LaunchedEffect(isVideoBackdrop, currentArtworkTrackId) {
        if (isVideoBackdrop) {
            mediaTypeExitProgress.snapTo(0f)
            if (morphMediaType) {
                mediaViewportProgress.animateTo(1f, tween(520, easing = FastOutSlowInEasing))
            } else {
                mediaViewportProgress.snapTo(1f)
            }
        } else {
            snapshotFlow { songVisualReady }.first { it }
            if (morphMediaType) {
                mediaViewportProgress.animateTo(0f, tween(520, easing = FastOutSlowInEasing))
                mediaTypeExitProgress.snapTo(1f)
            } else {
                mediaViewportProgress.snapTo(0f)
                mediaTypeExitProgress.animateTo(1f, tween(350, easing = FastOutSlowInEasing))
            }
        }
    }
    LaunchedEffect(isVideoBackdrop, morphMediaType, mediaMorphActive, currentVideoFrameReady) {
        if (isVideoBackdrop && morphMediaType) {
            if (!mediaMorphActive && currentVideoFrameReady) {
                mediaVideoReveal.animateTo(1f, tween(220, easing = FastOutSlowInEasing))
            }
        } else {
            mediaVideoReveal.snapTo(1f)
        }
    }

    // Same fade/half-blended-frame fix M3E uses: fast fade-in, relaxed fade-out.
    val controlsAlpha by animateFloatAsState(
        targetValue = if (state.showControlLayout) 1f else 0f,
        animationSpec = tween(durationMillis = if (state.showControlLayout) 180 else 250, easing = FastOutSlowInEasing),
        label = "appleMusicControlsAlpha",
    )
    val coverPlaybackFeedbackAlpha by animateFloatAsState(
        targetValue =
            if (!state.showControlLayout && (!state.controllerState.isPlaying || showLyricsResumeFeedback)) 1f else 0f,
        animationSpec = tween(durationMillis = if (!state.showControlLayout) 120 else 180),
        label = "appleMusicCoverPlaybackFeedback",
    )

    // The artwork zone is whatever's left above the measured title-row+cluster block, so the
    // whole MAIN view always totals exactly one screen height — no scroll, per spec.
    // remember, not rememberSaveable: a measured height must not survive a rotation, or a portrait
    // cluster height is paired with a landscape screen height for a frame. Seeded near its real
    // value so the FIRST frame doesn't draw a full-screen artwork that then snaps up.
    val boundedArtwork = artworkHeight != null
    var bottomContentHeightDp by remember { mutableIntStateOf(if (boundedArtwork) 0 else 330) }
    val artworkZoneHeightDp =
        artworkHeight?.value?.toInt()?.coerceAtLeast(1)
            ?: (screenInfo.hDP - bottomContentHeightDp).coerceAtLeast(200)
    SideEffect { onArtworkHeightChanged(artworkZoneHeightDp.dp) }
    val canvasVideoAlpha = remember { Animatable(0f) }
    val fallbackFrameReveal = remember { Animatable(1f) }
    var unavailableCanvasPreviewUrl by remember { mutableStateOf<String?>(null) }
    var canvasPreviewLoadResult by remember { mutableStateOf<Pair<String, Boolean>?>(null) }
    val latestCanvasFrameReady by rememberUpdatedState(hasRenderedCanvasFrame)
    val latestPreviewOnlyCanvasVideoUrl by rememberUpdatedState(previewOnlyCanvasVideoUrl)
    val latestCanvasPreviewLoadResult by rememberUpdatedState(canvasPreviewLoadResult)
    var previousCanvasTargetUrl by remember { mutableStateOf(canvasVideoData?.url) }
    var previousProgressDirection by remember { mutableStateOf(canvasVideoProgressReversed) }
    val isAnimatedToAnimatedHandoff =
        outgoingCanvasVideoData?.isVideo == true && canvasVideoData?.isVideo == true
    // Some animated-cover entries have no dedicated thumbnail. Falling through the staged
    // outgoing -> still -> incoming curve without a still leaves a transparent gap, seen as a
    // flash for those songs. Use the incoming track's regular artwork as that bridge.
    val incomingCanvasTrack =
        state.artworkQueue.firstOrNull { it.videoId == canvasVideoData?.videoId }
            ?: state.artworkQueue.getOrNull(state.currentOrderIndex)
                ?.takeIf { it.videoId == currentArtworkTrackId }
    val incomingVideoPreviewUrl =
        canvasVideoData?.thumbnailUrl?.takeIf { it.isNotBlank() && it != unavailableCanvasPreviewUrl }
            ?: incomingCanvasTrack
                ?.thumbnails
                ?.maxByOrNull { it.width * it.height }
                ?.url
                ?.requestLargePlayerArtwork()
                ?.takeIf { it != unavailableCanvasPreviewUrl }
            ?: state.currentTrackArtworkUrl()
                ?.takeIf { it != unavailableCanvasPreviewUrl }
    val onHandoffComplete by rememberUpdatedState(onCanvasVideoHandoffComplete)
    val onPreviewFallback by rememberUpdatedState(onCanvasPreviewFallback)
    LaunchedEffect(
        canvasVideoData?.url,
        outgoingCanvasVideoData?.url,
        hasRenderedCanvasFrame,
        isRapidSongChange,
        canvasVideoProgressReversed,
        isAnimatedToAnimatedHandoff,
        incomingVideoPreviewUrl,
    ) {
        val incomingUrl = canvasVideoData?.takeIf { it.isVideo }?.url
        if (incomingUrl == null) {
            canvasVideoAlpha.snapTo(0f)
            previousCanvasTargetUrl = null
            previousProgressDirection = canvasVideoProgressReversed
            return@LaunchedEffect
        }

        val directionChanged = previousProgressDirection != canvasVideoProgressReversed
        if (!directionChanged && incomingUrl != previousCanvasTargetUrl) {
            // A new video fades in from zero. When layer roles reverse, keep current progress;
            // the polarity change complements it immediately to preserve both visible opacities.
            canvasVideoAlpha.snapTo(0f)
            // A fallback reveal belongs to exactly one video URL. Carrying its partially revealed
            // value into the next URL makes the next clip start transparent and can expose the
            // old preview/blur underneath during rapid skips.
            if (latestPreviewOnlyCanvasVideoUrl != incomingUrl) {
                fallbackFrameReveal.snapTo(1f)
            }
        }
        previousCanvasTargetUrl = incomingUrl
        previousProgressDirection = canvasVideoProgressReversed
        val outgoingUrl = outgoingCanvasVideoData?.url
        if (isAnimatedToAnimatedHandoff && outgoingUrl != null) {
            // Keep the requested blur lead-in, but never let a missed decoder callback pin the
            // outgoing cover on screen. Its cached incoming still becomes the temporary owner.
            if (latestPreviewOnlyCanvasVideoUrl != incomingUrl) delay(200)
            val incomingFrameReady =
                latestCanvasFrameReady ||
                    withTimeoutOrNull(320) {
                        snapshotFlow { latestCanvasFrameReady }.first { it }
                    } != null
            if (!incomingFrameReady) {
                fallbackFrameReveal.snapTo(0f)
                onPreviewFallback(incomingUrl)
                canvasVideoAlpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing),
                )
                onHandoffComplete(incomingUrl, outgoingUrl)
                return@LaunchedEffect
            }
            // The preview is the middle layer of this dissolve. Do not start fading the outgoing
            // video until that actual bitmap has decoded; otherwise AsyncImage can contribute
            // zero pixels for a frame even though its alpha is already at the bridge peak.
            if (incomingVideoPreviewUrl != null && latestCanvasPreviewLoadResult?.first != incomingVideoPreviewUrl) {
                val previewLoadFinished =
                    withTimeoutOrNull(180) {
                        snapshotFlow { latestCanvasPreviewLoadResult }
                            .first { it?.first == incomingVideoPreviewUrl }
                    }
                if (previewLoadFinished == null && incomingVideoPreviewUrl != null) {
                    // Fall back to a direct dissolve if even the album-art bridge is unavailable.
                    unavailableCanvasPreviewUrl = incomingVideoPreviewUrl
                    return@LaunchedEffect
                }
            }
        } else if (!latestCanvasFrameReady) {
            // Preserve the existing first-frame gate for non A→A transitions.
            snapshotFlow { latestCanvasFrameReady }.first { it }
        }

        val isPreviewFallback = latestPreviewOnlyCanvasVideoUrl == incomingUrl
        if (isPreviewFallback) {
            // The still remains the cover owner until the new video has a valid first frame.
            fallbackFrameReveal.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
            )
            onPreviewFallback(null)
        }

        if (outgoingUrl == null) {
            if (!isPreviewFallback) {
                // The static cover already bridges decoder startup. Dissolve the first live
                // frame over that cover instead of snapping from poster to motion in one frame.
                canvasVideoAlpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = if (isRapidSongChange) 180 else 240,
                        easing = FastOutSlowInEasing,
                    ),
                )
            }
            onHandoffComplete(incomingUrl, outgoingUrl)
            return@LaunchedEffect
        }
        // Build the A→A blur before showing the incoming clip so its first visible frame is
        // already softened. Other transition types keep their original timing.
        // Animated artwork is already the canonical cover, so use a short handoff. The render
        // layer stages it through the cached preview, avoiding a live TextureView overlap.
        canvasVideoAlpha.animateTo(
            targetValue = if (canvasVideoProgressReversed) 0f else 1f,
            animationSpec = tween(
                durationMillis = if (isRapidSongChange) 180 else 240,
                easing = FastOutSlowInEasing,
            ),
        )
        onHandoffComplete(incomingUrl, outgoingUrl)
    }
    // A new URL can compose one frame before the LaunchedEffect below resets its Animatable.
    // Without this synchronous guard, the new TextureView can briefly inherit the previous
    // handoff's completed alpha, then snap back to the outgoing cover before fading in.
    val isStartingForwardAnimatedHandoff =
        isAnimatedToAnimatedHandoff &&
            canvasVideoData?.url != previousCanvasTargetUrl &&
            canvasVideoProgressReversed == previousProgressDirection
    val displayedCanvasVideoAlpha =
        if (isStartingForwardAnimatedHandoff) {
            0f
        } else if (canvasVideoProgressReversed) {
            1f - canvasVideoAlpha.value
        } else {
            canvasVideoAlpha.value
        }
    val visibleHandoffBlurRadius =
        if (isAnimatedToAnimatedHandoff || animatedToAnimatedBlurActive) {
            maxOf(songHandoffBlurRadius, animatedToAnimatedHandoffBlurRadius)
        } else {
            songHandoffBlurRadius
        }
    // Fade over a short but visible area so large covers blend gradually into the ambient
    // backdrop instead of ending at a visible horizontal cut. Keep it consistent during handoffs.
    val artworkBottomFade = if (boundedArtwork) 0.dp else 240.dp
    val canSwipePrevious = state.controllerState.isPreviousAvailable
    val canSwipeNext = state.controllerState.isNextAvailable
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .then(
                    if (landscapeVideoArea != null) {
                        val coverFade = 1f - mediaViewportProgress.value.coerceIn(0f, 1f)
                        Modifier.appleMusicVerticalFadeEdges(
                            topFade = 0.dp,
                            bottomFade = artworkZoneHeightDp.dp * 0.24f * coverFade,
                            sideFade = screenInfo.wDP.dp * 0.045f * coverFade,
                            endSideFade = screenInfo.wDP.dp * 0.12f * coverFade,
                            smoothFade = true,
                        )
                    } else Modifier,
                )
                .pointerInput(state.currentTrackId, canSwipePrevious, canSwipeNext) {
                    var horizontalDrag = 0f
                    val swipeThreshold = 36.dp.toPx()
                    detectHorizontalDragGestures(
                        onDragStart = { horizontalDrag = 0f },
                        onHorizontalDrag = { change, dragAmount ->
                            horizontalDrag += dragAmount
                            change.consume()
                        },
                        onDragEnd = {
                            when {
                                horizontalDrag > swipeThreshold && canSwipePrevious ->
                                    actions.onUIEvent(UIEvent.SkipToPrevious)
                                horizontalDrag < -swipeThreshold && canSwipeNext ->
                                    actions.onUIEvent(UIEvent.Next)
                            }
                            horizontalDrag = 0f
                        },
                        onDragCancel = { horizontalDrag = 0f },
                    )
                },
    ) {
        // When a video participates in the handoff, let the canvas/preview layers below own the
        // transition. Fading this whole page as well stacks a second alpha animation over the
        // video dissolve, which creates a visible brightness dip between animated covers.
        val artworkPageFadeDuration =
            if (canvasVideoData != null || outgoingCanvasVideoData != null) {
                0
            } else if (isRapidSongChange) {
                220
            } else {
                420
            }
        // A track can change while its album cover stays the same. Keep that artwork in place;
        // the title row already crossfades independently, as it does in the reference player.
        // Freeze the transition identity for this track. Artwork metadata often arrives one
        // recomposition after the player switches items; letting that URL replace the fallback
        // identity would start a second dissolve after the actual track handoff had finished.
        val artworkVisualIdentity = remember(currentArtworkTrackId) {
            state.currentArtworkVisualIdentity()
        }
        // Track changes use a stationary fade, not a horizontal pager movement. The pager index
        // is synced instantly below; static covers use this fade while video handoffs use the
        // dedicated canvas layers so two fades are never stacked.
        AnimatedContent(
            targetState =
                AppleMusicArtworkPageTarget(
                    page = state.currentOrderIndex.coerceAtLeast(0),
                    trackId = currentArtworkTrackId,
                    wasVideo = isVideoBackdrop,
                    visualIdentity = artworkVisualIdentity,
                ),
            modifier = Modifier.fillMaxSize().graphicsLayer { alpha = if (mediaMorphActive) 0f else 1f },
            transitionSpec = {
                // The video page intentionally has no artwork content. AnimatedContent's default
                // SizeTransform therefore collapses to that empty page and expands back from it;
                // if the user switches tracks again mid-flight, the next cover starts from the
                // interrupted size and visibly grows/slides into place. Keep the viewport fixed
                // and animate only the artwork layers themselves.
                (
                    when {
                        // The retained song bitmap and persistent video feed own media-type
                        // handoffs. Keep AnimatedContent out of that dissolve so alpha is applied once.
                        initialState.wasVideo != targetState.wasVideo ||
                            initialState.visualIdentity == targetState.visualIdentity ->
                            EnterTransition.None togetherWith ExitTransition.None
                        else ->
                            fadeIn(
                                animationSpec = tween(
                                    durationMillis = artworkPageFadeDuration,
                                    easing = FastOutSlowInEasing,
                                ),
                            ) togetherWith
                                fadeOut(
                                    animationSpec = tween(
                                        durationMillis = artworkPageFadeDuration,
                                        easing = FastOutSlowInEasing,
                                    ),
                                )
                    }
                ).using(sizeTransform = null)
            },
            label = "appleMusicStationaryArtworkFade",
        ) { target ->
            AppleMusicArtworkPage(
                state = state,
                actions = actions,
                localDensity = localDensity,
                page = target.page,
                pageTrackId = target.trackId,
                pageWasVideo = target.wasVideo,
                artworkZoneHeightDp = artworkZoneHeightDp,
                bottomContentHeightDp = bottomContentHeightDp,
                isVideoBackdrop = isVideoBackdrop,
                canvasTransitionBlur = canvasTransitionBlur,
                songHandoffBlurRadius = visibleHandoffBlurRadius,
                artworkBottomFade = artworkBottomFade,
                canvasEntryAlpha = canvasEntryAlpha,
                canvasData = canvasData,
                transitionCanvasVideoData = canvasVideoData,
                canvasTrackChangeAlpha = canvasTrackChangeAlpha.value,
                canvasVideoAlpha = displayedCanvasVideoAlpha,
                outgoingCanvasVideoData = outgoingCanvasVideoData,
                previewOnlyCanvasVideoUrl = previewOnlyCanvasVideoUrl,
                fallbackFrameReveal = fallbackFrameReveal.value,
                isRapidSongChange = isRapidSongChange,
                onVideoFrameColor = onVideoFrameColor,
                onCurrentArtworkReady = {
                    songArtworkReady = true
                    onCurrentArtworkReady()
                },
                boundedArtwork = boundedArtwork,
            )
        }

        // Keep Android's one SurfaceView and its frozen-frame owner mounted across song
        // changes. Removing this branch immediately discarded the video before its exit fade.
        if (isVideoBackdrop || (getPlatform() == Platform.Android && hasComposedVideo)) {
            AppleMusicVideoFeed(
                state = state,
                localDensity = localDensity,
                bottomContentHeightDp = bottomContentHeightDp,
                boundedArtwork = boundedArtwork,
                onVideoFrameColor = onVideoFrameColor,
                videoExitProgress = if (morphMediaType && (mediaMorphActive || !isVideoBackdrop)) 1f else
                    1f - (1f - videoExitProgress) *
                        (if (isVideoBackdrop) 1f else 1f - mediaTypeExitProgress.value),
                videoFrameRatio = videoFrameRatio,
                landscapeVideoArea = landscapeVideoArea,
                keepFrameTracking = isVideoBackdrop && morphMediaType && videoExitProgress < 1f,
                hasVideoHandoffCover = if (morphMediaType) true else
                    currentArtworkTrackId == mediaHandoffTrackId &&
                        videoEntryArtwork != null && videoEntryArtworkAlpha.value > 0f,
                onVideoFrameReady = {
                    if (it == currentArtworkTrackId) readyVideoTrackId = it
                },
                onVideoFrameSnapshot = { id, frame ->
                    if (id == currentArtworkTrackId && isVideoBackdrop) {
                        lastVideoArtwork = AppleMusicVideoEntryArtwork(null, frame)
                        lastVideoArtworkTrackId = id
                    }
                },
            )
        }
        if (!morphMediaType && currentArtworkTrackId == mediaHandoffTrackId &&
            videoEntryArtwork != null && videoEntryArtworkAlpha.value > 0f
        ) {
            val entryModifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(artworkZoneHeightDp.dp)
                .graphicsLayer { alpha = videoEntryArtworkAlpha.value }
                .appleMusicVerticalFadeEdges(
                    topFade = 0.dp,
                    bottomFade = artworkBottomFade,
                    sideFade = if (boundedArtwork) 0.dp else 32.dp,
                    smoothFade = true,
                )
            if (videoEntryArtwork.bitmap != null) {
                Image(videoEntryArtwork.bitmap, null, entryModifier, contentScale = ContentScale.Crop)
            } else {
                AsyncImage(
                    model = videoEntryArtwork.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = entryModifier,
                )
            }
        }

        // Draw video layers ABOVE their still preview. For video-to-video handoffs, the preview
        // bridges the two decoders: the outgoing clip fades into it, then the incoming video,
        // which has kept running behind its preview, is revealed. Never blend two moving
        // TextureViews at once.
        // Keys preserve the incoming player when the outgoing layer is removed after handoff.
        val canvasVideoLayers =
            if (isVideoBackdrop) {
                emptyList()
            } else {
                listOfNotNull(
                    outgoingCanvasVideoData?.takeIf { it.url.isNotBlank() }?.let { it to false },
                    canvasVideoData?.takeIf { it.url.isNotBlank() }?.let { it to true },
                )
            }
        if (canvasVideoLayers.isNotEmpty()) {
            val keepAnimatedCoverEdgeConnected =
                isAnimatedToAnimatedHandoff ||
                    (animatedToAnimatedTrackId != null && animatedToAnimatedTrackId == state.currentTrackId)
            Box(
                modifier =
                    Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .height(artworkZoneHeightDp.dp)
                        .graphicsLayer { alpha = if (mediaMorphActive) 0f else 1f }
                        .then(
                            if (visibleHandoffBlurRadius > 0f) {
                                Modifier.blur(visibleHandoffBlurRadius.dp, BlurredEdgeTreatment.Rectangle)
                            } else {
                                Modifier
                            },
                        )
                        // Apply the lower edge blend once to the complete A→A composition.
                        // Masking each TextureView separately lets the backdrop show through
                        // between decoder layers, making the lower cover look detached.
                        .then(
                            if (keepAnimatedCoverEdgeConnected && !boundedArtwork) {
                                Modifier.appleMusicVerticalFadeEdges(topFade = 0.dp, bottomFade = 300.dp, smoothFade = true)
                            } else {
                                Modifier
                            },
                        ),
            ) {
                if (isAnimatedToAnimatedHandoff) {
                    val videoProgress = displayedCanvasVideoAlpha.coerceIn(0f, 1f)
                    val hasPreviewBridge = !incomingVideoPreviewUrl.isNullOrBlank()
                    val outgoingAlpha =
                        if (hasPreviewBridge) outgoingVideoHandoffAlpha(videoProgress) else 1f - videoProgress
                    val incomingAlpha =
                        (if (hasPreviewBridge) incomingVideoHandoffAlpha(videoProgress) else videoProgress) *
                            if (previewOnlyCanvasVideoUrl == canvasVideoData?.url) fallbackFrameReveal.value else 1f
                    val previewAlpha =
                        if (hasPreviewBridge) (1f - outgoingAlpha - incomingAlpha).coerceIn(0f, 1f) else 0f
                    if (hasPreviewBridge) {
                        AsyncImage(
                            model =
                                ImageRequest
                                    .Builder(LocalPlatformContext.current)
                                    .data(incomingVideoPreviewUrl)
                                    .diskCachePolicy(CachePolicy.ENABLED)
                                    .diskCacheKey(incomingVideoPreviewUrl)
                                    .build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().alpha(previewAlpha),
                            onSuccess = {
                                if (incomingVideoPreviewUrl != null) {
                                    canvasPreviewLoadResult = incomingVideoPreviewUrl to true
                                }
                            },
                            onError = {
                                if (incomingVideoPreviewUrl != null) {
                                    canvasPreviewLoadResult = incomingVideoPreviewUrl to false
                                    unavailableCanvasPreviewUrl = incomingVideoPreviewUrl
                                }
                            },
                        )
                    }
                }
                canvasVideoLayers.forEach { (videoData, isIncoming) ->
                    val videoProgress = displayedCanvasVideoAlpha.coerceIn(0f, 1f)
                    key(videoData.url) {
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .graphicsLayer {
                                        alpha =
                                            (if (isIncoming && previewOnlyCanvasVideoUrl == videoData.url) {
                                                fallbackFrameReveal.value
                                            } else if (isAnimatedToAnimatedHandoff) {
                                                if (incomingVideoPreviewUrl != null) {
                                                    if (isIncoming) incomingVideoHandoffAlpha(videoProgress)
                                                    else outgoingVideoHandoffAlpha(videoProgress)
                                                } else if (isIncoming) {
                                                    videoProgress
                                                } else {
                                                    1f - videoProgress
                                                }
                                            } else if (isIncoming) {
                                                videoProgress
                                            } else if (isRapidSongChange) {
                                                0f
                                            } else {
                                                1f - videoProgress
                                            }) * canvasTrackChangeAlpha.value
                                    }
                                    .then(
                                        if (keepAnimatedCoverEdgeConnected) {
                                            Modifier
                                        } else {
                                            Modifier.appleMusicVerticalFadeEdges(
                                                topFade = 0.dp,
                                                bottomFade = artworkBottomFade,
                                                smoothFade = true,
                                            )
                                        },
                                    ),
                        ) {
                            MediaPlayerView(
                                url = videoData.url,
                                cropToBounds = true,
                                useTextureView = true,
                                coverSurfaceUntilReady = false,
                                modifier = Modifier.fillMaxSize(),
                                onFirstFrameRendered =
                                    if (isIncoming) {
                                        { onCanvasVideoFrameRendered(videoData.url) }
                                    } else {
                                        null
                                    },
                            )
                        }
                    }
                }
            }
        }

        if (morphMediaType && (mediaMorphActive || (isVideoBackdrop && mediaVideoReveal.value < 1f))) {
            AppleMusicMediaViewportHandoff(
                localDensity = localDensity,
                bottomContentHeightDp = bottomContentHeightDp,
                boundedArtwork = boundedArtwork,
                videoFrameRatio = videoFrameRatio,
                landscapeVideoArea = landscapeVideoArea,
                artworkZoneHeightDp = artworkZoneHeightDp,
                viewportProgress = mediaViewportProgress.value,
                toVideo = isVideoBackdrop,
                outgoingArtwork = mediaHandoffArtwork,
                incomingArtwork = AppleMusicVideoEntryArtwork(songArtworkUrl, songArtworkBitmap),
                videoReveal = mediaVideoReveal.value,
            )
        }

        // Box, not Column: the real content and the idle overlay below must OVERLAP (Z-stack),
        // not lay out one after another — matches NowPlayingContentM3Expressive's exact structure
        // for this same canvas-unfocused-overlay pattern.
        if (!boundedArtwork) {
            Box(
                modifier =
                    Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(),
            ) {
                Column(
                    modifier =
                        Modifier
                            .alpha(controlsAlpha)
                            .onGloballyPositioned { coords ->
                                bottomContentHeightDp =
                                    with(localDensity) { coords.size.height.toDp().value.toInt() }
                            },
                ) {
                    Spacer(modifier = Modifier.height(sharedClusterHeight))
                }
                // For a regular video, this restores the player controls if they were hidden.
                if (isVideoBackdrop) {
                    AnimatedVisibility(
                        visible = !state.showControlLayout,
                        enter = fadeIn(),
                        exit = fadeOut(),
                    ) {
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(bottomContentHeightDp.dp)
                                    .clickable(
                                        onClick = { actions.onToggleControls() },
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() },
                                    ),
                            contentAlignment = Alignment.BottomStart,
                        ) {
                            // The page's OWN colour, not black. This scrim is what the artwork fades
                            // INTO, so it has to be the same tone the page is painted with below it —
                            // appleMusicGradientColorAt(seed, 1f), i.e. the thumbnail's colour darkened,
                            // exactly like the bottom stop of backdropBrush. Fading to black instead
                            // laid a dark slab over a tinted page and left a visible seam where the two
                            // met, which read as "black smudge into transparent".
                            //
                            // Fully opaque at the bottom for the same reason: at 0.85 the artwork still
                            // showed through the last few pixels and tinted the seam a different colour
                            // than the page continuing beneath it.
                            val pageScrimColor = appleMusicGradientColorAt(seedColor, 1f)
                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxSize()
                                        .background(
                                            smoothScrimBrush(
                                                from = pageScrimColor.copy(alpha = 0f),
                                                to = pageScrimColor,
                                            ),
                                        ),
                            )
                            Column(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            bottom =
                                                with(localDensity) {
                                                    WindowInsets.systemBars.getBottom(localDensity).toDp()
                                                } + 16.dp,
                                        ),
                            ) {
                                AnimatedVisibility(
                                    visible = state.currentLyricLineIndex > -1,
                                    enter = fadeIn() + expandVertically(),
                                    exit = fadeOut() + shrinkVertically(),
                                ) {
                                    val lineText =
                                        state.screenData.lyricsData
                                            ?.lyrics
                                            ?.lines
                                            ?.getOrNull(state.currentLyricLineIndex)
                                            ?.words
                                            ?.stripRichSyncTimestamps()
                                    if (!lineText.isNullOrBlank()) {
                                        Column(modifier = Modifier.fillMaxWidth()) {
                                            Text(
                                                text = lineText,
                                                style = typography.idleLyric,
                                                maxLines = 1,
                                                modifier =
                                                    Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 16.dp, vertical = 2.dp)
                                                        .basicMarquee(iterations = Int.MAX_VALUE, animationMode = MarqueeAnimationMode.Immediately)
                                                        .focusable(),
                                            )
                                            val translatedLineText =
                                                state.screenData.lyricsData
                                                    ?.translatedLyrics
                                                    ?.first
                                                    ?.lines
                                                    ?.getOrNull(state.currentLyricLineIndex)
                                                    ?.words
                                                    ?.stripRichSyncTimestamps()
                                            if (!translatedLineText.isNullOrBlank()) {
                                                Text(
                                                    text = translatedLineText,
                                                    style = typography.idleTranslated,
                                                    maxLines = 1,
                                                    modifier =
                                                        Modifier
                                                            .fillMaxWidth()
                                                            .padding(horizontal = 16.dp, vertical = 2.dp)
                                                            .basicMarquee(iterations = Int.MAX_VALUE, animationMode = MarqueeAnimationMode.Immediately)
                                                            .focusable(),
                                                )
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                // The DEFAULT style shows its full NowPlayingTrackInfoRow here — 55dp thumbnail
                                // and the ⊕ / ☆ / ⋯ actions. This is that row, not a stripped-down copy of it.
                                // It types with the COMPACT slots, not the main ones: this is a track header
                                // floating over a canvas, the same job the Lyrics/Queue header does.
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                                ) {
                                    Box(
                                        modifier =
                                            Modifier
                                                .size(55.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .then(sharedLyricsHeaderGestures)
                                                .then(
                                                    if (canvasData != null) {
                                                        Modifier.clickable { actions.onUIEvent(UIEvent.PlayPause) }
                                                    } else {
                                                        Modifier
                                                    },
                                                ),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        AsyncImage(
                                            model =
                                                ImageRequest
                                                    .Builder(LocalPlatformContext.current)
                                                    .data(state.currentTrackArtworkUrl())
                                                    .diskCachePolicy(CachePolicy.ENABLED)
                                                    .diskCacheKey(state.currentTrackArtworkUrl())
                                                    .crossfade(300)
                                                    .build(),
                                            placeholder = rememberHolderPainter(),
                                            error = rememberHolderPainter(),
                                            contentDescription = stringResource(Res.string.playback_toggle),
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize(),
                                        )
                                        Box(
                                            modifier =
                                                Modifier
                                                    .size(36.dp)
                                                    .alpha(coverPlaybackFeedbackAlpha)
                                                    .background(Color.Black.copy(alpha = 0.48f), CircleShape),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Icon(
                                                imageVector = if (state.controllerState.isPlaying) WaveIcons.PlayArrow else WaveIcons.Pause,
                                                contentDescription = if (state.controllerState.isPlaying) "Resumed playback" else "Playback paused",
                                                tint = Color.White,
                                                modifier = Modifier.size(22.dp),
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f).then(sharedLyricsHeaderGestures)) {
                                        Text(
                                            text = state.screenData.nowPlayingTitle,
                                            style = typography.compactTitle,
                                            maxLines = 1,
                                            modifier =
                                                Modifier
                                                    .fillMaxWidth()
                                                    .basicMarquee(iterations = Int.MAX_VALUE, animationMode = MarqueeAnimationMode.Immediately)
                                                    .focusable(),
                                        )
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = state.screenData.artistName,
                                            style = typography.compactArtist,
                                            maxLines = 1,
                                            modifier =
                                                Modifier
                                                    .fillMaxWidth()
                                                    .basicMarquee(iterations = Int.MAX_VALUE, animationMode = MarqueeAnimationMode.Immediately)
                                                    .focusable(),
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    AppleMusicHeaderActions(state = state, actions = actions)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class AppleMusicVideoEntryArtwork(
    val imageUrl: String?,
    val bitmap: ImageBitmap?,
)

/** One Compose viewport owns both the geometry and pixels of a media-type handoff. */
@Composable
private fun AppleMusicMediaViewportHandoff(
    localDensity: Density,
    bottomContentHeightDp: Int,
    artworkZoneHeightDp: Int,
    boundedArtwork: Boolean,
    videoFrameRatio: Float,
    landscapeVideoArea: AppleMusicLandscapeVideoArea?,
    viewportProgress: Float,
    toVideo: Boolean,
    outgoingArtwork: AppleMusicVideoEntryArtwork?,
    incomingArtwork: AppleMusicVideoEntryArtwork,
    videoReveal: Float,
) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val progress = viewportProgress.coerceIn(0f, 1f)
        val inset = if (boundedArtwork) 0.dp else 6.3.dp
        val topChrome = if (boundedArtwork) 0.dp else
            with(localDensity) { WindowInsets.statusBars.getTop(localDensity).toDp() } + VIDEO_FRAME_TOP_GAP
        val ratio = videoFrameRatio.takeIf { it.isFinite() && it > 0f }?.coerceAtMost(16f / 10f) ?: (16f / 10f)
        val videoAreaHeight = (maxHeight - bottomContentHeightDp.dp).coerceAtLeast(0.dp)
        val usableHeight = landscapeVideoArea?.height ?: (videoAreaHeight - topChrome).coerceAtLeast(0.dp)
        val usableWidth = landscapeVideoArea?.width ?: (maxWidth - inset * 2f).coerceAtLeast(0.dp)
        val videoHeight = minOf(usableWidth / ratio, usableHeight)
        val videoWidth = videoHeight * ratio
        val videoLeft = landscapeVideoArea?.left ?: (maxWidth - videoWidth) / 2f
        val videoTop = (landscapeVideoArea?.top ?: topChrome) + (usableHeight - videoHeight) / 2f
        val viewportWidth = maxWidth + (videoWidth - maxWidth) * progress
        val viewportHeight = artworkZoneHeightDp.dp + (videoHeight - artworkZoneHeightDp.dp) * progress
        val cornerRadius = 20.dp * progress
        val outgoingAlpha = if (toVideo) 1f - progress else progress
        Box(
            Modifier
                .offset(x = videoLeft * progress, y = videoTop * progress)
                .size(viewportWidth, viewportHeight)
                .graphicsLayer { alpha = if (toVideo) 1f - videoReveal else 1f }
                .clip(RoundedCornerShape(cornerRadius))
                .appleMusicVerticalFadeEdges(
                    topFade = 0.dp,
                    bottomFade = if (boundedArtwork) 0.dp else 240.dp * (1f - progress),
                    sideFade = if (boundedArtwork) 0.dp else 32.dp * (1f - progress),
                    smoothFade = true,
                )
                .background(Color.Black),
        ) {
            if (!toVideo) {
                if (incomingArtwork.bitmap != null) {
                    Image(incomingArtwork.bitmap, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    AsyncImage(
                        model = incomingArtwork.imageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        error = rememberHolderPainter(),
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
            if (outgoingArtwork != null && outgoingAlpha > 0f) {
                // The outgoing layer alone dissolves; the incoming image stays opaque beneath it.
                // Its black bars share that alpha, so the album cannot leak through them early.
                BoxWithConstraints(Modifier.fillMaxSize().graphicsLayer { alpha = outgoingAlpha }.background(Color.Black)) {
                    val contentScale = if (toVideo) ContentScale.Crop else ContentScale.Fit
                    if (outgoingArtwork.bitmap != null) {
                        val outgoingImageModifier = if (toVideo) {
                            Modifier.fillMaxSize()
                        } else {
                            // PixelCopy supplies a square-cornered buffer, without the native
                            // video's outline. Its fitted bounds lie inside the viewport's black
                            // bars, so the expanding viewport cannot clip those inner corners.
                            // Keep the video's own curve until its image has faded away.
                            val frameRatio = outgoingArtwork.bitmap.width.toFloat() / outgoingArtwork.bitmap.height
                            val frameWidth = minOf(maxWidth, maxHeight * frameRatio)
                            Modifier
                                .size(frameWidth, frameWidth / frameRatio)
                                .align(Alignment.Center)
                                .clip(RoundedCornerShape(20.dp))
                        }
                        Image(outgoingArtwork.bitmap, null, outgoingImageModifier, contentScale = contentScale)
                    } else {
                        AsyncImage(
                            model = outgoingArtwork.imageUrl,
                            contentDescription = null,
                            contentScale = contentScale,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
        }
    }
}

private data class AppleMusicBackdropArtwork(
    val imageUrl: String,
    val bitmap: ImageBitmap,
)

private data class AppleMusicArtworkPageTarget(
    val page: Int,
    val trackId: String?,
    val wasVideo: Boolean,
    val visualIdentity: String,
)

/**
 * The page backdrop: frosted cover art for songs and a theme-appropriate ambient surround for
 * video feeds. Shared with fullscreen lyrics landscape layout.
 */
@Composable
internal fun BoxScope.AppleMusicArtworkBackdrop(
    state: NowPlayingContentState,
    actions: NowPlayingContentActions,
    seedColor: Color,
    canvasThumbnailUrl: String? = null,
    preferCanvasThumbnail: Boolean = false,
    videoFrameColorState: State<Color?>? = null,
    songArtworkHeight: Dp? = null,
    landscapeArtwork: Boolean = false,
) {
    val reduceVisualEffects = LocalReduceVisualEffects.current
    val isVideoPage = state.isCurrentVideoItem()
    val videoAmbientColor by animateColorAsState(
        targetValue =
            videoFrameColorState?.value
                ?: if (getPlatform() == Platform.Android) Color.Black else seedColor,
        animationSpec = tween(durationMillis = 1_050, easing = FastOutSlowInEasing),
        label = "videoFrameAmbientColor",
    )
    // The artwork bitmap feeds BOTH the frosted backdrop below and the palette every colour on
    // this page is derived from. The artwork pager normally supplies it, but that pager only lives
    // in MAIN; this loader keeps palette updates working on QUEUE and LYRICS too.
    //
    // Keep the loader outside the page content and pair each request with its owner. URL-only
    // checks are insufficient when playback revisits a track during a rapid A→B→A handoff.
    val backdropArtworkUrl =
        canvasThumbnailUrl?.takeIf { preferCanvasThumbnail && it.isNotBlank() }
            ?: state.currentTrackArtworkUrl()
    var backdropTarget by remember(state.currentTrackId, backdropArtworkUrl) {
        mutableStateOf(state.currentTrackId to backdropArtworkUrl)
    }
    val backdropRequest = remember(state.currentTrackId, backdropArtworkUrl) { Any() }
    val latestBackdropRequest by rememberUpdatedState(backdropRequest)
    val latestBackdropTarget by rememberUpdatedState(backdropTarget)
    // The request may change immediately, but the visible bitmap changes only after success.
    // Keep the outgoing artwork alive during loading and crossfade between decoded bitmaps.
    var loadedBackdropArtwork by remember { mutableStateOf<AppleMusicBackdropArtwork?>(null) }
    val (trackId, imageUrl) = backdropTarget
    var backdropLoadFailed by remember(trackId, imageUrl) { mutableStateOf(false) }
    // Request ownership follows the track, but visual readiness follows the artwork. Reuse the
    // already-decoded cover immediately when consecutive songs share an album image.
    val songBackdropReady = imageUrl.isNullOrBlank() || backdropLoadFailed ||
        loadedBackdropArtwork?.imageUrl == imageUrl
    var showVideoBackdrop by remember { mutableStateOf(isVideoPage) }
    LaunchedEffect(isVideoPage, songBackdropReady) {
        // A song cannot replace the video surround until its image (or fallback) is ready.
        if (isVideoPage || songBackdropReady) showVideoBackdrop = isVideoPage
    }
    val videoBackdropAlpha by animateFloatAsState(
        targetValue = if (showVideoBackdrop) 1f else 0f,
        animationSpec = tween(if (reduceVisualEffects) 0 else 350, easing = FastOutSlowInEasing),
        label = "videoToArtworkBackdrop",
    )

    // The approved mock's page gradient is THREE stops — a clearly-tinted top, ~55%-darkened by
    // mid-page (48%), warm near-black at the bottom. The first cut's two stops to near-black read
    // as a flat black page on any dark artwork (first device screenshots).
    val backdropBrush =
        remember(seedColor) {
            Brush.verticalGradient(
                0f to appleMusicGradientColorAt(seedColor, 0f),
                0.48f to appleMusicGradientColorAt(seedColor, 0.48f),
                1f to appleMusicGradientColorAt(seedColor, 1f),
            )
        }

    // Avoid drawing a full-screen blur while the opaque video surround covers it completely.
    if (videoBackdropAlpha < 1f) {
        Crossfade(
            targetState = loadedBackdropArtwork,
            modifier = Modifier.fillMaxSize(),
            animationSpec = tween(if (reduceVisualEffects) 0 else 350, easing = FastOutSlowInEasing),
            label = "appleMusicBackdropArtwork",
        ) { artwork ->
            if (artwork != null) {
                // Blend the matching cover crop and its extended colors before one blur.
                // Actual video pages keep their existing surround and background rendering.
                Box(Modifier.fillMaxSize().blur(BACKDROP_BLUR_RADIUS, BlurredEdgeTreatment.Unbounded)) {
                    Image(
                        bitmap = artwork.bitmap,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                    // Keep the song crop intact beneath the video surround's entire fade.
                    // Removing it on the media-type switch exposes a differently cropped
                    // image before videoBackdropAlpha has had a frame to advance.
                    if (songArtworkHeight != null) {
                        Box(Modifier.matchParentSize().appleMusicSongColorContinuation(
                            bitmap = artwork.bitmap,
                            artworkHeight = songArtworkHeight,
                            landscape = landscapeArtwork,
                        ))
                    }
                }
            }
        }
        Box(modifier = Modifier.fillMaxSize().alpha(BACKDROP_TINT_ALPHA).background(backdropBrush))
    }

    // Fade this opaque surround only after the song's backdrop is already underneath it.
    if (showVideoBackdrop || videoBackdropAlpha > 0f) {
        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = videoBackdropAlpha }
                .background(videoPlaybackBackgroundColor())
                .then(if (!reduceVisualEffects) Modifier.videoAmbientGlow(videoAmbientColor) else Modifier),
        )
    }

    // Songs keep the frosted-cover treatment. For videos the same image request stays alive only
    // as a palette source; alpha-zero prevents any still frame from appearing behind the feed.
    // Keeping this loader outside the page content also refreshes the palette on Queue/Lyrics.
    if (!imageUrl.isNullOrBlank()) {
        key(trackId, imageUrl) {
            AsyncImage(
                model =
                    ImageRequest
                        .Builder(LocalPlatformContext.current)
                        .data(imageUrl)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .diskCacheKey(imageUrl + "BIGGER")
                        .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                onSuccess = {
                    if (latestBackdropRequest === backdropRequest && latestBackdropTarget == (trackId to imageUrl)) {
                        val bitmap = it.result.image.toImageBitmap()
                        // A video thumbnail supplies its palette, but must not replace the
                        // outgoing song underneath the still-fading opaque video surround.
                        if (!isVideoPage && loadedBackdropArtwork?.imageUrl != imageUrl) {
                            loadedBackdropArtwork = AppleMusicBackdropArtwork(imageUrl, bitmap)
                        }
                        // Returning to the held song still needs to restore its palette.
                        actions.onArtworkBitmap(bitmap)
                    }
                },
                // Same fallback the artwork pager carries: maxresdefault is missing for plenty of
                // videos, on which hqdefault is the available thumbnail.
                onError = {
                    val fallback = imageUrl.replace("maxresdefault", "hqdefault")
                    if (latestBackdropRequest === backdropRequest && latestBackdropTarget == (trackId to imageUrl)) {
                        if (fallback != imageUrl) {
                            backdropTarget = trackId to fallback
                        } else {
                            backdropLoadFailed = true
                        }
                    }
                },
                modifier = Modifier.fillMaxSize().alpha(0f),
            )
        }
    }
}

/** A diffuse live-frame glow behind the video area, fading back into the active theme background. */
private fun Modifier.videoAmbientGlow(seedColor: Color): Modifier =
    drawWithCache {
        // Scale the glow to the longer edge so portrait colors reach the full player too.
        val glowSpan = maxOf(size.width, size.height)
        val primaryColor = lerp(seedColor, Color.White, 0.08f)
        val secondaryColor = lerp(seedColor, Color(0xFF7468FF), 0.24f)
        val primaryGlow =
            Brush.radialGradient(
                0f to primaryColor.copy(alpha = 0.64f),
                0.42f to primaryColor.copy(alpha = 0.25f),
                1f to Color.Transparent,
                center = Offset(size.width * 0.5f, size.height * 0.24f),
                radius = glowSpan * 0.92f,
            )
        val secondaryGlow =
            Brush.radialGradient(
                0f to secondaryColor.copy(alpha = 0.28f),
                0.5f to secondaryColor.copy(alpha = 0.10f),
                1f to Color.Transparent,
                center = Offset(size.width * 0.86f, size.height * 0.32f),
                radius = glowSpan * 0.68f,
            )
        onDrawBehind {
            drawRect(primaryGlow)
            drawRect(secondaryGlow)
        }
    }

@Composable
@OptIn(ExperimentalLayoutApi::class)
internal fun AppleMusicMainTitleRow(
    state: NowPlayingContentState,
    actions: NowPlayingContentActions,
    typography: AppleMusicTypography,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Crossfade(
                targetState = state.screenData.nowPlayingTitle,
                animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
                label = "appleMusicTrackTitle",
            ) { title ->
                Text(
                    text = title,
                    style = typography.mainTitle,
                    maxLines = 1,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .basicMarquee(iterations = Int.MAX_VALUE, animationMode = MarqueeAnimationMode.Immediately)
                            .focusable(),
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (state.screenData.isExplicit) {
                    ExplicitBadge(modifier = Modifier.size(20.dp).padding(end = 4.dp))
                }
                if (state.artistLinks.isNotEmpty()) {
                    FlowRow {
                        state.artistLinks.forEachIndexed { index, (name, channelId) ->
                            if (index > 0) Text(text = ", ", style = typography.mainArtist)
                            Text(
                                text = name,
                                style = typography.mainArtist,
                                modifier = Modifier.clickable { actions.onNavigateToArtistId(channelId) },
                            )
                        }
                    }
                } else {
                    Text(
                        text = state.screenData.artistName,
                        style = typography.mainArtist,
                        maxLines = 1,
                        modifier = Modifier.clickable { actions.onNavigateToArtist() },
                    )
                }
            }
        }
    }
}

private fun String.requestLargePlayerArtwork(): String {
    // YouTube Music encodes requested dimensions at the end of googleusercontent thumbnail URLs
    // (for example `=w544-h544`). Preserve the source ratio and request enough pixels for a
    // full-screen cover. Leave opaque/non-resizable URLs untouched.
    val sizeSpec = Regex("=w(\\d+)-h(\\d+)([^/]*)$").find(this) ?: return this
    val width = sizeSpec.groupValues[1].toIntOrNull() ?: return this
    val height = sizeSpec.groupValues[2].toIntOrNull() ?: return this
    val scale = maxOf(1f, 1200f / maxOf(width, height))
    val requestedWidth = (width * scale).toInt()
    val requestedHeight = (height * scale).toInt()
    val replacement = "=w${requestedWidth}-h${requestedHeight}${sizeSpec.groupValues[3]}"
    return replaceRange(sizeSpec.range.first, sizeSpec.range.last + 1, replacement)
}

/** Rounds only the player's top corners, following the physical display corner radii during a pull. */
internal fun Modifier.pullDownTopCornerCurve(
    pullProgress: State<Float>?,
    topLeftRadius: Dp,
    topRightRadius: Dp,
): Modifier {
    if (pullProgress == null || (topLeftRadius <= 0.dp && topRightRadius <= 0.dp)) return this
    return drawWithCache {
        val progress = pullProgress.value.coerceIn(0f, 1f)
        val maxRadius = minOf(size.width, size.height) / 2f
        val leftRadius = (topLeftRadius.toPx() * progress).coerceAtMost(maxRadius)
        val rightRadius = (topRightRadius.toPx() * progress).coerceAtMost(maxRadius)
        if (leftRadius < 0.5f && rightRadius < 0.5f) {
            onDrawWithContent { drawContent() }
        } else {
            val path = Path().apply {
                moveTo(0f, leftRadius)
                quadraticTo(0f, 0f, leftRadius, 0f)
                lineTo(size.width - rightRadius, 0f)
                quadraticTo(size.width, 0f, size.width, rightRadius)
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }
            onDrawWithContent {
                clipPath(path) { this@onDrawWithContent.drawContent() }
            }
        }
    }
}

@Composable
private fun AppleMusicVideoFeed(
    state: NowPlayingContentState,
    localDensity: Density,
    bottomContentHeightDp: Int,
    boundedArtwork: Boolean,
    onVideoFrameColor: ((Color) -> Unit)?,
    videoExitProgress: Float,
    videoFrameRatio: Float,
    landscapeVideoArea: AppleMusicLandscapeVideoArea?,
    keepFrameTracking: Boolean,
    hasVideoHandoffCover: Boolean,
    onVideoFrameReady: (String) -> Unit,
    onVideoFrameSnapshot: (String, ImageBitmap) -> Unit,
) {
    val usePortraitVideoContainer = !boundedArtwork && getPlatform() == Platform.Android
    val frameSideInset = if (usePortraitVideoContainer) 6.3.dp else 0.dp
    val frameCornerRadius = if (boundedArtwork || usePortraitVideoContainer) 20.dp else 0.dp
    Box(Modifier.fillMaxSize()) {
        // Centre the video in the region ABOVE the controls (top → cluster), not in
        // the whole screen: screen-centred, half of a 16:9 video sat behind the
        // control cluster and its subtitles landed on the dock.
        // Wide videos share a 16:10 frame with thin top/bottom bars. Taller videos
        // keep their own shape, fitted below the status bar and Desktop dismiss button,
        // with a gap so their fullscreen button stays clear of system chrome.
        val topChrome = if (boundedArtwork) 0.dp else
            with(localDensity) { WindowInsets.statusBars.getTop(localDensity).toDp() } +
                (if (getPlatform() == Platform.Desktop) DISMISS_BUTTON_TOP_INSET + DISMISS_BUTTON_SIZE else 0.dp) +
                VIDEO_FRAME_TOP_GAP
        BoxWithConstraints(
            modifier =
                Modifier
                    .align(Alignment.TopCenter)
                    // The page's own height minus the controls, not artworkZoneHeightDp:
                    // that one is measured against the window, and the Desktop side panel
                    // is shorter than the window by its shell padding, which pushed a tall
                    // frame down into the gap above the title.
                    .fillMaxSize()
                    .padding(
                        start = frameSideInset,
                        end = frameSideInset,
                        bottom = bottomContentHeightDp.dp,
                    ),
        ) {
            val ratio = videoFrameRatio
                .takeIf { it.isFinite() && it > 0f }
                ?.coerceAtMost(16f / 10f)
                ?: (16f / 10f)
            val usableVideoHeight = landscapeVideoArea?.height ?: (maxHeight - topChrome).coerceAtLeast(0.dp)
            val usableVideoWidth = landscapeVideoArea?.width ?: maxWidth
            val targetFrameHeight = minOf(usableVideoWidth / ratio, usableVideoHeight)
            val frameHeight = targetFrameHeight.coerceAtMost(usableVideoHeight)
            val frameWidth = (targetFrameHeight * ratio).coerceAtMost(usableVideoWidth)
            // Center within the safe space from the status bar to the title/control
            // block. Centering from y=0 biased the video upward by half the top inset.
            val frameTop = (landscapeVideoArea?.top ?: topChrome) + (usableVideoHeight - frameHeight) / 2
            // Keep the decoded video and its subtitle inside an AMOLED-black frame
            // with black bars around videos that have a different aspect ratio.
            Box(
                modifier =
                    Modifier
                        .then(
                            if (landscapeVideoArea != null) {
                                Modifier.offset(x = landscapeVideoArea.left, y = frameTop)
                            } else {
                                Modifier.align(Alignment.TopCenter).padding(top = frameTop)
                            },
                        )
                        .size(width = frameWidth, height = frameHeight)
                        // Both phone orientations use an inset card. Round the frame
                        // and native video surface together, including the black bars.
                        .then(if (frameCornerRadius > 0.dp) Modifier.clip(RoundedCornerShape(frameCornerRadius)) else Modifier)
                        // The renderer owns the fading letterbox background as well as its pixels.
                        .then(if (!boundedArtwork && !usePortraitVideoContainer && ratio < 1f) Modifier.clip(RoundedCornerShape(12.dp)) else Modifier),
            ) {
                MediaPlayerViewWithSubtitle(
                    playerName = MAIN_PLAYER,
                    // Keep the live video and frozen handoff overlay in the same viewport.
                    // The renderer fits the actual video inside it without stretching.
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .align(Alignment.Center),
                    fitVideoToBounds = true,
                    shouldShowSubtitle = false,
                    shouldPip = false,
                    shouldScaleDownSubtitle = true,
                    timelineState = state.timelineState,
                    lyricsData = state.screenData.lyricsData?.lyrics,
                    translatedLyricsData = state.screenData.lyricsData?.translatedLyrics?.first,
                    isInPipMode = state.isInPipMode,
                    mainTextStyle = typo().bodyLarge,
                    translatedTextStyle = typo().bodyMedium,
                    onVideoFrameColor = onVideoFrameColor,
                    freezeVideoFrameOnExit = true,
                    // SurfaceView uses its own compositor layer, so the parent's Compose alpha
                    // alone cannot perform the video-to-lyrics fade.
                    videoExitProgress = videoExitProgress,
                    isUiVisible = state.isExpanded && (videoExitProgress < 1f || keepFrameTracking),
                    videoCornerRadius = frameCornerRadius,
                    // The full-size artwork page owns audio covers. The video renderer's fallback
                    // otherwise flashes the incoming cover inside the outgoing video's small frame.
                    showAudioArtwork = false,
                    hasVideoHandoffCover = hasVideoHandoffCover,
                    onVideoFrameReady = onVideoFrameReady,
                    onVideoFrameSnapshot = onVideoFrameSnapshot,
                )
            }
        }
    }
}

/**
 * One pager page. The current page supplies the artwork surface; the persistent blurred backdrop
 * is the single owner of palette image callbacks. Adjacent pages show a bounded static thumbnail.
 */
@Composable
private fun AppleMusicArtworkPage(
    state: NowPlayingContentState,
    actions: NowPlayingContentActions,
    localDensity: Density,
    page: Int,
    pageTrackId: String?,
    pageWasVideo: Boolean,
    artworkZoneHeightDp: Int,
    bottomContentHeightDp: Int,
    isVideoBackdrop: Boolean,
    canvasTransitionBlur: Float,
    songHandoffBlurRadius: Float,
    artworkBottomFade: Dp,
    canvasEntryAlpha: Float,
    canvasData: NowPlayingScreenData.CanvasData?,
    transitionCanvasVideoData: NowPlayingScreenData.CanvasData?,
    canvasTrackChangeAlpha: Float,
    canvasVideoAlpha: Float,
    outgoingCanvasVideoData: NowPlayingScreenData.CanvasData?,
    previewOnlyCanvasVideoUrl: String?,
    fallbackFrameReveal: Float,
    isRapidSongChange: Boolean,
    onVideoFrameColor: ((Color) -> Unit)?,
    onCurrentArtworkReady: () -> Unit,
    boundedArtwork: Boolean = false,
) {
    // An outgoing music video never becomes a full-bleed static poster during a track fade.
    if (pageWasVideo) return
    val sharedViewModel: SharedViewModel = koinInject()
    val pageTrack = state.artworkQueue.getOrNull(page)
    val currentArtworkTrackId = state.currentTrackId
    val currentTrackArtworkUrl = state.currentTrackArtworkUrl()
    // AnimatedContent retains this page after a track change. Keep the artwork URL captured for
    // this page instead of replacing it with the incoming track's URL during the outgoing fade.
    var artworkUrl by remember(pageTrackId) {
        mutableStateOf(currentTrackArtworkUrl.takeIf { pageTrackId == currentArtworkTrackId })
    }
    LaunchedEffect(pageTrackId, currentArtworkTrackId, currentTrackArtworkUrl) {
        if (pageTrackId == currentArtworkTrackId && !currentTrackArtworkUrl.isNullOrBlank()) {
            artworkUrl = currentTrackArtworkUrl
        }
    }
    val cachedPageArtworkBitmap =
        sharedViewModel.cachedArtworkBitmap(pageTrackId)
            ?: sharedViewModel.cachedArtworkBitmap(artworkUrl)
    // AnimatedContent can retain a page after the queue index changes. Treat that page as current
    // only when its captured track identity still matches playback; an unmatched queue fallback
    // must never promote an unrelated favorite to the full-screen cover.
    val isCurrentPage =
        page == state.currentOrderIndex &&
            !pageTrackId.isNullOrBlank() &&
            pageTrackId == currentArtworkTrackId
    val pageShowsCanvasOrVideo =
        isCurrentPage && (canvasData != null || isVideoBackdrop)
    // When AnimatedContent retains the outgoing page, its static thumbnail must not sit on top of
    // that page's still-running animated-cover surface. Otherwise the video appears to stop and
    // revert to its static thumbnail during the handoff fade.
    val isOutgoingAnimatedCoverPage =
        !isCurrentPage &&
            (outgoingCanvasVideoData?.videoId?.let { it == pageTrack?.videoId } == true ||
                transitionCanvasVideoData?.videoId?.let { it == pageTrack?.videoId } == true)
    val isAnimatedToAnimatedHandoff =
        outgoingCanvasVideoData?.isVideo == true && transitionCanvasVideoData?.isVideo == true
    val keepIncomingPreview = previewOnlyCanvasVideoUrl == transitionCanvasVideoData?.url
    val incomingVideoAlpha =
        if (isAnimatedToAnimatedHandoff) {
            incomingVideoHandoffAlpha(canvasVideoAlpha) * if (keepIncomingPreview) fallbackFrameReveal else 1f
        } else {
            canvasVideoAlpha * if (keepIncomingPreview) fallbackFrameReveal else 1f
        }
    // Reveal the next still cover in sync with the outgoing animated layer. Keeping it fully
    // opaque under the outgoing video made the transition look like the animation snapped back
    // to a static cover on every song change.
    val stillArtworkIncomingAlpha =
        if (isOutgoingAnimatedCoverPage) {
            0f
        } else if (isCurrentPage && transitionCanvasVideoData != null && canvasData?.isVideo != true) {
            1f - canvasTrackChangeAlpha
        } else {
            1f
        }
    val stillCanvasEntryAlpha =
        if (isCurrentPage && transitionCanvasVideoData != null && canvasData?.isVideo != true) {
            stillArtworkIncomingAlpha
        } else {
            canvasEntryAlpha
        }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isCurrentPage) {
            // Reset the request with the track so a previous song's cover cannot linger while the
            // new artwork loads. A track change should show its own artwork (or the placeholder).
            // The pager shows this image as soon as an adjacent page enters the viewport. Ask
            // YouTube's resizable thumbnail endpoint for a larger rendition up front, so a
            // low-resolution queue thumbnail is not enlarged during the swipe and then replaced
            // by the full player artwork after the track changes.
            val staticThumb =
                pageTrack
                    ?.takeIf { it.videoId == pageTrackId }
                    ?.thumbnails
                    ?.maxByOrNull { it.width * it.height }
                    ?.url
                    ?.requestLargePlayerArtwork()
            // A cached animated cover also has a cached preview frame. Use that frame while the
            // video decoder warms up instead of briefly showing the unrelated album artwork.
            val animatedPreviewData =
                canvasData?.takeIf { it.isVideo }
                    ?: transitionCanvasVideoData?.takeIf {
                        isAnimatedToAnimatedHandoff && isCurrentPage
                    }
            val hasAnimatedPreview = canvasData?.isVideo == true || animatedPreviewData != null
            val artworkPlaceholder = animatedPreviewData?.thumbnailUrl ?: staticThumb
            val requestedArtworkUrl = artworkUrl
            LaunchedEffect(cachedPageArtworkBitmap, isCurrentPage) {
                if (cachedPageArtworkBitmap != null && isCurrentPage) onCurrentArtworkReady()
            }
            Box(
                modifier =
                    Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .height(artworkZoneHeightDp.dp)
                        .then(
                            // Keep the temporary softening for animated previews only. On ordinary
                            // covers, fading this blur away after the timed handoff looks like a
                            // final fog layer popping off—especially when the incoming image is uncached.
                            if (songHandoffBlurRadius > 0f) {
                                Modifier.blur(songHandoffBlurRadius.dp, BlurredEdgeTreatment.Rectangle)
                            } else {
                                Modifier
                            },
                        )
                        .alpha(
                            if (isVideoBackdrop) {
                                0f
                            } else if (hasAnimatedPreview) {
                                // The cached frame belongs to the incoming animation, so it can
                                // take over immediately while its decoder produces a live frame.
                                // This avoids holding the outgoing song (and its colors) on screen
                                // for the decoder's startup time.
                                if (keepIncomingPreview) {
                                    1f - fallbackFrameReveal
                                } else {
                                    1f - (if (isAnimatedToAnimatedHandoff) incomingVideoAlpha else canvasVideoAlpha)
                                }
                            } else {
                                stillArtworkIncomingAlpha
                            },
                        )
                        // Mask the complete cover once. A placeholder and decoded cover
                        // with separate masks brighten each other's translucent edges,
                        // then snap darker when the single handoff viewport takes over.
                        .appleMusicVerticalFadeEdges(
                            topFade = 0.dp,
                            bottomFade = artworkBottomFade,
                            sideFade = if (boundedArtwork) 0.dp else 32.dp,
                            smoothFade = true,
                        ),
            ) {
                // During A→A, the cached still is composed together with both video surfaces
                // above, then edge-faded as one unit. Keeping it in this page would give it a
                // separate mask and expose the backdrop between the independently faded layers.
                if (!isAnimatedToAnimatedHandoff && !artworkPlaceholder.isNullOrBlank() &&
                    (hasAnimatedPreview || cachedPageArtworkBitmap == null)
                ) {
                    AsyncImage(
                        model =
                            ImageRequest
                                .Builder(LocalPlatformContext.current)
                                .data(artworkPlaceholder)
                                .diskCachePolicy(CachePolicy.ENABLED)
                                .diskCacheKey(artworkPlaceholder)
                                .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier =
                            Modifier
                                .fillMaxSize(),
                    )
                }
                if (canvasData?.isVideo != true) {
                    if (cachedPageArtworkBitmap != null) {
                        Image(
                            bitmap = cachedPageArtworkBitmap,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .alpha(if (isVideoBackdrop) 0f else 1f),
                        )
                    } else key(state.currentTrackId, requestedArtworkUrl) {
                        AsyncImage(
                            model =
                                ImageRequest
                                    .Builder(LocalPlatformContext.current)
                                    .data(requestedArtworkUrl)
                                    .diskCachePolicy(CachePolicy.ENABLED)
                                    .diskCacheKey(requestedArtworkUrl + "BIGGER")
                                    .build(),
                            contentDescription = "",
                            onError = {
                                val fallback = requestedArtworkUrl?.replace("maxresdefault", "hqdefault")
                                if (
                                    artworkUrl == requestedArtworkUrl &&
                                    fallback != null &&
                                    fallback != requestedArtworkUrl
                                ) {
                                    artworkUrl = fallback
                                } else if (isCurrentPage && artworkUrl == requestedArtworkUrl) {
                                    onCurrentArtworkReady()
                                }
                            },
                            onSuccess = { result ->
                                requestedArtworkUrl?.let {
                                    sharedViewModel.cacheArtworkBitmap(it, result.result.image.toImageBitmap())
                                }
                                pageTrackId?.let {
                                    sharedViewModel.cacheArtworkBitmap(it, result.result.image.toImageBitmap())
                                }
                                if (isCurrentPage && artworkUrl == requestedArtworkUrl) {
                                    onCurrentArtworkReady()
                                }
                            },
                            contentScale = ContentScale.Crop,
                            error = rememberHolderPainter(),
                            // The artwork DISSOLVES (alpha mask) instead of being covered by a colour
                            // overlay: that overlay had to land on exactly the page gradient's colour at
                            // that Y, and any drift drew a hard horizontal line across the screen.
                            // Masking lets the real background show through — nothing left to match.
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    // Crossfade the still artwork out as the first decoded canvas frame
                                    // fades in; a permanently opaque still image makes the video appear
                                    // to pop over it even though the video itself is animating.
                                    .alpha(if (isVideoBackdrop) 0f else 1f),
                        )
                    }
                }
            }
            if (pageShowsCanvasOrVideo) {
                if (isVideoBackdrop) {
                    // The stable video feed is drawn outside the retained artwork pages.
                } else {
                    // Match the animated artwork's viewport to a static cover: same top artwork
                    // zone, crop behavior, and bottom fade. Keeping it full-screen made the art
                    // look zoomed compared with every non-animated cover.
                    Box(
                        modifier =
                            Modifier
                                .align(Alignment.TopCenter)
                                .fillMaxWidth()
                                .height(artworkZoneHeightDp.dp)
                                .alpha(
                                    if (canvasData?.isVideo == true) {
                                        canvasVideoAlpha * canvasTrackChangeAlpha
                                    } else {
                                        stillCanvasEntryAlpha
                                    },
                                )
                                .then(
                                    if (canvasTransitionBlur > 0f) {
                                        Modifier.blur(canvasTransitionBlur.dp, BlurredEdgeTreatment.Rectangle)
                                    } else {
                                        Modifier
                                    },
                                )
                                .appleMusicVerticalFadeEdges(topFade = 0.dp, bottomFade = if (boundedArtwork) 0.dp else 300.dp, smoothFade = true),
                    ) {
                        if (canvasData?.isVideo != true) {
                            canvasData?.url?.let { canvasUrl ->
                                AsyncImage(
                                    model =
                                        ImageRequest
                                            .Builder(LocalPlatformContext.current)
                                            .data(canvasUrl)
                                            .diskCachePolicy(CachePolicy.ENABLED)
                                            .diskCacheKey(canvasUrl)
                                            .build(),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }
                        }
                    }
                }
            }
        } else if (
            cachedPageArtworkBitmap != null || !artworkUrl.isNullOrBlank() ||
                (pageTrack != null && pageTrack.videoId == pageTrackId)
        ) {
            val staticThumb =
                pageTrack
                    ?.takeIf { it.videoId == pageTrackId }
                    ?.thumbnails
                    ?.maxByOrNull { it.width * it.height }
                    ?.url
                    ?.requestLargePlayerArtwork()
            Box(
                modifier =
                    Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .height(artworkZoneHeightDp.dp)
                        .alpha(if (isOutgoingAnimatedCoverPage) 0f else 1f),
            ) {
                if (cachedPageArtworkBitmap != null) {
                    Image(
                        bitmap = cachedPageArtworkBitmap,
                        contentDescription = pageTrack?.title,
                        contentScale = ContentScale.Crop,
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .appleMusicVerticalFadeEdges(
                                    topFade = 0.dp,
                                    bottomFade = artworkBottomFade,
                                    sideFade = if (boundedArtwork) 0.dp else 32.dp,
                                    smoothFade = true,
                                ),
                    )
                } else {
                    val outgoingArtworkUrl = artworkUrl ?: staticThumb
                    AsyncImage(
                        model =
                            ImageRequest
                                .Builder(LocalPlatformContext.current)
                                .data(outgoingArtworkUrl)
                                .diskCachePolicy(CachePolicy.ENABLED)
                                .diskCacheKey(outgoingArtworkUrl)
                                .crossfade(300)
                                .build(),
                        contentDescription = pageTrack?.title,
                        contentScale = ContentScale.Crop,
                        placeholder = rememberHolderPainter(),
                        error = rememberHolderPainter(),
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .appleMusicVerticalFadeEdges(
                                    topFade = 0.dp,
                                    bottomFade = artworkBottomFade,
                                    sideFade = if (boundedArtwork) 0.dp else 32.dp,
                                    smoothFade = true,
                                ),
                    )
                }
            }
        }
    }
}

// Radius of the frosted cover art behind the page. Large enough that no detail of the artwork
// survives as a shape — what is left is its colour and its broad light and dark areas, which is
// precisely what Apple's background is.
private val BACKDROP_BLUR_RADIUS = 80.dp
private const val VIDEO_LYRICS_TRANSITION_DURATION_MILLIS = 380
private const val VIDEO_LYRICS_ENTRY_DELAY_MILLIS = 80

// Desktop's glass dismiss button in the panel's top-left corner; a tall video frame starts below
// it so the fullscreen button does not land under the button.
private val DISMISS_BUTTON_INSET = 12.dp
private val DISMISS_BUTTON_TOP_INSET = 8.dp
private val DISMISS_BUTTON_SIZE = 48.dp

// Breathing room between that top chrome and a tall video frame.
private val VIDEO_FRAME_TOP_GAP = 16.dp

// A brief artwork-only blur softens decoder/image swaps without changing the handoff timing.
// Animated-to-animated changes scale this value by 1.5 to cover their more noticeable frame seam.
private const val SONG_HANDOFF_BLUR_RADIUS_DP = 8f
private const val ANIMATED_HANDOFF_PREROLL_BLUR_DP = 4f

// Stage an animated-to-animated handoff: the outgoing clip fades into the incoming preview first,
// then the already-running incoming video is revealed. A rapid A→B→A reversal retraces the curve.
private const val ANIMATED_HANDOFF_VIDEO_REVEAL_START = 0.34f

private fun incomingVideoHandoffAlpha(progress: Float): Float =
    smoothStep((progress - ANIMATED_HANDOFF_VIDEO_REVEAL_START) / (1f - ANIMATED_HANDOFF_VIDEO_REVEAL_START))

private fun outgoingVideoHandoffAlpha(progress: Float): Float =
    1f - smoothStep(progress / ANIMATED_HANDOFF_VIDEO_REVEAL_START)

private fun smoothStep(progress: Float): Float {
    val normalized = progress.coerceIn(0f, 1f)
    return normalized * normalized * (3f - 2f * normalized)
}

private const val BACKDROP_TINT_ALPHA = 0.38f

// Crossfade the outgoing canvas over the incoming cover when a track has no matching canvas.
// How long the LYRICS tab waits for a track's lyrics before deciding the track has none. Long
// enough to cover a normal fetch on a normal connection, short enough that a song with no lyrics
// does not leave the user staring at an empty page. Not a fixed budget for the request itself:
// lyrics arriving at any point cancel the wait outright.
