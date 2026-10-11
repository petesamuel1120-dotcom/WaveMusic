@file:OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)

package com.maxrave.simpmusic.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.drawPlainBackdrop
import com.maxrave.simpmusic.expect.ui.PlatformBackdrop
import com.maxrave.simpmusic.expect.ui.layerBackdrop
import com.maxrave.simpmusic.expect.ui.rememberBackdrop
import com.maxrave.simpmusic.ui.theme.LiquidGlassAppearance
import com.maxrave.simpmusic.ui.theme.LocalLiquidGlassAppearance
import com.maxrave.simpmusic.ui.theme.LocalLiquidGlassEnabled
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking
import org.jetbrains.skia.Bitmap

class GlassCaptureTest {
    @Test
    fun lazyListWhiteRowsAndTheirCapturesMoveTogetherOnTheFirstFrame() {
        val list = LazyListState()
        val scene = ImageComposeScene(400, 320) {
            val backdrop = rememberBackdrop(Color.Black)
            Box(Modifier.fillMaxSize().background(Color.Black)) {
                LazyColumn(Modifier.fillMaxSize().layerBackdrop(backdrop), state = list) {
                    items(40) { row ->
                        Canvas(Modifier.size(400.dp, 48.dp)) {
                            // Mirror the row: the left half remains visible, the right half
                            // is occluded by glass. Compare the capture to actual page pixels.
                            for (x in listOf(40f, 240f)) {
                                drawRect(if (row % 2 == 0) Color.White else Color.LightGray,
                                    Offset(x, 4f), Size(120f, 22f))
                                drawRect(Color.White, Offset(x, 32f), Size(120f, 2f))
                            }
                        }
                    }
                }
                for (y in listOf(100, 220)) {
                    Box(Modifier.offset(220.dp, y.dp).size(160.dp, 60.dp).drawPlainBackdrop(
                        backdrop = backdrop,
                        shape = { BottomDockShape },
                        effects = {},
                    ))
                }
            }
        }
        var time = 0L
        fun renderAndCompare() {
            val image = scene.render(time.also { time += 16_666_667L })
            val bitmap = Bitmap.makeFromImage(image)
            try {
                for (top in listOf(100, 220)) {
                    for (y in top + 2 until top + 58) {
                        assertEquals(bitmap.getColor(100, y), bitmap.getColor(300, y),
                            "Lazy-list source and capture disagree at y=$y")
                    }
                }
            } finally {
                bitmap.close()
                image.close()
            }
        }
        try {
            renderAndCompare()
            for (offset in (0..180 step 3).toList() + (180 downTo 0 step 5).toList()) {
                runBlocking { list.scrollToItem(offset / 48, offset % 48) }
                renderAndCompare()
            }
        } finally {
            scene.close()
        }
    }

    @Test
    fun reattachedSourceUsesNewPixelsImmediately() {
        val present = mutableStateOf(true)
        val color = mutableStateOf(Color.White)
        val scene = ImageComposeScene(160, 100) {
            val backdrop = rememberBackdrop(Color.Black)
            Box(Modifier.fillMaxSize().background(Color.Black)) {
                if (present.value) {
                    Canvas(Modifier.fillMaxSize().layerBackdrop(backdrop)) { drawRect(color.value) }
                }
                Box(Modifier.offset(20.dp, 20.dp).size(120.dp, 60.dp).drawPlainBackdrop(
                    backdrop = backdrop,
                    shape = { BottomDockShape },
                    effects = {},
                ))
            }
        }
        var time = 0L
        fun center(): Int {
            val image = scene.render(time.also { time += 16_666_667L })
            val bitmap = Bitmap.makeFromImage(image)
            try {
                return bitmap.getColor(80, 50)
            } finally {
                bitmap.close()
                image.close()
            }
        }
        try {
            assertEquals(Color.White.toArgb(), center())
            present.value = false
            assertEquals(Color.Black.toArgb(), center(), "Detached sources must not retain white pixels")
            color.value = Color.Red
            present.value = true
            assertEquals(Color.Red.toArgb(), center(), "Reattachment must use the new content on its first frame")
        } finally {
            scene.close()
        }
    }

    @Test
    fun movingWhiteArtworkAndTextUpdateBothFrostedSurfacesInTheSameFrame() {
        for (density in listOf(1f, 2.75f)) {
            for (intensity in listOf(0f, 0.25f)) {
                val appearance = LiquidGlassAppearance.fromIntensity(intensity)
                val position = mutableFloatStateOf(0f)
                val width = (320 * density).toInt()
                val height = (400 * density).toInt()
                fun scene(layered: Boolean) = ImageComposeScene(width, height, density = Density(density)) {
                    CompositionLocalProvider(
                        LocalLiquidGlassEnabled provides true,
                        LocalLiquidGlassAppearance provides appearance,
                    ) {
                        val backdrop = rememberBackdrop(Color.Black)
                        val textMeasurer = rememberTextMeasurer()
                        val text = remember(textMeasurer) {
                            textMeasurer.measure("Science — WHITE TEXT", TextStyle(color = Color.White, fontSize = 18.sp))
                        }
                        fun DrawScope.pattern(y: Float) {
                            // Opaque white artwork, narrow bright edges and antialiased glyphs
                            // enter and leave the capsules' blur kernels in both directions.
                            drawRect(Color.White, Offset(160f * density, y), Size(160f * density, 44f * density))
                            drawRect(Color.White, Offset(40f * density, y + 66f * density), Size(220f * density, 2f * density))
                            drawText(text, topLeft = Offset(20f * density, y + 86f * density))
                        }
                        Box(Modifier.fillMaxSize()) {
                            Box(Modifier.fillMaxSize().layerBackdrop(backdrop).background(Color.Black)) {
                                Canvas(Modifier.fillMaxSize().then(
                                    if (layered) Modifier.graphicsLayer { translationY = position.floatValue * density }
                                    else Modifier,
                                )) {
                                    pattern(if (layered) 0f else position.floatValue * density)
                                }
                            }
                            for ((y, surfaceHeight, blurScale) in listOf(Triple(140, 56, 1f), Triple(240, 60, 0.72f))) {
                                Box(Modifier.offset(20.dp, y.dp).size(280.dp, surfaceHeight.dp).drawInteractiveGlass(
                                    true, backdrop, 0.5f, BottomDockShape, interaction = null, blurScale = blurScale,
                                ))
                            }
                        }
                    }
                }
                val actual = scene(true)
                val expected = scene(false)
                var time = 0L
                try {
                    // No extra settling renders: a late/stale capture must fail on its first frame.
                    for (y in (0..310 step 7).toList() + (310 downTo 0 step 9).toList()) {
                        position.floatValue = y.toFloat()
                        val actualImage = actual.render(time)
                        val expectedImage = expected.render(time)
                        time += 16_666_667L
                        val a = Bitmap.makeFromImage(actualImage)
                        val e = Bitmap.makeFromImage(expectedImage)
                        try {
                            for ((top, bottom) in listOf(144 to 192, 244 to 296)) {
                                for (px in 60..260 step 20) {
                                    for (py in top..bottom step 6) {
                                        val ac = a.getColor((px * density).toInt(), (py * density).toInt())
                                        val ec = e.getColor((px * density).toInt(), (py * density).toInt())
                                        for (shift in listOf(0, 8, 16)) {
                                            val error = abs(((ac ushr shift) and 255) - ((ec ushr shift) and 255))
                                            assertTrue(error <= 5,
                                                "White content capture is stale: density=$density intensity=$intensity y=$y pixel=$px,$py error=$error")
                                        }
                                    }
                                }
                            }
                        } finally {
                            a.close(); e.close(); actualImage.close(); expectedImage.close()
                        }
                    }
                } finally {
                    actual.close(); expected.close()
                }
            }
        }
    }

    @Test
    fun pageAndTwoSiblingSurfacesUseOneContentRecording() {
        var draws = 0
        val color = mutableStateOf(Color.White)
        val scene = ImageComposeScene(240, 200) {
            val backdrop = rememberBackdrop(Color.Black)
            Box(Modifier.fillMaxSize()) {
                Canvas(Modifier.fillMaxSize().layerBackdrop(backdrop)) {
                    draws++
                    drawRect(color.value)
                }
                for (y in listOf(40, 120)) {
                    Box(Modifier.offset(40.dp, y.dp).size(160.dp, 40.dp).drawPlainBackdrop(
                        backdrop = backdrop,
                        shape = { BottomDockShape },
                        effects = {},
                    ))
                }
            }
        }
        var time = 0L
        try {
            for (frameColor in listOf(Color.White, Color.Black, Color.Red, Color.White)) {
                color.value = frameColor
                val beforeDraws = draws
                val image = scene.render(time.also { time += 16_666_667L })
                val bitmap = Bitmap.makeFromImage(image)
                try {
                    assertEquals(1, draws - beforeDraws, "Page content must be drawn once per update")
                    val page = bitmap.getColor(120, 20)
                    assertEquals(page, bitmap.getColor(120, 60), "Mini-player must sample this frame")
                    assertEquals(page, bitmap.getColor(120, 140), "Navigation must sample this frame")
                } finally {
                    bitmap.close()
                    image.close()
                }
            }
        } finally {
            scene.close()
        }
    }

    @Test
    fun changingFallbackColorKeepsTheSourceAttachedAndUpdatesTheFirstFrame() {
        val color = mutableStateOf(Color.Green)
        var currentBackdrop: PlatformBackdrop? = null
        val scene = ImageComposeScene(160, 100) {
            val backdrop = rememberBackdrop(color.value)
            currentBackdrop = backdrop
            Box(Modifier.fillMaxSize()) {
                Box(Modifier.matchParentSize().layerBackdrop(backdrop))
                Box(Modifier.offset(20.dp, 20.dp).size(120.dp, 60.dp).drawPlainBackdrop(
                    backdrop = backdrop,
                    shape = { BottomDockShape },
                    effects = {},
                ))
            }
        }
        var time = 0L
        fun center(): Int {
            val image = scene.render(time.also { time += 16_666_667L })
            val bitmap = Bitmap.makeFromImage(image)
            try {
                return bitmap.getColor(80, 50)
            } finally {
                bitmap.close()
                image.close()
            }
        }
        try {
            center()
            val initialBackdrop = currentBackdrop
            color.value = Color.Blue
            val pixel = center()
            assertSame(initialBackdrop, currentBackdrop, "A tint change must not replace capture identity")
            assertTrue((pixel and 255) > 240 && ((pixel ushr 8) and 255) < 15,
                "The first changed frame must contain the new fallback color")
        } finally {
            scene.close()
        }
    }
}
