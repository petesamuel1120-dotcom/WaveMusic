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
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.colorControls
import com.maxrave.simpmusic.expect.ui.layerBackdrop
import com.maxrave.simpmusic.expect.ui.rememberBackdrop
import com.maxrave.simpmusic.ui.theme.LiquidGlassAppearance
import com.maxrave.simpmusic.ui.theme.LocalLiquidGlassAppearance
import com.maxrave.simpmusic.ui.theme.LocalLiquidGlassEnabled
import org.jetbrains.skia.Bitmap
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

class GlassScrollTest {
    @Test
    fun movingSaturatedArtworkMatchesAnUncroppedBlurInBothDockSurfaces() {
        for (intensity in listOf(0f, 0.25f)) {
            for ((height, blurScale) in listOf(56 to 1f, 60 to 0.72f)) {
                val appearance = LiquidGlassAppearance.fromIntensity(intensity)
                val edge = mutableFloatStateOf(80f)
                fun scene(reference: Boolean) = ImageComposeScene(320, 320) {
                    CompositionLocalProvider(
                        LocalLiquidGlassEnabled provides true,
                        LocalLiquidGlassAppearance provides appearance,
                    ) {
                        val backdrop = rememberBackdrop(Color.Black)
                        Box(Modifier.fillMaxSize()) {
                            Canvas(Modifier.fillMaxSize().layerBackdrop(backdrop)) {
                                drawRect(Color.Black)
                                drawRect(Color.Yellow, Offset(160f, edge.floatValue), Size(160f, 22f))
                                drawRect(Color.Red, Offset(160f, edge.floatValue + 22f), Size(160f, 22f))
                                drawRect(Color.Green, Offset(160f, edge.floatValue + 44f), Size(160f, 22f))
                            }
                            val surface = if (reference) {
                                Modifier.drawBackdrop(
                                    backdrop = backdrop,
                                    shape = { BottomDockShape },
                                    highlight = null,
                                    effects = {
                                        if (size.hasGlassEffectBounds()) {
                                            blur(8f * height / 60f * blurScale * appearance.blurMultiplier)
                                            colorControls(brightness = 0.05f * appearance.reflectionMultiplier,
                                                saturation = 0.75f)
                                            // Enough of the page to make crop-edge treatment irrelevant.
                                            padding = 128f
                                        }
                                    },
                                    onDrawSurface = { drawRect(Color.Black.copy(alpha = appearance.scrimAlpha(0.272f, true))) },
                                )
                            } else {
                                Modifier.drawInteractiveGlass(true, backdrop, 0.5f, BottomDockShape,
                                    interaction = null, blurScale = blurScale)
                            }
                            Box(Modifier.offset(20.dp, 140.dp).size(280.dp, height.dp).then(surface))
                        }
                    }
                }
                val actual = scene(false)
                val expected = scene(true)
                var time = 0L
                var worstError = 0
                try {
                    for (position in 75..215 step 4) {
                        edge.floatValue = position.toFloat()
                        repeat(2) {
                            actual.render(time).close()
                            expected.render(time).close()
                            time += 16_666_667L
                        }
                        val aImage = actual.render(time)
                        val eImage = expected.render(time)
                        val a = Bitmap.makeFromImage(aImage)
                        val e = Bitmap.makeFromImage(eImage)
                        try {
                            for (y in 142 until 140 + height - 2 step 4) {
                                val ac = a.getColor(230, y)
                                val ec = e.getColor(230, y)
                                for (shift in listOf(0, 8, 16)) {
                                    worstError = maxOf(worstError, abs(((ac ushr shift) and 255) - ((ec ushr shift) and 255)))
                                }
                            }
                        } finally {
                            a.close(); e.close(); aImage.close(); eImage.close()
                        }
                        time += 16_666_667L
                    }
                    assertTrue(worstError <= 5,
                        "Crop edge changes the scrolling glass: intensity=$intensity height=$height error=$worstError")
                } finally {
                    actual.close(); expected.close()
                }
            }
        }
    }
}
