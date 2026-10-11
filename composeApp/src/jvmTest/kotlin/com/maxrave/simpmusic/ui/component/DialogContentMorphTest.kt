@file:OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)

package com.maxrave.simpmusic.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.runBlocking
import org.jetbrains.skia.Bitmap
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DialogContentMorphTest {
    @Test
    fun openingAndClosingKeepAllFourContentCornersVisibleWithoutRemeasuring() = runBlocking<Unit> {
        for (source in listOf(Rect(16f, 410f, 196f, 466f), Rect(20f, 150f, 430f, 230f))) {
            val state = DialogMotionState(DialogMotionOrigin(source))
            var compositions = 0
            var layouts = 0
            val scene = ImageComposeScene(450, 900) {
                MaterialTheme(colorScheme = darkColorScheme()) {
                Box(Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
                    DialogMotionSurface(state, Modifier.width(400.dp).height(560.dp)) {
                        SideEffect { compositions++ }
                        Box(Modifier.fillMaxSize().layout { measurable, constraints ->
                            layouts++
                            val item = measurable.measure(constraints)
                            layout(item.width, item.height) { item.place(0, 0) }
                        }.padding(28.dp)) {
                            Box(Modifier.align(Alignment.TopStart).size(20.dp).background(Color.Red))
                            Box(Modifier.align(Alignment.TopEnd).size(20.dp).background(Color.Green))
                            Box(Modifier.align(Alignment.BottomStart).size(20.dp).background(Color.Blue))
                            Box(Modifier.align(Alignment.BottomEnd).size(20.dp).background(Color.Yellow))
                        }
                    }
                }
                }
            }
            var time = 0L
            fun render() = scene.render(time.also { time += 16_666_667L })
            try {
                repeat(3) { render().close() }
                val initialCompositions = compositions
                val initialLayouts = layouts
                // Sample the same visible content on opening and reverse motion.
                for (fraction in listOf(.25f, .4f, .65f, 1f, .65f, .4f, .25f)) {
                    state.progress.snapTo(fraction)
                    val pixels = IntArray(4)
                    render().use { image ->
                        Bitmap.makeFromImage(image).use { bitmap ->
                            for (y in 0 until bitmap.height) for (x in 0 until bitmap.width) {
                                val color = bitmap.getColor(x, y)
                                val red = color shr 16 and 255
                                val green = color shr 8 and 255
                                val blue = color and 255
                                // Detect the marker's contrast, including its fade over the
                                // themed surface; faded primaries do not have zero other channels.
                                when {
                                    red - maxOf(green, blue) > 30 -> pixels[0]++
                                    green - maxOf(red, blue) > 30 -> pixels[1]++
                                    blue - maxOf(red, green) > 30 -> pixels[2]++
                                    minOf(red, green) - blue > 30 && kotlin.math.abs(red - green) < 10 -> pixels[3]++
                                }
                            }
                        }
                    }
                    assertTrue(pixels.all { it >= 4 }, "A content corner was cropped at $fraction from $source: ${pixels.toList()}")
                    assertTrue(pixels.max() - pixels.min() <= 20, "Content corners must scale together: ${pixels.toList()}")
                }
                assertEquals(initialCompositions, compositions, "Motion must not recompose content")
                assertEquals(initialLayouts, layouts, "Motion must not remeasure content")
            } finally {
                scene.close()
            }
        }
    }
}
