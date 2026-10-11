@file:OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)

package com.maxrave.simpmusic.ui.component

import com.maxrave.simpmusic.ui.theme.LocalAdvancedDialogAnimations

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DialogControlAnchorTest {
    @Test
    fun microphoneUsesTheWholeButtonAndReturnsToItWhenDismissed() = runBlocking<Unit> {
        val left = exercise(359f, interruptEntry = false)
        val right = exercise(413f, interruptEntry = false)
        assertEquals(left, right, "The animation must be identical across the microphone button")
        exercise(359f, interruptEntry = true)
    }

    private fun exercise(tapX: Float, interruptEntry: Boolean): Rect {
        val origins = DialogOriginTracker()
        val visible = mutableStateOf(false)
        var motion: DialogMotionState? = null
        var dismiss: (() -> Unit)? = null
        var callbacks = 0
        var time = 0L
        val scene = ImageComposeScene(450, 900) {
            CompositionLocalProvider(LocalAdvancedDialogAnimations provides true, LocalDialogOrigins provides origins) {
                Box(Modifier.fillMaxSize().background(Color.DarkGray).dialogMotionSource()) {
                    val anchor = rememberDialogMotionAnchor(32.dp)
                    IconButton(
                        onClick = { anchor.record(); visible.value = true },
                        modifier = Modifier.offset(354.dp, 56.dp).size(64.dp).then(anchor.modifier),
                    ) { BasicText("Mic") }
                    if (visible.value) {
                        AppDialogMotion({ callbacks++; visible.value = false }) { state, close ->
                            motion = state
                            dismiss = close
                            DialogMotionSurface(state, fullScreen = true) {
                                Box(Modifier.fillMaxSize().background(Color.Black)) { BasicText("Listening") }
                            }
                        }
                    }
                }
            }
        }
        fun frame() { time += 16_666_667L; scene.render(time).close() }
        try {
            repeat(3) { frame() }
            scene.sendPointerEvent(PointerEventType.Press, Offset(tapX, 88f), type = PointerType.Touch)
            frame()
            scene.sendPointerEvent(PointerEventType.Release, Offset(tapX, 88f), type = PointerType.Touch)
            frame()
            val state = assertNotNull(motion)
            val origin = assertNotNull(state.origin)
            assertEquals(Rect(354f, 56f, 418f, 120f), origin.bounds)
            assertEquals(32f, origin.cornerRadiusPx)
            val target = Rect(0f, 0f, 450f, 900f)
            val initial = dialogMorphFrame(origin, target, 0f)
            assertTrue((target.center + initial.travel - origin.center).getDistance() < 0.01f)
            assertTrue(kotlin.math.abs(target.width * initial.scaleX - origin.bounds.width) < 0.01f)
            assertTrue(kotlin.math.abs(target.height * initial.scaleY - origin.bounds.height) < 0.01f)
            repeat(if (interruptEntry) 5 else 40) { frame() }
            if (!interruptEntry) assertEquals(1f, state.progress.value)
            val before = state.progress.value
            assertNotNull(dismiss).invoke()
            assertNotNull(dismiss).invoke()
            assertEquals(before, state.progress.value, "Closing must reverse from the current position")
            repeat(45) { frame() }
            assertEquals(1, callbacks)
            assertEquals(0f, state.progress.value)
            assertEquals(1f, assertNotNull(origin.source).opacity, "Closing must restore the same source button")
            return origin.bounds
        } finally { scene.close() }
    }

    @Test
    fun accessibilityActivationResolvesTheMovedControlInsteadOfAStaleTap() {
        val origins = DialogOriginTracker()
        val y = mutableStateOf(100)
        var captured: DialogMotionOrigin? = null
        val scene = ImageComposeScene(450, 900) {
            CompositionLocalProvider(LocalAdvancedDialogAnimations provides true, LocalDialogOrigins provides origins) {
                Box(Modifier.fillMaxSize()) {
                    val anchor = rememberDialogMotionAnchor(24.dp)
                    IconButton(
                        onClick = { anchor.record(); captured = origins.takeRecent() },
                        modifier = Modifier.offset(20.dp, y.value.dp).width(180.dp).height(56.dp).then(anchor.modifier),
                    ) { BasicText("Global") }
                }
            }
        }
        var time = 0L
        fun frames() { repeat(3) { time += 16_666_667L; scene.render(time).close() } }
        fun descendants(node: SemanticsNode): List<SemanticsNode> = listOf(node) + node.children.flatMap(::descendants)
        try {
            frames()
            y.value = 420
            frames()
            origins.record(Rect(350f, 10f, 370f, 30f))
            scene.semanticsOwners.flatMap { descendants(it.rootSemanticsNode) }
                .first { it.config.getOrNull(SemanticsActions.OnClick) != null }
                .config[SemanticsActions.OnClick].action!!.invoke()
            assertEquals(Rect(20f, 420f, 200f, 476f), assertNotNull(captured).bounds)
        } finally { scene.close() }
    }
}
