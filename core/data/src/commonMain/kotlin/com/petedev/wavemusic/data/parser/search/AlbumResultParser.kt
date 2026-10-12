package com.petedev.wavemusic.data.parser.search

import com.petedev.wavemusic.domain.data.model.searchResult.albums.AlbumsResult
import com.petedev.wavemusic.domain.data.model.searchResult.songs.Artist
import com.petedev.wavemusic.domain.data.model.searchResult.songs.Thumbnail
import com.petedev.wavemusic.kotlinytmusicscraper.models.AlbumItem
import com.petedev.wavemusic.kotlinytmusicscraper.pages.SearchResult

internal fun parseSearchAlbum(result: SearchResult): ArrayList<AlbumsResult> {
    val albumsResult: ArrayList<AlbumsResult> = arrayListOf()
    result.items.forEach {
        val album = it as AlbumItem
        albumsResult.add(
            AlbumsResult(
                artists =
                    album.artists?.map { artistItem ->
                        Artist(
                            id = artistItem.id,
                            name = artistItem.name,
                        )
                    } ?: listOf(),
                browseId = album.browseId,
                category = "Album",
                duration = "",
                isExplicit = false,
                resultType = "Album",
                thumbnails = listOf(Thumbnail(544, Regex("([wh])120").replace(album.thumbnail, "$1544"), 544)),
                title = album.title,
                type = "Album",
                year = album.year.toString(),
            ),
        )
    }
    return albumsResult
}