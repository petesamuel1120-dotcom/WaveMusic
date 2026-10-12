package com.petedev.wavemusic.domain.utils

import com.petedev.wavemusic.domain.data.entities.SongEntity
import com.petedev.wavemusic.domain.data.model.browse.album.Track

/** Podcast identity must survive search, queue playback, history, and liking an episode. */
object PodcastEpisodeType {
    fun isEpisode(category: String?, resultType: String?, videoType: String?): Boolean =
        category.isEpisodeLabel() || resultType.isEpisodeLabel() || MusicVideoType.isPodcast(videoType)

    private fun String?.isEpisodeLabel(): Boolean =
        equals("Podcast", ignoreCase = true) || equals("Episode", ignoreCase = true)
}

fun Track.isPodcastEpisode(): Boolean = PodcastEpisodeType.isEpisode(category, resultType, videoType)

fun SongEntity.isPodcastEpisode(): Boolean = PodcastEpisodeType.isEpisode(category, resultType, videoType)
