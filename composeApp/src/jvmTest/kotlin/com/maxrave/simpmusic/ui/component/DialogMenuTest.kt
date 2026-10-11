@file:OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)

package com.maxrave.simpmusic.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ComponentOverrideApi
import androidx.compose.material3.LocalBasicAlertDialogOverride
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.unit.dp
import com.maxrave.simpmusic.ui.theme.LocalAdvancedDialogAnimations
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalMaterial3ComponentOverrideApi::class)
class DialogMenuTest {
    @Test
    fun chartSelectionCommitsOnceAfterClosingAndEarlyOutsideDismissRestoresTheMenu() {
        val origins = DialogOriginTracker()
        var selections = 0
        var selected = "Global"
        val scene = ImageComposeScene(450, 900) {
            MaterialTheme(colorScheme = darkColorScheme()) {
                CompositionLocalProvider(
                    LocalAdvancedDialogAnimations provides true,
                    LocalDialogOrigins provides origins,
                    LocalBasicAlertDialogOverride provides AnimatedAppAlertDialogOverride,
                ) {
                    Box(Modifier.fillMaxSize().padding(24.dp).dialogMotionSource()) {
                        DropdownButton("Global", listOf("Global", "Argentina", "Australia"), "Chart") {
                            selections++
                            selected = it
                        }
                    }
                }
            }
        }
        var time = 0L
        fun frames(count: Int) {
            repeat(count) { scene.render(time.also { time += 16_666_667L }).close() }
        }
        fun nodes(): List<SemanticsNode> {
            fun descendants(node: SemanticsNode): List<SemanticsNode> = listOf(node) + node.children.flatMap(::descendants)
            return scene.semanticsOwners.flatMap { descendants(it.rootSemanticsNode) }
        }
        fun textVisible(text: String) = nodes().any {
            it.config.getOrNull(SemanticsProperties.Text)?.any { value -> value.text == text } == true
        }
        fun tap(point: Offset) {
            scene.sendPointerEvent(PointerEventType.Press, point, type = PointerType.Touch)
            frames(1)
            scene.sendPointerEvent(PointerEventType.Release, point, type = PointerType.Touch)
            frames(1)
        }
        fun open() {
            val field = nodes().first { it.config.getOrNull(SemanticsProperties.EditableText)?.text == selected }
            tap(field.boundsInRoot.center)
        }
        try {
            frames(4)
            open()
            frames(45)
            assertTrue(textVisible("Chart"))
            val option = nodes().first {
                it.config.getOrNull(SemanticsActions.OnClick) != null &&
                    it.config.getOrNull(SemanticsProperties.Text)?.any { value -> value.text == "Argentina" } == true
            }
            assertTrue(option.config[SemanticsActions.OnClick].action!!.invoke())
            assertEquals(0, selections, "Selection must wait for the reverse motion")
            frames(45)
            assertEquals(1, selections)
            assertEquals("Argentina", selected)
            assertFalse(textVisible("Chart"))
            open()
            assertTrue(textVisible("Chart"))
            tap(Offset(4f, 895f))
            frames(45)
            assertFalse(textVisible("Chart"), "Outside dismissal during entry must finish cleanly")
            assertEquals(1, selections, "Dismissal must not select a country")
            open()
            frames(45)
            assertTrue(textVisible("Chart"), "The source must remain usable after repeated dismissals")
        } finally { scene.close() }
    }
}
