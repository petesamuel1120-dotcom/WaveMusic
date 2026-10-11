package com.maxrave.simpmusic.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource

/** Observe vertical content scrolling without consuming it or changing the page's constraints. */
class LandscapeHeaderScrollState(private val travelPx: Float) {
    var offsetPx by mutableFloatStateOf(0f)
        private set

    val connection = object : NestedScrollConnection {
        override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
            if (travelPx > 0f) {
                // Only actual vertical movement hides chrome; sideways shelves and an attempted
                // scroll past the bottom leave it alone. Pulling back at the top reveals it.
                val delta = if (consumed.y == 0f && available.y > 0f) available.y else consumed.y
                offsetPx = (offsetPx + delta).coerceIn(-travelPx, 0f)
            }
            return Offset.Zero
        }
    }
}

val LocalLandscapeHeaderScrollState = staticCompositionLocalOf<LandscapeHeaderScrollState?> { null }
internal val LocalLandscapeHeaderHosted = staticCompositionLocalOf { false }

/** Apply movement once to a complete toolbar, including its surface and buttons. */
@Composable
fun Modifier.landscapeScrollingHeader(pinned: Boolean = false): Modifier {
    if (pinned) return this
    val scrollState = LocalLandscapeHeaderScrollState.current ?: return this
    if (LocalLandscapeHeaderHosted.current) return this
    return graphicsLayer { translationY = scrollState.offsetPx }
}
