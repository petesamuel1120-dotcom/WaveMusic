package com.maxrave.simpmusic.ui.screen.player.content.applemusic

import com.maxrave.simpmusic.ui.component.localizedDescription
import simpmusic.composeapp.generated.resources.playback_toggle
import simpmusic.composeapp.generated.resources.Res

import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.maxrave.simpmusic.expect.ui.DeviceVolumeController
import com.maxrave.simpmusic.expect.ui.PlatformBackdrop
import com.maxrave.simpmusic.ui.component.landscapeContentWindowInsets
import com.maxrave.simpmusic.ui.icon.Pause
import com.maxrave.simpmusic.ui.icon.PlayArrow
import com.maxrave.simpmusic.ui.icon.SimpIcons
import com.maxrave.simpmusic.ui.screen.player.content.AppleMusicMainTitleRow
import com.maxrave.simpmusic.ui.screen.player.content.NowPlayingContentActions
import com.maxrave.simpmusic.ui.screen.player.content.NowPlayingContentState
import com.maxrave.simpmusic.ui.screen.player.content.isCurrentVideoItem
import com.maxrave.simpmusic.ui.theme.typo
import com.maxrave.simpmusic.viewModel.UIEvent
import kotlinx.coroutines.delay

// Align track information, queue headings and transport to the same pane edges.
internal val LandscapePlayerContentInset = 0.dp

/** An edge-to-edge cover beside a scrollable body, with one persistent transport. */
@Composable
internal fun AppleMusicLandscapePlayer(
    state: NowPlayingContentState,
    actions: NowPlayingContentActions,
    typography: AppleMusicTypography,
    viewState: AppleMusicView,
    onSelectView: (AppleMusicView) -> Unit,
    activePillContainer: Color,
    activePillContent: Color,
    backdrop: PlatformBackdrop,
    deviceVolumeController: DeviceVolumeController?,
    showLyricsControls: Boolean,
    onToggleLyricsControls: () -> Unit,
    artworkContent: @Composable (Dp) -> Unit,
) {
    val isPlaying = state.controllerState.isPlaying
    val artworkPlaybackTapEnabled = viewState == AppleMusicView.LYRICS && !showLyricsControls
    var previousPlaying by remember(state.currentTrackId) { mutableStateOf(isPlaying) }
    var showResumeFeedback by remember(state.currentTrackId) { mutableStateOf(false) }
    LaunchedEffect(isPlaying, state.currentTrackId, artworkPlaybackTapEnabled) {
        val justResumed = isPlaying && !previousPlaying
        previousPlaying = isPlaying
        if (artworkPlaybackTapEnabled && justResumed) {
            showResumeFeedback = true
            delay(1_500)
            showResumeFeedback = false
        } else if (!artworkPlaybackTapEnabled || !isPlaying) {
            showResumeFeedback = false
        }
    }
    val isVideoFeed = state.isCurrentVideoItem()
    val artworkPaneContent: @Composable (Modifier, Dp) -> Unit = { modifier, artworkHeight ->
        Box(
            modifier.then(
                if (artworkPlaybackTapEnabled) {
                    Modifier
                        .localizedDescription(Res.string.playback_toggle)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            role = Role.Button,
                            onClickLabel = if (isPlaying) "Pause playback" else "Resume playback",
                        ) { actions.onUIEvent(UIEvent.PlayPause) }
                } else {
                    Modifier
                },
            ),
            contentAlignment = Alignment.Center,
        ) {
            // Keep one artwork/media renderer alive while the right pane changes views.
            artworkContent(artworkHeight)
            androidx.compose.animation.AnimatedVisibility(
                visible = artworkPlaybackTapEnabled && (!isPlaying || showResumeFeedback),
                enter = fadeIn(tween(120)),
                exit = fadeOut(tween(180)),
            ) {
                Box(
                    Modifier.size(56.dp).background(Color.Black.copy(alpha = 0.48f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (isPlaying) SimpIcons.PlayArrow else SimpIcons.Pause,
                        contentDescription = if (isPlaying) "Resumed playback" else "Playback paused",
                        tint = Color.White,
                        modifier = Modifier.size(34.dp),
                    )
                }
            }
        }
    }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        if (!isVideoFeed) {
            // Use the raw viewport so covers reach the screen edges, just like portrait.
            // Mask the complete renderer once, including animated cover handoffs, so the
            // existing sampled backdrop shows through without a seam or a second tint.
            artworkPaneContent(
                Modifier.align(Alignment.CenterStart)
                    .fillMaxWidth(0.5f)
                    .fillMaxHeight()
                    .appleMusicVerticalFadeEdges(
                        topFade = 0.dp,
                        bottomFade = maxHeight * 0.24f,
                        sideFade = maxWidth * 0.045f,
                        endSideFade = maxWidth * 0.12f,
                        smoothFade = true,
                    ),
                maxHeight,
            )
        }
        // Keep controls inside the camera and system-bar safe edges over the full-size cover.
        BoxWithConstraints(
            Modifier.fillMaxSize()
                .windowInsetsPadding(landscapeContentWindowInsets())
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Vertical))
                .padding(top = 16.dp, bottom = 4.dp),
        ) {
            val videoRatio = state.videoAspectRatio
                .takeIf { it.isFinite() && it > 0f }
                ?.coerceAtMost(16f / 10f)
                ?: (16f / 10f)
            val paneGap = 12.dp
            val availableArtworkHeight = (maxHeight - 60.dp).coerceAtLeast(0.dp)
            // Videos retain their bounded viewport and native aspect ratio.
            // Leave room for the lyrics and transport, including when the user enlarges text.
            val minimumControlsWidth = minOf(280.dp * LocalDensity.current.fontScale.coerceAtLeast(1f), maxWidth * 0.6f)
            val maximumArtworkPaneWidth = minOf(
                maxWidth * 0.5f,
                (maxWidth - paneGap - minimumControlsWidth).coerceAtLeast(0.dp),
                640.dp,
            )
            // Anchor the right pane to the standard video frame in both media modes.
            // Left artwork and dock sizing remain independent, including tall videos.
            val controlsPaneStart = minOf(maximumArtworkPaneWidth, availableArtworkHeight * (16f / 10f)) + paneGap
            val controlsPaneWidth = (maxWidth - controlsPaneStart).coerceAtLeast(0.dp)
            val artworkPaneWidth =
                if (isVideoFeed) {
                    minOf(
                        maximumArtworkPaneWidth,
                        availableArtworkHeight * videoRatio,
                    )
                } else {
                    // Mirrored cutout insets keep this split at the full screen's midpoint.
                    (maxWidth * 0.5f - paneGap * 0.5f).coerceAtLeast(0.dp)
                }
            val artworkHeight = artworkPaneWidth / videoRatio
            val compactText = typo().bodySmall
            val landscapeTypography = typography.copy(
                mainTitle = typo().titleMedium,
                mainArtist = typography.mainArtist.copy(fontSize = compactText.fontSize, lineHeight = compactText.lineHeight),
                times = typography.times.copy(fontSize = compactText.fontSize, lineHeight = compactText.lineHeight),
            )
            Box(Modifier.fillMaxSize()) {
                Column(
                    Modifier.width(artworkPaneWidth).fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = if (isVideoFeed) Alignment.Center else Alignment.CenterStart,
                    ) {
                        if (isVideoFeed) {
                            artworkPaneContent(
                                Modifier.size(width = artworkPaneWidth, height = artworkHeight)
                                    .clip(RoundedCornerShape(20.dp)),
                                artworkHeight,
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    // Match the bottom-aligned 56dp transport row so both dock icons sit level
                    // with previous/play/next, centered over the cover's lower fade.
                    AppleMusicDock(
                        viewState = viewState,
                        onSelectView = onSelectView,
                        activeColor = activePillContainer,
                        activeContentColor = activePillContent,
                        backdrop = backdrop,
                        modifier = Modifier.height(56.dp),
                    )
                }
                Column(
                    Modifier.align(Alignment.CenterEnd)
                        .width(controlsPaneWidth)
                        .fillMaxHeight(),
                ) {
                    // Use the same compact title/action row in every landscape view.
                    if (viewState != AppleMusicView.MAIN) {
                        Row(Modifier.fillMaxWidth().padding(horizontal = LandscapePlayerContentInset), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.weight(1f)) {
                                AppleMusicMainTitleRow(state, actions, landscapeTypography)
                            }
                            AppleMusicHeaderActions(state, actions, modifier = Modifier.offset(x = 12.dp), compact = true)
                        }
                    }
                    Crossfade(
                        targetState = viewState,
                        animationSpec = tween(220),
                        modifier = Modifier.weight(1f).fillMaxWidth().clip(RoundedCornerShape(12.dp)),
                        label = "landscapePlayerBody",
                    ) { view ->
                        when (view) {
                            AppleMusicView.MAIN -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                                // Keep the title, artist and actions together just above the progress bar.
                                Row(Modifier.fillMaxWidth().padding(horizontal = LandscapePlayerContentInset).padding(bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.weight(1f)) {
                                        AppleMusicMainTitleRow(state, actions, landscapeTypography)
                                    }
                                    AppleMusicHeaderActions(state, actions, modifier = Modifier.offset(x = 12.dp), compact = true)
                                }
                            }
                            AppleMusicView.LYRICS -> AppleMusicLyricsView(
                                state = state, actions = actions, typography = landscapeTypography,
                                viewState = view, onSelectView = onSelectView,
                                activePillContainer = activePillContainer, activePillContent = activePillContent,
                                backdrop = backdrop, deviceVolumeController = deviceVolumeController,
                                showCluster = showLyricsControls, onToggleCluster = onToggleLyricsControls,
                                sharedClusterHeight = 0.dp, transitionBlur = 0f, embedded = true,
                            )
                            AppleMusicView.QUEUE -> AppleMusicQueueView(
                                state = state, actions = actions, typography = landscapeTypography,
                                viewState = view, onSelectView = onSelectView,
                                activePillContainer = activePillContainer, activePillContent = activePillContent,
                                backdrop = backdrop, deviceVolumeController = deviceVolumeController,
                                sharedClusterHeight = 0.dp, embedded = true,
                            )
                        }
                    }
                    androidx.compose.animation.AnimatedVisibility(
                        visible = viewState != AppleMusicView.LYRICS || showLyricsControls,
                    ) {
                        AppleMusicPlaybackControls(
                            state = state, actions = actions, typography = landscapeTypography,
                            compact = true, filledPlayButton = true,
                            modifier = Modifier.padding(top = 2.dp, start = LandscapePlayerContentInset, end = LandscapePlayerContentInset),
                        )
                    }
                }
            }
        }
    }
}
