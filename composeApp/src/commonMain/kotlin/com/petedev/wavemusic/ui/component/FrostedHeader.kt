package com.petedev.wavemusic.ui.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavBackStackEntry
import com.kyant.backdrop.drawPlainBackdrop
import com.petedev.wavemusic.expect.ui.PlatformBackdrop
import com.petedev.wavemusic.expect.ui.layerBackdrop
import com.petedev.wavemusic.extension.barBlurStyle
import com.petedev.wavemusic.ui.theme.LocalLiquidGlassEnabled
import com.petedev.wavemusic.ui.theme.LocalFrostedGlassHeaderStyle
import com.petedev.wavemusic.domain.manager.FrostedGlassHeaderStyle
import com.petedev.wavemusic.ui.theme.LocalAppColorScheme
import com.petedev.wavemusic.ui.theme.LocalAppIsDarkTheme
import com.petedev.wavemusic.ui.theme.LocalIsDarkTheme
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazePerformanceMode
import dev.chrisbanes.haze.HazeSourceRetention
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.hazeBlur

/** App-level fallback for chrome whose screen has no dedicated haze source. */
val LocalSharedHeaderHazeState = staticCompositionLocalOf<HazeState?> { null }

/** Background-only capture, shared by page sources without recording their glass controls. */
val LocalPageAuroraBackdrop = staticCompositionLocalOf<PlatformBackdrop?> { null }

/** Progress of the header surface; child control lenses recede as the shared pane appears. */
val LocalHeaderSurfaceProgress = staticCompositionLocalOf { 0f }

/** Capture only page content; the header must be a sibling of this source. */
@Composable
fun Modifier.frostedHeaderSource(
    backdrop: PlatformBackdrop,
    auroraBackground: Color? = null,
): Modifier {
    if (!LocalLiquidGlassEnabled.current) return this
    val host = LocalSharedHeaderState.current
    val sourceToken = remember(backdrop) { Any() }
    DisposableEffect(host, backdrop, sourceToken) {
        onDispose { host?.unregisterBackdropSource(backdrop, sourceToken) }
    }
    val source = layerBackdrop(backdrop).onGloballyPositioned { coordinates ->
        if (coordinates.isAttached && coordinates.size.width > 0 && coordinates.size.height > 0) {
            host?.registerBackdropSource(backdrop, sourceToken)
        } else {
            host?.unregisterBackdropSource(backdrop, sourceToken)
        }
    }
    if (auroraBackground == null) return source
    // Sample the fixed page aurora in window coordinates. Repainting a gradient inside a card
    // restarts it at that card's bounds, leaving a visible seam against the surrounding page.
    val pageAurora = LocalPageAuroraBackdrop.current
    return if (pageAurora != null) {
        source.drawPlainBackdrop(
            backdrop = pageAurora,
            shape = { RectangleShape },
            effects = {},
        )
    } else {
        source.background(auroraBackground).auroraGradientBackground(auroraBackground.luminance() > 0.5f)
    }
}

/** Apply the shared glass or solid surface to legacy toolbars. */
@Composable
fun LiquidGlassHeader(
    hazeState: HazeState,
    backdrop: PlatformBackdrop,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.background,
    shareHeader: Boolean = false,
    content: @Composable () -> Unit,
) {
    FrostedHeader(
        hazeState = hazeState,
        backdrop = backdrop,
        modifier = modifier,
        tint = tint,
        shareHeader = shareHeader,
    ) {
        content()
    }
}

/** A theme-tinted toolbar that blurs the screen content scrolling behind it. */
@Composable
fun FrostedHeader(
    hazeState: HazeState,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.background,
    fadeToContent: Boolean = true,
    backdrop: PlatformBackdrop? = null,
    shareHeader: Boolean = false,
    showSurface: Boolean = true,
    content: @Composable BoxScope.() -> Unit,
) {
    val liquidGlassEnabled = LocalLiquidGlassEnabled.current
    val frostedStyle = LocalFrostedGlassHeaderStyle.current
    val entry = LocalLifecycleOwner.current as? NavBackStackEntry
    val featherEdge = fadeToContent && LocalSharedHeaderState.current?.hasPortraitSearch(entry?.id) != true
    val headerBlurRadius = DefaultHeaderBlurRadius
    if (shareHeader && SharePageHeader(
            modifier,
            SharedHeaderPresentation(content, hazeState, backdrop, tint, showSurface, featherEdge),
        )
    ) return
    val isLightTint = tint.luminance() > 0.5f
    val blurFadeMask = remember(frostedStyle) {
        headerFeatherBrush(Color.White, frostedStyle)
    }
    // Transparent blur padding is composited over a stable tint, not a magnified edge pixel.
    // The fill shares the full-height taper and never paints on the page below.
    val surfaceFill = remember(tint, featherEdge, frostedStyle) {
        if (featherEdge) headerFeatherBrush(tint, frostedStyle) else SolidColor(tint)
    }
    val headerTintAlpha = if (isLightTint) 0.12f else 0.08f
    val blurStyle = remember(tint, headerTintAlpha, headerBlurRadius, featherEdge, blurFadeMask) {
        barBlurStyle(tint, tintAlpha = headerTintAlpha)
            .then {
                blurRadius(headerBlurRadius)
                // Avoid smearing a bright first/last captured row across the entire blur kernel.
                blurredEdgeTreatment(BlurredEdgeTreatment.Unbounded)
                noiseFactor(0f)
                if (featherEdge) mask(blurFadeMask)
            }
    }
    // Keep the renderer through its fade-out, then release its blur layers. The shared fixed
    // header has no surface here: its separate persistent pane supplies the background.
    val surfaceAlpha = animateFloatAsState(
        targetValue = if (showSurface && (!liquidGlassEnabled || frostedStyle.isEnabled)) 1f else 0f,
        animationSpec = tween(180, easing = LinearEasing),
        label = "Frosted header visibility",
    )
    Box(
        // Honor the actual status inset and toolbar height; a fixed cap can expose raw content
        // above or below the surface on devices with larger insets or text scaling.
        modifier,
    ) {
        CompositionLocalProvider(
            LocalHeaderBackdrop provides backdrop,
        ) {
            if (liquidGlassEnabled && frostedStyle.isEnabled && (showSurface || surfaceAlpha.value > 0f)) {
                if (backdrop != null) {
                    Box(
                        Modifier
                            .matchParentSize()
                            .clipToBounds()
                            .graphicsLayer {
                                alpha = surfaceAlpha.value
                                compositingStrategy = CompositingStrategy.Offscreen
                            }
                            .then(
                                if (featherEdge) Modifier.drawWithContent {
                                    drawContent()
                                    // Mask the complete glass pane (blur and tint),
                                    // keeping the toolbar's icons and text fully opaque.
                                    drawRect(brush = blurFadeMask, blendMode = BlendMode.DstIn)
                                } else Modifier,
                            )
                            .drawHeaderGlassBackdrop(
                                backdrop = backdrop,
                                tint = tint,
                            ),
                    )
                } else {
                    Box(
                        Modifier
                            .matchParentSize()
                            .clipToBounds()
                            .graphicsLayer { alpha = surfaceAlpha.value }
                            .background(surfaceFill)
                            .hazeBlur(
                                input = HazeInput.Sources(state = hazeState, retention = HazeSourceRetention.KeepLastFrame),
                                style = blurStyle,
                                performanceMode = HazePerformanceMode.Quality,
                                expandLayerBounds = true,
                            ),
                    )
                }
            } else if (!liquidGlassEnabled) {
                // Use the shared 90% theme fill; the header controls remain fully opaque.
                Box(
                    Modifier
                        .matchParentSize()
                        // Follow the same title-driven visibility as the liquid surface while
                        // leaving its renderer and controls untouched in opaque mode.
                        .graphicsLayer { alpha = surfaceAlpha.value }
                        .background(flatChromeBackground()),
                )
            }
            CompositionLocalProvider(LocalHeaderSurfaceProgress provides surfaceAlpha.value) {
                if (liquidGlassEnabled) {
                    content()
                } else {
                    // Artwork screens can force their content palette dark; flat chrome follows the app.
                    val appScheme = LocalAppColorScheme.current ?: MaterialTheme.colorScheme
                    CompositionLocalProvider(LocalIsDarkTheme provides LocalAppIsDarkTheme.current) {
                        MaterialTheme(colorScheme = appScheme) {
                            content()
                        }
                    }
                }
            }
        }
    }
}

/** Dissolve the toolbar's bottom edge without extending its painted bounds. */
private fun headerFeatherBrush(color: Color, style: FrostedGlassHeaderStyle): Brush =
    object : ShaderBrush() {
        override fun createShader(size: Size): Shader =
            LinearGradientShader(
                colors = (0..32).map { index ->
                    color.copy(alpha = color.alpha * headerMaterialOpacity(index / 32f, style))
                },
                colorStops = (0..32).map { it / 32f },
                from = Offset.Zero,
                to = Offset(0f, size.height),
            )
    }
