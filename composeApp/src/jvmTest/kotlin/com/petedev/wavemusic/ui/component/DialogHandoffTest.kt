@file:OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)

package com.petedev.wavemusic.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import com.petedev.wavemusic.ui.theme.LocalAdvancedDialogAnimations
import kotlinx.coroutines.runBlocking
import org.jetbrains.skia.Bitmap
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.math.abs

class DialogHandoffTest {
    @Test
    fun dropdownReturnsWithItsArrowAlreadyClosed() {
        val origins = DialogOriginTracker()
        val scene = ImageComposeScene(450, 900) {
            MaterialTheme(colorScheme = darkColorScheme()) {
                CompositionLocalProvider(LocalAdvancedDialogAnimations provides true, LocalDialogOrigins provides origins) {
                    Box(Modifier.fillMaxSize().background(Color.Black).padding(24.dp)) {
                        DropdownButton("Global", listOf("Global", "Argentina", "Australia"), "Chart") {}
                    }
                }
            }
        }
        var time = 0L
        fun render() = scene.render(time.also { time += 16_666_667L })
        fun frames(count: Int) { repeat(count) { render().close() } }
        fun nodes(): List<SemanticsNode> {
            fun descendants(node: SemanticsNode): List<SemanticsNode> = listOf(node) + node.children.flatMap(::descendants)
            return scene.semanticsOwners.flatMap { descendants(it.rootSemanticsNode) }
        }
        fun tap(point: Offset) {
            scene.sendPointerEvent(PointerEventType.Press, point, type = PointerType.Touch)
            frames(1)
            scene.sendPointerEvent(PointerEventType.Release, point, type = PointerType.Touch)
            frames(1)
        }
        try {
            frames(4)
            val field = nodes().first { it.config.getOrNull(SemanticsProperties.EditableText)?.text == "Global" }.boundsInRoot
            fun arrowPixels(bitmap: Bitmap): IntArray {
                val left = field.right.toInt() - 32
                val top = field.center.y.toInt() - 8
                return IntArray(16 * 16) { bitmap.getColor(left + it % 16, top + it / 16) and 255 }
            }
            tap(field.center)
            frames(45)
            tap(Offset(4f, 895f))
            var returning: IntArray? = null
            repeat(60) {
                render().use { image ->
                    val progress = origins.backgroundProgress(null)
                    if (progress in 0.004f..0.008f) {
                        Bitmap.makeFromImage(image).use { returning = arrowPixels(it) }
                    }
                }
            }
            frames(20)
            val settled = render().use { image ->
                Bitmap.makeFromImage(image).use { arrowPixels(it) }
            }
            val beforeRemoval = assertNotNull(returning, "Sample the control before its dialog window is removed")
            val meanDifference = beforeRemoval.zip(settled).sumOf { (a, b) -> abs(a - b) }.toFloat() / settled.size
            println("Returning vs settled arrow difference: $meanDifference")
            assertTrue(meanDifference < 3f, "The arrow must already match its closed state before dialog removal: $meanDifference")
        } finally { scene.close() }
    }

    @Test
    fun sourceKeepsItsContrastThroughoutTheReturnHandoff() = runBlocking<Unit> {
        checkSourceContrast(Color.White, 255)
    }

    @Test
    fun translucentSourceDoesNotBrightenWhenItReturns() = runBlocking<Unit> {
        checkSourceContrast(Color.White.copy(alpha = 0.5f), 128)
    }

    private suspend fun checkSourceContrast(color: Color, expectedBrightness: Int) {
        val origins = DialogOriginTracker()
        val visible = mutableStateOf(false)
        var motion: DialogMotionState? = null
        var open: (() -> Unit)? = null
        val scene = ImageComposeScene(240, 360) {
            CompositionLocalProvider(LocalAdvancedDialogAnimations provides true, LocalDialogOrigins provides origins) {
                Box(Modifier.fillMaxSize().background(Color.Black)) {
                    val anchor = rememberDialogMotionAnchor(0.dp)
                    open = { anchor.record(); visible.value = true }
                    Box(Modifier.offset(20.dp, 220.dp).size(180.dp, 56.dp).then(anchor.modifier)
                        .background(color))
                    if (visible.value) {
                        AppDialogMotion({ visible.value = false }) { state, _ ->
                            motion = state
                            DialogMotionSurface(state, Modifier.size(180.dp, 56.dp)) {}
                        }
                    }
                }
            }
        }
        var time = 0L
        fun render() = scene.render(time.also { time += 16_666_667L })
        try {
            repeat(3) { render().close() }
            assertNotNull(open).invoke()
            repeat(45) { render().close() }
            val state = assertNotNull(motion)
            state.progress.stop()
            val contrast = mutableListOf<Int>()
            for (fraction in listOf(0.045f, 0.03375f, 0.0225f, 0.01125f, 0f)) {
                state.progress.snapTo(fraction)
                render().use { image ->
                    Bitmap.makeFromImage(image).use { bitmap ->
                        contrast += bitmap.getColor(110, 248) and 255
                    }
                }
            }
            println("Source contrast through return: $contrast")
            assertTrue(contrast.all { abs(it - expectedBrightness) <= 2 },
                "The button must keep its original brightness ($expectedBrightness) during the return: $contrast")
        } finally { scene.close() }
    }
}
