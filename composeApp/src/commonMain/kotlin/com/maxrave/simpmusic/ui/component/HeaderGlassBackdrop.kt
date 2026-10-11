package com.maxrave.simpmusic.ui.component

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
import com.maxrave.simpmusic.expect.ui.PlatformBackdrop
import com.maxrave.simpmusic.ui.theme.LocalAppIsDarkTheme
import kotlin.math.ceil

/** Soft reference material, independent of the controls' intensity. */
internal val DefaultHeaderBlurRadius = 24.dp

/** A fixed page capture with progressively less diffusion toward the bottom of the header. */
@Composable
internal fun Modifier.drawHeaderGlassBackdrop(
    backdrop: PlatformBackdrop,
    tint: Color,
    singlePass: Boolean = false,
): Modifier {
    val density = LocalDensity.current
    val dark = LocalAppIsDarkTheme.current
    val radiusPx = with(density) { DefaultHeaderBlurRadius.toPx() }.coerceAtLeast(1f)
    val radii = remember(radiusPx, singlePass) {
        if (singlePass) listOf(radiusPx) else listOf(radiusPx, radiusPx * 0.47f, radiusPx * 0.15f)
    }
    val reductions = remember(radii) { radii.map(::headerBlurReduction) }
    val blurEffects = remember(radii, reductions) {
        radii.mapIndexed { index, radius ->
            val reducedRadius = radius / reductions[index]
            BlurEffect(reducedRadius, reducedRadius, TileMode.Clamp)
        }
    }
    val supported = remember(blurEffects) { blurEffects.all { it.isSupported() } }
    val surfaceTint = (if (dark) Color.Black else Color.White).copy(
        alpha = when {
            !supported -> if (dark) 0.65f else 0.72f
            else -> if (dark) 0.08f else 0.12f
        },
    )
    val capture = rememberGraphicsLayer().apply { compositingStrategy = CompositingStrategy.Offscreen }
    val half = rememberGraphicsLayer().apply { compositingStrategy = CompositingStrategy.Offscreen }
    val quarter = rememberGraphicsLayer().apply { compositingStrategy = CompositingStrategy.Offscreen }
    val eighth = rememberGraphicsLayer().apply { compositingStrategy = CompositingStrategy.Offscreen }
    val sixteenth = rememberGraphicsLayer().apply { compositingStrategy = CompositingStrategy.Offscreen }
    val thirtySecond = rememberGraphicsLayer().apply { compositingStrategy = CompositingStrategy.Offscreen }
    val reducedLayers = remember(half, quarter, eighth, sixteenth, thirtySecond) {
        listOf(half, quarter, eighth, sixteenth, thirtySecond)
    }
    val top = rememberGraphicsLayer()
    val middle = rememberGraphicsLayer()
    val lower = rememberGraphicsLayer()
    val profiles = remember(top, middle, lower, singlePass) {
        if (singlePass) listOf(top) else listOf(top, middle, lower)
    }
    profiles.forEachIndexed { index, layer ->
        layer.compositingStrategy = CompositingStrategy.Offscreen
        layer.renderEffect = if (supported) blurEffects[index] else null
    }
    val output = rememberGraphicsLayer().apply { compositingStrategy = CompositingStrategy.Offscreen }
    val captureState = remember { HeaderCaptureState() }
    val averagePaint = remember { Paint().apply { alpha = 0.25f; blendMode = BlendMode.Plus } }
    val addPaint = remember { Paint().apply { blendMode = BlendMode.Plus } }
    val maxReduction = reductions.max()
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
        val weights = headerDiffusionWeights(padding.toFloat(), size.height)
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
                        recordHeaderHalf(layer, previous, reducedSize, averagePaint)
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
                    if (!singlePass) output.record(this, layoutDirection, captureSize) {
                        val bounds = Rect(Offset.Zero, size)
                        profiles.forEachIndexed { index, layer ->
                            // Isolate each profile before applying its weight. SrcOver mixing
                            // would leak sharp page pixels between partially opaque blur profiles.
                            drawContext.canvas.saveLayer(bounds, addPaint)
                            try {
                                val factor = reductions[index].toFloat()
                                withTransform({ scale(factor, factor, pivot = Offset.Zero) }) { drawLayer(layer) }
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

    return recorder.drawWithContent {
        if (captureState.ready) {
            withTransform({ translate(-padding.toFloat(), -padding.toFloat()) }) {
                if (singlePass) {
                    // The persistent sheet already feathers its bottom edge. Draw one reduced
                    // blur directly, avoiding three full-size profile blends and their output layer.
                    val factor = reductions.first().toFloat()
                    withTransform({ scale(factor, factor, pivot = Offset.Zero) }) { drawLayer(top) }
                } else {
                    drawLayer(output)
                }
            }
        } else {
            drawRect(tint)
        }
        drawRect(surfaceTint)
        drawContent()
    }
}

/** Four shifted samples low-pass the input before each fixed-grid reduction. */
private fun DrawScope.recordHeaderHalf(
    output: GraphicsLayer,
    input: GraphicsLayer,
    outputSize: IntSize,
    averagePaint: Paint,
) {
    output.record(this, layoutDirection, outputSize) {
        val bounds = Rect(Offset.Zero, size)
        for (y in HeaderSampleOffsets) {
            for (x in HeaderSampleOffsets) {
                drawContext.canvas.saveLayer(bounds, averagePaint)
                try {
                    withTransform({
                        translate(x, y)
                        scale(0.5f, 0.5f, pivot = Offset.Zero)
                    }) { drawLayer(input) }
                } finally {
                    drawContext.canvas.restore()
                }
            }
        }
    }
}

private val HeaderSampleOffsets = floatArrayOf(-0.25f, 0.25f)

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

/** Three smooth weights whose sum stays one, including the padded area around the pane. */
private fun headerDiffusionWeights(padding: Float, height: Float): List<Brush> =
    (0..2).map { profile ->
        val stops = (0..32).map { index ->
            val y = index / 32f
            val first = headerSmoothStep((y - 0.35f) / 0.33f)
            val second = headerSmoothStep((y - 0.68f) / 0.32f)
            val weight = when (profile) {
                0 -> 1f - first
                1 -> first * (1f - second)
                else -> first * second
            }
            y to Color.White.copy(alpha = weight)
        }
        Brush.verticalGradient(*stops.toTypedArray(), startY = padding, endY = padding + height)
    }

internal fun headerSmoothStep(value: Float): Float {
    val x = value.coerceIn(0f, 1f)
    return x * x * (3f - 2f * x)
}

/** Draw-local readiness without snapshot writes that could schedule another frame. */
private class HeaderCaptureState {
    var ready = false
}
