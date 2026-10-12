package com.petedev.wavemusic.kotlinytmusicscraper.pages

import com.petedev.wavemusic.kotlinytmusicscraper.models.Album
import com.petedev.wavemusic.kotlinytmusicscraper.models.Artist
import com.petedev.wavemusic.kotlinytmusicscraper.models.MusicResponsiveListItemRenderer
import com.petedev.wavemusic.kotlinytmusicscraper.models.SongItem
import com.petedev.wavemusic.kotlinytmusicscraper.models.YTItem
import com.petedev.wavemusic.kotlinytmusicscraper.models.oddElements
import com.petedev.wavemusic.kotlinytmusicscraper.models.splitBySeparator
import com.petedev.wavemusic.kotlinytmusicscraper.utils.parseTime

data class ArtistItemsContinuationPage(
    val items: List<YTItem>,
    val continuation: String?,
) {
    companion object {
        fun fromMusicResponsiveListItemRenderer(renderer: MusicResponsiveListItemRenderer): SongItem? {
            return SongItem(
                id = renderer.videoId ?: return null,
                title =
                    renderer.flexColumns
                        .firstOrNull()
                        ?.musicResponsiveListItemFlexColumnRenderer
                        ?.text
                        ?.runs
                        ?.firstOrNull()
                        ?.text ?: return null,
                artists =
                    renderer.flexColumns
                        .getOrNull(1)
                        ?.musicResponsiveListItemFlexColumnRenderer
                        ?.text
                        ?.runs
                        // The column reads "Artist • Album • 13M plays"; only the first group is
                        // artists, so everything after the first " • " is dropped.
                        ?.splitBySeparator()
                        ?.firstOrNull()
                        ?.oddElements()
                        ?.map {
                            Artist(
                                name = it.text,
                                id = it.navigationEndpoint?.browseEndpoint?.browseId,
                            )
                        } ?: return null,
                album =
                    renderer.flexColumns.getOrNull(2)?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.firstOrNull()?.let {
                        Album(
                            name = it.text,
                            id = it.navigationEndpoint?.browseEndpoint?.browseId ?: return null,
                        )
                    },
                duration =
                    renderer.fixedColumns
                        ?.firstOrNull()
                        ?.musicResponsiveListItemFlexColumnRenderer
                        ?.text
                        ?.runs
                        ?.firstOrNull()
                        ?.text
                        ?.parseTime() ?: return null,
                thumbnail = renderer.thumbnail?.musicThumbnailRenderer?.getThumbnailUrl() ?: return null,
                explicit =
                    renderer.badges?.find {
                        it.musicInlineBadgeRenderer?.icon?.iconType == "MUSIC_EXPLICIT_BADGE"
                    } != null,
                endpoint =
                    renderer.overlay
                        ?.musicItemThumbnailOverlayRenderer
                        ?.content
                        ?.musicPlayButtonRenderer
                        ?.playNavigationEndpoint
                        ?.watchEndpoint,
                musicVideoType = renderer.musicVideoType,
            )
        }
    }
}