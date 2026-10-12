@file:OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)

package com.petedev.wavemusic.ui.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.skia.Bitmap
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

class SlideV2PlaylistTitleTest {
    @Test
    fun favoriteTitleFollowsThePageFadeWithoutSliding() = exercise("Favorite")

    @Test
    fun downloadedTitleFollowsThePageFadeWithoutSliding() = exercise("Downloaded")

    private data class TitlePresentation(val sticky: StickyPlaylistTitleState, val transition: SlideV2TitleTransition)

    private fun exercise(title: String) {
        val showing = mutableStateOf(false)
        val presentation = mutableStateOf<TitlePresentation?>(null)
        var time = 0L
        val scene = ImageComposeScene(360, 400) {
            Box(Modifier.fillMaxSize().background(Color.Black)) {
                AnimatedContent(
                    targetState = showing.value,
                    modifier = Modifier.fillMaxSize(),
                    transitionSpec = { (slideV2EnterTransition(true) togetherWith slideV2ExitTransition(true)).using(null) },
                ) { visible ->
                    if (visible) {
                        val list = rememberLazyListState()
                        val sticky = remember(list) { StickyPlaylistTitleState(list, "hero") }
                        val transition = slideV2TitleTransition(this)
                        // SharePageHeader uses rememberUpdatedState, publishing during composition.
                        presentation.value = TitlePresentation(sticky, transition)
                        Box(Modifier.fillMaxSize()) {
                            LazyColumn(state = list, modifier = Modifier.fillMaxSize().then(sticky.viewportModifier)) {
                                item(key = "hero") {
                                    val style = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold)
                                    Box(Modifier.fillMaxWidth().height(300.dp)) {
                                        Text(title, style = style, color = Color.White,
                                            modifier = Modifier.offset(y = 180.dp).fillMaxWidth().then(
                                                sticky.sourceModifier(style, hideSource = true),
                                            ))
                                    }
                                }
                            }
                            // This marker inherits the page's real Slide V2 graphics-layer alpha.
                            Box(Modifier.offset(x = 160.dp, y = 340.dp).size(20.dp).background(Color.White))
                        }
                    } else Box(Modifier.fillMaxSize())
                }
                presentation.value?.let { state ->
                    // The real shared-header host is shorter than the hero title's position.
                    Box(Modifier.fillMaxWidth().height(56.dp)) {
                    Box(Modifier.offset(y = 32.dp).padding(horizontal = 50.dp).fillMaxWidth().height(40.dp)) {
                        state.sticky.Target(title, style = TextStyle(fontSize = 20.sp))
                    }
                    with(state.sticky) {
                        Title(title, Color.White, transitionAlphaState = state.transition.alpha,
                            freezePosition = state.transition.inProgress, ignoreViewportTranslation = true)
                    }
                    }
                }
            }
        }
        fun render(): Pair<Int, Int> {
            time += 20_000_000L
            val image = scene.render(time)
            val bitmap = Bitmap.makeFromImage(image)
            return try {
                fun brightness(top: Int, bottom: Int): Int = (top until bottom).maxOf { y ->
                    (0 until 360).maxOf { x -> bitmap.getColor(x, y) and 255 }
                }
                brightness(175, 235) to brightness(335, 365)
            } finally { bitmap.close(); image.close() }
        }
        fun nodes(node: SemanticsNode): List<SemanticsNode> = listOf(node) + node.children.flatMap(::nodes)
        fun titleBounds(): Rect = scene.semanticsOwners.flatMap { nodes(it.rootSemanticsNode) }.single {
            it.config.getOrNull(SemanticsProperties.Text)?.any { text -> text.text == title } == true
        }.boundsInRoot
        try {
            repeat(4) { render() }
            for (visible in listOf(true, false)) {
                showing.value = visible
                var bounds: Rect? = null
                repeat(18) { frame ->
                    val (titleBrightness, pageBrightness) = render()
                    assertTrue(abs(titleBrightness - pageBrightness) <= 3,
                        "$title fade differs from the page: visible=$visible frame=$frame title=$titleBrightness page=$pageBrightness alpha=${presentation.value?.transition?.alpha?.value}")
                    if (pageBrightness > 5) {
                        val current = titleBounds()
                        val previous = bounds
                        if (previous != null) {
                            assertTrue(abs(current.left - previous.left) <= 1f && abs(current.top - previous.top) <= 1f,
                                "$title moved during its fade: visible=$visible frame=$frame previous=$previous current=$current")
                        }
                        bounds = current
                    }
                }
            }
        } finally { scene.close() }
    }
}
