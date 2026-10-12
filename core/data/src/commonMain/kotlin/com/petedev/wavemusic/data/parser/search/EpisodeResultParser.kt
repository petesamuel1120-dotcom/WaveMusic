package com.petedev.wavemusic.data.parser.search

import com.petedev.wavemusic.domain.data.model.searchResult.songs.Artist
import com.petedev.wavemusic.domain.data.model.searchResult.songs.SongsResult
import com.petedev.wavemusic.domain.data.model.searchResult.songs.Thumbnail
import com.petedev.wavemusic.kotlinytmusicscraper.pages.SearchResult

/** Keep episode identity when the playable result is stored as a track or liked item. */
internal fun parseSearchEpisode(result: SearchResult): ArrayList<SongsResult> =
    ArrayList(result.listEpisodes.distinctBy { it.id }.map { episode ->
        SongsResult(
            album = null,
            artists = episode.artists.map { Artist(id = it.id, name = it.name) },
            category = "Podcast",
            duration = episode.duration?.let { "%02d:%02d".format(it / 60, it % 60) },
            durationSeconds = episode.duration,
            feedbackTokens = null,
            isExplicit = episode.explicit,
            resultType = "Podcast",
            thumbnails = listOf(Thumbnail(height = 306, url = episode.thumbnail, width = 544)),
            title = episode.title,
            videoId = episode.id,
            videoType = episode.musicVideoType,
            year = "",
            isLiveVideo = episode.isLiveVideo,
        )
    })
