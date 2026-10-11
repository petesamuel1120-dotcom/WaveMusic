@file:OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)

package com.maxrave.simpmusic.ui.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import kotlin.test.*

class SlideV2TransitionTest {
    @Test fun travelIsShortAndDirectionReverses() {
        assertEquals(100, slideV2InitialOffset(800, true))
        assertEquals(-100, slideV2InitialOffset(800, false))
        assertEquals(0, slideV2InitialOffset(0, true))
        assertEquals(0, slideV2InitialOffset(-10, false))
        assertEquals(200, SLIDE_V2_ENTER_MILLIS)
        assertEquals(150, SLIDE_V2_EXIT_MILLIS)
    }

    private class Fixture : AutoCloseable {
        val page = mutableStateOf(0)
        val compositions = mutableMapOf<Int, Int>()
        val scene = ImageComposeScene(width = 800, height = 600)
        init {
            scene.setContent {
                AnimatedContent(
                    targetState = page.value,
                    modifier = Modifier.fillMaxSize(),
                    transitionSpec = {
                        val forward = targetState > initialState
                        slideV2EnterTransition(forward).togetherWith(slideV2ExitTransition(forward)).using(null)
                    },
                ) { current ->
                    SideEffect { compositions[current] = compositions.getOrDefault(current, 0) + 1 }
                    Box(Modifier.fillMaxSize().background(if (current == 0) Color.Red else Color.Blue)
                        .semantics { testTag = "page-$current" })
                }
            }
        }
        fun frame(ms: Long) { scene.render(ms * 1_000_000L).close() }
        fun pages(): List<SemanticsNode> {
            fun children(node: SemanticsNode): List<SemanticsNode> = listOf(node) + node.children.flatMap(::children)
            return scene.semanticsOwners.flatMap { children(it.rootSemanticsNode) }
                .filter { it.config.getOrNull(SemanticsProperties.TestTag)?.startsWith("page-") == true }
        }
        fun tags() = pages().map { it.config[SemanticsProperties.TestTag] }.toSet()
        override fun close() { scene.close() }
    }

    @Test fun forwardAndBackFinishQuicklyWithoutResizingThePage() {
        Fixture().use { f ->
            f.frame(0)
            f.page.value = 1
            f.frame(16)
            f.frame(32)
            for (time in 48L..192L step 16) {
                f.frame(time)
                f.pages().forEach { assertEquals(800, it.size.width); assertEquals(600, it.size.height) }
            }
            f.frame(240)
            assertEquals(setOf("page-1"), f.tags())
            f.page.value = 0
            for (time in 256L..496L step 16) f.frame(time)
            assertEquals(setOf("page-0"), f.tags())
        }
    }

    @Test fun pageContentDoesNotRecomposeOnEveryAnimationFrame() {
        Fixture().use { f ->
            f.frame(0)
            f.page.value = 1
            for (time in 16L..320L step 16) f.frame(time)
            assertEquals(setOf("page-1"), f.tags())
            assertTrue(f.compositions.getValue(1) <= 4, "Content must not recompose each frame: ${f.compositions}")
        }
    }

    @Test fun rapidDirectionChangesSettleOnTheLatestPage() {
        Fixture().use { f ->
            f.frame(0)
            f.page.value = 1
            f.frame(16)
            f.frame(80)
            f.page.value = 0
            f.frame(96)
            f.frame(400)
            assertEquals(setOf("page-0"), f.tags())
        }
    }
}
