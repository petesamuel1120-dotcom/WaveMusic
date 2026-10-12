package com.petedev.wavemusic.ui.component

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.currentBackStackEntryAsState
import com.petedev.wavemusic.extension.greyScale
import com.petedev.wavemusic.ui.navigation.destination.home.AnalyticsDestination
import com.petedev.wavemusic.ui.navigation.destination.home.HomeDestination
import com.petedev.wavemusic.ui.navigation.destination.library.LibraryDestination
import com.petedev.wavemusic.ui.navigation.destination.library.MixForYouDestination
import com.petedev.wavemusic.ui.navigation.destination.search.SearchDestination
import com.petedev.wavemusic.ui.theme.typo
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import com.petedev.wavemusic.resources.*
import kotlin.reflect.KClass

/**
 * The bottom bar with liquid glass OFF: the same floating capsule as the glass bar, drawn
 * flat. Home, Search, and Library share one capsule, with optional tabs around Search. A sliding
 * rounded indicator replaces the frosted blob, and solid theme surfaces replace refraction.
 */
@Composable
fun AppBottomNavigationBar(
    startDestination: Any = HomeDestination,
    navController: NavController,
    showAnalyticsTab: Boolean = false,
    showMixForYouTab: Boolean = false,
    reloadDestinationIfNeeded: (KClass<*>) -> Unit = { _ -> },
    embedded: Boolean = false,
) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    // `ordinal` identifies a tab, it is NOT the position — Mix for you and Analytics sit before
    // Library here while keeping the ordinal they were declared with, so that the numbering stays
    // stable whether or not those tabs are present.
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
    // A tab can disappear from the list under the user: tracking gets turned off while Analytics is
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
    val selectTabWhenSettled: (BottomNavScreen) -> Unit = { screen ->
        val currentDestination = navController.currentDestination
        val isAtTabRoot = currentDestination?.hasRoute(screen.destination::class) == true

        if (isAtTabRoot) {
            selectedIndex = screen.ordinal
            reloadDestinationIfNeeded(screen.destination::class)
        } else {
            val activeTab = navController.activeTabRoot(bottomNavScreens)
            selectedIndex = screen.ordinal
            navController.navigateToTabRoot(screen, activeTab)
            if (screen == BottomNavScreen.Library && activeTab == BottomNavScreen.Library) {
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

    val capsuleColor = flatChromeBackground()
    val indicatorColor =
        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            .compositeOver(capsuleColor.copy(alpha = 1f))
            .copy(alpha = capsuleColor.alpha)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        // Keep the single tab capsule centered at its measured width.
        horizontalArrangement = Arrangement.Center,
        modifier =
            Modifier
                .fillMaxWidth()
                .then(
                    if (embedded) {
                        // Landscape's dock owns the outer spacing and system insets.
                        Modifier
                    } else {
                        Modifier.windowInsetsPadding(NavigationBarDefaults.windowInsets)
                            .padding(horizontal = 12.dp)
                            .padding(top = 4.dp, bottom = 8.dp)
                    },
                ),
    ) {
        BoxWithConstraints(Modifier.weight(1f, fill = false)) {
            // Every tab the same width, capped so two tabs on a wide screen do not stretch into
            // slabs — the same budget rule as the glass tab bar.
            val tabWidth = ((maxWidth - CapsuleInset * 2) / barTabs.size).coerceAtMost(FlatTabWidth)
            val selectedPosition = barTabs.indexOfFirst { it.ordinal == selectedIndex }
            val indicatorOffset = animateDpAsState(
                tabWidth * selectedPosition.coerceAtLeast(0),
                animationSpec = tween(
                    durationMillis = BOTTOM_NAV_TRANSITION_DURATION_MILLIS,
                    easing = FastOutSlowInEasing,
                ),
                label = "flatBarIndicator",
            )
            Box(
                modifier =
                    Modifier
                        .height(FlatBarHeight)
                        .clip(BottomDockShape)
                        .background(capsuleColor)
                        .padding(horizontal = CapsuleInset),
                contentAlignment = Alignment.CenterStart,
            ) {
                // The sliding indicator — the flat stand-in for the glass bar's frosted blob.
                if (selectedPosition >= 0) {
                    Box(
                        modifier =
                            Modifier
                                // Keep the moving pill in the render layer. Reading the animated
                                // value in composition/offset caused the whole tab row to re-place
                                // on every frame during Home ↔ Library changes.
                                .graphicsLayer {
                                    translationX = indicatorOffset.value.toPx()
                                }
                                .size(width = tabWidth, height = FlatIndicatorHeight)
                                .clip(FlatIndicatorShape)
                                .background(indicatorColor),
                    )
                }
                Row {
                    barTabs.forEach { screen ->
                        val selected = selectedIndex == screen.ordinal
                        val contentColor =
                            if (selected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        Column(
                            modifier =
                                Modifier
                                    .width(tabWidth)
                                    .fillMaxHeight()
                                    .clip(FlatIndicatorShape)
                                    .clickable { selectTab(screen) },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            CompositionLocalProvider(LocalContentColor provides contentColor) {
                                screen.icon()
                            }
                            Text(
                                stringResource(screen.title),
                                style = typo().bodySmall,
                                color = contentColor,
                                maxLines = 1,
                                modifier = if (screen == BottomNavScreen.Home || screen == BottomNavScreen.Library) Modifier.offset(y = (-3).dp) else Modifier,
                            )
                        }
                    }
                }
            }
        }
    }
}

// Mirrors the glass tab bar's geometry (TabWidth/BarHeight/BlobHeight/BarInset in
// LiquidGlassTabBar.android.kt) so the two bars are one form in two materials.
private val FlatTabWidth = 96.dp
private val FlatBarHeight = FlatBottomDockHeight
private val FlatIndicatorHeight = 56.dp
private val CapsuleInset = 6.dp
private val FlatIndicatorShape =
    bottomDockSelectionShape(FlatBarHeight, (FlatBarHeight - FlatIndicatorHeight) / 2)

@Composable
fun AppNavigationRail(
    startDestination: Any = HomeDestination,
    navController: NavController,
    showAnalyticsTab: Boolean = false,
    showMixForYouTab: Boolean = false,
    reloadDestinationIfNeeded: (KClass<*>) -> Unit = { _ -> },
) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    // See the note in AppBottomNavigationBar: `ordinal` is the tab's identity, not its position.
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
    // A tab can disappear from the list under the user: tracking gets turned off while Analytics is
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
    val selectRailTabWhenSettled: (BottomNavScreen) -> Unit = { screen ->
        val currentDestination = navController.currentDestination
        val isAtTabRoot = currentDestination?.hasRoute(screen.destination::class) == true
        if (isAtTabRoot) {
            selectedIndex = screen.ordinal
            reloadDestinationIfNeeded(screen.destination::class)
        } else {
            val activeTab = navController.activeTabRoot(bottomNavScreens)
            selectedIndex = screen.ordinal
            navController.navigateToTabRoot(screen, activeTab)
            if (screen == BottomNavScreen.Library && activeTab == BottomNavScreen.Library) {
                reloadDestinationIfNeeded(screen.destination::class)
            }
        }
    }
    val selectRailTab =
        rememberSettledTabSelection(
            navController = navController,
            tabs = bottomNavScreens,
            currentBackStackEntry = currentBackStackEntry,
            onSelectSettledTab = selectRailTabWhenSettled,
        )
    NavigationRail {
        Spacer(Modifier.height(16.dp))
        Box(Modifier.padding(horizontal = 16.dp)) {
            Box(
                Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(Res.drawable.mono),
                    contentDescription = null,
                    modifier =
                        Modifier
                            .height(32.dp)
                            .clip(CircleShape),
                )
            }
        }
        Spacer(Modifier.weight(1f))
        bottomNavScreens.forEach { screen ->
            NavigationRailItem(
                icon = screen.icon,
                label = {
                    Text(
                        stringResource(screen.title),
                        style =
                            if (selectedIndex == screen.ordinal) {
                                typo().bodySmall
                            } else {
                                typo().bodySmall.greyScale()
                            },
                    )
                },
                selected = selectedIndex == screen.ordinal,
                onClick = { selectRailTab(screen) },
            )
        }
        Spacer(Modifier.height(32.dp))
    }
}

/**
 * Select a tab's root and discard any detail pages above the active tab first. Saving only that
 * root lets the tab retain its scroll and loaded UI state while ensuring a tab tap never restores
 * an old detail page such as Liked Music.
 */
internal fun NavController.navigateToTabRoot(
    target: BottomNavScreen,
    activeTab: BottomNavScreen?,
) {
    if (currentDestination?.hasRoute(target.destination::class) == true) return

    // A non-inclusive saved pop aliases the removed stack to the retained root. Returning
    // from Search to Home would therefore save Search under Home's restore key. Discard the
    // suffix here; the requested root is still live and retains its own UI state.
    if (popBackStack(target.destination, inclusive = false, saveState = false)) return

    // Otherwise restore the target's saved stack. Resolve the source again from the live stack so
    // rapid taps cannot act on stale selectedIndex or previousBackStackEntry state.
    val tabs =
        listOf(
            BottomNavScreen.Home,
            BottomNavScreen.MixForYou,
            BottomNavScreen.Analytics,
            BottomNavScreen.Library,
            BottomNavScreen.Search,
        )
    val activeRoot = activeTabRoot(tabs) ?: activeTab
    if (target == BottomNavScreen.Search) {
        // Search is a page, not a replacement tab root: leave the exact current page underneath
        // so system back returns to it, including when Search was opened from a playlist.
        navigate(target.destination) {
            launchSingleTop = true
            restoreState = false
        }
        return
    }

    // When leaving Search for a different tab, save the tab that Search was opened from too.
    // This avoids leaving that root underneath the newly selected tab.
    val sourceTab =
        if (activeRoot == BottomNavScreen.Search) {
            previousTabRoot(BottomNavScreen.Search, tabs) ?: activeRoot
        } else {
            activeRoot
        }
    // Save only the outgoing root, not Search or a detail page stacked above it.
    // Root state (including scroll position) is retained by the inclusive save below.
    if (sourceTab != null) {
        popBackStack(sourceTab.destination, inclusive = false, saveState = false)
    }
    navigate(target.destination) {
        if (sourceTab != null) {
            popUpTo(sourceTab.destination) {
                inclusive = true
                saveState = true
            }
            restoreState = true
        } else {
            popUpTo(graph.id) { saveState = false }
            restoreState = false
        }
        launchSingleTop = true
    }

    // Older saved stacks can contain a detail suffix or even lack the requested root
    // entirely because of the previous non-inclusive save alias. Repair both cases.
    if (currentDestination?.hasRoute(target.destination::class) != true &&
        !popBackStack(target.destination, inclusive = false, saveState = false)
    ) {
        clearBackStack(target.destination)
        navigate(target.destination) {
            popUpTo(graph.id) { saveState = false }
            launchSingleTop = true
            restoreState = false
        }
    }
}
