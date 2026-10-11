package com.maxrave.domain.data.type

data class ChartItem(
    val name: String,
    val ytPlaylistId: String,
) : PlaylistType {
    override fun playlistType(): PlaylistType.Type = PlaylistType.Type.YOUTUBE_PLAYLIST
}
