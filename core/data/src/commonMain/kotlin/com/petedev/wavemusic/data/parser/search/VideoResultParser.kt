package com.petedev.wavemusic.data.parser.search

import com.petedev.wavemusic.domain.data.model.searchResult.songs.Artist
import com.petedev.wavemusic.domain.data.model.searchResult.songs.Thumbnail
import com.petedev.wavemusic.domain.data.model.searchResult.videos.VideosResult
import com.petedev.wavemusic.kotlinytmusicscraper.models.SongItem
import com.petedev.wavemusic.kotlinytmusicscraper.pages.SearchResult

internal fun parseSearchVideo(result: SearchResult): ArrayList<VideosResult> {
    val songsResult: ArrayList<VideosResult> = arrayListOf()
    result.items.forEach {
        val song = it as SongItem
        songsResult.add(
            VideosResult(
                artists =
                    song.artists.map { artistItem ->
                        Artist(
                            id = artistItem.id,
                            name = artistItem.name,
                        )
                    },
                category = "Video",
                duration = if (song.duration != null) "%02d:%02d".format(song.duration!! / 60, song.duration!! % 60) else "",
                durationSeconds = song.duration ?: 0,
                resultType = "Video",
                thumbnails = listOf(Thumbnail(306, Regex("([wh])120").replace(song.thumbnail, "$1544"), 544)),
                title = song.title,
                videoId = song.id,
                videoType = song.musicVideoType,
                views = null,
                year = "",
                isLiveVideo = song.isLiveVideo,
            ),
        )
    }
    return songsResult
}
