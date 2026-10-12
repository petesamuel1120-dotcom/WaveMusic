package com.petedev.wavemusic.data.parser

import com.petedev.wavemusic.domain.data.model.browse.artist.ResultPlaylist
import com.petedev.wavemusic.domain.data.model.home.chart.Artists
import com.petedev.wavemusic.domain.data.model.home.chart.Chart
import com.petedev.wavemusic.domain.data.model.home.chart.ChartItemPlaylist
import com.petedev.wavemusic.domain.data.model.home.chart.ItemArtist
import com.petedev.wavemusic.kotlinytmusicscraper.models.MusicCarouselShelfRenderer
import com.petedev.wavemusic.kotlinytmusicscraper.models.SectionListRenderer

internal fun parseChart(data: SectionListRenderer?, countryCode: String? = null): Chart? {
    val weeklyVideoChart = officialMusicVideoChart(countryCode)
    val content = data?.contents ?: if (weeklyVideoChart != null) emptyList() else return null
    val listArtistItem: ArrayList<ItemArtist> = arrayListOf()
    val listItemPlaylist: ArrayList<ChartItemPlaylist> = arrayListOf()
    content.forEach {
        val contents = it.musicCarouselShelfRenderer?.contents ?: return@forEach
        val artistRows = contents.filter { row ->
            row
                .musicResponsiveListItemRenderer
                ?.navigationEndpoint
                ?.browseEndpoint
                ?.browseEndpointContextSupportedConfigs
                ?.browseEndpointContextMusicConfig
                ?.pageType == "MUSIC_PAGE_TYPE_ARTIST"
        }
        parseArtistChart(artistRows)?.let { artists -> listArtistItem.addAll(artists) }
        val playlists = contents.mapNotNull { contentItem ->
            val renderer = contentItem.musicTwoRowItemRenderer ?: return@mapNotNull null
            val endpoint = renderer.navigationEndpoint?.browseEndpoint ?: return@mapNotNull null
            val id = endpoint.browseId?.takeIf { id -> id.startsWith("VL") } ?: return@mapNotNull null
            val title = renderer.title?.runs?.joinToString("") { run -> run.text }
                ?.takeIf { title -> title.isNotBlank() } ?: return@mapNotNull null
            val author = renderer.subtitle?.runs?.joinToString(" ") { run -> run.text }.orEmpty()
            ResultPlaylist(
                id = id,
                thumbnails = renderer.thumbnailRenderer?.musicThumbnailRenderer?.thumbnail?.thumbnails
                    ?.toListThumbnail().orEmpty(),
                title = title,
                author = author,
            )
        }
        // Podcast, song and unsupported rows must not discard valid country charts.
        if (playlists.isNotEmpty()) {
            listItemPlaylist.add(
                ChartItemPlaylist(
                    title =
                        it.musicCarouselShelfRenderer
                            ?.header
                            ?.musicCarouselShelfBasicHeaderRenderer
                            ?.title
                            ?.runs
                            ?.firstOrNull()
                            ?.text ?: "",
                    playlists = playlists,
                ),
            )
        }
    }
    val sourcePlaylists = listItemPlaylist.flatMap { it.playlists }
    val primaryCharts = listOfNotNull(
        sourcePlaylists.firstOrNull {
            it.title.startsWith("Daily Top Music Videos", ignoreCase = true)
        },
        weeklyVideoChart ?: sourcePlaylists.firstOrNull {
            it.title.startsWith("Top 100 Music Videos", ignoreCase = true)
        },
    ).distinctBy { it.id.removePrefix("VL") }
    return Chart(
        artists = Artists(itemArtists = listArtistItem, playlist = ""),
        countries = null,
        listChartItem = if (primaryCharts.isEmpty()) listItemPlaylist else {
            // Show the daily video chart first, followed by the weekly chart,
            // preserving their IDs and covers.
            val additionalShelves = listItemPlaylist.filterNot { shelf ->
                shelf.playlists.any { playlist ->
                    primaryCharts.any { it.id == playlist.id } ||
                        playlist.title.startsWith("Daily Top Music Videos", ignoreCase = true) ||
                        playlist.title.startsWith("Top 100 Music Videos", ignoreCase = true)
                }
            }
            listOf(ChartItemPlaylist(title = "Video charts", playlists = primaryCharts)) + additionalShelves
        },
    )
}

internal fun parseArtistChart(contents: List<MusicCarouselShelfRenderer.Content>?): ArrayList<ItemArtist>? =
    if (contents != null) {
        val artists: ArrayList<ItemArtist> = arrayListOf()
        for (i in contents.indices) {
            val content = contents[i]
            if (content.musicResponsiveListItemRenderer != null) {
                val title =
                    content.musicResponsiveListItemRenderer
                        ?.flexColumns
                        ?.getOrNull(0)
                        ?.musicResponsiveListItemFlexColumnRenderer
                        ?.text
                        ?.runs
                        ?.getOrNull(
                            0,
                        )?.text
                val subscriber =
                    content.musicResponsiveListItemRenderer
                        ?.flexColumns
                        ?.getOrNull(1)
                        ?.musicResponsiveListItemFlexColumnRenderer
                        ?.text
                        ?.runs
                        ?.getOrNull(
                            0,
                        )?.text
                val thumbnails =
                    content.musicResponsiveListItemRenderer
                        ?.thumbnail
                        ?.musicThumbnailRenderer
                        ?.thumbnail
                        ?.thumbnails
                val artistId =
                    content.musicResponsiveListItemRenderer
                        ?.navigationEndpoint
                        ?.browseEndpoint
                        ?.browseId
                artists.add(
                    ItemArtist(
                        browseId = artistId ?: "",
                        rank = "${i + 1}",
                        subscribers = subscriber ?: "",
                        thumbnails = thumbnails?.toListThumbnail() ?: listOf(),
                        title = title ?: "",
                        trend = "",
                    ),
                )
            }
        }
        artists
    } else {
        null
    }
