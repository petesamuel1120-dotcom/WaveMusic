package com.petedev.wavemusic.domain.data.model.searchResult.artists

import com.petedev.wavemusic.domain.data.model.searchResult.songs.Thumbnail
import com.petedev.wavemusic.domain.data.type.ArtistType
import com.petedev.wavemusic.domain.data.type.SearchResultType

data class ArtistsResult(
    val artist: String,
    val browseId: String,
    val category: String,
    val radioId: String,
    val resultType: String,
    val shuffleId: String,
    val thumbnails: List<Thumbnail>,
) : ArtistType,
    SearchResultType {
    override fun objectType(): SearchResultType.Type = SearchResultType.Type.ARTIST
}