package org.simpmusic.lyrics.parser

import com.petedev.wavemusic.domain.extension.decodeHtmlEntities
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import org.simpmusic.lyrics.domain.Lyrics

fun parseSyncedLyrics(data: String): Lyrics {
    val regex = Regex("\\[(\\d{1,2}):(\\d{2})\\.(\\d{2,3})\\](.*)")
    val lines = data.lines()
    val linesLyrics = ArrayList<Lyrics.LyricsX.Line>()
    lines.map { line ->
        val matchResult = regex.matchEntire(line)
        if (matchResult != null) {
            val minutes = matchResult.groupValues[1].toLong()
            val seconds = matchResult.groupValues[2].toLong()
            val rawMilliseconds = matchResult.groupValues[3].toLong()
            val milliseconds = if (matchResult.groupValues[3].length == 3) rawMilliseconds else rawMilliseconds * 10
            val timeInMillis = minutes * 60_000L + seconds * 1000L + milliseconds
            // Trim, don't drop a fixed first character. Most providers leave a space after
            // "]", but LRCLIB packs the text right against it — "[00:27.12]boy, you got me"
            // — so removeRange(0, 1) ate the first letter of every single line.
            val rawContent = matchResult.groupValues[4]
            val content = if (rawContent.isBlank()) "♫" else rawContent.trimStart()
            linesLyrics.add(
                Lyrics.LyricsX.Line(
                    endTimeMs = "0",
                    startTimeMs = timeInMillis.toString(),
                    syllables = listOf(),
                    words = decodeHtmlEntities(content),
                ),
            )
        }
    }
    return Lyrics(
        lyrics =
            Lyrics.LyricsX(
                lines = linesLyrics,
                syncType = "LINE_SYNCED",
            ),
    )
}

fun parseRichSyncLyrics(data: String): Lyrics {
    // Unescape JSON string if needed (remove quotes and replace \n with actual newlines)
    val unescapedData =
        data
            .trim()
            .removePrefix("\"")
            .removeSuffix("\"")
            .replace("\\\\", "\\")
            .replace("\\n", "\n")
            .replace("\\r", "\r")
            .replace("\\t", "\t")

    // Handle different line separators (Unix \n, Windows \r\n, Mac \r)
    val lines = unescapedData.lines()
    // Skip offset line if present (starts with [offset:)
    val lyricsLines =
        lines.filter { line ->
            line.isNotBlank() && !line.trim().startsWith("[offset:")
        }

    println("[parseRichSyncLyrics] Total lines: ${lines.size}, Filtered lines: ${lyricsLines.size}")
    if (lyricsLines.isNotEmpty()) {
        println("[parseRichSyncLyrics] First line sample: ${lyricsLines.first()}")
    }

    // Regex to match [MM:SS.mm] format (flexible with 1-2 digits)
    val regex = Regex("\\[(\\d{1,2}):(\\d{2})\\.(\\d{2,3})\\](.+)")
    // Strip a leading voice marker (e.g. "v1:", "v2:") that some lyrics
    // providers prepend before the first word timestamp, so it doesn't leak
    // into the word content and stick to the first word.
    val voiceMarkerRegex = Regex("""^v\d+:""")
    val linesLyrics = ArrayList<Lyrics.LyricsX.Line>()

    lyricsLines.forEachIndexed { index, line ->
        val matchResult = regex.matchEntire(line.trim())
        if (matchResult != null) {
            val minutes = matchResult.groupValues[1].toLongOrNull() ?: 0L
            val seconds = matchResult.groupValues[2].toLongOrNull() ?: 0L
            val centiseconds = matchResult.groupValues[3].toLongOrNull() ?: 0L

            // Convert to milliseconds
            // If centiseconds has 3 digits (milliseconds), use directly
            // If 2 digits (centiseconds), multiply by 10
            val millisPart = if (matchResult.groupValues[3].length == 3) centiseconds else centiseconds * 10
            val timeInMillis = minutes * 60_000L + seconds * 1000L + millisPart

            // Keep the rich sync content as-is (with <MM:SS.mm> word format)
            val content =
                matchResult.groupValues[4]
                    .trimStart()
                    .replace(voiceMarkerRegex, "")
                    .trimStart()

            if (content.isNotBlank()) {
                linesLyrics.add(
                    Lyrics.LyricsX.Line(
                        endTimeMs = "0",
                        startTimeMs = timeInMillis.toString(),
                        syllables = listOf(),
                        words = content,
                    ),
                )
            }
        } else {
            if (index < 3) { // Only log first 3 failed matches to avoid spam
                println("[parseRichSyncLyrics] Line $index failed to match: '${line.take(100)}'")
            }
        }
    }

    println("[parseRichSyncLyrics] Parsed ${linesLyrics.size} lines successfully")

    return Lyrics(
        lyrics =
            Lyrics.LyricsX(
                lines = linesLyrics,
                syncType = "RICH_SYNCED",
            ),
    )
}

/**
 * Parse QQ Music's QRC document (as returned by BetterLyrics Portato) into the player's
 * inline-timestamp rich-sync format. QRC places each word's timing token *after* its text.
 */
fun parseQrcLyrics(
    data: String,
    title: String? = null,
    artist: String? = null,
): Lyrics {
    val envelope = Regex("""LyricContent="([\s\S]*?)"\s*(?:/?>|[a-zA-Z]+=)""").find(data)
    val body =
        (envelope?.groupValues?.getOrNull(1) ?: data)
            .replace("&#10;", "\n")
            .replace("&#13;", "\r")
            .replace("&quot;", "\"")
            .replace("&apos;", "'")
            .replace("&#39;", "'")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
    val lineHeader = Regex("^\\[(\\d+),(\\d+)\\]")
    val wordTiming = Regex("\\((\\d+),(\\d+)\\)")
    val creditLine = Regex("(?i)^(lyrics|composed|arranged|produced|written|作词|作曲|编曲|制作人)\\s*(by)?\\s*[:：]")
    val titleKey = title?.normalizedLyricMetadata()
    val artistKey = artist?.normalizedLyricMetadata()
    val lines = ArrayList<Lyrics.LyricsX.Line>()

    body.lineSequence().forEach { rawLine ->
        val line = rawLine.trim()
        val header = lineHeader.find(line) ?: return@forEach
        val startMs = header.groupValues[1].toLongOrNull() ?: return@forEach
        val durationMs = header.groupValues[2].toLongOrNull() ?: 0L
        val content = line.substring(header.range.last + 1)
        val timings = wordTiming.findAll(content).toList()
        if (timings.isEmpty()) return@forEach

        val richWords = buildString {
            var contentStart = 0
            timings.forEachIndexed { index, timing ->
                val text = content.substring(contentStart, timing.range.first)
                val wordStart = timing.groupValues[1].toLongOrNull() ?: startMs
                val timestamp = formatMsToLrc(wordStart)
                if (text.isNotBlank()) append("<").append(timestamp).append(">").append(text)
                contentStart = timing.range.last + 1
                // The trailing fragment, if any, has no following QRC token. Retain it with the
                // line's end time so valid provider text isn't silently discarded.
                if (index == timings.lastIndex) {
                    val tail = content.substring(contentStart).trim()
                    if (tail.isNotEmpty()) append("<").append(formatMsToLrc(startMs + durationMs)).append(">").append(tail)
                }
            }
        }.trim()
        val plainText = richWords.replace(Regex("<\\d{2}:\\d{2}\\.\\d{2,3}>"), "").trim()
        val key = plainText.normalizedLyricMetadata()
        if (plainText.isBlank() || creditLine.containsMatchIn(plainText) ||
            (titleKey != null && key == titleKey) || (artistKey != null && key == artistKey)
        ) return@forEach

        lines +=
            Lyrics.LyricsX.Line(
                startTimeMs = startMs.toString(),
                endTimeMs = (startMs + durationMs).toString(),
                syllables = emptyList(),
                words = richWords,
            )
    }

    return Lyrics(lyrics = Lyrics.LyricsX(lines = lines, syncType = "RICH_SYNCED"))
}

private fun String.normalizedLyricMetadata(): String =
    lowercase()
        .filter { it.isLetterOrDigit() }

/** Parse the LyricsPlus v2 JSON response, preserving its optional word/syllable timings. */
fun parseLyricsPlusLyrics(data: String): Lyrics {
    val root = Json.parseToJsonElement(data).jsonObject
    val payload = root["data"]?.let { runCatching { it.jsonObject }.getOrNull() } ?: root
    val entries = payload["lyrics"] as? JsonArray ?: return Lyrics(lyrics = Lyrics.LyricsX(emptyList(), "UNSYNCED"))
    val responseTypeIsWord = payload["type"]?.jsonPrimitive?.contentOrNull.equals("word", ignoreCase = true)
    var hasWordTiming = false
    val lines = entries.mapNotNull { entry ->
        val item = runCatching { entry.jsonObject }.getOrNull() ?: return@mapNotNull null
        val startMs = item["time"]?.jsonPrimitive?.longOrNull ?: return@mapNotNull null
        val durationMs = item["duration"]?.jsonPrimitive?.longOrNull ?: 0L
        val text = item["text"]?.jsonPrimitive?.contentOrNull.orEmpty()
        if (text.isBlank()) return@mapNotNull null
        val syllables = item["syllabus"] as? JsonArray
        val timedParts = syllables?.mapNotNull { part ->
            val word = runCatching { part.jsonObject }.getOrNull() ?: return@mapNotNull null
            val wordStart = word["time"]?.jsonPrimitive?.longOrNull ?: return@mapNotNull null
            val wordText = word["text"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
            wordStart to decodeHtmlEntities(wordText)
        }.orEmpty().filter { it.second.isNotBlank() }
        val richWords = timedParts.takeIf { it.isNotEmpty() }?.let {
            richTextWithLineBoundaries(it, decodeHtmlEntities(text))
        }
        if (richWords != null) hasWordTiming = true
        Lyrics.LyricsX.Line(
            startTimeMs = startMs.toString(),
            endTimeMs = (startMs + durationMs).toString(),
            syllables = emptyList(),
            words = richWords ?: text,
        )
    }
    val syncType = if (responseTypeIsWord && hasWordTiming) "RICH_SYNCED" else "LINE_SYNCED"
    return Lyrics(lyrics = Lyrics.LyricsX(lines, syncType))
}

/** Use the provider's complete line for spaces only when all non-space characters agree. */
private fun richTextWithLineBoundaries(parts: List<Pair<Long, String>>, text: String): String {
    val segmentsText = parts.joinToString("") { it.second }.filterNot(Char::isWhitespace)
    if (segmentsText != text.filterNot(Char::isWhitespace)) {
        return parts.joinToString("") { (time, segment) -> "<${formatMsToLrc(time)}>$segment" }
    }
    return buildString {
        var cursor = 0
        for ((time, segment) in parts) {
            while (cursor < text.length && text[cursor].isWhitespace()) append(text[cursor++])
            append('<').append(formatMsToLrc(time)).append('>')
            val start = cursor
            var remaining = segment.count { !it.isWhitespace() }
            while (remaining > 0 && cursor < text.length) {
                if (!text[cursor++].isWhitespace()) remaining--
            }
            append(text.substring(start, cursor))
        }
        append(text.substring(cursor))
    }
}

/**
 * Parse TTML (Timed Text Markup Language) lyrics from BetterLyrics.
 * Supports both line-synced and word-by-word synced lyrics.
 *
 * TTML format: `<p begin="M:SS.mmm" end="M:SS.mmm">` contains `<span begin="..." end="...">word</span>`
 * If spans with timing exist → word-by-word (RICH_SYNCED)
 * If no spans → line-synced (LINE_SYNCED)
 */
fun parseTtmlLyrics(data: String): Lyrics {
    val lines = mutableListOf<Lyrics.LyricsX.Line>()
    var hasWordTiming = false
    val paragraph = Regex("""<(?:[\w-]+:)?p\b([^>]*)>([\s\S]*?)</(?:[\w-]+:)?p\s*>""")
    val tokens = Regex("""<!--[\s\S]*?-->|<!\[CDATA\[[\s\S]*?]]>|<[^>]+>|[^<]+""")
    val tagName = Regex("""^</?\s*([\w:-]+)""")
    val timeMarker = Regex("""<\d{2,}:\d{2}\.\d{2,3}>""")

    for (p in paragraph.findAll(data)) {
        val attributes = ttmlAttributes(p.groupValues[1])
        val begin = attributes["begin"]?.let(::parseTtmlTime) ?: continue
        val end = attributes["end"]?.let(::parseTtmlTime) ?: begin
        val contexts = mutableListOf<TtmlInlineContext>()
        val content = StringBuilder()
        var previousTime: Long? = null
        var lineHasWordTiming = false

        fun appendText(raw: String, cdata: Boolean = false) {
            if (contexts.lastOrNull()?.ignored == true) return
            // Newline-only nodes between tags are pretty-print indentation. Real separators
            // are spaces in inline text, between spans, or in explicit whitespace spans.
            val decoded = if (cdata) raw else decodeHtmlEntities(raw)
            if (!cdata && decoded.isBlank() && ('\n' in decoded || '\r' in decoded)) return
            val text = decoded.replace(Regex("[\\t\\r\\n ]+"), " ")
            if (text.isEmpty()) return
            if (text.isNotBlank()) {
                val context = contexts.lastOrNull()
                val time = context?.time ?: begin
                if (previousTime != time) {
                    content.append('<').append(formatMsToLrc(time)).append('>')
                    previousTime = time
                }
                if (context?.timed == true) lineHasWordTiming = true
            }
            content.append(text)
        }

        for (match in tokens.findAll(p.groupValues[2])) {
            val token = match.value
            when {
                token.startsWith("<!--") -> Unit
                token.startsWith("<![CDATA[") -> appendText(token.removePrefix("<![CDATA[").removeSuffix("]]>"), true)
                token.startsWith("</") -> if (contexts.isNotEmpty()) contexts.removeAt(contexts.lastIndex)
                token.startsWith('<') -> {
                    val name = tagName.find(token)?.groupValues?.get(1)?.substringAfter(':') ?: continue
                    if (name == "br") { appendText(" "); continue }
                    if (token.endsWith("/>")) continue
                    val attrs = ttmlAttributes(token)
                    val parent = contexts.lastOrNull()
                    val spanTime = attrs["begin"]?.let(::parseTtmlTime)
                    val role = attrs.entries.firstOrNull { it.key.substringAfter(':') == "role" }?.value
                    contexts += TtmlInlineContext(
                        time = spanTime ?: parent?.time ?: begin,
                        timed = spanTime != null || parent?.timed == true,
                        ignored = parent?.ignored == true || role == "x-translation" || role == "x-roman",
                    )
                }
                else -> appendText(token)
            }
        }
        val words = content.toString().trim()
        if (words.isNotBlank()) {
            lines += Lyrics.LyricsX.Line(
                startTimeMs = begin.toString(),
                endTimeMs = end.toString(),
                syllables = emptyList(),
                words = words,
            )
            hasWordTiming = hasWordTiming || lineHasWordTiming
        }
    }
    return Lyrics(lyrics = Lyrics.LyricsX(
        lines = if (hasWordTiming) lines else lines.map { it.copy(words = it.words.replace(timeMarker, "").trim()) },
        syncType = if (hasWordTiming) "RICH_SYNCED" else "LINE_SYNCED",
    ))
}

private data class TtmlInlineContext(val time: Long, val timed: Boolean, val ignored: Boolean)

private fun ttmlAttributes(tag: String): Map<String, String> =
    Regex("""([\w:-]+)\s*=\s*(?:"([^"]*)"|'([^']*)')""").findAll(tag).associate {
        it.groupValues[1] to (it.groupValues[2].ifEmpty { it.groupValues[3] })
    }

/**
 * Parse TTML time format to milliseconds.
 * Supports: "M:SS.mmm", "MM:SS.mmm", "H:MM:SS.mmm", "SS.mmm"
 */
private fun parseTtmlTime(time: String): Long {
    if (time.endsWith("ms")) return time.removeSuffix("ms").toDoubleOrNull()?.toLong() ?: 0L
    if (time.endsWith('s')) return time.removeSuffix("s").toDoubleOrNull()?.let { (it * 1000).toLong() } ?: 0L
    val parts = time.split(":")
    return when (parts.size) {
        3 -> {
            val hours = parts[0].toLongOrNull() ?: 0L
            val minutes = parts[1].toLongOrNull() ?: 0L
            val secParts = parts[2].split(".")
            val seconds = secParts[0].toLongOrNull() ?: 0L
            val millis = parseMillisPart(secParts.getOrNull(1))
            hours * 3_600_000L + minutes * 60_000L + seconds * 1000L + millis
        }
        2 -> {
            val minutes = parts[0].toLongOrNull() ?: 0L
            val secParts = parts[1].split(".")
            val seconds = secParts[0].toLongOrNull() ?: 0L
            val millis = parseMillisPart(secParts.getOrNull(1))
            minutes * 60_000L + seconds * 1000L + millis
        }
        else -> {
            val secParts = time.split(".")
            val seconds = secParts[0].toLongOrNull() ?: 0L
            val millis = parseMillisPart(secParts.getOrNull(1))
            seconds * 1000L + millis
        }
    }
}

private fun parseMillisPart(part: String?): Long {
    if (part.isNullOrEmpty()) return 0L
    return part.take(3).padEnd(3, '0').toLongOrNull() ?: 0L
}

private fun formatMsToLrc(ms: Long): String {
    val minutes = ms / 60_000L
    val seconds = (ms % 60_000L) / 1000L
    val centis = (ms % 1000L) / 10L
    val m = if (minutes < 10) "0$minutes" else "$minutes"
    val s = if (seconds < 10) "0$seconds" else "$seconds"
    val c = if (centis < 10) "0$centis" else "$centis"
    return "$m:$s.$c"
}

fun parseUnsyncedLyrics(data: String): Lyrics {
    val lines = data.lines()
    val linesLyrics = ArrayList<Lyrics.LyricsX.Line>()
    lines.map { line ->
        linesLyrics.add(
            Lyrics.LyricsX.Line(
                endTimeMs = "0",
                startTimeMs = "0",
                syllables = listOf(),
                words = line,
            ),
        )
    }
    return Lyrics(
        lyrics =
            Lyrics.LyricsX(
                lines = linesLyrics,
                syncType = "UNSYNCED",
            ),
    )
}
