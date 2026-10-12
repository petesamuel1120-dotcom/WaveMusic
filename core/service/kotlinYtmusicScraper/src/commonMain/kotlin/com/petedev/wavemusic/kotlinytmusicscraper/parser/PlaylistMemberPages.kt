package com.petedev.wavemusic.kotlinytmusicscraper.parser

import com.petedev.wavemusic.kotlinytmusicscraper.models.Continuation
import com.petedev.wavemusic.kotlinytmusicscraper.models.MusicResponsiveListItemRenderer
import com.petedev.wavemusic.kotlinytmusicscraper.models.MusicShelfRenderer
import com.petedev.wavemusic.kotlinytmusicscraper.models.SectionListRenderer
import com.petedev.wavemusic.kotlinytmusicscraper.models.response.BrowseResponse
import com.petedev.wavemusic.kotlinytmusicscraper.models.youtube.data.ContinuationItemRenderer

data class PlaylistMemberPage(
    val contents: List<MusicShelfRenderer.Content>,
    val rows: List<MusicResponsiveListItemRenderer>,
    val continuation: String?,
    val header: Any?,
    val isEditable: Boolean,
    val hasPlaylistSource: Boolean,
)

/** A video ID identifies a playable item; only its edit token proves playlist membership. */
fun MusicResponsiveListItemRenderer.hasPlaylistMembership(): Boolean =
    !playlistItemData?.playlistSetVideoId.isNullOrBlank() ||
        menu?.menuRenderer?.items.orEmpty().any { item ->
            item.menuServiceItemRenderer?.let { service ->
                service.icon?.iconType == "REMOVE_FROM_PLAYLIST" &&
                    service.serviceEndpoint?.playlistEditEndpoint?.actions.orEmpty().any { !it.setVideoId.isNullOrBlank() }
            } == true
        }

private fun List<MusicShelfRenderer.Content>.members(requireMembership: Boolean): List<MusicShelfRenderer.Content> {
    val marked = any { it.musicResponsiveListItemRenderer?.hasPlaylistMembership() == true }
    return if (requireMembership || marked) filter { it.musicResponsiveListItemRenderer?.hasPlaylistMembership() == true }
    else this
}

private fun List<Continuation>?.nextMemberToken(): String? =
    orEmpty().firstNotNullOfOrNull { it.nextContinuationData?.continuation }

private fun ContinuationItemRenderer.browseToken(): String? {
    val endpoint = continuationEndpoint ?: return null
    endpoint.continuationCommand?.token?.takeIf { it.isNotBlank() }?.let { return it }
    // Modern playlist tokens may share a command executor with voting/popup commands.
    // Only the BROWSE command advances the member list; WATCH tokens are not interchangeable.
    return endpoint.commandExecutorCommand?.commands.orEmpty().firstNotNullOfOrNull {
        it.continuationCommand?.takeIf { command -> command.request == "CONTINUATION_REQUEST_TYPE_BROWSE" }
            ?.token?.takeIf(String::isNotBlank)
    }
}

private fun List<MusicShelfRenderer.Content>.lastToken(): String? =
    asReversed().firstNotNullOfOrNull { it.continuationItemRenderer?.browseToken() }

/**
 * Read only playlist member shelves/actions. Generic section/reload tokens belong to the
 * Suggestions feed and must not become the main playlist's pagination token.
 */
fun BrowseResponse.playlistMemberPage(
    requireMembership: Boolean = false,
    allowLikedMusicShelves: Boolean = false,
): PlaylistMemberPage {
    val primary = contents?.singleColumnBrowseResultsRenderer?.tabs?.firstOrNull()
        ?.tabRenderer?.content?.sectionListRenderer
        ?: contents?.twoColumnBrowseResultsRenderer?.tabs?.firstOrNull()
            ?.tabRenderer?.content?.sectionListRenderer
    val secondary = contents?.twoColumnBrowseResultsRenderer?.secondaryContents?.sectionListRenderer
    val initialSections = primary?.contents.orEmpty() + secondary?.contents.orEmpty()
    val header = this.header?.musicEditablePlaylistDetailHeaderRenderer
        ?: this.header?.musicDetailHeaderRenderer
        ?: initialSections.firstNotNullOfOrNull { it.musicEditablePlaylistDetailHeaderRenderer }
        ?: initialSections.firstNotNullOfOrNull { it.musicResponsiveHeaderRenderer }
    val editable = header is BrowseResponse.Header.MusicEditablePlaylistDetailHeaderRenderer
    val strict = requireMembership || editable
    val selected = mutableListOf<MusicShelfRenderer.Content>()
    val tokens = mutableListOf<String>()
    var hasPlaylistSource = false

    fun addSections(sections: List<SectionListRenderer.Content>) {
        for (section in sections) {
            section.musicPlaylistShelfRenderer?.let { shelf ->
                hasPlaylistSource = true
                val members = shelf.contents.orEmpty().members(strict)
                selected += members
                if (members.any { it.musicResponsiveListItemRenderer != null } ||
                    shelf.contents.orEmpty().none { it.musicResponsiveListItemRenderer != null }) {
                    (shelf.continuations.nextMemberToken() ?: shelf.contents.orEmpty().lastToken())?.let(tokens::add)
                }
            }
            // musicShelfRenderer is the recommendations feed. Its rows can refer to items
            // already in another playlist; their edit tokens do not change this shelf's role.
        }
    }
    addSections(initialSections)
    continuationContents?.musicPlaylistShelfContinuation?.let { shelf ->
        hasPlaylistSource = true
        val members = shelf.contents.members(strict)
        selected += members
        if (members.any { it.musicResponsiveListItemRenderer != null } ||
            shelf.contents.none { it.musicResponsiveListItemRenderer != null }) {
            (shelf.continuations.nextMemberToken() ?: shelf.contents.lastToken())?.let(tokens::add)
        }
    }
    addSections(continuationContents?.sectionListContinuation?.contents.orEmpty())

    val actionRows = mutableListOf<MusicResponsiveListItemRenderer>()
    onResponseReceivedActions.orEmpty().mapNotNull { it.appendContinuationItemsAction }.forEach { action ->
        val rows = action.continuationItems.mapNotNull { it.musicResponsiveListItemRenderer }
        val targetsPlaylist = "playlist" in action.targetId.lowercase()
        val marked = rows.any { it.hasPlaylistMembership() }
        val members = when {
            !targetsPlaylist -> emptyList()
            strict || marked -> rows.filter { it.hasPlaylistMembership() }
            targetsPlaylist -> rows
            else -> emptyList()
        }
        if (members.isNotEmpty() || (targetsPlaylist && rows.isEmpty())) {
            hasPlaylistSource = true
            actionRows += members
            action.continuationItems.asReversed().firstNotNullOfOrNull {
                it.continuationItemRenderer?.browseToken()
            }?.let(tokens::add)
        }
    }
    // LM is the authenticated Liked Music collection, not an editable playlist with Suggestions.
    // YouTube also serves its older likes through generic music shelves and append actions.
    // Callers opt in only for LM; ordinary playlists retain their strict member-only behavior.
    if (allowLikedMusicShelves && !strict && !hasPlaylistSource) {
        val sections = initialSections + continuationContents?.sectionListContinuation?.contents.orEmpty()
        sections.forEach { section ->
            section.musicShelfRenderer?.let { shelf ->
                hasPlaylistSource = true
                selected += shelf.contents.orEmpty()
                (shelf.continuations.nextMemberToken() ?: shelf.contents.orEmpty().lastToken())?.let(tokens::add)
            }
        }
        continuationContents?.musicShelfContinuation?.let { shelf ->
            hasPlaylistSource = true
            actionRows += shelf.contents.mapNotNull { it.musicResponsiveListItemRenderer }
            shelf.continuations.nextMemberToken()?.let(tokens::add)
        }
        onResponseReceivedActions.orEmpty().mapNotNull { it.appendContinuationItemsAction }.forEach { action ->
            // LM has no Suggestions feed. Append target IDs may be opaque shelf-instance IDs,
            // so using their spelling as proof of membership rejects genuine older likes.
            hasPlaylistSource = true
            actionRows += action.continuationItems.mapNotNull { it.musicResponsiveListItemRenderer }
            action.continuationItems.asReversed().firstNotNullOfOrNull {
                it.continuationItemRenderer?.browseToken()
            }?.let(tokens::add)
        }
    }
    if (allowLikedMusicShelves && hasPlaylistSource && tokens.isEmpty()) {
        (primary?.continuations.nextMemberToken()
            ?: secondary?.continuations.nextMemberToken()
            ?: continuationContents?.sectionListContinuation?.continuations.nextMemberToken())?.let(tokens::add)
    }
    val rows = (selected.mapNotNull { it.musicResponsiveListItemRenderer } + actionRows)
        .filter { !it.videoId.isNullOrBlank() }.distinctBy { it.videoId }
    return PlaylistMemberPage(selected, rows, tokens.firstOrNull(), header, editable, hasPlaylistSource)
}
