package org.simpmusic.lyrics

import com.maxrave.ktorext.crypto.Hmac
import com.maxrave.ktorext.crypto.HmacUri
import com.maxrave.logger.Logger
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.simpmusic.lyrics.am.AMAlbumResource
import org.simpmusic.lyrics.am.AMSongWithAlbum
import org.simpmusic.lyrics.am.parseAMHlsVariants
import org.simpmusic.lyrics.am.pickAMRendition
import org.simpmusic.lyrics.am.AMArtistResource
import org.simpmusic.lyrics.am.AMSearchResponse
import org.simpmusic.lyrics.models.request.LyricsBody
import org.simpmusic.lyrics.models.request.TranslatedLyricsBody
import org.simpmusic.lyrics.models.response.BaseResponse
import org.simpmusic.lyrics.models.response.BetterLyricsResponse
import org.simpmusic.lyrics.models.response.LrclibObject
import org.simpmusic.lyrics.models.response.LyricsResponse
import org.simpmusic.lyrics.models.response.TranslatedLyricsResponse
import org.simpmusic.lyrics.parser.parseSyncedLyrics
import org.simpmusic.lyrics.parser.parseUnsyncedLyrics
import org.simpmusic.lyrics.parser.parseLyricsPlusLyrics
import org.simpmusic.lyrics.parser.parseQrcLyrics
import org.simpmusic.lyrics.parser.parseRichSyncLyrics
import org.simpmusic.lyrics.parser.parseTtmlLyrics
import kotlin.math.abs

private const val TAG = "SimpMusicLyricsClient"

class SimpMusicLyricsClient {
    private val algorithm = ""

    private val hmacService = Hmac("HmacSHA256", "simpmusic-lyrics")
    private val lyricsService = SimpMusicLyrics()
    private var insertingLyrics: Pair<String?, Boolean> = (null to false)
    private val isInsertingLyrics: Boolean
        get() = insertingLyrics.second

    private var tooManyRequest: Boolean = false

    private var insertingTranslatedLyrics: Pair<String?, Boolean> = (null to false)
    private val isInsertingTranslatedLyrics: Boolean
        get() = insertingTranslatedLyrics.second

    suspend fun getLyrics(videoId: String): Result<List<LyricsResponse>> =
        runCatching {
            lyricsService.findLyricsByVideoId(videoId).bodyOrThrow<List<LyricsResponse>>()
        }

    suspend fun getTranslatedLyrics(
        videoId: String,
        language: String,
    ): Result<TranslatedLyricsResponse> =
        runCatching {
            if (language.length != 2) {
                throw IllegalArgumentException("Language code must be a 2-letter code")
            }
            lyricsService.findTranslatedLyrics(videoId, language).bodyOrThrow<TranslatedLyricsResponse>()
        }

    suspend fun insertLyrics(lyricsBody: LyricsBody): Result<LyricsResponse> =
        runCatching {
            if (tooManyRequest) {
                throw IllegalStateException("Too many requests, please wait before trying again.")
            }
            if (isInsertingLyrics && insertingLyrics.first == lyricsBody.videoId) {
                throw IllegalStateException("Already inserting lyrics, please wait until the current operation is complete.")
            }
            insertingLyrics = lyricsBody.videoId to true
            val hmacTimestamp =
                hmacService.getMacTimestampPair(
                    HmacUri.BASE_HMAC_URI,
                )
            lyricsService.insertLyrics(lyricsBody, hmacTimestamp).bodyOrThrow<LyricsResponse>()
        }

    suspend fun insertTranslatedLyrics(translatedLyricsBody: TranslatedLyricsBody): Result<TranslatedLyricsResponse> =
        runCatching {
            if (translatedLyricsBody.language.length != 2) {
                throw IllegalArgumentException("Language code must be a 2-letter code")
            }
            if (isInsertingTranslatedLyrics && insertingTranslatedLyrics.first == translatedLyricsBody.videoId) {
                throw IllegalStateException("Already inserting translated lyrics, please wait until the current operation is complete.")
            }
            insertingTranslatedLyrics = translatedLyricsBody.videoId to true
            val hmacTimestamp =
                hmacService.getMacTimestampPair(
                    HmacUri.TRANSLATED_HMAC_URI,
                )
            lyricsService.insertTranslatedLyrics(translatedLyricsBody, hmacTimestamp).bodyOrThrow<TranslatedLyricsResponse>()
        }

    suspend fun voteLyrics(
        lyricsId: String,
        upvote: Boolean,
    ): Result<LyricsResponse> =
        runCatching {
            val hmacTimestamp =
                hmacService.getMacTimestampPair(
                    HmacUri.VOTE_HMAC_URI,
                )
            lyricsService.voteLyrics(lyricsId, upvote, hmacTimestamp).bodyOrThrow<LyricsResponse>()
        }

    suspend fun voteTranslatedLyrics(
        translatedLyricsId: String,
        upvote: Boolean,
    ): Result<TranslatedLyricsResponse> =
        runCatching {
            val hmacTimestamp =
                hmacService.getMacTimestampPair(
                    HmacUri.VOTE_TRANSLATED_HMAC_URI,
                )
            lyricsService.voteTranslatedLyrics(translatedLyricsId, upvote, hmacTimestamp).bodyOrThrow<TranslatedLyricsResponse>()
        }

    suspend fun searchLrclibLyrics(
        q_track: String,
        q_artist: String,
        duration: Int?,
    ) = runCatching {
        val rs =
            lyricsService
                .searchLrclibLyrics(
                    q_track = q_track,
                    q_artist = q_artist,
                ).body<List<LrclibObject>>()
        val lrclibObject: LrclibObject? =
            if (duration != null) {
                rs.find { abs(it.duration.toInt() - duration) <= 10 }
            } else {
                rs.firstOrNull()
            }
        if (lrclibObject != null) {
            val syncedLyrics = lrclibObject.syncedLyrics
            val plainLyrics = lrclibObject.plainLyrics
            if (!syncedLyrics.isNullOrEmpty()) {
                parseSyncedLyrics(syncedLyrics)
            } else if (!plainLyrics.isNullOrEmpty()) {
                parseUnsyncedLyrics(plainLyrics)
            } else {
                null
            }
        } else {
            null
        }
    }

    suspend fun searchBetterLyrics(
        q_track: String,
        q_artist: String,
        durationSeconds: Int?,
    ) = runCatching {
        val rs =
            lyricsService
                .searchBetterLyrics(
                    q_track = q_track,
                    q_artist = q_artist,
                    durationSeconds = durationSeconds,
                ).body<BetterLyricsResponse>()
        rs.ttml
    }

    suspend fun searchBetterLyricsPortato(
        q_track: String,
        q_artist: String,
        durationSeconds: Int?,
    ): Result<org.simpmusic.lyrics.domain.Lyrics?> =
        runCatching {
            val response = lyricsService.searchBetterLyricsPortato(q_track, q_artist, durationSeconds)
            if (response.status.value !in 200..299) {
                Logger.w(TAG, "BetterLyrics Portato returned HTTP ${response.status.value}")
                return@runCatching null
            }
            val qrc = response.body<JsonObject>()["lyrics"]?.jsonPrimitive?.contentOrNull ?: return@runCatching null
            parseQrcLyrics(qrc, q_track, q_artist).takeIf { !it.lyrics?.lines.isNullOrEmpty() }
        }

    suspend fun searchKugouLyrics(
        q_track: String,
        q_artist: String,
        durationSeconds: Int?,
    ): Result<org.simpmusic.lyrics.domain.Lyrics?> =
        runCatching {
            val lrc = lyricsService.searchKugouLyrics(q_track, q_artist, durationSeconds)
            if (lrc.isNullOrBlank()) {
                Logger.w(TAG, "KuGou found no matching lyric candidate")
                return@runCatching null
            }
            parseSyncedLyrics(lrc).takeIf { !it.lyrics?.lines.isNullOrEmpty() }
                ?: run {
                    Logger.w(TAG, "KuGou returned LRC that could not be parsed")
                    null
                }
        }

    suspend fun searchYouLyPlusLyrics(
        q_track: String,
        q_artist: String,
        album: String?,
        durationSeconds: Int?,
    ): Result<org.simpmusic.lyrics.domain.Lyrics?> =
        runCatching {
            val mirrors =
                listOf(
                    "https://lyricsplus.binimum.org",
                    "https://lyricsplus.prjktla.my.id",
                    "https://lyricsplus.prjktla.workers.dev",
                    "https://lyricsplus.atomix.one",
                    "https://lyricsplus-seven.vercel.app",
                )
            var bestLyrics: org.simpmusic.lyrics.domain.Lyrics? = null
            // Search every TTML mirror before the LRC endpoint so an early line-synced result
            // cannot hide word-timed lyrics available from a different mirror.
            for (mirror in mirrors) {
                val ttmlLyrics =
                    runCatching {
                        val response = lyricsService.searchLyricsPlusTtml(mirror, q_track, q_artist, album, durationSeconds)
                        if (response.status.value in 200..299) {
                            val body = response.bodyAsText()
                            val ttml =
                                if (body.trimStart().startsWith("<")) {
                                    body
                                } else {
                                    runCatching {
                                        Json.parseToJsonElement(body).jsonObject["ttml"]?.jsonPrimitive?.contentOrNull
                                    }.getOrNull()
                                }
                            ttml?.let(::parseTtmlLyrics)?.takeIf { !it.lyrics?.lines.isNullOrEmpty() }
                        } else {
                            null
                        }
                    }.getOrNull()
                if (ttmlLyrics != null) {
                    if (ttmlLyrics.lyrics?.syncType.equals("RICH_SYNCED", ignoreCase = true)) return@runCatching ttmlLyrics
                    if (bestLyrics == null) bestLyrics = ttmlLyrics
                }
            }

            for (mirror in mirrors) {
                val candidate =
                    runCatching {
                        val response = lyricsService.searchLyricsPlus(mirror, q_track, q_artist, album, durationSeconds)
                        if (response.status.value in 200..299) {
                            parseLyricsPlusLyrics(response.bodyAsText())
                        } else {
                            null
                        }
                    }.getOrNull()
                if (!candidate?.lyrics?.lines.isNullOrEmpty()) {
                    if (candidate.lyrics?.syncType.equals("RICH_SYNCED", ignoreCase = true)) return@runCatching candidate
                    if (bestLyrics == null) bestLyrics = candidate
                }
            }
            if (bestLyrics == null) Logger.w(TAG, "YouLyPlus found no lyrics on its TTML or LRC endpoints")
            bestLyrics
        }

    suspend fun searchUnisonLyrics(
        videoId: String,
        q_track: String,
        q_artist: String,
        album: String?,
        durationSeconds: Int?,
    ): Result<org.simpmusic.lyrics.domain.Lyrics?> =
        runCatching {
            val searchResponse = lyricsService.searchUnisonLyrics(q_track, q_artist, album, durationSeconds)
            if (searchResponse.status.value in 200..299) {
                val searchBody = searchResponse.bodyAsText()
                val entries =
                    runCatching {
                        Json.parseToJsonElement(searchBody).jsonObject["data"]?.jsonArray
                    }.getOrNull().orEmpty()

                for (entry in entries) {
                    val summary = runCatching { entry.jsonObject }.getOrNull() ?: continue
                    parseUnisonLyrics(summary)?.let { return@runCatching it }

                    val video = summary["videoId"]?.jsonPrimitive?.contentOrNull
                    val id = summary["id"]?.jsonPrimitive?.contentOrNull
                    val detailResponse =
                        when {
                            !video.isNullOrBlank() -> lyricsService.searchUnisonLyricsByVideoId(video)
                            !id.isNullOrBlank() -> lyricsService.searchUnisonLyricsById(id)
                            else -> null
                        } ?: continue
                    if (detailResponse.status.value !in 200..299) continue
                    parseUnisonResponse(detailResponse.bodyAsText())?.let { return@runCatching it }
                }
            } else {
                Logger.w(TAG, "Unison metadata search returned HTTP ${searchResponse.status.value}")
            }

            if (videoId.isNotBlank()) {
                val videoResponse = lyricsService.searchUnisonLyricsByVideoId(videoId)
                if (videoResponse.status.value in 200..299) {
                    parseUnisonResponse(videoResponse.bodyAsText()).also { result ->
                        if (result == null) Logger.w(TAG, "Unison returned no lyrics for the selected track")
                    }
                } else {
                    Logger.w(TAG, "Unison video ID lookup returned HTTP ${videoResponse.status.value}")
                    null
                }
            } else {
                Logger.w(TAG, "Unison found no lyrics for the selected track")
                null
            }
        }

    private fun parseUnisonResponse(body: String): org.simpmusic.lyrics.domain.Lyrics? {
        val root = runCatching { Json.parseToJsonElement(body).jsonObject }.getOrNull() ?: return null
        val payload = root["data"]?.let { runCatching { it.jsonObject }.getOrNull() } ?: root
        return parseUnisonLyrics(payload)
    }

    private fun parseUnisonLyrics(payload: JsonObject): org.simpmusic.lyrics.domain.Lyrics? {
        val text = payload["lyrics"]?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() } ?: return null
        val format = payload["format"]?.jsonPrimitive?.contentOrNull.orEmpty()
        val syncType = payload["syncType"]?.jsonPrimitive?.contentOrNull.orEmpty()
        val parsed =
            when {
                format.equals("ttml", ignoreCase = true) -> parseTtmlLyrics(text)
                format.equals("plain", ignoreCase = true) || format.equals("text", ignoreCase = true) -> parseUnsyncedLyrics(text)
                syncType.equals("richsync", ignoreCase = true) ||
                    syncType.equals("word_synced", ignoreCase = true) ||
                    syncType.equals("syllable_synced", ignoreCase = true) -> parseRichSyncLyrics(text)
                else -> parseSyncedLyrics(text)
            }
        return parsed.takeIf { !it.lyrics?.lines.isNullOrEmpty() }
    }

    suspend fun searchAMArtist(
        name: String,
        limit: Int = 5,
    ): Result<List<AMArtistResource>> =
        runCatching {
            val response = lyricsService.searchAMArtist(name, limit)
            if (response.status.value !in 200..299) {
                throw Exception("AM search failed: ${response.status.value}")
            }
            val parsed = response.body<AMSearchResponse>()
            val resources = parsed.resources?.artists.orEmpty()
            // Keep the search ranking from results.data; fall back to the resources map order.
            parsed.results
                ?.artists
                ?.data
                ?.mapNotNull { resources[it.id] }
                ?: resources.values.toList()
        }

    /**
     * Fetch a single artist by id, including [AMEditorialArtwork] (name-logo PNG) and keyColor.
     * Returns null when the id is not present in the response.
     */
    suspend fun getAMArtist(id: String): Result<AMArtistResource?> =
        runCatching {
            val response = lyricsService.getAMArtist(id)
            if (response.status.value !in 200..299) {
                throw Exception("AM artist fetch failed: ${response.status.value}")
            }
            response
                .body<AMSearchResponse>()
                .resources
                ?.artists
                ?.get(id)
        }

    /**
     * Search the AM catalog for albums, ranked the way AM ranked them. Each result already carries
     * its animated artwork when it has one, so the caller needs no follow-up request.
     */
    suspend fun searchAMAlbum(
        query: String,
        limit: Int = 5,
    ): Result<List<AMAlbumResource>> =
        runCatching {
            val response = lyricsService.searchAMAlbum(query, limit)
            if (response.status.value !in 200..299) {
                throw Exception("AM album search failed: ${response.status.value}")
            }
            val parsed = response.body<AMSearchResponse>()
            val resources = parsed.resources?.albums.orEmpty()
            // Keep the search ranking from results.data; fall back to the resources map order.
            parsed.results
                ?.albums
                ?.data
                ?.mapNotNull { resources[it.id] }
                ?: resources.values.toList()
        }

    /**
     * Tracks reached by title, each paired with the album it appears on.
     *
     * The pairing comes from `relationships.albums`, never from the flat `resources.albums` map:
     * that map holds every album any hit belongs to, so reading an album out of it directly
     * attaches whichever release happens to carry artwork to whatever track was searched for.
     * Order follows the search ranking; the caller still decides which hit is the right track.
     */
    suspend fun searchAMSongsWithAlbums(
        query: String,
        limit: Int = 5,
    ): Result<List<AMSongWithAlbum>> =
        runCatching {
            val response = lyricsService.searchAMSongs(query, limit)
            if (response.status.value !in 200..299) {
                throw Exception("AM song search failed: ${response.status.value}")
            }
            val parsed = response.body<AMSearchResponse>()
            val songs = parsed.resources?.songs.orEmpty()
            val albums = parsed.resources?.albums.orEmpty()
            val ranked =
                parsed.results
                    ?.songs
                    ?.data
                    ?.mapNotNull { songs[it.id] }
                    ?: songs.values.toList()
            ranked.mapNotNull { song ->
                val albumId =
                    song.relationships
                        ?.albums
                        ?.data
                        ?.firstOrNull()
                        ?.id
                val album = albumId?.let { albums[it] } ?: return@mapNotNull null
                AMSongWithAlbum(
                    songName = song.attributes?.name,
                    artistName = song.attributes?.artistName,
                    durationInMillis = song.attributes?.durationInMillis,
                    album = album,
                )
            }
        }

    /**
     * Resolve an animated-artwork master playlist down to the single rendition worth playing, and
     * return its media-playlist url.
     *
     * Doing this here rather than leaving it to the player is what makes the two platforms agree:
     * mpv defaults to `--hls-bitrate=max` and would take the top of the ladder — on a measured
     * artwork that is 2048x2732 10-bit HEVC at 20 Mbps, 52 MB for a 22-second loop — while
     * ExoPlayer would pick by its own bandwidth estimate. Handing both a chosen rendition removes
     * the question. Returns null when the url is not a master playlist.
     */
    suspend fun selectAMRendition(
        masterUrl: String,
        minWidth: Int,
    ): Result<String?> =
        runCatching {
            val response = lyricsService.fetchAMHlsPlaylist(masterUrl)
            if (response.status.value !in 200..299) {
                throw Exception("AM playlist fetch failed: ${response.status.value}")
            }
            parseAMHlsVariants(response.bodyAsText(), masterUrl)
                .pickAMRendition(minWidth)
                ?.uri
        }

    private suspend inline fun <reified T> HttpResponse.bodyOrThrow(): T {
        if (this.status.value == 429) {
            tooManyRequest = true
            Logger.e(TAG, "Too many requests: ${this.status.value}")
        } else {
            tooManyRequest = false
        }
        try {
            val data = body<BaseResponse<T>>()
            if (data.error != null) {
                val error = data.error
                Logger.e(TAG, "Error response: ${error.reason} (code: ${error.code})")
                throw Exception("Error response: ${error.reason} (code: ${error.code})")
            }
            return data.data ?: throw Exception("Response data is null")
        } catch (e: Exception) {
            throw e
        }
    }
}
