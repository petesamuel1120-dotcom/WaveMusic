@file:OptIn(
    androidx.compose.ui.ExperimentalComposeUiApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.material3.ExperimentalMaterial3ComponentOverrideApi::class,
)

package com.maxrave.simpmusic.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.unit.Density
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.maxrave.simpmusic.ui.theme.LocalAdvancedDialogAnimations
import com.maxrave.simpmusic.ui.theme.LocalAppColorScheme
import com.maxrave.simpmusic.ui.theme.LocalAppIsDarkTheme
import kotlinx.coroutines.runBlocking
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Image
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SettingsDialogReturnTest {
    @Test
    fun shortAndWrappedRowsReturnCleanlyInBothThemesAndDensities() = runBlocking<Unit> {
        for (dark in listOf(false, true)) for (density in listOf(1f, 2f)) {
            Fixture(dark, density).use { fixture ->
                for (label in listOf("Theme", "Lyrics provider")) {
                    val original = fixture.rowPixels(label)
                    fixture.open(label, frames = 45)
                    fixture.assertReturnPixels(label, original)
                    fixture.dismiss()
                    assertTrue(original.contentEquals(fixture.rowPixels(label)), "The restored live row must also match")
                }
            }
        }
    }

    @Test
    fun quickDismissalsAndRepeatedOpeningDoNotReplayPressedFeedback() = runBlocking<Unit> {
        for (dark in listOf(false, true)) {
            Fixture(dark, 1f).use { fixture ->
                repeat(3) { attempt ->
                    val label = if (attempt % 2 == 0) "Theme" else "Lyrics provider"
                    val original = fixture.rowPixels(label)
                    fixture.open(label, frames = 2)
                    fixture.assertReturnPixels(label, original)
                    fixture.dismiss()
                    assertEquals(1f, assertNotNull(fixture.source).opacity, "A quick dismissal must restore its source")
                }
            }
        }
    }

    @Test
    fun livePressFeedbackRemainsVisibleWithAnimationsEnabledAndDisabled() {
        for (dark in listOf(false, true)) for (animations in listOf(false, true)) {
            Fixture(dark, 1f, animations).use { fixture ->
                val original = fixture.rowPixels("Theme")
                fixture.press("Theme")
                fixture.frames(12)
                val pressed = fixture.rowPixels("Theme")
                assertTrue(original.zip(pressed).count { (a, b) -> colorDifference(a, b) > 3 } > 50,
                    "The live row must still show touch feedback: dark=$dark animations=$animations")
            }
        }
    }

    private class Fixture(dark: Boolean, private val density: Float, animations: Boolean = true) : AutoCloseable {
        private val scheme = if (dark) darkColorScheme() else lightColorScheme()
        private val origins = DialogOriginTracker()
        private val visible = mutableStateOf(false)
        private var motion: DialogMotionState? = null
        private var close: ((() -> Unit) -> Unit)? = null
        var source: DialogMotionSourceState? = null
            private set
        private var time = 0L
        private val scene = ImageComposeScene((400 * density).toInt(), (760 * density).toInt(), Density(density)) {
            MaterialTheme(colorScheme = scheme) {
                CompositionLocalProvider(
                    LocalDensity provides Density(density),
                    LocalAdvancedDialogAnimations provides animations,
                    LocalDialogOrigins provides origins,
                    LocalAppColorScheme provides scheme,
                    LocalAppIsDarkTheme provides dark,
                    LocalBasicAlertDialogOverride provides AnimatedAppAlertDialogOverride,
                ) {
                    Box(Modifier.fillMaxSize().background(scheme.background).dialogBackdropBlur()) {
                        Column(Modifier.offset(20.dp, 144.dp).width(360.dp)
                            .background(scheme.surfaceContainer, RoundedCornerShape(24.dp))) {
                            SettingItem("Theme", "System default", onClick = { visible.value = true })
                            SettingItem("Lyrics provider",
                                "Select the lyrics provider used for synchronized and translated lyrics on the player screen",
                                onClick = { visible.value = true })
                        }
                        if (visible.value) {
                            AmoledAlertDialog(
                                onDismissRequest = { visible.value = false },
                                title = {
                                    motion = LocalDialogMotionOwner.current
                                    close = rememberDialogCloseAction()
                                    Text("Choose an option")
                                },
                                text = {
                                    Column {
                                        listOf("System default", "Dark", "Light").forEach { label ->
                                            Row { RadioButton(label == "System default", {}); Text(label) }
                                        }
                                    }
                                },
                                confirmButton = { DialogTextButton({ visible.value = false }) { Text("Change") } },
                                dismissButton = { DialogTextButton({ visible.value = false }) { Text("Cancel") } },
                            )
                        }
                    }
                }
            }
        }

        init { frames(5) }
        private fun render(): Image = scene.render(time.also { time += 16_666_667L })
        fun frames(count: Int) { repeat(count) { render().close() } }
        private fun descendants(node: SemanticsNode): List<SemanticsNode> =
            listOf(node) + node.children.flatMap(::descendants)
        private fun bounds(label: String): Rect = scene.semanticsOwners.flatMap { descendants(it.rootSemanticsNode) }
            .first { node ->
                node.config.getOrNull(SemanticsActions.OnClick) != null &&
                    node.config.getOrNull(SemanticsProperties.Text)?.any { it.text == label } == true
            }.boundsInRoot

        fun rowPixels(label: String): IntArray {
            val row = bounds(label)
            val margin = (4 * density).toInt()
            val left = row.left.toInt() - margin
            val top = row.top.toInt() - margin
            val width = row.width.toInt() + 2 * margin
            val height = row.height.toInt() + 2 * margin
            return render().use { image ->
                Bitmap.makeFromImage(image).use { bitmap ->
                    IntArray(width * height) { i -> bitmap.getColor(left + i % width, top + i / width) }
                }
            }
        }
        fun press(label: String) {
            val row = bounds(label)
            scene.sendPointerEvent(PointerEventType.Press,
                Offset(row.left + 24 * density, row.center.y), type = PointerType.Touch)
        }
        fun open(label: String, frames: Int) {
            val row = bounds(label)
            val point = Offset(row.left + 24 * density, row.center.y)
            press(label)
            this.frames(1)
            scene.sendPointerEvent(PointerEventType.Release, point, type = PointerType.Touch)
            this.frames(frames)
            source = assertNotNull(assertNotNull(motion).origin?.source)
        }
        suspend fun assertReturnPixels(label: String, original: IntArray) {
            val state = assertNotNull(motion)
            state.progress.stop()
            state.progress.snapTo(0f)
            frames(3)
            val returned = rowPixels(label)
            val differences = original.zip(returned).map { (a, b) -> colorDifference(a, b) }
            val changed = differences.count { it > 3 }
            val mean = differences.average()
            println("Returning $label at density=$density: changed=$changed mean=$mean")
            assertTrue(changed < 20 && mean < 0.1,
                "The returning row must not replay a pressed highlight, center flash or dialog outline: changed=$changed mean=$mean")
        }
        fun dismiss() {
            assertNotNull(close).invoke { visible.value = false }
            frames(45)
            assertTrue(!visible.value)
        }
        override fun close() { scene.close() }
    }

    companion object {
        private fun colorDifference(a: Int, b: Int): Int = maxOf(
            abs((a shr 16 and 255) - (b shr 16 and 255)),
            abs((a shr 8 and 255) - (b shr 8 and 255)),
            abs((a and 255) - (b and 255)),
        )
    }
}
