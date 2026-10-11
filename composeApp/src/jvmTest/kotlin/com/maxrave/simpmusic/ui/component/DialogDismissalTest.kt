@file:OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)

package com.maxrave.simpmusic.ui.component

import com.maxrave.simpmusic.ui.theme.LocalAdvancedDialogAnimations

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.*
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.unit.dp
import kotlin.test.*

class DialogDismissalTest {
    @Test
    fun validationCanKeepDialogOpenAndAFollowingDismissRestoresTheCapturedRow() {
        exercise(closeImmediately = false)
    }

    @Test
    fun dismissalBeforeEntryHasAdvancedStillRunsOnceAndRestoresTheRow() {
        exercise(closeImmediately = true)
    }

    private fun exercise(closeImmediately: Boolean) {
        val origins = DialogOriginTracker()
        val visible = mutableStateOf(false)
        var motion: DialogMotionState? = null
        var close: ((() -> Unit) -> Unit)? = null
        var callbacks = 0
        var time = 0L
        val scene = ImageComposeScene(450, 900) {
            CompositionLocalProvider(LocalAdvancedDialogAnimations provides true, LocalDialogOrigins provides origins) {
                Box(Modifier.fillMaxSize()) {
                    Box(Modifier.offset(20.dp, 200.dp).width(410.dp).height(80.dp)
                        .dialogMotionClickable { visible.value = true }.background(Color.DarkGray)) {
                        BasicText("Theme")
                    }
                    if (visible.value) {
                        AppDialogMotion({ visible.value = false }) { state, _ ->
                            motion = state
                            close = rememberDialogCloseAction()
                            DialogMotionSurface(state, Modifier.width(360.dp).height(400.dp)) {
                                BasicText("Theme choices")
                            }
                        }
                    }
                }
            }
        }
        fun frames(count: Int) {
            repeat(count) { time += 16_666_667L; scene.render(time).close() }
        }
        var source: DialogMotionSourceState? = null
        try {
            frames(3)
            scene.sendPointerEvent(PointerEventType.Press, Offset(40f, 240f), type = PointerType.Touch)
            scene.sendPointerEvent(PointerEventType.Release, Offset(40f, 240f), type = PointerType.Touch)
            frames(1)
            val state = assertNotNull(motion)
            source = assertNotNull(state.origin?.source)
            assertEquals(410, assertNotNull(source.visual).size.width, "Capture the row before hiding it")
            if (!closeImmediately) frames(40)
            val before = state.progress.value
            assertNotNull(close).invoke {
                callbacks++
                if (closeImmediately) visible.value = false
            }
            assertNotNull(close).invoke { error("Repeated close must not replace the pending action") }
            assertEquals(before, state.progress.value, "Closing must start at the current visual position")
            frames(45)
            assertEquals(1, callbacks)
            if (!closeImmediately) {
                // A failed validation leaves its dialog mounted. It must become usable again.
                frames(40)
                assertTrue(visible.value)
                assertFalse(state.closing)
                assertEquals(1f, state.progress.value)
                assertEquals(0f, source.opacity)
                assertNotNull(close).invoke { callbacks++; visible.value = false }
                frames(45)
                assertEquals(2, callbacks)
            }
            assertFalse(visible.value)
            assertNull(source.motion)
            assertEquals(1f, source.opacity)
            assertNotNull(source.visual, "Keep the original control's layer for future openings")
        } finally {
            scene.close()
        }
        assertNull(assertNotNull(source).visual, "Release the captured layer with its source composition")
    }
}
