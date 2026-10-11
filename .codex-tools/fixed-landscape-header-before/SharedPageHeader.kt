package com.maxrave.simpmusic.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
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
import com.maxrave.simpmusic.expect.ui.PlatformBackdrop
import dev.chrisbanes.haze.HazeState

// Centering the same controls in a shorter bar lifts them without adding a visual offset.
// Insets and page/search spacing follow these heights as well.
val SharedHeaderContentHeight: Dp
    @Composable get() = if (isCompactLandscapeChrome()) 38.dp else 56.dp
val CompactHeaderContentHeight: Dp
    @Composable get() = if (isCompactLandscapeChrome()) 38.dp else 52.dp

typealias SharedHeaderContent = @Composable BoxScope.() -> Unit

/** Page presentation; the shared host supplies the fixed viewport source for its surface and controls. */
data class SharedHeaderPresentation(
    val content: SharedHeaderContent,
    val hazeState: HazeState,
    val backdrop: PlatformBackdrop?,
    val tint: Color,
    val showSurface: Boolean,
    val fadeToContent: Boolean,
    val pinInLandscape: Boolean = false,
)

class SharedHeaderState {
    private val headers = mutableStateMapOf<String, State<SharedHeaderPresentation>>()
    var lastHeader: SharedHeaderPresentation? = null
    var homeSurfaceVisible by mutableStateOf(false)
        private set
    var librarySurfaceVisible by mutableStateOf(false)
        private set
    private val backdropSources = mutableStateMapOf<PlatformBackdrop, Set<Any>>()
    private val portraitSearches = mutableStateMapOf<String, Set<Any>>()

    /** A page source can be sampled only after a visible source has been laid out. */
    internal fun isBackdropReady(backdrop: PlatformBackdrop): Boolean =
        !backdropSources[backdrop].isNullOrEmpty()

    internal fun registerBackdropSource(backdrop: PlatformBackdrop, source: Any) {
        backdropSources[backdrop] = backdropSources[backdrop].orEmpty() + source
    }

    internal fun unregisterBackdropSource(backdrop: PlatformBackdrop, source: Any) {
        val remaining = backdropSources[backdrop].orEmpty() - source
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
