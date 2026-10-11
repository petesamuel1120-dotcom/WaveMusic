package com.maxrave.simpmusic.ui.navigation.graph

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
import com.maxrave.simpmusic.expect.ui.PlatformBackdrop
import com.maxrave.simpmusic.expect.Orientation
import com.maxrave.simpmusic.expect.currentOrientation
import com.maxrave.simpmusic.expect.ui.layerBackdrop
import com.maxrave.simpmusic.expect.ui.rememberBackdrop
import com.maxrave.simpmusic.Platform
import com.maxrave.simpmusic.getPlatform
import com.maxrave.domain.manager.DataStoreManager
import com.maxrave.simpmusic.ui.component.FrostedHeader
import com.maxrave.simpmusic.ui.component.LocalPageContentInsets
import com.maxrave.simpmusic.ui.component.LandscapeHeaderScrollState
import com.maxrave.simpmusic.ui.component.LocalLandscapeHeaderScrollState
import com.maxrave.simpmusic.ui.component.SharedHeaderContent
import com.maxrave.simpmusic.ui.component.SharedHeaderContentHeight
import com.maxrave.simpmusic.ui.component.LocalSharedHeaderHosted
import com.maxrave.simpmusic.ui.component.LocalSharedHeaderState
import com.maxrave.simpmusic.ui.component.LocalSharedHeaderHazeState
import com.maxrave.simpmusic.ui.component.LocalPageAuroraBackdrop
import com.maxrave.simpmusic.ui.component.LocalPageBottomInset
import com.maxrave.simpmusic.ui.component.SharedHeaderState
import com.maxrave.simpmusic.ui.component.BOTTOM_NAV_TRANSITION_DURATION_MILLIS
import com.maxrave.simpmusic.ui.component.auroraGradientBackground
import com.maxrave.simpmusic.ui.component.YouTubeAccountDialog
import com.maxrave.simpmusic.ui.navigation.destination.home.AnalyticsDestination
import com.maxrave.simpmusic.ui.navigation.destination.home.HomeDestination
import com.maxrave.simpmusic.ui.navigation.destination.home.MoodDestination
import com.maxrave.simpmusic.ui.navigation.destination.home.NotificationDestination
import com.maxrave.simpmusic.ui.navigation.destination.home.RecentlySongsDestination
import com.maxrave.simpmusic.ui.navigation.destination.home.SettingsDestination
import com.maxrave.simpmusic.ui.navigation.destination.home.LiquidGlassSettingsDestination
import com.maxrave.simpmusic.ui.navigation.destination.home.WrappedDestination
import com.maxrave.simpmusic.ui.theme.ForceDarkContent
import com.maxrave.simpmusic.ui.theme.LocalLiquidGlassEnabled
import com.maxrave.simpmusic.ui.theme.desktopPanelDark
import com.maxrave.simpmusic.ui.navigation.destination.library.LibraryDestination
import com.maxrave.simpmusic.ui.navigation.destination.library.LibraryDynamicPlaylistDestination
import com.maxrave.simpmusic.ui.navigation.destination.library.MixForYouDestination
import com.maxrave.simpmusic.ui.navigation.destination.list.BrowseDestination
import com.maxrave.simpmusic.ui.navigation.destination.list.ArtistDestination
import com.maxrave.simpmusic.ui.navigation.destination.list.MoreAlbumsDestination
import com.maxrave.simpmusic.ui.navigation.destination.list.PlaylistDestination
import com.maxrave.simpmusic.ui.navigation.destination.player.FullscreenDestination
import com.maxrave.simpmusic.ui.navigation.destination.search.SearchDestination
import com.maxrave.simpmusic.ui.screen.home.HomeScreen
import com.maxrave.simpmusic.ui.screen.home.HomeTopAppBar
import com.maxrave.simpmusic.ui.screen.home.analytics.AnalyticsScreen
import com.maxrave.simpmusic.ui.screen.home.wrapped.WrappedScreen
import com.maxrave.simpmusic.ui.screen.library.LibraryScreen
import com.maxrave.simpmusic.ui.screen.library.MixForYouScreen
import com.maxrave.simpmusic.ui.screen.other.SearchScreen
import com.maxrave.simpmusic.ui.screen.other.SearchScreenHeader
import com.maxrave.simpmusic.ui.screen.other.rememberSearchHeaderState
import com.maxrave.simpmusic.ui.screen.player.FullscreenPlayer
import com.maxrave.simpmusic.viewModel.SettingsViewModel
import com.maxrave.simpmusic.viewModel.SharedViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.jetbrains.compose.resources.stringResource
import simpmusic.composeapp.generated.resources.Res
import simpmusic.composeapp.generated.resources.home
import simpmusic.composeapp.generated.resources.library
import dev.chrisbanes.haze.hazeSource
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
    val statusBarHeight = with(density) { WindowInsets.statusBars.getTop(this).toDp() }
    val searchHeaderState = rememberSearchHeaderState()
    var sharedAccountInfo by remember { mutableStateOf<Pair<String?, String?>?>(null) }
    var showYouTubeAccountDialog by rememberSaveable { mutableStateOf(false) }
    val sharedViewModel: SharedViewModel = koinInject()
    val pageTransitionStyle by sharedViewModel.getPageTransitionStyle().collectAsState(DataStoreManager.PAGE_TRANSITION_FADE)
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
    val sharedHeaderHazeState = rememberHazeState()
    val sharedHeaderState = remember { SharedHeaderState() }
    val destination = backStackEntry?.destination
    val fullBleedArtworkPage =
        destination?.hasRoute(ArtistDestination::class) == true ||
            destination?.hasRoute(PlaylistDestination::class) == true
    val isHome = destination?.hasRoute(HomeDestination::class) ?: (startDestination is HomeDestination)
    val isLibrary = destination?.hasRoute(LibraryDestination::class) ?: (startDestination is LibraryDestination)
    val isSearch = destination?.hasRoute(SearchDestination::class) == true
    val sharedHeaderContentHeight = SharedHeaderContentHeight
    val sharedHeaderHeight = statusBarHeight + sharedHeaderContentHeight
    val usesSharedHeader = isHome || isLibrary || isSearch
    val showsSharedHeader = usesSharedHeader || destination.hasSharedPageHeader()
    val pageHeader = sharedHeaderState.contentFor(backStackEntry?.id)
    val pageHeaderPresentation =
        if (usesSharedHeader) null else pageHeader?.value ?: sharedHeaderState.lastHeader
    val entryLifecycle = backStackEntry?.lifecycle?.currentStateFlow?.collectAsState()?.value
    val pageBackdrop = pageHeader?.value?.backdrop
    val headerBackdrop = pageBackdrop?.takeIf {
        entryLifecycle == Lifecycle.State.RESUMED && sharedHeaderState.isBackdropReady(it)
    } ?: backdrop
    val isAndroid = getPlatform() == Platform.Android
    val useScrollingLandscapeHeader = isAndroid && currentOrientation() == Orientation.LANDSCAPE
    // New destinations and orientation changes begin with their controls fully visible.
    val headerTravelPx = with(density) { sharedHeaderHeight.toPx() }
    val landscapeHeaderScrollState = remember(backStackEntry?.id, useScrollingLandscapeHeader, headerTravelPx) {
        LandscapeHeaderScrollState(if (useScrollingLandscapeHeader) headerTravelPx else 0f)
    }
    val isFullscreen =
        destination?.hasRoute(FullscreenDestination::class)
            ?: (startDestination is FullscreenDestination)
    val isWrapped =
        destination?.hasRoute(WrappedDestination::class)
            ?: (startDestination is WrappedDestination)
    var lastPrimaryHeaderIsHome by rememberSaveable { mutableStateOf(isHome) }
    SideEffect {
        if (isHome || isLibrary) {
            lastPrimaryHeaderIsHome = isHome
        }
    }
    val headerTitleIsHome = if (isHome || isLibrary) isHome else lastPrimaryHeaderIsHome
    val primaryHeaderTitle = stringResource(if (headerTitleIsHome) Res.string.home else Res.string.library)
    val primaryHeaderContent: SharedHeaderContent = {
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
            )
        }
    }
    SideEffect {
        if (!showsSharedHeader) sharedHeaderState.lastHeader = null
    }
    val sharedHeaderEnterTransition =
        if (pageTransitionStyle == DataStoreManager.PAGE_TRANSITION_FADE) {
            pageEnterTransition()
        } else {
            fadeIn(tween(BOTTOM_NAV_TRANSITION_DURATION_MILLIS, easing = FastOutSlowInEasing))
        }
    val sharedHeaderExitTransition =
        if (pageTransitionStyle == DataStoreManager.PAGE_TRANSITION_FADE) {
            pageExitTransition()
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
    CompositionLocalProvider(
        LocalLandscapeHeaderScrollState provides landscapeHeaderScrollState.takeIf { useScrollingLandscapeHeader },
    ) {
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
                if (LocalLiquidGlassEnabled.current) {
                    Modifier.hazeSource(sharedHeaderHazeState)
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
                    .background(tabBackground)
                    .auroraGradientBackground(isLightTheme),
            )
        }
    }
    CompositionLocalProvider(
        LocalSharedHeaderHazeState provides sharedHeaderHazeState,
        LocalSharedHeaderState provides sharedHeaderState,
        LocalPageAuroraBackdrop provides pageAuroraBackdrop.takeUnless { isFullscreen || isWrapped },
        LocalPageBottomInset provides bottomContentInset,
        LocalPageContentInsets provides contentInsets,
    ) {
    NavHost(
        navController,
        modifier = Modifier.fillMaxSize().then(
            if (useScrollingLandscapeHeader) {
                Modifier
                    .then(if (fullBleedArtworkPage) Modifier else Modifier.windowInsetsPadding(contentInsets))
                    .clipToBounds()
                    .nestedScroll(landscapeHeaderScrollState.connection)
            } else {
                Modifier
            },
        ),
        startDestination = startDestination,
        enterTransition = {
            if (pageTransitionStyle == DataStoreManager.PAGE_TRANSITION_SLIDE) {
                pageSlideEnterTransition(isPop = false)
            } else {
                pageEnterTransition()
            }
        },
        exitTransition = {
            if (pageTransitionStyle == DataStoreManager.PAGE_TRANSITION_SLIDE) {
                pageSlideExitTransition(isPop = false)
            } else {
                pageExitTransition()
            }
        },
        popEnterTransition = {
            if (pageTransitionStyle == DataStoreManager.PAGE_TRANSITION_SLIDE) {
                pageSlideEnterTransition(isPop = true)
            } else {
                pageEnterTransition()
            }
        },
        popExitTransition = {
            if (pageTransitionStyle == DataStoreManager.PAGE_TRANSITION_SLIDE) {
                pageSlideExitTransition(isPop = true)
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
            headerTint = tabBackground,
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
                        .height(sharedHeaderHeight),
                tint = pageHeaderPresentation?.tint ?: tabBackground,
                fadeToContent = (pageHeaderPresentation?.fadeToContent ?: true) &&
                    !sharedHeaderState.hasPortraitSearch(backStackEntry?.id),
                backdrop = headerBackdrop,
                showSurface =
                    when {
                        isSearch -> false
                        isHome -> sharedHeaderState.homeSurfaceVisible
                        isLibrary -> sharedHeaderState.librarySurfaceVisible
                        else -> pageHeaderPresentation?.showSurface ?: true
                    },
                pinInLandscape = pageHeaderPresentation?.pinInLandscape == true,
            ) {
                Box(
                    modifier = Modifier.fillMaxSize()
                        .windowInsetsPadding(contentInsets)
                        .windowInsetsPadding(WindowInsets.statusBars),
                    contentAlignment = Alignment.Center,
                ) {
                    val headerContent =
                        if (usesSharedHeader) primaryHeaderContent
                        else pageHeaderPresentation?.content
                    if (headerContent != null) {
                        SideEffect {
                            if (showsSharedHeader && pageHeaderPresentation != null) {
                                sharedHeaderState.lastHeader = pageHeaderPresentation
                            }
                        }
                        CompositionLocalProvider(LocalSharedHeaderHosted provides true) {
                            headerContent()
                        }
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
}

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
            it.hasRoute(NotificationDestination::class)
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
