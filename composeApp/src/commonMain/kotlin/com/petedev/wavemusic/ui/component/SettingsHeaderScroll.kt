package com.petedev.wavemusic.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle

/** Uses the Home/Library scroll tracker for settings toolbars and floating controls. */
@Composable
internal fun rememberSettingsHeaderScrollOffset(
    headerHeight: Dp,
    pageKey: Any? = Unit,
    keepVisible: Boolean = false,
    sharedHeader: Boolean = true,
    scrollPosition: () -> Pair<Int, Int>,
): State<Float> {
    val host = LocalSharedHeaderState.current
    val pinned = host?.buttonsPinned == true
    val lifecycleOwner = LocalLifecycleOwner.current
    val maxHeightPx = with(LocalDensity.current) { headerHeight.toPx() }
    val readPosition by rememberUpdatedState(scrollPosition)
    val offset = remember(pageKey) { mutableFloatStateOf(0f) }
    LaunchedEffect(host, lifecycleOwner, pageKey, pinned, keepVisible, sharedHeader, maxHeightPx) {
        // Outgoing settings pages can remain composed during navigation. Only the resumed
        // page may publish scroll changes into the shared toolbar.
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            offset.floatValue = 0f
            if (sharedHeader) host?.resetHeaderOffset()
            val tracker = HeaderScrollOffsetTracker()
            snapshotFlow { readPosition() }.collect { (index, scrollOffset) ->
                val nextOffset = if (pinned || keepVisible) {
                    0f
                } else {
                    tracker.update(index, scrollOffset, offset.floatValue, maxHeightPx)
                }
                offset.floatValue = nextOffset
                if (sharedHeader) host?.updateHeaderOffset(nextOffset)
            }
        }
    }
    return offset
}
