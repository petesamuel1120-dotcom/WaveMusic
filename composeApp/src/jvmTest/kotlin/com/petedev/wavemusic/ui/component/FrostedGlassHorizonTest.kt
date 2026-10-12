@file:OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)
package com.petedev.wavemusic.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntSize
import com.petedev.wavemusic.domain.manager.FrostedGlassHeaderStyle
import com.petedev.wavemusic.expect.ui.layerBackdrop
import com.petedev.wavemusic.expect.ui.rememberBackdrop
import com.petedev.wavemusic.ui.theme.LocalAppIsDarkTheme
import com.petedev.wavemusic.ui.theme.LocalLiquidGlassEnabled
import org.jetbrains.skia.Bitmap
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FrostedGlassHorizonTest {
    @Test fun horizonCoversBothDockRowsAndFadesWithoutAnUpperSeam() {
        Fixture().use { f ->
            val glass = f.pixels()
            f.visible.value = false
            val raw = f.pixels()
            for (y in 0..100) for (x in 24..296) {
                assertTrue(kotlin.math.abs((glass[y * 320 + x] and 255) - (raw[y * 320 + x] and 255)) <= 2,
                    "Material must join the page cleanly at its upper edge")
            }
            assertTrue(contrast(glass, 145) < contrast(raw, 145) - 12, "Mini-player row must receive frost")
            assertTrue(contrast(glass, 205) < contrast(glass, 145) - 12, "Navbar row must receive stronger frost")
        }
    }

    @Test fun allProfilesIncreaseInStrengthThroughTheDock() {
        val contrasts = listOf(FrostedGlassHeaderStyle.Moderate, FrostedGlassHeaderStyle.Balanced, FrostedGlassHeaderStyle.Fuller)
            .map { style -> Fixture(style).use { contrast(it.pixels(), 170) } }
        assertTrue(contrasts[0] > contrasts[1] + 5, "Balanced must sit below Moderate in contrast: $contrasts")
        assertTrue(contrasts[1] > contrasts[2] + 5, "Fuller must sit below Balanced in contrast: $contrasts")
    }

    @Test fun offAndLiquidGlassDisabledLeaveThePageUnchanged() {
        for ((style, enabled) in listOf(FrostedGlassHeaderStyle.Off to true, FrostedGlassHeaderStyle.Fuller to false)) {
            Fixture(style, enabled).use { f ->
                val disabled = f.pixels()
                f.visible.value = false
                assertTrue(disabled.contentEquals(f.pixels()))
            }
        }
    }

    @Test fun measuredDockHeightExtendsCoverageWithoutMovingTheScreenBottom() {
        Fixture().use { f ->
            f.dock.updateSize(IntSize(320, 56))
            val navOnly = f.pixels()
            f.dock.updateSize(IntSize(320, 112))
            val withPlayer = f.pixels()
            assertTrue(contrast(withPlayer, 145) < contrast(navOnly, 145) - 12, "Player appearance must extend the pane upward")
            for (x in 48..272) assertTrue(kotlin.math.abs((navOnly[238 * 320 + x] and 255) -
                (withPlayer[238 * 320 + x] and 255)) <= 2, "The strongest edge must remain at the screen bottom")
        }
    }

    private fun contrast(pixels: IntArray, y: Int): Int {
        val row = (48..272).map { pixels[y * 320 + it] and 255 }
        return row.max() - row.min()
    }

    private class Fixture(
        private val style: FrostedGlassHeaderStyle = FrostedGlassHeaderStyle.Balanced,
        private val enabled: Boolean = true,
    ) : AutoCloseable {
        val visible = mutableStateOf(true)
        val dock = FrostedHorizonDockState().apply { updateSize(IntSize(320, 112)) }
        private var time = 0L
        private val scene = ImageComposeScene(320, 240) {
            CompositionLocalProvider(LocalLiquidGlassEnabled provides enabled, LocalAppIsDarkTheme provides true) {
                val backdrop = rememberBackdrop(Color.Black)
                Box(Modifier.fillMaxSize()) {
                    Canvas(Modifier.fillMaxSize().layerBackdrop(backdrop)) {
                        drawRect(Color.Black)
                        for (x in 0..320 step 16) drawRect(Color.White, Offset(x.toFloat(), 0f), Size(8f, size.height))
                    }
                    FrostedGlassHorizonHost(backdrop, dock, style, visible.value,
                        Modifier.align(Alignment.BottomCenter).fillMaxWidth())
                }
            }
        }
        fun pixels(): IntArray {
            repeat(2) { scene.render(time.also { time += 16_666_667L }).close() }
            return scene.render(time.also { time += 16_666_667L }).use { image ->
                Bitmap.makeFromImage(image).use { bitmap -> IntArray(320 * 240) { bitmap.getColor(it % 320, it / 320) } }
            }
        }
        override fun close() { scene.close() }
    }
}
