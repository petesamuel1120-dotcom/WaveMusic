package org.simpmusic.lyrics

import com.maxrave.ktorext.curl.CurlLogger
import com.maxrave.ktorext.encoding.brotli
import com.maxrave.ktorext.getEngine
import com.maxrave.logger.Logger
import io.ktor.client.HttpClient
import io.ktor.client.engine.ProxyConfig
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.compression.ContentEncoding
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.AcceptAllCookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.headers
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import org.simpmusic.lyrics.am.AMTokenManager
import org.simpmusic.lyrics.models.request.LyricsBody
import org.simpmusic.lyrics.models.request.TranslatedLyricsBody
import org.simpmusic.lyrics.models.request.VoteBody

class SimpMusicLyrics {
    private var httpClient = createClient()
    var proxy: ProxyConfig? = null
        set(value) {
            field = value
            httpClient.close()
            httpClient = createClient()
        }

    private val baseUrl = "https://api-lyrics.simpmusic.org/v1/"

    private val amTokenManager = AMTokenManager()

    private fun createClient() =
        HttpClient(getEngine()) {
            expectSuccess = false
            followRedirects = true
            install(HttpCache)
            install(CurlLogger) {
                logger = { Logger.d("SimpMusicLyrics", it) }
            }
            install(HttpSend) {
                maxSendCount = 100
            }
            install(HttpCookies) {
                storage = AcceptAllCookiesStorage()
            }
            install(ContentNegotiation) {
                json(
                    Json {
                        prettyPrint = true
                        isLenient = true
                        ignoreUnknownKeys = true
                        explicitNulls = false
                        encodeDefaults = true
                    },
                )
            }
            install(ContentEncoding) {
                brotli(1.0F)
                gzip(0.9F)
                deflate(0.8F)
            }
            defaultRequest {
                url("https://api-lyrics.simpmusic.org/v1")
            }
            if (proxy != null) {
                engine {
                    proxy = this@SimpMusicLyrics.proxy
                }
            }
        }

    private fun HttpRequestBuilder.buildDefaultHeaders(
        timestamp: String? = null,
        hmac: String? = null,
    ) {
        headers {
            header(HttpHeaders.Accept, "application/json")
            header(HttpHeaders.UserAgent, "SimpMusicLyrics/1.0")
            header(HttpHeaders.ContentType, "application/json")
            timestamp?.let {
                header("X-Timestamp", it)
            }
            hmac?.let {
                header("X-HMAC", it)
            }
        }
    }

    private fun HttpRequestBuilder.buildAMHeaders(token: String) {
        headers {
            header("Authorization", "Bearer $token")
            header("Origin", "https://music.apple.com")
            header("Referer", "https://music.apple.com/")
            header(
                HttpHeaders.UserAgent,
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:152.0) Gecko/20100101 Firefox/152.0",
            )
            header(HttpHeaders.Accept, "application/json")
        }
    }

    suspend fun findLyricsByVideoId(videoId: String) =
        httpClient.get(baseUrl + videoId) {
            buildDefaultHeaders()
        }

    suspend fun findTranslatedLyrics(
        videoId: String,
        language: String,
    ) = httpClient.get(baseUrl + "translated/$videoId/$language") {
        buildDefaultHeaders()
    }

    suspend fun insertLyrics(
        lyricsBody: LyricsBody,
        hmacTimestamp: Pair<String, String>,
    ) = httpClient.post {
        buildDefaultHeaders(
            timestamp = hmacTimestamp.second,
            hmac = hmacTimestamp.first,
        )
        setBody(lyricsBody)
    }

    suspend fun insertTranslatedLyrics(
        translatedLyricsBody: TranslatedLyricsBody,
        hmacTimestamp: Pair<String, String>,
    ) = httpClient.post(baseUrl + "translated") {
        buildDefaultHeaders(
            timestamp = hmacTimestamp.second,
            hmac = hmacTimestamp.first,
        )
        setBody(translatedLyricsBody)
    }

    suspend fun voteLyrics(
        id: String,
        upvote: Boolean,
        hmacTimestamp: Pair<String, String>,
    ) = httpClient.post(baseUrl + "vote") {
        buildDefaultHeaders(
            timestamp = hmacTimestamp.second,
            hmac = hmacTimestamp.first,
        )
        setBody(
            VoteBody(
                id = id,
                vote = if (upvote) 1 else 0, // 1 for upvote, 0 for downvote
            ),
        )
    }

    suspend fun voteTranslatedLyrics(
        id: String,
        upvote: Boolean,
        hmacTimestamp: Pair<String, String>,
    ) = httpClient.post(baseUrl + "translated/vote") {
        buildDefaultHeaders(
            timestamp = hmacTimestamp.second,
            hmac = hmacTimestamp.first,
        )
        setBody(
            VoteBody(
                id = id,
                vote = if (upvote) 1 else 0, // 1 for upvote, 0 for downvote
            ),
        )
    }

    suspend fun searchLrclibLyrics(
        q_track: String,
        q_artist: String,
    ) = httpClient.get("https://lrclib.net/api/search") {
        buildDefaultHeaders()
        parameter("q", "$q_artist $q_track")
    }

    suspend fun searchBetterLyrics(
        q_track: String,
        q_artist: String,
        durationSeconds: Int?,
    ) = httpClient.get("https://lyrics-api.boidu.dev/getLyrics") {
        buildDefaultHeaders()
        parameter("s", q_track)
        parameter("a", q_artist)
        durationSeconds?.let {
            parameter("d", it)
        }
    }

    suspend fun searchBetterLyricsPortato(
        q_track: String,
        q_artist: String,
        durationSeconds: Int?,
    ) = httpClient.get("https://lyrics-api.boidu.dev/qq/getLyrics") {
        buildDefaultHeaders()
        parameter("s", q_track)
        parameter("a", q_artist)
        durationSeconds?.let { parameter("d", it) }
    }

    /**
     * Fetch KuGou LRC directly. The KuGou route on the BetterLyrics proxy requires a private
     * API key for uncached searches, so use KuGou's public mobile search and lyrics endpoints.
     *
     * ArchiveTune (2026)
     * © Rukamori — github.com/rukamori
     * GPL-3.0 License | Contributors: see git history
     * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
     * Adapted request flow from https://github.com/rukamori/lyrics
     */
    suspend fun searchKugouLyrics(
        q_track: String,
        q_artist: String,
        durationSeconds: Int?,
    ): String? {
        val songSearch =
            httpClient
                .get("https://mobileservice.kugou.com/api/v3/search/song") {
                    buildDefaultHeaders()
                    parameter("version", 9108)
                    parameter("plat", 0)
                    parameter("pagesize", 8)
                    parameter("showtype", 0)
                    parameter("keyword", "$q_track - $q_artist")
                }.bodyAsText()
        val songs =
            runCatching {
                Json.parseToJsonElement(songSearch)
                    .jsonObject["data"]
                    ?.jsonObject
                    ?.get("info")
                    ?.jsonArray
                    .orEmpty()
            }.getOrDefault(emptyList())

        for (song in songs) {
            val songObject = runCatching { song.jsonObject }.getOrNull() ?: continue
            val hash = songObject["hash"]?.jsonPrimitive?.contentOrNull ?: continue
            val songDuration = songObject["duration"]?.jsonPrimitive?.contentOrNull?.toIntOrNull()
            if (durationSeconds != null && songDuration != null && kotlin.math.abs(songDuration - durationSeconds) > 8) {
                continue
            }

            val candidate = runCatching { searchKugouLyricsCandidate(hash = hash) }.getOrNull() ?: continue
            val lyrics = runCatching { downloadKugouLyrics(candidate.first, candidate.second) }.getOrNull()
            if (!lyrics.isNullOrBlank()) return lyrics
        }

        val keywordCandidate =
            runCatching {
                searchKugouLyricsCandidate(
                    keyword = "$q_track - $q_artist",
                    durationMillis = durationSeconds?.times(1000),
                )
            }.getOrNull() ?: return null
        return runCatching { downloadKugouLyrics(keywordCandidate.first, keywordCandidate.second) }.getOrNull()
    }

    private suspend fun searchKugouLyricsCandidate(
        hash: String? = null,
        keyword: String? = null,
        durationMillis: Int? = null,
    ): Pair<Long, String>? {
        val body =
            httpClient
                .get("https://lyrics.kugou.com/search") {
                    buildDefaultHeaders()
                    parameter("ver", 1)
                    parameter("man", "yes")
                    parameter("client", "pc")
                    hash?.let { parameter("hash", it) }
                    keyword?.let { parameter("keyword", it) }
                    durationMillis?.let { parameter("duration", it) }
                }.bodyAsText()
        val candidate =
            runCatching {
                Json.parseToJsonElement(body)
                    .jsonObject["candidates"]
                    ?.jsonArray
                    ?.firstOrNull()
                    ?.jsonObject
            }.getOrNull() ?: return null
        val id = candidate["id"]?.jsonPrimitive?.longOrNull ?: return null
        val accessKey = candidate["accesskey"]?.jsonPrimitive?.contentOrNull ?: return null
        return id to accessKey
    }

    @OptIn(ExperimentalEncodingApi::class)
    private suspend fun downloadKugouLyrics(
        id: Long,
        accessKey: String,
    ): String? {
        val body =
            httpClient
                .get("https://lyrics.kugou.com/download") {
                    buildDefaultHeaders()
                    parameter("fmt", "lrc")
                    parameter("charset", "utf8")
                    parameter("client", "pc")
                    parameter("ver", 1)
                    parameter("id", id)
                    parameter("accesskey", accessKey)
                }.bodyAsText()
        return runCatching {
            val content = Json.parseToJsonElement(body).jsonObject["content"]?.jsonPrimitive?.contentOrNull
            content?.let { Base64.Default.decode(it).decodeToString() }
        }.getOrNull()
    }

    suspend fun searchLyricsPlus(
        baseUrl: String,
        q_track: String,
        q_artist: String,
        album: String?,
        durationSeconds: Int?,
    ) = httpClient.get("$baseUrl/v2/lyrics/get") {
        buildDefaultHeaders()
        header("X-Client-Package", "SimpMusic <https://github.com/maxrave-dev/SimpMusic>")
        parameter("title", q_track)
        parameter("artist", q_artist)
        album?.takeIf { it.isNotBlank() }?.let { parameter("album", it) }
        durationSeconds?.let { parameter("duration", it) }
        parameter("source", "apple,lyricsplus,musixmatch,spotify,musixmatch-word,qq")
    }

    suspend fun searchLyricsPlusTtml(
        baseUrl: String,
        q_track: String,
        q_artist: String,
        album: String?,
        durationSeconds: Int?,
    ) = httpClient.get("$baseUrl/v1/ttml/get") {
        buildDefaultHeaders()
        header("X-Client-Package", "SimpMusic <https://github.com/maxrave-dev/SimpMusic>")
        parameter("title", q_track)
        parameter("artist", q_artist)
        album?.takeIf { it.isNotBlank() }?.let { parameter("album", it) }
        durationSeconds?.let { parameter("duration", it) }
    }

    suspend fun searchUnisonLyrics(
        q_track: String,
        q_artist: String,
        album: String?,
        durationSeconds: Int?,
    ) = httpClient.get("https://unison.boidu.dev/lyrics/search") {
        buildDefaultHeaders()
        parameter("song", q_track)
        parameter("artist", q_artist)
        album?.takeIf { it.isNotBlank() }?.let { parameter("album", it) }
        durationSeconds?.takeIf { it > 0 }?.let { parameter("duration", it) }
        parameter("limit", 5)
    }

    suspend fun searchUnisonLyricsByVideoId(videoId: String) =
        httpClient.get("https://unison.boidu.dev/lyrics") {
            buildDefaultHeaders()
            parameter("v", videoId)
        }

    suspend fun searchUnisonLyricsById(id: String) =
        httpClient.get("https://unison.boidu.dev/lyrics/$id") {
            buildDefaultHeaders()
        }

    suspend fun searchAMArtist(
        name: String,
        limit: Int,
    ): HttpResponse {
        val response = requestAMSearch(name, limit, amTokenManager.getToken(httpClient))
        // Expired bearer -> refresh the token once and retry on a fresh request.
        if (response.status.value == 401) {
            amTokenManager.clearToken()
            return requestAMSearch(name, limit, amTokenManager.getToken(httpClient))
        }
        return response
    }

    private suspend fun requestAMSearch(
        name: String,
        limit: Int,
        token: String,
    ) = httpClient.get("https://amp-api-edge.music.apple.com/v1/catalog/us/search") {
        parameter("term", name)
        parameter("types", "artists")
        parameter("fields[artists]", "url,name,artwork")
        parameter("art[url]", "f")
        parameter("format[resources]", "map")
        parameter("extend", "artistUrl")
        parameter("l", "en-US")
        parameter("limit", limit)
        parameter("platform", "web")
        buildAMHeaders(token)
    }

    suspend fun getAMArtist(id: String): HttpResponse {
        val response = requestAMArtist(id, amTokenManager.getToken(httpClient))
        // Expired bearer -> refresh the token once and retry on a fresh request.
        if (response.status.value == 401) {
            amTokenManager.clearToken()
            return requestAMArtist(id, amTokenManager.getToken(httpClient))
        }
        return response
    }

    private suspend fun requestAMArtist(
        id: String,
        token: String,
    ) = httpClient.get("https://amp-api.music.apple.com/v1/catalog/us/artists/$id") {
        parameter("art[url]", "c,f")
        parameter("extend", "editorialArtwork,hero,keyColor")
        parameter("format[resources]", "map")
        parameter("l", "en-US")
        parameter("platform", "web")
        buildAMHeaders(token)
    }

    /**
     * Search the AM catalog for an album. `extend=editorialVideo` fills the animated artwork in
     * on the search response itself, so — unlike the artist flow — no per-album follow-up request
     * is needed. Mirrors [searchAMArtist], including the refresh-once-on-401 retry.
     */
    suspend fun searchAMAlbum(
        query: String,
        limit: Int,
    ): HttpResponse {
        val response = requestAMAlbumSearch(query, limit, amTokenManager.getToken(httpClient))
        // Expired bearer -> refresh the token once and retry on a fresh request.
        if (response.status.value == 401) {
            amTokenManager.clearToken()
            return requestAMAlbumSearch(query, limit, amTokenManager.getToken(httpClient))
        }
        return response
    }

    private suspend fun requestAMAlbumSearch(
        query: String,
        limit: Int,
        token: String,
    ) = httpClient.get("https://amp-api-edge.music.apple.com/v1/catalog/us/search") {
        parameter("term", query)
        parameter("types", "albums")
        // Narrowing to the fields actually read drops ~23% off the response without losing
        // editorialVideo, and this runs once per played track.
        parameter("fields[albums]", "name,artistName,url,artwork,editorialVideo")
        parameter("format[resources]", "map")
        parameter("extend", "editorialVideo")
        parameter("l", "en-US")
        parameter("limit", limit)
        parameter("platform", "web")
        buildAMHeaders(token)
    }

    /**
     * Find the album a *track* belongs to, and bring that album's animated artwork back with it.
     *
     * Searching `types=albums` with a track title cannot do this: AM answers with the singles that
     * share the title — "Blinding Lights - Single" rather than "After Hours" — and a single never
     * carries animated artwork, so the search succeeds while the result is always empty. Asking for
     * songs and including their albums returns the parent album itself, already extended.
     */
    suspend fun searchAMSongs(
        query: String,
        limit: Int,
    ): HttpResponse {
        val response = requestAMSongSearch(query, limit, amTokenManager.getToken(httpClient))
        // Expired bearer -> refresh the token once and retry on a fresh request.
        if (response.status.value == 401) {
            amTokenManager.clearToken()
            return requestAMSongSearch(query, limit, amTokenManager.getToken(httpClient))
        }
        return response
    }

    private suspend fun requestAMSongSearch(
        query: String,
        limit: Int,
        token: String,
    ) = httpClient.get("https://amp-api-edge.music.apple.com/v1/catalog/us/search") {
        parameter("term", query)
        parameter("types", "songs")
        parameter("include[songs]", "albums")
        parameter("format[resources]", "map")
        parameter("extend", "editorialVideo")
        parameter("l", "en-US")
        parameter("limit", limit)
        parameter("platform", "web")
        buildAMHeaders(token)
    }

    /**
     * Fetch an animated-artwork playlist off Apple's media CDN. Deliberately without the AM headers:
     * this host takes no bearer, serves no DRM and needs no origin — sending them only invites the
     * edge to treat the request as something it is not.
     */
    suspend fun fetchAMHlsPlaylist(url: String): HttpResponse = httpClient.get(url)
}
