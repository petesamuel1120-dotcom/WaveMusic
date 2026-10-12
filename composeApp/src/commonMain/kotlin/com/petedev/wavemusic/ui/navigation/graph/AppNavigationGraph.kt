package com.petedev.wavemusic.ui.navigation.graph

import androidx.compose.ui.draw.drawBehind

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.RectangleShape
import com.kyant.backdrop.drawPlainBackdrop
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.petedev.wavemusic.expect.ui.PlatformBackdrop
import com.petedev.wavemusic.expect.Orientation
import com.petedev.wavemusic.expect.currentOrientation
import com.petedev.wavemusic.expect.ui.layerBackdrop
import com.petedev.wavemusic.expect.ui.rememberBackdrop
import com.petedev.wavemusic.Platform
import com.petedev.wavemusic.getPlatform
import com.petedev.wavemusic.domain.manager.DataStoreManager
import com.petedev.wavemusic.ui.component.FrostedHeader
import com.petedev.wavemusic.ui.component.PersistentLiquidGlassHeaderBackdrop
import com.petedev.wavemusic.ui.component.HeaderBackdropExtensionHeight
import com.petedev.wavemusic.ui.component.LocalPageContentInsets
import com.petedev.wavemusic.ui.component.SharedHeaderContent
import com.petedev.wavemusic.ui.component.SharedHeaderContentHeight
import com.petedev.wavemusic.ui.component.LocalSharedHeaderHosted
import com.petedev.wavemusic.ui.component.SharedHeaderControlsMorphHost
import com.petedev.wavemusic.ui.component.LocalSharedHeaderState
import com.petedev.wavemusic.ui.component.LocalPageAuroraBackdrop
import com.petedev.wavemusic.ui.component.LocalPageArtworkBackground
import com.petedev.wavemusic.ui.component.usesPageArtworkBackground
import com.petedev.wavemusic.ui.component.LocalAppBackgroundPreset
import com.petedev.wavemusic.ui.component.LocalPageBottomInset
import com.petedev.wavemusic.ui.component.SharedHeaderState
import com.petedev.wavemusic.ui.component.BOTTOM_NAV_TRANSITION_DURATION_MILLIS
import com.petedev.wavemusic.ui.component.slideV2EnterTransition
import com.petedev.wavemusic.ui.component.slideV2ExitTransition
import com.petedev.wavemusic.ui.component.slideV2FadeIn
import com.petedev.wavemusic.ui.component.slideV2FadeOut
import com.petedev.wavemusic.ui.component.slideV2MovingForward
import com.petedev.wavemusic.ui.component.SLIDE_V2_EXIT_MILLIS
import com.petedev.wavemusic.ui.component.auroraGradientBackground
import com.petedev.wavemusic.ui.component.YouTubeAccountDialog
import com.petedev.wavemusic.ui.navigation.destination.home.AnalyticsDestination
import com.petedev.wavemusic.ui.navigation.destination.home.HomeDestination
import com.petedev.wavemusic.ui.navigation.destination.home.MoodDestination
import com.petedev.wavemusic.ui.navigation.destination.home.NotificationDestination
import com.petedev.wavemusic.ui.navigation.destination.home.RecentlySongsDestination
import com.petedev.wavemusic.ui.navigation.destination.home.SettingsDestination
import com.petedev.wavemusic.ui.navigation.destination.home.ListenTogetherSettingsDestination
import com.petedev.wavemusic.ui.navigation.destination.home.LiquidGlassSettingsDestination
import com.petedev.wavemusic.ui.navigation.destination.home.WrappedDestination
import com.petedev.wavemusic.ui.theme.ForceDarkContent
import com.petedev.wavemusic.ui.theme.LocalLiquidGlassEnabled
import com.petedev.wavemusic.ui.theme.LocalFrostedGlassHeaderStyle
import com.petedev.wavemusic.ui.theme.LocalReduceVisualEffects
import kotlinx.coroutines.delay
import com.petedev.wavemusic.ui.theme.desktopPanelDark
import com.petedev.wavemusic.ui.navigation.destination.library.LibraryDestination
import com.petedev.wavemusic.ui.navigation.destination.library.LibraryDynamicPlaylistDestination
import com.petedev.wavemusic.ui.navigation.destination.library.MixForYouDestination
import com.petedev.wavemusic.ui.navigation.destination.list.AlbumDestination
import com.petedev.wavemusic.ui.navigation.destination.list.ArtistDestination
import com.petedev.wavemusic.ui.navigation.destination.list.BrowseDestination
import com.petedev.wavemusic.ui.navigation.destination.list.LocalPlaylistDestination
import com.petedev.wavemusic.ui.navigation.destination.list.MoreAlbumsDestination
import com.petedev.wavemusic.ui.navigation.destination.list.PlaylistDestination
import com.petedev.wavemusic.ui.navigation.destination.list.PodcastDestination
import com.petedev.wavemusic.ui.navigation.destination.player.FullscreenDestination
import com.petedev.wavemusic.ui.navigation.destination.search.SearchDestination
import com.petedev.wavemusic.ui.screen.home.HomeScreen
import com.petedev.wavemusic.ui.screen.home.HomeTopAppBar
import com.petedev.wavemusic.ui.screen.home.analytics.AnalyticsScreen
import com.petedev.wavemusic.ui.screen.home.wrapped.WrappedScreen
import com.petedev.wavemusic.ui.screen.library.LibraryScreen
import com.petedev.wavemusic.ui.screen.library.MixForYouScreen
import com.petedev.wavemusic.ui.screen.other.SearchScreen
import com.petedev.wavemusic.ui.screen.other.SearchScreenHeader
import com.petedev.wavemusic.ui.screen.other.rememberSearchHeaderState
import com.petedev.wavemusic.ui.screen.player.FullscreenPlayer
import com.petedev.wavemusic.viewModel.SettingsViewModel
import com.petedev.wavemusic.viewModel.SharedViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.jetbrains.compose.resources.stringResource
import com.petedev.wavemusic.resources.Res
import androidx.compose.ui.graphics.graphicsLayer
import com.petedev.wavemusic.resources.home
import com.petedev.wavemusic.resources.library
import com.petedev.wavemusic.resources.library_your_playlists
import dev.chrisbanes.haze.rememberHazeState

private const val IMMERSIVE_BACKDROP_FADE_MILLIS = 460
private const val PAGE_FADE_IN_MILLIS = 260
private const val PAGE_FADE_OUT_MILLIS = 170

@Composable
@ExperimentalMaterial3Api
@ExperimentalFoundationApi
fun AppNavigationGraph(
    innerPadding: PaddingValues,
    navController: NavHostController,
    backdrop: PlatformBackdrop,
    windowAuroraBackdrop: PlatformBackdrop? = null,
    contentInsets: WindowInsets = WindowInsets(0, 0, 0, 0),
    startDestination: Any = HomeDestination,
    hideNavBar: () -> Unit = { },
    showNavBar: (shouldShowNowPlayingSheet: Boolean) -> Unit = { },
    showNowPlayingSheet: () -> Unit = {},
    onScrolling: (onTop: Boolean) -> Unit = {},
    bottomContentInset: Dp = innerPadding.calculateBottomPadding(),
    ) {
    val density = LocalDensity.current
    val statusBarHeight = with(density) { WindowInsets.statusBars.union(WindowInsets.displayCutout).getTop(this).toDp() }
    val searchHeaderState = rememberSearchHeaderState()
    var sharedAccountInfo by remember { mutableStateOf<Pair<String?, String?>?>(null) }
    var showYouTubeAccountDialog by rememberSaveable { mutableStateOf(false) }
    val sharedViewModel: SharedViewModel = koinInject()
    val fixedHeader by sharedViewModel.fixedHeader.collectAsState()
    val pinHeaderButtons = fixedHeader
    val pageTransitionStyle by sharedViewModel.getPageTransitionStyle().collectAsState(DataStoreManager.PAGE_TRANSITION_SLIDE_V2)
    val reduceVisualEffects = LocalReduceVisualEffects.current
    val effectivePageTransitionStyle =
        if (reduceVisualEffects) DataStoreManager.PAGE_TRANSITION_SLIDE_V2 else pageTransitionStyle
    val accountSettingsViewModel: SettingsViewModel = koinViewModel()
    val loggedIn by accountSettingsViewModel.loggedIn.collectAsState()
    LaunchedEffect(accountSettingsViewModel) {
        accountSettingsViewModel.getLoggedIn()
    }
    // Back-stack updates recompose this graph while Home/Library are animating. Keep the callback
    // identity stable so those large LazyColumn destinations don't recompose just because the
    // header/account owner recomposed.
    val updateSharedAccountInfo: (Pair<String?, String?>?) -> Unit = remember(loggedIn) {
        { incoming ->
            val hasCachedAvatar = !sharedAccountInfo?.second.isNullOrBlank()
            when {
                (loggedIn == null || loggedIn == DataStoreManager.TRUE) &&
                    incoming?.second.isNullOrBlank() && hasCachedAvatar -> Unit

                loggedIn == null || loggedIn == DataStoreManager.TRUE -> sharedAccountInfo = incoming
                else -> sharedAccountInfo = null
            }
        }
    }
    LaunchedEffect(loggedIn) {
        if (loggedIn != null && loggedIn != DataStoreManager.TRUE) {
            sharedAccountInfo = null
        }
    }
    val openYouTubeAccount = remember { { showYouTubeAccountDialog = true } }
    val backStackEntry by navController.currentBackStackEntryAsState()
    var previousBackStackEntryId by remember { mutableStateOf<String?>(null) }
    val sharedHeaderHazeState = rememberHazeState()
    val sharedHeaderState = remember { SharedHeaderState() }
    LaunchedEffect(sharedHeaderState, pinHeaderButtons) {
        sharedHeaderState.setHeaderButtonsPinned(pinHeaderButtons)
    }
    val destination = backStackEntry?.destination
    val fullBleedArtworkPage =
        destination?.hasRoute(ArtistDestination::class) == true ||
            destination?.hasRoute(PlaylistDestination::class) == true ||
            destination?.hasRoute(PodcastDestination::class) == true
    val isHome = destination?.hasRoute(HomeDestination::class) ?: (startDestination is HomeDestination)
    val isLibrary = destination?.hasRoute(LibraryDestination::class) ?: (startDestination is LibraryDestination)
    val isSearch = destination?.hasRoute(SearchDestination::class) == true
    val isPageRouteTransitioning =
        sharedHeaderState.pageTransitionInProgress ||
            (previousBackStackEntryId != null && backStackEntry?.id != null && previousBackStackEntryId != backStackEntry?.id)
    LaunchedEffect(backStackEntry?.id) {
        val currentEntryId = backStackEntry?.id
        val previousEntryId = previousBackStackEntryId
        previousBackStackEntryId = currentEntryId
        sharedHeaderState.updatePrimaryHeaderVisibility(true)
        // Each newly opened page starts with its complete header visible.
        sharedHeaderState.resetHeaderOffset()
        if (destination?.hasRoute(SettingsDestination::class) == true ||
            destination?.hasRoute(LiquidGlassSettingsDestination::class) == true
        ) {
            // Settings starts with visible controls, then its own scroll observer takes over.
            // Never inherit Home's partially collapsed offset when opening a settings page.
            sharedHeaderState.resetHeaderOffset()
        }
        if (previousEntryId != null && currentEntryId != null && previousEntryId != currentEntryId) {
            sharedHeaderState.updatePageTransitionInProgress(true)
            val transitionDurationMillis =
                when (effectivePageTransitionStyle) {
                    DataStoreManager.PAGE_TRANSITION_FADE -> PAGE_FADE_IN_MILLIS
                    DataStoreManager.PAGE_TRANSITION_SLIDE -> BOTTOM_NAV_TRANSITION_DURATION_MILLIS
                    DataStoreManager.PAGE_TRANSITION_SLIDE_V2 -> SLIDE_V2_EXIT_MILLIS
                    else -> PAGE_FADE_IN_MILLIS
                }
            delay(transitionDurationMillis.toLong())
            sharedHeaderState.updatePageTransitionInProgress(false)
        }
    }
    val sharedHeaderContentHeight = SharedHeaderContentHeight
    val sharedHeaderHeight = statusBarHeight + sharedHeaderContentHeight
    val usesSharedHeader = isHome || isLibrary || isSearch
    val showsSharedHeader = destination.hasLiquidGlassHeader()
    val pageHeader = sharedHeaderState.contentFor(backStackEntry?.id)
    val isShowingOutgoingPageHeader =
        !usesSharedHeader && pageHeader == null &&
            sharedHeaderState.lastHeaderEntryId != backStackEntry?.id &&
            sharedHeaderState.lastHeader != null
    val pageHeaderPresentation =
        if (isShowingOutgoingPageHeader) {
            sharedHeaderState.lastHeader?.value
        } else if (usesSharedHeader) {
            null
        } else {
            pageHeader?.value ?: sharedHeaderState.lastHeader?.takeIf {
                sharedHeaderState.lastHeaderEntryId == backStackEntry?.id
            }?.value
        }
    val entryLifecycle = backStackEntry?.lifecycle?.currentStateFlow?.collectAsState()?.value
    // Capture the composed viewport, including both pages' transition transforms.
    // A page-local capture remains stationary while its parent slides on the GPU.
    val headerBackdrop = backdrop
    val isAndroid = getPlatform() == Platform.Android
    val useLandscapeContentInsets = isAndroid && currentOrientation() == Orientation.LANDSCAPE
    val isFullscreen =
        destination?.hasRoute(FullscreenDestination::class)
            ?: (startDestination is FullscreenDestination)
    val isWrapped =
        destination?.hasRoute(WrappedDestination::class)
            ?: (startDestination is WrappedDestination)
    var lastPrimaryHeaderIsHome by rememberSaveable { mutableStateOf(isHome) }
    SideEffect {
        if ((isHome || isLibrary) && entryLifecycle == Lifecycle.State.RESUMED) {
            lastPrimaryHeaderIsHome = isHome
        }
    }
    val headerTitleIsHome =
        if ((isHome || isLibrary) && entryLifecycle == Lifecycle.State.RESUMED) {
            isHome
        } else {
            lastPrimaryHeaderIsHome
        }
    val showingYourPlaylistsPage = isLibrary && sharedHeaderState.libraryPlaylistBackAction != null
    val primaryHeaderTitle =
        stringResource(
            when {
                headerTitleIsHome -> Res.string.home
                showingYourPlaylistsPage -> Res.string.library_your_playlists
                else -> Res.string.library
            },
        )
    val primaryHeaderContent: SharedHeaderContent = key(backStackEntry?.id) { {
        if (isSearch) {
            SearchScreenHeader(
                state = searchHeaderState,
                backdrop = backdrop,
                contentHeight = sharedHeaderContentHeight,
            )
        } else {
            HomeTopAppBar(
                navController = navController,
                accountInfo = sharedAccountInfo,
                title = primaryHeaderTitle,
                onOpenYouTubeAccount = openYouTubeAccount,
                contentHeight = sharedHeaderContentHeight,
                showingPlaylistPage = showingYourPlaylistsPage,
                onBackFromPlaylistPage = { sharedHeaderState.libraryPlaylistBackAction?.invoke() },
            )
        }
    } }
    SideEffect {
        // Primary tabs provide their own header. Drop the previous page header there so it
        // cannot briefly reappear as the fallback when a page route is opened again.
        if (!isPageRouteTransitioning && (usesSharedHeader || !showsSharedHeader)) {
            sharedHeaderState.lastHeader = null
            sharedHeaderState.lastHeaderEntryId = null
        }
    }
    val sharedHeaderEnterTransition =
        if (effectivePageTransitionStyle == DataStoreManager.PAGE_TRANSITION_FADE) {
            pageEnterTransition()
        } else if (effectivePageTransitionStyle == DataStoreManager.PAGE_TRANSITION_SLIDE_V2) {
            slideV2FadeIn()
        } else {
            fadeIn(tween(BOTTOM_NAV_TRANSITION_DURATION_MILLIS, easing = FastOutSlowInEasing))
        }
    val sharedHeaderExitTransition =
        if (effectivePageTransitionStyle == DataStoreManager.PAGE_TRANSITION_FADE) {
            pageExitTransition()
        } else if (effectivePageTransitionStyle == DataStoreManager.PAGE_TRANSITION_SLIDE_V2) {
            slideV2FadeOut()
        } else {
            fadeOut(tween(BOTTOM_NAV_TRANSITION_DURATION_MILLIS, easing = FastOutSlowInEasing))
        }

    val themeBackground = MaterialTheme.colorScheme.background
    val isLightTheme = themeBackground.luminance() > 0.5f
    val tabBackground =
        if (getPlatform() == Platform.Desktop) {
            if (isLightTheme) MaterialTheme.colorScheme.surfaceContainer else desktopPanelDark
        } else {
            themeBackground
        }

    val pageAuroraBackdrop = windowAuroraBackdrop ?: rememberBackdrop(tabBackground)
    val pageArtworkBackground = LocalPageArtworkBackground.current
    val activeArtworkBackground =
        pageArtworkBackground?.value.takeIf {
            destination.usesPageArtworkBackground()
        }
    val animatedPageBackground = animateColorAsState(
        targetValue = activeArtworkBackground ?: tabBackground,
        animationSpec = tween(BOTTOM_NAV_TRANSITION_DURATION_MILLIS, easing = FastOutSlowInEasing),
        label = "pageArtworkBackground",
    )
    SideEffect {
        if (!destination.usesPageArtworkBackground()) {
            pageArtworkBackground?.value = null
        }
    }
    Box(Modifier.fillMaxSize().background(tabBackground)) {
    // Capture the ambient background together with destination content. Capturing the NavHost
    // alone painted its solid base over the aurora behind transparent pages and their headers.
    Box(
        Modifier
            .fillMaxSize()
            .then(
                if (LocalLiquidGlassEnabled.current) {
                    Modifier.layerBackdrop(backdrop)
                } else {
                    Modifier
                },
            )
            .then(
                if (windowAuroraBackdrop != null) {
                    Modifier.drawPlainBackdrop(
                        backdrop = windowAuroraBackdrop,
                        shape = { RectangleShape },
                        effects = {},
                    )
                } else {
                    Modifier
                },
            ),
    ) {
    // Landscape reuses the full-window capture instead of restarting the gradient in its inset.
    if (windowAuroraBackdrop == null) {
        // Keep one shared aurora fixed behind normal destinations so pages fade through the same
        // visual space. Artwork screens paint their own background above it; coverless pages reveal
        // this layer instead of switching to a disconnected solid surface.
        AnimatedVisibility(
            visible = !isFullscreen && !isWrapped,
            enter = fadeIn(tween(IMMERSIVE_BACKDROP_FADE_MILLIS, easing = FastOutSlowInEasing)),
            exit = fadeOut(tween(IMMERSIVE_BACKDROP_FADE_MILLIS, easing = FastOutSlowInEasing)),
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .then(
                        if (LocalLiquidGlassEnabled.current) {
                            Modifier.layerBackdrop(pageAuroraBackdrop)
                        } else {
                            Modifier
                        },
                    )
                    .drawBehind { drawRect(animatedPageBackground.value) }
                    .then(
                        if (
                            activeArtworkBackground == null &&
                            LocalAppBackgroundPreset.current == DataStoreManager.APP_BACKGROUND_AURORA
                        ) {
                            Modifier.auroraGradientBackground(isLightTheme)
                        } else {
                            Modifier
                        },
                    ),
            )
        }
    }
    CompositionLocalProvider(
        LocalSharedHeaderState provides sharedHeaderState,
        LocalPageAuroraBackdrop provides pageAuroraBackdrop.takeUnless { isFullscreen || isWrapped },
        LocalPageBottomInset provides bottomContentInset,
        LocalPageContentInsets provides contentInsets,
    ) {
    NavHost(
        navController,
        modifier = Modifier.fillMaxSize().then(
            if (useLandscapeContentInsets) {
                Modifier
                    .then(if (fullBleedArtworkPage) Modifier else Modifier.windowInsetsPadding(contentInsets))
                    .clipToBounds()
            } else {
                Modifier
            },
        ),
        startDestination = startDestination,
        enterTransition = {
            if (effectivePageTransitionStyle == DataStoreManager.PAGE_TRANSITION_SLIDE) {
                pageSlideEnterTransition(isPop = false)
            } else if (effectivePageTransitionStyle == DataStoreManager.PAGE_TRANSITION_SLIDE_V2) {
                slideV2EnterTransition(isSlideV2MovingForward(isPop = false))
            } else {
                pageEnterTransition()
            }
        },
        exitTransition = {
            if (effectivePageTransitionStyle == DataStoreManager.PAGE_TRANSITION_SLIDE) {
                pageSlideExitTransition(isPop = false)
            } else if (effectivePageTransitionStyle == DataStoreManager.PAGE_TRANSITION_SLIDE_V2) {
                slideV2ExitTransition(isSlideV2MovingForward(isPop = false))
            } else {
                pageExitTransition()
            }
        },
        popEnterTransition = {
            if (effectivePageTransitionStyle == DataStoreManager.PAGE_TRANSITION_SLIDE) {
                pageSlideEnterTransition(isPop = true)
            } else if (effectivePageTransitionStyle == DataStoreManager.PAGE_TRANSITION_SLIDE_V2) {
                slideV2EnterTransition(isSlideV2MovingForward(isPop = true))
            } else {
                pageEnterTransition()
            }
        },
        popExitTransition = {
            if (effectivePageTransitionStyle == DataStoreManager.PAGE_TRANSITION_SLIDE) {
                pageSlideExitTransition(isPop = true)
            } else if (effectivePageTransitionStyle == DataStoreManager.PAGE_TRANSITION_SLIDE_V2) {
                slideV2ExitTransition(isSlideV2MovingForward(isPop = true))
            } else {
                pageExitTransition()
            }
        },
        // Keep the NavHost viewport fixed while destinations transition. The default size
        // transform can visibly resize the whole page when rapid tab changes interrupt one another.
        sizeTransform = { null },
    ) {
        // Bottom bar destinations
        composable<HomeDestination> {
            HomeScreen(
                onScrolling = onScrolling,
                navController = navController,
                sharedHeaderHeight = sharedHeaderHeight,
                onAccountInfoChanged = updateSharedAccountInfo,
            )
        }
        composable<SearchDestination> {
            SearchScreen(
                navController = navController,
                sharedHeaderHeight = sharedHeaderHeight,
                headerState = searchHeaderState,
            )
        }
        composable<LibraryDestination> {
            LibraryScreen(
                innerPadding = innerPadding,
                navController = navController,
                onScrolling = onScrolling,
                sharedHeaderHeight = sharedHeaderHeight,
                onAccountInfoChanged = updateSharedAccountInfo,
                useFadeForCategoryTransitions = effectivePageTransitionStyle == DataStoreManager.PAGE_TRANSITION_FADE,
                useSlideV2ForCategoryTransitions = effectivePageTransitionStyle == DataStoreManager.PAGE_TRANSITION_SLIDE_V2,
            )
        }
        // Only reachable as a tab while signed in to YouTube
        composable<MixForYouDestination> {
            MixForYouScreen(
                innerPadding = innerPadding,
                navController = navController,
                onScrolling = onScrolling,
                onOpenYouTubeAccount = openYouTubeAccount,
            )
        }
        // Only reachable as a tab while local tracking is enabled.
        // ForceDarkContent for the same reason as album/playlist/artist: the page background comes
        // from the artwork via toImmersiveBackground(), which always lands dark, so the light
        // theme's dark-on-light text and icons would be unreadable.
        composable<AnalyticsDestination> {
            ForceDarkContent {
                AnalyticsScreen(
                    navController = navController,
                    innerPadding = innerPadding,
                )
            }
        }
        // Reached only from the Analytics screen's entry banner, so it inherits that screen's
        // gate on local tracking. ForceDarkContent for a different reason than Analytics: the reel
        // is drawn on its own near-black ground whatever the user's theme is, because it is an
        // event and because every card is also a share image that has to survive leaving the app.
        composable<WrappedDestination> {
            ForceDarkContent {
                WrappedScreen(
                    navController = navController,
                    hideNavBar = hideNavBar,
                    // Deliberately not the fullscreen player's `showNavBar(true)` + open sheet:
                    // leaving the reel goes back to Analytics, and raising the Now Playing sheet
                    // over it would be a screen the user never asked for.
                    showNavBar = { showNavBar(false) },
                )
            }
        }
        composable<FullscreenDestination> {
            ForceDarkContent {
                FullscreenPlayer(
                    navController,
                    hideNavBar = hideNavBar,
                    showNavBar = {
                        showNavBar.invoke(true)
                        showNowPlayingSheet.invoke()
                    },
                )
            }
        }
        // Home screen graph
        homeScreenGraph(
            innerPadding = innerPadding,
            navController = navController,
            onOpenYouTubeAccount = openYouTubeAccount,
        )
        // Library screen graph
        libraryScreenGraph(
            innerPadding = innerPadding,
            navController = navController,
            sharedHeaderHeight = sharedHeaderHeight,
            headerTint = tabBackground,
            useSlideV2TitleTransition = effectivePageTransitionStyle == DataStoreManager.PAGE_TRANSITION_SLIDE_V2,
        )
        // List screen graph
        listScreenGraph(
            innerPadding = innerPadding,
            navController = navController,
        )
        // Login screen graph
        loginScreenGraph(
            innerPadding = innerPadding,
            navController = navController,
            hideBottomBar = hideNavBar,
            showBottomBar = {
                showNavBar(false)
            },
        )
    }
    }
    }
        // This pane samples the fixed viewport and sits outside both the source and the
        // translated header. Scrolling never recreates it or moves its sampling coordinates.
        // The controls keep their own lenses and LocalHeaderSurfaceProgress remains unchanged.
        if (LocalFrostedGlassHeaderStyle.current.isEnabled && LocalLiquidGlassEnabled.current) {
            AnimatedVisibility(
                visible = showsSharedHeader,
                enter = sharedHeaderEnterTransition,
                exit = sharedHeaderExitTransition,
            ) {
                PersistentLiquidGlassHeaderBackdrop(
                    backdrop = backdrop,
                    modifier = Modifier.fillMaxWidth().height(sharedHeaderHeight + HeaderBackdropExtensionHeight),
                    tint = pageHeaderPresentation?.tint ?: tabBackground,
                )
            }
        }
        AnimatedVisibility(
            visible = showsSharedHeader,
            enter = sharedHeaderEnterTransition,
            exit = sharedHeaderExitTransition,
        ) {
            FrostedHeader(
                // One persistent viewport source drives the frost on every shared-header route.
                // Page backdrops remain available for the controls, without switching blur inputs
                // when lifecycle/readiness changes or when the scrolling page re-registers.
                hazeState = sharedHeaderHazeState,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(sharedHeaderHeight)
                        .graphicsLayer {
                            translationY = if (isSearch || pinHeaderButtons) 0f else sharedHeaderState.headerOffsetPx
                        },
                tint = pageHeaderPresentation?.tint ?: tabBackground,
                fadeToContent = (pageHeaderPresentation?.fadeToContent ?: true) &&
                    !sharedHeaderState.hasPortraitSearch(backStackEntry?.id),
                backdrop = headerBackdrop,
                // Flat controls and their backdrop share the scroll offset on every page.
                // Surface visibility does not depend on the artwork title docking.
                showSurface =
                    if (LocalLiquidGlassEnabled.current) {
                        false
                    } else {
                        true
                    },
            ) {
                Box(
                    modifier = Modifier.fillMaxSize()
                        .windowInsetsPadding(contentInsets)
                        .windowInsetsPadding(WindowInsets.statusBars.union(WindowInsets.displayCutout)),
                    contentAlignment = Alignment.Center,
                ) {
                    val headerContent =
                        if (isShowingOutgoingPageHeader) pageHeaderPresentation?.content
                        else if (usesSharedHeader) primaryHeaderContent
                        else pageHeaderPresentation?.content
                    val headerEntryKey =
                        if (isShowingOutgoingPageHeader) sharedHeaderState.lastHeaderEntryId
                        else backStackEntry?.id
                    if (headerContent != null) {
                        SideEffect {
                            if (
                                showsSharedHeader &&
                                !isPageRouteTransitioning &&
                                pageHeader?.value != null
                            ) {
                                sharedHeaderState.lastHeader = pageHeader
                                sharedHeaderState.lastHeaderEntryId = backStackEntry?.id
                            }
                        }
                    }
                    CompositionLocalProvider(LocalSharedHeaderHosted provides true) {
                        SharedHeaderControlsMorphHost(
                            entryKey = headerEntryKey ?: "primaryHeader",
                            content = headerContent,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
        }
        if (showYouTubeAccountDialog) {
            YouTubeAccountDialog(
                isLoggedIn = loggedIn?.let { it == DataStoreManager.TRUE },
                viewModel = accountSettingsViewModel,
                navController = navController,
                onDismiss = { showYouTubeAccountDialog = false },
            )
        }
    }
}

/** Shared by header rendering and the window's status-bar transparency policy. */
internal fun NavDestination?.hasLiquidGlassHeader(): Boolean =
    this?.let {
        it.hasRoute(HomeDestination::class) ||
            it.hasRoute(LibraryDestination::class) ||
            it.hasRoute(SearchDestination::class)
    } == true || hasSharedPageHeader()

private fun NavDestination?.hasSharedPageHeader(): Boolean =
    this?.let {
        it.hasRoute(SettingsDestination::class) ||
            it.hasRoute(LiquidGlassSettingsDestination::class) ||
            it.hasRoute(BrowseDestination::class) ||
            it.hasRoute(MoreAlbumsDestination::class) ||
            it.hasRoute(LibraryDynamicPlaylistDestination::class) ||
            it.hasRoute(MoodDestination::class) ||
            it.hasRoute(MixForYouDestination::class) ||
            it.hasRoute(RecentlySongsDestination::class) ||
            it.hasRoute(NotificationDestination::class) ||
            it.hasRoute(PlaylistDestination::class) ||
            it.hasRoute(LocalPlaylistDestination::class) ||
            it.hasRoute(AlbumDestination::class) ||
            it.hasRoute(ArtistDestination::class) ||
            it.hasRoute(PodcastDestination::class)
    } == true

/** A quick fade-through with no directional movement, scale shift, or start delay. */
private fun pageEnterTransition(): EnterTransition =
    fadeIn(
        animationSpec = tween(PAGE_FADE_IN_MILLIS, easing = LinearOutSlowInEasing),
    )

private fun pageExitTransition(): ExitTransition =
    fadeOut(
        animationSpec = tween(PAGE_FADE_OUT_MILLIS, easing = FastOutLinearInEasing),
    )

private fun AnimatedContentTransitionScope<NavBackStackEntry>.pageSlideEnterTransition(isPop: Boolean): EnterTransition {
    val movingForward = isMovingForwardByRoute(isPop)
    return slideInHorizontally(
        animationSpec = tween(BOTTOM_NAV_TRANSITION_DURATION_MILLIS, easing = FastOutSlowInEasing),
        initialOffsetX = { fullWidth -> if (movingForward) fullWidth else -fullWidth },
    )
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.pageSlideExitTransition(isPop: Boolean): ExitTransition {
    val movingForward = isMovingForwardByRoute(isPop)
    return slideOutHorizontally(
        animationSpec = tween(BOTTOM_NAV_TRANSITION_DURATION_MILLIS, easing = FastOutSlowInEasing),
        targetOffsetX = { fullWidth -> if (movingForward) -fullWidth else fullWidth },
    )
}

/** Directs the slide by route order, so moving to a later route advances left and back routes reverse. */
private fun AnimatedContentTransitionScope<NavBackStackEntry>.isMovingForwardByRoute(isPop: Boolean): Boolean {
    val initialOrder = routeTransitionOrder(initialState.destination)
    val targetOrder = routeTransitionOrder(targetState.destination)
    return if (initialOrder == targetOrder) !isPop else targetOrder > initialOrder
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.isSlideV2MovingForward(isPop: Boolean): Boolean =
    slideV2MovingForward(
        routeTransitionOrder(initialState.destination).takeIf { it in 0..4 },
        routeTransitionOrder(targetState.destination).takeIf { it in 0..4 },
        isPop,
    )

private fun routeTransitionOrder(destination: NavDestination): Int =
    when {
        destination.hasRoute(HomeDestination::class) -> 0
        destination.hasRoute(MixForYouDestination::class) -> 1
        destination.hasRoute(SearchDestination::class) -> 2
        destination.hasRoute(AnalyticsDestination::class) -> 3
        destination.hasRoute(LibraryDestination::class) -> 4
        destination.hasRoute(FullscreenDestination::class) -> 20
        destination.hasRoute(WrappedDestination::class) -> 21
        else -> 10 // Settings and detail routes sit after the primary tabs.
    }
