package com.petedev.wavemusic.kotlinytmusicscraper.parser

import com.petedev.wavemusic.kotlinytmusicscraper.models.MusicResponsiveListItemRenderer
import com.petedev.wavemusic.kotlinytmusicscraper.models.MusicShelfRenderer
import com.petedev.wavemusic.kotlinytmusicscraper.models.SongItem
import com.petedev.wavemusic.kotlinytmusicscraper.models.WatchEndpoint
import com.petedev.wavemusic.kotlinytmusicscraper.pages.NextPage
import com.petedev.wavemusic.kotlinytmusicscraper.models.getContinuation
import com.petedev.wavemusic.kotlinytmusicscraper.models.response.BrowseResponse
import com.petedev.wavemusic.kotlinytmusicscraper.models.response.LikeStatus
import com.petedev.wavemusic.logger.Logger

fun BrowseResponse.fromPlaylistToTrack(): List<SongItem> =
    playlistMemberPage().contents.mapNotNull { it.toSongItem() }

fun BrowseResponse.fromPlaylistToTrackWithSetVideoId(): List<Pair<SongItem, String>> =
    playlistMemberPage(requireMembership = true).contents.mapNotNull { content ->
        val token = content.toPlaylistItemData()?.playlistSetVideoId?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
        (content.toSongItem() ?: return@mapNotNull null) to token
    }

fun BrowseResponse.fromPlaylistContinuationToTracks(): List<SongItem> =
    playlistMemberPage().rows.mapNotNull(NextPage::fromMusicResponsiveListItemRenderer)

fun BrowseResponse.fromPlaylistContinuationToTrackWithSetVideoId(): List<Pair<SongItem, String>> =
    playlistMemberPage(requireMembership = true).rows.mapNotNull { renderer ->
        val token = renderer.playlistItemData?.playlistSetVideoId?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
        (NextPage.fromMusicResponsiveListItemRenderer(renderer) ?: return@mapNotNull null) to token
    }

fun BrowseResponse.getPlaylistContinuation(): String? =
    this.onResponseReceivedActions
        .orEmpty()
        .mapNotNull { action ->
            action.appendContinuationItemsAction
                ?.continuationItems
                ?.lastOrNull()
                ?.continuationItemRenderer
                ?.continuationEndpoint
                ?.continuationCommand
                ?.token
        }.lastOrNull()
        ?: this.contents
            ?.twoColumnBrowseResultsRenderer
            ?.secondaryContents
            ?.sectionListRenderer
            ?.contents
            ?.lastOrNull()
            ?.musicPlaylistShelfRenderer
            ?.contents
            ?.lastOrNull()
            ?.continuationItemRenderer
            ?.continuationEndpoint
            ?.continuationCommand
            ?.token
        ?: this.contents
            ?.singleColumnBrowseResultsRenderer
            ?.tabs
            ?.firstOrNull()
            ?.tabRenderer
            ?.content
            ?.sectionListRenderer
            ?.continuations
            ?.getContinuation()
        ?: this.contents
            ?.twoColumnBrowseResultsRenderer
            ?.secondaryContents
            ?.sectionListRenderer
            ?.continuations
            ?.getContinuation()
        ?: this.contents
            ?.singleColumnBrowseResultsRenderer
            ?.tabs
            ?.firstOrNull()
            ?.tabRenderer
            ?.content
            ?.sectionListRenderer
            ?.contents
            ?.firstOrNull()
            ?.musicPlaylistShelfRenderer
            ?.continuations
            ?.firstOrNull()
            ?.nextContinuationData
            ?.continuation
        ?: this.contents
            ?.twoColumnBrowseResultsRenderer
            ?.secondaryContents
            ?.sectionListRenderer
            ?.contents
            ?.firstOrNull()
            ?.musicPlaylistShelfRenderer
            ?.continuations
            ?.firstOrNull()
            ?.nextContinuationData
            ?.continuation
        ?: this.contents
            ?.twoColumnBrowseResultsRenderer
            ?.secondaryContents
            ?.sectionListRenderer
            ?.contents
            ?.firstOrNull()
            ?.musicPlaylistShelfRenderer
            ?.contents
            ?.lastOrNull()
            ?.continuationItemRenderer
            ?.continuationEndpoint
            ?.continuationCommand
            ?.token
        ?: this.continuationContents
            ?.musicPlaylistShelfContinuation
            ?.continuations
            ?.getContinuation()

fun BrowseResponse.getContinuePlaylistContinuation(): String? =
    this.continuationContents
        ?.musicPlaylistShelfContinuation
        ?.continuations
        ?.getContinuation()

fun BrowseResponse.getPlaylistRadioEndpoint(): WatchEndpoint? {
    val header =
        this.header?.musicDetailHeaderRenderer
            ?: this.header
                ?.musicEditablePlaylistDetailHeaderRenderer
                ?.header
                ?.musicDetailHeaderRenderer
    if (header != null) {
        return header.menu.menuRenderer.items
            .find {
                it.menuNavigationItemRenderer?.icon?.iconType == "MIX"
            }?.menuNavigationItemRenderer
            ?.navigationEndpoint
            ?.watchPlaylistEndpoint
    } else {
        return this.contents
            ?.twoColumnBrowseResultsRenderer
            ?.tabs
            ?.firstOrNull()
            ?.tabRenderer
            ?.content
            ?.sectionListRenderer
            ?.contents
            ?.firstOrNull()
            ?.musicEditablePlaylistDetailHeaderRenderer
            ?.header
            ?.musicResponsiveHeaderRenderer
            ?.buttons
            ?.lastOrNull()
            ?.menuRenderer
            ?.items
            ?.find {
                it.menuNavigationItemRenderer?.icon?.iconType == "MIX"
            }?.menuNavigationItemRenderer
            ?.navigationEndpoint
            ?.watchPlaylistEndpoint
            ?: this.contents
                ?.twoColumnBrowseResultsRenderer
                ?.tabs
                ?.firstOrNull()
                ?.tabRenderer
                ?.content
                ?.sectionListRenderer
                ?.contents
                ?.firstOrNull()
                ?.musicResponsiveHeaderRenderer
                ?.buttons
                ?.findLast { it.menuRenderer != null }
                ?.menuRenderer
                ?.items
                ?.find {
                    it.menuNavigationItemRenderer?.icon?.iconType == "MIX"
                }?.menuNavigationItemRenderer
                ?.navigationEndpoint
                ?.watchPlaylistEndpoint
    }
}

fun BrowseResponse.getPlaylistShuffleEndpoint(): WatchEndpoint? {
    val header =
        this.header?.musicDetailHeaderRenderer
            ?: this.header
                ?.musicEditablePlaylistDetailHeaderRenderer
                ?.header
                ?.musicDetailHeaderRenderer
    if (header != null) {
        return header.menu.menuRenderer.topLevelButtons
            ?.firstOrNull()
            ?.buttonRenderer
            ?.navigationEndpoint
            ?.watchPlaylistEndpoint
    } else {
        return this.contents
            ?.twoColumnBrowseResultsRenderer
            ?.tabs
            ?.firstOrNull()
            ?.tabRenderer
            ?.content
            ?.sectionListRenderer
            ?.contents
            ?.firstOrNull()
            ?.musicEditablePlaylistDetailHeaderRenderer
            ?.header
            ?.musicResponsiveHeaderRenderer
            ?.buttons
            ?.lastOrNull()
            ?.menuRenderer
            ?.items
            ?.find {
                it.menuNavigationItemRenderer?.icon?.iconType == "MUSIC_SHUFFLE"
            }?.menuNavigationItemRenderer
            ?.navigationEndpoint
            ?.watchPlaylistEndpoint
            ?: this.contents
                ?.twoColumnBrowseResultsRenderer
                ?.tabs
                ?.firstOrNull()
                ?.tabRenderer
                ?.content
                ?.sectionListRenderer
                ?.contents
                ?.firstOrNull()
                ?.musicResponsiveHeaderRenderer
                ?.buttons
                ?.findLast { it.menuRenderer != null }
                ?.menuRenderer
                ?.items
                ?.find {
                    it.menuNavigationItemRenderer?.icon?.iconType == "MUSIC_SHUFFLE"
                }?.menuNavigationItemRenderer
                ?.navigationEndpoint
                ?.watchPlaylistEndpoint
    }
}

fun MusicShelfRenderer.Content.toPlaylistItemData(): MusicResponsiveListItemRenderer.PlaylistItemData? =
    this.musicResponsiveListItemRenderer?.playlistItemData

fun MusicShelfRenderer.Content.toSongItem(): SongItem? {
    val flexColumns = this.musicResponsiveListItemRenderer?.flexColumns
    val fixedColumns = this.musicResponsiveListItemRenderer?.fixedColumns
    val menu = this.musicResponsiveListItemRenderer?.menu
    return SongItem(
        id =
            flexColumns
                ?.firstOrNull()
                ?.musicResponsiveListItemFlexColumnRenderer
                ?.text
                ?.runs
                ?.firstOrNull()
                ?.navigationEndpoint
                ?.watchEndpoint
                ?.videoId ?: return null,
        title =
            flexColumns
                .firstOrNull()
                ?.musicResponsiveListItemFlexColumnRenderer
                ?.text
                ?.runs
                ?.firstOrNull()
                ?.text ?: return null,
        artists =
            flexColumns
                .apply {
                    Logger.w(
                        "PlaylistParser",
                        "Artists: ${this.map {
                            it.musicResponsiveListItemFlexColumnRenderer.text
                                ?.runs
                                ?.firstOrNull()
                                ?.text
                        }}",
                    )
                }.filter {
                    it.musicResponsiveListItemFlexColumnRenderer.isArtist()
                }.apply {
                    Logger.w(
                        "PlaylistParser",
                        "Artists after filter: ${this.map {
                            it.musicResponsiveListItemFlexColumnRenderer.text
                                ?.runs
                                ?.firstOrNull()
                                ?.text
                        }}",
                    )
                }.mapNotNull { it.musicResponsiveListItemFlexColumnRenderer.toArtist() },
        album =
            flexColumns
                .find {
                    it.musicResponsiveListItemFlexColumnRenderer.isAlbum()
                }?.musicResponsiveListItemFlexColumnRenderer
                ?.toAlbum(),
        duration =
            fixedColumns
                ?.firstOrNull()
                ?.musicResponsiveListItemFlexColumnRenderer
                ?.text
                ?.runs
                ?.firstOrNull()
                ?.text
                .toDurationSeconds(),
        thumbnail =
            this.musicResponsiveListItemRenderer
                ?.thumbnail
                ?.musicThumbnailRenderer
                ?.getThumbnailUrl() ?: "",
        endpoint =
            flexColumns
                .first()
                .musicResponsiveListItemFlexColumnRenderer
                .text
                ?.runs
                ?.firstOrNull()
                ?.navigationEndpoint
                ?.watchEndpoint,
        explicit =
            this.musicResponsiveListItemRenderer?.badges?.toSongBadges()?.contains(
                SongItem.SongBadges.Explicit,
            ) ?: false,
        thumbnails =
            this.musicResponsiveListItemRenderer
                ?.thumbnail
                ?.musicThumbnailRenderer
                ?.thumbnail,
        likeStatus =
            menu
                ?.menuRenderer
                ?.topLevelButtons
                ?.firstOrNull()
                ?.likeButtonRenderer
                ?.toLikeStatus()
                ?: LikeStatus.INDIFFERENT,
        badges = this.musicResponsiveListItemRenderer?.badges?.toSongBadges(),
        musicVideoType = this.musicResponsiveListItemRenderer?.musicVideoType,
    )
}

/**
 * Check if the browse response has reload params, if true, this has the suggestion tracks
 */
private fun BrowseResponse.suggestionShelves(): List<MusicShelfRenderer> = (
    contents?.singleColumnBrowseResultsRenderer?.tabs?.firstOrNull()
        ?.tabRenderer?.content?.sectionListRenderer?.contents.orEmpty() +
        contents?.twoColumnBrowseResultsRenderer?.secondaryContents?.sectionListRenderer?.contents.orEmpty() +
        continuationContents?.sectionListContinuation?.contents.orEmpty()
    ).mapNotNull { it.musicShelfRenderer }.filter { shelf ->
        shelf.continuations.orEmpty().any { it.reloadContinuationData?.continuation != null }
    }

fun BrowseResponse.hasReloadParams(): Boolean = getReloadParams() != null

fun BrowseResponse.getReloadParams(): String? =
    continuationContents?.musicShelfContinuation?.continuations.orEmpty()
        .firstNotNullOfOrNull { it.reloadContinuationData?.continuation }
        ?: suggestionShelves().firstNotNullOfOrNull { shelf ->
            shelf.continuations.orEmpty().firstNotNullOfOrNull { it.reloadContinuationData?.continuation }
        }

fun BrowseResponse.getSuggestionSongItems(): List<SongItem> {
    val direct = continuationContents?.musicShelfContinuation
    if (direct?.continuations.orEmpty().any { it.reloadContinuationData?.continuation != null }) {
        return direct?.contents.orEmpty().mapNotNull { it.musicResponsiveListItemRenderer }
            .mapNotNull(NextPage::fromMusicResponsiveListItemRenderer)
    }
    return suggestionShelves().flatMap { shelf -> shelf.contents.orEmpty().mapNotNull { it.toSongItem() } }
}
