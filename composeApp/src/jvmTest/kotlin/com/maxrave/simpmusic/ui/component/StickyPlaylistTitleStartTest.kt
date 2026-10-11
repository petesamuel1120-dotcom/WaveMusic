@file:OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)

package com.maxrave.simpmusic.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class StickyPlaylistTitleStartTest {
    @Test
    fun landscapeTitleStartsWithTheMetadataThenMovesToTheToolbarAndBack() {
        exercise(topInset = 24, sourceFontSize = 24)
    }

    @Test
    fun largerTypeAndDifferentInsetsStillStartWithTheMetadata() {
        exercise(topInset = 40, sourceFontSize = 32)
    }

    private fun exercise(topInset: Int, sourceFontSize: Int) {
        var list: LazyListState? = null
        var scope: CoroutineScope? = null
        val title = "Favorite"
        val sourceStyle = TextStyle(fontSize = sourceFontSize.sp)
        val targetStyle = TextStyle(fontSize = 18.sp)
        var time = 0L
        val scene = ImageComposeScene(900, 420) {
            val state = rememberLazyListState()
            val sticky = remember { StickyPlaylistTitleState(state, "hero") }
            list = state
            scope = rememberCoroutineScope()
            Box(Modifier.fillMaxSize().background(Color.Black)) {
                LazyColumn(state = state, contentPadding = PaddingValues(top = topInset.dp),
                    modifier = Modifier.fillMaxSize().then(sticky.viewportModifier)) {
                    item { Spacer(Modifier.height(56.dp)) }
                    item { Spacer(Modifier.height(0.dp)) }
                    item(key = "hero") {
                        Box(Modifier.fillMaxWidth().height(190.dp)) {
                            Text(title, style = sourceStyle,
                                modifier = Modifier.padding(start = 220.dp, top = 4.dp, end = 24.dp)
                                    .fillMaxWidth().then(sticky.sourceModifier(sourceStyle, textAlign = TextAlign.Start)))
                            Text("346 tracks", modifier = Modifier.padding(start = 220.dp, top = 44.dp))
                        }
                    }
                    items(20) { Text("Song $it", modifier = Modifier.fillMaxWidth().height(56.dp)) }
                }
                Box(Modifier.offset(y = 32.dp).padding(horizontal = 50.dp).fillMaxWidth().height(40.dp)) {
                    sticky.Target(title, style = targetStyle)
                }
                with(sticky) { Title(title, Color.White) }
            }
        }
        fun frames() { repeat(4) { time += 16_666_667L; scene.render(time).close() } }
        fun nodes(node: SemanticsNode): List<SemanticsNode> = listOf(node) + node.children.flatMap(::nodes)
        fun titleBounds() = scene.semanticsOwners.flatMap { nodes(it.rootSemanticsNode) }.filter {
            it.config.getOrNull(SemanticsProperties.Text)?.any { text -> text.text == title } == true
        }.also { assertEquals(1, it.size, "Only one visible title should move") }.single().boundsInRoot
        try {
            frames()
            val initial = titleBounds()
            assertEquals(220f, initial.left, "The title must share the track count's starting edge")
            assertEquals((topInset + 60).toFloat(), initial.top)
            val forward = mutableMapOf<Int, androidx.compose.ui.geometry.Rect>()
            for (offset in listOf(0, 10, 20, 40, 70, 120, 350, 120, 70, 40, 20, 10, 0)) {
                assertNotNull(scope).launch { assertNotNull(list).scrollToItem(0, offset) }
                frames()
                val bounds = titleBounds()
                val previous = forward.putIfAbsent(offset, bounds)
                if (previous != null) assertEquals(previous, bounds, "Reverse scrolling must follow the same title path")
                if (offset >= 120) {
                    assertTrue(abs(bounds.center.x - 450f) < 1f)
                    assertEquals(32f, bounds.top)
                }
            }
            assertEquals(initial, titleBounds())
        } finally {
            scene.close()
        }
    }
}
