package com.petedev.wavemusic.kotlinytmusicscraper.pages

import com.petedev.wavemusic.kotlinytmusicscraper.models.SongItem

data class PlaylistContinuationPage(
    val songs: List<SongItem>,
    val continuation: String?,
)