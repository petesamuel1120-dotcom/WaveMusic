package com.petedev.wavemusic.domain.data.model.browse.artist

import com.petedev.wavemusic.domain.data.model.searchResult.songs.Thumbnail

data class ResultRelated(
    val browseId: String,
    val subscribers: String,
    val thumbnails: List<Thumbnail>,
    val title: String,
)