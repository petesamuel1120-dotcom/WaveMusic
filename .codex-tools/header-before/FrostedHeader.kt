package com.maxrave.simpmusic.ui.component

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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.drawPlainBackdrop
import com.maxrave.simpmusic.expect.ui.PlatformBackdrop
import com.maxrave.simpmusic.expect.ui.layerBackdrop
import com.maxrave.simpmusic.extension.barBlurStyle
import com.maxrave.simpmusic.ui.theme.LocalLiquidGlassEnabled
import com.maxrave.simpmusic.ui.theme.LocalLiquidGlassAppearance
import com.maxrave.simpmusic.ui.theme.LocalAppColorScheme
import com.maxrave.simpmusic.ui.theme.LocalAppIsDarkTheme
import com.maxrave.simpmusic.ui.theme.LocalIsDarkTheme
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazePerformanceMode
import dev.chrisbanes.haze.HazeSourceRetention
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.hazeBlur

private val HeaderBlurRadius = 24.dp
private val MinHeaderBlurRadius = 24.dp
private val MaxHeaderBlurRadius = 48.dp
// Feather within the measured toolbar; never cover the next row or search field.
private val HeaderFeatherHeight = 8.dp

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
    pinInLandscape: Boolean = false,
    content: @Composable BoxScope.() -> Unit,
) {
    val liquidGlassEnabled = LocalLiquidGlassEnabled.current
    val appearance = LocalLiquidGlassAppearance.current
    val entry = LocalLifecycleOwner.current as? NavBackStackEntry
    val featherEdge = fadeToContent && LocalSharedHeaderState.current?.hasPortraitSearch(entry?.id) != true
    // Chrome remains frosted even at the liquid end of the controls' appearance slider.
    val headerBlurRadius = (HeaderBlurRadius * appearance.blurMultiplier)
        .coerceIn(MinHeaderBlurRadius, MaxHeaderBlurRadius)
    if (shareHeader && SharePageHeader(
            modifier,
            SharedHeaderPresentation(content, hazeState, backdrop, tint, showSurface, featherEdge, pinInLandscape),
        )
    ) return
    val isLightTint = tint.luminance() > 0.5f
    val density = LocalDensity.current
    val featherHeightPx = with(density) { HeaderFeatherHeight.toPx() }
    val blurFadeMask = remember(featherHeightPx) {
        headerFeatherBrush(Color.White, featherHeightPx)
    }
    // Transparent blur padding is composited over a stable tint, not a magnified edge pixel.
    // The fill shares the small in-bounds feather, so it never paints on the page below.
    val surfaceFill = remember(tint, featherEdge, featherHeightPx) {
        if (featherEdge) headerFeatherBrush(tint, featherHeightPx) else SolidColor(tint)
    }
    val blurStyle = remember(tint, isLightTint, appearance.tintMultiplier, headerBlurRadius, featherEdge, blurFadeMask) {
        barBlurStyle(tint, tintAlpha = (if (isLightTint) 0.24f else 0.34f) * appearance.tintMultiplier)
            .then {
                blurRadius(headerBlurRadius)
                // Avoid smearing a bright first/last captured row across the entire blur kernel.
                blurredEdgeTreatment(BlurredEdgeTreatment.Unbounded)
                noiseFactor(0f)
                if (featherEdge) mask(blurFadeMask)
            }
    }
    // Keep the renderer attached across title/search threshold changes. Only its opacity changes,
    // so rapid scroll reversals cannot repeatedly discard its capture and retained frame.
    val surfaceAlpha = animateFloatAsState(
        targetValue = if (showSurface) 1f else 0f,
        animationSpec = tween(180, easing = LinearEasing),
        label = "Frosted header visibility",
    )
    Box(
        // Honor the actual status inset and toolbar height; a fixed cap can expose raw content
        // above or below the surface on devices with larger insets or text scaling.
        modifier.landscapeScrollingHeader(pinned = pinInLandscape),
    ) {
        CompositionLocalProvider(
            LocalLandscapeHeaderHosted provides true,
            LocalHeaderBackdrop provides backdrop,
        ) {
            if (liquidGlassEnabled) {
                if (backdrop != null) {
                    Box(
                        Modifier
                            .matchParentSize()
                            .clipToBounds()
                            .graphicsLayer { alpha = surfaceAlpha.value }
                            // RectangleShape bypasses the shared lens effect; a zero-radius
                            // rounded rectangle stays visually square while using the same
                            // intensity-driven refraction as the dock and mini-player.
                            .liquidGlass(backdrop, shape = RoundedCornerShape(0.dp), interactive = false),
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
            } else {
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

/** Dissolve only the last few pixels inside the toolbar, without extending its painted bounds. */
private fun headerFeatherBrush(color: Color, featherHeightPx: Float): Brush =
    object : ShaderBrush() {
        override fun createShader(size: Size): Shader =
            LinearGradientShader(
                colors = listOf(color, color.copy(alpha = 0f)),
                from = Offset(0f, (size.height - featherHeightPx).coerceAtLeast(0f)),
                to = Offset(0f, size.height),
            )
    }
