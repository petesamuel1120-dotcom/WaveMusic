package org.simpmusic.lyrics.parser

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LyricsWordBoundariesTest {
    private fun plain(words: String) = words.replace(Regex("<\\d{2,}:\\d{2}\\.\\d{2,3}>"), "").trim()

    @Test
    fun adjacentTimedSyllablesStayJoinedAndKeepTheirTiming() {
        val lyrics = parseTtmlLyrics("""<tt><p begin="1s" end="3s"><span begin="1s">A </span><span begin="1.2s">me</span><span begin="1.4s">di</span><span begin="1.6s">cal</span> <span begin="2s">tool.</span></p></tt>""")
        assertEquals("RICH_SYNCED", lyrics.lyrics!!.syncType)
        val line = lyrics.lyrics!!.lines!!.single()
        assertEquals("1000", line.startTimeMs)
        assertEquals("3000", line.endTimeMs)
        assertEquals("A medical tool.", plain(line.words))
        assertTrue(line.words.contains("<00:01.20>me<00:01.40>di<00:01.60>cal"))
    }

    @Test
    fun realSpacesInsideOutsideAndBetweenSpansArePreserved() {
        for (separator in listOf(" ", "<span> </span>", "<span begin='1500ms'> </span>")) {
            val lyrics = parseTtmlLyrics("""<p end='3s' begin='1s'><span begin='1s'>Good</span>$separator<span begin='2s'>morning</span></p>""")
            assertEquals("Good morning", plain(lyrics.lyrics!!.lines!!.single().words))
        }
        val lyrics = parseTtmlLyrics("""<p begin='1s'><span begin='1s'>Good </span><span begin='2s'>morning</span></p>""")
        assertEquals("Good morning", plain(lyrics.lyrics!!.lines!!.single().words))
    }

    @Test
    fun indentationDoesNotTurnSyllablesIntoWords() {
        val lyrics = parseTtmlLyrics("""
            <tt:p end="3s" begin="1s">
                <tt:span begin="1s">me</tt:span>
                <tt:span begin="1.2s">di</tt:span>
                <tt:span begin="1.4s">cal</tt:span>
            </tt:p>
        """.trimIndent())
        assertEquals("medical", plain(lyrics.lyrics!!.lines!!.single().words))
    }

    @Test
    fun nestedStylesInheritTimingAndSecondaryTextIsExcluded() {
        val lyrics = parseTtmlLyrics("""<p begin="00:01.000" end="00:03.000"><span begin="00:01.200"><span style="bold">bright</span></span> <span begin="00:02.000">&amp; clear</span><span ttm:role="x-translation">translated words</span><span ttm:role="x-roman">romanization</span></p>""")
        val words = lyrics.lyrics!!.lines!!.single().words
        assertEquals("bright & clear", plain(words))
        assertTrue(words.startsWith("<00:01.20>bright"))
    }

    @Test
    fun lineOnlyLyricsAndCjkDoNotGainSpaces() {
        val plainLyrics = parseTtmlLyrics("""<p end="02:03.4567" begin="01:02.3">Keep <span style="bold">these</span> words.</p>""")
        assertEquals("LINE_SYNCED", plainLyrics.lyrics!!.syncType)
        assertEquals("Keep these words.", plainLyrics.lyrics!!.lines!!.single().words)
        assertEquals("62300", plainLyrics.lyrics!!.lines!!.single().startTimeMs)
        assertEquals("123456", plainLyrics.lyrics!!.lines!!.single().endTimeMs)
        val cjk = parseTtmlLyrics("""<p begin="1s"><span begin="1s">你</span><span begin="2s">好</span></p>""")
        assertEquals("你好", plain(cjk.lyrics!!.lines!!.single().words))
    }

    @Test
    fun lyricsPlusUsesFullLineSpacesWithoutDroppingSyllableTimes() {
        val lyrics = parseLyricsPlusLyrics("""{"type":"word","lyrics":[{"time":1000,"duration":2000,"text":"A medical tool.","syllabus":[{"time":1000,"text":"A"},{"time":1200,"text":"me"},{"time":1400,"text":"di"},{"time":1600,"text":"cal"},{"time":2000,"text":"tool."}]}]}""")
        assertEquals("RICH_SYNCED", lyrics.lyrics!!.syncType)
        val words = lyrics.lyrics!!.lines!!.single().words
        assertEquals("A medical tool.", plain(words))
        assertTrue(words.contains("<00:01.20>me<00:01.40>di<00:01.60>cal"))
        assertTrue(words.contains("cal <00:02.00>tool."))
    }

    @Test
    fun lyricsPlusDoesNotGuessWhenTheLineAndSegmentsDisagree() {
        val lyrics = parseLyricsPlusLyrics("""{"type":"word","lyrics":[{"time":1000,"text":"Different words","syllabus":[{"time":1000,"text":"Original "},{"time":2000,"text":"text"}]}]}""")
        assertEquals("Original text", plain(lyrics.lyrics!!.lines!!.single().words))
    }
}
