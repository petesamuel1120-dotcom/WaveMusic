@file:OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)

package com.petedev.wavemusic.ui.component

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
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
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class StickyPlaylistTitleStartTest {
    @Test
    fun firstVisibleScrollFrameAlreadyUsesTheHeaderOrigin() {
        for ((hostLeft, hostTop) in listOf(0 to 24, 24 to 48)) {
            var list: LazyListState? = null
            var scope: CoroutineScope? = null
            var showOverlay by mutableStateOf(false)
            var hostTranslation by mutableStateOf(0f)
            val title = "Joyner Lucas"
            val sourceStyle = TextStyle(fontSize = 32.sp)
            var time = 0L
            val placedTitlePositions = mutableListOf<Offset>()
            fun nodes(node: SemanticsNode): List<SemanticsNode> = listOf(node) + node.children.flatMap(::nodes)
            lateinit var scene: ImageComposeScene
            scene = ImageComposeScene(900, 600) {
                val state = rememberLazyListState()
                val sticky = remember { StickyPlaylistTitleState(state, "hero") }
                list = state
                scope = rememberCoroutineScope()
                Box(Modifier.fillMaxSize().background(Color.Black)) {
                    LazyColumn(state = state, modifier = Modifier.fillMaxSize().then(sticky.viewportModifier)) {
                        item(key = "hero") {
                            Box(Modifier.fillMaxWidth().height(400.dp)) {
                                Text(title, style = sourceStyle, textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(top = 240.dp).fillMaxWidth()
                                        .then(sticky.sourceModifier(sourceStyle)))
                            }
                        }
                        items(20) { Spacer(Modifier.fillMaxWidth().height(56.dp)) }
                    }
                    Box(Modifier.offset(x = hostLeft.dp, y = hostTop.dp).fillMaxWidth().height(96.dp)
                        .graphicsLayer { translationY = hostTranslation }
                        .layout { measurable, constraints ->
                            val placeable = measurable.measure(constraints)
                            layout(placeable.width, placeable.height) {
                                placeable.place(0, 0)
                                // Observe placement before global-position callbacks run;
                                // desktop render() may otherwise repair the position in one call.
                                if (showOverlay) {
                                    scene.semanticsOwners.flatMap { nodes(it.rootSemanticsNode) }.singleOrNull {
                                        it.config.getOrNull(SemanticsProperties.Text)?.any { text -> text.text == title } == true
                                    }?.let { placedTitlePositions += it.positionInRoot }
                                }
                            }
                        }) {
                        Box(Modifier.padding(horizontal = 50.dp).fillMaxWidth().height(40.dp)) {
                            sticky.Target(title, style = TextStyle(fontSize = 18.sp))
                        }
                        with(sticky) { Title(title, Color.White, visible = showOverlay) }
                    }
                }
            }
            fun render() = scene.render((time + 16_666_667L).also { time = it })
            fun frames() { repeat(4) { render().close() } }
            fun pixels(): ByteArray = render().use { image ->
                image.encodeToData(org.jetbrains.skia.EncodedImageFormat.PNG)!!.use { it.bytes }
            }
            try {
                frames()
                assertNotNull(scope).launch { assertNotNull(list).scrollToItem(0, 10) }
                frames()
                // The artist page creates the overlay only when scrolling begins. Inspect
                // its first paint, before global-position callbacks can correct a bad origin.
                repeat(2) {
                    placedTitlePositions.clear()
                    showOverlay = true
                    val firstFrame = pixels()
                    assertEquals(230f, placedTitlePositions.first().y,
                        "The title's first placement must already account for the header origin")
                    val titleNode = scene.semanticsOwners.flatMap { nodes(it.rootSemanticsNode) }.single {
                        it.config.getOrNull(SemanticsProperties.Text)?.any { text -> text.text == title } == true
                    }
                    assertEquals(230f, titleNode.positionInRoot.y,
                        "The first scroll frame must retain the hero title's window position")
                    assertEquals(titleNode.positionInRoot, placedTitlePositions.first(),
                        "The first placement must already have the final horizontal and vertical position")
                    frames()
                    assertContentEquals(firstFrame, pixels(),
                        "Creating the title must not produce a one-frame jump or flash")
                    showOverlay = false
                    frames()
                }
                showOverlay = true
                frames()
                val beforeTranslation = pixels()
                hostTranslation = 16f
                frames()
                assertContentEquals(beforeTranslation, pixels(),
                    "Moving the header's graphics layer must not displace the hero title")
            } finally {
                scene.close()
            }
        }
    }

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
