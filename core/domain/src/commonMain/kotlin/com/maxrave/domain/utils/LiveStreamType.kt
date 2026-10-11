package com.maxrave.domain.utils

import com.maxrave.domain.data.entities.SongEntity
import com.maxrave.domain.data.model.browse.album.Track

/** Identifies continuous live streams while leaving ordinary live performances searchable. */
object LiveStreamType {
    private val continuousStreamPattern = Regex("\\b24\\s*(?:/|x|×)\\s*7\\b")

    fun isLiveStream(
        isLiveVideo: Boolean,
        title: String?,
        description: String? = null,
    ): Boolean {
        if (isLiveVideo) return true

        val text = listOfNotNull(title, description).joinToString(" ").lowercase()
        return continuousStreamPattern.containsMatchIn(text) ||
            listOf("live stream", "livestream", "live feed", "live radio", "radio stream")
                .any { phrase -> text.contains(phrase) }
    }

    fun isLiveStream(track: Track): Boolean = isLiveStream(track.isLiveVideo, track.title)

    fun isLiveStream(song: SongEntity): Boolean = isLiveStream(song.isLiveVideo, song.title)
}
