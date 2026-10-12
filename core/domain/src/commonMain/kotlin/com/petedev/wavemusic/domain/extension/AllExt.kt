package com.petedev.wavemusic.domain.extension

import com.petedev.wavemusic.domain.mediaservice.handler.QueueMediaType
import com.petedev.wavemusic.common.MERGING_DATA_TYPE
import com.petedev.wavemusic.domain.data.entities.SongEntity
import com.petedev.wavemusic.domain.data.model.browse.album.Track
import com.petedev.wavemusic.domain.data.model.cookie.CookieItem
import com.petedev.wavemusic.domain.data.player.GenericMediaItem
import com.petedev.wavemusic.domain.data.player.GenericMediaMetadata
import com.petedev.wavemusic.domain.mediaservice.handler.NowPlayingTrackState
import com.petedev.wavemusic.domain.utils.connectArtists
import com.petedev.wavemusic.domain.utils.MusicVideoType
import com.petedev.wavemusic.domain.utils.toListName
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
fun now(): LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

@OptIn(ExperimentalTime::class)
fun epochMillisToLocalDateTime(epochMillis: Long): LocalDateTime =
    Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(TimeZone.currentSystemDefault())

fun LocalDateTime.isBefore(other: LocalDateTime): Boolean = this < other

fun LocalDateTime.isAfter(other: LocalDateTime): Boolean = this > other

@OptIn(ExperimentalTime::class)
fun LocalDateTime.plusSeconds(seconds: Long): LocalDateTime =
    this
        .toInstant(TimeZone.currentSystemDefault())
        .plus(seconds, DateTimeUnit.SECOND, TimeZone.currentSystemDefault())
        .toLocalDateTime(TimeZone.currentSystemDefault())

fun startTimestampOfThisYear(): LocalDateTime = LocalDateTime(now().year, 1, 1, 0, 0, 0, 0)

fun LocalDateTime.beforeXDays(x: Int): LocalDateTime = this.date.minus(x, DateTimeUnit.DAY).atTime(this.time)

fun GenericMediaItem.isSong(): Boolean = this.metadata.description?.contains(MERGING_DATA_TYPE.SONG) == true

fun GenericMediaItem.isVideo(): Boolean = this.metadata.description?.contains(MERGING_DATA_TYPE.VIDEO) == true

/** Returns the unprefixed YouTube video ID for whichever representation is available. */
fun NowPlayingTrackState.activeVideoId(): String? =
    track?.videoId?.takeIf { it.isNotBlank() }
        ?: songEntity?.videoId?.takeIf { it.isNotBlank() }
        ?: mediaItem.mediaId
            .removePrefix(MERGING_DATA_TYPE.VIDEO)
            .takeIf { it.isNotBlank() }

/** Publish one item identity without carrying the outgoing song's title/artwork into it. */
fun NowPlayingTrackState.withMediaItem(item: GenericMediaItem, queueTrack: com.petedev.wavemusic.domain.data.model.browse.album.Track?): NowPlayingTrackState =
    copy(
        mediaItem = item,
        track = queueTrack,
        songEntity = songEntity?.takeIf { it.videoId == item.mediaId.removePrefix(MERGING_DATA_TYPE.VIDEO) },
    )

/** A database emission from an outgoing item must never repaint the current item. */
fun NowPlayingTrackState.withSongForMediaItem(expectedMediaId: String, song: SongEntity): NowPlayingTrackState =
    if (mediaItem.mediaId == expectedMediaId && song.videoId == mediaItem.mediaId.removePrefix(MERGING_DATA_TYPE.VIDEO)) {
        copy(songEntity = song)
    } else {
        this
    }

fun GenericMediaItem.toSongEntity(): SongEntity =
    SongEntity(
        videoId = this.mediaId,
        albumId = null,
        albumName = this.metadata.albumTitle.toString(),
        artistId = null,
        artistName = listOf(this.metadata.artist.toString()),
        duration = "",
        durationSeconds = 0,
        isAvailable = true,
        isExplicit = false,
        likeStatus = "INDIFFERENT",
        thumbnails = this.metadata.artworkUri.toString(),
        title = this.metadata.title.toString(),
        videoType = "",
        category = "",
        resultType = "",
        liked = false,
        totalPlayTime = 0,
        downloadState = 0,
    )

fun SongEntity.toGenericMediaItem(): GenericMediaItem {
    val isSong =
        if (MusicVideoType.isKnown(this.videoType)) {
            MusicVideoType.isAudio(this.videoType)
        } else {
            this.thumbnails?.contains("w544") == true && this.thumbnails.contains("h544")
        }
    return GenericMediaItem(
        mediaId = this.videoId,
        uri = this.videoId,
        metadata =
            GenericMediaMetadata(
                title = this.title,
                artist = this.artistName?.connectArtists(),
                albumTitle = this.albumName,
                artworkUri = this.thumbnails,
                description = if (isSong) MERGING_DATA_TYPE.SONG else MERGING_DATA_TYPE.VIDEO,
            ),
        customCacheKey = this.videoId,
    )
}

fun Track.toGenericMediaItem(): GenericMediaItem {
    var thumbUrl =
        this.thumbnails?.lastOrNull()?.url
            ?: "http://i.ytimg.com/vi/${this.videoId}/maxresdefault.jpg"
    if (thumbUrl.contains("w120")) {
        thumbUrl = Regex("([wh])120").replace(thumbUrl, "$1544")
    }
    val artistName: String = this.artists.toListName().connectArtists()
    val isSong = QueueMediaType.fromTrack(this) == QueueMediaType.SONG
    // LIVE badges also appear on recorded performances. Route through the HLS-only source only
    // when YouTube marks the item live and the listing has no finite runtime; otherwise a normal
    // VOD URL would be handed to HlsMediaSource and fail manifest parsing.
    val isLiveVideo = !isSong && this.isLiveVideo && (durationSeconds == null || durationSeconds <= 0)
    return GenericMediaItem(
        mediaId = this.videoId,
        uri = this.videoId,
        metadata =
            GenericMediaMetadata(
                title = this.title,
                artist = artistName,
                albumTitle = this.album?.name,
                artworkUri = thumbUrl,
                description =
                    when {
                        isSong -> MERGING_DATA_TYPE.SONG
                        isLiveVideo -> MERGING_DATA_TYPE.LIVE_VIDEO
                        else -> MERGING_DATA_TYPE.VIDEO
                    },
            ),
        // Keep live manifest requests out of the regular song/video cache namespace.
        customCacheKey = if (isLiveVideo) "${MERGING_DATA_TYPE.LIVE_VIDEO}${this.videoId}" else this.videoId,
    )
}

fun CookieItem.Content.toNetscapeFormat(includeSubdomains: Boolean = true): String {
    with(this) {
        val stringList =
            listOf(
                domain,
                includeSubdomains.toString().uppercase(),
                path,
                isSecure.toString().uppercase(),
                expiresUtc.toString(),
                name,
                value,
            )

        val builder = StringBuilder(stringList.first())

        for (s in stringList.subList(1, stringList.size)) {
            if (s.isNotEmpty()) {
                if (builder.isNotEmpty()) {
                    builder.append("\u0009")
                }
                builder.append(s)
            }
        }
        return builder.toString()
    }
}

fun CookieItem.toNetScapeString(): String =
    "# Netscape HTTP Cookie File\n" +
        "# WebView Generated by the Wave Music app\n" +
        "# This is a generated file! Do not edit.\n\n" +
        this.content
            .fold(StringBuilder("")) { acc, cookie ->
                acc.append(cookie.toNetscapeFormat()).append("\n")
            }.toString()

/**
 * Decodes HTML entities in a string to their corresponding characters.
 * Handles both named entities (e.g. &amp;) and numeric entities (e.g. &#39; &#x27;).
 */
fun decodeHtmlEntities(text: String): String {
    // Named HTML entities
    val namedEntities =
        mapOf(
            "&apos;" to "'",
            "&quot;" to "\"",
            "&amp;" to "&",
            "&lt;" to "<",
            "&gt;" to ">",
            "&#x27;" to "'",
            "&#x22;" to "\"",
            "&#x26;" to "&",
            "&#x3C;" to "<",
            "&#x3E;" to ">",
        )

    var result = text

    // Replace named entities
    for ((entity, char) in namedEntities) {
        result = result.replace(entity, char, ignoreCase = true)
    }

    // Replace hex numeric entities: &#xHH; or &#xHH (with or without semicolon)
    result =
        Regex("""&#x([0-9a-fA-F]+);?""").replace(result) { match ->
            val codePoint = match.groupValues[1].toIntOrNull(16)
            if (codePoint != null) {
                Char(codePoint).toString()
            } else {
                match.value
            }
        }

    // Replace decimal numeric entities: &#DD; or &#DD (with or without semicolon)
    result =
        Regex("""&#(\d+);?""").replace(result) { match ->
            val codePoint = match.groupValues[1].toIntOrNull()
            if (codePoint != null) {
                Char(codePoint).toString()
            } else {
                match.value
            }
        }

    return result
}
