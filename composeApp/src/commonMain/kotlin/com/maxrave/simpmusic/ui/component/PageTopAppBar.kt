package com.maxrave.simpmusic.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.maxrave.simpmusic.expect.ui.PlatformBackdrop
import com.maxrave.simpmusic.ui.theme.LocalLiquidGlassEnabled

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
    val onyxBackground = isOnyxBackground()
    val useContainers = controlContainersEnabled && controlBackdrop != null &&
        (LocalLiquidGlassEnabled.current || keepControlContainers || onyxBackground)
    val navigationContent: @Composable () -> Unit = {
        if (navigationIcon != null) {
            if (useContainers && navigationIconInGlassContainer) {
                LiquidGlassContainer(controlBackdrop!!, Modifier.padding(start = controlHorizontalInset ?: 8.dp).size(controlSize), showFlatContainer = onyxBackground) {
                    Box(contentAlignment = Alignment.Center) { navigationIcon() }
                }
            } else if (!navigationIconInGlassContainer || controlHorizontalInset != null) {
                Box(
                    modifier = Modifier.padding(start = controlHorizontalInset ?: 8.dp).size(controlSize),
                    contentAlignment = Alignment.Center,
                ) { navigationIcon() }
            } else {
                navigationIcon()
            }
        }
    }
    val actionContent: @Composable RowScope.() -> Unit = {
        if (actions != null) {
            if (useContainers) {
                LiquidGlassButtonGroup(controlBackdrop!!, Modifier.padding(end = controlHorizontalInset ?: 8.dp).height(controlSize), showFlatContainer = onyxBackground) {
                    actions()
                }
            } else if (controlHorizontalInset != null) {
                Row(modifier = Modifier.padding(end = controlHorizontalInset), content = actions)
            } else {
                actions()
            }
        }
    }
    if (LocalLiquidGlassEnabled.current && !LocalSharedHeaderHosted.current) {
        TopAppBar(
            title = title,
            modifier = modifier,
            navigationIcon = navigationContent,
            actions = actionContent,
            windowInsets = windowInsets,
            colors = colors,
            expandedHeight = contentHeight,
        )
    } else {
        CenterAlignedTopAppBar(
            title = title,
            modifier = modifier,
            navigationIcon = navigationContent,
            actions = actionContent,
            expandedHeight = contentHeight,
            windowInsets = if (LocalSharedHeaderHosted.current) WindowInsets(0) else windowInsets,
            colors = colors,
        )
    }
}
