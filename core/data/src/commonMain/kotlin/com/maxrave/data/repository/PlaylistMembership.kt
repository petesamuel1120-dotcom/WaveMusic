package com.maxrave.data.repository

import com.maxrave.domain.utils.PLAYLIST_ITEM_ALREADY_PRESENT
import com.maxrave.kotlinytmusicscraper.models.response.BrowseResponse
import com.maxrave.kotlinytmusicscraper.parser.playlistMemberPage
import kotlinx.coroutines.CancellationException

internal data class PlaylistMembershipPage(val videoIds: List<String>, val continuation: String?)

/** Read actual playlist shelves, never the suggestion shelf beside an empty playlist. */
internal fun playlistMembershipPage(response: BrowseResponse): PlaylistMembershipPage? {
    val page = response.playlistMemberPage(requireMembership = true)
    if (!page.hasPlaylistSource) return null
    return PlaylistMembershipPage(page.rows.mapNotNull { it.videoId }, page.continuation)
}

/** Verify a rejected add without changing the fast path for successful adds. */
internal suspend fun rejectedPlaylistAddMessage(
    videoId: String,
    loadPage: suspend (String?) -> Result<PlaylistMembershipPage?>,
): String {
    var continuation: String? = null
    val seen = mutableSetOf<String>()
    repeat(100) {
        val result = loadPage(continuation)
        val exception = result.exceptionOrNull()
        if (exception is CancellationException) throw exception
        val page = result.getOrNull() ?: return "FAILED"
        if (videoId in page.videoIds) return PLAYLIST_ITEM_ALREADY_PRESENT
        val next = page.continuation ?: return "FAILED"
        if (!seen.add(next)) return "FAILED"
        continuation = next
    }
    return "FAILED"
}
