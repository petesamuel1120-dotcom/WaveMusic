@file:OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)

package com.maxrave.simpmusic.ui.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maxrave.simpmusic.extension.parseRichSyncWords
import com.maxrave.simpmusic.extension.wordGroups
import com.maxrave.simpmusic.ui.screen.player.content.stripRichSyncTimestamps
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RichSyncWordBoundariesTest {
    private val line = "<00:01.00>An <00:01.20>me<00:01.40>di<00:01.60>cal <00:02.00>tool."

    @Test
    fun parsingKeepsBoundariesAndIndividualTimings() {
        val parsed = requireNotNull(parseRichSyncWords(line, "1000", "3000"))
        assertEquals(listOf(true, true, false, false, true), parsed.words.map { it.startsWord })
        assertEquals(listOf(1000L, 1200L, 1400L, 1600L, 2000L), parsed.words.map { it.startTimeMs })
        assertEquals(listOf("An", "medical", "tool."), parsed.wordGroups().map { group -> group.joinToString("") { it.value.text } })
        assertEquals("An medical tool.", line.stripRichSyncTimestamps())
    }

    @Test
    fun leadingAndStandaloneWhitespaceAreWordBoundaries() {
        val parsed = requireNotNull(parseRichSyncWords("<00:01.00>Good<00:01.50> <00:02.00>morning", "1000", "3000"))
        assertEquals(listOf(true, true), parsed.words.map { it.startsWord })
        assertEquals("Good morning", "<00:01.00>Good<00:02.00> morning".stripRichSyncTimestamps())
    }

    @Test
    fun cjkCanWrapWithoutArtificialSpaces() {
        val parsed = requireNotNull(parseRichSyncWords("<00:01.00>你<00:02.00>好", "1000", "3000"))
        assertEquals(2, parsed.wordGroups().size)
        assertEquals(listOf(true, false), parsed.words.map { it.startsWord })
        assertEquals("你好", "<00:01.00>你<00:02.00>好".stripRichSyncTimestamps())
    }

    @Test
    fun renderedSyllablesTouchAndWrapAsACompleteWord() {
        for (direction in listOf(LayoutDirection.Ltr, LayoutDirection.Rtl)) {
            val parsed = requireNotNull(parseRichSyncWords(line, "1000", "3000"))
            val scene = ImageComposeScene(90, 240) {
                MaterialTheme {
                    CompositionLocalProvider(LocalLayoutDirection provides direction) {
                        RichSyncLyricsLineItem(parsed, null, currentTimeMs = 0L, isCurrent = false,
                            customFontSize = 20.sp, customPadding = 0.dp)
                    }
                }
            }
            fun descendants(node: SemanticsNode): List<SemanticsNode> = listOf(node) + node.children.flatMap(::descendants)
            fun bounds(text: String) = scene.semanticsOwners.flatMap { descendants(it.rootSemanticsNode) }.first {
                it.config.getOrNull(SemanticsProperties.Text)?.any { value -> value.text == text } == true
            }.boundsInRoot
            try {
                repeat(4) { scene.render(it * 16_666_667L).close() }
                val me = bounds("me")
                val di = bounds("di")
                val cal = bounds("cal")
                assertEquals(me.top, di.top)
                assertEquals(me.top, cal.top)
                assertEquals(me.right, di.left, 0.01f)
                assertEquals(di.right, cal.left, 0.01f)
                assertTrue(bounds("tool.").top > me.top, "The next word should wrap, not the timed syllables")
            } finally {
                scene.close()
            }
        }
    }
}
