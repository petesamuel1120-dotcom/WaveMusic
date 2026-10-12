package com.petedev.wavemusic.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavBackStackEntry
import com.petedev.wavemusic.expect.ui.PlatformBackdrop
import dev.chrisbanes.haze.HazeState
import com.petedev.wavemusic.ui.theme.LocalLiquidGlassEnabled

// Centering the same controls in a shorter bar lifts them without adding a visual offset.
// Insets and page/search spacing follow these heights as well.
val SharedHeaderContentHeight: Dp
    @Composable get() = landscapeChromeSize(
        if (isCompactLandscapeChrome()) 38.dp else if (LocalLiquidGlassEnabled.current) 56.dp else 48.dp,
    )
val CompactHeaderContentHeight: Dp
    @Composable get() = SharedHeaderContentHeight

typealias SharedHeaderContent = @Composable BoxScope.() -> Unit

/** Page presentation; the shared host supplies the fixed viewport source for its surface and controls. */
data class SharedHeaderPresentation(
    val content: SharedHeaderContent,
    val hazeState: HazeState,
    val backdrop: PlatformBackdrop?,
    val tint: Color,
    val showSurface: Boolean,
    val fadeToContent: Boolean,
)

class SharedHeaderState {
    private val headers = mutableStateMapOf<String, State<SharedHeaderPresentation>>()
    var lastHeader: State<SharedHeaderPresentation>? = null
    var lastHeaderEntryId: String? = null
    var libraryPlaylistBackAction: (() -> Unit)? by mutableStateOf(null)
    var primaryHeaderVisible by mutableStateOf(true)
        private set
    var pageTransitionInProgress by mutableStateOf(false)
        private set
    var homeSurfaceVisible by mutableStateOf(false)
        private set
    var librarySurfaceVisible by mutableStateOf(false)
        private set
    var headerOffsetPx by mutableFloatStateOf(0f)
        private set

    var buttonsPinned by mutableStateOf(true)
        private set

    fun setHeaderButtonsPinned(pinned: Boolean) {
        buttonsPinned = pinned
        if (pinned) headerOffsetPx = 0f
    }

    fun updateHeaderOffset(offsetPx: Float) {
        headerOffsetPx = if (buttonsPinned) 0f else offsetPx
    }

    fun resetHeaderOffset() {
        headerOffsetPx = 0f
    }
    private val backdropSources = mutableStateMapOf<PlatformBackdrop, Set<Any>>()
    private val portraitSearches = mutableStateMapOf<String, Set<Any>>()

    /** A page source can be sampled only after a visible source has been laid out. */
    internal fun isBackdropReady(backdrop: PlatformBackdrop): Boolean =
        !backdropSources[backdrop].isNullOrEmpty()

    internal fun registerBackdropSource(backdrop: PlatformBackdrop, source: Any) {
        val current = backdropSources[backdrop].orEmpty()
        if (source !in current) {
            backdropSources[backdrop] = current + source
        }
    }

    internal fun unregisterBackdropSource(backdrop: PlatformBackdrop, source: Any) {
        val current = backdropSources[backdrop] ?: return
        if (source !in current) return
        val remaining = current - source
        if (remaining.isEmpty()) backdropSources.remove(backdrop) else backdropSources[backdrop] = remaining
    }

    /** Protect the field for its entire composition, including AnimatedVisibility's exit. */
    internal fun hasPortraitSearch(entryId: String?): Boolean =
        !portraitSearches[entryId].isNullOrEmpty()

    internal fun registerPortraitSearch(entryId: String, field: Any) {
        portraitSearches[entryId] = portraitSearches[entryId].orEmpty() + field
    }

    internal fun unregisterPortraitSearch(entryId: String, field: Any) {
        val remaining = portraitSearches[entryId].orEmpty() - field
        if (remaining.isEmpty()) portraitSearches.remove(entryId) else portraitSearches[entryId] = remaining
    }

    fun contentFor(entryId: String?): State<SharedHeaderPresentation>? = entryId?.let { headers[it] }

    fun updateHomeSurfaceVisibility(visible: Boolean) {
        homeSurfaceVisible = visible
    }

    fun updateLibrarySurfaceVisibility(visible: Boolean) {
        librarySurfaceVisible = visible
    }

    fun updatePrimaryHeaderVisibility(visible: Boolean) {
        primaryHeaderVisible = visible
    }

    fun updatePageTransitionInProgress(inProgress: Boolean) {
        pageTransitionInProgress = inProgress
    }

    internal fun register(entryId: String, header: State<SharedHeaderPresentation>) {
        headers[entryId] = header
    }

    internal fun unregister(entryId: String, header: State<SharedHeaderPresentation>) {
        if (headers[entryId] === header) headers.remove(entryId)
    }
}

val LocalSharedHeaderState = staticCompositionLocalOf<SharedHeaderState?> { null }
val LocalSharedHeaderHosted = staticCompositionLocalOf { false }
val LocalHeaderBackdrop = staticCompositionLocalOf<PlatformBackdrop?> { null }

/** Reports a direction after a small amount of real scroll, avoiding one-pixel reversals. */
class HeaderScrollDirectionTracker(private val thresholdPx: Float) {
    private var previousIndex: Int? = null
    private var previousOffset = 0
    private var pendingOffsetDelta = 0f

    /** Returns true for upward movement, false for downward movement, or null below threshold. */
    fun update(index: Int, offset: Int): Boolean? {
        val oldIndex = previousIndex
        if (oldIndex == null) {
            previousIndex = index
            previousOffset = offset
            return null
        }

        if (index != oldIndex) {
            previousIndex = index
            previousOffset = offset
            pendingOffsetDelta = 0f
            return index < oldIndex
        }

        pendingOffsetDelta += (offset - previousOffset).toFloat()
        previousOffset = offset
        return when {
            pendingOffsetDelta >= thresholdPx -> {
                pendingOffsetDelta = 0f
                false
            }
            pendingOffsetDelta <= -thresholdPx -> {
                pendingOffsetDelta = 0f
                true
            }
            else -> null
        }
    }
}

/** Tracks scroll deltas and updates a continuous header offset between [-maxHeightPx, 0f]. */
class HeaderScrollOffsetTracker {
    private var previousIndex: Int? = null
    private var previousOffset: Int = 0

    fun update(
        index: Int,
        offset: Int,
        currentHeaderOffsetPx: Float,
        maxHeightPx: Float,
        forceShowAtTop: Boolean = true,
    ): Float {
        if (forceShowAtTop && index == 0 && offset == 0) {
            previousIndex = 0
            previousOffset = 0
            return 0f
        }

        val oldIndex = previousIndex
        if (oldIndex == null) {
            previousIndex = index
            previousOffset = offset
            return currentHeaderOffsetPx
        }

        val deltaPx = if (index == oldIndex) {
            (offset - previousOffset).toFloat()
        } else if (index > oldIndex) {
            (offset + 150 - previousOffset).coerceAtLeast(1).toFloat()
        } else {
            (offset - (previousOffset + 150)).coerceAtMost(-1).toFloat()
        }

        previousIndex = index
        previousOffset = offset

        if (maxHeightPx <= 0f) return 0f
        return (currentHeaderOffsetPx - deltaPx).coerceIn(-maxHeightPx, 0f)
    }
}

/** Leave a layout marker so page content and search controls retain their spacing. */
@Composable
internal fun SharePageHeader(
    modifier: Modifier,
    presentation: SharedHeaderPresentation,
): Boolean {
    val host = LocalSharedHeaderState.current ?: return false
    val entry = LocalLifecycleOwner.current as? NavBackStackEntry ?: return false
    val currentHeader = rememberUpdatedState(presentation)
    DisposableEffect(host, entry.id) {
        host.register(entry.id, currentHeader)
        onDispose { host.unregister(entry.id, currentHeader) }
    }
    val density = LocalDensity.current
    val statusBarHeight = with(density) { WindowInsets.statusBars.getTop(this).toDp() }
    Box(modifier.height(statusBarHeight + SharedHeaderContentHeight))
    return true
}
