package com.petedev.wavemusic.extension

import com.petedev.wavemusic.domain.extension.decodeHtmlEntities

/**
 * Represents a single word with its timing information for rich sync lyrics
 */
data class WordTiming(
    val text: String,
    val startTimeMs: Long,
    /** A real space in the source, rather than merely another syllable timestamp. */
    val startsWord: Boolean = true,
)

/**
 * Represents a parsed rich sync line with all word timings
 */
data class ParsedRichSyncLine(
    val words: List<WordTiming>,
    val lineStartTimeMs: Long,
    val lineEndTimeMs: Long,
)

/**
 * Parses rich sync lyrics format into word-level timing data
 *
 * Expected format: <MM:SS.mm> word <MM:SS.mm> word ...
 * Example: <00:16.62> Và <00:16.64> em <00:16.68> nói
 *
 * @param words The rich sync string containing timestamps and words
 * @param lineStartTimeMs The start time of the line as a string
 * @param lineEndTimeMs The end time of the line as a string
 * @return ParsedRichSyncLine if parsing succeeds, null if parsing fails or input is invalid
 */
fun parseRichSyncWords(
    words: String,
    lineStartTimeMs: String,
    lineEndTimeMs: String,
): ParsedRichSyncLine? {
    // Handle edge cases
    if (words.isBlank()) {
        println("[parseRichSyncWords] Input is blank")
        return null
    }

    println("[parseRichSyncWords] Input preview: ${words.take(100)}")

    // Strategy: Find all timestamps first, then extract text between them
    // Regex to match timestamp only: <MM:SS.mm> or <MM:SS.mmm>
    val timestampRegex = Regex("""<(\d{2}):(\d{2})\.(\d{2,3})>""")

    val wordTimings = mutableListOf<WordTiming>()
    var pendingWordBoundary = true

    // Find all timestamp matches with their positions
    val timestamps = timestampRegex.findAll(words).toList()

    timestamps.forEachIndexed { index, match ->
        val (minutes, seconds, fraction) = match.destructured

        // Convert time to milliseconds
        // If 2 digits (centiseconds): multiply by 10 to get ms (e.g., 62 -> 620ms)
        // If 3 digits (milliseconds): use as-is (e.g., 620 -> 620ms)
        val fractionMs = fraction.toLongOrNull() ?: 0L
        val timeMs =
            (minutes.toLongOrNull() ?: 0L) * 60000L +
                (seconds.toLongOrNull() ?: 0L) * 1000L +
                if (fraction.length == 2) fractionMs * 10L else fractionMs

        // Extract text after this timestamp until the next timestamp (or end of string)
        val startPos = match.range.last + 1
        val endPos =
            if (index < timestamps.size - 1) {
                timestamps[index + 1].range.first
            } else {
                words.length
            }

        // Keep the whitespace evidence before trimming: adjacent timestamps can split a single
        // word into syllables. A timestamp itself never introduces a display-space.
        val sourceText = decodeHtmlEntities(words.substring(startPos, endPos))
        val textBetween = sourceText.trim()

        // Only add if there's actual text (not just whitespace or empty)
        if (textBetween.isNotBlank()) {
            wordTimings.add(WordTiming(
                text = textBetween,
                startTimeMs = timeMs,
                startsWord = wordTimings.isEmpty() || pendingWordBoundary || sourceText.first().isWhitespace(),
            ))
            pendingWordBoundary = sourceText.last().isWhitespace()
        } else if (sourceText.any { it.isWhitespace() }) {
            pendingWordBoundary = true
        }
    }

    // If no valid words were parsed, return null (fallback to LINE_SYNCED)
    if (wordTimings.isEmpty()) {
        println("[parseRichSyncWords] No words matched the regex")
        return null
    }

    println("[parseRichSyncWords] Successfully parsed ${wordTimings.size} words")

    // Parse line timing
    val lineStart = lineStartTimeMs.toLongOrNull() ?: 0L
    val lineEnd = lineEndTimeMs.toLongOrNull() ?: Long.MAX_VALUE

    return ParsedRichSyncLine(
        words = wordTimings,
        lineStartTimeMs = lineStart,
        lineEndTimeMs = lineEnd,
    )
}

/** Keep Latin syllables together for wrapping; CJK segments retain their natural break points. */
fun ParsedRichSyncLine.wordGroups(): List<List<IndexedValue<WordTiming>>> {
    val groups = mutableListOf<MutableList<IndexedValue<WordTiming>>>()
    words.withIndex().forEach { segment ->
        val first = segment.value.text.firstOrNull()
        val cjkBreak = first != null && (first in '\u3400'..'\u9fff' || first in '\u3040'..'\u30ff' ||
            first in '\uac00'..'\ud7af')
        if (groups.isEmpty() || segment.value.startsWord || cjkBreak) groups += mutableListOf<IndexedValue<WordTiming>>()
        groups.last() += segment
    }
    return groups
}
