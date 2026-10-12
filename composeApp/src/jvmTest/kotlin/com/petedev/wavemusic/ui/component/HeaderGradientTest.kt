@file:OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)

package com.petedev.wavemusic.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.petedev.wavemusic.expect.ui.layerBackdrop
import com.petedev.wavemusic.expect.ui.rememberBackdrop
import com.petedev.wavemusic.ui.theme.LocalAppIsDarkTheme
import com.petedev.wavemusic.ui.theme.LocalLiquidGlassEnabled
import com.petedev.wavemusic.ui.theme.LocalFrostedGlassHeaderStyle
import com.petedev.wavemusic.domain.manager.FrostedGlassHeaderStyle
import org.jetbrains.skia.Bitmap
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HeaderGradientTest {
    @Test fun blurStrengthDecreasesBeforeAnyOpacityMaskIsApplied() {
        Fixture(fade = false).use { f ->
            val pixels = f.pixels()
            val top = contrast(pixels, 6)
            val middle = contrast(pixels, 50)
            val lower = contrast(pixels, 90)
            assertTrue(middle > top + 12, "Diffusion must decrease beneath the top: $top / $middle")
            assertTrue(lower > middle + 12, "Diffusion must keep decreasing: $middle / $lower")
        }
    }

    @Test fun lowerHeaderKeepsArtworkDetailAndItsBottomJoinsThePage() {
        for (style in FrostedGlassHeaderStyle.entries.filter { it.isEnabled }) Fixture(fade = true, style = style).use { f ->
            f.visible.value = false
            val raw = f.pixels()
            f.visible.value = true
            val header = f.pixels()
            assertTrue(contrast(header, 60) >= contrast(raw, 60) * 0.45f,
                "$style must preserve visible artwork detail beneath the controls")
            for (y in 103..150) for (x in 24..296) {
                val before = raw[y * 320 + x]
                val after = header[y * 320 + x]
                if (y >= 104) assertEquals(before, after, "Header painted beyond its bounds")
                else assertTrue(kotlin.math.abs((before and 255) - (after and 255)) <= 2,
                    "Visible line at the bottom of the header")
            }
        }
    }

    @Test fun balancedVisiblyFallsBetweenModerateAndFuller() {
        val contrastByStyle = listOf(FrostedGlassHeaderStyle.Moderate, FrostedGlassHeaderStyle.Balanced, FrostedGlassHeaderStyle.Fuller)
            .map { style -> Fixture(fade = true, style = style).use { contrast(it.pixels(), 50) } }
        assertTrue(contrastByStyle[0] >= contrastByStyle[1] + 5, "Balanced must soften more than Moderate: $contrastByStyle")
        assertTrue(contrastByStyle[1] >= contrastByStyle[2] + 5, "Fuller must soften more than Balanced: $contrastByStyle")
    }

    @Test fun offLeavesThePageUnchanged() {
        Fixture(fade = true, style = FrostedGlassHeaderStyle.Off).use { f ->
            val off = f.pixels()
            f.visible.value = false
            assertTrue(off.contentEquals(f.pixels()), "Off must draw no header material")
        }
    }

    @Test fun scrollingUnpinnedButtonsDoesNotMoveTheBackdrop() {
        Fixture(fade = true, controls = true).use { f ->
            f.headerState.setHeaderButtonsPinned(false)
            val expanded = f.pixels()
            f.headerState.updateHeaderOffset(-56f)
            val collapsed = f.pixels()
            assertTrue(expanded[55 * 320 + 290] != collapsed[55 * 320 + 290], "Unpinned controls must move")
            for (y in 0..103) for (x in 24..260) {
                assertEquals(expanded[y * 320 + x], collapsed[y * 320 + x], "Frost moved with the unpinned controls at $x/$y")
            }
        }
    }

    private fun contrast(pixels: IntArray, y: Int): Int {
        val row = (48..272).map { x -> pixels[y * 320 + x] and 255 }
        return row.max() - row.min()
    }

    private class Fixture(
        private val fade: Boolean,
        private val style: FrostedGlassHeaderStyle = FrostedGlassHeaderStyle.Default,
        private val controls: Boolean = false,
    ) : AutoCloseable {
        val visible = mutableStateOf(true)
        val headerState = SharedHeaderState()
        private var time = 0L
        private val scene = ImageComposeScene(width = 320, height = 180) {
            CompositionLocalProvider(LocalLiquidGlassEnabled provides true, LocalAppIsDarkTheme provides true,
                LocalFrostedGlassHeaderStyle provides style) {
                val backdrop = rememberBackdrop(Color.Black)
                Box(Modifier.fillMaxSize()) {
                    Canvas(Modifier.fillMaxSize().layerBackdrop(backdrop)) {
                        drawRect(Color.Black)
                        for (x in 0..320 step 16) drawRect(Color.White, Offset(x.toFloat(), 0f), Size(8f, size.height))
                    }
                    if (visible.value) {
                        if (fade) PersistentLiquidGlassHeaderBackdrop(backdrop, Modifier.fillMaxWidth().height(104.dp), Color.Black)
                        else Box(Modifier.fillMaxWidth().height(104.dp).graphicsLayer { clip = true }
                            .drawHeaderGlassBackdrop(backdrop, Color.Black))
                    }
                    if (controls) Canvas(Modifier.fillMaxWidth().height(104.dp).graphicsLayer {
                        translationY = headerState.headerOffsetPx
                    }) {
                        drawRect(Color.Red, Offset(280f, 40f), Size(24f, 28f))
                    }
                }
            }
        }
        fun pixels(): IntArray {
            repeat(2) { scene.render(time.also { time += 16_666_667L }).close() }
            return scene.render(time.also { time += 16_666_667L }).use { image ->
                Bitmap.makeFromImage(image).use { bitmap -> IntArray(320 * 180) { bitmap.getColor(it % 320, it / 320) } }
            }
        }
        override fun close() { scene.close() }
    }
}
