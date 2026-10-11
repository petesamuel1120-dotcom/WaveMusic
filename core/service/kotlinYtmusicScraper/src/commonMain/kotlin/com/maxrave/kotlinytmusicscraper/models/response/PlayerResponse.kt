package com.maxrave.kotlinytmusicscraper.models.response

import com.maxrave.kotlinytmusicscraper.models.ResponseContext
import com.maxrave.kotlinytmusicscraper.models.Thumbnails
import com.maxrave.kotlinytmusicscraper.models.youtube.YouTubeInitialPage
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * PlayerResponse with [com.zionhuang.innertube.models.YouTubeClient.ANDROID_MUSIC] client
 */
@Serializable
data class PlayerResponse(
    val responseContext: ResponseContext,
    val playabilityStatus: PlayabilityStatus,
    val playerConfig: PlayerConfig? = null,
    val streamingData: StreamingData? = null,
    val videoDetails: VideoDetails? = null,
    @SerialName("playbackTracking")
    val playbackTracking: PlaybackTracking? = null,
    @SerialName("captions")
    val captions: YouTubeInitialPage.Captions? = null,
) {
    @Serializable
    data class PlayabilityStatus(
        val status: String,
        val reason: String? = null,
    )

    @Serializable
    data class PlayerConfig(
        val audioConfig: AudioConfig,
    ) {
        @Serializable
        data class AudioConfig(
            val loudnessDb: Double?,
            val perceptualLoudnessDb: Double?,
        )
    }

    @Serializable
    data class StreamingData(
        val hlsManifestUrl: String? = null,
        val formats: List<Format>? = null,
        val adaptiveFormats: List<Format> = emptyList(),
        val expiresInSeconds: Int = 0,
        val serverAbrStreamingUrl: String? = null,
    ) {
        @Serializable
        data class Format(
            val itag: Int,
            val url: String?,
            val mimeType: String,
            val bitrate: Int,
            val width: Int?,
            val height: Int?,
            val contentLength: Long?,
            val quality: String,
            val fps: Int?,
            val qualityLabel: String?,
            val averageBitrate: Int?,
            val audioQuality: String?,
            val approxDurationMs: String?,
            val audioSampleRate: Int?,
            val audioChannels: Int?,
            val loudnessDb: Double?,
            val lastModified: Long?,
            val signatureCipher: String?,
        ) {
            val isAudio: Boolean
                get() = width == null
        }
    }

    @Serializable
    data class VideoDetails(
        val videoId: String = "",
        val title: String? = null,
        val author: String? = null,
        val channelId: String = "",
        val authorAvatar: String? = null,
        val authorSubCount: String? = null,
        val lengthSeconds: String? = null,
        val musicVideoType: String? = null,
        val viewCount: String? = null,
        val thumbnail: Thumbnails? = null,
        val description: String? = null,
        val isLiveContent: Boolean? = null,
    )

    @Serializable
    data class PlaybackTracking(
        @SerialName("videostatsPlaybackUrl")
        val videostatsPlaybackUrl: VideostatsPlaybackUrl?,
        @SerialName("videostatsWatchtimeUrl")
        val videostatsWatchtimeUrl: VideostatsWatchtimeUrl?,
        @SerialName("atrUrl")
        val atrUrl: AtrUrl?,
    ) {
        @Serializable
        data class VideostatsPlaybackUrl(
            @SerialName("baseUrl")
            val baseUrl: String?,
        )

        @Serializable
        data class VideostatsWatchtimeUrl(
            @SerialName("baseUrl")
            val baseUrl: String?,
        )

        @Serializable
        data class AtrUrl(
            @SerialName("baseUrl")
            val baseUrl: String?,
        )
    }
}
