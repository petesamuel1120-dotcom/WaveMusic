package com.petedev.wavemusic.kotlinytmusicscraper.pages

import com.petedev.wavemusic.kotlinytmusicscraper.models.Album
import com.petedev.wavemusic.kotlinytmusicscraper.models.AlbumItem
import com.petedev.wavemusic.kotlinytmusicscraper.models.Artist
import com.petedev.wavemusic.kotlinytmusicscraper.models.ArtistItem
import com.petedev.wavemusic.kotlinytmusicscraper.models.MusicResponsiveListItemRenderer
import com.petedev.wavemusic.kotlinytmusicscraper.models.PlaylistItem
import com.petedev.wavemusic.kotlinytmusicscraper.models.SongItem
import com.petedev.wavemusic.kotlinytmusicscraper.models.Thumbnails
import com.petedev.wavemusic.kotlinytmusicscraper.models.YTItem
import com.petedev.wavemusic.kotlinytmusicscraper.models.oddElements
import com.petedev.wavemusic.kotlinytmusicscraper.models.splitBySeparator
import com.petedev.wavemusic.kotlinytmusicscraper.utils.parseTime

data class SearchResult(
    val items: List<YTItem>,
    val listPodcast: List<PodcastItem>,
    val continuation: String? = null,
    val listEpisodes: List<SongItem> = emptyList(),
)

object SearchPage {
    /** Episode rows use a watch endpoint; their show link belongs to the metadata line. */
    fun toEpisode(renderer: MusicResponsiveListItemRenderer?): SongItem? {
        renderer ?: return null
        val id = renderer.navigationEndpoint?.watchEndpoint?.videoId ?: renderer.videoId ?: return null
        val title = renderer.flexColumns.firstOrNull()?.musicResponsiveListItemFlexColumnRenderer
            ?.text?.runs?.joinToString("") { it.text }?.takeIf { it.isNotBlank() } ?: return null
        val metadata = renderer.flexColumns.drop(1).flatMap {
            it.musicResponsiveListItemFlexColumnRenderer.text?.runs.orEmpty()
        }
        val show = metadata.firstOrNull { it.navigationEndpoint?.browseEndpoint?.browseId?.startsWith("MPSP") == true }
            ?: metadata.lastOrNull { it.navigationEndpoint?.browseEndpoint != null }
            ?: metadata.lastOrNull { it.text.isNotBlank() && it.text != " • " && it.text.parseTime() == null }
        val duration = renderer.fixedColumns.orEmpty().flatMap {
            it.musicResponsiveListItemFlexColumnRenderer.text?.runs.orEmpty()
        }.firstNotNullOfOrNull { it.text.parseTime() }
            ?: metadata.asReversed().firstNotNullOfOrNull { it.text.parseTime() }
        return SongItem(
            id = id,
            title = title,
            artists = show?.let { listOf(Artist(it.text, it.navigationEndpoint?.browseEndpoint?.browseId)) }.orEmpty(),
            duration = duration,
            thumbnail = renderer.thumbnail?.musicThumbnailRenderer?.getThumbnailUrl() ?: return null,
            thumbnails = renderer.thumbnail.musicThumbnailRenderer.thumbnail,
            musicVideoType = renderer.musicVideoType,
            isLiveVideo = renderer.isLiveVideo,
        )
    }

    fun toPodcast(renderer: MusicResponsiveListItemRenderer?): PodcastItem? {
        return if (renderer == null) {
            null
        } else {
            PodcastItem(
                id = renderer.navigationEndpoint?.browseEndpoint?.browseId ?: return null,
                title =
                    renderer.flexColumns
                        .firstOrNull()
                        ?.musicResponsiveListItemFlexColumnRenderer
                        ?.text
                        ?.runs
                        ?.firstOrNull()
                        ?.text
                        ?: return null,
                author =
                    Artist(
                        name =
                            renderer.flexColumns
                                .lastOrNull()
                                ?.musicResponsiveListItemFlexColumnRenderer
                                ?.text
                                ?.runs
                                ?.firstOrNull()
                                ?.text
                                ?: return null,
                        id =
                            renderer.flexColumns
                                .lastOrNull()
                                ?.musicResponsiveListItemFlexColumnRenderer
                                ?.text
                                ?.runs
                                ?.firstOrNull()
                                ?.navigationEndpoint
                                ?.browseEndpoint
                                ?.browseId
                                ?: return null,
                    ),
                thumbnail = renderer.thumbnail?.musicThumbnailRenderer?.thumbnail ?: return null,
            )
        }
    }

    fun toYTItem(renderer: MusicResponsiveListItemRenderer?): YTItem? {
        if (renderer == null) {
            return null
        } else {
            val secondaryLine =
                renderer.flexColumns
                    .getOrNull(1)
                    ?.musicResponsiveListItemFlexColumnRenderer
                    ?.text
                    ?.runs
                    ?.splitBySeparator()
                    ?: return null
            return when {
                renderer.isSong -> {
                    SongItem(
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
                            secondaryLine.firstOrNull()?.oddElements()?.map {
                                Artist(
                                    name = it.text,
                                    id = it.navigationEndpoint?.browseEndpoint?.browseId,
                                )
                            } ?: return null,
                        album =
                            secondaryLine
                                .getOrNull(1)
                                ?.firstOrNull()
                                ?.takeIf { it.navigationEndpoint?.browseEndpoint != null }
                                ?.let {
                                    Album(
                                        name = it.text,
                                        id = it.navigationEndpoint?.browseEndpoint?.browseId!!,
                                    )
                                },
                        duration =
                            secondaryLine
                                .lastOrNull()
                                ?.firstOrNull()
                                ?.text
                                ?.parseTime(),
                        thumbnail =
                            renderer.thumbnail?.musicThumbnailRenderer?.getThumbnailUrl()
                                ?: return null,
                        explicit =
                            renderer.badges?.find {
                                it.musicInlineBadgeRenderer?.icon?.iconType == "MUSIC_EXPLICIT_BADGE"
                            } != null,
                        thumbnails = renderer.thumbnail.musicThumbnailRenderer.thumbnail,
                        musicVideoType = renderer.musicVideoType,
                        isLiveVideo = renderer.isLiveVideo,
                    )
                }

                renderer.isArtist -> {
                    ArtistItem(
                        id = renderer.navigationEndpoint?.browseEndpoint?.browseId ?: return null,
                        title =
                            renderer.flexColumns
                                .firstOrNull()
                                ?.musicResponsiveListItemFlexColumnRenderer
                                ?.text
                                ?.runs
                                ?.firstOrNull()
                                ?.text
                                ?: return null,
                        thumbnail =
                            renderer.thumbnail?.musicThumbnailRenderer?.getThumbnailUrl()
                                ?: return null,
                        shuffleEndpoint =
                            renderer.menu
                                ?.menuRenderer
                                ?.items
                                ?.find { it.menuNavigationItemRenderer?.icon?.iconType == "MUSIC_SHUFFLE" }
                                ?.menuNavigationItemRenderer
                                ?.navigationEndpoint
                                ?.watchPlaylistEndpoint
                                ?: return null,
                        radioEndpoint =
                            renderer.menu.menuRenderer.items
                                .find { it.menuNavigationItemRenderer?.icon?.iconType == "MIX" }
                                ?.menuNavigationItemRenderer
                                ?.navigationEndpoint
                                ?.watchPlaylistEndpoint
                                ?: return null,
                    )
                }

                renderer.isAlbum -> {
                    AlbumItem(
                        browseId =
                            renderer.navigationEndpoint?.browseEndpoint?.browseId
                                ?: return null,
                        playlistId =
                            renderer.playlistId
                                ?: return null,
                        title =
                            renderer.flexColumns
                                .firstOrNull()
                                ?.musicResponsiveListItemFlexColumnRenderer
                                ?.text
                                ?.runs
                                ?.firstOrNull()
                                ?.text ?: return null,
                        artists =
                            secondaryLine.getOrNull(1)?.oddElements()?.map {
                                Artist(
                                    name = it.text,
                                    id = it.navigationEndpoint?.browseEndpoint?.browseId,
                                )
                            } ?: return null,
                        year =
                            secondaryLine
                                .getOrNull(2)
                                ?.firstOrNull()
                                ?.text
                                ?.toIntOrNull(),
                        thumbnail =
                            renderer.thumbnail?.musicThumbnailRenderer?.getThumbnailUrl()
                                ?: return null,
                        explicit =
                            renderer.badges?.find {
                                it.musicInlineBadgeRenderer?.icon?.iconType == "MUSIC_EXPLICIT_BADGE"
                            } != null,
                    )
                }

                renderer.isPlaylist -> {
                    PlaylistItem(
                        id =
                            renderer.navigationEndpoint
                                ?.browseEndpoint
                                ?.browseId
                                ?.removePrefix("VL")
                                ?: return null,
                        title =
                            renderer.flexColumns
                                .firstOrNull()
                                ?.musicResponsiveListItemFlexColumnRenderer
                                ?.text
                                ?.runs
                                ?.firstOrNull()
                                ?.text ?: return null,
                        author =
                            secondaryLine.firstOrNull()?.firstOrNull()?.let {
                                Artist(
                                    name = it.text,
                                    id = it.navigationEndpoint?.browseEndpoint?.browseId,
                                )
                            } ?: return null,
                        songCountText =
                            renderer.flexColumns
                                .getOrNull(1)
                                ?.musicResponsiveListItemFlexColumnRenderer
                                ?.text
                                ?.runs
                                ?.lastOrNull()
                                ?.text ?: return null,
                        thumbnail =
                            renderer.thumbnail?.musicThumbnailRenderer?.getThumbnailUrl()
                                ?: return null,
                        playEndpoint =
                            renderer.overlay
                                ?.musicItemThumbnailOverlayRenderer
                                ?.content
                                ?.musicPlayButtonRenderer
                                ?.playNavigationEndpoint
                                ?.watchPlaylistEndpoint
                                ?: return null,
                        shuffleEndpoint =
                            renderer.menu
                                ?.menuRenderer
                                ?.items
                                ?.find { it.menuNavigationItemRenderer?.icon?.iconType == "MUSIC_SHUFFLE" }
                                ?.menuNavigationItemRenderer
                                ?.navigationEndpoint
                                ?.watchPlaylistEndpoint
                                ?: return null,
                        radioEndpoint =
                            renderer.menu.menuRenderer.items
                                .find { it.menuNavigationItemRenderer?.icon?.iconType == "MIX" }
                                ?.menuNavigationItemRenderer
                                ?.navigationEndpoint
                                ?.watchPlaylistEndpoint
                                ?: return null,
                    )
                }

                else -> null
            }
        }
    }
}

data class PodcastItem(
    val id: String,
    val title: String,
    val author: Artist,
    val thumbnail: Thumbnails,
)
