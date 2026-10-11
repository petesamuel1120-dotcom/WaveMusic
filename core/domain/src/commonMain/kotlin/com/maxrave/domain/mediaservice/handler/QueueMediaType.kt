package com.maxrave.domain.mediaservice.handler

import com.maxrave.domain.data.model.browse.album.Track
import com.maxrave.domain.utils.MusicVideoType

/** Automatic recommendations follow the media kind of the item that started them. */
enum class QueueMediaType {
    SONG,
    VIDEO;

    fun accepts(track: Track): Boolean = fromTrack(track) == this

    companion object {
        fun fromTrack(track: Track): QueueMediaType {
            if (track.resultType?.equals("video", ignoreCase = true) == true) return VIDEO
            if (MusicVideoType.isKnown(track.videoType)) {
                return if (MusicVideoType.isAudio(track.videoType)) SONG else VIDEO
            }
            val thumbnail = track.thumbnails?.lastOrNull()
            val square = thumbnail != null && thumbnail.height > 0 && thumbnail.height == thumbnail.width
            val url = thumbnail?.url.orEmpty()
            return if (square && !url.contains("hq720") && !url.contains("maxresdefault")) SONG else VIDEO
        }
    }
}

/** Use a matching counterpart when available; never fall back to the opposite media kind. */
fun <T> selectQueueMedia(
    items: List<T>,
    mediaType: QueueMediaType,
    typeOf: (T) -> QueueMediaType,
    counterpartOf: (T) -> T?,
): List<T> = items.mapNotNull { item ->
    if (typeOf(item) == mediaType) item else counterpartOf(item)?.takeIf { typeOf(it) == mediaType }
}
