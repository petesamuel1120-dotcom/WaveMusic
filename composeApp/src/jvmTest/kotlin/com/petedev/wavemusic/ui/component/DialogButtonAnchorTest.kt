@file:OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)

package com.petedev.wavemusic.ui.component

import com.petedev.wavemusic.ui.theme.LocalAdvancedDialogAnimations

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DialogButtonAnchorTest {
    @Test
    fun settingsRowsRecordTheirCenterAndBoundsForEdgeTapsAndAccessibilityAfterResizing() {
        val origins = DialogOriginTracker()
        val rowWidth = mutableStateOf(310)
        val rowY = mutableStateOf(150)
        val fontScale = mutableStateOf(1f)
        val enabled = mutableStateOf(true)
        var captured: DialogMotionOrigin? = null
        var activations = 0
        val scene = ImageComposeScene(360, 800) {
            CompositionLocalProvider(
                LocalAdvancedDialogAnimations provides true, LocalDialogOrigins provides origins,
                LocalDensity provides Density(1f, fontScale.value),
            ) {
                MaterialTheme {
                    Box(Modifier.fillMaxSize().dialogMotionSource()) {
                        Box(Modifier.offset(21.dp, rowY.value.dp).width(rowWidth.value.dp)) {
                            SettingItem(
                                title = "Theme",
                                subtitle = "System default",
                                isEnable = enabled.value,
                                onClick = {
                                    activations++
                                    // Consume the origin just as AppDialogMotion does when opening.
                                    captured = origins.takeRecent()
                                },
                            )
                        }
                    }
                }
            }
        }
        var time = 0L
        fun frames() {
            repeat(4) {
                time += 16_000_000
                scene.render(time).close()
            }
        }
        fun descendants(node: SemanticsNode): List<SemanticsNode> =
            listOf(node) + node.children.flatMap(::descendants)
        fun row() = scene.semanticsOwners.flatMap { descendants(it.rootSemanticsNode) }.first {
            it.config.getOrNull(SemanticsActions.OnClick) != null &&
                it.config.getOrNull(SemanticsProperties.Text)?.any { text -> text.text == "Theme" } == true
        }
        fun tap(position: Offset) {
            scene.sendPointerEvent(PointerEventType.Press, position, type = PointerType.Touch)
            scene.sendPointerEvent(PointerEventType.Release, position, type = PointerType.Touch)
            frames()
        }
        fun assertCenter(expected: Offset) {
            assertTrue((assertNotNull(captured).center - expected).getDistance() < 0.01f,
                "Dialog origin $captured must equal button center $expected")
        }
        fun assertSourceBounds(expected: Rect) {
            val actual = assertNotNull(
                assertNotNull(captured).bounds,
                "Setting row taps must retain the complete source bounds",
            )
            assertTrue(
                (actual.topLeft - expected.topLeft).getDistance() < 0.01f &&
                    (actual.bottomRight - expected.bottomRight).getDistance() < 0.01f,
                "Dialog source bounds $actual must match the setting row bounds $expected",
            )
        }
        try {
            frames()
            val initial = row().boundsInRoot
            tap(Offset(initial.left + 5f, initial.center.y))
            assertCenter(initial.center)
            assertSourceBounds(initial)
            tap(Offset(initial.right - 5f, initial.center.y))
            assertCenter(initial.center)
            assertSourceBounds(initial)

            rowY.value = 430
            rowWidth.value = 240
            fontScale.value = 1.5f
            frames()
            val moved = row().boundsInRoot
            tap(Offset(moved.left + 5f, moved.center.y))
            assertCenter(moved.center)
            assertSourceBounds(moved)

            // Semantic activation must replace a stale tap without needing a pointer release.
            origins.record(Rect(340f, 10f, 360f, 30f))
            row().config[SemanticsActions.OnClick].action!!.invoke()
            assertCenter(moved.center)
            assertSourceBounds(moved)
            assertEquals(4, activations)

            enabled.value = false
            frames()
            tap(moved.center)
            assertEquals(4, activations, "Disabled settings must not open a dialog")
        } finally {
            scene.close()
        }
    }
}
