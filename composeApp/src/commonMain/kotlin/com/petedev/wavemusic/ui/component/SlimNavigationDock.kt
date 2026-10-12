package com.petedev.wavemusic.ui.component

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceIn
import androidx.compose.ui.util.lerp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.currentBackStackEntryAsState
import com.petedev.wavemusic.expect.ui.PlatformBackdrop
import com.petedev.wavemusic.ui.screen.MiniPlayer
import com.petedev.wavemusic.ui.theme.LocalIsDarkTheme
import com.petedev.wavemusic.ui.theme.LocalLiquidGlassAppearance
import com.petedev.wavemusic.ui.theme.LocalLiquidGlassEnabled
import com.petedev.wavemusic.ui.theme.LocalReduceVisualEffects
import com.petedev.wavemusic.ui.theme.typo
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

/** Home and Search pinned to the edges with a compact player between them. */
@Composable
fun SlimNavigationDock(
    navController: NavController,
    backdrop: PlatformBackdrop,
    showMiniPlayer: Boolean,
    miniPlayerVisible: Boolean,
    isDismissingMiniPlayer: Boolean,
    onOpenNowPlaying: () -> Unit,
    onDismissNowPlaying: () -> Unit,
    onSwipeExpandProgress: (Float) -> Unit,
    onCloseMiniPlayer: () -> Unit,
    reloadDestinationIfNeeded: (kotlin.reflect.KClass<*>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val slimTabs = remember { listOf(BottomNavScreen.Home, BottomNavScreen.Search) }
    val allTabs = remember {
        listOf(
            BottomNavScreen.Home,
            BottomNavScreen.MixForYou,
            BottomNavScreen.Analytics,
            BottomNavScreen.Library,
            BottomNavScreen.Search,
        )
    }
    var selectedIndex by rememberSaveable { mutableIntStateOf(BottomNavScreen.Home.ordinal) }

    LaunchedEffect(currentBackStackEntry, allTabs) {
        selectedIndex =
            navController
                .activeTabRoot(allTabs)
                ?.takeIf { it in slimTabs }
                ?.ordinal ?: -1
    }

    val selectTabWhenSettled: (BottomNavScreen) -> Unit = { screen ->
        if (navController.currentDestination?.hasRoute(screen.destination::class) == true) {
            selectedIndex = screen.ordinal
            if (screen == BottomNavScreen.Search) {
                reloadDestinationIfNeeded(screen.destination::class)
            }
        } else {
            selectedIndex = screen.ordinal
            navController.navigateToTabRoot(screen, navController.activeTabRoot(allTabs))
        }
    }
    val selectTab =
        rememberSettledTabSelection(
            navController = navController,
            tabs = slimTabs,
            currentBackStackEntry = currentBackStackEntry,
            onSelectSettledTab = selectTabWhenSettled,
        )

    val layoutDirection = LocalLayoutDirection.current
    val playerHeight = GlassBottomDockHeight

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(
            modifier =
                modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom))
                    .padding(horizontal = 12.dp)
                    .padding(top = 4.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SlimDockTab(
                screen = BottomNavScreen.Home,
                selected = selectedIndex == BottomNavScreen.Home.ordinal,
                backdrop = backdrop,
                onClick = { selectTab(BottomNavScreen.Home) },
            )
            MorphingDockVisibility(
                visibleState = rememberMiniPlayerVisibility(showMiniPlayer && miniPlayerVisible),
                modifier = Modifier.weight(1f),
                enter =
                    fadeIn(tween(260, easing = FastOutSlowInEasing)) +
                        slideInVertically(
                            animationSpec = tween(320, easing = FastOutSlowInEasing),
                        ) { height -> -height / 3 },
                exit =
                    if (isDismissingMiniPlayer) {
                        fadeOut(tween(190, easing = FastOutSlowInEasing)) +
                            slideOutVertically(
                                animationSpec = tween(220, easing = FastOutSlowInEasing),
                            ) { height -> height / 5 }
                    } else {
                        ExitTransition.None
                    },
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                    MiniPlayer(
                        modifier = Modifier.fillMaxWidth().height(playerHeight),
                        backdrop = backdrop,
                        onClose = onCloseMiniPlayer,
                        onClick = onOpenNowPlaying,
                        onDismissNowPlaying = onDismissNowPlaying,
                        onSwipeExpandProgress = onSwipeExpandProgress,
                        compact = true,
                    )
                }
            }
            SlimDockTab(
                screen = BottomNavScreen.Search,
                selected = selectedIndex == BottomNavScreen.Search.ordinal,
                backdrop = backdrop,
                onClick = { selectTab(BottomNavScreen.Search) },
            )
        }
    }
}

/**
 * A continuous dock morph. The full-width player stays composed while it narrows and travels down
 * into the gap between Home and Search. The navigation capsule moves to the left edge and folds
 * its other tabs into Home; Search separates to the right edge. The same controls stay on screen
 * throughout the handoff, so there is no full-bar/mini-bar swap.
 */
@Composable
fun MorphingSlimNavigationDock(
    navController: NavController,
    backdrop: PlatformBackdrop,
    expanded: Boolean,
    showMiniPlayer: Boolean,
    miniPlayerVisible: Boolean,
    isDismissingMiniPlayer: Boolean,
    showAnalyticsTab: Boolean,
    showMixForYouTab: Boolean,
    reloadDestinationIfNeeded: (kotlin.reflect.KClass<*>) -> Unit,
    onDockExpandedChange: (Boolean) -> Unit,
    onOpenNowPlaying: () -> Unit,
    onDismissNowPlaying: () -> Unit,
    onSwipeExpandProgress: (Float) -> Unit,
    onCloseMiniPlayer: () -> Unit,
    onOpenFullscreenLyrics: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val allTabs =
        remember(showAnalyticsTab, showMixForYouTab) {
            listOfNotNull(
                BottomNavScreen.Home,
                BottomNavScreen.MixForYou.takeIf { showMixForYouTab },
                BottomNavScreen.Analytics.takeIf { showAnalyticsTab },
                BottomNavScreen.Library,
                BottomNavScreen.Search,
            )
        }
    val navigationTabs = remember(allTabs) { allTabs.filter { it != BottomNavScreen.Search } }
    var selectedIndex by rememberSaveable { mutableIntStateOf(BottomNavScreen.Home.ordinal) }

    LaunchedEffect(currentBackStackEntry, allTabs) {
        val activeTab = navController.activeTabRoot(allTabs)
        if (activeTab == BottomNavScreen.Search) {
            onDockExpandedChange(false)
        } else if (selectedIndex == BottomNavScreen.Search.ordinal && activeTab != null) {
            onDockExpandedChange(true)
        }
        selectedIndex = activeTab?.ordinal ?: -1
    }

    val selectTabWhenSettled: (BottomNavScreen) -> Unit = { screen ->
        if (navController.currentDestination?.hasRoute(screen.destination::class) == true) {
            selectedIndex = screen.ordinal
            reloadDestinationIfNeeded(screen.destination::class)
        } else {
            val activeTab = navController.activeTabRoot(allTabs)
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
            tabs = allTabs,
            currentBackStackEntry = currentBackStackEntry,
            onSelectSettledTab = selectTabWhenSettled,
        )
    val selectDockTab: (BottomNavScreen) -> Unit = { screen ->
        onDockExpandedChange(screen != BottomNavScreen.Search)
        selectTab(screen)
    }

    val useLiquidGlass = LocalLiquidGlassEnabled.current
    val navHeight = if (useLiquidGlass) GlassBottomDockHeight else FlatBottomDockHeight
    val maxTabWidth = if (useLiquidGlass) 92.dp else 96.dp
    val capsuleInset = if (useLiquidGlass) 4.dp else 6.dp
    val fullSearchSize = navHeight
    val playerExpandedHeight = 56.dp
    // Keep the same visible gap for both styles. The slot offset is reduced by the amount
    // absorbed when the shorter mini-player is centered against a taller navigation capsule.
    val playerSlotGap = 8.dp + ((navHeight - playerExpandedHeight).coerceAtLeast(0.dp) * 0.5f)
    val hasMiniPlayer = showMiniPlayer && miniPlayerVisible
    val playerVisibility = rememberMiniPlayerVisibility(hasMiniPlayer)
    // The outgoing card still occupies its original slot while fading. Dropping this space
    // when playback stops relocates the card onto the navigation controls during its exit.
    val hasMiniPlayerSlot = playerVisibility.currentState || playerVisibility.targetState
    val reduceVisualEffects = LocalReduceVisualEffects.current
    val layoutDirection = LocalLayoutDirection.current
    val collapseProgress by
        animateFloatAsState(
            targetValue = if (expanded) 0f else 1f,
            animationSpec = tween(durationMillis = if (reduceVisualEffects) 0 else 300, easing = FastOutSlowInEasing),
            label = "slim dock handoff",
        )
    // Clear the center lane first, then lower the player into it. On the reverse handoff the
    // player rises before the controls gather back together.
    val sideProgress = smoothStep((collapseProgress / 0.58f).coerceIn(0f, 1f))
    val verticalProgress = smoothStep(((collapseProgress - 0.2f) / 0.8f).coerceIn(0f, 1f))
    // Scaffold must see a constant bottom inset throughout scrolling. Only the dock's children
    // morph; resizing this viewport would remeasure the destination and feed scroll corrections
    // back into the next direction change on every animation frame.
    val dockViewportHeight = navHeight + (if (hasMiniPlayerSlot) playerExpandedHeight + playerSlotGap else 0.dp)
    val dockTopOffset = (dockViewportHeight - navHeight) * verticalProgress

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom))
                .padding(bottom = 8.dp),
    ) {
        BoxWithConstraints(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(dockViewportHeight).then(
                        // The glass lens scales past the dock's bounds while pressed. Keep it
                        // unclipped in every morph state, including Search's compact layout.
                        if (useLiquidGlass) Modifier else Modifier.clipToBounds(),
                    ),
        ) {
            val sidePadding = 12.dp
            val outerWidth = (maxWidth - sidePadding * 2).coerceAtLeast(0.dp)
            val expandedCapsuleAvailable =
                (outerWidth - fullSearchSize - DockSearchGap).coerceAtLeast(0.dp)
            val expandedTabWidth =
                ((expandedCapsuleAvailable - capsuleInset * 2) / navigationTabs.size)
                    .coerceAtLeast(0.dp)
                    .coerceAtMost(maxTabWidth)
            val expandedCapsuleWidth = expandedTabWidth * navigationTabs.size + capsuleInset * 2
            val expandedCapsuleX =
                sidePadding + (expandedCapsuleAvailable - expandedCapsuleWidth) / 2
            val expandedSearchX = expandedCapsuleX + expandedCapsuleWidth + DockSearchGap
            val compactCapsuleWidth = 48.dp
            val compactSearchSize = 48.dp
            val compactCapsuleX = sidePadding
            val compactSearchX = maxWidth - sidePadding - compactSearchSize
            val capsuleX = interpolateDp(expandedCapsuleX, compactCapsuleX, sideProgress)
            val capsuleWidth =
                interpolateDp(expandedCapsuleWidth, compactCapsuleWidth, sideProgress)
            val capsuleHeight = interpolateDp(navHeight, compactSearchSize, sideProgress)
            val searchSize = interpolateDp(fullSearchSize, compactSearchSize, sideProgress)
            val searchX = interpolateDp(expandedSearchX, compactSearchX, sideProgress)
            val navCenterY =
                interpolateDp(
                    start = navHeight / 2 + (if (hasMiniPlayerSlot) playerExpandedHeight + playerSlotGap else 0.dp),
                    end = navHeight / 2,
                    fraction = verticalProgress,
                )

            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                MorphingDockNavigationCapsule(
                    modifier =
                        dockPosition(x = capsuleX, y = dockTopOffset + navCenterY - capsuleHeight / 2)
                            .zIndex(1f),
                    width = capsuleWidth,
                    height = capsuleHeight,
                    progress = sideProgress,
                    tabs = navigationTabs,
                    selectedIndex = selectedIndex,
                    expandedTabWidth = expandedTabWidth,
                    expandedInset = capsuleInset,
                    backdrop = backdrop,
                    onSelect = { selectDockTab(it) },
                )
                SlimDockTab(
                    modifier =
                        dockPosition(x = searchX, y = dockTopOffset + navCenterY - searchSize / 2)
                            .zIndex(1f),
                    screen = BottomNavScreen.Search,
                    selected = selectedIndex == BottomNavScreen.Search.ordinal,
                    backdrop = backdrop,
                    onClick = { selectDockTab(BottomNavScreen.Search) },
                    width = searchSize,
                    height = searchSize,
                )

                val playerWidthFull = (maxWidth - sidePadding * 2).coerceAtLeast(0.dp)
                val playerWidthCompact =
                    (maxWidth - sidePadding * 2 - compactCapsuleWidth - compactSearchSize - DockControlGap * 2)
                        .coerceAtLeast(72.dp)
                val playerWidth =
                    interpolateDp(playerWidthFull, playerWidthCompact, sideProgress)
                val playerHeight = interpolateDp(playerExpandedHeight, navHeight, verticalProgress)
                MorphingDockVisibility(
                    visibleState = playerVisibility,
                    modifier =
                        dockPosition(
                                x = (maxWidth - playerWidth) / 2,
                                y = dockTopOffset + (navHeight - playerHeight) / 2,
                            ).width(playerWidth)
                            .height(playerHeight)
                            .zIndex(2f),
                    enter = fadeIn(tween(240, easing = FastOutSlowInEasing)),
                    exit =
                        if (isDismissingMiniPlayer) {
                            fadeOut(tween(190, easing = FastOutSlowInEasing)) +
                                slideOutVertically(tween(220, easing = FastOutSlowInEasing)) { it / 5 }
                        } else {
                            ExitTransition.None
                        },
                ) {
                    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                        MiniPlayer(
                            modifier = Modifier.fillMaxSize(),
                            backdrop = backdrop,
                            onClose = onCloseMiniPlayer,
                            onClick = onOpenNowPlaying,
                            onDismissNowPlaying = onDismissNowPlaying,
                            onSwipeExpandProgress = onSwipeExpandProgress,
                            onOpenFullscreenLyrics = onOpenFullscreenLyrics,
                            compact = sideProgress >= 0.42f,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MorphingDockNavigationCapsule(
    modifier: Modifier,
    width: Dp,
    height: Dp,
    progress: Float,
    tabs: List<BottomNavScreen>,
    selectedIndex: Int,
    expandedTabWidth: Dp,
    expandedInset: Dp,
    backdrop: PlatformBackdrop,
    onSelect: (BottomNavScreen) -> Unit,
) {
    val homeIndex = tabs.indexOf(BottomNavScreen.Home)
    val selectedTabIndex = tabs.indexOfFirst { it.ordinal == selectedIndex }
    val homeSelected = selectedIndex == BottomNavScreen.Home.ordinal
    val inset = expandedInset * (1f - progress)
    val homeSlotWidth = interpolateDp(expandedTabWidth, 48.dp, progress)
    val tabsCount = tabs.size
    val density = LocalDensity.current
    val tabWidthPx = with(density) { expandedTabWidth.toPx() }
    val animationScope = rememberCoroutineScope()
    val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
    val appearance = LocalLiquidGlassAppearance.current
    val isDark = LocalIsDarkTheme.current
    val useLiquidGlass = LocalLiquidGlassEnabled.current
    val capsuleInteraction = if (useLiquidGlass) rememberGlassInteraction() else null
    val currentTabWidthPx by rememberUpdatedState(tabWidthPx)
    val currentTabs by rememberUpdatedState(tabs)
    val currentOnSelect by rememberUpdatedState(onSelect)
    val currentSelectedIndex by rememberUpdatedState(selectedTabIndex)
    val currentProgress by rememberUpdatedState(progress)
    val currentUseLiquidGlass by rememberUpdatedState(useLiquidGlass)
    val currentIsLtr by rememberUpdatedState(isLtr)
    var currentIndex by remember(tabs) { mutableIntStateOf(selectedTabIndex.coerceAtLeast(0)) }
    var dragSettlingTarget by remember(tabs) { mutableIntStateOf(-1) }
    val draggedFlag = remember(tabs) { booleanArrayOf(false) }
    val dragStartedOnSelectedTab = remember(tabs) { booleanArrayOf(false) }

    val dampedDrag =
        remember(animationScope, tabs) {
            DampedDragAnimation(
                animationScope = animationScope,
                initialValue = selectedTabIndex.coerceAtLeast(0).toFloat(),
                valueRange = 0f..(tabsCount - 1).coerceAtLeast(0).toFloat(),
                visibilityThreshold = 0.001f,
                initialScale = 1f,
                // Match the restrained press zoom used by the standalone Search glass button.
                pressedScale = 1.12f,
                onDragStarted = { position ->
                    draggedFlag[0] = false
                    val slot =
                        (position.x / currentTabWidthPx.coerceAtLeast(1f))
                            .toInt()
                            .coerceIn(0, tabsCount - 1)
                    val logicalSlot = if (currentIsLtr) slot else tabsCount - 1 - slot
                    val startTab = currentTabs.getOrNull(logicalSlot)
                    val startsOnHomeOrLibrary =
                        startTab == BottomNavScreen.Home || startTab == BottomNavScreen.Library
                    // Let the selected Home/Library pill respond to a hold in compact mode too;
                    // horizontal selection dragging is restricted to the expanded rail below.
                    val enabled =
                        currentUseLiquidGlass &&
                            logicalSlot == currentSelectedIndex &&
                            startsOnHomeOrLibrary
                    dragStartedOnSelectedTab[0] = enabled
                    enabled
                },
                onDragStopped = {
                    if (draggedFlag[0] && dragStartedOnSelectedTab[0]) {
                        val logicalHomeIndex =
                            if (currentIsLtr) currentTabs.indexOf(BottomNavScreen.Home)
                            else tabsCount - 1 - currentTabs.indexOf(BottomNavScreen.Home)
                        val logicalLibraryIndex =
                            if (currentIsLtr) currentTabs.indexOf(BottomNavScreen.Library)
                            else tabsCount - 1 - currentTabs.indexOf(BottomNavScreen.Library)
                        val midpoint = (logicalHomeIndex + logicalLibraryIndex) / 2f
                        val target =
                            if (targetValue < midpoint) logicalHomeIndex else logicalLibraryIndex
                        val changedTab = target != currentIndex
                        currentIndex = target
                        if (changedTab) {
                            dragSettlingTarget = target
                            currentOnSelect(currentTabs[target])
                        } else {
                            dragSettlingTarget = -1
                            val dragAnimation = this
                            animationScope.launch { dragAnimation.animateToValue(target.toFloat()) }
                        }
                    }
                },
                onDrag = { _, timeMillis, dragAmount ->
                    if (dragAmount.x != 0f && dragStartedOnSelectedTab[0] && currentProgress <= 0.01f) {
                        draggedFlag[0] = true
                        val direction = if (currentIsLtr) 1f else -1f
                        updateValue(
                            (targetValue + dragAmount.x / currentTabWidthPx.coerceAtLeast(1f) * direction)
                                .coerceIn(0f, (tabsCount - 1).coerceAtLeast(0).toFloat()),
                            timeMillis,
                        )
                    }
                },
            )
        }

    LaunchedEffect(selectedTabIndex, tabs) {
        if (selectedTabIndex >= 0 && tabsCount > 0) {
            val target = selectedTabIndex.coerceIn(0, tabsCount - 1)
            currentIndex = target
            if (dragSettlingTarget == target) {
                dragSettlingTarget = -1
                dampedDrag.animateToValue(target.toFloat())
            } else {
                dampedDrag.animateToValue(
                    target.toFloat(),
                    animationSpec = dampedDrag.selectionAnimationSpec,
                )
            }
        }
    }

    val compactLabelAlpha = (1f - progress * 1.8f).coerceIn(0f, 1f)
    val indicatorWidth =
        if (homeSelected) interpolateDp(expandedTabWidth, 34.dp, progress)
        else expandedTabWidth * (1f - progress)
    val indicatorHeight =
        interpolateDp(if (useLiquidGlass) 52.dp else 56.dp, 34.dp, progress)
    val selectionShape = bottomDockSelectionShape(height, (height - indicatorHeight) / 2)
    val indicatorAlpha =
        when {
            selectedTabIndex < 0 -> 0f
            homeSelected -> 1f
            else -> 1f - progress
        }

    Box(
        modifier =
            modifier
                .width(width)
                .height(height)
                .then(
                    capsuleInteraction?.let { interaction ->
                        Modifier.pointerInput(interaction) { interaction.detectPress(this) }
                    } ?: Modifier,
                ),
        contentAlignment = Alignment.CenterStart,
    ) {
        // Keep the capsule's glass on a background sibling. Putting drawBackdrop on the parent
        // clips the selected lens layer to the capsule bounds, preventing its pressed bulge from
        // crossing the rim like it does in the regular navigation bar.
        Box(
            Modifier
                .matchParentSize()
                .then(
                    capsuleInteraction?.let { interaction ->
                        Modifier.drawInteractiveGlass(
                            isDark = isDark,
                            backdrop = backdrop,
                            luminanceAnimation = 0.5f,
                            shape = BottomDockShape,
                            interaction = interaction,
                        )
                    } ?: Modifier.slimDockSurface(backdrop = backdrop, shape = BottomDockShape),
                ),
        )

        if (indicatorAlpha > 0f && indicatorWidth > 0.dp) {
            Box(
                modifier =
                    Modifier
                        .width(indicatorWidth.coerceAtLeast(0.dp))
                        .height(indicatorHeight)
                        .align(Alignment.CenterStart)
                        .graphicsLayer {
                            alpha = indicatorAlpha
                            val position = dampedDrag.value
                            val insetPx = inset.toPx()
                            val homeSlotWidthPx = homeSlotWidth.toPx()
                            val expandedTabWidthPx = expandedTabWidth.toPx()
                            val indicatorWidthPx = indicatorWidth.toPx()
                            val homeCenteringOffset =
                                ((homeSlotWidthPx - indicatorWidthPx).coerceAtLeast(0f) / 2f) *
                                    (1f - position.coerceIn(0f, 1f))
                            translationX =
                                when {
                                    position <= 1f -> insetPx + homeSlotWidthPx * position + homeCenteringOffset
                                    else ->
                                        insetPx + homeSlotWidthPx +
                                            expandedTabWidthPx * (position - 1f) * (1f - progress)
                                }
                            scaleX = 1f + (dampedDrag.scaleX - 1f) * appearance.motionMultiplier
                            scaleY = 1f + (dampedDrag.scaleY - 1f) * appearance.motionMultiplier
                            val velocity = dampedDrag.velocity / 10f * appearance.motionMultiplier
                            scaleX /= 1f - (velocity * 0.75f).fastCoerceIn(-0.2f, 0.2f)
                            scaleY *= 1f - (velocity * 0.25f).fastCoerceIn(-0.2f, 0.2f)
                        }
                        .then(
                            // The capsule beneath this indicator already supplies the frosted
                            // backdrop. A second blur/color/lens stack adds a full GPU pass on
                            // every scroll frame even when its press-only lens has zero strength.
                            Modifier
                                .clip(selectionShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                        ),
            )
        }

        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = inset).then(dampedDrag.modifier),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            tabs.forEachIndexed { index, screen ->
                val label = stringResource(screen.title)
                val isHome = index == homeIndex
                val slotWidth = if (isHome) homeSlotWidth else expandedTabWidth * (1f - progress)
                val selected = selectedTabIndex >= 0 && currentIndex == index
                val tint =
                    if (selected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                Box(
                    modifier =
                        Modifier
                            .width(slotWidth.coerceAtLeast(0.dp))
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .clickable(
                                enabled = slotWidth >= 32.dp,
                                interactionSource = null,
                                indication = null,
                            ) {
                                currentIndex = index
                                onSelect(screen)
                            }
                            .semantics {
                                contentDescription = label
                                role = Role.Tab
                                this.selected = selected
                            }
                            .graphicsLayer {
                                val alpha = if (isHome) 1f else 1f - progress
                                this.alpha = alpha
                                scaleX = alpha.coerceAtLeast(0.01f)
                            },
                    contentAlignment = Alignment.Center,
                ) {
                    CompositionLocalProvider(LocalContentColor provides tint) {
                        Box(
                            Modifier
                                .align(Alignment.Center)
                                .absoluteOffset(y = interpolateDp((-8).dp, 0.dp, progress)),
                        ) {
                            screen.icon()
                        }
                    }
                    Text(
                        text = label,
                        style = typo().bodySmall,
                        color = tint,
                        maxLines = 1,
                        modifier =
                            Modifier
                                .align(Alignment.Center)
                                .absoluteOffset(y = 13.dp)
                                .graphicsLayer { alpha = compactLabelAlpha },
                    )
                }
            }
        }
    }
}

@Composable
private fun MorphingDockVisibility(
    visibleState: MutableTransitionState<Boolean>,
    modifier: Modifier = Modifier,
    enter: EnterTransition,
    exit: ExitTransition,
    content: @Composable () -> Unit,
) {
    androidx.compose.animation.AnimatedVisibility(
        visibleState = visibleState,
        modifier = modifier,
        enter = enter,
        exit = exit,
    ) {
        content()
    }
}

@Composable
private fun SlimDockTab(
    modifier: Modifier = Modifier,
    screen: BottomNavScreen,
    selected: Boolean,
    backdrop: PlatformBackdrop,
    onClick: () -> Unit,
    width: Dp = 48.dp,
    height: Dp = 48.dp,
) {
    val label = stringResource(screen.title)
    val tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        modifier =
            Modifier
                .then(modifier)
                .size(width = width, height = height)
                .slimDockSurface(backdrop = backdrop, shape = CircleShape)
                .clickable(onClick = onClick, role = Role.Tab)
                .semantics {
                    contentDescription = label
                    role = Role.Tab
                    this.selected = selected
                },
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Box(
                Modifier
                    .size(34.dp)
                    .background(tint.copy(alpha = 0.14f), CircleShape),
            )
        }
        CompositionLocalProvider(LocalContentColor provides tint) {
            screen.icon()
        }
    }
}

/** Match the mini-player and header's theme tint and opacity when glass is disabled. */
@Composable
private fun Modifier.slimDockSurface(backdrop: PlatformBackdrop, shape: Shape): Modifier =
    if (LocalLiquidGlassEnabled.current) {
        liquidGlass(backdrop = backdrop, shape = shape)
    } else {
        clip(shape).background(flatChromeBackground())
    }

private val DockControlGap = 8.dp
private val DockSearchGap = 12.dp

/** Dock geometry is measured from the physical left edge, independently of text direction. */
internal fun BoxScope.dockPosition(x: Dp, y: Dp): Modifier =
    Modifier.align(AbsoluteAlignment.TopLeft).absoluteOffset(x = x, y = y)

private fun interpolateDp(start: Dp, end: Dp, fraction: Float): Dp =
    (start.value + (end.value - start.value) * fraction.coerceIn(0f, 1f)).dp

private fun smoothStep(fraction: Float): Float = fraction * fraction * (3f - 2f * fraction)
