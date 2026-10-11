package com.maxrave.simpmusic.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute

internal const val BOTTOM_NAV_TRANSITION_DURATION_MILLIS = 300

/**
 * Coalesce tab taps that arrive before Navigation Compose finishes the current destination
 * transition. Replay the latest queued tap once the incoming entry is RESUMED, including a reselect
 * of the tab that the transition just reached.
 */
@Composable
internal fun rememberSettledTabSelection(
    navController: NavController,
    tabs: List<BottomNavScreen>,
    currentBackStackEntry: NavBackStackEntry?,
    onSelectSettledTab: (BottomNavScreen) -> Unit,
): (BottomNavScreen) -> Unit {
    var pendingTabOrdinal by remember { mutableIntStateOf(-1) }
    val entryLifecycleState =
        currentBackStackEntry?.lifecycle?.currentStateFlow?.collectAsState()?.value
    val onSelectSettledTabLatest by rememberUpdatedState(onSelectSettledTab)

    LaunchedEffect(pendingTabOrdinal, currentBackStackEntry, entryLifecycleState, tabs) {
        if (pendingTabOrdinal < 0 || entryLifecycleState != Lifecycle.State.RESUMED) {
            return@LaunchedEffect
        }

        val pendingTab = tabs.firstOrNull { it.ordinal == pendingTabOrdinal }
        pendingTabOrdinal = -1
        if (pendingTab != null) onSelectSettledTabLatest(pendingTab)
    }

    return remember(navController, tabs) {
        { screen ->
            val liveLifecycleState =
                navController.currentBackStackEntry?.lifecycle?.currentStateFlow?.value
            if (liveLifecycleState == Lifecycle.State.RESUMED) {
                pendingTabOrdinal = -1
                onSelectSettledTabLatest(screen)
            } else {
                pendingTabOrdinal = screen.ordinal
            }
        }
    }
}

/** Find the tab that owns the current detail destination from the live stack, not its predecessor. */
internal fun NavController.activeTabRoot(tabs: List<BottomNavScreen>): BottomNavScreen? =
    currentBackStack.value
        .asReversed()
        .firstNotNullOfOrNull { entry ->
            tabs.firstOrNull { tab -> entry.destination.hasRoute(tab.destination::class) }
        }

/** Find the tab root immediately below [currentTab], if it is still in the live stack. */
internal fun NavController.previousTabRoot(
    currentTab: BottomNavScreen,
    tabs: List<BottomNavScreen>,
): BottomNavScreen? {
    val stack = currentBackStack.value
    val currentRootIndex =
        stack.indexOfLast { entry ->
            entry.destination.hasRoute(currentTab.destination::class)
        }
    if (currentRootIndex <= 0) return null

    return stack
        .take(currentRootIndex)
        .asReversed()
        .firstNotNullOfOrNull { entry ->
            tabs.firstOrNull { tab ->
                tab != currentTab && entry.destination.hasRoute(tab.destination::class)
            }
        }
}

/** Switch tabs while retaining each root's state, but never restore its detail pages. */
internal fun NavController.openTabRoot(screen: BottomNavScreen, tabs: List<BottomNavScreen>) {
    // Playlist/artist routes are siblings of the tab routes. Their destination hierarchy cannot
    // identify the tab which opened them; use the nearest root in the actual back stack.
    val sourceRoot = currentBackStack.value.lastOrNull { entry ->
        tabs.any { entry.destination.hasRoute(it.destination::class) }
    }
    if (sourceRoot != null && currentBackStackEntry != sourceRoot) {
        popBackStack(sourceRoot.destination.id, inclusive = false, saveState = false)
    }
    if (currentDestination?.hasRoute(screen.destination::class) == true) return

    navigate(screen.destination) {
        popUpTo(graph.startDestinationId) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
    // An older app session may already have saved a root together with a detail page. Restore
    // the root's scroll/data state, then remove that suffix before the next composition.
    if (currentDestination?.hasRoute(screen.destination::class) != true) {
        popBackStack(screen.destination, inclusive = false, saveState = false)
    }
}
