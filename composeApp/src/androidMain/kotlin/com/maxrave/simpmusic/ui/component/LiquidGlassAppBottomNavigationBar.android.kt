package com.maxrave.simpmusic.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.Dimension
import androidx.constraintlayout.compose.Visibility
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.currentBackStackEntryAsState
import com.maxrave.domain.data.player.GenericMediaItem
import com.maxrave.simpmusic.expect.ui.PlatformBackdrop
import com.maxrave.simpmusic.ui.navigation.destination.home.AnalyticsDestination
import com.maxrave.simpmusic.ui.navigation.destination.home.HomeDestination
import com.maxrave.simpmusic.ui.navigation.destination.library.LibraryDestination
import com.maxrave.simpmusic.ui.navigation.destination.library.MixForYouDestination
import com.maxrave.simpmusic.ui.navigation.destination.search.SearchDestination
import com.maxrave.simpmusic.ui.screen.MiniPlayer
import com.maxrave.simpmusic.viewModel.SharedViewModel
import kotlin.reflect.KClass

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
actual fun LiquidGlassAppBottomNavigationBar(
    startDestination: Any,
    navController: NavController,
    backdrop: PlatformBackdrop,
    viewModel: SharedViewModel,
    isScrolledToTop: Boolean,
    showAnalyticsTab: Boolean,
    showMixForYouTab: Boolean,
    onOpenNowPlaying: () -> Unit,
    onSwipeExpandProgress: (Float) -> Unit,
    reloadDestinationIfNeeded: (KClass<*>) -> Unit,
    embedded: Boolean,
) {
    // Keep a fixed luminance for the glass. Repeatedly rasterizing the full bar into a bitmap just
    // to sample 25 pixels created avoidable GPU readbacks and crossed the UI/render thread boundary
    // on some Android devices. The mini-player uses the same stable mid-tone value.
    val glassLuminance = 0.5f
    val nowPlayingData by viewModel.nowPlayingState.collectAsStateWithLifecycle()
    // MiniPlayer visibility: derived, never stored.
    //
    // This is a second copy of the rule App.kt applies to the plain bottom bar, and it carried the
    // same two faults. rememberSaveable(true) makes the first composition assert "a track is
    // playing" before anything knows — nowPlayingState starts null and only fills in once the
    // service has connected and the queue has been restored — and the LaunchedEffect that
    // corrected it could only run AFTER that frame had already been drawn. So the bar laid itself
    // out with the mini player, dropped it, then brought it back, animating each step through
    // decoupledConstraints.
    //
    // Fixing the copy in App.kt did nothing here, because with liquid glass on it is THIS file
    // that draws the mini player.
    val isShowMiniPlayer by remember {
        derivedStateOf {
            val item = nowPlayingData?.mediaItem
            item != null && item != GenericMediaItem.EMPTY
        }
    }

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val bottomNavScreens =
        remember(showAnalyticsTab, showMixForYouTab) {
            listOfNotNull(
                BottomNavScreen.Home,
                BottomNavScreen.MixForYou.takeIf { showMixForYouTab },
                BottomNavScreen.Analytics.takeIf { showAnalyticsTab },
                BottomNavScreen.Library,
                BottomNavScreen.Search,
            )
        }
    val barTabs =
        remember(showAnalyticsTab, showMixForYouTab) {
            listOfNotNull(
                BottomNavScreen.Home,
                BottomNavScreen.MixForYou.takeIf { showMixForYouTab },
                BottomNavScreen.Search,
                BottomNavScreen.Analytics.takeIf { showAnalyticsTab },
                BottomNavScreen.Library,
            )
        }
    var selectedIndex by rememberSaveable {
        mutableIntStateOf(
            when (startDestination) {
                is HomeDestination -> BottomNavScreen.Home.ordinal
                is SearchDestination -> BottomNavScreen.Search.ordinal
                is LibraryDestination -> BottomNavScreen.Library.ordinal
                is AnalyticsDestination -> BottomNavScreen.Analytics.ordinal
                is MixForYouDestination -> BottomNavScreen.MixForYou.ordinal
                else -> BottomNavScreen.Home.ordinal // Default to Home if not recognized
            },
        )
    }
    // A tab can disappear from the bar under the user: tracking gets turned off while Analytics is
    // selected, or the YouTube session ends while Mix for you is. Fall back to Home in both cases so
    // nothing is left highlighted.
    LaunchedEffect(showAnalyticsTab, showMixForYouTab) {
        if ((!showAnalyticsTab && selectedIndex == BottomNavScreen.Analytics.ordinal) ||
            (!showMixForYouTab && selectedIndex == BottomNavScreen.MixForYou.ordinal)
        ) {
            selectedIndex = BottomNavScreen.Home.ordinal
        }
    }
    LaunchedEffect(currentBackStackEntry, bottomNavScreens) {
        navController.activeTabRoot(bottomNavScreens)?.let { selectedIndex = it.ordinal }
    }
    // Keep the full glass tab bar in one fixed layout on every destination, including Search.
    // Keep both slots stable. Animating ConstraintLayout's visibility/width here made the player
    // card shrink toward its anchor as the track was cleared, while the nav capsule also shifted.
    val constraintSet = remember { decoupledConstraints() }
    var isDismissingMiniPlayer by remember { mutableStateOf(false) }
    LaunchedEffect(isShowMiniPlayer) {
        if (isShowMiniPlayer) isDismissingMiniPlayer = false
    }

    val selectTabWhenSettled: (BottomNavScreen) -> Unit = { screen ->
        val currentDestination = navController.currentDestination
        val isAtTabRoot = currentDestination?.hasRoute(screen.destination::class) == true

        if (isAtTabRoot) {
            selectedIndex = screen.ordinal
            reloadDestinationIfNeeded(screen.destination::class)
        } else {
            val currentTab = navController.activeTabRoot(bottomNavScreens)
            selectedIndex = screen.ordinal
            navController.navigateToTabRoot(screen, currentTab)
            if (screen == BottomNavScreen.Library && currentTab == BottomNavScreen.Library) {
                reloadDestinationIfNeeded(screen.destination::class)
            }
        }
    }
    val selectTab =
        rememberSettledTabSelection(
            navController = navController,
            tabs = bottomNavScreens,
            currentBackStackEntry = currentBackStackEntry,
            onSelectSettledTab = selectTabWhenSettled,
        )

    if (embedded) {
        // Reuse the portrait capsule in the landscape dock. Its parent supplies the mini-player
        // and handles system insets once for both surfaces.
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            BoxWithConstraints(Modifier.weight(1f, fill = false)) {
                LiquidGlassTabBar(
                    tabs = barTabs,
                    selectedTab = barTabs.indexOfFirst { it.ordinal == selectedIndex },
                    backdrop = backdrop,
                    luminance = glassLuminance,
                    availableWidth = maxWidth,
                    fillAvailableWidth = true,
                    onTabSelected = { screen -> selectTab(screen) },
                )
            }
        }
        return
    }

    ConstraintLayout(
        constraintSet = constraintSet,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    WindowInsets.navigationBars.asPaddingValues(),
                ).padding(
                    bottom = 8.dp,
                ),
    ) {
        /** LTR: HOME -> MIX FOR YOU -> SEARCH -> ANALYTICS -> LIBRARY */
        Row(
            verticalAlignment = Alignment.CenterVertically,
            // Center the single capsule as one unit.
            horizontalArrangement = Arrangement.Center,
            modifier =
                Modifier
                    .then(
                        Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                    ).layoutId("toolbar"),
        ) {
            BoxWithConstraints(Modifier.weight(1f, fill = false)) {
                LiquidGlassTabBar(
                    tabs = barTabs,
                    selectedTab = barTabs.indexOfFirst { it.ordinal == selectedIndex },
                    backdrop = backdrop,
                    luminance = glassLuminance,
                    availableWidth = maxWidth,
                    onTabSelected = { screen -> selectTab(screen) },
                )
            }
        }
        AnimatedVisibility(
            visibleState = rememberMiniPlayerVisibility(isShowMiniPlayer),
            modifier = Modifier.layoutId("miniPlayer"),
            enter = miniPlayerEnterTransition(),
            exit = if (isDismissingMiniPlayer) {
                fadeOut(tween(190, easing = FastOutSlowInEasing)) +
                    slideOutVertically(tween(220, easing = FastOutSlowInEasing)) { it / 5 }
            } else {
                // Opening the full player is already driven by the shared player progress.
                // Avoid adding another exit animation when that progress moves the whole bar.
                ExitTransition.None
            },
        ) {
            MiniPlayer(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .height(56.dp),
                backdrop = backdrop,
                onClick = onOpenNowPlaying,
                onDismissNowPlaying = { onSwipeExpandProgress(0f) },
                onSwipeExpandProgress = onSwipeExpandProgress,
                onClose = {
                    isDismissingMiniPlayer = true
                    viewModel.stopPlayer()
                    viewModel.isServiceRunning = false
                },
            )
        }
    }
}

private fun decoupledConstraints(): ConstraintSet =
    ConstraintSet {
        val toolbar = createRefFor("toolbar")
        constrain(toolbar) {
            bottom.linkTo(parent.bottom)
            height = Dimension.wrapContent
            // The full bar always spans the screen, including while Search is open.
            width = Dimension.fillToConstraints
            start.linkTo(parent.start)
            end.linkTo(parent.end)
            visibility = Visibility.Visible
        }
        val miniPlayer = createRefFor("miniPlayer")
        constrain(miniPlayer) {
            start.linkTo(parent.start)
            end.linkTo(parent.end)
            bottom.linkTo(toolbar.top, margin = 12.dp)
            width = Dimension.matchParent
        }
    }
