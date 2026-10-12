package com.petedev.wavemusic.data.parser

import com.petedev.wavemusic.data.mapping.toTrack
import com.petedev.wavemusic.domain.data.model.browse.playlist.PlaylistBrowse
import com.petedev.wavemusic.kotlinytmusicscraper.models.SectionListRenderer
import com.petedev.wavemusic.kotlinytmusicscraper.models.response.BrowseResponse
import com.petedev.wavemusic.kotlinytmusicscraper.pages.NextPage
import com.petedev.wavemusic.kotlinytmusicscraper.parser.playlistMemberPage

internal fun declaredPlaylistTrackCount(header: Any?): Int? {
    val effective = (header as? BrowseResponse.Header.MusicEditablePlaylistDetailHeaderRenderer)?.header
        ?.let { it.musicResponsiveHeaderRenderer ?: it.musicDetailHeaderRenderer } ?: header
    val runs = when (effective) {
        is BrowseResponse.Header.MusicDetailHeaderRenderer -> effective.secondSubtitle.runs
        is SectionListRenderer.Content.MusicResponsiveHeaderRenderer -> effective.secondSubtitle?.runs
        else -> null
    }
    val text = if (effective is SectionListRenderer.Content.MusicResponsiveHeaderRenderer && runs.orEmpty().size >= 5)
        runs?.getOrNull(2)?.text else runs?.firstOrNull()?.text
    return text?.trim()?.let {
        Regex("""^[\p{Nd}][\p{Nd},.\u00a0 ]*""").find(it)?.value
            ?.filter(Char::isDigit)?.toIntOrNull()
    }
}

internal fun parsePlaylistMemberBrowse(
    response: BrowseResponse,
    playlistId: String,
    viewString: String,
): Pair<PlaylistBrowse, String?>? {
    val isLikedMusic = playlistId.removePrefix("VL") == "LM"
    val page = response.playlistMemberPage(allowLikedMusicShelves = isLikedMusic)
    val playlist = parsePlaylistData(page.header, page.contents, playlistId, viewString) ?: return null
    val declared = declaredPlaylistTrackCount(page.header)
    // A complete/empty playlist may have a section-level token for recommendations. Only use
    // such a legacy token when the header proves that actual member pages are still missing.
    val legacyToken = if (declared != null && declared > playlist.tracks.size && page.rows.isNotEmpty()) {
        val section = response.contents?.singleColumnBrowseResultsRenderer?.tabs?.firstOrNull()
            ?.tabRenderer?.content?.sectionListRenderer
            ?: response.contents?.twoColumnBrowseResultsRenderer?.secondaryContents?.sectionListRenderer
        section?.continuations.orEmpty().firstNotNullOfOrNull { it.nextContinuationData?.continuation }
    } else null
    // LM's explicit token is authoritative even if its displayed count describes only page one.
    val next = if (!isLikedMusic && declared != null && playlist.tracks.size >= declared) null
    else page.continuation ?: legacyToken
    return playlist to next
}

/** The header count is already a total; continuation rows must never be added to it again. */
internal suspend fun parseFullPlaylistMemberBrowse(
    initial: BrowseResponse,
    playlistId: String,
    viewString: String,
    loadContinuation: suspend (String) -> BrowseResponse,
): PlaylistBrowse? {
    val (playlist, firstToken) = parsePlaylistMemberBrowse(initial, playlistId, viewString) ?: return null
    val tracks = playlist.tracks.toMutableList()
    val visited = mutableSetOf<String>()
    var token = firstToken
    while (token != null && visited.add(token)) {
        val page = loadContinuation(token).playlistMemberPage(
            requireMembership = playlist.isEditable,
            allowLikedMusicShelves = playlistId.removePrefix("VL") == "LM",
        )
        if (playlistId.removePrefix("VL") == "LM") {
            check(page.hasPlaylistSource) { "Unrecognized Liked Music continuation" }
        }
        tracks += page.rows.mapNotNull(NextPage::fromMusicResponsiveListItemRenderer).map { it.toTrack() }
        token = page.continuation
    }
    val members = tracks.distinctBy { it.videoId }
    return playlist.copy(tracks = members, trackCount = maxOf(playlist.trackCount, members.size))
}
