package com.petedev.wavemusic.domain.data.model.home.chart

import com.petedev.wavemusic.domain.data.model.browse.artist.ResultPlaylist

data class ChartItemPlaylist(
    val title: String,
    val playlists: List<ResultPlaylist>,
)