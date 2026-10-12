package com.petedev.wavemusic.domain.utils

/** Only the Songs ranking should resolve audio renditions; genre charts contain videos. */
fun isAudioChartPlaylist(title: String, author: String?): Boolean {
    val youtubePublisher = author?.contains("YouTube", ignoreCase = true) == true
    return youtubePublisher && title.startsWith("Top 100 Songs", ignoreCase = true)
}

/** Cached genre charts renamed by the previous audio conversion need a fresh browse. */
fun isConvertedGenreVideoChart(title: String, author: String?): Boolean =
    author?.contains("YouTube", ignoreCase = true) == true &&
        title.startsWith("Top 50 ", ignoreCase = true) &&
        title.contains(" Songs", ignoreCase = true)

fun audioChartTitle(title: String): String =
    title.replace("Music Videos", "Songs", ignoreCase = true)
