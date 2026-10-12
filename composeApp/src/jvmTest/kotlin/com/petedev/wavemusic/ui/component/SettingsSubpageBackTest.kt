@file:OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)

package com.petedev.wavemusic.ui.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import com.petedev.wavemusic.domain.manager.DataStoreManager
import kotlinx.coroutines.launch
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SettingsSubpageBackTest {
    private val styles = listOf(DataStoreManager.PAGE_TRANSITION_SLIDE_V2, DataStoreManager.PAGE_TRANSITION_FADE)

    @Test
    fun interfaceBackButtonRunsTheSelectedOverviewTransition() {
        for (style in styles) Fixture(style).use { f ->
            f.frame(0)
            f.back()
            for (time in 16L..96L step 16) f.frame(time)
            assertEquals(null, f.page.value)
            assertFalse(f.committed.value, "A button must not suppress AnimatedContent: $style")
            assertEquals(0f, f.progress.floatValue)
            assertEquals(setOf("interface", "overview"), f.pages().keys)
            val overviewX = f.pages().getValue("overview").positionInRoot.x
            if (style == DataStoreManager.PAGE_TRANSITION_SLIDE_V2) {
                assertTrue(overviewX < 0f && overviewX >= -400f, "Slide v2 must enter from the left: $overviewX")
            } else {
                assertEquals(0f, overviewX, "Fade must not translate the Settings overview")
            }
            f.frame(400)
            assertEquals(setOf("overview"), f.pages().keys)
            assertEquals(1, f.closeCount)
        }
    }

    @Test
    fun swipeBackUsesTheSelectedMotionAndFinishesWithoutASecondTransition() {
        for (style in styles) Fixture(style).use { f ->
            f.frame(0)
            f.progress.floatValue = 0.4f
            f.frame(16)
            val detailX = f.pages().getValue("interface").boundsInRoot.left
            assertEquals(if (style == DataStoreManager.PAGE_TRANSITION_SLIDE_V2) 160f else 0f, detailX)
            assertEquals(0.6f, settingsBackAlpha(style, 0.4f, entering = false))
            f.back()
            for (time in 32L..432L step 16) f.frame(time)
            assertTrue(f.committed.value)
            assertEquals(1f, f.progress.floatValue)
            assertEquals(setOf("overview"), f.pages().keys)
            assertEquals(1, f.closeCount)
        }
    }

    @Test
    fun cancelledSwipeRestoresInterfaceWithoutReturningToOverview() {
        for (style in styles) Fixture(style).use { f ->
            f.frame(0)
            f.progress.floatValue = 0.6f
            f.frame(16)
            f.cancel()
            for (time in 32L..432L step 16) f.frame(time)
            assertEquals("user_interface", f.page.value)
            assertEquals(0f, f.progress.floatValue)
            assertEquals(0f, f.pages().getValue("interface").boundsInRoot.left)
            assertFalse(f.committed.value)
            assertEquals(0, f.closeCount)
        }
    }

    @Test
    fun originalSlideKeepsItsFullWidthBackMotion() {
        Fixture(DataStoreManager.PAGE_TRANSITION_SLIDE).use { f ->
            f.frame(0)
            f.progress.floatValue = 0.4f
            f.frame(16)
            assertEquals(320f, f.pages().getValue("interface").boundsInRoot.left)
            assertEquals(1f, settingsBackAlpha(DataStoreManager.PAGE_TRANSITION_SLIDE, 0.4f, entering = false))
            f.back()
            for (time in 32L..432L step 16) f.frame(time)
            assertTrue(f.committed.value)
            assertEquals(setOf("overview"), f.pages().keys)
        }
    }

    private class Fixture(private val style: String) : AutoCloseable {
        val page = mutableStateOf<String?>("user_interface")
        val progress = mutableFloatStateOf(0f)
        val committed = mutableStateOf(false)
        var closeCount = 0
        private lateinit var backAction: () -> Unit
        private lateinit var cancelAction: () -> Unit
        private val scene = ImageComposeScene(width = 800, height = 600)

        init {
            scene.setContent {
                val scope = rememberCoroutineScope()
                SideEffect {
                    backAction = {
                        scope.launch {
                            finishSettingsSubpageBack(style, progress, { committed.value = true }) {
                                closeCount++
                                page.value = null
                            }
                        }
                    }
                    cancelAction = { scope.launch { cancelSettingsSubpageBack(progress) } }
                }
                AnimatedContent(
                    targetState = page.value,
                    modifier = Modifier.fillMaxSize(),
                    transitionSpec = {
                        when {
                            committed.value -> EnterTransition.None togetherWith ExitTransition.None
                            style == DataStoreManager.PAGE_TRANSITION_SLIDE_V2 ->
                                slideV2EnterTransition(targetState != null)
                                    .togetherWith(slideV2ExitTransition(targetState != null)).using(null)
                            else -> fadeIn(tween(260)).togetherWith(fadeOut(tween(170)))
                        }
                    },
                ) { displayedPage ->
                    Box(Modifier.fillMaxSize().graphicsLayer {
                        if (displayedPage != null) {
                            translationX = settingsBackTranslationX(style, 800, progress.floatValue, entering = false)
                            alpha = settingsBackAlpha(style, progress.floatValue, entering = false)
                        }
                    }.background(if (displayedPage == null) Color.Red else Color.Blue)
                        .semantics { testTag = if (displayedPage == null) "overview" else "interface" })
                }
            }
        }

        fun frame(ms: Long) { scene.render(ms * 1_000_000L).close() }
        fun back() = backAction()
        fun cancel() = cancelAction()
        fun pages(): Map<String, SemanticsNode> {
            fun nodes(node: SemanticsNode): List<SemanticsNode> = listOf(node) + node.children.flatMap(::nodes)
            return scene.semanticsOwners.flatMap { nodes(it.rootSemanticsNode) }
                .mapNotNull { node -> node.config.getOrNull(SemanticsProperties.TestTag)?.let { it to node } }.toMap()
        }
        override fun close() = scene.close()
    }
}
