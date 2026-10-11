package com.maxrave.simpmusic.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import coil3.compose.LocalPlatformContext
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.Size
import kotlin.math.roundToInt

private val YouTubeArtworkDimensions = Regex("=w(\\d+)-h(\\d+)([^/]*)$")
private const val PlaylistCoverMinimumDimension = 1024

/** Cached songs and fresh browse rows can name the same artwork at different resolutions. */
fun String.toPlaylistCoverThumbnailUrl(): String {
    val dimensions = YouTubeArtworkDimensions.find(this) ?: return this
    val width = dimensions.groupValues[1].toIntOrNull() ?: return this
    val height = dimensions.groupValues[2].toIntOrNull() ?: return this
    if (width <= 0 || height <= 0) return this
    val scale = maxOf(1.0, PlaylistCoverMinimumDimension.toDouble() / minOf(width, height))
    val requestedWidth = (width * scale).roundToInt()
    val requestedHeight = (height * scale).roundToInt()
    return replaceRange(
        dimensions.range.first,
        dimensions.range.last + 1,
        "=w${requestedWidth}-h${requestedHeight}${dimensions.groupValues[3]}",
    )
}

@Composable
fun rememberPlaylistCoverRequest(thumbnail: String?): ImageRequest {
    val context = LocalPlatformContext.current
    val artworkUrl = thumbnail?.toPlaylistCoverThumbnailUrl()
    return remember(context, artworkUrl) {
        // Large covers must not reuse the small bitmaps decoded for song rows.
        val cacheKey = artworkUrl?.let { "playlist-cover:$it" }
        ImageRequest.Builder(context)
            .data(artworkUrl)
            .size(Size.ORIGINAL)
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCacheKey(cacheKey)
            .memoryCacheKey(cacheKey)
            .crossfade(false)
            .build()
    }
}
