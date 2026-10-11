package com.maxrave.simpmusic.ui.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlin.math.abs

/** Reveal the list when search opens or is submitted, optionally when the first query is entered. */
@Composable
fun SearchResultsScrollEffect(
    state: LazyListState,
    searchVisible: Boolean,
    hasQuery: Boolean,
    firstResultIndex: Int,
    headerHeight: Dp,
    submissionCount: Int = 0,
    resultsReady: Boolean = true,
    scrollOnQueryChange: Boolean = true,
) {
    val density = LocalDensity.current
    val resultTop = headerHeight + (if (searchVisible && !isCompactLandscapeChrome()) InPageSearchBarHeight else 0.dp) + 8.dp
    val currentResultTop = rememberUpdatedState(with(density) { resultTop.toPx() })
    val currentResultsReady = rememberUpdatedState(resultsReady)
    val queryScrollTrigger = hasQuery && scrollOnQueryChange
    LaunchedEffect(state, searchVisible, queryScrollTrigger, firstResultIndex, submissionCount) {
        // Dismissing the overlay must leave the user's current list position alone,
        // including when a submitted query remains active after the keyboard closes.
        if (!searchVisible) return@LaunchedEffect
        // Let the field and keyboard finish opening/closing before measuring the list.
        delay(320)
        snapshotFlow {
            currentResultsReady.value && state.layoutInfo.totalItemsCount > firstResultIndex
        }.first { it }
        state.animateScrollToSearchResults(firstResultIndex, currentResultTop.value)
    }
}

private suspend fun LazyListState.animateScrollToSearchResults(index: Int, topInsetPx: Float) {
    // The preceding hero's bottom is measurable even when the first row is below the viewport.
    // Distant rows use an estimate first, then a measured correction; no instant scrollToItem.
    repeat(4) {
        withFrameNanos { }
        val layout = layoutInfo
        if (index >= layout.totalItemsCount) return
        val visible = layout.visibleItemsInfo
        val resultOffset = visible.firstOrNull { it.index == index }?.offset?.toFloat()
            ?: visible.firstOrNull { it.index == index - 1 }?.let { (it.offset + it.size).toFloat() }
        val nearest = visible.minByOrNull { abs(it.index - index) } ?: return
        val averageSize = visible.filter { it.size > 0 }.map { it.size }.average()
        if (resultOffset == null && (averageSize.isNaN() || averageSize <= 0.0)) return
        val estimatedOffset = resultOffset
            ?: (nearest.offset + (index - nearest.index) * averageSize).toFloat()
        // Lazy item offsets exclude contentPadding; viewportStartOffset accounts for it.
        val distance = estimatedOffset - (layout.viewportStartOffset + topInsetPx)
        if (abs(distance) < 1f) return
        val consumed = animateScrollBy(
            distance,
            tween(
                durationMillis = (280 + abs(distance) / 6).toInt().coerceIn(280, 560),
                easing = FastOutSlowInEasing,
            ),
        )
        if (abs(consumed) < 1f) return
    }
}
