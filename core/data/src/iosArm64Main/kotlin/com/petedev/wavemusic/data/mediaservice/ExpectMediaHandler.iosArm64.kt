package com.petedev.wavemusic.data.mediaservice

actual fun createMediaServiceHandler(
    dataStoreManager: com.petedev.wavemusic.domain.manager.DataStoreManager,
    songRepository: com.petedev.wavemusic.domain.repository.SongRepository,
    streamRepository: com.petedev.wavemusic.domain.repository.StreamRepository,
    localPlaylistRepository: com.petedev.wavemusic.domain.repository.LocalPlaylistRepository,
    analyticsRepository: com.petedev.wavemusic.domain.repository.AnalyticsRepository,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
): com.petedev.wavemusic.domain.mediaservice.handler.MediaPlayerHandler {
    TODO("Not yet implemented")
}