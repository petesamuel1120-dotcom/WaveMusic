package com.petedev.wavemusic.data.mediaservice

import com.petedev.wavemusic.domain.repository.AnalyticsRepository

actual fun createMediaServiceHandler(
    dataStoreManager: com.petedev.wavemusic.domain.manager.DataStoreManager,
    songRepository: com.petedev.wavemusic.domain.repository.SongRepository,
    streamRepository: com.petedev.wavemusic.domain.repository.StreamRepository,
    localPlaylistRepository: com.petedev.wavemusic.domain.repository.LocalPlaylistRepository,
    analyticsRepository: AnalyticsRepository,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
): com.petedev.wavemusic.domain.mediaservice.handler.MediaPlayerHandler =
    MediaServiceHandlerImpl(
        dataStoreManager,
        songRepository,
        streamRepository,
        localPlaylistRepository,
        analyticsRepository,
        coroutineScope,
    )