package org.simpmusic.lyrics.am

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Scrapes the short-lived web-player bearer token (JWT) from the public web bundle — no login
 * required. The token is cached until a caller reports it expired via [clearToken]. The
 * [HttpClient] is passed in per call so this reuses the same client as the rest of the service.
 */
internal class AMTokenManager {
    private var cachedToken: String? = null
    private val mutex = Mutex()

    suspend fun getToken(client: HttpClient): String =
        mutex.withLock {
            cachedToken?.let { return@withLock it }
            scrapeToken(client).also { cachedToken = it }
        }

    fun clearToken() {
        cachedToken = null
    }

    private suspend fun scrapeToken(client: HttpClient): String {
        val home = client.get(HOME_URL).bodyAsText()
        val bundlePath =
            INDEX_JS_REGEX.find(home)?.value
                ?: error("AM: web bundle URL not found")
        val bundle = client.get(HOME_URL + bundlePath).bodyAsText()

        // Match the full 3-segment JWT; the header field order rotates between rotations so a
        // prefix match (e.g. "eyJh") would break. The bundle embeds several JWTs, so prefer the
        // web-play issuer over a blind first match.
        val candidates = JWT_REGEX.findAll(bundle).map { it.value }.toList()
        return candidates.firstOrNull { it.isWebPlayToken() }
            ?: candidates.firstOrNull()
            ?: error("AM: no token found in bundle")
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun String.isWebPlayToken(): Boolean =
        runCatching {
            val payload = split(".").getOrNull(1) ?: return false
            Base64.UrlSafe
                .decode(payload.restorePadding())
                .decodeToString()
                .contains("\"iss\":\"AMPWebPlay\"")
        }.getOrDefault(false)

    // The base64url payload of a JWT is unpadded; the decoder needs the '=' padding restored.
    private fun String.restorePadding(): String {
        val remainder = length % 4
        return if (remainder == 0) this else this + "=".repeat(4 - remainder)
    }

    private companion object {
        const val HOME_URL = "https://music.apple.com"
        val INDEX_JS_REGEX = Regex("""/assets/index~[^/]+\.js""")
        val JWT_REGEX = Regex("""eyJ[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+""")
    }
}
