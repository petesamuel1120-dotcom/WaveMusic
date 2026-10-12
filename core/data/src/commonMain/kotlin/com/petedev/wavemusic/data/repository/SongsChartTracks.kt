package com.petedev.wavemusic.data.repository

import com.petedev.wavemusic.data.mapping.toTrack
import com.petedev.wavemusic.domain.data.model.browse.album.Track
import com.petedev.wavemusic.domain.utils.MusicVideoType
import com.petedev.wavemusic.kotlinytmusicscraper.YouTube
import com.petedev.wavemusic.kotlinytmusicscraper.models.SongItem
import com.petedev.wavemusic.kotlinytmusicscraper.models.WatchEndpoint
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

/** Keep chart order while selecting the audio rendition of each ranked recording. */
internal suspend fun YouTube.songsChartTracks(playlistId: String, tracks: List<Track>): List<Track> {
    if (tracks.all { MusicVideoType.isAudio(it.videoType) }) return tracks
    val audioByOriginalId = mutableMapOf<String, Track>()
    val rankedIds = tracks.map { it.videoId }.toSet()
    var continuation: String? = null
    val seenTokens = mutableSetOf<String>()
    var pages = 0
    var hasRankedItems = false
    do {
        val page = next(
            WatchEndpoint(playlistId = playlistId.removePrefix("VL")),
            continuation = continuation,
        ).getOrElse { error ->
            if (error is CancellationException) throw error
            null
        } ?: break
        pages++
        hasRankedItems = page.items.any { it.id in rankedIds || it.counterpart?.id in rankedIds }
        page.items.forEach { item ->
            val audio = sequenceOf(item, item.counterpart)
                .filterNotNull()
                .firstOrNull { MusicVideoType.isAudio(it.musicVideoType) }
                ?.toChartTrack()
            if (audio != null) {
                sequenceOf(item.id, item.counterpart?.id).filterNotNull()
                    .filter { it in rankedIds }
                    .forEach { audioByOriginalId[it] = audio }
            }
        }
        continuation = page.continuation
    } while (pages < 10 && continuation != null && seenTokens.add(continuation!!) && hasRankedItems &&
        tracks.any { !MusicVideoType.isAudio(it.videoType) && it.videoId !in audioByOriginalId })

    // Counterparts may be absent for signed-out accounts. Restrict the fallback to
    // the Songs search filter and matching recording/artist metadata.
    val requests = Semaphore(4)
    val resolved = coroutineScope {
        tracks.map { original ->
            async {
                if (MusicVideoType.isAudio(original.videoType)) original
                else audioByOriginalId[original.videoId] ?: requests.withPermit {
                    val artists = original.artists.orEmpty().map { normalizeChartArtist(it.name) }.toSet()
                    val query = (listOf(original.title) + original.artists.orEmpty().map { it.name }).joinToString(" ")
                    val results = search(query, YouTube.SearchFilter.FILTER_SONG).getOrElse { error ->
                        if (error is CancellationException) throw error
                        null
                    }
                    results?.items?.filterIsInstance<SongItem>()?.firstOrNull { candidate ->
                        normalizeChartRecording(candidate.title, artists) == normalizeChartRecording(original.title, artists) &&
                            candidate.artists.any { normalizeChartArtist(it.name) in artists } &&
                            (!original.isExplicit || candidate.explicit) &&
                            MusicVideoType.isAudio(candidate.musicVideoType)
                    }?.toChartTrack()
                }
            }
        }.awaitAll().filterNotNull()
    }
    check(resolved.isNotEmpty() || tracks.isEmpty()) { "Could not load audio versions for this songs chart" }
    return resolved
}

private fun normalizeChartName(value: String): String = value.lowercase()
    .replace(Regex("\\((?:official\\s+)?(?:music\\s+)?(?:video|audio|lyric(?:s| video)?)\\)|\\[(?:official\\s+)?(?:music\\s+)?(?:video|audio|lyrics?)\\]"), "")
    .filter { it.isLetterOrDigit() }

private fun normalizeChartArtist(value: String): String =
    normalizeChartName(value.lowercase().removeSuffix("vevo").removeSuffix(" - topic"))

private fun normalizeChartRecording(value: String, artists: Set<String>): String {
    val withoutArtist = value.substringBefore(" - ").let { prefix ->
        if (artists.any { it.isNotBlank() && normalizeChartArtist(prefix).contains(it) }) {
            value.substringAfter(" - ", value)
        } else value
    }
    return normalizeChartName(withoutArtist.replace(
        Regex("\\s*(?:\\(|\\[)(?:feat\\.?|ft\\.?|featuring)\\s+[^)\\]]*(?:\\)|\\])", RegexOption.IGNORE_CASE), "",
    ))
}

private fun SongItem.toChartTrack(): Track = toTrack().copy(
    duration = duration?.let { seconds -> "${seconds / 60}:${(seconds % 60).toString().padStart(2, '0')}" },
    isAvailable = true,
    resultType = "song",
)
