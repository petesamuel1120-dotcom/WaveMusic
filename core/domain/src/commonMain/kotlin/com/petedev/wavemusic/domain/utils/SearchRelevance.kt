package com.petedev.wavemusic.domain.utils

import com.petedev.wavemusic.domain.data.type.SearchResultType
import com.petedev.wavemusic.domain.data.model.searchResult.songs.SongsResult
import com.petedev.wavemusic.domain.data.model.searchResult.videos.VideosResult
import com.petedev.wavemusic.domain.data.model.searchResult.albums.AlbumsResult
import com.petedev.wavemusic.domain.data.model.searchResult.artists.ArtistsResult
import com.petedev.wavemusic.domain.data.model.searchResult.playlists.PlaylistsResult

/** Rank all categories together; stable ties retain the provider's order. */
fun rankSearchResults(query: String, results: List<SearchResultType>): List<SearchResultType> {
    fun normalize(value: String) = value.lowercase().map { if (it.isLetterOrDigit()) it else ' ' }
        .joinToString("").split(' ').filter { it.isNotEmpty() }
    val queryWords = normalize(query)
    if (queryWords.isEmpty()) return results
    return results.sortedByDescending { result ->
        val (title, creator) = when (result) {
            is SongsResult -> result.title.orEmpty() to result.artists.orEmpty().joinToString(" ") { it.name }
            is VideosResult -> result.title to result.artists.orEmpty().joinToString(" ") { it.name }
            is AlbumsResult -> result.title to result.artists.joinToString(" ") { it.name }
            is ArtistsResult -> result.artist to ""
            is PlaylistsResult -> result.title to result.author
            else -> "" to ""
        }
        val titleWords = normalize(title)
        val creatorWords = normalize(creator)
        val titleMatches = queryWords.count { it in titleWords }
        val combinedMatches = queryWords.count { it in titleWords || it in creatorWords }
        val exactTitle = titleWords == queryWords
        val titleInQuery = titleWords.isNotEmpty() && queryWords.windowed(titleWords.size).any { it == titleWords }
        val fullMatch = combinedMatches == queryWords.size
        (if (exactTitle) 1000 else 0) + (if (fullMatch) 500 else 0) +
            (if (titleInQuery) 250 else 0) + 100 * titleMatches / queryWords.size +
            50 * combinedMatches / queryWords.size
    }
}
