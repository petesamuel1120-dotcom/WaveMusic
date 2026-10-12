package com.petedev.wavemusic.domain.data.model.browse.playlist

import com.petedev.wavemusic.domain.data.model.streams.YouTubeWatchEndpoint

data class PlaylistState(
    val id: String,
    val title: String,
    val isRadio: Boolean,
    val author: Author,
    val thumbnail: String? = null,
    val description: String? = null,
    val year: String,
    val trackCount: Int = 0,
    val radioEndpoint: YouTubeWatchEndpoint? = null,
    val shuffleEndpoint: YouTubeWatchEndpoint? = null,
    /** User-owned playlists require edit tokens on every member row. */
    val isEditable: Boolean = false,
)