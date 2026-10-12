@file:OptIn(ExperimentalMaterial3Api::class)

package com.petedev.wavemusic.ui.screen.player

import androidx.compose.animation.Animatable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.kmpalette.rememberPaletteState
import com.petedev.wavemusic.common.Config.MAIN_PLAYER
import com.petedev.wavemusic.domain.extension.activeVideoId
import com.petedev.wavemusic.domain.extension.isVideo
import com.petedev.wavemusic.domain.mediaservice.handler.MediaPlayerHandler
import com.petedev.wavemusic.domain.manager.DataStoreManager
import com.petedev.wavemusic.domain.repository.PodcastRepository
import com.petedev.wavemusic.logger.Logger
import com.petedev.wavemusic.Platform
import com.petedev.wavemusic.expect.ui.rememberVideoAspectRatio
import com.petedev.wavemusic.expect.ui.PredictiveBackHandler
import com.petedev.wavemusic.expect.ui.rememberTopDisplayCornerRadii
import com.petedev.wavemusic.extension.GradientAngle
import com.petedev.wavemusic.extension.GradientOffset
import com.petedev.wavemusic.extension.KeepScreenOn
import com.petedev.wavemusic.extension.getColorFromPalette
import com.petedev.wavemusic.extension.getScreenSizeInfo
import com.petedev.wavemusic.extension.hsvToColor
import com.petedev.wavemusic.extension.rememberIsInPipMode
import com.petedev.wavemusic.getPlatform
import com.petedev.wavemusic.ui.component.AddToPlaylistModalBottomSheet
import com.petedev.wavemusic.ui.component.withLyricsTranslation
import com.petedev.wavemusic.ui.component.InfoPlayerBottomSheet
import com.petedev.wavemusic.ui.component.NowPlayingBottomSheet
import com.petedev.wavemusic.ui.component.QueueBottomSheet
import com.petedev.wavemusic.ui.component.VoteLyricsDialog
import com.petedev.wavemusic.ui.icon.KeyboardArrowDown
import com.petedev.wavemusic.ui.icon.WaveIcons
import com.petedev.wavemusic.ui.navigation.destination.list.ArtistDestination
import com.petedev.wavemusic.ui.navigation.destination.list.PodcastDestination
import com.petedev.wavemusic.ui.navigation.destination.player.FullscreenDestination
import com.petedev.wavemusic.ui.screen.player.content.NowPlayingContentActions
import com.petedev.wavemusic.ui.screen.player.content.NowPlayingContentAppleMusic
import com.petedev.wavemusic.ui.screen.player.content.NowPlayingContentState
import com.petedev.wavemusic.ui.screen.player.content.pullDownTopCornerCurve
import com.petedev.wavemusic.viewModel.shouldOfferLyricsTranslation
import com.petedev.wavemusic.ui.screen.player.content.PlayerBackdropColor
import com.petedev.wavemusic.viewModel.LyricsProvider
import com.petedev.wavemusic.viewModel.NowPlayingBottomSheetUIEvent
import com.petedev.wavemusic.viewModel.NowPlayingBottomSheetViewModel
import com.petedev.wavemusic.viewModel.SharedViewModel
import com.petedev.wavemusic.viewModel.UIEvent
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.abs

private const val TAG = "NowPlayingScreen"

// Desktop's fullscreen lyrics page: in a little slower than out, so opening reads as arriving and
// closing gets out of the way.
private const val FULLSCREEN_LYRICS_ENTER_MS = 300
private const val FULLSCREEN_LYRICS_EXIT_MS = 220
// Progress translates an entire screen: the default Float threshold (0.01) can
// finish with ~20px left to travel. Settle below a pixel even on a 4K display.
private const val PLAYER_PROGRESS_VISIBILITY_THRESHOLD = 0.0001f

internal fun playerCollapseSpringSpec() =
    spring<Float>(
        dampingRatio = 1f,
        stiffness = 180f,
        visibilityThreshold = PLAYER_PROGRESS_VISIBILITY_THRESHOLD,
    )

internal fun playerExpandSpringSpec() =
    spring<Float>(
        dampingRatio = 1f,
        stiffness = 180f,
        visibilityThreshold = PLAYER_PROGRESS_VISIBILITY_THRESHOLD,
    )

private fun playerDragReturnSpringSpec() = spring<Float>(
    dampingRatio = 1f,
    stiffness = 220f,
    visibilityThreshold = PLAYER_PROGRESS_VISIBILITY_THRESHOLD,
)

// Direct manipulation must follow the finger before handing off to the settling spring.
// Squaring a small pull nearly hid it, then releasing launched a full-screen collapse.
internal fun predictiveBackCurve(progress: Float): Float = progress.coerceIn(0f, 1f)

internal fun playerDockAlpha(progress: Float): Float {
    val remaining = 1f - progress.coerceIn(0f, 1f)
    return remaining * remaining * (3f - 2f * remaining)
}

/** Moves the retained player with a single layer, avoiding a full-screen fade/mask buffer. */
private fun Modifier.playerTransitionSlide(progress: State<Float>): Modifier =
    graphicsLayer {
        val visibility = progress.value.coerceIn(0f, 1f)
        translationY = size.height * (1f - visibility)
    }

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NowPlayingScreen(
    sharedViewModel: SharedViewModel = koinInject(),
    navController: NavController,
    swipeTransitionProgress: State<Float>,
    isOnTop: Boolean,
    isClosing: Boolean = false,
    onReverseClose: () -> Unit = {},
    onPredictiveBackProgress: (Float) -> Unit = {},
    onPredictiveBackCancelled: () -> Unit = {},
    onPredictiveBackCommitted: () -> Unit = {},
    onDismiss: () -> Unit = {},
) {
    val coroutineScope = rememberCoroutineScope()
    val (topLeftCornerRadius, topRightCornerRadius) = rememberTopDisplayCornerRadii()
    val predictiveBackProgress = remember { mutableFloatStateOf(0f) }
    val currentIsClosing by rememberUpdatedState(isClosing)
    val currentOnReverseClose by rememberUpdatedState(onReverseClose)
    val gesturePullProgress = remember(predictiveBackProgress) {
        derivedStateOf {
            predictiveBackCurve(predictiveBackProgress.floatValue)
        }
    }
    // Top corners follow pulls, predictive back, and the full open/close transition for both
    // cover and lyrics views.
    val topCornerProgress = remember(gesturePullProgress, swipeTransitionProgress) {
        derivedStateOf {
            maxOf(
                gesturePullProgress.value,
                (1f - swipeTransitionProgress.value).coerceIn(0f, 1f),
            )
        }
    }
    LaunchedEffect(isOnTop) {
        if (!isOnTop) predictiveBackProgress.floatValue = 0f
    }

    PredictiveBackHandler(
        // Once a close has committed, let the next system back reach the screen underneath
        // while this surface finishes its collapse animation. Keeping this handler enabled
        // would replay the same close gesture from its current progress.
        enabled = isOnTop && !isClosing,
        onProgress = {
            predictiveBackProgress.floatValue = it.coerceIn(0f, 1f)
            onPredictiveBackProgress(predictiveBackCurve(it))
        },
        onCancelled = {
            animate(
                initialValue = predictiveBackProgress.floatValue,
                targetValue = 0f,
                animationSpec = playerDragReturnSpringSpec(),
            ) { value, _ ->
                predictiveBackProgress.floatValue = value
                onPredictiveBackProgress(predictiveBackCurve(value))
            }
            onPredictiveBackCancelled()
        },
        onBack = {
            // Hand the current gesture position to the screen transition. The screen then
            // closes once from that point and immediately releases this back handler so the
            // next system back reaches the page underneath.
            onPredictiveBackCommitted()
        },
    )

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                // Keep motion to translation plus the lightweight corner clip; fading the whole
                // lyrics surface required an expensive full-screen compositing buffer.
                .playerTransitionSlide(progress = swipeTransitionProgress)
                .pullDownTopCornerCurve(
                    pullProgress = topCornerProgress,
                    topLeftRadius = topLeftCornerRadius,
                    topRightRadius = topRightCornerRadius,
                )
                // Leave the expanded player composed behind the app while collapsed: this keeps
                // its animated-cover player alive without intercepting taps on the mini-player.
                .zIndex(if (isOnTop) 1f else -1f)
                .pointerInput(Unit) {
                    var hasStartedPullDown = false
                    var hasReversedClose = false
                    var draggedDistancePx = 0f
                    var pullStartProgress = 0f
                    detectVerticalDragGestures(
                        onDragStart = {
                            hasStartedPullDown = false
                            hasReversedClose = false
                            draggedDistancePx = 0f
                            pullStartProgress = 0f
                        },
                        onVerticalDrag = { change, dragAmount ->
                            if (currentIsClosing && dragAmount < 0f && !hasReversedClose) {
                                // An upward pull reverses an in-flight close from its current
                                // spring position, just like tapping the mini-player.
                                change.consume()
                                hasReversedClose = true
                                currentOnReverseClose()
                            } else if (dragAmount > 0f || predictiveBackProgress.floatValue > 0f) {
                                change.consume()
                                val dragRange = size.height.coerceAtLeast(1).toFloat()
                                if (dragAmount > 0f && !hasStartedPullDown) {
                                    // A pull can begin while the tap-open spring is still moving.
                                    // Start gesture progress at the exact on-screen position so
                                    // taking over from the spring does not snap the player.
                                    val collapsedAmount =
                                        (1f - swipeTransitionProgress.value).coerceIn(0f, 1f)
                                    predictiveBackProgress.floatValue = collapsedAmount
                                    pullStartProgress = predictiveBackProgress.floatValue
                                    hasStartedPullDown = true
                                }
                                if (hasStartedPullDown) {
                                    draggedDistancePx = (draggedDistancePx + dragAmount).coerceAtLeast(0f)
                                }
                                val progress =
                                    (predictiveBackProgress.floatValue + dragAmount * MINI_PLAYER_VERTICAL_DRAG_GAIN / dragRange)
                                    .coerceIn(0f, 1f)
                                predictiveBackProgress.floatValue = progress
                                onPredictiveBackProgress(predictiveBackCurve(progress))
                            }
                        },
                        onDragEnd = {
                            coroutineScope.launch {
                                if (!hasStartedPullDown) {
                                    return@launch
                                }
                                if (playerPullCrossedThreshold(draggedDistancePx)) {
                                    onPredictiveBackCommitted()
                                } else {
                                    animate(
                                        initialValue = predictiveBackProgress.floatValue,
                                        targetValue = pullStartProgress,
                                        animationSpec = playerDragReturnSpringSpec(),
                                    ) { value, _ ->
                                        predictiveBackProgress.floatValue = value
                                        onPredictiveBackProgress(predictiveBackCurve(value))
                                    }
                                    onPredictiveBackCancelled()
                                }
                                hasStartedPullDown = false
                                draggedDistancePx = 0f
                            }
                        },
                        onDragCancel = {
                            coroutineScope.launch {
                                if (!hasStartedPullDown) {
                                    return@launch
                                }
                                animate(
                                    initialValue = predictiveBackProgress.floatValue,
                                    targetValue = pullStartProgress,
                                    animationSpec = playerDragReturnSpringSpec(),
                                ) { value, _ ->
                                    predictiveBackProgress.floatValue = value
                                    onPredictiveBackProgress(predictiveBackCurve(value))
                                }
                                onPredictiveBackCancelled()
                                hasStartedPullDown = false
                                draggedDistancePx = 0f
                            }
                        },
                    )
                },
    ) {
        com.petedev.wavemusic.ui.component.RetainedPlayerContent(active = isOnTop) {
            NowPlayingScreenContent(
                sharedViewModel = sharedViewModel,
                navController = navController,
                isExpanded = true,
                dismissIcon = WaveIcons.KeyboardArrowDown,
                onDismiss = onDismiss,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NowPlayingScreenContent(
    sharedViewModel: SharedViewModel = koinInject(),
    mediaPlayerHandler: MediaPlayerHandler = koinInject(),
    dataStoreManager: DataStoreManager = koinInject(),
    navController: NavController,
    isExpanded: Boolean,
    dismissIcon: ImageVector,
    onDismiss: () -> Unit = {},
) {
    val coroutineScope = rememberCoroutineScope()
    val podcastRepository = koinInject<PodcastRepository>()

    // ViewModel State
    val controllerState by sharedViewModel.controllerState.collectAsStateWithLifecycle()
    val screenDataState by sharedViewModel.nowPlayingScreenData.collectAsStateWithLifecycle()
    val translationLanguage by dataStoreManager.translationLanguage.collectAsStateWithLifecycle("en")
    val showLyricsTranslationButton by dataStoreManager.showLyricsTranslationButton.collectAsStateWithLifecycle(DataStoreManager.TRUE)
    val sourceLyricsText = remember(screenDataState.lyricsData?.lyrics?.lines) {
        screenDataState.lyricsData?.lyrics?.lines.orEmpty().joinToString(" ") { it.words }
    }
    val sourceCanTranslateLyrics = remember(sourceLyricsText, translationLanguage) {
        shouldOfferLyricsTranslation(sourceLyricsText, translationLanguage)
    }
    val timelineState by sharedViewModel.timeline.collectAsStateWithLifecycle()
    // Audio-delay correction, read here and applied ONLY to the lyric line below. The seek bar and
    // the elapsed-time readout keep the raw position: they report where the player is, while a
    // lyric reports what the ear is hearing, and those two are what the offset separates.
    val lyricsOffsetMs by sharedViewModel.getLyricsOffsetMs().collectAsStateWithLifecycle(0)
    val castState by sharedViewModel.castState.collectAsStateWithLifecycle()

    val translatedVoteState by sharedViewModel.translatedVoteState.collectAsStateWithLifecycle()
    val lyricsVoteState by sharedViewModel.lyricsVoteState.collectAsStateWithLifecycle()
    val lyricsTranslationRequestState by sharedViewModel.lyricsTranslationRequestState.collectAsStateWithLifecycle()

    // Artwork Pager state — Spotify-style horizontal swipe between queue tracks.
    // The pager wraps the Canvas + Thumbnail layers. Controller layout below stays fixed.
    val nowPlayingState by sharedViewModel.nowPlayingState.collectAsStateWithLifecycle()
    // The removed watch-video preference must not turn audio items into video surfaces. Explicit
    // video results always use the video feed; ordinary songs stay audio-only.
    val shouldShowVideo = nowPlayingState?.mediaItem?.isVideo() ?: screenDataState.isVideo
    val queueDataState by sharedViewModel.getQueueDataState().collectAsStateWithLifecycle()
    val artworkQueue by remember {
        derivedStateOf { queueDataState?.data?.listTracks ?: emptyList() }
    }
    // Prefer the queue Track, but liked/downloaded playlist items may only have a media item or
    // SongEntity at the point playback starts. Resolve all forms to the same unprefixed ID.
    val nowPlayingVideoId: String? = nowPlayingState?.activeVideoId()
    val savedPodcastEpisode = key(nowPlayingVideoId) {
        val savedEpisode by remember(nowPlayingVideoId, podcastRepository) {
            nowPlayingVideoId?.let { id -> podcastRepository.getEpisode(id).map { it != null } }
                ?: kotlinx.coroutines.flow.flowOf(false)
        }.collectAsStateWithLifecycle(false)
        savedEpisode
    }
    val lyricsEnabled = nowPlayingState.allowsLyrics(savedPodcastEpisode)

    val canTranslateLyrics = lyricsEnabled && sourceCanTranslateLyrics && !nowPlayingVideoId.isNullOrBlank() &&
        screenDataState.currentVideoId == nowPlayingVideoId && screenDataState.lyricsVideoId == nowPlayingVideoId

    val savedTranslationEnabled = key(nowPlayingVideoId) {
        val enabled by remember(nowPlayingVideoId, dataStoreManager) {
            dataStoreManager.getSongLyricsTranslationEnabled(nowPlayingVideoId.orEmpty())
        }.collectAsStateWithLifecycle(false)
        enabled
    }
    // Update immediately on a tap; disk writes run in the shared ViewModel so closing
    // the player does not cancel them. A new track starts from its own saved choice.
    var translationChoice by remember(nowPlayingVideoId) { mutableStateOf<Boolean?>(null) }
    val translationEnabledForCurrentTrack = translationChoice ?: savedTranslationEnabled
    val playerScreenData = remember(screenDataState, canTranslateLyrics, translationEnabledForCurrentTrack, lyricsEnabled) {
        val data = screenDataState.withLyricsTranslation(canTranslateLyrics && translationEnabledForCurrentTrack)
        if (lyricsEnabled) data else data.copy(lyricsData = null, lyricsLoading = false, lyricsVideoId = null)
    }
    // currentOrderIndex() is a plain getter over the player, NOT Compose state, so it is read
    // inside this remember block — whose keys (the queue, and the track now playing) are exactly
    // the two things that can move the player's position. nowPlayingState is published FROM the
    // player's own transition callback, so by the time nowPlayingVideoId changes here the player
    // index has already moved; reading it any earlier would sample the outgoing track.
    val currentOrderIndex by remember(artworkQueue, nowPlayingVideoId) {
        derivedStateOf {
            deriveOrderIndex(
                queue = artworkQueue,
                nowPlayingVideoId = nowPlayingVideoId,
                playerOrderIndex = mediaPlayerHandler.currentOrderIndex(),
            )
        }
    }

    // Single PagerState — the unified ArtworkPager renders BOTH the fullscreen canvas
    // background and the centered square thumbnail in each page, so we don't need two
    // pagers + state mirroring.
    val artworkPagerState =
        rememberPagerState(
            initialPage = currentOrderIndex.coerceAtLeast(0),
            pageCount = { artworkQueue.size.coerceAtLeast(1) },
        )
    // ① Player → Pager: sync to the confirmed player track immediately.
    //
    // Keyed on the track ALONE. It used to be keyed on the queue SIZE as well, and a changing key
    // cancels the running effect — including the animateScrollToPage in flight. A cancelled scroll
    // simply stops where it is. Swipe gestures now skip through the player first, just like the
    // transport buttons; this effect then animates the artwork pager to the confirmed track.
    // Turning on the radio appends to the queue repeatedly, so the size changed again and again
    // and cut the animation short each time. The size is still read below — just as a value, not
    // as a trigger; ③ handles the case where the queue shrinks under the current page.
    LaunchedEffect(currentOrderIndex) {
        val target = currentOrderIndex
        if (artworkQueue.isNotEmpty() &&
            target in 0 until artworkQueue.size &&
            target != artworkPagerState.currentPage
        ) {
            // The artwork content itself provides the fade. Animating the pager as well delayed
            // the visible song change and reintroduced horizontal motion on top of that fade.
            artworkPagerState.scrollToPage(target)
        }
    }

    // ③ Queue mutation guard — when queue shrinks below currentPage, scroll to last index
    // to avoid IndexOutOfBoundsException during recomposition.
    LaunchedEffect(artworkQueue.size) {
        if (artworkQueue.isNotEmpty() && artworkPagerState.currentPage >= artworkQueue.size) {
            runCatching { artworkPagerState.scrollToPage(artworkQueue.lastIndex) }
        }
    }

    // State
    val isInPipMode = rememberIsInPipMode()

    val mainScrollState = rememberScrollState()

    var showHideMiddleLayout by rememberSaveable {
        // Lyrics can open directly from the mini-player while the canvas is already active.
        // Seed from the current track to avoid briefly showing the default artwork backdrop.
        mutableStateOf(screenDataState.canvasData == null)
    }

    var showSheet by rememberSaveable {
        mutableStateOf(false)
    }

    val fullscreenLyricsRequested by sharedViewModel.fullscreenLyricsRequest.collectAsStateWithLifecycle()

    var showHideControlLayout by rememberSaveable {
        // The request may arrive before this screen is composed; start in the requested state.
        mutableStateOf(!fullscreenLyricsRequested || !lyricsEnabled)
    }

    var showQueueBottomSheet by rememberSaveable {
        mutableStateOf(false)
    }

    var showInfoBottomSheet by rememberSaveable {
        mutableStateOf(false)
    }

    var showVoteDialog by rememberSaveable {
        mutableStateOf(false)
    }

    // NEW: Add to Playlist state
    var showAddToPlaylistDirectly by rememberSaveable {
        mutableStateOf(false)
    }

    var shouldShowToolbar by remember {
        mutableStateOf(false)
    }

    // Palette state
    val paletteState = rememberPaletteState()

    val startColor =
        remember {
            Animatable(Color.DarkGray)
        }
    val endColor =
        remember {
            Animatable(PlayerBackdropColor)
        }
    val gradientOffset by remember {
        mutableStateOf(GradientOffset(GradientAngle.CW135))
    }

    var spotShadowColor by remember {
        mutableStateOf(Color.White)
    }
    var previousPaletteTrackId by remember { mutableStateOf(nowPlayingVideoId) }
    var previousPaletteTrackChangeNanos by remember { mutableLongStateOf(0L) }
    var isRapidPaletteTrackChange by remember { mutableStateOf(false) }
    val latestRapidPaletteTrackChange by rememberUpdatedState(isRapidPaletteTrackChange)
    LaunchedEffect(nowPlayingVideoId) {
        if (nowPlayingVideoId != previousPaletteTrackId) {
            val nowNanos = withFrameNanos { it }
            isRapidPaletteTrackChange =
                previousPaletteTrackId != null && nowPlayingVideoId != null &&
                    previousPaletteTrackChangeNanos != 0L &&
                    nowNanos - previousPaletteTrackChangeNanos < 650_000_000L
            previousPaletteTrackId = nowPlayingVideoId
            previousPaletteTrackChangeNanos = nowNanos
        }
    }

    LaunchedEffect(screenDataState) {
        Logger.d(TAG, "ScreenDataState: $screenDataState")
        showHideMiddleLayout = screenDataState.canvasData == null
    }

    // Palette generation lives in its own NEVER-restarting effect. Keyed on screenDataState it
    // was cancelled mid-generate every time ANOTHER field of the data class arrived (canvasData,
    // lyrics, songInfo), and kmpalette parks on Loading when generate() is cancelled — palette
    // stays null, startColor stays black, and the M3E style falls back to the app seed (the
    // "canvas songs are always cyan" bug). Canvas songs hit this deterministically because the
    // canvas fetch always lands after the artwork bitmap.
    LaunchedEffect(Unit) {
        snapshotFlow { screenDataState.bitmap }
            .filterNotNull()
            .distinctUntilChanged()
            .collectLatest {
                paletteState.generate(it)
            }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { paletteState.palette }
            .distinctUntilChanged()
            .collectLatest {
                spotShadowColor = it.getColorFromPalette()
                startColor.animateTo(
                    it.getColorFromPalette(),
                    animationSpec =
                        tween(
                            durationMillis = if (latestRapidPaletteTrackChange) 300 else 820,
                            easing = FastOutSlowInEasing,
                        ),
                )
                // Lands on the same backdrop colour the fade and the area below the gradient
                // use, so the palette ramp resolves into the surface instead of a black patch.
                endColor.animateTo(
                    PlayerBackdropColor,
                    animationSpec =
                        tween(
                            durationMillis = if (latestRapidPaletteTrackChange) 300 else 820,
                            easing = FastOutSlowInEasing,
                        ),
                )
            }
    }

    LaunchedEffect(spotShadowColor) {
        Logger.d(TAG, "spotShadowColor: $spotShadowColor")
    }

    var isSliding by rememberSaveable(nowPlayingVideoId) {
        mutableStateOf(false)
    }
    var sliderValue by rememberSaveable(nowPlayingVideoId) {
        mutableFloatStateOf(0f)
    }
    var pendingSeekPositionMs by remember(nowPlayingVideoId) { mutableStateOf<Long?>(null) }
    LaunchedEffect(key1 = timelineState, key2 = isSliding, key3 = nowPlayingVideoId) {
        val pending = pendingSeekPositionMs
        if (pending != null && abs(timelineState.current - pending) <= 900L) {
            pendingSeekPositionMs = null
            isSliding = false
        }
        if (!isSliding && pendingSeekPositionMs == null) {
            sliderValue =
                if (timelineState.total > 0L) {
                    timelineState.currentProgressFraction * 100f
                } else {
                    0f
                }
        }
    }

    // Do not run an invisible infinite animation. Reading its hue here invalidated the whole
    // retained player (including lyrics) on every frame, even while the mini-player was showing.
    val rainbowColor = if (timelineState.isCrossfading) {
        val infiniteTransition = rememberInfiniteTransition(label = "crossfadeRainbow")
        val rainbowHue by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "rainbowHue",
        )
        hsvToColor(rainbowHue, 1f, 1f)
    } else {
        Color.White
    }
    val sliderTrackColor by animateColorAsState(
        targetValue = if (timelineState.isCrossfading) rainbowColor else Color.White,
        animationSpec = tween(300),
        label = "sliderCrossfadeColor",
    )

    // Show ControlLayout Or Show Artist Badge
    LaunchedEffect(fullscreenLyricsRequested, lyricsEnabled) {
        if (!lyricsEnabled) showHideControlLayout = true
        if (fullscreenLyricsRequested) {
            if (lyricsEnabled) showHideControlLayout = false
            sharedViewModel.consumeFullscreenLyricsRequest()
        }
    }
    val controlLayoutAlpha: Float by animateFloatAsState(
        targetValue = if (showHideControlLayout) 1f else 0f,
        animationSpec =
            tween(
                durationMillis = 500,
                easing = LinearEasing,
            ),
        label = "ControlLayoutAlpha",
    )

    var currentLyricLineIndex by rememberSaveable {
        mutableIntStateOf(-1)
    }

    // Canvas subtitle sync
    LaunchedEffect(timelineState, screenDataState.lyricsData?.lyrics, lyricsOffsetMs) {
        val lyrics = screenDataState.lyricsData?.lyrics
        if (lyrics == null || lyrics.syncType == "UNSYNCED" || lyrics.syncType == null) {
            currentLyricLineIndex = -1
            return@LaunchedEffect
        }
        val lines = lyrics.lines ?: return@LaunchedEffect
        val translatedLines =
            screenDataState.lyricsData
                ?.translatedLyrics
                ?.first
                ?.lines
        // What the ear is hearing right now, which is what a lyric answers to. Keyed on the offset
        // as well so dragging the setting while paused still moves the line.
        val nowMs = timelineState.current - lyricsOffsetMs
        if (nowMs > 0L) {
            lines.indices.forEach { i ->
                val startTimeMs = lines[i].startTimeMs.toLongOrNull() ?: 0L
                val endTimeMs =
                    if (i < lines.size - 1) {
                        lines[i + 1].startTimeMs.toLongOrNull() ?: 0L
                    } else {
                        startTimeMs + 60000
                    }
                if (nowMs in startTimeMs..endTimeMs) {
                    currentLyricLineIndex = i
                }
            }
            if (lines.isNotEmpty() &&
                nowMs in 0..(lines.getOrNull(0)?.startTimeMs?.toLongOrNull() ?: 0L)
            ) {
                currentLyricLineIndex = -1
            }
        } else {
            currentLyricLineIndex = -1
        }
    }

    if (screenDataState.lyricsData != null && controllerState.isPlaying) {
        KeepScreenOn()
    }
    val state =
        NowPlayingContentState(
            screenData = playerScreenData,
            controllerState = controllerState,
            timelineState = timelineState,
            timelineFlow = sharedViewModel.timeline,
            castState = castState,
            shouldShowVideo = shouldShowVideo,
            artworkQueue = artworkQueue,
            currentOrderIndex = currentOrderIndex,
            currentTrackId = nowPlayingVideoId,
            artistLinks = sharedViewModel.nowPlayingState.value?.songEntity?.let { song ->
                song.artistName.orEmpty().mapIndexedNotNull { index, name ->
                    song.artistId?.getOrNull(index)?.takeIf { it.isNotBlank() }?.let { name to it }
                }
            }.orEmpty(),
            artworkPagerState = artworkPagerState,
            startColor = startColor,
            endColor = endColor,
            spotShadowColor = spotShadowColor,
            gradientOffset = gradientOffset,
            sliderTrackColor = sliderTrackColor,
            sliderValue = sliderValue,
            currentLyricLineIndex = currentLyricLineIndex,
            showControlLayout = showHideControlLayout || !lyricsEnabled,
            controlLayoutAlpha = controlLayoutAlpha,
            showHideMiddleLayout = showHideMiddleLayout,
            shouldShowToolbar = shouldShowToolbar,
            isInPipMode = isInPipMode,
            mainScrollState = mainScrollState,
            isExpanded = isExpanded,
            dismissIcon = dismissIcon,
            videoAspectRatio = rememberVideoAspectRatio(MAIN_PLAYER) ?: 16f / 9,
            translationLoading = lyricsTranslationRequestState.videoId == nowPlayingVideoId && lyricsTranslationRequestState.isLoading,
            translationError = lyricsTranslationRequestState.takeIf { it.videoId == nowPlayingVideoId }?.error,
            canTranslateLyrics = canTranslateLyrics,
            showLyricsTranslationButton = showLyricsTranslationButton == DataStoreManager.TRUE,
            translationEnabled = canTranslateLyrics && translationEnabledForCurrentTrack,
            isSliding = isSliding,
            lyricsEnabled = lyricsEnabled,
        )
    // Podcast episodes expose their show name in the same player slot as an artist. Resolve the
    // playing video against the saved episode record first, so tapping that name returns to the
    // podcast page; ordinary songs continue to open their artist page.
    val navigateToArtistOrPodcast: (String?) -> Unit = { artistChannelId ->
        val song = sharedViewModel.nowPlayingState.value?.songEntity
        val videoId = song?.videoId ?: screenDataState.songInfoData?.videoId
        val fallbackArtistId =
            artistChannelId
                ?: song?.artistId?.firstOrNull()?.takeIf { it.isNotEmpty() }
                ?: screenDataState.songInfoData?.authorId?.takeIf { it.isNotEmpty() }

        coroutineScope.launch {
            val episode = videoId?.let { podcastRepository.getEpisode(it).firstOrNull() }
            when {
                episode != null -> {
                    onDismiss()
                    navController.navigate(PodcastDestination(podcastId = episode.podcastId))
                }

                fallbackArtistId?.startsWith("MPSP") == true && !lyricsEnabled -> {
                    onDismiss()
                    navController.navigate(PodcastDestination(podcastId = fallbackArtistId))
                }

                fallbackArtistId != null -> {
                    onDismiss()
                    navController.navigate(ArtistDestination(channelId = fallbackArtistId))
                }
            }
        }
    }

    val actions =
        NowPlayingContentActions(
            onUIEvent = { sharedViewModel.onUIEvent(it) },
            onSeekToQueueIndex = { index ->
                mediaPlayerHandler.playMediaItemInMediaSource(index)
            },
            onArtworkBitmap = { sharedViewModel.setBitmap(it) },
            onSliderChange = { newValue ->
                isSliding = true
                sliderValue = newValue
            },
            onSliderChangeFinished = {
                pendingSeekPositionMs =
                    if (timelineState.total > 0L) {
                        (timelineState.total * sliderValue / 100f).toLong()
                    } else {
                        null
                    }
                sharedViewModel.onUIEvent(
                    UIEvent.UpdateProgress(sliderValue),
                )
                if (pendingSeekPositionMs == null) isSliding = false
            },
            onToggleControls = {
                if (lyricsEnabled) showHideControlLayout = !showHideControlLayout
            },
            onNavigateToArtist = { navigateToArtistOrPodcast(null) },
            onNavigateToArtistId = { channelId -> navigateToArtistOrPodcast(channelId) },
            onShowMoreSheet = { showSheet = true },
            onShowQueue = { showQueueBottomSheet = true },
            onShowInfo = { showInfoBottomSheet = true },
            onShowAddToPlaylist = { showAddToPlaylistDirectly = true },
            onToggleLyricsControls = { if (lyricsEnabled) showHideControlLayout = !showHideControlLayout },
            onTranslateLyrics = {
                translationChoice = true
                sharedViewModel.setSongLyricsTranslationEnabled(nowPlayingVideoId.orEmpty(), true)
                sharedViewModel.translateCurrentLyrics()
            },
            onTranslationEnabledChanged = { enabled ->
                translationChoice = enabled
                sharedViewModel.setSongLyricsTranslationEnabled(nowPlayingVideoId.orEmpty(), enabled)
            },
            onShowVoteDialog = { showVoteDialog = true },
            onEnterFullscreenVideo = {
                onDismiss()
                navController.navigate(FullscreenDestination)
            },
            onDismiss = onDismiss,
            onToolbarVisibilityChange = { shouldShowToolbar = it },
            onMoveQueueItem = { from, to ->
                coroutineScope.launch {
                    mediaPlayerHandler.swap(from, to)
                }
            },
            onRemoveQueueItem = { index ->
                mediaPlayerHandler.removeMediaItem(index)
            },
        )

    // The remaining Now Playing sheets stay after the main content so they draw above it.
    if (showSheet) {
        NowPlayingBottomSheet(
            onDismiss = {
                showSheet = false
            },
            navController = navController,
            onNavigateToOtherScreen = {
                onDismiss()
            },
            song = null, // Auto set now playing
            showAddToPlaylist = false,
            setSleepTimerEnable = true,
            changeMainLyricsProviderEnable = true,
            onChangeLyricsProvider = { provider -> sharedViewModel.changeLyricsProviderAndRefetch(provider) },
            onRefetchLyrics = { sharedViewModel.refetchCurrentLyrics(translationEnabledForCurrentTrack) },
        )
    }

    if (showQueueBottomSheet) {
        QueueBottomSheet(
            onDismiss = {
                showQueueBottomSheet = false
            },
        )
    }

    if (showInfoBottomSheet) {
        InfoPlayerBottomSheet(
            onDismiss = {
                showInfoBottomSheet = false
            },
        )
    }

    // NEW: Add to Playlist Bottom Sheet
    if (showAddToPlaylistDirectly) {
        val viewModel: NowPlayingBottomSheetViewModel = koinViewModel()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        LaunchedEffect(Unit) {
            viewModel.resetPlaylists()
            viewModel.setSongEntity(null) // Uses current playing song
        }

        AddToPlaylistModalBottomSheet(
            isBottomSheetVisible = true,
            listLocalPlaylist = uiState.listLocalPlaylist,
            listYouTubePlaylist = uiState.listYouTubePlaylist,
            onDismiss = { showAddToPlaylistDirectly = false },
            onClick = { playlist ->
                viewModel.onUIEvent(NowPlayingBottomSheetUIEvent.AddToPlaylist(playlist.id))
                showAddToPlaylistDirectly = false
            },
            onYTPlaylistClick = { playlist ->
                viewModel.onUIEvent(NowPlayingBottomSheetUIEvent.AddToYouTubePlaylist(playlist.browseId, playlist.title))
                showAddToPlaylistDirectly = false
            },
            videoId = uiState.songUIState.videoId,
            onCreatePlaylist = viewModel.playlistCreationAction(),
        )
    }

    // Vote Dialog
    if (showVoteDialog) {
        val canVoteLyrics =
            screenDataState.lyricsData?.lyricsProvider == LyricsProvider.SIMPMUSIC &&
                screenDataState.lyricsData
                    ?.lyrics
                    ?.simpMusicLyrics != null
        val canVoteTranslatedLyrics =
            screenDataState.lyricsData?.translatedLyrics?.second == LyricsProvider.SIMPMUSIC &&
                screenDataState.lyricsData
                    ?.translatedLyrics
                    ?.first
                    ?.simpMusicLyrics != null

        VoteLyricsDialog(
            canVoteLyrics = canVoteLyrics,
            canVoteTranslatedLyrics = canVoteTranslatedLyrics,
            lyricsVoteState = lyricsVoteState,
            translatedLyricsVoteState = translatedVoteState,
            onVoteLyrics = { upvote ->
                sharedViewModel.voteLyrics(upvote)
            },
            onVoteTranslatedLyrics = { upvote ->
                sharedViewModel.voteTranslatedLyrics(upvote)
            },
            onDismiss = {
                showVoteDialog = false
            },
        )
    }

    NowPlayingContentAppleMusic(
        state = state,
        actions = actions,
    )
}
