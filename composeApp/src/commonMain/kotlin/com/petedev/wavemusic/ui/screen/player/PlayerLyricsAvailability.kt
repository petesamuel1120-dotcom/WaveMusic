package com.petedev.wavemusic.ui.screen.player

import com.petedev.wavemusic.domain.extension.activeVideoId
import com.petedev.wavemusic.domain.mediaservice.handler.NowPlayingTrackState
import com.petedev.wavemusic.domain.utils.isPodcastEpisode

/** Episode markers survive queue/history playback; the saved episode record covers older items. */
internal fun NowPlayingTrackState?.allowsLyrics(savedPodcastEpisode: Boolean = false): Boolean {
    if (savedPodcastEpisode) return false
    val state = this ?: return true
    val id = state.activeVideoId()
    val track = state.track?.takeIf { it.videoId == id }
    val song = state.songEntity?.takeIf { it.videoId == id }
    return track?.isPodcastEpisode() != true && song?.isPodcastEpisode() != true
}
