package com.petedev.wavemusic.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.petedev.wavemusic.expect.ui.PlatformBackdrop
import com.petedev.wavemusic.ui.theme.LocalLiquidGlassEnabled

@Composable
fun pageHeaderContentHeight(): Dp =
    if (LocalSharedHeaderState.current != null) SharedHeaderContentHeight else CompactHeaderContentHeight

/** All page bars use the playlist's compact glass controls over the unblurred page source. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(),
    contentHeight: Dp = if (LocalSharedHeaderHosted.current) SharedHeaderContentHeight else CompactHeaderContentHeight,
    keepControlContainers: Boolean = false,
    navigationIconInGlassContainer: Boolean = true,
    controlHorizontalInset: Dp? = null,
    backdrop: PlatformBackdrop? = LocalHeaderBackdrop.current,
    // Solid toolbars can use plain icon buttons regardless of the app's glass setting.
    controlContainersEnabled: Boolean = true,
) {
    // Shared controls are rendered outside the moving destination. Its explicit backdrop
    // belongs to that page; prefer the host's fixed source when the header is lifted into it.
    val controlBackdrop =
        if (LocalSharedHeaderHosted.current) LocalHeaderBackdrop.current ?: backdrop else backdrop
    val controlSize = headerGlassControlSize()
    val defaultIconSize = headerGlassIconSize()
    val onyxBackground = isOnyxBackground()
    val useContainers = controlContainersEnabled && controlBackdrop != null &&
        (LocalLiquidGlassEnabled.current || keepControlContainers || onyxBackground)
    val navigationContent: @Composable () -> Unit = {
        if (navigationIcon != null) {
            if (useContainers && navigationIconInGlassContainer) {
                LiquidGlassContainer(controlBackdrop!!, Modifier.size(controlSize).sharedHeaderControlsMorph(SharedHeaderControl.Navigation), showFlatContainer = onyxBackground) {
                    CompositionLocalProvider(LocalHeaderIconSize provides defaultIconSize) {
                        Box(contentAlignment = Alignment.Center) { navigationIcon() }
                    }
                }
            } else {
                Box(
                    modifier = Modifier.size(controlSize).sharedHeaderControlsMorph(SharedHeaderControl.Navigation),
                    contentAlignment = Alignment.Center,
                ) {
                    CompositionLocalProvider(LocalHeaderIconSize provides defaultIconSize) { navigationIcon() }
                }
            }
        }
    }
    val actionContent: @Composable RowScope.() -> Unit = {
        if (actions != null) {
            if (useContainers) {
                LiquidGlassButtonGroup(controlBackdrop!!, Modifier.height(controlSize).sharedHeaderControlsMorph(SharedHeaderControl.Actions), showFlatContainer = onyxBackground) {
                    CompositionLocalProvider(LocalHeaderIconSize provides defaultIconSize) { actions() }
                }
            } else {
                Row(modifier = Modifier.sharedHeaderControlsMorph(SharedHeaderControl.Actions)) {
                    CompositionLocalProvider(LocalHeaderIconSize provides defaultIconSize) { actions() }
                }
            }
        }
    }
    val centerTitle = !LocalLiquidGlassEnabled.current || LocalSharedHeaderHosted.current
    val density = LocalDensity.current
    var actionsWidth by remember { mutableStateOf(0.dp) }
    val titleEdge = maxOf(controlSize, actionsWidth) + 12.dp
    CompositionLocalProvider(
        LocalHeaderControlSize provides controlSize,
        LocalMinimumInteractiveComponentSize provides Dp.Unspecified,
    ) {
        Box(
            modifier.fillMaxWidth()
                .windowInsetsPadding(if (LocalSharedHeaderHosted.current) WindowInsets(0) else windowInsets)
                .height(contentHeight)
                .padding(horizontal = controlHorizontalInset ?: AppContentHorizontalInset),
        ) {
            // Matching slots anchor one-to-one control morphs at the same window coordinates.
            Row(Modifier.matchParentSize(), verticalAlignment = Alignment.CenterVertically) {
                if (navigationIcon != null) {
                    CompositionLocalProvider(LocalContentColor provides colors.navigationIconContentColor) {
                        navigationContent()
                    }
                } else Spacer(Modifier.width(controlSize))
                Spacer(Modifier.weight(1f))
                Row(
                    Modifier.onSizeChanged { actionsWidth = with(density) { it.width.toDp() } },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CompositionLocalProvider(LocalContentColor provides colors.actionIconContentColor) {
                        actionContent()
                    }
                }
            }
            Box(
                Modifier.matchParentSize().padding(
                    start = if (centerTitle) titleEdge else controlSize + 12.dp,
                    end = titleEdge,
                ).sharedHeaderTitleVisibility(),
                contentAlignment = if (centerTitle) Alignment.Center else Alignment.CenterStart,
            ) {
                CompositionLocalProvider(LocalContentColor provides colors.titleContentColor) {
                    LandscapeChromeScaledContent { title() }
                }
            }
        }
    }
}
