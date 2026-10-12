@file:OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)

package com.petedev.wavemusic.ui.component

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
    @Test fun travelUsesHalfTheViewportAndDetailDirectionReverses() {
        assertEquals(400, slideV2InitialOffset(800, true))
        assertEquals(-400, slideV2InitialOffset(800, false))
        assertEquals(0, slideV2InitialOffset(0, true))
        assertEquals(0, slideV2InitialOffset(-10, false))
    }

    private class Fixture(private val viewportWidthPx: Int? = null) : AutoCloseable {
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
                        slideV2EnterTransition(forward, viewportWidthPx)
                            .togetherWith(slideV2ExitTransition(forward, viewportWidthPx)).using(null)
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

    @Test fun forwardAndBackSettleWithoutResizingThePage() {
        Fixture().use { f ->
            f.frame(0)
            f.page.value = 1
            f.frame(16)
            f.frame(32)
            for (time in 48L..320L step 16) {
                f.frame(time)
                f.pages().forEach { assertEquals(800, it.size.width); assertEquals(600, it.size.height) }
            }
            f.frame(400)
            assertEquals(setOf("page-1"), f.tags())
            f.page.value = 0
            for (time in 416L..800L step 16) f.frame(time)
            assertEquals(setOf("page-0"), f.tags())
        }
    }

    @Test fun tabDirectionsFollowTheNavbarAndReverseOnReturn() {
        Fixture().use { f ->
            f.frame(0)
            for ((page, start) in listOf(1 to 16L, 0 to 416L)) {
                f.page.value = page
                f.frame(start)
                f.frame(start + 16)
                f.frame(start + 64)
                val pages = f.pages().associateBy { it.config[SemanticsProperties.TestTag] }
                val incoming = pages.getValue("page-$page").positionInRoot.x
                val outgoing = pages.getValue("page-${1 - page}").positionInRoot.x
                val sign = if (page == 1) 1 else -1
                assertTrue(incoming * sign > 0f && incoming * sign <= 400f, "Incoming tab: $incoming")
                assertTrue(outgoing * sign < 0f && outgoing * sign >= -400f, "Outgoing tab: $outgoing")
                f.frame(start + 384)
                assertEquals(setOf("page-$page"), f.tags())
            }
        }
    }

    @Test fun homeSearchLibraryDirectionIgnoresTabRestorePopOperations() {
        for (isPop in listOf(false, true)) {
            assertTrue(slideV2MovingForward(0, 2, isPop))
            assertTrue(slideV2MovingForward(0, 4, isPop))
            assertFalse(slideV2MovingForward(2, 0, isPop))
            assertFalse(slideV2MovingForward(4, 0, isPop))
        }
        assertTrue(slideV2MovingForward(4, null, false))
        assertFalse(slideV2MovingForward(null, 4, true))
        assertTrue(slideV2MovingForward(null, null, false))
        assertFalse(slideV2MovingForward(null, null, true))
    }

    @Test fun explicitViewportKeepsContentWidthFromChangingTravelDistance() {
        Fixture(viewportWidthPx = 400).use { f ->
            f.frame(0)
            f.page.value = 1
            f.frame(16)
            f.frame(32)
            val incoming = f.pages().single { it.config[SemanticsProperties.TestTag] == "page-1" }
            assertTrue(incoming.positionInRoot.x > 0f && incoming.positionInRoot.x <= 200f)
            assertEquals(800, incoming.size.width)
        }
    }

    @Test fun pageContentDoesNotRecomposeOnEveryAnimationFrame() {
        Fixture().use { f ->
            f.frame(0)
            f.page.value = 1
            for (time in 16L..400L step 16) f.frame(time)
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
