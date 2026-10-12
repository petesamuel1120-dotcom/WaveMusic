package com.petedev.wavemusic.domain.data.model.searchResult.songs

import com.petedev.wavemusic.domain.data.type.SearchResultType
import com.petedev.wavemusic.domain.utils.PodcastEpisodeType

data class SongsResult(
    val album: Album?,
    val artists: List<Artist>?,
    val category: String?,
    val duration: String?,
    val durationSeconds: Int?,
    val feedbackTokens: FeedbackTokens?,
    val isExplicit: Boolean?,
    val resultType: String?,
    val thumbnails: List<Thumbnail>?,
    val title: String?,
    val videoId: String,
    val videoType: String?,
    val year: Any,
    val isLiveVideo: Boolean = false,
) : SearchResultType {
    override fun objectType(): SearchResultType.Type =
        if (PodcastEpisodeType.isEpisode(category, resultType, videoType)) SearchResultType.Type.EPISODE
        else SearchResultType.Type.SONG
}
