@file:OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)

package com.petedev.wavemusic.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SlimDockDirectionTest {
    @Test
    fun physicalDockPositionsStayOnScreenInBothTextDirections() {
        for (screenWidth in listOf(320, 360, 450)) {
            for (compact in listOf(false, true)) {
                fun measure(direction: LayoutDirection): List<Rect> {
                    val bounds = mutableMapOf<Int, Rect>()
                    val scene = ImageComposeScene(screenWidth, 200) {
                        CompositionLocalProvider(LocalLayoutDirection provides direction) {
                            Box(Modifier.fillMaxSize()) {
                                val playerWidth = if (compact) screenWidth - 136 else screenWidth - 24
                                val slots = listOf(
                                    Triple(12, 100, if (compact) 48 else screenWidth - 96),
                                    Triple(screenWidth - 72, 100, 60),
                                    Triple((screenWidth - playerWidth) / 2, if (compact) 100 else 20, playerWidth),
                                )
                                slots.forEachIndexed { index, (x, y, width) ->
                                    Box(dockPosition(x.dp, y.dp).size(width.dp, 56.dp)
                                        .onGloballyPositioned { bounds[index] = it.boundsInRoot() })
                                }
                            }
                        }
                    }
                    try {
                        repeat(3) { scene.render(it * 16_666_667L).close() }
                        return (0..2).map { bounds.getValue(it) }
                    } finally {
                        scene.close()
                    }
                }
                val ltr = measure(LayoutDirection.Ltr)
                val rtl = measure(LayoutDirection.Rtl)
                assertEquals(ltr, rtl)
                for (bounds in rtl) {
                    assertTrue(bounds.left >= 0f && bounds.right <= screenWidth,
                        "Dock surface extends off-screen: $bounds at width $screenWidth")
                }
            }
        }
    }
}
