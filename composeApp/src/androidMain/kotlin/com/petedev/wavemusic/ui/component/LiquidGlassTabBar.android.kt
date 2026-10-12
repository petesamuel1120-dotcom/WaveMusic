package com.petedev.wavemusic.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceIn
import com.petedev.wavemusic.expect.ui.PlatformBackdrop
import com.petedev.wavemusic.ui.component.BOTTOM_NAV_TRANSITION_DURATION_MILLIS
import com.petedev.wavemusic.ui.theme.LocalIsDarkTheme
import com.petedev.wavemusic.ui.theme.LocalLiquidGlassAppearance
import com.petedev.wavemusic.ui.theme.typo
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

private val TabWidth = 92.dp
private val BarHeight = GlassBottomDockHeight
private val BlobHeight = 52.dp
// Breathing room between the capsule edge and the pill on the first/last tab. The pill is a full
// tab wide, so without this it sits flush against the capsule's rounded end.
private val BarInset = 4.dp
private val SelectionShape = bottomDockSelectionShape(BarHeight, (BarHeight - BlobHeight) / 2)

/**
 * iOS 26 / Kyant-style liquid sliding tab bar.
 *
 * Three stacked layers (bottom → top):
 *  1. a dark, luminance-adaptive glass **capsule** (the same [drawInteractiveGlass] as the MiniPlayer),
 *  2. a **frosted blob** that slides to the selected tab, squashing/stretching with
 *     drag velocity (`DampedDragAnimation`) — the selection indicator, and
 *  3. crisp **icons + labels** on top (so the active label stays sharp, unlike when
 *     the blob is drawn over it).
 *
 * Adapted from Kyant's `LiquidBottomTabs`/`DampedDragAnimation` to WaveMusic's
 * [BottomNavScreen]s, fixed tab width (to fit the existing ConstraintLayout) and the
 * bottom bar's luminance sampling.
 *
 * @param luminance stable luminance value (0..1) driving the glass brightness.
 * @param onTabSelected fired when the user taps a tab or drag-snaps the blob.
 */
@Composable
fun LiquidGlassTabBar(
    tabs: List<BottomNavScreen>,
    selectedTab: Int,
    backdrop: PlatformBackdrop,
    luminance: Float,
    modifier: Modifier = Modifier,
    availableWidth: Dp = Dp.Unspecified,
    fillAvailableWidth: Boolean = false,
    onTabSelected: (BottomNavScreen) -> Unit,
) {
    val density = LocalDensity.current
    val appearance = LocalLiquidGlassAppearance.current
    val tabsCount = tabs.size
    // Every tab must be the same width — the blob's position is `index * tabWidth` and the drag
    // gesture divides by it, so uneven tabs would put the blob off its icon. What must NOT be
    // fixed is the number itself: with a hardcoded TabWidth the capsule simply grew past the
    // screen once a fourth tab appeared, and what fell off the right edge was the search FAB.
    // [availableWidth] is the space the row actually hands this capsule, so the tabs divide up
    // what exists instead of a guess. The embedded landscape dock fills its reserved slot;
    // portrait retains its cap so fewer tabs on a tablet do not stretch into slabs.
    val tabWidth =
        if (availableWidth.isSpecified && availableWidth > 0.dp) {
            val availableTabWidth = (availableWidth - BarInset * 2) / tabsCount
            if (fillAvailableWidth) availableTabWidth else availableTabWidth.coerceAtMost(TabWidth)
        } else {
            TabWidth
        }
    val tabWidthPx = with(density) { tabWidth.toPx() }
    // The callbacks below are remembered; read layout and selection values from their latest state.
    val currentTabWidthPx by rememberUpdatedState(tabWidthPx)
    val currentTabs by rememberUpdatedState(tabs)
    val currentOnTabSelected by rememberUpdatedState(onTabSelected)
    val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
    val currentIsLtr by rememberUpdatedState(isLtr)
    val animationScope = rememberCoroutineScope()
    val barInteraction = rememberGlassInteraction()
    val isDark = LocalIsDarkTheme.current

    var currentIndex by remember { mutableIntStateOf(selectedTab.coerceAtLeast(0)) }
    val currentSelectedIndex by rememberUpdatedState(currentIndex)
    var dragSettlingTarget by remember { mutableIntStateOf(-1) }
    // [0] = a real drag happened (vs a pure tap) — keeps the blob from snapping back
    // and stealing a tab tap.
    val draggedFlag = remember { booleanArrayOf(false) }
    val dragStartTab = remember { intArrayOf(-1) }
    val dragStartedOnSelectedTab = remember { booleanArrayOf(false) }

    val dampedDrag =
        remember(animationScope, tabsCount) {
            DampedDragAnimation(
                animationScope = animationScope,
                initialValue = selectedTab.coerceAtLeast(0).toFloat(),
                valueRange = 0f..(tabsCount - 1).toFloat(),
                visibilityThreshold = 0.001f,
                initialScale = 1f,
                // On press the blob inflates from 52dp up to ~70dp — past the 60dp bar height — so it
                // visibly bulges out of the capsule row (Apple Music style), then springs back on release.
                pressedScale = 70f / 52f,
                onDragStarted = { position ->
                    draggedFlag[0] = false
                    val slot = (position.x / currentTabWidthPx.coerceAtLeast(1f)).toInt()
                        .coerceIn(0, tabsCount - 1)
                    dragStartTab[0] = if (currentIsLtr) slot else tabsCount - 1 - slot
                    // A touch that starts on another tab belongs to that tab. Only a drag begun
                    // on the selected pill should snap to an arbitrary release position.
                    dragStartedOnSelectedTab[0] = dragStartTab[0] == currentSelectedIndex
                    true
                },
                onDragStopped = {
                    if (draggedFlag[0]) {
                        val target =
                            if (dragStartedOnSelectedTab[0]) {
                                targetValue.roundToInt().coerceIn(0, tabsCount - 1)
                            } else {
                                dragStartTab[0].coerceIn(0, tabsCount - 1)
                            }
                        val changedTab = target != currentIndex
                        currentIndex = target
                        // Start the page transition on release. The indicator's spring settles
                        // independently so its motion never delays navigation.
                        if (changedTab) {
                            dragSettlingTarget = target
                            currentOnTabSelected(currentTabs[target])
                        }
                    }
                },
                onDrag = { _, timeMillis, dragAmount ->
                    if (dragAmount.x != 0f) {
                        draggedFlag[0] = true
                        if (dragStartedOnSelectedTab[0]) {
                            val direction = if (currentIsLtr) 1f else -1f
                            updateValue(
                                (targetValue + dragAmount.x / currentTabWidthPx * direction)
                                    .coerceIn(0f, (tabsCount - 1).toFloat()),
                                timeMillis,
                            )
                        }
                    }
                },
            )
        }

    // The selected tab is authoritative. Drive both the label selection and the blob from this
    // one effect so rapid taps cancel the old spring and settle at the newest selected tab.
    LaunchedEffect(selectedTab) {
        if (selectedTab >= 0) {
            val target = selectedTab.coerceIn(0, tabsCount - 1)
            currentIndex = target
            if (dragSettlingTarget == target) {
                // A drag already carried the blob most of the way; keep the short spring settle.
                dragSettlingTarget = -1
                dampedDrag.animateToValue(target.toFloat())
            } else {
                // Taps move the glass selection in sync with the Home/Library page transition.
                dampedDrag.animateToValue(
                    target.toFloat(),
                    animationSpec = dampedDrag.selectionAnimationSpec,
                )
            }
        }
    }

    Box(
        modifier =
            modifier
                .height(BarHeight)
                .width(tabWidth * tabsCount + BarInset * 2)
                // Press detection for the whole capsule lives on the outer Box — it's the common
                // ancestor of the glass, blob and tab Row, so it sees the touch on the Initial pass
                // before the children. The capsule glass sits underneath the Row and would never get
                // the event on its own; it only reads barInteraction to draw the scale + glow.
                .pointerInput(barInteraction) { barInteraction.detectPress(this) },
        contentAlignment = Alignment.CenterStart,
    ) {
        // 1) Dark frosted glass capsule — the exact same glass the MiniPlayer uses, so the bottom
        // bar and the mini player read as one material (drawInteractiveGlass, no white veil).
        // barInteraction makes the whole capsule respond to a press (scale + touch glow) like iOS;
        // it's observe-only, so tab taps and the blob drag keep working.
        Box(
            Modifier
                .matchParentSize()
                .drawInteractiveGlass(
                    isDark,
                    backdrop,
                    luminance,
                    BottomDockShape,
                    barInteraction,
                    blurScale = 0.72f,
                    // Keep a light frost for dark labels, while letting the page behind the
                    // capsule remain visible instead of turning the whole bar opaque white.
                    minScrim = if (isDark) 0.12f else 0.18f,
                    maxScrim = if (isDark) 0.5f else 0.4f,
                ),
        )

        // 2) Frosted blob selection indicator — slides behind the icons.
        Box(
            Modifier
                .graphicsLayer {
                    // Hide the indicator only when this bar has no selected tab.
                    alpha = if (selectedTab >= 0) 1f else 0f
                    // Per-tab slot start, no inset: the pill is exactly one tab wide, so any bias
                    // here shifts it off its own tab (it used to be inset 4dp to match a pill that
                    // was 8dp narrower than the slot).
                    translationX =
                        (if (isLtr) dampedDrag.value else (tabsCount - 1) - dampedDrag.value) * tabWidthPx +
                        BarInset.toPx()
                    scaleX = 1f + (dampedDrag.scaleX - 1f) * appearance.motionMultiplier
                    scaleY = 1f + (dampedDrag.scaleY - 1f) * appearance.motionMultiplier
                    val velocity = dampedDrag.velocity / 10f * appearance.motionMultiplier
                    scaleX /= 1f - (velocity * 0.75f).fastCoerceIn(-0.2f, 0.2f)
                    scaleY *= 1f - (velocity * 0.25f).fastCoerceIn(-0.2f, 0.2f)
                }
                // Reuse the capsule's already-diffused glass, as the slim dock does. Sampling
                // the raw page again with a smaller blur made the selected slot change color
                // at a different rate and added a second effect pass on every scroll frame.
                .clip(SelectionShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                .width(tabWidth)
                    .height(BlobHeight),
        )

        // 3) Crisp icons + labels on top, carrying the blob drag gesture.
        Row(
            // Tabs tile exactly [BarInset..+tabWidth..], the same origin the blob uses above — any
            // mismatch here puts the icon off the centre of its own pill.
            Modifier
                .matchParentSize()
                .padding(horizontal = BarInset)
                .then(dampedDrag.modifier),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            tabs.forEachIndexed { position, screen ->
                LiquidGlassTab(
                    screen = screen,
                    selected = selectedTab >= 0 && currentIndex == position,
                    width = tabWidth,
                ) {
                    if (position == currentIndex) {
                        // Re-tapping the active tab: snapshotFlow won't fire (state unchanged),
                        // so call onTabSelected directly to keep the reload / scroll-to-top behaviour.
                        onTabSelected(screen)
                    } else {
                        // Navigate immediately on tap. Don't route this through snapshotFlow: a
                        // concurrent drag-stop on the same Row can reset currentIndex back before the
                        // flow emits, which silently drops the tap (observed: currentIndex stuck at 0).
                        currentIndex = position
                        onTabSelected(screen)
                    }
                }
            }
        }
    }
}

@Composable
private fun LiquidGlassTab(
    screen: BottomNavScreen,
    selected: Boolean,
    width: Dp,
    onClick: () -> Unit,
) {
    val color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
    Column(
        Modifier
            .width(width)
            .fillMaxHeight()
            .clip(SelectionShape)
            .clickable(
                interactionSource = null,
                indication = null,
                role = Role.Tab,
                onClick = onClick,
            ),
        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CompositionLocalProvider(LocalContentColor provides color) {
            screen.icon()
            Text(
                text = stringResource(screen.title),
                style = typo().bodySmall,
                color = color,
                maxLines = 1,
                modifier = if (screen == BottomNavScreen.Home || screen == BottomNavScreen.Library) Modifier.offset(y = (-3).dp) else Modifier,
            )
        }
    }
}
