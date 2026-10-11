@file:OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)

package com.maxrave.simpmusic.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.maxrave.simpmusic.expect.ui.layerBackdrop
import com.maxrave.simpmusic.expect.ui.rememberBackdrop
import com.maxrave.simpmusic.ui.theme.LiquidGlassAppearance
import com.maxrave.simpmusic.ui.theme.LocalLiquidGlassAppearance
import com.maxrave.simpmusic.ui.theme.LocalLiquidGlassEnabled
import org.jetbrains.skia.Bitmap
import kotlin.test.Test
import kotlin.test.assertTrue

class GlassWhiteMotionTest {
    @Test
    fun thinWhiteStrokesKeepTheirBrightnessDuringSubpixelScrollAndReversals() {
        for (density in listOf(1f, 2.75f)) {
            for (intensity in listOf(0f, 0.25f, 0.26f, 0.375f, 0.49f, 0.50f, 0.51f, 0.75f)) {
                val phase = mutableFloatStateOf(0f)
                val scene = ImageComposeScene(
                    (320 * density).toInt(), (320 * density).toInt(), density = Density(density),
                ) {
                    CompositionLocalProvider(
                        LocalLiquidGlassEnabled provides true,
                        LocalLiquidGlassAppearance provides LiquidGlassAppearance.fromIntensity(intensity),
                    ) {
                        val backdrop = rememberBackdrop(Color.Black)
                        Box(Modifier.fillMaxSize()) {
                            Canvas(Modifier.fillMaxSize().layerBackdrop(backdrop)) {
                                drawRect(Color.Black)
                                // Constant white coverage, like closely packed fine glyph strokes.
                                // A diffuse reflection must not pulse as their phase changes.
                                for (row in -4..(size.height.toInt() + 4) step 4) {
                                    drawRect(Color.White, Offset(0f, row + phase.floatValue), Size(size.width, 1f))
                                }
                            }
                            for ((y, height, blurScale) in listOf(Triple(80, 56, 1f), Triple(200, 60, 0.72f))) {
                                Box(Modifier.offset(20.dp, y.dp).size(280.dp, height.dp).drawInteractiveGlass(
                                    true, backdrop, 0.5f, BottomDockShape, interaction = null, blurScale = blurScale,
                                ))
                            }
                        }
                    }
                }
                var time = 0L
                val low = IntArray(2) { 255 }
                val high = IntArray(2)
                try {
                    for (step in (0..32).toList() + (32 downTo 0).toList()) {
                        phase.floatValue = step / 8f
                        val image = scene.render(time.also { time += 16_666_667L })
                        val bitmap = Bitmap.makeFromImage(image)
                        try {
                            for ((index, y) in listOf(108, 230).withIndex()) {
                                val value = bitmap.getColor((160 * density).toInt(), (y * density).toInt()) and 255
                                low[index] = minOf(low[index], value)
                                high[index] = maxOf(high[index], value)
                            }
                        } finally {
                            bitmap.close()
                            image.close()
                        }
                    }
                    for (index in low.indices) {
                        assertTrue(high[index] - low[index] <= 3,
                            "White strokes pulse: density=$density intensity=$intensity surface=$index range=${low[index]}..${high[index]}")
                    }
                } finally {
                    scene.close()
                }
            }
        }
    }
}
