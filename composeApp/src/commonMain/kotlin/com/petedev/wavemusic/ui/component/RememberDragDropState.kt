package com.petedev.wavemusic.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.zIndex
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

@Composable
fun rememberDragDropState(
    lazyListState: LazyListState,
    reorderDampingRatio: Float = 0.9f,
    reorderAnimationDurationMillis: Int? = null,
    onSwap: (Int, Int) -> Unit,
): DragDropState {
    val scope = rememberCoroutineScope()
    val currentOnSwap by rememberUpdatedState(onSwap)
    val state =
        remember(lazyListState, reorderDampingRatio, reorderAnimationDurationMillis) {
            DragDropState(
                state = lazyListState,
                reorderDampingRatio = reorderDampingRatio,
                reorderAnimationDurationMillis = reorderAnimationDurationMillis,
                onSwap = { from, to -> currentOnSwap(from, to) },
                scope = scope,
            )
        }
    return state
}

val LazyListItemInfo.offsetEnd: Int
    get() = this.offset + this.size

@OptIn(ExperimentalFoundationApi::class)
@ExperimentalFoundationApi
@Composable
fun LazyItemScope.DraggableItem(
    dragDropState: DragDropState,
    index: Int,
    modifier: Modifier,
    itemKey: Any,
    content: @Composable ColumnScope.(isDragging: Boolean) -> Unit,
) {
    // Follow the pointer directly while dragging. Animating each pointer update makes the row
    // trail behind the finger and gives queue reordering a sticky, clunky feel.
    val current = dragDropState.draggingItemOffset
    // Keep the gap in step with the pointer. Animating each crossed index lets the dragged row
    // overtake rows that are still moving, which makes their artwork and labels overlap.
    val placeholderTranslation = dragDropState.placeholderTranslationFor(index)
    val dragging = dragDropState.isDragging(itemKey)
    val draggingModifier =
        if (dragging) {
            Modifier
                .zIndex(1f)
                .graphicsLayer {
                    translationY = current
                }
        } else if (dragDropState.isSettling(itemKey)) {
            Modifier
                .zIndex(1f)
                .graphicsLayer {
                    // Read the Animatable directly in the layer so settling does not launch a
                    // second, nested animation or recompose the entire list on every frame.
                    translationY = dragDropState.settlingItemOffset
                }
        } else {
            Modifier
                .graphicsLayer {
                    // Once the new order reaches layout, the row already occupies its preview
                    // slot. Removing the translation must be immediate, without a second slide.
                    translationY = if (dragDropState.hasGapPreview) placeholderTranslation else 0f
                }
                .animateItem(
                    fadeInSpec = null,
                    fadeOutSpec = null,
                    placementSpec = if (dragDropState.isReordering) null else dragDropState.reorderAnimationSpec(),
                )
        }
    Column(modifier = modifier.then(draggingModifier)) {
        content(dragging)
    }
}

class DragDropState internal constructor(
    val state: LazyListState,
    internal val reorderDampingRatio: Float,
    internal val reorderAnimationDurationMillis: Int?,
    private val scope: CoroutineScope,
    private val onSwap: (Int, Int) -> Unit,
) {
    internal fun <T> reorderAnimationSpec(): FiniteAnimationSpec<T> =
        reorderAnimationDurationMillis?.let { durationMillis ->
            tween(durationMillis = durationMillis, easing = FastOutSlowInEasing)
        } ?: spring(dampingRatio = reorderDampingRatio, stiffness = Spring.StiffnessMedium)

    private var draggedDistance by mutableFloatStateOf(0f)
    private var draggingItemInitialOffset by mutableIntStateOf(0)
    internal val draggingItemOffset: Float
        get() =
            draggingItemLayoutInfo?.let { item ->
                draggingItemInitialOffset + draggedDistance - item.offset
            } ?: 0f
    private val draggingItemLayoutInfo: LazyListItemInfo?
        get() =
            state.layoutInfo.visibleItemsInfo
                .firstOrNull { it.key == draggedItemKey }

    private var draggedItemKey: Any? = null
    private var settlingItemKey by mutableStateOf<Any?>(null)
    private var settleJob: Job? = null
    internal val hasGapPreview: Boolean get() = initiallyDraggedElement != null
    internal val isReordering: Boolean get() = currentIndexOfDraggedItem != null || settlingItemKey != null
    internal val settlingItemOffset: Float
        get() = previousItemOffset.value -
            (state.layoutInfo.visibleItemsInfo.firstOrNull { it.key == settlingItemKey }?.offset ?: 0)

    internal fun isDragging(key: Any): Boolean =
        currentIndexOfDraggedItem != null && key == draggedItemKey

    internal fun isSettling(key: Any): Boolean = settlingItemKey != null && key == settlingItemKey
    internal var previousItemOffset = Animatable(0f)
        private set

    // used to obtain initial offsets on drag start
    private var initiallyDraggedElement by mutableStateOf<LazyListItemInfo?>(null)

    var currentIndexOfDraggedItem by mutableStateOf<Int?>(null)

    /** Rows follow the open slot left by the pointer-following item as it crosses them. */
    fun placeholderTranslationFor(index: Int): Float {
        val source = initiallyDraggedElement ?: return 0f
        val target = currentSwapFromTo?.second ?: return 0f
        // Once the player publishes the new order, the dragged row already occupies its target
        // slot. Drop the preview offsets at the same time instead of shifting the new neighbors.
        if (settlingItemKey != null && draggingItemLayoutInfo?.index == target) return 0f
        val draggedSize = source.size.toFloat()
        return when {
            target > source.index && index in (source.index + 1)..target -> -draggedSize
            target < source.index && index in target until source.index -> draggedSize
            else -> 0f
        }
    }

    private var currentSwapFromTo by mutableStateOf<Pair<Int, Int>?>(null)

    fun onDragStart(offset: Offset) {
        state.layoutInfo.visibleItemsInfo
            .firstOrNull { item -> offset.y.toInt() in item.offset..(item.offset + item.size) }
            ?.let(::startDragging)
    }

    /** Starts a reorder drag from an explicit row index, for dedicated drag-handle gestures. */
    fun onDragStart(index: Int) {
        state.layoutInfo.visibleItemsInfo
            .firstOrNull { item -> item.index == index }
            ?.let(::startDragging)
    }

    private fun startDragging(item: LazyListItemInfo) {
        if (settleJob?.isActive == true) return
        draggedDistance = 0f
        currentSwapFromTo = null
        draggedItemKey = item.key
        currentIndexOfDraggedItem = item.index
        initiallyDraggedElement = item
        draggingItemInitialOffset = item.offset
    }

    fun onDragInterrupted(end: Boolean = false) {
        val sourceIndex = currentIndexOfDraggedItem ?: return
        val key = draggedItemKey ?: return
        val move = currentSwapFromTo?.takeIf { end && it.first != it.second }
        val destination = move?.second ?: sourceIndex
        val landingTop = state.layoutInfo.visibleItemsInfo.firstOrNull { it.index == destination }?.offset
            ?: draggingItemLayoutInfo?.offset ?: draggingItemInitialOffset
        // Store an absolute visual position and the row's identity, not the destination index.
        // The player publishes its new order asynchronously; until then that index is another song.
        previousItemOffset = Animatable(draggingItemInitialOffset + draggedDistance)
        settlingItemKey = key
        currentIndexOfDraggedItem = null
        if (move == null) currentSwapFromTo = null
        settleJob = scope.launch {
            try {
                // Finish the preview first. The real move can then replace it without competing
                // with row placement animations.
                previousItemOffset.animateTo(landingTop.toFloat(), reorderAnimationSpec())
                if (move != null) {
                    onSwap(move.first, move.second)
                    withTimeoutOrNull(1500L) {
                        snapshotFlow {
                            state.layoutInfo.visibleItemsInfo.firstOrNull { it.key == key }?.index
                        }.first { it == destination }
                    }
                }
            } finally {
                currentSwapFromTo = null
                initiallyDraggedElement = null
                settlingItemKey = null
                draggedItemKey = null
                draggingItemInitialOffset = 0
                draggedDistance = 0f
            }
        }
    }

    fun onDrag(offset: Offset) {
        val source = initiallyDraggedElement ?: return
        val sourceIndex = currentIndexOfDraggedItem ?: return
        draggedDistance += offset.y
        val draggedCenter = draggingItemInitialOffset + draggedDistance + source.size / 2f
        val sourceCenter = (draggingItemLayoutInfo?.offset ?: source.offset) + source.size / 2f
        val visibleItems = state.layoutInfo.visibleItemsInfo
        val target =
            when {
                draggedCenter > sourceCenter ->
                    visibleItems
                        .asSequence()
                        .filter { it.index > sourceIndex && draggedCenter > it.offset + it.size / 2f }
                        .maxByOrNull { it.index }
                draggedCenter < sourceCenter ->
                    visibleItems
                        .asSequence()
                        .filter { it.index < sourceIndex && draggedCenter < it.offset + it.size / 2f }
                        .minByOrNull { it.index }
                else -> null
            }
        currentSwapFromTo = target?.let { sourceIndex to it.index }
    }

    fun checkForOverScroll(): Float {
        return initiallyDraggedElement?.let {
            val startOffset = it.offset + draggedDistance
            val endOffset = it.offsetEnd + draggedDistance
            return@let when {
                draggedDistance > 0 -> (endOffset - state.layoutInfo.viewportEndOffset + 50f).takeIf { diff -> diff > 0 }
                draggedDistance < 0 -> (startOffset - state.layoutInfo.viewportStartOffset - 50f).takeIf { diff -> diff < 0 }
                else -> null
            }
        } ?: 0f
    }
}
