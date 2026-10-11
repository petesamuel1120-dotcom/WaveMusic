@file:OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)

package com.maxrave.simpmusic.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.dp
import com.maxrave.simpmusic.ui.theme.LocalAdvancedDialogAnimations
import kotlinx.coroutines.runBlocking
import org.jetbrains.skia.Bitmap
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DialogChoreographyTest {
    @Test
    fun sectionsRevealInOrderAndReverseWithoutRecomposingOrRemeasuring() = runBlocking<Unit> {
        val motion = DialogMotionState(null)
        var compositions = 0
        var layouts = 0
        val scene = ImageComposeScene(120, 180) {
            CompositionLocalProvider(
                LocalAdvancedDialogAnimations provides true,
                LocalDialogMotionOwner provides motion,
            ) {
                SideEffect { compositions++ }
                Column(Modifier.fillMaxSize().background(Color.Black).layout { measurable, constraints ->
                    layouts++
                    val measured = measurable.measure(constraints)
                    layout(measured.width, measured.height) { measured.place(0, 0) }
                }) {
                    for (stage in listOf(DialogContentStage.Title, DialogContentStage.Body, DialogContentStage.Actions)) {
                        DialogMotionContent(stage) { Box(Modifier.size(100.dp, 50.dp).background(Color.White)) }
                    }
                }
            }
        }
        var time = 0L
        fun render() = scene.render(time.also { time += 16_666_667L })
        fun brightness(): List<Int> = render().use { image ->
            Bitmap.makeFromImage(image).use { bitmap ->
                listOf(25, 75, 125).map { y -> bitmap.getColor(50, y) and 255 }
            }
        }
        try {
            repeat(3) { render().close() }
            val initialCompositions = compositions
            val initialLayouts = layouts
            motion.progress.snapTo(0.4f)
            val opening = brightness()
            assertTrue(opening[0] > opening[1] && opening[1] > opening[2] && opening[2] > 0,
                "The title must lead the body and actions: $opening")
            motion.progress.snapTo(1f)
            assertEquals(listOf(255, 255, 255), brightness(), "Settled content must be fully visible")
            motion.progress.snapTo(0.4f)
            assertEquals(opening, brightness(), "Closing must reverse the same choreography")
            motion.progress.snapTo(0f)
            assertEquals(listOf(0, 0, 0), brightness(), "Sections must fade completely on return")
            assertEquals(initialCompositions, compositions)
            assertEquals(initialLayouts, layouts)
        } finally { scene.close() }
    }

    @Test
    fun disabledAnimationsAndContentOutsideDialogsKeepTheirNormalPixels() = runBlocking<Unit> {
        val motion = DialogMotionState(null)
        fun scene(enabled: Boolean, owner: DialogMotionState?) = ImageComposeScene(120, 180) {
            CompositionLocalProvider(
                LocalAdvancedDialogAnimations provides enabled,
                LocalDialogMotionOwner provides owner,
            ) {
                Column(Modifier.fillMaxSize().background(Color.Black)) {
                    DialogMotionContent(DialogContentStage.Title) {
                        Box(Modifier.size(100.dp, 50.dp).background(Color.Red))
                    }
                    Box(Modifier.size(100.dp, 50.dp).dialogContentMotion(DialogContentStage.Option, 6)
                        .background(Color.Green))
                    DialogMotionContent(DialogContentStage.Actions) {
                        Box(Modifier.size(100.dp, 50.dp).background(Color.Blue))
                    }
                }
            }
        }
        fun pixels(scene: ImageComposeScene): IntArray {
            repeat(3) { scene.render(it * 16_666_667L).close() }
            return scene.render(50_000_001L).use { image ->
                Bitmap.makeFromImage(image).use { bitmap ->
                    IntArray(bitmap.width * bitmap.height) { bitmap.getColor(it % bitmap.width, it / bitmap.width) }
                }
            }
        }
        val disabled = scene(false, motion)
        val outside = scene(true, null)
        motion.progress.snapTo(1f)
        val settled = scene(true, motion)
        try {
            val expected = pixels(outside)
            assertContentEquals(expected, pixels(settled), "Settled stages must preserve the original layout")
            motion.progress.snapTo(0f)
            assertContentEquals(expected, pixels(disabled), "The setting must disable every section transform")
        } finally {
            disabled.close()
            outside.close()
            settled.close()
        }
    }
}
