package com.petedev.wavemusic

import com.petedev.wavemusic.ui.component.AmoledAlertDialog
import com.petedev.wavemusic.ui.component.AppContentHorizontalInset
import com.petedev.wavemusic.ui.component.BOTTOM_NAV_TRANSITION_DURATION_MILLIS
import com.petedev.wavemusic.ui.component.landscapeContentWindowInsets
import com.petedev.wavemusic.ui.component.auroraGradientBackground
import com.petedev.wavemusic.ui.component.LocalAppBackgroundPreset
import com.petedev.wavemusic.ui.component.LocalPageArtworkBackground
import com.petedev.wavemusic.ui.component.usesPageArtworkBackground
import com.petedev.wavemusic.ui.component.miniPlayerEnterTransition
import com.petedev.wavemusic.ui.component.rememberMiniPlayerVisibility

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import com.petedev.wavemusic.ui.component.DialogTextButton as TextButton
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND
import coil3.toUri
import kotlin.math.roundToInt
import com.petedev.wavemusic.domain.data.player.GenericMediaItem
import com.petedev.wavemusic.domain.manager.DataStoreManager
import com.petedev.wavemusic.domain.manager.DataStoreManager.Values.TRUE
import com.petedev.wavemusic.logger.Logger
import com.petedev.wavemusic.expect.Orientation
import com.petedev.wavemusic.expect.currentOrientation
import com.petedev.wavemusic.expect.openUrl
import com.petedev.wavemusic.expect.ui.BackHandler
import com.petedev.wavemusic.expect.ui.layerBackdrop
import com.petedev.wavemusic.expect.ui.rememberBackdrop
import com.petedev.wavemusic.extension.copy
import com.petedev.wavemusic.ui.component.AppBottomNavigationBar
import com.petedev.wavemusic.ui.component.AppNavigationRail
import com.petedev.wavemusic.ui.component.BottomNavScreen
import com.petedev.wavemusic.ui.component.FlatBottomDockHeight
import com.petedev.wavemusic.ui.component.GlassBottomDockHeight
import com.petedev.wavemusic.ui.component.LiquidGlassAppBottomNavigationBar
import com.petedev.wavemusic.ui.component.MorphingSlimNavigationDock
import com.petedev.wavemusic.ui.component.SlimNavigationDock
import com.petedev.wavemusic.ui.component.FrostedGlassHorizonHost
import com.petedev.wavemusic.ui.component.FrostedHorizonDockState
import com.petedev.wavemusic.ui.component.dialogMotionSource
import com.petedev.wavemusic.ui.component.dialogBackdropBlur
import com.petedev.wavemusic.ui.icon.ArrowForwardIos
import com.petedev.wavemusic.ui.icon.WaveIcons
import com.petedev.wavemusic.ui.navigation.destination.home.AnalyticsDestination
import com.petedev.wavemusic.ui.navigation.destination.home.HomeDestination
import com.petedev.wavemusic.ui.navigation.destination.home.NotificationDestination
import com.petedev.wavemusic.ui.navigation.destination.home.SettingsDestination
import com.petedev.wavemusic.ui.navigation.destination.home.WrappedDestination
import com.petedev.wavemusic.ui.navigation.destination.library.LibraryDestination
import com.petedev.wavemusic.ui.navigation.destination.library.LibraryDynamicPlaylistDestination
import com.petedev.wavemusic.ui.navigation.destination.library.MixForYouDestination
import com.petedev.wavemusic.ui.navigation.destination.list.AlbumDestination
import com.petedev.wavemusic.ui.navigation.destination.list.ArtistDestination
import com.petedev.wavemusic.ui.navigation.destination.list.PlaylistDestination
import com.petedev.wavemusic.ui.navigation.destination.player.FullscreenDestination
import com.petedev.wavemusic.ui.navigation.destination.search.SearchDestination
import com.petedev.wavemusic.ui.navigation.graph.AppNavigationGraph
import com.petedev.wavemusic.ui.navigation.graph.hasLiquidGlassHeader
import com.petedev.wavemusic.ui.screen.MiniPlayer
import com.petedev.wavemusic.ui.screen.player.NowPlayingScreen
import com.petedev.wavemusic.ui.screen.player.NowPlayingScreenContent
import com.petedev.wavemusic.ui.screen.player.playerCollapseSpringSpec
import com.petedev.wavemusic.ui.screen.player.playerExpandSpringSpec
import com.petedev.wavemusic.ui.screen.player.playerDockAlpha
import com.petedev.wavemusic.ui.theme.AppTheme
import com.petedev.wavemusic.ui.theme.ForceDarkContent
import com.petedev.wavemusic.ui.theme.desktopPanelDark
import com.petedev.wavemusic.ui.theme.desktopWindowDark
import com.petedev.wavemusic.ui.theme.desktopWindowLight
import com.petedev.wavemusic.ui.theme.fontFamily
import com.petedev.wavemusic.ui.theme.parseThemeColorHex
import com.petedev.wavemusic.ui.theme.typo
import com.petedev.wavemusic.utils.VersionManager
import com.petedev.wavemusic.viewModel.SharedViewModel
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownTypography
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import com.petedev.wavemusic.resources.Res
import com.petedev.wavemusic.resources.cancel
import com.petedev.wavemusic.resources.do_not_show_again
import com.petedev.wavemusic.resources.download
import com.petedev.wavemusic.resources.good_night
import com.petedev.wavemusic.resources.notification
import com.petedev.wavemusic.resources.settings
import com.petedev.wavemusic.resources.sleep_timer_off
import com.petedev.wavemusic.resources.this_app_needs_to_access_your_notification
import com.petedev.wavemusic.resources.this_link_is_not_supported
import com.petedev.wavemusic.resources.unknown
import com.petedev.wavemusic.resources.update_available
import com.petedev.wavemusic.resources.update_message
import com.petedev.wavemusic.resources.yes
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.distinctUntilChanged

private const val BOTTOM_BAR_VISIBILITY_DURATION_MS = 360
private const val BOTTOM_BAR_HIDE_DURATION_MS = 520

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class, ExperimentalFoundationApi::class)
@Composable
fun App(
    viewModel: SharedViewModel = koinInject(),
    showDesktopNotificationPermissionDialog: Boolean = false,
    onDismissDesktopNotificationPermissionDialog: (doNotShowAgain: Boolean) -> Unit = {},
    onOpenDesktopNotificationSettings: (doNotShowAgain: Boolean) -> Unit = {},
) {
    val appLanguage by koinInject<DataStoreManager>()
        .getString(com.petedev.wavemusic.common.SELECTED_LANGUAGE).collectAsStateWithLifecycle(initialValue = null)
    // Desktop locale changes don't recreate a window. Recreate the UI resource tree so cached
    // labels and accessibility text follow the new locale as Android's activity does.
    androidx.compose.runtime.key(if (getPlatform() == Platform.Desktop) appLanguage else Unit) {
        AppContent(viewModel, showDesktopNotificationPermissionDialog, onDismissDesktopNotificationPermissionDialog,
            onOpenDesktopNotificationSettings)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class, ExperimentalFoundationApi::class)
@Composable
private fun AppContent(
    viewModel: SharedViewModel,
    showDesktopNotificationPermissionDialog: Boolean,
    onDismissDesktopNotificationPermissionDialog: (doNotShowAgain: Boolean) -> Unit,
    onOpenDesktopNotificationSettings: (doNotShowAgain: Boolean) -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val density = LocalDensity.current
    val imeInsets = WindowInsets.ime
    val isKeyboardVisible by remember(imeInsets, density) {
        derivedStateOf { imeInsets.getBottom(density) > 0 }
    }
    val windowSize = currentWindowAdaptiveInfo().windowSizeClass
    val navController = rememberNavController()
    val isDesktopShell = getPlatform() == Platform.Desktop

    val sleepTimerState by viewModel.sleepTimerState.collectAsStateWithLifecycle()
    val miniPlayerHasMedia by remember(viewModel) {
        viewModel.nowPlayingState.map { state ->
            val item = state?.mediaItem
            item != null && item != GenericMediaItem.EMPTY
        }.distinctUntilChanged()
    }.collectAsStateWithLifecycle(initialValue = viewModel.nowPlayingState.value?.mediaItem.let {
        it != null && it != GenericMediaItem.EMPTY
    })
    val updateData by viewModel.updateResponse.collectAsStateWithLifecycle()
    val updateCheckRevision by viewModel.updateCheckRevision.collectAsStateWithLifecycle()
    val intent by viewModel.intent.collectAsStateWithLifecycle()
    val showNotificationPermissionDialog by viewModel.showNotificationPermissionDialog.collectAsStateWithLifecycle()

    val isLiquidGlassEnabled by viewModel.getEnableLiquidGlass().collectAsStateWithLifecycle(DataStoreManager.TRUE)
    val slimNavigationBar by viewModel.getSlimNavigationBar()
        .collectAsStateWithLifecycle(DataStoreManager.FALSE)
    val advancedDialogAnimations by viewModel.getAdvancedDialogAnimations()
        .collectAsStateWithLifecycle(DataStoreManager.FALSE)
    val reduceVisualEffects by viewModel.getReduceVisualEffects()
        .collectAsStateWithLifecycle(
            if (com.petedev.wavemusic.data.dataStore.prefersReducedVisualEffects()) DataStoreManager.TRUE else DataStoreManager.FALSE,
        )
    val reduceVisualEffectsEnabled = reduceVisualEffects == DataStoreManager.TRUE
    // App-shell navigation follows the saved switch on Android, and Reduce Visual Effects
    // suppresses it regardless of that saved preference.
    val useLiquidGlass = isLiquidGlassEnabled == TRUE && !reduceVisualEffectsEnabled
    val useSlimNavigationDock =
        slimNavigationBar == DataStoreManager.TRUE &&
            getPlatform() == Platform.Android &&
            currentOrientation() == Orientation.PORTRAIT &&
            !reduceVisualEffectsEnabled
    val appBackgroundPreset by viewModel.getAppBackgroundPreset()
        .collectAsStateWithLifecycle(DataStoreManager.APP_BACKGROUND_AURORA)
    val pageArtworkBackground = remember { mutableStateOf<Color?>(null) }
    val liquidGlassIntensity by viewModel.liquidGlassIntensity.collectAsStateWithLifecycle()
    val frostedGlassHeaderStyle by viewModel.frostedGlassHeaderStyle.collectAsStateWithLifecycle()
    val frostedGlassHorizonStyle by viewModel.frostedGlassHorizonStyle.collectAsStateWithLifecycle()
    val horizonDockState = remember { FrostedHorizonDockState() }
    // Local tracking still powers listening data and Most Played, but Analytics is no longer a nav tab.
    val showAnalyticsTab = false
    // Keep Mix for You out of the bottom navigation. Its screen remains in the graph so existing
    // saved navigation state can return safely, but the tab itself was causing heavy work while
    // switching between the primary destinations.
    val showMixForYouTab = false

    val themeMode by viewModel.getThemeMode().collectAsStateWithLifecycle(DataStoreManager.THEME_MODE_SYSTEM)
    val themeColorSource by viewModel.getThemeColorSource().collectAsStateWithLifecycle(DataStoreManager.THEME_COLOR_DEFAULT)
    val customThemeColorHex by viewModel.getCustomThemeColor().collectAsStateWithLifecycle(DataStoreManager.DEFAULT_THEME_COLOR_HEX)
    // MiniPlayer visibility: derived, never stored.
    //
    // This used to be a rememberSaveable Boolean written by a LaunchedEffect. Two things went
    // wrong with that. The effect only runs AFTER the first composition, so the first frame drew
    // whatever the initial value said — and rememberSaveable RESTORES a previously saved value,
    // so flipping that initial value from true to false changed nothing on a process that had
    // already saved true. The bar therefore showed, hid, and showed again on every start.
    //
    // Reading it straight from nowPlayingData removes both failure modes: there is no first-frame
    // guess to be wrong, and no saved copy to disagree with the source.
    val isShowMiniPlayer by remember {
        derivedStateOf {
            miniPlayerHasMedia
        }
    }
    var isDismissingMiniPlayer by remember { mutableStateOf(false) }
    LaunchedEffect(isShowMiniPlayer) {
        if (isShowMiniPlayer) isDismissingMiniPlayer = false
    }

    // Now playing screen
    var isShowNowPlaylistScreen by rememberSaveable {
        mutableStateOf(false)
    }
    // Keep the expanded player composed after its first open. Its animated-cover ExoPlayer is
    // owned by the composition, so disposing the screen on collapse restarts the video on reopen.
    var hasOpenedNowPlayingScreen by rememberSaveable { mutableStateOf(false) }
    var isPreparingNowPlayingScreen by remember { mutableStateOf(false) }
    var miniPlayerSwipeProgress by remember { mutableStateOf(0f) }
    var isMiniPlayerSwipeTransitionActive by remember { mutableStateOf(false) }
    var predictiveBackProgress by remember { mutableStateOf(0f) }
    var isPredictiveBackGestureActive by remember { mutableStateOf(false) }
    var pendingPlayerProgressHandoff by remember { mutableStateOf<Float?>(null) }
    var playerProgressTransitionRevision by remember { mutableStateOf(0) }
    val tapOpenProgress = remember {
        Animatable(if (isShowNowPlaylistScreen) 1f else 0f)
    }
    // One interruptible animation owns tap-open/tap-close progress. A visibility change cancels
    // the previous animateTo and retargets from its current value, so reversing mid-transition
    // carries the player smoothly in the opposite direction.
    LaunchedEffect(
        isShowNowPlaylistScreen,
        playerProgressTransitionRevision,
    ) {
        // A gesture temporarily owns progress. Stop the spring underneath it; when the gesture
        // ends, the handoff below resumes the spring from the visible position.
        if (
            pendingPlayerProgressHandoff == null &&
            (isMiniPlayerSwipeTransitionActive || isPredictiveBackGestureActive)
        ) {
            return@LaunchedEffect
        }
        // Gestures and programmatic opens/closes can temporarily use different progress sources.
        // Seed the spring from the exact visible progress before releasing the gesture source.
        pendingPlayerProgressHandoff?.let { visibleProgress ->
            tapOpenProgress.snapTo(visibleProgress.coerceIn(0f, 1f))
            pendingPlayerProgressHandoff = null
            isMiniPlayerSwipeTransitionActive = false
            miniPlayerSwipeProgress = 0f
            predictiveBackProgress = 0f
            isPredictiveBackGestureActive = false
        }
        tapOpenProgress.animateTo(
            targetValue = if (isShowNowPlaylistScreen) 1f else 0f,
            animationSpec =
                if (isShowNowPlaylistScreen) {
                    playerExpandSpringSpec()
                } else {
                    playerCollapseSpringSpec()
                },
        )
    }
    // Keep the State identity stable and read progress only in graphics-layer lambdas. Passing a
    // changing Float into the expanded player recomposed its entire lyrics tree on every frame.
    val currentSwipeProgressState = remember {
        derivedStateOf {
            when {
                pendingPlayerProgressHandoff != null ->
                    pendingPlayerProgressHandoff!!.coerceIn(0f, 1f)
                isMiniPlayerSwipeTransitionActive -> miniPlayerSwipeProgress
                isPredictiveBackGestureActive -> (1f - predictiveBackProgress).coerceIn(0f, 1f)
                isPreparingNowPlayingScreen -> 0.02f
                else -> tapOpenProgress.value.coerceIn(0f, 1f)
            }
        }
    }
    // Keep the retained player above the app for as long as it is visible or moving. Deriving this
    // from the same progress avoids a delayed close coroutine lowering a newly reopened surface.
    val isNowPlayingOverlayOnTop by remember {
        derivedStateOf {
            !isPreparingNowPlayingScreen &&
                (isShowNowPlaylistScreen || currentSwipeProgressState.value > 0.001f)
        }
    }
    val isNowPlayingClosing by remember {
        derivedStateOf {
            !isShowNowPlaylistScreen && currentSwipeProgressState.value > 0.001f
        }
    }
    // Keep the mini-player composed behind the expanded surface at full cover. An in-flight drag
    // may have started on its pointer handler and still needs that handler if the finger reverses.
    // Clamping above means this changes only with track visibility, not on every progress frame.
    val isMiniPlayerVisible by remember(isShowMiniPlayer) {
        // Keep the mini-player composed at exactly 1f so an in-flight drag that opened it can
        // still receive the rest of that same pointer sequence if the user reverses direction.
        derivedStateOf { currentSwipeProgressState.value <= 1f }
    }
    val closeMiniPlayer: () -> Unit = remember {
        {
            isDismissingMiniPlayer = true
            viewModel.stopPlayer()
            viewModel.isServiceRunning = false
        }
    }

    // Keep callbacks stable across gesture updates. Recreating these function references would
    // invalidate the glass navigation bar and mini-player subtree while their layers are moving.
    val updateMiniPlayerSwipeProgress: (Float) -> Unit = remember {
        { progress ->
            // Zero can be a reversal while the finger is still down, not a release.
            pendingPlayerProgressHandoff = null
            miniPlayerSwipeProgress = progress.coerceIn(0f, 1f)
            if (!isMiniPlayerSwipeTransitionActive) {
                playerProgressTransitionRevision += 1
                focusManager.clearFocus(force = true)
                keyboardController?.hide()
            }
            hasOpenedNowPlayingScreen = true
            isMiniPlayerSwipeTransitionActive = true
            isShowNowPlaylistScreen = true
        }
    }
    val updatePredictiveBackProgress: (Float) -> Unit = remember {
        { progress ->
            if (!isPredictiveBackGestureActive) {
                pendingPlayerProgressHandoff = null
                playerProgressTransitionRevision += 1
            }
            predictiveBackProgress = progress.coerceIn(0f, 1f)
            isPredictiveBackGestureActive = true
        }
    }
    val clearPredictiveBackProgress: () -> Unit = remember {
        {
            pendingPlayerProgressHandoff = currentSwipeProgressState.value
            isPredictiveBackGestureActive = false
            predictiveBackProgress = 0f
            playerProgressTransitionRevision += 1
        }
    }
    val openNowPlaying: () -> Unit = remember {
        {
            if (isMiniPlayerSwipeTransitionActive) {
                // Release an upward pull into the spring from its exact visible position.
                pendingPlayerProgressHandoff = currentSwipeProgressState.value
                isShowNowPlaylistScreen = true
                playerProgressTransitionRevision += 1
            } else if (!isShowNowPlaylistScreen && !isPreparingNowPlayingScreen) {
                pendingPlayerProgressHandoff =
                    if (isMiniPlayerSwipeTransitionActive || isPredictiveBackGestureActive || isPreparingNowPlayingScreen) {
                        currentSwipeProgressState.value
                    } else {
                        null
                    }
                // Hiding the IME alone leaves its focused editor alive; Android can keep the
                // composing/suggestion and text-selection toolbar windows above the player.
                focusManager.clearFocus(force = true)
                keyboardController?.hide()
                if (!hasOpenedNowPlayingScreen) {
                    // Compose the lyrics/artwork surface behind the mini-player for one frame
                    // before its first entrance. Building the large tree on the same frame as
                    // the animation was the hitch on first open.
                    hasOpenedNowPlayingScreen = true
                    isPreparingNowPlayingScreen = true
                } else {
                    isShowNowPlaylistScreen = true
                }
            }
        }
    }
    LaunchedEffect(isPreparingNowPlayingScreen) {
        if (isPreparingNowPlayingScreen) {
            withFrameNanos { }
            isPreparingNowPlayingScreen = false
            isShowNowPlaylistScreen = true
        }
    }
    val dismissNowPlaying: () -> Unit = remember {
        {
            if (
                isShowNowPlaylistScreen ||
                isPreparingNowPlayingScreen ||
                isMiniPlayerSwipeTransitionActive ||
                isPredictiveBackGestureActive
            ) {
                pendingPlayerProgressHandoff =
                    if (isMiniPlayerSwipeTransitionActive || isPredictiveBackGestureActive || isPreparingNowPlayingScreen) {
                        currentSwipeProgressState.value
                    } else {
                        null
                    }
                isPreparingNowPlayingScreen = false
                isShowNowPlaylistScreen = false
                // Transfer a committed predictive gesture to the shared player spring. Keeping
                // gesture ownership active would leave the player as the top back handler until
                // the close animation ended, and animate it a second time after the handoff.
                isPredictiveBackGestureActive = false
                predictiveBackProgress = 0f
                playerProgressTransitionRevision += 1
            }
        }
    }
    // Fullscreen
    var isInFullscreen by rememberSaveable {
        mutableStateOf(false)
    }

    var isNavBarVisible by rememberSaveable {
        mutableStateOf(true)
    }

    var shouldShowUpdateDialog by rememberSaveable {
        mutableStateOf(false)
    }

    LaunchedEffect(intent) {
        val intent = intent ?: return@LaunchedEffect
        val data = intent.data
        Logger.d("MainActivity", "onCreate: $data")
        if (data != null) {
            if (data == "simpmusic://notification".toUri()) {
                viewModel.setIntent(null)
                navController.navigate(
                    NotificationDestination,
                )
            } else if (data.scheme == "wordbyword" && data.host == "lastfm-auth") {
                // Last.fm sends the user back here after they approve access, carrying the request
                // token: wordbyword://lastfm-auth?token=xxx. The callback is fixed on the API
                // account, which is why the scheme is not "simpmusic".
                val token = data.getQueryParameter("token")
                Logger.d("MainActivity", "Last.fm callback, token present: ${!token.isNullOrEmpty()}")
                viewModel.setIntent(null)
                // Deliberately no navigation: the login screen is almost certainly already open —
                // the browser was opened from it — and navigating would stack a second copy on top
                // of it. The token is handed straight to the shared view model, and the screen
                // closes itself when it sees a session key appear.
                token?.let { viewModel.completeLastfmLogin(it) }
            } else if (data.host == "simpmusic.org" || data.scheme == "simpmusic") {
                // https://simpmusic.org/app/watch?v=VIDEO_ID
                // https://simpmusic.org/app/playlist?list=PLAYLIST_ID
                // https://simpmusic.org/app/channel/CHANNEL_ID
                // simpmusic://watch?v=VIDEO_ID  (host="watch", no path)
                // simpmusic://playlist?list=PLAYLIST_ID
                // simpmusic://channel/CHANNEL_ID
                val segments = data.pathSegments
                // For simpmusic.org: segments = ["app", "watch"] → appPath = segments[1]
                // For simpmusic://: host IS the appPath (e.g. host="watch"), segments = []
                val appPath =
                    if (data.scheme == "simpmusic") {
                        data.host
                    } else {
                        segments.getOrNull(1)
                    }
                Logger.d("MainActivity", "simpmusic.org deep link, appPath: $appPath")
                viewModel.setIntent(null)
                when (appPath) {
                    "watch" -> {
                        data.getQueryParameter("v")?.let { videoId ->
                            viewModel.loadSharedMediaItem(videoId)
                        }
                    }

                    "playlist" -> {
                        data.getQueryParameter("list")?.let { playlistId ->
                            if (playlistId.startsWith("OLAK5uy_")) {
                                navController.navigate(AlbumDestination(browseId = playlistId))
                            } else if (playlistId.startsWith("VL")) {
                                navController.navigate(PlaylistDestination(playlistId = playlistId))
                            } else {
                                navController.navigate(PlaylistDestination(playlistId = "VL$playlistId"))
                            }
                        }
                    }

                    "channel", "c" -> {
                        // simpmusic://channel/UCxxx → segments = ["UCxxx"]
                        // simpmusic.org/app/channel/UCxxx → segments = ["app", "channel", "UCxxx"]
                        val artistId =
                            if (data.scheme == "simpmusic") {
                                segments.firstOrNull()
                            } else {
                                segments.getOrNull(2)
                            }
                        artistId?.let {
                            if (it.startsWith("UC")) {
                                navController.navigate(ArtistDestination(channelId = it))
                            } else {
                                viewModel.makeToast(getString(Res.string.this_link_is_not_supported))
                            }
                        }
                    }

                    "album" -> {
                        data.getQueryParameter("id")?.let { albumId ->
                            navController.navigate(AlbumDestination(browseId = albumId))
                        }
                    }

                    // simpmusic://library                     → the Library tab
                    // simpmusic://library?type=favorite       → one of its collections
                    // Added for the Playlists widget, whose shortcuts have to reach these
                    // screens from the home screen without the app already running.
                    "library" -> {
                        val type = data.getQueryParameter("type")
                        if (type.isNullOrBlank()) {
                            navController.navigate(LibraryDestination)
                        } else {
                            navController.navigate(LibraryDynamicPlaylistDestination(type = type))
                        }
                    }

                    else -> {
                        viewModel.makeToast(getString(Res.string.this_link_is_not_supported))
                    }
                }
            } else {
                Logger.d("MainActivity", "onCreate: $data")
                when (val path = data.pathSegments.firstOrNull()) {
                    "playlist" -> {
                        data
                            .getQueryParameter("list")
                            ?.let { playlistId ->
                                viewModel.setIntent(null)
                                if (playlistId.startsWith("OLAK5uy_")) {
                                    navController.navigate(
                                        AlbumDestination(
                                            browseId = playlistId,
                                        ),
                                    )
                                } else if (playlistId.startsWith("VL")) {
                                    navController.navigate(
                                        PlaylistDestination(
                                            playlistId = playlistId,
                                        ),
                                    )
                                } else {
                                    navController.navigate(
                                        PlaylistDestination(
                                            playlistId = "VL$playlistId",
                                        ),
                                    )
                                }
                            }
                    }

                    "channel", "c" -> {
                        data.lastPathSegment?.let { artistId ->
                            if (artistId.startsWith("UC")) {
                                viewModel.setIntent(null)
                                navController.navigate(
                                    ArtistDestination(
                                        channelId = artistId,
                                    ),
                                )
                            } else {
                                viewModel.makeToast(
                                    getString(
                                        Res.string.this_link_is_not_supported,
                                    ),
                                )
                            }
                        }
                    }

                    else -> {
                        when {
                            path == "watch" -> data.getQueryParameter("v")
                            data.host == "youtu.be" -> path
                            else -> null
                        }?.let { videoId ->
                            viewModel.loadSharedMediaItem(videoId)
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(updateData, updateCheckRevision) {
        val response = updateData ?: return@LaunchedEffect
        if (viewModel.showedUpdateDialog &&
            response.tagName.trim().removePrefix("v").removePrefix("V") !=
                VersionManager.getVersionName().trim().removePrefix("v").removePrefix("V")
        ) {
            shouldShowUpdateDialog = true
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val isSettingsScreen =
        navBackStackEntry?.destination?.hierarchy?.any {
            it.hasRoute(SettingsDestination::class)
        } == true
    val isSearchScreen =
        navBackStackEntry?.destination?.hierarchy?.any {
            it.hasRoute(SearchDestination::class)
        } == true
    LaunchedEffect(navBackStackEntry) {
        Logger.d("MainActivity", "Current destination: ${navBackStackEntry?.destination?.route}")
        if (navBackStackEntry?.destination?.route?.contains("FullscreenDestination") == true) {
            isShowNowPlaylistScreen = false
        }
        // Wrapped counts as fullscreen for the same reason the video player does: it is a
        // full-bleed reel, and the rail and the mini player would sit on top of the card the user
        // is meant to be reading — and on top of every card captured as a share image.
        isInFullscreen = navBackStackEntry?.destination?.hierarchy?.any {
            it.hasRoute(FullscreenDestination::class) || it.hasRoute(WrappedDestination::class)
        } == true
    }
    LaunchedEffect(showAnalyticsTab) {
        // Turning tracking off removes the Analytics tab, so leaving the user standing on it would
        // strand them on a screen no tab points at anymore.
        if (!showAnalyticsTab &&
            navBackStackEntry?.destination?.hierarchy?.any {
                it.hasRoute(AnalyticsDestination::class)
            } == true
        ) {
            navController.navigate(HomeDestination) {
                popUpTo(navController.graph.startDestinationId) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }
    LaunchedEffect(showMixForYouTab) {
        // Same for signing out of YouTube: the Mix for you tab goes away, so nobody may be left
        // standing on a screen that has no mixes to show and no tab pointing at it.
        if (!showMixForYouTab &&
            navBackStackEntry?.destination?.hierarchy?.any {
                it.hasRoute(MixForYouDestination::class)
            } == true
        ) {
            navController.navigate(HomeDestination) {
                popUpTo(navController.graph.startDestinationId) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }
    var isScrolledToTop by rememberSaveable {
        mutableStateOf(false)
    }
    var isSlimDockExpanded by rememberSaveable {
        mutableStateOf(true)
    }
    // Scroll accumulation is gesture bookkeeping, not UI state. Avoid a snapshot write per pixel.
    val slimDockScrollDistance = remember { floatArrayOf(0f) }
    val slimDockScrollRequests = remember { Channel<Boolean>(Channel.CONFLATED) }
    val slimDockScrollThreshold = with(density) { 96.dp.toPx() }
    val canRespondToSlimDockScroll =
        useSlimNavigationDock && isNavBarVisible && !isSettingsScreen && !isInFullscreen
    LaunchedEffect(canRespondToSlimDockScroll) {
        slimDockScrollDistance[0] = 0f
        slimDockScrollRequests.tryReceive()
        if (!canRespondToSlimDockScroll) return@LaunchedEffect
        for (expand in slimDockScrollRequests) {
            if (expand == isSlimDockExpanded) continue
            isSlimDockExpanded = expand
            // Finish one morph before accepting the latest reversed scroll intent. Conflation
            // drops intermediate directions instead of repeatedly restarting GPU/layout work.
            delay(320L)
        }
    }
    val slimDockScrollConnection = remember(canRespondToSlimDockScroll, slimDockScrollThreshold) {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                // Programmatic scrolling and page remeasurement must not reverse a dock morph.
                if (source != NestedScrollSource.UserInput) return Offset.Zero
                if (!canRespondToSlimDockScroll || consumed.y == 0f) {
                    slimDockScrollDistance[0] = 0f
                    return Offset.Zero
                }

                // Match the reference behavior: downward scrolling compacts the dock; upward
                // scrolling restores the full navigation and mini-player.
                if (slimDockScrollDistance[0] * consumed.y < 0f) slimDockScrollDistance[0] = 0f
                slimDockScrollDistance[0] += consumed.y
                if (kotlin.math.abs(slimDockScrollDistance[0]) >= slimDockScrollThreshold) {
                    slimDockScrollRequests.trySend(slimDockScrollDistance[0] > 0f)
                    slimDockScrollDistance[0] = 0f
                }
                return Offset.Zero
            }
        }
    }
    LaunchedEffect(useSlimNavigationDock, isScrolledToTop, isSearchScreen) {
        if (!useSlimNavigationDock || (isScrolledToTop && !isSearchScreen)) {
            slimDockScrollRequests.tryReceive()
            isSlimDockExpanded = true
            slimDockScrollDistance[0] = 0f
        }
    }
    val hideNavigationBar: () -> Unit = remember { { isNavBarVisible = false } }
    val showNavigationBar: (Boolean) -> Unit = remember {
        { _ -> isNavBarVisible = true }
    }
    val showNowPlayingSheet: () -> Unit = remember {
        {
            focusManager.clearFocus(force = true)
            keyboardController?.hide()
            if (!isShowNowPlaylistScreen) {
                pendingPlayerProgressHandoff =
                    if (isMiniPlayerSwipeTransitionActive || isPredictiveBackGestureActive || isPreparingNowPlayingScreen) {
                        currentSwipeProgressState.value
                    } else {
                        null
                    }
                isShowNowPlaylistScreen = true
            }
        }
    }
    val onDestinationScrolling: (Boolean) -> Unit = remember {
        { onTop -> isScrolledToTop = onTop }
    }
    val reloadDestinationIfNeeded: (kotlin.reflect.KClass<*>) -> Unit = remember {
        { destinationClass -> viewModel.reloadDestination(destinationClass) }
    }
    val isTablet = windowSize.isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND)
    val isTabletLandscape = isTablet && currentOrientation() == Orientation.LANDSCAPE
    val usePlayerSidePanel = isDesktopShell && isTabletLandscape
    val useLandscapeBottomBar = getPlatform() == Platform.Android && currentOrientation() == Orientation.LANDSCAPE

    AppTheme(
        themeMode = themeMode,
        themeColorSource = themeColorSource,
        customThemeColor = parseThemeColorHex(customThemeColorHex),
        appBackgroundPreset = appBackgroundPreset,
        // Desktop normally uses glass by design; AppTheme applies the Reduce Visual Effects override.
        liquidGlassEnabled = isLiquidGlassEnabled == TRUE || getPlatform() == Platform.Desktop,
        liquidGlassIntensity = liquidGlassIntensity,
        reduceVisualEffectsEnabled = reduceVisualEffectsEnabled,
        advancedDialogAnimationsEnabled = advancedDialogAnimations == DataStoreManager.TRUE,
        // Let the fixed sheet reach behind system icons on Android versions where the window
        // still honors an opaque status-bar color. Icon contrast continues to follow the theme.
        transparentStatusBar = frostedGlassHeaderStyle.isEnabled && useLiquidGlass &&
            navBackStackEntry?.destination.hasLiquidGlassHeader(),
        // The visible player keeps white status icons regardless of the app theme.
        forceWhiteStatusBarIcons =
            (isShowMiniPlayer && !usePlayerSidePanel && isNowPlayingOverlayOnTop) || isInFullscreen,
    ) {
        val isLightScheme = MaterialTheme.colorScheme.background.luminance() > 0.5f
        CompositionLocalProvider(
            LocalAppBackgroundPreset provides appBackgroundPreset,
            LocalPageArtworkBackground provides pageArtworkBackground,
            com.petedev.wavemusic.ui.theme.LocalFrostedGlassHeaderStyle provides frostedGlassHeaderStyle,
        ) {
        // Match glass sampling to the selected solid background as well as the theme mode.
        val backdrop = rememberBackdrop(MaterialTheme.colorScheme.background)

        // The desktop shell is a window colour with panels floating on it. The two schemes mirror
        // each other: the window takes the extreme (pure black / pure white) and the panel steps
        // one shade back towards the middle, so the panels read as raised either way.
        val desktopWindow = if (isLightScheme) desktopWindowLight else desktopWindowDark
        val desktopPanel =
            if (isLightScheme) MaterialTheme.colorScheme.surfaceContainer else desktopPanelDark
        val windowAuroraBackdrop =
            if (useLandscapeBottomBar) rememberBackdrop(MaterialTheme.colorScheme.background) else null
        val activeArtworkBackground =
            pageArtworkBackground.value.takeIf {
                navBackStackEntry?.destination.usesPageArtworkBackground()
            }
        val animatedWindowBackground by animateColorAsState(
            targetValue = activeArtworkBackground ?: MaterialTheme.colorScheme.background,
            animationSpec = tween(BOTTOM_NAV_TRANSITION_DURATION_MILLIS, easing = FastOutSlowInEasing),
            label = "windowArtworkBackground",
        )
        val landscapeContentInsets =
            if (useLandscapeBottomBar) landscapeContentWindowInsets() else WindowInsets(0, 0, 0, 0)
        Box(Modifier.fillMaxSize().dialogBackdropBlur().dialogMotionSource()) {
            if (windowAuroraBackdrop != null) {
                // Paint one full-window background, including the camera and navigation margins.
                // This source contains no UI; inset content samples it in window coordinates.
                Box(
                    Modifier
                        .matchParentSize()
                        .layerBackdrop(windowAuroraBackdrop)
                        .background(animatedWindowBackground)
                        .then(
                            if (
                                !isInFullscreen &&
                                activeArtworkBackground == null &&
                                LocalAppBackgroundPreset.current == DataStoreManager.APP_BACKGROUND_AURORA
                            ) {
                                Modifier.auroraGradientBackground(isLightScheme)
                            } else {
                                Modifier
                            },
                        ),
                )
            }
        Scaffold(
            // Keep the shell and shared headers full width; only content and controls avoid the camera.
            contentWindowInsets =
                if (useLandscapeBottomBar) {
                    ScaffoldDefaults.contentWindowInsets.only(WindowInsetsSides.Vertical)
                } else {
                    ScaffoldDefaults.contentWindowInsets
                },
            containerColor =
                when {
                    isDesktopShell -> desktopWindow
                    useLandscapeBottomBar -> Color.Transparent
                    else -> MaterialTheme.colorScheme.background
                },
            bottomBar = {
                if (!isTablet || useLandscapeBottomBar) {
                    AnimatedVisibility(
                        visible = isNavBarVisible && !isSettingsScreen && !isInFullscreen,
                        enter =
                            fadeIn(tween(BOTTOM_BAR_VISIBILITY_DURATION_MS, easing = FastOutSlowInEasing)) +
                                slideInVertically(
                                    animationSpec = tween(BOTTOM_BAR_VISIBILITY_DURATION_MS, easing = FastOutSlowInEasing),
                                ) { fullHeight -> fullHeight },
                        exit =
                            fadeOut(tween(BOTTOM_BAR_HIDE_DURATION_MS, easing = FastOutSlowInEasing)) +
                                slideOutVertically(
                                    animationSpec = tween(BOTTOM_BAR_HIDE_DURATION_MS, easing = FastOutSlowInEasing),
                                ) { fullHeight -> fullHeight },
                    ) {
                        Box(
                            modifier =
                                Modifier
                                    .then(
                                        if ((useSlimNavigationDock && !useLandscapeBottomBar) || isShowMiniPlayer) {
                                            Modifier
                                        } else {
                                            Modifier.animateContentSize(
                                                animationSpec = tween(320, easing = FastOutSlowInEasing),
                                                // Animate removal only. Expanding this clipped box
                                                // reveals a new player as a thin strip before its card.
                                                // Keep the nav capsule at the window bottom while
                                                // that space shrinks, rather than lifting it first.
                                                alignment = Alignment.BottomCenter,
                                            )
                                        },
                                    ).graphicsLayer {
                                        val progress = currentSwipeProgressState.value.coerceIn(0f, 1f)
                                        // Let the card dissolve while the player arrives; a full dock-height
                                        // drop moved the two surfaces apart abruptly at the handoff.
                                        translationY = size.height * 0.35f * progress
                                        alpha = playerDockAlpha(progress)
                                    }.onSizeChanged(horizonDockState::updateSize),
                            contentAlignment = Alignment.BottomCenter,
                        ) {
                            // Keep the dock at the window bottom. The IME covers/reveals it
                            // in its own animation; moving or resizing it here causes a second,
                            // unsynchronised raise-and-sink transition and changes content padding.
                            if (useSlimNavigationDock && !useLandscapeBottomBar) {
                                MorphingSlimNavigationDock(
                                    navController = navController,
                                    backdrop = backdrop,
                                    expanded = isSlimDockExpanded,
                                    showMiniPlayer = isShowMiniPlayer,
                                    miniPlayerVisible = isMiniPlayerVisible,
                                    isDismissingMiniPlayer = isDismissingMiniPlayer,
                                    showAnalyticsTab = showAnalyticsTab,
                                    showMixForYouTab = showMixForYouTab,
                                    reloadDestinationIfNeeded = reloadDestinationIfNeeded,
                                    onDockExpandedChange = { expanded ->
                                        slimDockScrollRequests.tryReceive()
                                        slimDockScrollDistance[0] = 0f
                                        isSlimDockExpanded = expanded
                                    },
                                    onOpenNowPlaying = openNowPlaying,
                                    onDismissNowPlaying = dismissNowPlaying,
                                    onSwipeExpandProgress = updateMiniPlayerSwipeProgress,
                                    onCloseMiniPlayer = closeMiniPlayer,
                                    onOpenFullscreenLyrics = {
                                        viewModel.requestFullscreenLyrics()
                                        openNowPlaying()
                                    },
                                )
                            } else {
                                AnimatedContent(
                                    targetState = useSlimNavigationDock && !isSlimDockExpanded,
                                    modifier = Modifier.fillMaxWidth(),
                                    transitionSpec = {
                                        if (targetState) {
                                            (
                                                (fadeIn(tween(210, delayMillis = 35, easing = FastOutSlowInEasing)) +
                                                    slideInVertically(
                                                        animationSpec = tween(320, easing = FastOutSlowInEasing),
                                                    ) { height -> height / 5 } +
                                                    scaleIn(
                                                        initialScale = 0.92f,
                                                        animationSpec = tween(320, easing = FastOutSlowInEasing),
                                                    )) togetherWith
                                                    (fadeOut(tween(180, easing = FastOutSlowInEasing)) +
                                                        slideOutVertically(
                                                            animationSpec = tween(250, easing = FastOutSlowInEasing),
                                                        ) { height -> -height / 12 } +
                                                        scaleOut(
                                                            targetScale = 0.96f,
                                                            animationSpec = tween(260, easing = FastOutSlowInEasing),
                                                        ))
                                            ).using(
                                                SizeTransform(clip = false) { _, _ ->
                                                    tween(340, easing = FastOutSlowInEasing)
                                                },
                                            )
                                        } else {
                                            (
                                                (fadeIn(tween(270, delayMillis = 25, easing = FastOutSlowInEasing)) +
                                                    slideInVertically(
                                                        animationSpec = tween(360, easing = FastOutSlowInEasing),
                                                    ) { height -> height / 4 } +
                                                    scaleIn(
                                                        initialScale = 0.94f,
                                                        animationSpec = tween(360, easing = FastOutSlowInEasing),
                                                    )) togetherWith
                                                    (fadeOut(tween(180, easing = FastOutSlowInEasing)) +
                                                        slideOutVertically(
                                                            animationSpec = tween(260, easing = FastOutSlowInEasing),
                                                        ) { height -> height / 3 } +
                                                        scaleOut(
                                                            targetScale = 0.9f,
                                                            animationSpec = tween(280, easing = FastOutSlowInEasing),
                                                        ))
                                            ).using(
                                                SizeTransform(clip = false) { _, _ ->
                                                    tween(340, easing = FastOutSlowInEasing)
                                                },
                                            )
                                        }
                                    },
                                    label = "slim navigation dock transition",
                                ) { slimDockCollapsed ->
                                    if (useLandscapeBottomBar && !slimDockCollapsed) {
                                        BoxWithConstraints(
                                            Modifier
                                                .fillMaxWidth()
                                                .windowInsetsPadding(landscapeContentInsets)
                                                .windowInsetsPadding(
                                                    WindowInsets.navigationBars.only(WindowInsetsSides.Bottom),
                                                )
                                                .padding(horizontal = AppContentHorizontalInset)
                                                .padding(top = 4.dp, bottom = 8.dp),
                                        ) {
                                            val tabCount = 3 + (if (showAnalyticsTab) 1 else 0) + (if (showMixForYouTab) 1 else 0)
                                            // Give landscape glass tabs 2dp more than portrait and fill
                                            // their slot to the page's safe right edge. Keep flat sizing.
                                            val navigationTabWidth = if (useLiquidGlass) 94.dp else 96.dp
                                            val navigationEndInsets = if (useLiquidGlass) 8.dp else 12.dp
                                            val navigationWidth = (navigationTabWidth * tabCount + navigationEndInsets)
                                                .coerceAtMost(maxWidth * 0.5f)
                                            // Preserve the visible gap, including the old capsule's
                                            // unused leading half-slot, while filling its right edge.
                                            val previousNavigationWidth = (96.dp * tabCount + 12.dp).coerceAtMost(maxWidth * 0.5f)
                                            val previousCapsuleWidth = (92.dp * tabCount + 8.dp).coerceAtMost(previousNavigationWidth)
                                            val navigationGap = 12.dp + if (useLiquidGlass) {
                                                (previousNavigationWidth - previousCapsuleWidth) / 2
                                            } else {
                                                0.dp
                                            }
                                            Row(
                                                Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                            ) {
                                                Box(Modifier.weight(1f)) {
                                                    androidx.compose.animation.AnimatedVisibility(
                                                        visibleState = rememberMiniPlayerVisibility(isShowMiniPlayer && isMiniPlayerVisible),
                                                        enter = miniPlayerEnterTransition(),
                                                        exit = if (isDismissingMiniPlayer) {
                                                            fadeOut(tween(190, easing = FastOutSlowInEasing)) +
                                                                slideOutVertically(tween(220, easing = FastOutSlowInEasing)) { it / 5 }
                                                        } else {
                                                            ExitTransition.None
                                                        },
                                                    ) {
                                                        MiniPlayer(
                                                            Modifier.fillMaxWidth().height(
                                                                if (useLiquidGlass) {
                                                                    GlassBottomDockHeight
                                                                } else {
                                                                    FlatBottomDockHeight
                                                                },
                                                            ),
                                                            backdrop = backdrop,
                                                            onClick = openNowPlaying,
                                                            onDismissNowPlaying = dismissNowPlaying,
                                                            onSwipeExpandProgress = updateMiniPlayerSwipeProgress,
                                                            onClose = closeMiniPlayer,
                                                            onOpenFullscreenLyrics = {
                                                                viewModel.requestFullscreenLyrics()
                                                                openNowPlaying()
                                                            },
                                                        )
                                                    }
                                                }
                                                Spacer(Modifier.width(navigationGap))
                                                Box(Modifier.width(navigationWidth)) {
                                                    if (useLiquidGlass) {
                                                        LiquidGlassAppBottomNavigationBar(
                                                            navController = navController,
                                                            backdrop = backdrop,
                                                            viewModel = viewModel,
                                                            onOpenNowPlaying = openNowPlaying,
                                                            onDismissNowPlaying = dismissNowPlaying,
                                                            onSwipeExpandProgress = updateMiniPlayerSwipeProgress,
                                                            isScrolledToTop = isScrolledToTop,
                                                            showAnalyticsTab = showAnalyticsTab,
                                                            showMixForYouTab = showMixForYouTab,
                                                            reloadDestinationIfNeeded = reloadDestinationIfNeeded,
                                                            embedded = true,
                                                        )
                                                    } else {
                                                        AppBottomNavigationBar(
                                                            navController = navController,
                                                            showAnalyticsTab = showAnalyticsTab,
                                                            showMixForYouTab = showMixForYouTab,
                                                            reloadDestinationIfNeeded = reloadDestinationIfNeeded,
                                                            embedded = true,
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    } else if (slimDockCollapsed) {
                                        SlimNavigationDock(
                                            navController = navController,
                                            backdrop = backdrop,
                                            showMiniPlayer = isShowMiniPlayer,
                                            miniPlayerVisible = isMiniPlayerVisible,
                                            isDismissingMiniPlayer = isDismissingMiniPlayer,
                                            onOpenNowPlaying = openNowPlaying,
                                            onDismissNowPlaying = dismissNowPlaying,
                                            onSwipeExpandProgress = updateMiniPlayerSwipeProgress,
                                            onCloseMiniPlayer = closeMiniPlayer,
                                            reloadDestinationIfNeeded = reloadDestinationIfNeeded,
                                            modifier =
                                                if (useLandscapeBottomBar) {
                                                    Modifier.windowInsetsPadding(landscapeContentInsets)
                                                } else {
                                                    Modifier
                                                },
                                        )
                                    } else Column {
                                        // Shared full-player progress still moves this whole dock. This
                                        // transition only runs when playback creates a new player slot.
                                        AnimatedVisibility(
                                            visibleState = rememberMiniPlayerVisibility(
                                                isShowMiniPlayer &&
                                                    isMiniPlayerVisible &&
                                                    !useLiquidGlass,
                                            ),
                                            enter = miniPlayerEnterTransition(),
                                            exit = if (isDismissingMiniPlayer) {
                                                fadeOut(tween(190, easing = FastOutSlowInEasing)) +
                                                    slideOutVertically(tween(220, easing = FastOutSlowInEasing)) { it / 5 }
                                            } else {
                                                // Opening/closing the full player already moves this entire
                                                // surface with shared progress, so don't add another exit.
                                                ExitTransition.None
                                            },
                                        ) {
                                            MiniPlayer(
                                                Modifier
                                                    // 56dp card + the 4dp gap below.
                                                    .height(60.dp)
                                                    .fillMaxWidth()
                                                    .padding(
                                                        // Match the tighter 12dp side inset used by the main UI.
                                                        horizontal = AppContentHorizontalInset,
                                                    ).padding(
                                                        bottom = 4.dp,
                                                    ),
                                                backdrop = backdrop,
                                                onClick = {
                                                    openNowPlaying()
                                                },
                                                onDismissNowPlaying = dismissNowPlaying,
                                                onSwipeExpandProgress = updateMiniPlayerSwipeProgress,
                                                onClose = closeMiniPlayer,
                                            )
                                        }
                                        if (useLiquidGlass) {
                                            LiquidGlassAppBottomNavigationBar(
                                                navController = navController,
                                                backdrop = backdrop,
                                                viewModel = viewModel,
                                                onOpenNowPlaying = openNowPlaying,
                                                onDismissNowPlaying = dismissNowPlaying,
                                                onSwipeExpandProgress = updateMiniPlayerSwipeProgress,
                                                isScrolledToTop = isScrolledToTop,
                                                showAnalyticsTab = showAnalyticsTab,
                                                showMixForYouTab = showMixForYouTab,
                                                reloadDestinationIfNeeded = reloadDestinationIfNeeded,
                                            )
                                        } else {
                                            AppBottomNavigationBar(
                                                navController = navController,
                                                showAnalyticsTab = showAnalyticsTab,
                                                showMixForYouTab = showMixForYouTab,
                                                reloadDestinationIfNeeded = reloadDestinationIfNeeded,
                                            )
                                        }
                                    }
                                    }
                                }
                        }
                    }
                }
            },
            content = { innerPadding ->
                Box(
                    Modifier.fillMaxSize(),
                ) {
                    Row(
                        Modifier.fillMaxSize(),
                    ) {
                        if (isTablet && !useLandscapeBottomBar && !isInFullscreen && !isKeyboardVisible && !isSettingsScreen) {
                            AppNavigationRail(
                                navController = navController,
                                showAnalyticsTab = showAnalyticsTab,
                                showMixForYouTab = showMixForYouTab,
                                reloadDestinationIfNeeded = reloadDestinationIfNeeded,
                            )
                        }
                        // Desktop only: the content sits in its own rounded panel floating on a
                        // pure black window, Spotify style, while the rail stays flat black
                        // outside it. Phones keep one continuous surface — the inset only reads
                        // as deliberate when there is a window frame around it.
                        Box(
                            Modifier
                                .fillMaxSize()
                                .weight(1f)
                                .then(
                                    if (isDesktopShell) {
                                        Modifier
                                            .padding(8.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(desktopPanel)
                                    } else {
                                        Modifier
                                    },
                                ),
                        ) {
                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .nestedScroll(slimDockScrollConnection),
                            ) {
                                AppNavigationGraph(
                                    innerPadding = innerPadding,
                                    bottomContentInset = innerPadding.calculateBottomPadding() +
                                        if (isTablet && !useLandscapeBottomBar && isShowMiniPlayer &&
                                            isMiniPlayerVisible && !isInFullscreen && !isSettingsScreen
                                        ) {
                                            if (getPlatform() == Platform.Android) {
                                                if (useLiquidGlass) 56.dp else 60.dp
                                            } else {
                                                80.dp
                                            }
                                        } else {
                                            0.dp
                                        },
                                    windowAuroraBackdrop = windowAuroraBackdrop,
                                    contentInsets = landscapeContentInsets,
                                    navController = navController,
                                    backdrop = backdrop,
                                    hideNavBar = hideNavigationBar,
                                    showNavBar = showNavigationBar,
                                    showNowPlayingSheet = showNowPlayingSheet,
                                    onScrolling = onDestinationScrolling,
                                )
                            }
                            FrostedGlassHorizonHost(
                                backdrop = backdrop,
                                dockState = horizonDockState,
                                style = frostedGlassHorizonStyle,
                                visible = !isSettingsScreen && !isInFullscreen &&
                                    if (isTablet && !useLandscapeBottomBar) {
                                        isShowMiniPlayer && isMiniPlayerVisible
                                    } else {
                                        isNavBarVisible
                                    },
                                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                                    .graphicsLayer {
                                        alpha = playerDockAlpha(currentSwipeProgressState.value)
                                    },
                            )
                            // Keep this as a fallback only until the expanded player takes the
                            // top layer. Once it is on top, NowPlayingScreen's predictive handler
                            // must own the gesture so its live progress can drive the edge curves.
                            BackHandler(enabled = isShowNowPlaylistScreen && !isNowPlayingOverlayOnTop) {
                                dismissNowPlaying()
                            }
                            this@Row.AnimatedVisibility(
                                modifier =
                                    Modifier
                                        .padding(innerPadding)
                                        .align(Alignment.BottomCenter)
                                        .then(if (isTablet && !useLandscapeBottomBar) {
                                            Modifier.onSizeChanged(horizonDockState::updateSize)
                                        } else Modifier)
                                        .graphicsLayer {
                                            alpha = playerDockAlpha(currentSwipeProgressState.value)
                                        },
                                visibleState = rememberMiniPlayerVisibility(
                                    isShowMiniPlayer &&
                                        isTablet &&
                                        !useLandscapeBottomBar &&
                                        !isInFullscreen &&
                                        !isSettingsScreen &&
                                        isMiniPlayerVisible,
                                ),
                                enter = miniPlayerEnterTransition(),
                                exit = if (isDismissingMiniPlayer) {
                                    fadeOut(tween(190, easing = FastOutSlowInEasing)) +
                                        slideOutVertically(tween(220, easing = FastOutSlowInEasing)) { it / 5 }
                                } else {
                                    ExitTransition.None
                                },
                            ) {
                                MiniPlayer(
                                    if (getPlatform() == Platform.Android) {
                                        Modifier
                                            // Glass keeps its 52dp card; the flat one is 56dp.
                                            .height(if (useLiquidGlass) 56.dp else 60.dp)
                                            .fillMaxWidth(0.8f)
                                            .padding(
                                                horizontal = 12.dp,
                                            ).padding(
                                                bottom = 4.dp,
                                            )
                                    } else {
                                        // Floating capsule, Apple Music style: a fixed size so it never
                                        // stretches to the window width, and no haze at all — the capsule
                                        // paints its own liquid glass. Layering haze underneath blurs the
                                        // same pixels twice and reads as a dark smear, not glass.
                                        // padding BEFORE height: the other way round the bottom margin
                                        // eats into the 72dp and the capsule ends up 52dp tall.
                                        // 60dp is the floor for this layout: the content row is centred
                                        // on the capsule's axis and the 16dp progress box hangs off the
                                        // bottom, so the height has to cover the taller of the artwork
                                        // (32dp) and the two text lines (~33dp), plus that 16dp, plus a
                                        // gap. Going lower means shrinking the artwork again.
                                        Modifier
                                            .wrapContentWidth()
                                            .padding(bottom = 20.dp)
                                            .height(60.dp)
                                    },
                                    backdrop = backdrop,
                                    onClick = {
                                        openNowPlaying()
                                    },
                                    onDismissNowPlaying = dismissNowPlaying,
                                    onSwipeExpandProgress = updateMiniPlayerSwipeProgress,
                                    onClose = closeMiniPlayer,
                                    // The page lives in the Now Playing panel, so the panel opens with it.
                                    onOpenFullscreenLyrics = {
                                        viewModel.requestFullscreenLyrics()
                                        openNowPlaying()
                                    },
                                )
                            }
                        }
                        if (usePlayerSidePanel && !isInFullscreen) {
                            AnimatedVisibility(
                                isShowNowPlaylistScreen,
                                enter = expandHorizontally() + fadeIn(),
                                exit = fadeOut() + shrinkHorizontally(),
                            ) {
                                Row(
                                    Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(0.35f),
                                ) {
                                    Spacer(Modifier.width(8.dp))
                                    Box(
                                        Modifier
                                            .padding(
                                                innerPadding.copy(
                                                    start = 0.dp,
                                                    top = 0.dp,
                                                    bottom = 0.dp,
                                                ),
                                            ).then(
                                                // Matches the inset of the content panel so the two
                                                // read as a pair of floating cards, not one panel
                                                // with a seam down the middle.
                                                if (isDesktopShell) {
                                                    Modifier.padding(top = 8.dp, end = 8.dp, bottom = 8.dp)
                                                } else {
                                                    Modifier
                                                },
                                            ).clip(
                                                RoundedCornerShape(12.dp),
                                            ).then(
                                                if (isDesktopShell) {
                                                    Modifier.background(desktopPanel)
                                                } else {
                                                    Modifier
                                                },
                                            ),
                                    ) {
                                        ForceDarkContent {
                                            NowPlayingScreenContent(
                                                navController = navController,
                                                sharedViewModel = viewModel,
                                                isExpanded = true,
                                                dismissIcon = WaveIcons.ArrowForwardIos,
                                            ) {
                                                isShowNowPlaylistScreen = false
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if ((hasOpenedNowPlayingScreen || isPreparingNowPlayingScreen) && isShowMiniPlayer && !usePlayerSidePanel) {
                    ForceDarkContent {
                        NowPlayingScreen(
                            sharedViewModel = viewModel,
                            navController = navController,
                            swipeTransitionProgress = currentSwipeProgressState,
                            isOnTop = isNowPlayingOverlayOnTop,
                            isClosing = isNowPlayingClosing,
                            onReverseClose = openNowPlaying,
                            onPredictiveBackProgress = updatePredictiveBackProgress,
                            onPredictiveBackCancelled = clearPredictiveBackProgress,
                            onPredictiveBackCommitted = dismissNowPlaying,
                            onDismiss = dismissNowPlaying,
                        )
                    }
                }

                if (sleepTimerState.isDone) {
                    Logger.w("MainActivity", "Sleep Timer Done: $sleepTimerState")
                    AmoledAlertDialog(
                        properties =
                            DialogProperties(
                                dismissOnBackPress = false,
                                dismissOnClickOutside = false,
                            ),
                        onDismissRequest = {
                            viewModel.stopSleepTimer()
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                viewModel.stopSleepTimer()
                            }) {
                                Text(
                                    stringResource(Res.string.yes),
                                    style = typo().bodySmall,
                                )
                            }
                        },
                        text = {
                            Text(
                                stringResource(Res.string.sleep_timer_off),
                                style = typo().labelSmall,
                            )
                        },
                        title = {
                            Text(
                                stringResource(Res.string.good_night),
                                style = typo().bodySmall,
                            )
                        },
                    )
                }

                if (shouldShowUpdateDialog) {
                    val response = updateData ?: return@Scaffold
                    AmoledAlertDialog(
                        properties =
                            DialogProperties(
                                dismissOnBackPress = false,
                                dismissOnClickOutside = false,
                            ),
                        onDismissRequest = {
                            shouldShowUpdateDialog = false
                            viewModel.showedUpdateDialog = false
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    shouldShowUpdateDialog = false
                                    viewModel.showedUpdateDialog = false
                                    openUrl("https://github.com/petesamuel1120-dotcom/WaveMusic/releases/latest")
                                },
                            ) {
                                Text(
                                    stringResource(Res.string.download),
                                    style = typo().bodySmall,
                                )
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    shouldShowUpdateDialog = false
                                    viewModel.showedUpdateDialog = false
                                },
                            ) {
                                Text(
                                    stringResource(Res.string.cancel),
                                    style = typo().bodySmall,
                                )
                            }
                        },
                        title = {
                            Text(
                                stringResource(Res.string.update_available),
                                style = typo().labelSmall,
                            )
                        },
                        text = {
                            val formatted =
                                response.releaseTime?.let { input ->
                                    try {
                                        val instant = kotlin.time.Instant.parse(input)
                                        val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
                                        dateTime.format(
                                            LocalDateTime.Format {
                                                day()
                                                char(' ')
                                                monthName(MonthNames.ENGLISH_ABBREVIATED)
                                                char(' ')
                                                year()
                                                char(' ')
                                                hour()
                                                char(':')
                                                minute()
                                                char(':')
                                                second()
                                            },
                                        )
                                    } catch (e: Exception) {
                                        stringResource(Res.string.unknown)
                                    }
                                } ?: stringResource(Res.string.unknown)

                            val updateMessage =
                                runBlocking {
                                    getString(
                                        Res.string.update_message,
                                        response.tagName,
                                        formatted,
                                    )
                                }
                            Column(
                                Modifier
                                    .heightIn(
                                        max = 400.dp,
                                    ).verticalScroll(
                                        rememberScrollState(),
                                    ),
                            ) {
                                Text(
                                    text = updateMessage,
                                    style = typo().labelMedium,
                                    modifier =
                                        Modifier.padding(
                                            vertical = 8.dp,
                                        ),
                                )
                                Markdown(
                                    response.body,
                                    typography =
                                        markdownTypography(
                                            h1 = typo().labelLarge,
                                            h2 = typo().labelMedium,
                                            h3 = typo().labelSmall,
                                            text = typo().bodySmall,
                                            bullet = typo().bodySmall,
                                            paragraph = typo().bodySmall,
                                            textLink =
                                                TextLinkStyles(
                                                    SpanStyle(
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Normal,
                                                        fontFamily = fontFamily(),
                                                        textDecoration = TextDecoration.Underline,
                                                    ),
                                                ),
                                        ),
                                )
                            }
                        },
                    )
                }

                if (showNotificationPermissionDialog || showDesktopNotificationPermissionDialog) {
                    var doNotShowAgain by remember { mutableStateOf(false) }
                    val dismissPermissionDialog = {
                        if (showDesktopNotificationPermissionDialog) {
                            onDismissDesktopNotificationPermissionDialog(doNotShowAgain)
                        } else {
                            viewModel.dismissNotificationPermissionDialog(doNotShowAgain)
                        }
                    }
                    AmoledAlertDialog(
                        onDismissRequest = {
                            dismissPermissionDialog()
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    if (showDesktopNotificationPermissionDialog) {
                                        onOpenDesktopNotificationSettings(doNotShowAgain)
                                    } else {
                                        viewModel.dismissNotificationPermissionDialog(doNotShowAgain)
                                    }
                                },
                            ) {
                                Text(
                                    stringResource(
                                        if (showDesktopNotificationPermissionDialog) {
                                            Res.string.settings
                                        } else {
                                            Res.string.yes
                                        },
                                    ),
                                    style = typo().bodySmall,
                                )
                            }
                        },
                        dismissButton =
                            if (showDesktopNotificationPermissionDialog) {
                                {
                                    TextButton(onClick = dismissPermissionDialog) {
                                        Text(
                                            stringResource(Res.string.cancel),
                                            style = typo().bodySmall,
                                        )
                                    }
                                }
                            } else {
                                null
                            },
                        title = {
                            Text(
                                stringResource(Res.string.notification),
                                style = typo().labelSmall,
                            )
                        },
                        text = {
                            Column {
                                Text(
                                    stringResource(Res.string.this_app_needs_to_access_your_notification),
                                    style = typo().bodySmall,
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier =
                                        Modifier
                                            .clickable { doNotShowAgain = !doNotShowAgain }
                                            .fillMaxWidth(),
                                ) {
                                    Checkbox(
                                        checked = doNotShowAgain,
                                        onCheckedChange = { doNotShowAgain = it },
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        stringResource(Res.string.do_not_show_again),
                                        style = typo().bodySmall,
                                    )
                                }
                            }
                        },
                    )
                }
            },
        )
        }
        }
    }
}
