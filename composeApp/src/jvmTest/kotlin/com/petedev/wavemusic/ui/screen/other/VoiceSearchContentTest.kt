@file:OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)

package com.petedev.wavemusic.ui.screen.other

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import com.petedev.wavemusic.expect.ui.VoiceSearchState
import com.petedev.wavemusic.ui.theme.LocalAppColorScheme
import com.petedev.wavemusic.ui.theme.LocalAppIsDarkTheme
import org.jetbrains.skia.Bitmap
import org.jetbrains.compose.resources.getString
import kotlinx.coroutines.runBlocking
import com.petedev.wavemusic.resources.Res
import com.petedev.wavemusic.resources.voice_example
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VoiceSearchContentTest {
    @Test
    fun listeningAnimatesThenRetryBecomesGrayAndStillInEveryThemeAndOrientation() {
        val exampleText = runBlocking { getString(Res.string.voice_example) }
        for (light in listOf(false, true)) for ((width, height) in listOf(450 to 900, 900 to 450, 320 to 640)) {
            Harness(width, height, light).use { ui ->
                ui.frames(20)
                val mic = ui.node("Stop listening").boundsInRoot
                if (height > width) {
                    val example = ui.nodes().first {
                        it.config.getOrNull(SemanticsProperties.Text)?.any { text -> text.text == exampleText } == true
                    }
                    val gap = mic.center.y - 112f - example.boundsInRoot.bottom
                    assertTrue(gap in 12f..20f, "Prompt must stay close to the widest ring without touching it")
                }
                ui.bitmap().use { bitmap ->
                    val color = bitmap.getColor(mic.center.x.toInt() + 30, mic.center.y.toInt())
                    assertTrue(red(color) > 200 && green(color) < 80, "Listening microphone must be active")
                }
                val early = ui.pixels()
                ui.frames(25)
                assertFalse(early.contentEquals(ui.pixels()), "Listening rings must move")
                assertEquals(mic, ui.node("Stop listening").boundsInRoot, "Rings must not move the touch target")
                ui.state.value = VoiceSearchState.ERROR
                ui.frames(20)
                assertTrue(ui.texts().contains("Didn't catch that"))
                assertTrue(ui.texts().contains("Tap the microphone to try again"))
                if (height > width) {
                    val retry = ui.nodes().first {
                        it.config.getOrNull(SemanticsProperties.Text)?.any { text -> text.text == "Tap the microphone to try again" } == true
                    }
                    val gap = mic.center.y - 112f - retry.boundsInRoot.bottom
                    assertTrue(gap in 12f..20f, "Retry text must keep the same close spacing")
                }
                ui.bitmap().use { bitmap ->
                    val color = bitmap.getColor(mic.center.x.toInt() + 30, mic.center.y.toInt())
                    assertTrue(kotlin.math.abs(red(color) - green(color)) < 12, "Retry microphone must be neutral gray")
                }
                val idle = ui.pixels()
                ui.frames(40)
                assertTrue(idle.contentEquals(ui.pixels()), "Retry must not keep a listening animation running")
                ui.tap(ui.node("Start voice search").boundsInRoot.center)
                assertEquals(1, ui.micClicks)
                ui.state.value = VoiceSearchState.LISTENING
                ui.frames(20)
                val restarted = ui.pixels()
                ui.frames(25)
                assertFalse(restarted.contentEquals(ui.pixels()), "Retry must restart the rings")
            }
        }
    }

    @Test
    fun onlyTheHandleDismissesWithATapOrCompletedDownwardPull() {
        Harness(450, 900, false).use { ui ->
            ui.frames(3)
            val handle = ui.node("Close voice search")
            assertTrue(handle.boundsInRoot.width >= 48f && handle.boundsInRoot.height >= 48f)
            val center = handle.boundsInRoot.center
            ui.drag(center, center + Offset(0f, 20f))
            assertEquals(0, ui.dismissals, "A small accidental movement must not dismiss")
            ui.drag(center, center + Offset(0f, -80f))
            assertEquals(0, ui.dismissals, "An upward pull must not dismiss")
            ui.drag(center, center + Offset(0f, 100f))
            assertEquals(1, ui.dismissals)
            ui.tap(center)
            assertEquals(2, ui.dismissals)
            assertTrue(handle.config[SemanticsActions.OnClick].action!!.invoke())
            assertEquals(3, ui.dismissals, "Accessibility must be able to close voice search")
        }
    }

    @Test
    fun processingAndPermissionRequestsDisableTheMicrophoneAndKeepTheirMessages() {
        Harness(450, 900, false).use { ui ->
            for ((state, title) in listOf(
                VoiceSearchState.PROCESSING to "Processing…",
                VoiceSearchState.REQUESTING_PERMISSION to "Allow microphone access",
            )) {
                ui.state.value = state
                ui.frames(20)
                val mic = ui.node("Start voice search")
                assertTrue(mic.config.getOrNull(SemanticsProperties.Disabled) != null)
                ui.tap(mic.boundsInRoot.center)
                assertEquals(0, ui.micClicks)
                assertTrue(ui.texts().contains(title))
            }
        }
    }

    private class Harness(width: Int, height: Int, light: Boolean) : AutoCloseable {
        val state = mutableStateOf(VoiceSearchState.LISTENING)
        var dismissals = 0
        var micClicks = 0
        var time = 0L
        val scene = ImageComposeScene(width, height) {
            val colors = if (light) lightColorScheme(background = Color.White) else darkColorScheme(background = Color.Black)
            CompositionLocalProvider(LocalAppIsDarkTheme provides !light, LocalAppColorScheme provides colors) {
                MaterialTheme(colorScheme = colors) {
                    VoiceSearchContent(state.value, "", { dismissals++ }, { micClicks++ })
                }
            }
        }
        fun frames(count: Int) { repeat(count) { scene.render(time.also { time += 16_666_667L }).close() } }
        fun bitmap(): Bitmap = scene.render(time.also { time += 16_666_667L }).use { Bitmap.makeFromImage(it) }
        fun pixels() = scene.render(time.also { time += 16_666_667L }).use { it.encodeToData()!!.bytes }
        fun nodes(): List<SemanticsNode> {
            fun descendants(node: SemanticsNode): List<SemanticsNode> = listOf(node) + node.children.flatMap(::descendants)
            return scene.semanticsOwners.flatMap { descendants(it.rootSemanticsNode) }
        }
        fun node(description: String) = nodes().first {
            it.config.getOrNull(SemanticsProperties.ContentDescription)?.contains(description) == true
        }
        fun texts() = nodes().flatMap { it.config.getOrNull(SemanticsProperties.Text).orEmpty() }.map { it.text }
        fun tap(point: Offset) {
            scene.sendPointerEvent(PointerEventType.Press, point, type = PointerType.Touch)
            frames(1)
            scene.sendPointerEvent(PointerEventType.Release, point, type = PointerType.Touch)
            frames(2)
        }
        fun drag(start: Offset, end: Offset) {
            scene.sendPointerEvent(PointerEventType.Press, start, type = PointerType.Touch)
            frames(1)
            for (step in 1..5) {
                scene.sendPointerEvent(PointerEventType.Move, start + (end - start) * (step / 5f), type = PointerType.Touch)
                frames(1)
            }
            scene.sendPointerEvent(PointerEventType.Release, end, type = PointerType.Touch)
            frames(2)
        }
        override fun close() = scene.close()
    }

    private fun red(color: Int) = (color ushr 16) and 0xff
    private fun green(color: Int) = (color ushr 8) and 0xff
}
