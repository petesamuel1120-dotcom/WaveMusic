package com.maxrave.media3.service.mediasourcefactory

import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.drm.DrmSessionManagerProvider
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.MergingMediaSource
import androidx.media3.exoplayer.upstream.LoadErrorHandlingPolicy
import com.maxrave.common.MERGING_DATA_TYPE
import com.maxrave.logger.Logger

@UnstableApi
internal class MergingMediaSourceFactory(
    private val defaultMediaSourceFactory: DefaultMediaSourceFactory,
    private val liveMediaSourceFactory: DefaultMediaSourceFactory = defaultMediaSourceFactory,
) : MediaSource.Factory {
    override fun setDrmSessionManagerProvider(drmSessionManagerProvider: DrmSessionManagerProvider): MediaSource.Factory {
        defaultMediaSourceFactory.setDrmSessionManagerProvider(drmSessionManagerProvider)
        liveMediaSourceFactory.setDrmSessionManagerProvider(drmSessionManagerProvider)
        return this
    }

    override fun setLoadErrorHandlingPolicy(loadErrorHandlingPolicy: LoadErrorHandlingPolicy): MediaSource.Factory {
        defaultMediaSourceFactory.setLoadErrorHandlingPolicy(loadErrorHandlingPolicy)
        liveMediaSourceFactory.setLoadErrorHandlingPolicy(loadErrorHandlingPolicy)
        return this
    }

    override fun getSupportedTypes(): IntArray = defaultMediaSourceFactory.supportedTypes

    override fun createMediaSource(mediaItem: MediaItem): MediaSource {
        Logger.w("Merging Media Source", mediaItem.mediaMetadata.description.toString())
        val description = mediaItem.mediaMetadata.description?.toString().orEmpty()
        val isLiveVideo = description.contains(MERGING_DATA_TYPE.LIVE_VIDEO)
        val isVideo = description.contains(MERGING_DATA_TYPE.VIDEO)
        if (isLiveVideo) {
            // Live HLS is already muxed and has a moving timeline. Merging the same live program
            // into independent audio/video children leaves ExoPlayer waiting on incompatible live
            // timelines, so let HlsMediaSource expose both tracks from one source. The app's media
            // URI is normally just a YouTube id, so provide an HLS-looking URI for factory
            // selection; ResolvingDataSource replaces it with the actual manifest before opening.
            val hlsMediaItem = mediaItem.buildUpon().setUri("${mediaItem.mediaId}.m3u8").build()
            return liveMediaSourceFactory.createMediaSource(hlsMediaItem)
        }
        if (isVideo) {
            // Always resolve the adaptive video stream alongside its audio stream. Gating this on
            // the retired preference left video items playing audio and made the UI show the poster.
            val videoItem =
                mediaItem
                    .buildUpon()
                    .setMediaId("${MERGING_DATA_TYPE.VIDEO}${mediaItem.mediaId}")
                    .setCustomCacheKey("${MERGING_DATA_TYPE.VIDEO}${mediaItem.mediaId}")
                    .build()
            return MergingMediaSource(
                defaultMediaSourceFactory.createMediaSource(videoItem),
                defaultMediaSourceFactory.createMediaSource(mediaItem),
            )
        } else {
            return defaultMediaSourceFactory.createMediaSource(mediaItem)
        }
    }
}
