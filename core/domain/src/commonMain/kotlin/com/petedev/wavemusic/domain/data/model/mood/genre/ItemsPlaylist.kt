package com.petedev.wavemusic.domain.data.model.mood.genre

data class ItemsPlaylist(
    val contents: List<Content>,
    val header: String,
    val type: String,
)