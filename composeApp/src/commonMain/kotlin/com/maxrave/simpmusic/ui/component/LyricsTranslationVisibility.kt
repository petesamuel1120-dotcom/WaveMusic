package com.maxrave.simpmusic.ui.component

import com.maxrave.simpmusic.viewModel.NowPlayingScreenData

/** Filter display data without discarding cached translations or enabling them on arrival. */
internal fun NowPlayingScreenData.withLyricsTranslation(enabled: Boolean): NowPlayingScreenData {
    if (lyricsData?.translatedLyrics == null) return this
    if (enabled && !currentVideoId.isNullOrBlank() && lyricsVideoId == currentVideoId) return this
    return copy(lyricsData = lyricsData?.copy(translatedLyrics = null))
}
