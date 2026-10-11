package com.maxrave.simpmusic.expect.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.layer.CompositingStrategy
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop as nativeBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.colorControls
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import kotlin.math.sign

/**
 * The backdrop a glass surface samples from.
 *
 * The page is recorded once and that recording is shared by the window and the glass.
 * Kyant still handles coordinates and inverse press transforms, but its second capture
 * pass replays the recording instead of drawing scrolling content again.
 */
@Stable
class PlatformBackdrop internal constructor(
    internal val native: LayerBackdrop,
    internal val contentLayer: GraphicsLayer,
) : Backdrop by native {
    // Header sheets read the completed page layer directly, using the same viewport coordinates.
    internal var sourceCoordinates: LayoutCoordinates? = null
}

/** Marks a composable as the source layer that sibling glass surfaces refract. */
fun Modifier.layerBackdrop(backdrop: PlatformBackdrop): Modifier =
    this.onGloballyPositioned { backdrop.sourceCoordinates = it }
        .nativeBackdrop(backdrop.native).drawWithContent {
        // The native source's onDraw replays this completed layer. Keep its modifier
        // outside the recorder so the capture can never record a reference to itself.
        backdrop.contentLayer.record { this@drawWithContent.drawContent() }
        drawLayer(backdrop.contentLayer)
    }

@Composable
fun rememberBackdrop(color: Color): PlatformBackdrop {
    val contentLayer = rememberGraphicsLayer().apply {
        // Rasterize the page before its white edges/glyphs enter the blur chain.
        // Auto only isolates drawing commands; Offscreen makes both consumers
        // use the same composited source within the measured viewport.
        compositingStrategy = CompositingStrategy.Offscreen
    }
    val background = rememberUpdatedState(color)
    val replay: ContentDrawScope.() -> Unit = remember(contentLayer) {
        {
            drawRect(background.value)
            drawLayer(contentLayer)
        }
    }
    // Changing a tint updates the recorded pixels without replacing the source or
    // discarding the coordinates already held by the mini-player and navigation.
    val native = rememberLayerBackdrop(onDraw = replay)
    return remember(native, contentLayer) { PlatformBackdrop(native, contentLayer) }
}

fun Modifier.drawBackdropCustomShape(
    backdrop: PlatformBackdrop,
    layer: GraphicsLayer,
    luminanceAnimation: Float,
    shape: Shape,
): Modifier =
    this.drawBackdrop(
        backdrop = backdrop,
        effects = {
            val l = (luminanceAnimation * 2f - 1f).let { sign(it) * it * it }
            vibrancy()
            colorControls(
                // Neutral brightness/contrast: the old curve washed the glass out to white on bright
                // backgrounds ("đục trắng"). Darkening is done in onDrawSurface (keeps "đục đen").
                brightness = 0.05f,
                contrast = 1f,
                saturation = 1.5f,
            )
            blur(
                if (l > 0f) {
                    lerp(8f.dp.toPx(), 16f.dp.toPx(), l)
                } else {
                    lerp(8f.dp.toPx(), 2f.dp.toPx(), -l)
                },
            )
            lens(24f.dp.toPx(), size.minDimension / 2f, true)
        },
        onDrawBackdrop = { drawBackdrop ->
            drawBackdrop()
            layer.record { drawBackdrop() }
        },
        shape = { shape },
        onDrawSurface = {
            val darken = lerp(0.12f, 0.5f, ((luminanceAnimation - 0.3f) / 0.5f).coerceIn(0f, 1f))
            drawRect(Color.Black.copy(alpha = darken))
        },
    )
