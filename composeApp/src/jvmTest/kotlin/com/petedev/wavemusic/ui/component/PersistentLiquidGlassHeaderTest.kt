@file:OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)

package com.petedev.wavemusic.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.petedev.wavemusic.expect.ui.layerBackdrop
import com.petedev.wavemusic.expect.ui.rememberBackdrop
import com.petedev.wavemusic.ui.theme.LiquidGlassAppearance
import com.petedev.wavemusic.ui.theme.LocalAppIsDarkTheme
import com.petedev.wavemusic.ui.theme.LocalLiquidGlassAppearance
import com.petedev.wavemusic.ui.theme.LocalLiquidGlassEnabled
import org.jetbrains.skia.Bitmap
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PersistentLiquidGlassHeaderTest {
    @Test
    fun fixedPaneStaysInPlaceWhenHeaderControlsHide() {
        Fixture().use { fixture ->
            val visible = fixture.pixels()
            for (offset in listOf(-19f, -38f, -76f, 0f)) {
                fixture.header.updateHeaderOffset(offset)
                val hidden = fixture.pixels()
                for (y in 2..74) for (x in 12..200) {
                    assertEquals(visible[y * 320 + x], hidden[y * 320 + x], "Pane moved with controls at $offset")
                }
            }
        }
    }

    @Test
    fun bottomFadeDissolvesTheMaterialWithoutPaintingBelowTheHeader() {
        Fixture().use { fixture ->
            fixture.pane.value = false
            val raw = fixture.pixels()
            fixture.pane.value = true
            val glass = fixture.pixels()
            assertTrue(distance(raw[20 * 320 + 160], glass[20 * 320 + 160]) > 12)
            assertTrue(distance(raw[75 * 320 + 160], glass[75 * 320 + 160]) <= 4,
                "Bottom edge must blend back into the page")
            for (y in 76..150) for (x in 0 until 320) {
                assertEquals(raw[y * 320 + x], glass[y * 320 + x], "Glass painted below the header")
            }
        }
    }

    @Test
    fun disablingGlassRemovesThePaneEvenWhenItsPreferenceRemainsOn() {
        Fixture().use { fixture ->
            fixture.pane.value = false
            fixture.glass.value = false
            val raw = fixture.pixels()
            fixture.pane.value = true
            assertTrue(raw.contentEquals(fixture.pixels()))
        }
    }

    @Test
    fun paneKeepsHeaderControlReflectionsAndSourcePixelsUnchanged() {
        for (isDark in listOf(true, false)) for (intensity in listOf(0.25f, 1f)) {
            Fixture(isDark = isDark, initialIntensity = intensity).use { fixture ->
                fixture.pane.value = false
                val controlOnly = fixture.pixels()
                fixture.pane.value = true
                val withPane = fixture.pixels()
                // Interior and rim of the existing 48dp control, excluding transparent corners.
                for (y in 27..62) for (x in 271..281) {
                    assertEquals(controlOnly[y * 320 + x], withPane[y * 320 + x],
                        "Header control reflection changed: dark=$isDark intensity=$intensity at $x,$y")
                }
                // Repeated captures cannot accumulate the pane into its own source.
                repeat(8) {
                    assertTrue(withPane.contentEquals(fixture.pixels()), "Backdrop feedback or unstable capture")
                }
            }
        }
    }

    @Test
    fun liveIntensityChangesTheTopMaterial() {
        Fixture().use { fixture ->
            val frosted = fixture.pixels()
            fixture.intensity.floatValue = 1f
            val liquid = fixture.pixels()
            val changed = (1..34).sumOf { y ->
                (5..200).count { x -> distance(frosted[y * 320 + x], liquid[y * 320 + x]) > 4 }
            }
            assertTrue(changed > 300, "The existing intensity slider must visibly change the persistent pane")
        }
    }

    @Test
    fun movingFineWhiteStrokesDoNotPulseAcrossFrostedIntensitiesAndScrollReversals() {
        for (isDark in listOf(true, false)) for (density in listOf(1f, 2.75f))
            for (intensity in listOf(0f, 0.25f, 0.375f, 0.5f, 0.75f, 1f)) {
            Fixture(scale = density, isDark = isDark, initialIntensity = intensity, fineStrokes = true).use { fixture ->
                val sampleCount = fixture.sheetSamples().size
                val low = IntArray(sampleCount) { 255 }
                val high = IntArray(sampleCount)
                for (step in (0..32).toList() + (32 downTo 0).toList()) {
                    fixture.phase.floatValue = step / 8f
                    val samples = fixture.sheetSamples()
                    for (index in samples.indices) {
                        val value = samples[index] and 255
                        low[index] = minOf(low[index], value)
                        high[index] = maxOf(high[index], value)
                    }
                }
                for (index in low.indices) assertTrue(high[index] - low[index] <= 4,
                    "Top glass pulses: dark=$isDark density=$density intensity=$intensity sample=$index range=${low[index]}..${high[index]}")
            }
        }
    }

    @Test
    fun fullWidthSheetHasNoSharpFrameBehindStatusIconsAtAnyIntensity() {
        for (persistent in listOf(true, false)) for (isDark in listOf(true, false))
            for (intensity in listOf(0f, 0.25f, 0.5f, 0.75f, 1f)) {
            Fixture(isDark = isDark, initialIntensity = intensity, solidColor = Color(0xFF526A83), persistent = persistent).use { fixture ->
                val pixels = fixture.pixels()
                // A softly varying blur at the viewport boundary is fine. A reflective outline
                // or lens cutout creates a discontinuity instead of continuous status-bar coverage.
                for (y in 1..28) for (x in listOf(0, 1, 2, 3, 4, 315, 316, 317, 318)) {
                    assertTrue(distance(pixels[y * 320 + x], pixels[y * 320 + x + 1]) <= 15,
                        "Visible side frame: dark=$isDark intensity=$intensity at $x,$y")
                }
                for (y in 0..6) for (x in 8..311) {
                    assertTrue(distance(pixels[y * 320 + x], pixels[(y + 1) * 320 + x]) <= 15,
                        "Visible top frame: dark=$isDark intensity=$intensity at $x,$y")
                }
            }
        }
    }

    private fun distance(first: Int, second: Int) = listOf(0, 8, 16).sumOf {
        kotlin.math.abs(((first ushr it) and 255) - ((second ushr it) and 255))
    }

    internal class Fixture(
        val scale: Float = 1f,
        isDark: Boolean = true,
        initialIntensity: Float = 0.25f,
        fineStrokes: Boolean = false,
        solidColor: Color? = null,
        persistent: Boolean = true,
    ) : AutoCloseable {
        val width = (320 * scale).toInt()
        private val height = (180 * scale).toInt()
        val pane = mutableStateOf(true)
        val glass = mutableStateOf(true)
        val intensity = mutableFloatStateOf(initialIntensity)
        val phase = mutableFloatStateOf(0f)
        val header = SharedHeaderState()
        private var time = 0L
        private val scene = ImageComposeScene(width, height, density = Density(scale)) {
            CompositionLocalProvider(
                LocalLiquidGlassEnabled provides glass.value,
                LocalLiquidGlassAppearance provides LiquidGlassAppearance.fromIntensity(intensity.floatValue),
                LocalAppIsDarkTheme provides isDark,
            ) {
                val backdrop = rememberBackdrop(if (isDark) Color.Black else Color.White)
                Box(Modifier.fillMaxSize()) {
                    Canvas(Modifier.fillMaxSize().layerBackdrop(backdrop)) {
                        if (solidColor != null) {
                            drawRect(solidColor)
                        } else if (fineStrokes) {
                            drawRect(Color.Black)
                            for (row in -4..(size.height.toInt() + 4) step 4) {
                                drawRect(Color.White, Offset(0f, row + phase.floatValue), Size(size.width, 1f))
                            }
                        } else {
                            drawRect(Brush.horizontalGradient(listOf(Color(0xFF154A78), Color(0xFF8A3470), Color(0xFFF9B365))))
                            for (row in 0..180 step 16) {
                                drawRect(Color.White, Offset(0f, row.dp.toPx()), Size(size.width, 3.dp.toPx()))
                            }
                        }
                    }
                    if (pane.value) {
                        if (persistent) PersistentLiquidGlassHeaderBackdrop(
                            backdrop, Modifier.fillMaxWidth().height(76.dp),
                            tint = if (isDark) Color.Black else Color.White,
                        ) else FrostedHeader(
                            hazeState = androidx.compose.runtime.remember { dev.chrisbanes.haze.HazeState() },
                            backdrop = backdrop,
                            modifier = Modifier.fillMaxWidth().height(76.dp),
                            tint = if (isDark) Color.Black else Color.White,
                        ) {}
                    }
                    if (!fineStrokes && solidColor == null) Box(Modifier.fillMaxWidth().height(76.dp).graphicsLayer {
                        translationY = header.headerOffsetPx
                    }) {
                        LiquidGlassContainer(backdrop, Modifier.offset(252.dp, 20.dp).size(48.dp), interactive = false) {}
                    }
                }
            }
        }

        fun pixels(): IntArray {
            // Settle invalidation/layout separately from the sampled frame.
            repeat(2) { scene.render(time.also { time += 16_666_667L }).close() }
            val image = scene.render(time.also { time += 16_666_667L })
            val bitmap = Bitmap.makeFromImage(image)
            return try {
                IntArray(width * height) { bitmap.getColor(it % width, it / width) }
            } finally {
                bitmap.close()
                image.close()
            }
        }

        fun sheetSamples(): IntArray {
            repeat(2) { scene.render(time.also { time += 16_666_667L }).close() }
            val image = scene.render(time.also { time += 16_666_667L })
            val bitmap = Bitmap.makeFromImage(image)
            return try {
                // Include the status-bar area and both viewport edges; center-only sampling
                // missed the square lens's border/corner artifacts at high intensity.
                listOf(1, 4, 10, 24, 35).flatMap { y ->
                    listOf(0, 2, 8, 160, 312, 318, 319).map { x ->
                        bitmap.getColor((x * scale).toInt(), (y * scale).toInt())
                    }
                }.toIntArray()
            } finally {
                bitmap.close()
                image.close()
            }
        }

        fun savePreview(path: String) {
            repeat(2) { scene.render(time.also { time += 16_666_667L }).close() }
            scene.render(time.also { time += 16_666_667L }).use { image ->
                image.encodeToData()?.use { java.io.File(path).writeBytes(it.bytes) }
            }
        }

        override fun close() = scene.close()
    }
}
