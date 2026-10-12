package com.petedev.wavemusic.ui.screen.player.content.applemusic

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.rememberCombinedBackdrop
import com.kyant.backdrop.highlight.Highlight
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import com.petedev.wavemusic.domain.manager.DataStoreManager
import com.petedev.wavemusic.expect.ui.DeviceVolumeController
import com.petedev.wavemusic.expect.ui.PlatformBackdrop
import com.petedev.wavemusic.expect.ui.isLyricsBlurSupported
import com.petedev.wavemusic.expect.ui.layerBackdrop
import com.petedev.wavemusic.expect.ui.rememberBackdrop
import com.petedev.wavemusic.ui.component.AppleMusicLyricPaddingX
import com.petedev.wavemusic.ui.component.LyricsView
import com.petedev.wavemusic.ui.component.liquidGlass
import com.petedev.wavemusic.ui.icon.Error
import com.petedev.wavemusic.ui.icon.FullscreenExit
import com.petedev.wavemusic.ui.icon.OpenInFull
import com.petedev.wavemusic.ui.icon.WaveIcons
import com.petedev.wavemusic.ui.icon.Translate
import com.petedev.wavemusic.ui.screen.player.content.NowPlayingContentActions
import com.petedev.wavemusic.ui.screen.player.content.NowPlayingContentState
import com.petedev.wavemusic.viewModel.LyricsProvider
import com.petedev.wavemusic.viewModel.NowPlayingScreenData
import com.petedev.wavemusic.viewModel.UIEvent
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import com.petedev.wavemusic.resources.Res
import com.petedev.wavemusic.resources.ai_translated
import com.petedev.wavemusic.resources.line_synced
import com.petedev.wavemusic.resources.lyrics_provider_betterlyrics
import com.petedev.wavemusic.resources.lyrics_provider_portato
import com.petedev.wavemusic.resources.lyrics_provider_youlyplus
import com.petedev.wavemusic.resources.lyrics_provider_kugou
import com.petedev.wavemusic.resources.lyrics_provider_unison
import com.petedev.wavemusic.resources.lyrics_provider_lrc
import com.petedev.wavemusic.resources.lyrics_provider_simpmusic
import com.petedev.wavemusic.resources.lyrics_provider_youtube
import com.petedev.wavemusic.resources.loading_lyrics
import com.petedev.wavemusic.resources.no_lyrics_found
import com.petedev.wavemusic.resources.offline_mode
import com.petedev.wavemusic.resources.rich_synced
import com.petedev.wavemusic.resources.spotify_lyrics_provider
import com.petedev.wavemusic.resources.unsynced

/**
 * The LYRICS body: compact header, the app's own [LyricsView] — the SAME renderer the other
 * styles' lyrics card embeds (owner's rule: reuse the existing component, never rebuild it) —
 * two persistent floating action buttons, the sync-type and provider caption lines, and the shared bottom cluster.
 * Loading and missing lyrics are reported in place so a slow provider never ejects the user from this page.
 */
@Composable
internal fun AppleMusicLyricsView(
    state: NowPlayingContentState,
    actions: NowPlayingContentActions,
    typography: AppleMusicTypography,
    viewState: AppleMusicView,
    onSelectView: (AppleMusicView) -> Unit,
    activePillContainer: Color,
    activePillContent: Color,
    backdrop: PlatformBackdrop,
    deviceVolumeController: DeviceVolumeController?,
    showCluster: Boolean,
    onToggleCluster: () -> Unit,
    sharedClusterHeight: androidx.compose.ui.unit.Dp,
    transitionBlur: Float,
    modifier: Modifier = Modifier,
    embedded: Boolean = false,
    dataStoreManager: DataStoreManager = koinInject(),
) {
    val localDensity = LocalDensity.current
    // The navbar samples the full page behind it. Combine the player's artwork source with a
    // transparent source that records the moving lyric lines, so these controls frost both layers.
    val scrollingLyricsBackdrop = rememberBackdrop(Color.Transparent)
    val glassBackdrop = rememberCombinedBackdrop(backdrop, scrollingLyricsBackdrop)
    val navigationBarInset = with(localDensity) { WindowInsets.navigationBars.getBottom(localDensity).toDp() }
    val fullscreenActionBottomPadding = if (embedded) 4.dp else navigationBarInset + 24.dp
    val seekTrackHorizontalInset = if (embedded) LandscapePlayerContentInset else 16.dp
    val lyricHorizontalGutter = if (embedded) LandscapePlayerContentInset else AppleMusicLyricPaddingX
    // The 18dp glyph sits inside a 32dp action target, so leave just enough trailing room for
    // its visible edge to line up with the progress track's end in either orientation.
    val actionGlyphInset = (32.dp - 18.dp) / 2
    val actionRowEndPadding = (seekTrackHorizontalInset - actionGlyphInset).coerceAtLeast(0.dp)
    val lyricsData = state.screenData.lyricsData

    // LyricsView applies AppleMusicLyricPaddingX ITSELF, but only on its Apple Music branch — its
    // Classic branch renders LyricsLineItem, which has no horizontal padding of its own. So this
    // tab, which contributes no gutter, left Classic lyrics flush against the screen edge while
    // the header right above them sits at 20dp. Same split the fullscreen sheet already does:
    // the caller contributes whatever the renderer does not, so the TOTAL is the same either way.
    val lyricsStyle by dataStoreManager.lyricsStyle.collectAsStateWithLifecycle(DataStoreManager.LYRICS_STYLE_CLASSIC)
    val rendererOwnsGutter = lyricsStyle == DataStoreManager.LYRICS_STYLE_APPLE_MUSIC && isLyricsBlurSupported()
    val currentLyricsTrackId = state.currentTrackId ?: state.screenData.currentVideoId
    val lyricsMatchCurrentTrack = currentLyricsTrackId == state.screenData.lyricsVideoId
    val currentTrackLyrics = lyricsData?.takeIf { lyricsMatchCurrentTrack }
    val incomingLyrics = currentTrackLyrics?.takeIf { data ->
        !data.lyrics.error && !data.lyrics.lines.isNullOrEmpty()
    }
    val hasTranslationForCurrentTrack = currentTrackLyrics?.translatedLyrics != null
    val showTranslationButton = state.showLyricsTranslationButton && state.canTranslateLyrics
    val trackKey =
        currentLyricsTrackId?.takeIf { it.isNotBlank() }
            ?: "${state.screenData.thumbnailURL}:${state.screenData.nowPlayingTitle}"
    val translationEnabled = state.translationEnabled
    val incomingVisibleLyricsData =
        remember(incomingLyrics, translationEnabled, state.translationLoading) {
            incomingLyrics?.let { data ->
                if (translationEnabled && !state.translationLoading) data else data.copy(translatedLyrics = null)
            }
        }

    // A track can be published a frame before its lyrics request begins. Keep the loading state
    // visible briefly across that handoff instead of flashing "No lyrics found" between tracks.
    var lyricsResultGraceElapsed by remember(trackKey) { mutableStateOf(false) }
    LaunchedEffect(trackKey) {
        lyricsResultGraceElapsed = false
        delay(LYRICS_EMPTY_STATE_GRACE_MS)
        lyricsResultGraceElapsed = true
    }
    val showLyricsLoading = state.screenData.lyricsLoading || !lyricsResultGraceElapsed

    var outgoingLyrics by remember { mutableStateOf<NowPlayingScreenData.LyricsData?>(null) }
    var outgoingTrackKey by remember { mutableStateOf<String?>(null) }
    val outgoingTimeline = remember { MutableStateFlow(state.timelineState) }
    SideEffect {
        if (incomingLyrics != null) {
            outgoingLyrics = incomingVisibleLyricsData ?: incomingLyrics
            outgoingTrackKey = trackKey
            outgoingTimeline.value = state.timelineState
        }
    }
    // Bridge the brief cache/identity handoff with the last rendered set. Replace
    // it directly when the new set arrives; a genuinely slow/missing lookup still
    // reaches its loading/empty state after the grace period. Old lines cannot seek.
    val renderableLyrics = incomingLyrics ?: outgoingLyrics.takeIf {
        (!lyricsResultGraceElapsed && outgoingTrackKey != trackKey) ||
            state.screenData.currentVideoId != currentLyricsTrackId
    }
    val visibleLyricsData = if (incomingLyrics != null) incomingVisibleLyricsData else renderableLyrics
    val displayedTrackKey = if (incomingLyrics != null) trackKey else outgoingTrackKey

    // Apple hands the whole page to the lyrics once you stop touching it, and brings the shared
    // transport back the moment you touch it again. Visibility is hoisted so the one transport
    // instance remains stable across the MAIN/LYRICS body transition.
    var showTranslationError by remember { mutableStateOf(false) }
    val floatingActionsBottomPadding by animateDpAsState(
        targetValue = if (showCluster) 0.dp else fullscreenActionBottomPadding,
        animationSpec = tween(durationMillis = 360, easing = FastOutSlowInEasing),
        label = "floatingActionsBottomPadding",
    )
    // Animate the reserved controls area as a single measured spacer. AnimatedVisibility's
    // expand/shrink transition was changing the lyrics LazyColumn's weighted constraints while
    // it was still attached to the same page; reversing that transition (fullscreen → controls)
    // could leave the player with an invalid measurement on some Android devices.
    val clusterSpacerHeight by animateDpAsState(
        targetValue = if (showCluster) sharedClusterHeight else 0.dp,
        animationSpec = tween(durationMillis = 360, easing = FastOutSlowInEasing),
        label = "lyricsClusterSpacerHeight",
    )
    // Keep lyrics scrolling inside the full-screen reading state. Tapping the blank page area or
    // the persistent fullscreen button brings the transport cluster back.

    LaunchedEffect(state.translationError) {
        showTranslationError = state.translationError != null
        if (state.translationError != null) {
            delay(3_000)
            showTranslationError = false
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
        // The compact header is shared by NowPlayingContentAppleMusic across Lyrics and Queue.
        // Reserve its original 55dp image + 24dp vertical padding so the body doesn't jump.
        Spacer(Modifier.height(79.dp))
        }

        Box(
            modifier =
                Modifier
                    .weight(1f)
                    // The lyric LINES consume their own taps to seek, so this only ever fires on
                    // the gutter and the gaps between lines — which is exactly the "tap the page,
                    // not a lyric" gesture. No ripple: this is a whole page, not a button.
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                    ) {
                        onToggleCluster()
                    },
        ) {
            if (renderableLyrics != null) {
                // Transparent background: the page's artwork-tinted gradient is the backdrop,
                // not LyricsView's own dark card color.
                // The app's own renderer, with its own line sizes/animations. The provider caption
                // rides as its LAST list item so it scrolls with the lyrics, per the owner's
                // original spec.
                LyricsView(
                    lyricsData = visibleLyricsData ?: renderableLyrics,
                    timeLine = if (incomingLyrics != null) state.timelineFlow else outgoingTimeline,
                    isPlaying = incomingLyrics != null && state.controllerState.isPlaying,
                    songKey = displayedTrackKey,
                    appleStyleHorizontalGutter = lyricHorizontalGutter,
                    onLineClick = { f ->
                        if (incomingLyrics != null) actions.onUIEvent(UIEvent.UpdateProgress(f))
                    },
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .then(
                                if (transitionBlur > 0f) {
                                    Modifier.blur(transitionBlur.dp, BlurredEdgeTreatment.Rectangle)
                                } else {
                                    Modifier
                                },
                            )
                            // The lines dissolve at both edges instead of being guillotined by
                            // the header/cluster — applied from OUTSIDE the component.
                            // Softer bottom fade than the top: a lyric line is ~54dp tall, and a
                            // 44dp fade dissolved most of the last visible line.
                            .appleMusicVerticalFadeEdges(topFade = if (embedded) 18.dp else 28.dp, bottomFade = if (embedded) 12.dp else 18.dp)
                            // Zero under the Apple Music renderer, which insets its own lines so
                            // its blur has margin to spill into; the full gutter under Classic,
                            // which insets nothing. Applied AFTER the fade so the fade still spans
                            // the full width.
                            .padding(horizontal = if (rendererOwnsGutter) 0.dp else lyricHorizontalGutter)
                            .layerBackdrop(scrollingLyricsBackdrop),
                    backgroundColor = Color.Transparent,
                    footerContent = {
                        // Right-aligned, and stacked: plain text rather than the AIBadge pill the
                        // other styles use. Classic puts that badge beside a "Lyrics" heading where
                        // it has a row to itself; here it would sit inline with the provider
                        // caption, which is already the quietest thing on the page — a filled pill
                        // next to it shouts. Two right-anchored lines read as one footnote block.
                        Column(
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier.fillMaxWidth().padding(top = 20.dp, bottom = 8.dp),
                        ) {
                            if (visibleLyricsData?.translatedLyrics?.second == LyricsProvider.AI) {
                                Text(
                                    text = stringResource(Res.string.ai_translated),
                                    style = typography.footer,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                            // One line per fact, not one line joined by a bullet. "Word by word"
                            // and "Lyrics provided by SimpMusic Lyrics" are two different things,
                            // and glued together they make a single line long enough to run the
                            // width of the screen.
                            //
                            // fillMaxWidth + TextAlign.End on each, not just the Column's
                            // alignment: Column alignment places a whole text block, so a line that
                            // wraps still ends up left-aligned within itself.
                            Text(
                                text = appleMusicLyricsSyncText(visibleLyricsData ?: renderableLyrics),
                                style = typography.footer,
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            val provider = appleMusicLyricsProviderText(visibleLyricsData ?: renderableLyrics)
                            if (provider.isNotBlank()) {
                                Text(
                                    text = provider,
                                    style = typography.footer,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }
                    },
                )
                // In player-controls mode, place the two actions side-by-side in the shared
                // controls area, immediately above the seek bar. Reading mode keeps them at the
                // bottom edge of the lyrics page, clear of the navigation inset.
                Row(
                    modifier =
                    Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomEnd)
                        // Lyrics now extend to the progress bar boundary; leave the floating
                        // actions anchored at that same boundary, over the lyrics.
                        // Keep this non-negative at the layout boundary so future animation
                        // changes cannot feed an invalid value to Compose padding.
                        .padding(end = actionRowEndPadding, bottom = floatingActionsBottomPadding.coerceAtLeast(0.dp)),
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (showTranslationError && showTranslationButton) {
                        state.translationError?.let { error ->
                            TranslationErrorGlassPill(
                                message = error,
                                backdrop = glassBackdrop,
                                textStyle = typography.footer,
                                modifier = Modifier.weight(1f, fill = false),
                            )
                        }
                    }
                    if (showTranslationButton) {
                        AppleMusicFloatingTranslateButton(
                            backdrop = glassBackdrop,
                            activeContainerColor = activePillContainer,
                            activeContentColor = activePillContent,
                            isLoading = state.translationLoading,
                            isEnabled = translationEnabled && hasTranslationForCurrentTrack,
                            contentDescription = if (translationEnabled) "Hide translation" else "Show translation",
                            onClick = {
                                if (hasTranslationForCurrentTrack) {
                                    actions.onTranslationEnabledChanged(!translationEnabled)
                                } else {
                                    actions.onTranslateLyrics()
                                }
                            },
                        )
                    }
                    AppleMusicFloatingCircleButton(
                        backdrop = glassBackdrop,
                        activeContainerColor = activePillContainer,
                        activeContentColor = activePillContent,
                        icon = if (showCluster) WaveIcons.OpenInFull else WaveIcons.FullscreenExit,
                        contentDescription = if (showCluster) "Hide player controls" else "Show player controls",
                        active = !showCluster,
                        onClick = {
                            onToggleCluster()
                        },
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    // Keep the indicator slot in both states so the label stays at the same
                    // vertical position when "Loading lyrics" changes to "No lyrics found".
                    Box(Modifier.fillMaxWidth().height(42.dp)) {
                        if (showLyricsLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.TopCenter).offset(y = (-6).dp).size(28.dp),
                                color = Color.White.copy(alpha = 0.82f),
                                strokeWidth = 2.dp,
                            )
                        }
                    }
                    Text(
                        text = stringResource(if (showLyricsLoading) Res.string.loading_lyrics else Res.string.no_lyrics_found),
                        style = typography.queueSectionSubtitle,
                        color = Color.White.copy(alpha = 0.78f),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }

        // Keep one stable child in the Column while its height animates. The lyrics Box above
        // then gets the freed space without being remeasured through AnimatedVisibility's
        // enter/exit transition.
        Spacer(modifier = Modifier.height(clusterSpacerHeight))
    }
}

private const val LYRICS_EMPTY_STATE_GRACE_MS = 600L

@Composable
private fun TranslationErrorGlassPill(
    message: String,
    backdrop: Backdrop,
    textStyle: TextStyle,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(16.dp)
    val singleLineMessage = remember(message) { message.replace(Regex("\\s+"), " ").trim() }

    Row(
        modifier =
            modifier
                .liquidGlass(
                    backdrop = backdrop,
                    luminanceAnimation = 0.5f,
                    shape = shape,
                    blurScale = 1.2f,
                    highlight = Highlight.Plain,
                ).padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = WaveIcons.Error,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(18.dp),
        )
        BasicText(
            text = singleLineMessage,
            color = { Color.White },
            style = textStyle.copy(color = Color.White),
            maxLines = 1,
            softWrap = false,
            autoSize = TextAutoSize.StepBased(minFontSize = 3.sp, maxFontSize = textStyle.fontSize),
            modifier = Modifier.weight(1f, fill = false),
        )
    }
}

/** Keep the translation control visible for mixed or uncertain lyrics. Hide it only when the
 * configured target language clearly accounts for most of the words we can identify. */

@Composable
private fun AppleMusicFloatingCircleButton(
    backdrop: Backdrop,
    activeContainerColor: Color,
    activeContentColor: Color,
    icon: ImageVector,
    contentDescription: String,
    active: Boolean = false,
    onClick: () -> Unit,
) {
    val iconColor by animateColorAsState(
        targetValue = if (active) activeContentColor else Color.White,
        animationSpec = tween(180),
        label = "lyricsActionActiveIcon",
    )
    val backgroundColor by animateColorAsState(
        targetValue = activeContainerColor,
        animationSpec = tween(180),
        label = "lyricsActionActiveBackground",
    )
    if (active) {
        Box(
            modifier =
                Modifier
                    .appleMusicPressInflate()
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(backgroundColor)
                    .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = iconColor,
                modifier = Modifier.size(18.dp),
            )
        }
    } else {
        Box(
            modifier =
                Modifier
                    .appleMusicPressInflate()
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.Transparent)
                    .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = iconColor,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun AppleMusicFloatingTranslateButton(
    backdrop: Backdrop,
    activeContainerColor: Color,
    activeContentColor: Color,
    isLoading: Boolean,
    isEnabled: Boolean,
    contentDescription: String,
    onClick: () -> Unit,
) {
    val iconColor by animateColorAsState(
        targetValue = if (isEnabled) activeContentColor else Color.White,
        animationSpec = tween(180),
        label = "translationActionActiveIcon",
    )
    val backgroundColor by animateColorAsState(
        targetValue = activeContainerColor,
        animationSpec = tween(180),
        label = "translationActionActiveBackground",
    )
    val buttonContent: @Composable androidx.compose.foundation.layout.BoxScope.() -> Unit = {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(15.dp),
                color = iconColor,
                strokeWidth = 2.dp,
            )
        } else {
            Icon(
                imageVector = WaveIcons.Translate,
                contentDescription = contentDescription,
                tint = iconColor,
                modifier = Modifier.size(18.dp),
            )
        }
    }
    if (isEnabled) {
        Box(
            modifier =
                Modifier
                    .appleMusicPressInflate()
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(backgroundColor)
                    .clickable(enabled = !isLoading, onClick = onClick),
            contentAlignment = Alignment.Center,
            content = buttonContent,
        )
    } else {
        Box(
            modifier =
                Modifier
                    .appleMusicPressInflate()
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.Transparent)
                    .clickable(enabled = !isLoading, onClick = onClick),
            contentAlignment = Alignment.Center,
            content = buttonContent,
        )
    }
}

/** The sync-type half of the caption: "Word by word", "Line Synced" or "Unsynced". Its own line. */
@Composable
private fun appleMusicLyricsSyncText(lyricsData: NowPlayingScreenData.LyricsData): String {
    val syncTypeText =
        when (lyricsData.lyrics.syncType) {
            "LINE_SYNCED" -> stringResource(Res.string.line_synced)
            "RICH_SYNCED" -> stringResource(Res.string.rich_synced)
            else -> stringResource(Res.string.unsynced)
        }
    return syncTypeText
}

/** The provider half of the caption. Its own line — see the footer for why. */
@Composable
private fun appleMusicLyricsProviderText(lyricsData: NowPlayingScreenData.LyricsData): String =
    when (lyricsData.lyricsProvider) {
        LyricsProvider.SIMPMUSIC -> stringResource(Res.string.lyrics_provider_simpmusic)
        LyricsProvider.LRCLIB -> stringResource(Res.string.lyrics_provider_lrc)
        LyricsProvider.YOUTUBE -> stringResource(Res.string.lyrics_provider_youtube)
        LyricsProvider.SPOTIFY -> stringResource(Res.string.spotify_lyrics_provider)
        LyricsProvider.OFFLINE -> stringResource(Res.string.offline_mode)
        LyricsProvider.BETTER_LYRICS -> stringResource(Res.string.lyrics_provider_betterlyrics)
        LyricsProvider.BETTER_LYRICS_PORTATO -> stringResource(Res.string.lyrics_provider_portato)
        LyricsProvider.YOULYPLUS -> stringResource(Res.string.lyrics_provider_youlyplus)
        LyricsProvider.KUGOU -> stringResource(Res.string.lyrics_provider_kugou)
        LyricsProvider.UNISON -> stringResource(Res.string.lyrics_provider_unison)
        LyricsProvider.AI -> ""
    }
