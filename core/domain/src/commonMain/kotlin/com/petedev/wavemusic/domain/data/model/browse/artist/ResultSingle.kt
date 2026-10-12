package com.petedev.wavemusic.domain.data.model.browse.artist

import com.petedev.wavemusic.domain.data.model.searchResult.songs.Thumbnail
import com.petedev.wavemusic.domain.data.type.HomeContentType

data class ResultSingle(
    val browseId: String,
    val thumbnails: List<Thumbnail>,
    val title: String,
    val year: String,
) : HomeContentType