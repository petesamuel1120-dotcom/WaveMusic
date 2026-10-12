@file:OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)

package com.petedev.wavemusic.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.petedev.wavemusic.expect.ui.layerBackdrop
import com.petedev.wavemusic.expect.ui.rememberBackdrop
import com.petedev.wavemusic.ui.theme.LocalAppIsDarkTheme
import com.petedev.wavemusic.ui.theme.LocalLiquidGlassEnabled
import org.jetbrains.skia.Bitmap
import kotlin.test.Test
import kotlin.test.assertTrue

class HeaderTransitionBackdropTest {
    @Test
    fun hostedLensSamplesTheMovingViewportEvenWithAPageLocalArgument() {
        val translation = mutableFloatStateOf(0f)
        val passViewport = mutableStateOf(false)
        var time = 0L
        val scene = ImageComposeScene(320, 160, density = Density(1f)) {
            CompositionLocalProvider(
                LocalLiquidGlassEnabled provides true,
                LocalAppIsDarkTheme provides true,
            ) {
                val viewport = rememberBackdrop(Color.Blue)
                val page = rememberBackdrop(Color.Red)
                Box(Modifier.fillMaxSize()) {
                    Box(Modifier.fillMaxSize().layerBackdrop(viewport)) {
                        Canvas(Modifier.fillMaxSize()) { drawRect(Color.Blue) }
                        // Only the parent GPU transform changes; the page capture stays red.
                        Box(Modifier.size(160.dp).graphicsLayer {
                            translationX = translation.floatValue
                        }) {
                            Canvas(Modifier.fillMaxSize().layerBackdrop(page)) { drawRect(Color.Red) }
                        }
                    }
                    CompositionLocalProvider(
                        LocalSharedHeaderHosted provides true,
                        LocalHeaderBackdrop provides viewport,
                    ) {
                        LiquidGlassContainer(
                            backdrop = if (passViewport.value) viewport else page,
                            modifier = Modifier.offset(48.dp, 40.dp).size(48.dp),
                            interactive = false,
                        ) {}
                    }
                }
            }
        }
        fun lensPixels(): IntArray {
            repeat(3) { scene.render(time.also { time += 16_666_667L }).close() }
            val image = scene.render(time.also { time += 16_666_667L })
            val bitmap = Bitmap.makeFromImage(image)
            return try {
                (54..88).flatMap { y -> (62..82).map { x -> bitmap.getColor(x, y) } }.toIntArray()
            } finally {
                bitmap.close()
                image.close()
            }
        }
        try {
            val before = lensPixels()
            translation.floatValue = 200f
            val moved = lensPixels()
            assertTrue(before.indices.any { before[it] != moved[it] },
                "Lens must refresh when the page moves only through a graphics-layer transform")
            passViewport.value = true
            val explicitViewport = lensPixels()
            assertTrue(moved.contentEquals(explicitViewport),
                "Hosted page-local arguments must sample the same live viewport as the host")
        } finally {
            scene.close()
        }
    }
}
