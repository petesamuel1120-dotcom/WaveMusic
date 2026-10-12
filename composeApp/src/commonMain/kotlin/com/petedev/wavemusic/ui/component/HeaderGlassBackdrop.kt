package com.petedev.wavemusic.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.layer.CompositingStrategy
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.petedev.wavemusic.expect.ui.PlatformBackdrop
import com.petedev.wavemusic.ui.theme.LocalAppIsDarkTheme
import com.petedev.wavemusic.ui.theme.LocalFrostedGlassHeaderStyle
import com.petedev.wavemusic.domain.manager.FrostedGlassHeaderStyle
import kotlin.math.ceil
import kotlin.math.pow

/** Soft reference material, independent of the controls' intensity. */
internal val DefaultHeaderBlurRadius = 14.dp

/** A fixed page capture with progressively less diffusion toward the bottom of the header. */
@Composable
internal fun Modifier.drawHeaderGlassBackdrop(
    backdrop: PlatformBackdrop,
    tint: Color,
): Modifier = drawFrostedGlassBackdrop(backdrop, tint, LocalFrostedGlassHeaderStyle.current, fromBottom = false)

/** The same cached diffusion chain serves either edge of the viewport. */
@Composable
internal fun Modifier.drawFrostedGlassBackdrop(
    backdrop: PlatformBackdrop,
    tint: Color,
    style: FrostedGlassHeaderStyle,
    fromBottom: Boolean,
): Modifier {
    val density = LocalDensity.current
    val dark = LocalAppIsDarkTheme.current
    val radiusPx = with(density) { DefaultHeaderBlurRadius.toPx() }.coerceAtLeast(1f)
    val radii = remember(radiusPx) {
        listOf(radiusPx, radiusPx * 0.12f)
    }
    // Both profiles share the same filtered input. A second full-resolution capture
    // would make the gentle part unnecessarily expensive and susceptible to shimmer.
    val maxReduction = remember(radiusPx) { headerBlurReduction(radiusPx) }
    val reductions = remember(maxReduction) { listOf(maxReduction, maxReduction) }
    val blurEffects = remember(radii, reductions) {
        radii.mapIndexed { index, radius ->
            val reducedRadius = radius / reductions[index]
            BlurEffect(reducedRadius, reducedRadius, TileMode.Clamp)
        }
    }
    val supported = remember(blurEffects) { blurEffects.all { it.isSupported() } }
    val capture = rememberGraphicsLayer().apply { compositingStrategy = CompositingStrategy.Offscreen }
    val half = rememberGraphicsLayer().apply { compositingStrategy = CompositingStrategy.Offscreen }
    val quarter = rememberGraphicsLayer().apply { compositingStrategy = CompositingStrategy.Offscreen }
    val eighth = rememberGraphicsLayer().apply { compositingStrategy = CompositingStrategy.Offscreen }
    val sixteenth = rememberGraphicsLayer().apply { compositingStrategy = CompositingStrategy.Offscreen }
    val thirtySecond = rememberGraphicsLayer().apply { compositingStrategy = CompositingStrategy.Offscreen }
    val reducedLayers = remember(half, quarter, eighth, sixteenth, thirtySecond) {
        listOf(half, quarter, eighth, sixteenth, thirtySecond)
    }
    // Preserve low-pass filtering at every reduction so thin strokes cannot shimmer
    // against the fixed sampling grid. A native filter needs one input reference;
    // four shifted blends multiplied the page's render-tree traversal at each level.
    val reductionFilter = remember { BlurEffect(0.5f, 0.5f, TileMode.Clamp) }
    reducedLayers.forEach { it.renderEffect = if (supported) reductionFilter else null }
    val top = rememberGraphicsLayer()
    val lower = rememberGraphicsLayer()
    val profiles = remember(top, lower) { listOf(top, lower) }
    profiles.forEachIndexed { index, layer ->
        layer.compositingStrategy = CompositingStrategy.Offscreen
        layer.renderEffect = if (supported) blurEffects[index] else null
    }
    val output = rememberGraphicsLayer().apply { compositingStrategy = CompositingStrategy.Offscreen }
    val captureState = remember { HeaderCaptureState() }
    val addPaint = remember { Paint().apply { blendMode = BlendMode.Plus } }
    // Include the complete Gaussian kernel and the prefilter's input support.
    val padding = ceil(3f * radiusPx + maxReduction).toInt()
    var coordinates: LayoutCoordinates? by remember { mutableStateOf(null) }

    val recorder = onGloballyPositioned {
        coordinates = it
    }.drawWithCache {
        // Allocate masks only when the measured pane or material changes, never per scroll frame.
        val captureSize = IntSize(
            roundHeaderCapture(size.width, padding, maxReduction),
            roundHeaderCapture(size.height, padding, maxReduction),
        )
        val paneSize = IntSize(ceil(size.width).toInt(), ceil(size.height).toInt())
        val weights = headerDiffusionWeights(size.height, style, fromBottom)
        onDrawWithContent {
            val target = coordinates?.takeIf { it.isAttached }
            val source = backdrop.sourceCoordinates?.takeIf { it.isAttached }
            val sourceSize = backdrop.contentLayer.size
            captureState.ready = false
            if (size.hasGlassEffectBounds() && target != null && source != null &&
                sourceSize.width > 0 && sourceSize.height > 0
            ) {
                val offset = target.localPositionOf(source, Offset.Zero)
                if (offset.x.isFinite() && offset.y.isFinite()) {
                    capture.record(this, layoutDirection, captureSize) {
                        drawRect(tint)
                        // Extend only the viewport's side pixels into the blur padding.
                        // Theme-filled side padding otherwise becomes a narrow dark/light
                        // frame as the blur gets gentler. Keep vertical padding stable so
                        // a bright scrolling first row cannot smear across the status bar.
                        drawHeaderSidePadding(backdrop.contentLayer,
                            Offset(padding + offset.x, padding + offset.y), sourceSize)
                        withTransform({ translate(padding + offset.x, padding + offset.y) }) {
                            drawLayer(backdrop.contentLayer)
                        }
                    }
                    var previous = capture
                    var reducedSize = captureSize
                    var reduction = 1
                    for (layer in reducedLayers) {
                        if (reduction >= maxReduction) break
                        reducedSize = IntSize(reducedSize.width / 2, reducedSize.height / 2)
                        recordHeaderHalf(layer, previous, reducedSize)
                        previous = layer
                        reduction *= 2
                    }
                    profiles.forEachIndexed { index, layer ->
                        val factor = reductions[index]
                        val input = if (factor == 1) capture else reducedLayers[headerReductionIndex(factor)]
                        layer.record(this, layoutDirection, IntSize(captureSize.width / factor, captureSize.height / factor)) {
                            drawLayer(input)
                        }
                    }
                    // Blend only the visible header, not the padded capture. The padding
                    // is still sampled by the blur kernels without adding full-size blends.
                    output.record(this, layoutDirection, paneSize) {
                        val bounds = Rect(Offset.Zero, size)
                        profiles.forEachIndexed { index, layer ->
                            // Isolate each profile before applying its weight. SrcOver mixing
                            // would leak sharp page pixels between partially opaque blur profiles.
                            drawContext.canvas.saveLayer(bounds, addPaint)
                            try {
                                val factor = reductions[index].toFloat()
                                withTransform({
                                    translate(-padding.toFloat(), -padding.toFloat())
                                    scale(factor, factor, pivot = Offset.Zero)
                                }) { drawLayer(layer) }
                                drawRect(weights[index], blendMode = BlendMode.DstIn)
                            } finally {
                                drawContext.canvas.restore()
                            }
                        }
                    }
                    captureState.ready = true
                }
            }
            // The consumer is inside this recorder and samples this draw's completed input.
            drawContent()
        }
    }

    return recorder.drawWithCache {
        // A gentle contrast scrim protects status icons at the screen edge without
        // increasing diffusion or extending an opaque strip over the artist image.
        val tintColor = if (dark) Color.Black else Color.White
        val peakAlpha = if (supported) { if (dark) 0.30f else 0.40f } else { if (dark) 0.65f else 0.72f }
        val lowerAlpha = if (supported) { if (dark) 0.08f else 0.12f } else peakAlpha
        val tintBrush = Brush.verticalGradient(
            *(0..32).map { index ->
                val position = if (fromBottom) 1f - index / 32f else index / 32f
                position to tintColor.copy(alpha = peakAlpha + (lowerAlpha - peakAlpha) * headerSmoothStep(position))
            }.toTypedArray(),
            startY = 0f,
            endY = size.height,
        )
        onDrawWithContent {
            if (captureState.ready) {
                drawLayer(output)
            } else {
                drawRect(tint)
            }
            drawRect(tintBrush)
            drawContent()
        }
    }
}

private fun DrawScope.drawHeaderSidePadding(source: GraphicsLayer, origin: Offset, sourceSize: IntSize) {
    val top = origin.y.coerceAtLeast(0f)
    val bottom = (origin.y + sourceSize.height).coerceAtMost(size.height)
    if (bottom <= top) return
    if (origin.x > 0f) {
        clipRect(0f, top, origin.x.coerceAtMost(size.width), bottom) {
            withTransform({
                translate(0f, origin.y)
                scale(origin.x, 1f, pivot = Offset.Zero)
            }) { drawLayer(source) }
        }
    }
    val right = origin.x + sourceSize.width
    if (right < size.width) {
        clipRect(right.coerceAtLeast(0f), top, size.width, bottom) {
            withTransform({
                translate(right, origin.y)
                scale(size.width - right, 1f, pivot = Offset.Zero)
                translate(-(sourceSize.width - 1f), 0f)
            }) { drawLayer(source) }
        }
    }
}

/** Let the GPU filter each fixed 2:1 reduction without four temporary blend surfaces. */
private fun DrawScope.recordHeaderHalf(
    output: GraphicsLayer,
    input: GraphicsLayer,
    outputSize: IntSize,
) {
    output.record(this, layoutDirection, outputSize) {
        withTransform({ scale(0.5f, 0.5f, pivot = Offset.Zero) }) { drawLayer(input) }
    }
}

private fun headerBlurReduction(radiusPx: Float): Int {
    var factor = 1
    while (radiusPx / factor > 8f && factor < 32) factor *= 2
    return factor
}

private fun headerReductionIndex(factor: Int): Int {
    var index = 0
    var value = factor
    while (value > 2) { value /= 2; index++ }
    return index
}

private fun roundHeaderCapture(extent: Float, padding: Int, grid: Int): Int =
    ceil((extent + padding * 2) / grid).toInt() * grid

/** Mix two aligned blur kernels continuously; the result keeps its original colour and alpha. */
private fun headerDiffusionWeights(height: Float, style: FrostedGlassHeaderStyle, fromBottom: Boolean): List<Brush> =
    (0..1).map { profile ->
        val stops = (0..32).map { index ->
            val y = index / 32f
            val strong = headerStrongBlurWeight(if (fromBottom) 1f - y else y, style)
            val weight = if (profile == 0) strong else 1f - strong
            y to Color.White.copy(alpha = weight)
        }
        Brush.verticalGradient(*stops.toTypedArray(), startY = 0f, endY = height)
    }

/** Keep peak diffusion at the edge, varying how long it lasts through the toolbar. */
internal fun headerStrongBlurWeight(
    position: Float,
    style: FrostedGlassHeaderStyle = FrostedGlassHeaderStyle.Default,
): Float {
    val start = when (style) {
        FrostedGlassHeaderStyle.Moderate -> 0.16f
        FrostedGlassHeaderStyle.Balanced -> 0.20f
        FrostedGlassHeaderStyle.Fuller -> 0.24f
        FrostedGlassHeaderStyle.Off -> return 0f
    }
    return 1f - headerSmoothStep((position - start) / (1f - start))
}

/** Fade through the entire material rather than adding a feather to an opaque blurred band. */
internal fun headerMaterialOpacity(
    position: Float,
    style: FrostedGlassHeaderStyle = FrostedGlassHeaderStyle.Default,
): Float {
    val exponent = when (style) {
        FrostedGlassHeaderStyle.Moderate -> 1.35f
        FrostedGlassHeaderStyle.Balanced -> 1.55f
        FrostedGlassHeaderStyle.Fuller -> 1.75f
        FrostedGlassHeaderStyle.Off -> return 0f
    }
    return 1f - headerSmoothStep(position.coerceIn(0f, 1f).pow(exponent))
}

internal fun headerSmoothStep(value: Float): Float {
    val x = value.coerceIn(0f, 1f)
    return x * x * (3f - 2f * x)
}

/** Draw-local readiness without snapshot writes that could schedule another frame. */
private class HeaderCaptureState {
    var ready = false
}
