@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package com.petedev.wavemusic.ui.screen.player.content.applemusic

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.indication
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.petedev.wavemusic.domain.manager.DataStoreManager
import com.petedev.wavemusic.domain.mediaservice.handler.MediaPlayerHandler
import com.petedev.wavemusic.domain.mediaservice.handler.QueueData
import com.petedev.wavemusic.domain.mediaservice.handler.RepeatState
import com.petedev.wavemusic.expect.ui.DeviceVolumeController
import com.petedev.wavemusic.expect.ui.PlatformBackdrop
import com.petedev.wavemusic.ui.component.DraggableItem
import com.petedev.wavemusic.ui.component.QueueItemBottomSheet
import com.petedev.wavemusic.ui.component.SongFullWidthItems
import com.petedev.wavemusic.ui.component.rememberDragDropState
import com.petedev.wavemusic.ui.icon.Repeat
import com.petedev.wavemusic.ui.icon.RepeatOne
import com.petedev.wavemusic.ui.icon.Shuffle
import com.petedev.wavemusic.ui.icon.WaveIcons
import com.petedev.wavemusic.ui.screen.player.content.NowPlayingContentActions
import com.petedev.wavemusic.ui.screen.player.content.NowPlayingContentState
import com.petedev.wavemusic.viewModel.UIEvent
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import com.petedev.wavemusic.resources.Res
import com.petedev.wavemusic.resources.endless_queue
import com.petedev.wavemusic.resources.queue

/**
 * The QUEUE body: compact header, centered [Shuffle][Repeat] pills, a Queue header
 * (with the endless-queue switch) and the upcoming tracks — the OLD queue sheet's
 * [SongFullWidthItems] rows with handle-only long-press-drag reorder and its per-item ⋯ sheet
 * ([QueueItemBottomSheet]: move up/down/delete), matching
 * [com.petedev.wavemusic.ui.component.QueueBottomSheet] exactly.
 * Ends in the same fixed bottom cluster as MAIN/LYRICS.
 */
@Composable
internal fun AppleMusicQueueView(
    state: NowPlayingContentState,
    actions: NowPlayingContentActions,
    typography: AppleMusicTypography,
    viewState: AppleMusicView,
    onSelectView: (AppleMusicView) -> Unit,
    activePillContainer: Color,
    activePillContent: Color,
    backdrop: PlatformBackdrop,
    deviceVolumeController: DeviceVolumeController?,
    sharedClusterHeight: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
    embedded: Boolean = false,
    dataStoreManager: DataStoreManager = koinInject(),
    musicServiceHandler: MediaPlayerHandler = koinInject(),
) {
    val localDensity = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    // Upcoming tracks live in the SAME index space as musicServiceHandler.swap(from, to) and
    // removeMediaItem(index): artworkQueue == queueData.data.listTracks, and both operations
    // read/write that list (and the player timeline) at the SAME position — confirmed by reading
    // MediaServiceHandlerImpl.removeMediaItem/swap and ExoPlayerAdapter.moveMediaItem/
    // removeMediaItem/getUnshuffledIndex, which all treat their index argument as "current
    // shuffle/display order", i.e. exactly artworkQueue's own order. `offset` converts a position
    // within this UPCOMING-ONLY sublist back to that absolute space.
    val offset = state.currentOrderIndex + 1
    // withIndex() BEFORE drop(), so every row carries the index it had in the full queue.
    //
    // This used to be a plain drop() with `offset + localIndex` added back at click time, and that
    // is exactly where the Apple Music queue diverged from QueueBottomSheet — which never computes
    // an index at all, it reads the one itemsIndexed hands it over the whole list. Adding the
    // offset back makes every row's identity depend on state.currentOrderIndex, a value DERIVED in
    // the shell; the moment that derivation is off by anything, the row the user tapped and the
    // index sent to the player are two different songs. Carrying the original index removes the
    // arithmetic, so a wrong currentOrderIndex can now only cut the list in the wrong PLACE — it
    // can no longer play the wrong song, because the index travels with the track it belongs to.
    val upcoming =
        remember(state.artworkQueue, state.currentOrderIndex) {
            if (state.currentOrderIndex < 0) emptyList() else state.artworkQueue.withIndex().drop(offset)
        }
    // Row keys by track, NOT by position: a radio queue drops played tracks off the front, and a
    // position-based key changes for every row when that happens, so the list loses its scroll
    // anchor and jumps under the user. The occurrence number keeps it unique when a radio repeats
    // a song. Indexed by the ABSOLUTE queue position each row carries.
    val rowKeys =
        remember(state.artworkQueue) {
            val seen = HashMap<String, Int>(state.artworkQueue.size)
            state.artworkQueue.map { track ->
                val occurrence = seen.getOrElse(track.videoId) { 0 }
                seen[track.videoId] = occurrence + 1
                "${track.videoId}#$occurrence"
            }
        }

    Column(modifier = modifier.fillMaxSize()) {
        if (!embedded) {
        // statusBars + 20dp, not a bare status-bar offset: the grabber that
        // NowPlayingContentAppleMusic draws above the view Crossfade floats over this column, and
        // without the extra room the header's title slides underneath it.
        Spacer(
            modifier =
                Modifier.height(
                    with(localDensity) { WindowInsets.statusBars.getTop(localDensity).toDp() } + 20.dp,
                ),
        )
        // Shared above the Lyrics/Queue AnimatedContent so the song row never remounts.
        Spacer(Modifier.height(79.dp))
        }
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            // A single row leaves an actual song viewport on short landscape phones. Stack
            // at narrow widths or large font scales so labels and controls remain comfortable.
            val inlineActions = embedded && maxWidth >= 400.dp * localDensity.fontScale.coerceAtLeast(1f)
            Column {
                if (!inlineActions) {
                    AppleMusicQueuePillsRow(
                        state = state,
                        actions = actions,
                        backdrop = backdrop,
                        activePillContainer = activePillContainer,
                        activePillContent = activePillContent,
                        compact = embedded,
                        modifier = Modifier.padding(top = if (embedded) 0.dp else 4.dp, bottom = if (embedded) 0.dp else 20.dp),
                    )
                }
                AppleMusicContinuePlayingHeader(
                    dataStoreManager = dataStoreManager,
                    typography = typography,
                    activePillContainer = activePillContainer,
                    activePillContent = activePillContent,
                    compact = embedded,
                    queueActions = if (inlineActions) {
                        {
                            AppleMusicQueuePillsRow(
                                state = state,
                                actions = actions,
                                backdrop = backdrop,
                                activePillContainer = activePillContainer,
                                activePillContent = activePillContent,
                                compact = true,
                                modifier = Modifier.width(202.dp).padding(end = 8.dp),
                            )
                        }
                    } else null,
                    modifier = Modifier.padding(bottom = if (embedded) 2.dp else 8.dp),
                )
            }
        }

        val lazyListState = rememberLazyListState()
        // rememberDragDropState retains its swap lambda for this list, so capture the changing
        // offset through state instead of using the value from the first composition.
        val currentOffset by rememberUpdatedState(offset)
        val dragDropState =
            rememberDragDropState(lazyListState, reorderAnimationDurationMillis = 240) { from, to ->
                actions.onMoveQueueItem(from + currentOffset, to + currentOffset)
            }

        // Follow the track change. The list holds only what is still to come, so every time the
        // player advances the row that was at the top leaves it and everything shifts up by one —
        // but the scroll offset does not move, so a queue the user had scrolled through stays
        // parked mid-list with the track that is actually next off-screen. Re-anchoring to the top
        // is what "showing the current position" means for an upcoming-only list.
        LaunchedEffect(state.currentOrderIndex) {
            if (state.currentOrderIndex >= 0) {
                lazyListState.animateScrollToItem(0)
            }
        }

        // Endless/radio queues page in as you scroll — same trigger QueueBottomSheet uses.
        val loadMoreState by remember {
            derivedStateOf { musicServiceHandler.queueData.value?.queueState ?: QueueData.StateSource.STATE_CREATED }
        }
        val shouldLoadMore by remember {
            derivedStateOf {
                val layoutInfo = lazyListState.layoutInfo
                val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull() ?: return@derivedStateOf true
                lastVisibleItem.index >= layoutInfo.totalItemsCount - 3 && layoutInfo.totalItemsCount > 0
            }
        }
        LaunchedEffect(Unit) {
            snapshotFlow { shouldLoadMore }
                .collect { if (it && loadMoreState == QueueData.StateSource.STATE_INITIALIZED) musicServiceHandler.loadMore() }
        }
        var overscrollJob by remember { mutableStateOf<Job?>(null) }

        // Same per-item sheet the old queue sheet opens from a row's ⋯ (move up/down/delete).
        var queueItemSheetIndex by remember { mutableStateOf(-1) }
        var queueItemSheetVideoId by remember { mutableStateOf<String?>(null) }
        if (queueItemSheetIndex >= 0) {
            QueueItemBottomSheet(
                onDismiss = { queueItemSheetIndex = -1 },
                index = queueItemSheetIndex,
                videoId = queueItemSheetVideoId,
            )
        }

        val topFade = if (embedded) 4.dp else QUEUE_TOP_FADE
        val bottomFade = if (embedded) 6.dp else QUEUE_BOTTOM_FADE
        Box(
            modifier =
                Modifier
                    .weight(1f)
                    .appleMusicVerticalFadeEdges(topFade = topFade, bottomFade = bottomFade),
        ) {
            LazyColumn(
                state = lazyListState,
                // Space at BOTH ends equal to the fade at that end, so each fade lands on blank
                // space instead of dissolving a real row. Only the bottom had it, which is why the
                // first upcoming track — the one you most want to read — came up half faded out.
                contentPadding = PaddingValues(top = topFade, bottom = bottomFade),
                modifier =
                    Modifier
                        .fillMaxSize(),
            ) {
                itemsIndexed(
                    upcoming,
                    // Absolute index in the key: `upcoming` is a sublist, so a bare local index
                    // shifts on every track change and invalidates every row.
                    key = { _, item -> rowKeys.getOrElse(item.index) { item.value.videoId } },
                ) { localIndex, item ->
                    val track = item.value
                    val rowKey = rowKeys.getOrElse(item.index) { track.videoId }
                    // The queue-wide index this row actually has. Everything the PLAYER is told
                    // uses this; only the drag gesture below uses localIndex, because that one is
                    // genuinely about position within the visible list.
                    val queueIndex = item.index
                    DraggableItem(
                        dragDropState = dragDropState,
                        index = localIndex,
                        modifier = Modifier,
                        itemKey = rowKey,
                    ) { _ ->
                        // Keep the existing queue row and its per-item ⋯ sheet. Reordering is
                        // enabled exclusively from its dedicated drag handle.
                        SongFullWidthItems(
                            track = track,
                            isPlaying = false,
                            modifier = Modifier.fillMaxWidth(),
                            onClickListener = { videoId ->
                                if (videoId == track.videoId) actions.onSeekToQueueIndex(queueIndex)
                            },
                            onMoreClickListener = {
                                queueItemSheetVideoId = track.videoId
                                queueItemSheetIndex = queueIndex
                            },
                            shouldShowDragHandle = true,
                            dragHandleContentDescription = "Drag to reorder",
                            // Reordering starts only from the dedicated handle. Other parts of the
                            // row keep their normal tap behavior and the list remains scrollable.
                            dragHandleModifier =
                                Modifier.size(48.dp).pointerInput(localIndex) {
                                    detectDragGestures(
                                        onDragStart = { dragDropState.onDragStart(localIndex) },
                                        onDrag = { change, dragOffset ->
                                            change.consume()
                                            dragDropState.onDrag(dragOffset)
                                            if (overscrollJob?.isActive == true) {
                                                return@detectDragGestures
                                            }
                                            dragDropState
                                                .checkForOverScroll()
                                                .takeIf { it != 0f }
                                                ?.let { delta ->
                                                    overscrollJob =
                                                        coroutineScope.launch {
                                                            dragDropState.state.animateScrollBy(
                                                                delta * 1.3f,
                                                                tween(easing = FastOutLinearInEasing),
                                                            )
                                                        }
                                                } ?: run { overscrollJob?.cancel() }
                                        },
                                        onDragEnd = {
                                            dragDropState.onDragInterrupted(true)
                                            overscrollJob?.cancel()
                                        },
                                        onDragCancel = {
                                            dragDropState.onDragInterrupted()
                                            overscrollJob?.cancel()
                                        },
                                    )
                                },
                            // Place the ⋮ first and the drag handle at the far right, matching the
                            // queue row controls in the reference.
                            endContentPadding = 0.dp,
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(sharedClusterHeight))
    }
}

/** Center the two queue actions while retaining the cover screen's playlist button. */
@Composable
private fun AppleMusicQueuePillsRow(
    state: NowPlayingContentState,
    actions: NowPlayingContentActions,
    backdrop: PlatformBackdrop,
    activePillContainer: Color,
    activePillContent: Color,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val repeatState = state.controllerState.repeatState
    Box(
        modifier = modifier.fillMaxWidth().padding(horizontal = if (compact) LandscapePlayerContentInset else 20.dp),
        contentAlignment = Alignment.Center,
    ) {
    Row(
        modifier = Modifier.fillMaxWidth(2f / 3f),
        horizontalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 10.dp),
    ) {
        AppleMusicQueuePill(
            icon = WaveIcons.Shuffle,
            backdrop = backdrop,
            active = state.controllerState.isShuffle,
            activeContainer = activePillContainer,
            activeContent = activePillContent,
            onClick = { actions.onUIEvent(UIEvent.Shuffle) },
            modifier = Modifier.weight(1f),
            compact = compact,
        )
        AppleMusicQueuePill(
            icon = if (repeatState is RepeatState.One) WaveIcons.RepeatOne else WaveIcons.Repeat,
            backdrop = backdrop,
            active = repeatState !is RepeatState.None,
            activeContainer = activePillContainer,
            activeContent = activePillContent,
            onClick = { actions.onUIEvent(UIEvent.Repeat) },
            modifier = Modifier.weight(1f),
            compact = compact,
        )
    }
    }
}

@Composable
private fun AppleMusicQueuePill(
    icon: ImageVector,
    contentDescription: String = "",
    backdrop: PlatformBackdrop,
    active: Boolean,
    activeContainer: Color,
    activeContent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val shape = RoundedCornerShape(20.dp)
    val interactionSource = remember { MutableInteractionSource() }
    val surface = Modifier.background(if (active) activeContainer else AppleMusicPillInactive)
    val iconTint = if (active) activeContent else Color.White
    Box(
        modifier =
            modifier
                // Gentler than the default 1.35×: a pill is 83dp wide with only a 10dp gap, so
                // the full inflate would visibly overlap its neighbour on every tap.
                .appleMusicPressInflate(pressedScale = 1.08f)
                .height(40.dp)
                // Keep the full hit area, but draw feedback inside the visible pill.
                .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
                .then(if (compact) Modifier.padding(vertical = 2.dp) else Modifier)
                .clip(shape)
                .then(surface)
                .indication(interactionSource, LocalIndication.current),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconTint,
            modifier = Modifier.size(if (compact) 18.dp else 20.dp),
        )
    }
}

@Composable
private fun AppleMusicContinuePlayingHeader(
    dataStoreManager: DataStoreManager,
    typography: AppleMusicTypography,
    activePillContainer: Color,
    activePillContent: Color,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    queueActions: (@Composable () -> Unit)? = null,
) {
    val coroutineScope = rememberCoroutineScope()
    // remember the mapped flow: a bare .map in composition builds a new Flow every recomposition
    // (FlowOperatorInvokedInComposition, promoted to an error here).
    val endlessQueueFlow =
        remember(dataStoreManager) {
            dataStoreManager.endlessQueue.map { it == DataStoreManager.TRUE }
        }
    val endlessQueueEnabled by endlessQueueFlow.collectAsStateWithLifecycle(initialValue = false)
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = if (compact) LandscapePlayerContentInset else 20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(Res.string.queue),
                style = typography.queueSectionHeader,
                maxLines = if (compact) 1 else Int.MAX_VALUE,
                modifier = Modifier.weight(1f),
            )
            queueActions?.invoke()
            // The switch needs its own label, exactly like the queue sheet's — unlabelled it
            // reads as a mystery toggle.
            Text(
                text = stringResource(Res.string.endless_queue),
                style = typography.queueSectionSubtitle,
                maxLines = if (compact) 1 else Int.MAX_VALUE,
                modifier = Modifier.padding(end = if (compact) 4.dp else 8.dp),
            )
            Switch(
                checked = endlessQueueEnabled,
                onCheckedChange = { checked ->
                    coroutineScope.launch { dataStoreManager.setEndlessQueue(checked) }
                },
                colors =
                    SwitchDefaults.colors(
                        // On state takes the artwork-derived pair this style already uses for its
                        // active pills, instead of the theme's green — on a page painted from the
                        // cover art, a fixed accent is the one element that does not belong to the
                        // record playing.
                        checkedTrackColor = activePillContainer,
                        checkedThumbColor = activePillContent,
                        checkedBorderColor = Color.Transparent,
                        // Transparent when off, so the control reads as an outline sitting on the
                        // page rather than a grey slab: this row has no surface of its own, and
                        // Material's default unchecked track paints one.
                        uncheckedTrackColor = Color.Transparent,
                        uncheckedBorderColor = Color.White.copy(alpha = 0.45f),
                        uncheckedThumbColor = Color.White.copy(alpha = 0.75f),
                    ),
                modifier = Modifier.appleMusicPressInflate(pressedScale = 1.04f, dampingRatio = 1f),
            )
        }
    }
}

// Keep a small fade buffer at the top, but use more of the space directly under the Queue header.
private val QUEUE_TOP_FADE = 8.dp
private val QUEUE_BOTTOM_FADE = 18.dp
