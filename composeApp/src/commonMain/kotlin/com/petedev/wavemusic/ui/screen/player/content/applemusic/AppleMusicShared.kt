package com.petedev.wavemusic.ui.screen.player.content.applemusic

import com.petedev.wavemusic.resources.more

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.indication
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.petedev.wavemusic.domain.mediaservice.handler.ControlState
import com.petedev.wavemusic.domain.mediaservice.handler.RepeatState
import com.petedev.wavemusic.Platform
import com.petedev.wavemusic.expect.ui.DeviceVolumeController
import com.petedev.wavemusic.expect.ui.PlatformBackdrop
import com.petedev.wavemusic.extension.formatDuration
import com.petedev.wavemusic.getPlatform
import com.petedev.wavemusic.ui.component.ExplicitBadge
import com.petedev.wavemusic.ui.component.AppleMusicTranslatedColor
import com.petedev.wavemusic.ui.component.heartBurst
import com.petedev.wavemusic.ui.component.rememberHeartBurstState
import com.petedev.wavemusic.ui.component.rememberHolderPainter
import com.petedev.wavemusic.ui.icon.FastForward
import com.petedev.wavemusic.ui.icon.FastRewind
import com.petedev.wavemusic.ui.icon.GraphicEq
import com.petedev.wavemusic.ui.icon.Lyrics
import com.petedev.wavemusic.ui.icon.MoreVert
import com.petedev.wavemusic.ui.icon.PlaylistAdd
import com.petedev.wavemusic.ui.icon.Pause
import com.petedev.wavemusic.ui.icon.PlayArrow
import com.petedev.wavemusic.ui.icon.QueueMusic
import com.petedev.wavemusic.ui.icon.Repeat
import com.petedev.wavemusic.ui.icon.RepeatOne
import com.petedev.wavemusic.ui.icon.Shuffle
import com.petedev.wavemusic.ui.icon.WaveIcons
import com.petedev.wavemusic.ui.icon.Star
import com.petedev.wavemusic.ui.icon.StarBorder
import com.petedev.wavemusic.ui.icon.VolumeDown
import com.petedev.wavemusic.ui.icon.VolumeUp
import com.petedev.wavemusic.ui.screen.player.content.NowPlayingContentActions
import com.petedev.wavemusic.ui.screen.player.content.NowPlayingContentState
import com.petedev.wavemusic.ui.screen.player.content.AppleMusicMainTitleRow
import com.petedev.wavemusic.ui.theme.typo
import com.petedev.wavemusic.viewModel.UIEvent
import org.jetbrains.compose.resources.stringResource
import com.petedev.wavemusic.resources.Res
import com.petedev.wavemusic.resources.add_to_a_playlist
import com.petedev.wavemusic.resources.crossfading
import kotlin.math.roundToLong

/** Which body the dock is currently showing. Held by the top-level Apple Music composable. */
internal enum class AppleMusicView { MAIN, LYRICS, QUEUE }

/**
 * The page gradient's colour at [fraction] of the screen height: the seed barely darkened at the
 * top, ~32%-darkened by mid-page (48%), ~78%-darkened at the bottom. Used to BUILD the gradient
 * and to land the artwork fade on the gradient's own colour at the exact Y where it ends.
 */
internal fun appleMusicGradientColorAt(
    seedColor: Color,
    fraction: Float,
): Color {
    // Every stop is the SEED darkened, never a fixed near-black: the old bottom stop was a
    // hardcoded warm black, so on the artwork view — where only the lower half of the gradient
    // is visible under the artwork — the page read as plain black instead of tinted.
    val top = lerp(seedColor, Color.Black, 0.05f)
    val mid = lerp(seedColor, Color.Black, 0.32f)
    val bottom = lerp(seedColor, Color.Black, 0.78f)
    return if (fraction <= 0.48f) {
        lerp(top, mid, (fraction / 0.48f).coerceIn(0f, 1f))
    } else {
        lerp(mid, bottom, ((fraction - 0.48f) / 0.52f).coerceIn(0f, 1f))
    }
}

internal val AppleMusicTextSecondary = Color.White.copy(alpha = 0.72f)
internal val AppleMusicPillInactive = Color.White.copy(alpha = 0.24f)
internal val AppleMusicTrackInactive = Color.White.copy(alpha = 0.26f)
internal val AppleMusicTrackActive = Color.White.copy(alpha = 0.92f)

@Immutable
internal data class AppleMusicTypography(
    val mainTitle: TextStyle,
    val mainArtist: TextStyle,
    val compactTitle: TextStyle,
    val compactArtist: TextStyle,
    val queueSectionHeader: TextStyle,
    val queueSectionSubtitle: TextStyle,
    val times: TextStyle,
    val badge: TextStyle,
    val footer: TextStyle,
    val idleLyric: TextStyle,
    val idleTranslated: TextStyle,
)

/**
 * Maps every Apple Music text slot 1:1 onto the [typo] roles the OTHER styles use for the same
 * element — no custom sizes, no custom scaling (owner's rule: this style types like the rest of
 * the app). Precedents: M3E's track title/artist row (titleMedium/bodyMedium), LyricsView's
 * in-player line (headlineMedium), SongFullWidthItems rows (titleSmall/bodySmall), the queue
 * sheet's section headers (titleMedium) and M3E's canvas overlay lines (bodyMedium white/cream).
 */
@Composable
internal fun rememberAppleMusicTypography(): AppleMusicTypography {
    val t = typo()
    return AppleMusicTypography(
        mainTitle = t.titleMedium,
        mainArtist = t.bodyMedium.copy(color = Color.White.copy(alpha = 0.86f)),
        // The compact header — Lyrics, Queue, and the row under a running canvas — carries the
        // SIZES FullscreenLyricsSheet uses for the same job (LyricsView.kt: labelSmall over
        // bodySmall), while mainTitle/mainArtist stay 18/13 because they head the controller
        // layout. Borrow the size from labelSmall but keep the titleMedium ROLE: typo() bakes a
        // color into every role — title* carry titleColor, body*/label* carry bodyColor — so
        // switching the title to labelSmall outright would also switch it to the subtitle's grey.
        // Keep the compact artist size, but use brighter secondary white for contrast over covers.
        compactTitle = t.titleMedium.copy(fontSize = t.labelSmall.fontSize),
        compactArtist = t.bodySmall.copy(color = Color.White.copy(alpha = 0.86f)),
        queueSectionHeader = t.titleMedium,
        queueSectionSubtitle = t.bodySmall.copy(color = Color.White.copy(alpha = 0.86f)),
        times = t.bodyMedium.copy(color = Color.White.copy(alpha = 0.86f)),
        badge = t.bodySmall,
        footer = t.bodySmall,
        idleLyric = t.bodyMedium.copy(color = Color.White),
        idleTranslated = t.bodyMedium.copy(color = AppleMusicTranslatedColor),
    )
}

/**
 * True alpha fade at the edges of a region (DstIn mask): content dissolves into whatever is
 * behind it without touching the wrapped component.
 */
internal fun Modifier.appleMusicVerticalFadeEdges(
    topFade: Dp,
    bottomFade: Dp,
    sideFade: Dp = 0.dp,
    endSideFade: Dp = sideFade,
    smoothFade: Boolean = false,
): Modifier =
    graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        .drawWithCache {
            val topPx = topFade.toPx().coerceAtMost(size.height / 2f)
            val bottomPx = bottomFade.toPx().coerceAtMost(size.height / 2f)
            val topStop = if (size.height > 0f) topPx / size.height else 0f
            val bottomStop = if (size.height > 0f) bottomPx / size.height else 0f
            val verticalMask = Brush.verticalGradient(*appleMusicFadeStops(topStop, bottomStop, smoothFade))
            val sidePx = sideFade.toPx().coerceAtMost(size.width / 2f)
            val endSidePx = endSideFade.toPx().coerceAtMost(size.width / 2f)
            val horizontalMask = if ((sidePx > 0f || endSidePx > 0f) && size.width > 0f) {
                Brush.horizontalGradient(*appleMusicFadeStops(sidePx / size.width, endSidePx / size.width, smoothFade))
            } else {
                null
            }
            onDrawWithContent {
                drawContent()
                drawRect(verticalMask, blendMode = BlendMode.DstIn)
                horizontalMask?.let { drawRect(it, blendMode = BlendMode.DstIn) }
            }
        }

/**
 * Press-to-swell ("phồng to ra") like the liquid-glass buttons: the control springs up while a
 * finger is on it and settles back on release.
 *
 * Detection is a plain awaitEachGesture down/up watch, NOT GlassInteraction's drag inspector —
 * that one only starts its animation from a DRAG start, so a normal tap on these buttons never
 * animated anything. `requireUnconsumed = false` keeps it working under the wrapped `clickable`,
 * and nothing here consumes events, so clicks and slider drags still land.
 */
@Composable
internal fun Modifier.appleMusicPressInflate(
    pressedScale: Float = 1.35f,
    dampingRatio: Float = 0.45f,
): Modifier {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = spring(dampingRatio = dampingRatio, stiffness = 380f),
        label = "appleMusicPressInflate",
    )
    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }.pointerInput(Unit) {
            awaitEachGesture {
                awaitFirstDown(requireUnconsumed = false)
                pressed = true
                waitForUpOrCancellation()
                pressed = false
            }
        }
}

/**
 * One 25dp glyph button with a tight circular ripple — [IconButton] sized and clipped exactly
 * like Classic/M3E's plain icon buttons (Info, PlaylistAdd, Queue, Replay5…), not a custom
 * Box+clickable and not an unconstrained [com.petedev.wavemusic.ui.component.RippleIconButton].
 */
@Composable
internal fun AppleMusicGlyphButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    hitTargetSize: Dp = size,
    tint: Color = Color.White,
    contentDescription: String = "",
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.appleMusicPressInflate().size(hitTargetSize).clip(CircleShape),
    ) {
        Icon(imageVector = icon, contentDescription = contentDescription, tint = tint, modifier = Modifier.size(size))
    }
}

/** Playlist, ☆ (favourite with the heart-burst), and ⋯ actions shared by player headers. */
@Composable
internal fun AppleMusicHeaderActions(
    state: NowPlayingContentState,
    actions: NowPlayingContentActions,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    showPlaylistAction: Boolean = true,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        if (showPlaylistAction) {
            AppleMusicGlyphButton(
                icon = WaveIcons.PlaylistAdd,
                onClick = { actions.onShowAddToPlaylist() },
                size = if (compact) 26.dp else 28.dp,
                hitTargetSize = if (compact) 48.dp else 52.dp,
                contentDescription = stringResource(Res.string.add_to_a_playlist),
            )
        }
        val likeBurst = rememberHeartBurstState()
        val likeInteraction = remember { MutableInteractionSource() }
        Box(
            modifier =
                Modifier
                    .appleMusicPressInflate()
                    .size(if (compact) 48.dp else 52.dp)
                    .clickable(
                        interactionSource = likeInteraction,
                        indication = null,
                    ) {
                        if (!state.controllerState.isLiked) likeBurst.fire()
                        actions.onUIEvent(UIEvent.ToggleLike)
                    },
            contentAlignment = Alignment.Center,
        ) {
            // Clip only the press surface so the favorite burst can still spill outside it.
            Box(
                Modifier.matchParentSize()
                    .clip(CircleShape)
                    .indication(likeInteraction, LocalIndication.current),
            )
            Box(modifier = Modifier.size(if (compact) 28.dp else 30.dp).heartBurst(likeBurst), contentAlignment = Alignment.Center) {
                Crossfade(targetState = state.controllerState.isLiked, label = "appleMusicFavorite") { liked ->
                    Icon(
                        imageVector = if (liked) WaveIcons.Star else WaveIcons.StarBorder,
                        contentDescription = "",
                        tint = Color.White,
                        modifier = Modifier.size(if (compact) 28.dp else 30.dp),
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .appleMusicPressInflate()
                .size(if (compact) 48.dp else 50.dp)
                .clip(CircleShape)
                .clickable { actions.onShowMoreSheet() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = WaveIcons.MoreVert,
                contentDescription = stringResource(Res.string.more),
                tint = Color.White,
                modifier = Modifier.size(24.dp).offset(y = 1.dp),
            )
        }
    }
}

/**
 * 56dp rounded thumbnail + title/artist column + [AppleMusicHeaderActions], used where the
 * artwork isn't on screen (LYRICS and QUEUE bodies).
 */
@Composable
internal fun AppleMusicCompactHeader(
    state: NowPlayingContentState,
    actions: NowPlayingContentActions,
    typography: AppleMusicTypography,
    artworkInteraction: Modifier = Modifier,
    trackInfoInteraction: Modifier = Modifier,
    artworkOverlay: (@Composable () -> Unit)? = null,
    prominentTrackInfo: Boolean = false,
    actionsVerticalOffset: Dp = (-10).dp,
    showPlaylistAction: Boolean = true,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(55.dp).clip(RoundedCornerShape(4.dp)).then(artworkInteraction),
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                model =
                    ImageRequest
                        .Builder(LocalPlatformContext.current)
                        .data(state.screenData.thumbnailURL)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .diskCacheKey(state.screenData.thumbnailURL)
                        .crossfade(300)
                        .build(),
                placeholder = rememberHolderPainter(),
                error = rememberHolderPainter(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
            )
            artworkOverlay?.invoke()
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f).then(trackInfoInteraction)) {
            // Lyrics and Queue use the same prominent marquee title as the cover row so the
            // shared header keeps identical typography and alignment across both pages.
            Text(
                text = state.screenData.nowPlayingTitle,
                style = if (prominentTrackInfo) typography.mainTitle else typography.compactTitle,
                maxLines = 1,
                overflow = if (prominentTrackInfo) TextOverflow.Clip else TextOverflow.Ellipsis,
                modifier =
                    if (prominentTrackInfo) {
                        Modifier.fillMaxWidth().basicMarquee(iterations = Int.MAX_VALUE).focusable()
                    } else {
                        Modifier.fillMaxWidth()
                    },
            )
            Spacer(modifier = Modifier.height(if (prominentTrackInfo) 4.dp else 2.dp))
            CompactHeaderArtists(
                state = state,
                actions = actions,
                style = if (prominentTrackInfo) typography.mainArtist else typography.compactArtist,
                prominent = prominentTrackInfo,
            )
        }
        AppleMusicHeaderActions(
            state = state,
            actions = actions,
            showPlaylistAction = showPlaylistAction,
            // Match the cover row's lifted vertical placement and bring the visible dots in line
            // with the progress bar's right endpoint, accounting for the hit target's inset.
            modifier = Modifier.offset(x = 15.dp, y = actionsVerticalOffset),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CompactHeaderArtists(
    state: NowPlayingContentState,
    actions: NowPlayingContentActions,
    style: TextStyle,
    prominent: Boolean,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (state.screenData.isExplicit) {
            ExplicitBadge(modifier = Modifier.size(20.dp).padding(end = 4.dp))
        }
        if (prominent && state.artistLinks.isNotEmpty()) {
            FlowRow(maxLines = 1) {
                state.artistLinks.forEachIndexed { index, (name, channelId) ->
                    if (index > 0) Text(text = ", ", style = style)
                    Text(
                        text = name,
                        style = style,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.clickable { actions.onNavigateToArtistId(channelId) },
                    )
                }
            }
        } else {
            Text(
                text = state.screenData.artistName,
                style = style,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.clickable { actions.onNavigateToArtist() },
            )
        }
    }
}

/** 7dp track shared by the progress bar and the volume row — same visual language, only the color and callbacks differ. */
@Composable
internal fun AppleMusicThinSlider(
    value: Float,
    activeColor: Color,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    onValueChangeFinished: (() -> Unit)? = null,
    enabled: Boolean = true,
    compact: Boolean = false,
) {
    // The slider's "phồng" is a track that thickens while touched/dragged — same spring feel as
    // the button inflate, expressed the way a bar can.
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val dragged by interactionSource.collectIsDraggedAsState()
    val trackHeight by animateDpAsState(
        targetValue =
            if (pressed || dragged) {
                if (compact) 12.dp else 14.dp
            } else {
                if (compact) 6.dp else 7.dp
            },
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 300f),
        label = "appleMusicSliderInflate",
    )
    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
        Slider(
            value = value,
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished,
            modifier = modifier,
            interactionSource = interactionSource,
            enabled = enabled,
            track = {
                // Hand-drawn instead of SliderDefaults.Track. M3 rounds each half of the track with
                // TWO different radii: the OUTER end with trackCornerSize (height/2 — fully round)
                // and the INNER end, where the two halves meet, with trackInsideCornerSize (2dp —
                // reads as square). At value 0 the inactive half owns the entire bar, so its square
                // inner end lands on the LEFT while its round outer end sits on the right — the
                // mismatched bar. Clipping ONE container and drawing the played portion inside it
                // keeps both ends of the bar equally round, and gives the played portion the flat
                // right edge Apple's own bar has.
                val fraction = value.coerceIn(0f, 1f)
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(trackHeight)
                            .clip(RoundedCornerShape(percent = 50))
                            .background(AppleMusicTrackInactive),
                ) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(fraction)
                                .background(activeColor),
                    )
                }
            },
            thumb = {
                // No thumb — the approved mock and Apple's own bars are track-only; the
                // active/inactive split marks the position (same call the MiniPlayer makes).
                Spacer(Modifier.size(0.dp))
            },
        )
    }
}

/** Elapsed and total time, with a crossfade label in the center when active. */
@Composable
internal fun AppleMusicTimesRow(
    state: NowPlayingContentState,
    typography: AppleMusicTypography,
    modifier: Modifier = Modifier,
) {
    // `total` stays -1 until the player reports a duration, and SimpleMediaState.Ready carries
    // ONLY the duration — never a position. After a queue restore nothing is playing, and the
    // position poll only runs while isPlaying, so no later event arrives to correct it. Deriving
    // elapsed from total therefore zeroed BOTH numbers at once, which is why a restored queue read
    // 00:00 / -00:00: the played time was never actually unknown, TimeLine.current held it.
    val knownTotal = state.timelineState.total.takeIf { it > 0L }
    val elapsedMs =
        if (knownTotal != null) {
            // Still derived from the slider while the duration IS known, so this number tracks the
            // finger while scrubbing instead of waiting for the player to report the seek back.
            (knownTotal * (state.sliderValue / 100f)).roundToLong()
        } else {
            state.timelineState.current.coerceAtLeast(0L)
        }
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = formatDuration(elapsedMs),
            style = typography.times,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Left,
        )
        // Reserve the center slot for the crossfade label so the time row doesn't resize when
        // playback switches between normal and crossfaded audio.
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            // Sweep head for the "Crossfading" shimmer, 0..1. Runs UNCONDITIONALLY — put behind the
            // crossfade check it would restart from zero every time the label appears, which is the
            // same reason the other two styles declare it outside their own visibility gate.
            val sweepTransition = rememberInfiniteTransition(label = "appleMusicCrossfadeSweep")
            val crossfadeSweep by sweepTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec =
                    infiniteRepeatable(
                        animation = tween(3200, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart,
                    ),
                label = "appleMusicSweepHead",
            )
            val crossfadeLabelAlpha by animateFloatAsState(
                targetValue = if (state.timelineState.isCrossfading) 1f else 0f,
                label = "appleMusicCrossfadeLabelAlpha",
            )
            Box(modifier = Modifier.alpha(crossfadeLabelAlpha)) {
                // Identical treatment to Classic and M3 Expressive: a highlight swept through the
                // glyphs with a text brush — no overlay, no clipping.
                val shimmerSpan = 140f
                val shimmerHead = crossfadeSweep * (shimmerSpan * 3f) - shimmerSpan
                val labelColor = typography.times.color
                Text(
                    text = stringResource(Res.string.crossfading),
                    style =
                        typography.times.copy(
                            brush =
                                Brush.horizontalGradient(
                                    0f to labelColor.copy(alpha = 0.45f),
                                    // The sweep head is PURE white, not the resting label colour — that
                                    // colour is an adaptive grey, and a grey gleam reads as no gleam.
                                    0.5f to Color.White,
                                    1f to labelColor.copy(alpha = 0.45f),
                                    startX = shimmerHead,
                                    endX = shimmerHead + shimmerSpan,
                                    tileMode = TileMode.Clamp,
                                ),
                        ),
                    textAlign = TextAlign.Center,
                )
            }
        }
        Text(
            text = formatDuration(knownTotal ?: -1L),
            style = typography.times,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Right,
        )
    }
}

/** FastRewind(44dp) → Previous, Play/Pause(62dp, plain white — no container disc), FastForward(44dp) → Next. */
@Composable
internal fun AppleMusicTransportRow(
    controllerState: ControlState,
    onUIEvent: (UIEvent) -> Unit,
    modifier: Modifier = Modifier,
    // Only the fullscreen lyrics page asks for these; the Now Playing page keeps its three buttons.
    showShuffleAndRepeat: Boolean = false,
    compact: Boolean = false,
    filledPlayButton: Boolean = false,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        // Mock: a tight centered cluster with a 58dp gap — NOT SpaceEvenly, which spreads the
        // rewind/forward glyphs to the screen edges (first device screenshots). With shuffle and
        // repeat on the two ends the five span the row instead, the way Apple's desktop player
        // lays them out — five buttons at a 58dp gap no longer fit beside the artwork.
        horizontalArrangement =
            if (showShuffleAndRepeat || compact) {
                Arrangement.SpaceBetween
            } else {
                Arrangement.spacedBy(58.dp, Alignment.CenterHorizontally)
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showShuffleAndRepeat) {
            AppleMusicGlyphButton(
                icon = WaveIcons.Shuffle,
                onClick = { onUIEvent(UIEvent.Shuffle) },
                size = 40.dp,
                tint = Color.White.copy(alpha = if (controllerState.isShuffle) 1f else 0.4f),
            )
        }
        IconButton(
            onClick = { if (controllerState.isPreviousAvailable) onUIEvent(UIEvent.Previous) },
            modifier = Modifier.appleMusicPressInflate().size(if (compact) 48.dp else 56.dp).clip(CircleShape),
        ) {
            Icon(
                imageVector = WaveIcons.FastRewind,
                contentDescription = "",
                tint = Color.White.copy(alpha = if (controllerState.isPreviousAvailable) 1f else 0.4f),
                modifier = Modifier.size(if (compact) 32.dp else 46.dp),
            )
        }
        // No loading state on this button. It is play/pause and nothing else: it stays pressable
        // and keeps showing the transport glyph even while the player is buffering, so the control
        // never disappears out from under a finger reaching for it.
        Box(
            modifier =
                Modifier
                    .appleMusicPressInflate()
                    .size(if (compact) 56.dp else 76.dp)
                    .clip(CircleShape)
                    .then(if (filledPlayButton) Modifier.background(Color.White) else Modifier)
                    .clickable { onUIEvent(UIEvent.PlayPause) },
            contentAlignment = Alignment.Center,
        ) {
            Crossfade(targetState = controllerState.isPlaying, label = "appleMusicPlayPauseIcon") { isPlaying ->
                Icon(
                    imageVector = if (isPlaying) WaveIcons.Pause else WaveIcons.PlayArrow,
                    contentDescription = "",
                    tint = if (filledPlayButton) Color.Black else Color.White,
                    modifier = Modifier.size(if (compact) 36.dp else 66.dp),
                )
            }
        }
        IconButton(
            onClick = { if (controllerState.isNextAvailable) onUIEvent(UIEvent.Next) },
            modifier = Modifier.appleMusicPressInflate().size(if (compact) 48.dp else 56.dp).clip(CircleShape),
        ) {
            Icon(
                imageVector = WaveIcons.FastForward,
                contentDescription = "",
                tint = Color.White.copy(alpha = if (controllerState.isNextAvailable) 1f else 0.4f),
                modifier = Modifier.size(if (compact) 32.dp else 46.dp),
            )
        }
        if (showShuffleAndRepeat) {
            val repeatState = controllerState.repeatState
            AppleMusicGlyphButton(
                icon = if (repeatState is RepeatState.One) WaveIcons.RepeatOne else WaveIcons.Repeat,
                onClick = { onUIEvent(UIEvent.Repeat) },
                size = 40.dp,
                tint = Color.White.copy(alpha = if (repeatState !is RepeatState.None) 1f else 0.4f),
            )
        }
    }
}

@Composable
internal fun AppleMusicVolumeRow(
    controller: DeviceVolumeController,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = WaveIcons.VolumeDown,
            contentDescription = "",
            tint = AppleMusicTextSecondary,
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Box(modifier = Modifier.weight(1f).height(18.dp), contentAlignment = Alignment.Center) {
            AppleMusicThinSlider(
                value = controller.volumeFraction,
                activeColor = AppleMusicTrackActive,
                onValueChange = { controller.setVolumeFraction(it) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Icon(
            imageVector = WaveIcons.VolumeUp,
            contentDescription = "",
            tint = AppleMusicTextSecondary,
            modifier = Modifier.size(18.dp),
        )
    }
}

/** One 44dp dock button: a light circle behind a DARK glyph while [active] (white-on-light was unreadable). */
@Composable
internal fun AppleMusicDockButton(
    icon: ImageVector,
    active: Boolean,
    activeColor: Color,
    activeContentColor: Color,
    backdrop: PlatformBackdrop,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val activeSurfaceModifier = Modifier.background(if (active && enabled) activeColor else Color.Transparent)
    Box(
        modifier =
            modifier
                .then(if (enabled) Modifier.appleMusicPressInflate() else Modifier)
                .size(40.dp)
                .clip(CircleShape)
                .then(activeSurfaceModifier)
                .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "",
            tint =
                when {
                    !enabled -> Color.White.copy(alpha = 0.38f)
                    !active -> Color.White.copy(alpha = 0.85f)
                    else -> activeContentColor
                },
            modifier = Modifier.size(22.dp),
        )
    }
}

/**
 * Lyrics and Queue controls, centered as a pair.
 */
@Composable
internal fun AppleMusicDock(
    viewState: AppleMusicView,
    onSelectView: (AppleMusicView) -> Unit,
    activeColor: Color,
    activeContentColor: Color,
    backdrop: PlatformBackdrop,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    lyricsEnabled: Boolean = true,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(52.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Re-tapping the active tab returns to MAIN — the dock is a toggle, not one-way nav.
        AppleMusicDockButton(
            icon = WaveIcons.Lyrics,
            active = viewState == AppleMusicView.LYRICS,
            activeColor = activeColor,
            activeContentColor = activeContentColor,
            backdrop = backdrop,
            // Keep the control stable while lyrics for a newly selected song are loading. The
            // lyrics page owns its empty/loading state; gating this button on transient null data
            // made its icon flash on every track handoff.
            enabled = enabled && lyricsEnabled,
            onClick = {
                if (enabled && lyricsEnabled) {
                    onSelectView(if (viewState == AppleMusicView.LYRICS) AppleMusicView.MAIN else AppleMusicView.LYRICS)
                }
            },
        )
        AppleMusicDockButton(
            icon = WaveIcons.QueueMusic,
            active = viewState == AppleMusicView.QUEUE,
            activeColor = activeColor,
            activeContentColor = activeContentColor,
            backdrop = backdrop,
            enabled = enabled,
            onClick = {
                onSelectView(if (viewState == AppleMusicView.QUEUE) AppleMusicView.MAIN else AppleMusicView.QUEUE)
            },
        )
    }
}

/**
 * Progress bar + times + transport. The regular Apple Music player can draw this in a fixed slot
 * above MAIN/LYRICS so one instance stays alive while the body crossfades; fullscreen lyrics uses
 * the natural-height form.
 */
@Composable
internal fun AppleMusicPlaybackControls(
    state: NowPlayingContentState,
    actions: NowPlayingContentActions,
    typography: AppleMusicTypography,
    showShuffleAndRepeat: Boolean = false,
    modifier: Modifier = Modifier,
    fixedSlot: Boolean = false,
    topActionContent: (@Composable () -> Unit)? = null,
    compact: Boolean = false,
    filledPlayButton: Boolean = false,
) {
    // A fixed slot lets MAIN and LYRICS reserve identical geometry while the parent draws one
    // persistent instance above their crossfade. Bottom alignment keeps the controls at the
    // same vertical position as before this shared layer was introduced.
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .then(if (fixedSlot) Modifier.height(APPLE_MUSIC_PLAYBACK_CONTROLS_SLOT) else Modifier),
        verticalArrangement = if (fixedSlot) Arrangement.Bottom else Arrangement.Top,
    ) {
        topActionContent?.invoke()
        // Fixed 18dp shell: the track swells on touch, but inside a CONSTANT footprint —
        // otherwise the growing slider re-measures this whole column and the artwork above
        // it visibly jumps. It also gives the bar a real 18dp touch target instead of 7dp.
        Box(modifier = Modifier.fillMaxWidth().height(18.dp), contentAlignment = Alignment.Center) {
            AppleMusicThinSlider(
                value = state.sliderValue / 100f,
                activeColor = if (state.timelineState.isCrossfading) state.sliderTrackColor else AppleMusicTrackActive,
                onValueChange = { actions.onSliderChange(it * 100f) },
                onValueChangeFinished = actions.onSliderChangeFinished,
                enabled = state.timelineState.canSeek,
                compact = compact,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        AppleMusicTimesRow(state = state, typography = typography, modifier = Modifier.padding(top = if (compact) 0.dp else 8.dp))
        Spacer(modifier = Modifier.height(if (compact) 4.dp else 12.dp))
        AppleMusicTransportRow(
            controllerState = state.controllerState,
            onUIEvent = actions.onUIEvent,
            showShuffleAndRepeat = showShuffleAndRepeat,
            compact = compact,
            filledPlayButton = filledPlayButton,
        )
    }
}

internal val APPLE_MUSIC_PLAYBACK_CONTROLS_SLOT = 250.dp

// In Lyrics mode the shared controls slot has no song-title/action row, leaving this much unused
// headroom above the progress bar. Let the lyrics viewport extend through that headroom.
internal val APPLE_MUSIC_LYRICS_CONTENT_EXTENSION = 120.dp

/**
 * Progress bar + times + transport + dock — the fixed block every Apple Music body
 * (MAIN, LYRICS, QUEUE) reserves at the bottom. MAIN/LYRICS draw the playback controls once in the
 * parent when [drawPlaybackControls] is false. On Desktop only the dock renders.
 */
@Composable
internal fun AppleMusicBottomCluster(
    state: NowPlayingContentState,
    actions: NowPlayingContentActions,
    typography: AppleMusicTypography,
    viewState: AppleMusicView,
    onSelectView: (AppleMusicView) -> Unit,
    activePillContainer: Color,
    activePillContent: Color,
    backdrop: PlatformBackdrop,
    deviceVolumeController: DeviceVolumeController?,
    modifier: Modifier = Modifier,
    drawPlaybackControls: Boolean = true,
    playbackControlsAlpha: Float = 1f,
    dockAlpha: Float = 1f,
) {
    val localDensity = LocalDensity.current
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 8.dp)) {
        if (getPlatform() == Platform.Android) {
            if (drawPlaybackControls && playbackControlsAlpha > 0f) {
                AppleMusicPlaybackControls(
                    state = state,
                    actions = actions,
                    typography = typography,
                    fixedSlot = true,
                    modifier = Modifier.alpha(playbackControlsAlpha),
                    topActionContent = if (viewState == AppleMusicView.MAIN) {
                        {
                            Row(
                                // Cover-only positioning: the lyrics/queue compact header is a
                                // separate composable and keeps its existing vertical alignment.
                                modifier = Modifier.fillMaxWidth().offset(y = (-10).dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(Modifier.weight(1f)) {
                                    AppleMusicMainTitleRow(state = state, actions = actions, typography = typography)
                                }
                                AppleMusicHeaderActions(
                                    state = state,
                                    actions = actions,
                                    modifier = Modifier.offset(x = 12.dp),
                                )
                            }
                        }
                    } else {
                        null
                    },
                )
            } else {
                Spacer(modifier = Modifier.height(APPLE_MUSIC_PLAYBACK_CONTROLS_SLOT))
            }
            Spacer(modifier = Modifier.height(14.dp))
        } else {
            Spacer(modifier = Modifier.height(16.dp))
        }
        Box(modifier = Modifier.alpha(dockAlpha)) {
            AppleMusicDock(
                viewState = viewState,
                onSelectView = onSelectView,
                activeColor = activePillContainer,
                activeContentColor = activePillContent,
                backdrop = backdrop,
                enabled = dockAlpha > 0.99f,
                lyricsEnabled = state.lyricsEnabled,
            )
        }
        Spacer(
            modifier =
                Modifier.height(
                    // Breathing room under the dock: the bare inset parked the icons right on the
                    // gesture bar.
                    with(localDensity) { WindowInsets.systemBars.getBottom(localDensity).toDp() } + 12.dp,
                ),
        )
    }
}
