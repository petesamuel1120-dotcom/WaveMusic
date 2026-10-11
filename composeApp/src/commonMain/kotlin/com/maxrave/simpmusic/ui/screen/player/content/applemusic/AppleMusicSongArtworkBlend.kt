package com.maxrave.simpmusic.ui.screen.player.content.applemusic

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Continue the visible cover's crop and edge colors before handing off to the wider ambient image. */
internal fun Modifier.appleMusicSongColorContinuation(
    bitmap: ImageBitmap,
    artworkHeight: Dp,
    landscape: Boolean,
): Modifier = drawWithCache {
    val coverWidth = if (landscape) size.width * 0.5f else size.width
    val coverHeight = artworkHeight.toPx().coerceIn(1f, size.height.coerceAtLeast(1f))
    val cropScale = maxOf(coverWidth / bitmap.width, coverHeight / bitmap.height)
    val cropOffset = Offset(
        (coverWidth - bitmap.width * cropScale) * 0.5f,
        (coverHeight - bitmap.height * cropScale) * 0.5f,
    )
    // Clamp extends actual edge pixels. A second full-screen Crop would sample unrelated
    // areas of the cover right where the sharp artwork dissolves into the background.
    val coverBrush = ShaderBrush(ImageShader(bitmap, TileMode.Clamp, TileMode.Clamp))
    val axisSize = if (landscape) size.width else size.height
    val coverEdge = if (landscape) coverWidth else coverHeight
    val continuation = if (landscape) size.width * 0.28f else minOf(220.dp.toPx(), size.height * 0.3f)
    val blendEnd = (coverEdge + continuation).coerceAtMost(axisSize)
    val stops = Array(17) { index ->
        val progress = index / 16f
        val eased = progress * progress * (3f - 2f * progress)
        ((coverEdge + (blendEnd - coverEdge) * progress) / axisSize.coerceAtLeast(1f)) to
            Color.Black.copy(alpha = 1f - eased)
    }
    val blendMask = if (landscape) Brush.horizontalGradient(*stops) else Brush.verticalGradient(*stops)
    val bounds = Rect(Offset.Zero, size)
    val layerPaint = Paint()
    onDrawBehind {
        if (cropScale.isFinite() && cropScale > 0f && axisSize > 0f) {
            drawIntoCanvas { it.saveLayer(bounds, layerPaint) }
            withTransform({
                translate(cropOffset.x, cropOffset.y)
                scale(cropScale, cropScale, Offset.Zero)
            }) {
                drawRect(
                    brush = coverBrush,
                    topLeft = Offset(-cropOffset.x / cropScale, -cropOffset.y / cropScale),
                    size = Size(size.width / cropScale, size.height / cropScale),
                )
            }
            drawRect(blendMask, blendMode = BlendMode.DstIn)
            drawIntoCanvas { it.restore() }
        }
    }
}

/** Smooth alpha ramps meet the opaque cover and ambient background with a flat slope. */
internal fun appleMusicFadeStops(
    leading: Float,
    trailing: Float,
    smooth: Boolean,
): Array<Pair<Float, Color>> {
    if (!smooth) return arrayOf(
        0f to Color.Transparent, leading to Color.Black,
        1f - trailing to Color.Black, 1f to Color.Transparent,
    )
    val stops = mutableListOf<Pair<Float, Color>>()
    if (leading > 0f) {
        for (index in 0..16) {
            val t = index / 16f
            stops += leading * t to Color.Black.copy(alpha = t * t * (3f - 2f * t))
        }
    } else {
        stops += 0f to Color.Black
    }
    if (trailing > 0f) {
        for (index in 0..16) {
            val t = index / 16f
            stops += (1f - trailing + trailing * t) to Color.Black.copy(alpha = 1f - t * t * (3f - 2f * t))
        }
    } else {
        stops += 1f to Color.Black
    }
    return stops.toTypedArray()
}
