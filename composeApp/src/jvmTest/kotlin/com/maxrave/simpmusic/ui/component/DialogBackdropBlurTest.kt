@file:OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)

package com.maxrave.simpmusic.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.runBlocking
import org.jetbrains.skia.Bitmap
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DialogBackdropBlurTest {
    @Test
    fun nativeWindowBlurHandsOffToFallbackWithoutDoubleBlurOrStaleState() = runBlocking<Unit> {
        val origins = DialogOriginTracker()
        val parent = DialogMotionState(null)
        val child = DialogMotionState(null, parent)
        origins.attach(parent)
        origins.attach(child)
        parent.progress.snapTo(1f)
        child.progress.snapTo(.4f)
        parent.nativeBackdropBlur = true
        child.nativeBackdropBlur = true
        // Native windows already blur their ancestors; the Compose layer must stay untouched.
        assertEquals(0f, origins.backgroundProgress(null))
        assertEquals(0f, origins.backgroundProgress(parent))
        child.nativeBackdropBlur = false
        // E.g. battery saver disables native blur while the child is part way through its spring.
        assertEquals(.4f, origins.backgroundProgress(null))
        assertEquals(.4f, origins.backgroundProgress(parent))
        assertEquals(0f, origins.backgroundProgress(child))
        parent.nativeBackdropBlur = false
        assertEquals(1f, origins.backgroundProgress(null))
        origins.detach(child)
        assertEquals(0f, origins.backgroundProgress(parent))
        origins.detach(parent)
        assertEquals(0f, origins.backgroundProgress(null))
    }

    @Test
    fun backgroundSoftensAndRestoresWhileDialogStaysSharpWithoutRemeasuring() = runBlocking<Unit> {
        for ((width, height) in listOf(450 to 900, 900 to 450)) for (sheetWindow in listOf(false, true)) {
            val origins = DialogOriginTracker()
            var motion: DialogMotionState? = null
            var compositions = 0
            var layouts = 0
            val scene = ImageComposeScene(width, height) {
                CompositionLocalProvider(LocalDialogOrigins provides origins) {
                    MaterialTheme(colorScheme = darkColorScheme()) {
                        Box(Modifier.fillMaxSize()) {
                            val backdrop = if (sheetWindow) {
                                Modifier.dialogMotionSource(clearPreviousOrigin = true)
                            } else {
                                Modifier.dialogBackdropBlur()
                            }
                            Box(Modifier.fillMaxSize().then(backdrop).background(Color(0xFF202026))
                                .drawBehind { drawRect(Color.White, Offset(80f, 40f), Size(20f, 180f)) }
                                .layout { measurable, constraints ->
                                    layouts++
                                    val child = measurable.measure(constraints)
                                    layout(child.width, child.height) { child.place(0, 0) }
                                }) { SideEffect { compositions++ } }
                            AppDialogMotion({}) { state, _ ->
                                motion = state
                                Dialog({}, properties = animatedDialogProperties(DialogProperties())) {
                                    Box(Modifier.fillMaxSize().dialogBackdropBlur(), contentAlignment = Alignment.Center) {
                                        DialogWindowScrim({ state.progress.value })
                                        DialogMotionSurface(state, Modifier.width(300.dp).height(260.dp)) {
                                            Box(Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
                                                Box(Modifier.width(20.dp).height(120.dp).background(Color.White))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            var time = 0L
            fun render() = scene.render(time.also { time += 16_666_667L })
            suspend fun pixels(progress: Float): Bitmap {
                motion!!.progress.snapTo(progress)
                render().close()
                return render().use { Bitmap.makeFromImage(it) }
            }
            fun red(bitmap: Bitmap, x: Int, y: Int) = bitmap.getColor(x, y) shr 16 and 255
            try {
                repeat(4) { render().close() }
                val initialCompositions = compositions
                val initialLayouts = layouts
                var clearCenter = 0
                var clearOutside = 0
                pixels(0f).use {
                    clearCenter = red(it, 90, 100)
                    clearOutside = red(it, 68, 100)
                }
                var midwayCenter = 0
                pixels(0.25f).use { midwayCenter = red(it, 90, 100) }
                pixels(1f).use {
                    assertTrue(red(it, 90, 100) < midwayCenter && midwayCenter < clearCenter,
                        "Backdrop must soften progressively ($width x $height, sheet=$sheetWindow)")
                    assertTrue(red(it, 68, 100) > clearOutside + 5, "Blur must spread the background feature")
                    assertTrue(red(it, width / 2, height / 2) > 245, "Foreground must remain sharp")
                    assertTrue(red(it, width / 2 - 12, height / 2) < 5, "Foreground edge must remain sharp")
                }
                // Reverse through the same frame, then remove the render effect completely.
                pixels(0.25f).use { assertEquals(midwayCenter, red(it, 90, 100)) }
                pixels(0f).use {
                    assertEquals(clearCenter, red(it, 90, 100))
                    assertEquals(clearOutside, red(it, 68, 100))
                }
                assertEquals(initialCompositions, compositions, "Blur must not recompose the background")
                assertEquals(initialLayouts, layouts, "Blur must not remeasure the background")
            } finally {
                scene.close()
                assertEquals(0f, origins.backgroundProgress(null), "Disposal must release the backdrop")
            }
        }
    }

    @Test
    fun nestedAndAbruptDismissalsReleaseOnlyTheirOwnBlur() = runBlocking<Unit> {
        val origins = DialogOriginTracker()
        val parentVisible = mutableStateOf(true)
        val childVisible = mutableStateOf(true)
        var parent: DialogMotionState? = null
        var child: DialogMotionState? = null
        val scene = ImageComposeScene(450, 900) {
            CompositionLocalProvider(LocalDialogOrigins provides origins) {
                if (parentVisible.value) AppDialogMotion({}) { parentState, _ ->
                    parent = parentState
                    if (childVisible.value) AppDialogMotion({}) { childState, _ -> child = childState }
                }
            }
        }
        var time = 0L
        fun frames() { repeat(3) { scene.render(time.also { time += 16_666_667L }).close() } }
        try {
            frames()
            parent!!.progress.snapTo(1f)
            child!!.progress.snapTo(0.4f)
            assertEquals(1f, origins.backgroundProgress(null))
            assertEquals(0.4f, origins.backgroundProgress(parent))
            assertEquals(0f, origins.backgroundProgress(child))
            childVisible.value = false
            frames()
            assertEquals(1f, origins.backgroundProgress(null), "Parent must retain its background blur")
            assertEquals(0f, origins.backgroundProgress(parent), "Parent surface must regain sharpness")
            childVisible.value = true
            frames()
            assertEquals(0f, child!!.progress.value, "A new dialog must start with a clear backdrop")
            child!!.progress.snapTo(1f)
            parentVisible.value = false
            frames()
            assertEquals(0f, origins.backgroundProgress(null), "Abrupt removal must never leave stale blur")
        } finally {
            scene.close()
        }
    }
}
